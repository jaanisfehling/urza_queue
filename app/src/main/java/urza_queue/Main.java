package urza_queue;

import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static Connection conn;
    public final static LinkedBlockingQueue<CrawlTask> crawlTasks = new LinkedBlockingQueue<>();
    public final static Set<CrawlTask> enqueuedTasks = new HashSet<>();

    public static void main(String[] args) throws SQLException {
        System.out.println("Running on JVM version " + System.getProperty("java.version"));

        // Setup Crawl Task Queue
        conn = DriverManager.getConnection("jdbc:postgresql://localhost:32768/", "postgres", "mysecretpassword");
        List<CrawlTask> targets = fetchCrawlTargets();
        crawlTasks.addAll(targets);
        enqueuedTasks.addAll(targets);

        // Update the Database every Hour
        ScheduledExecutorService executorService;
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(Main::updateCrawlTargets, 30, 30, TimeUnit.SECONDS);

        // Expose Websocket
        String host = "localhost";
        int port = 8887;
        System.out.println("Starting Server at " + host + " and port " + port);
        WebSocketServer server = new Server(new InetSocketAddress(host, port));
        server.run();
    }

    public static List<CrawlTask> fetchCrawlTargets() {
        List<CrawlTask> result = new ArrayList<>();
        String query = "SELECT * from \"target\"";
        
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);

            // Add results line-by-line to Set
            while (rs.next()) {
                String listViewUrl = rs.getString("list_view_url");
                String articleSelector = rs.getString("article_selector");
                String mostRecentArticleUrl = rs.getString("most_recent_article_url");
                CrawlTask task = new CrawlTask(listViewUrl, articleSelector, mostRecentArticleUrl);
                result.add(task);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    public static void updateCrawlTargets() {
        String query = "SELECT * from \"target\"";

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);

            // Look for missing targets
            while (rs.next()) {
                String listViewUrl = rs.getString("list_view_url");
                String articleSelector = rs.getString("article_selector");
                String mostRecentArticleUrl = rs.getString("most_recent_article_url");
                CrawlTask task = new CrawlTask(listViewUrl, articleSelector, mostRecentArticleUrl);
                if (!enqueuedTasks.contains(task)) {
                    crawlTasks.put(task);
                    enqueuedTasks.add(task);
                }
            }
        } catch (SQLException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    public static CrawlTask updateCrawlTask(CrawlTask task) {
        String query = "SELECT * FROM \"target\" WHERE list_view_url=?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, task.listViewUrl);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String articleSelector = rs.getString("article_selector");
                String mostRecentArticleUrl = rs.getString("most_recent_article_url");
                task.articleSelector = articleSelector;
                task.mostRecentArticleUrl = mostRecentArticleUrl;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return task;
    }
}
