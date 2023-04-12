package urza_queue;

import org.java_websocket.server.WebSocketServer;

import java.io.FileInputStream;
import java.io.IOException;
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
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {
    public static Logger logger = Logger.getLogger("Urza Queue");
    public static Connection conn;
    public final static LinkedBlockingQueue<CrawlTask> crawlTasks = new LinkedBlockingQueue<>();
    public final static Set<CrawlTask> enqueuedTasks = new HashSet<>();

    public static void main(String[] args) throws SQLException {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("logging.properties"));
        } catch (SecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        logger.log(Level.CONFIG, "Running on JVM version " + System.getProperty("java.version"));
        logger.log(Level.CONFIG, "Number of Available Processors: " + Runtime.getRuntime().availableProcessors());

        // Setup Crawl Task Queue
        conn = DriverManager.getConnection("jdbc:postgresql://host.docker.internal:32768/", "postgres", "mysecretpassword");
        List<CrawlTask> targets = fetchCrawlTargets();
        crawlTasks.addAll(targets);
        enqueuedTasks.addAll(targets);

        // Update the Database every Hour
        ScheduledExecutorService executorService;
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(Main::updateCrawlTasks, 30, 30, TimeUnit.SECONDS);

        // Expose Websocket
        String host = "172.17.0.1";
        int port = 10000;
        logger.log(Level.INFO, "Starting Server at " + host + " and port " + port);
        WebSocketServer server = new Server(new InetSocketAddress(host, port));
        server.run();
    }

    public static List<CrawlTask> fetchCrawlTargets() throws SQLException {
        List<CrawlTask> result = new ArrayList<>();
        String query = "SELECT * from \"target\"";
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        // Add results line-by-line to Set
        while (rs.next()) {
            String listViewUrl = rs.getString("list_view_url");
            String articleSelector = rs.getString("article_selector");
            String mostRecentArticleUrl = rs.getString("most_recent_article_url");
            String nextPageSelector = rs.getString("next_page_selector");
            CrawlTask task = new CrawlTask(listViewUrl, articleSelector, mostRecentArticleUrl, nextPageSelector);
            result.add(task);
        }

        return result;
    }

    public static void updateCrawlTasks() {
        logger.log(Level.INFO, "Updating Crawl Tasks");
        String query = "SELECT * from \"target\"";

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);

            // Look for missing targets
            while (rs.next()) {
                String listViewUrl = rs.getString("list_view_url");
                String articleSelector = rs.getString("article_selector");
                String mostRecentArticleUrl = rs.getString("most_recent_article_url");
                String nextPageSelector = rs.getString("next_page_selector");
                CrawlTask task = new CrawlTask(listViewUrl, articleSelector, mostRecentArticleUrl, nextPageSelector);
                if (!enqueuedTasks.contains(task)) {
                    logger.log(Level.INFO, "New crawl task, adding to queue: " + task.toString());
                    crawlTasks.put(task);
                    enqueuedTasks.add(task);
                }
            }
        } catch (SQLException | InterruptedException e) {
            logger.log(Level.SEVERE, "SQL Exception: Cannot update Crawl Tasks " + e.getMessage());
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
                String nextPageSelector = rs.getString("next_page_selector");
                task.articleSelector = articleSelector;
                task.mostRecentArticleUrl = mostRecentArticleUrl;
                task.nextPageSelector = nextPageSelector;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL Exception: Cannot update Crawl Task: " + e.getMessage());
        }
        return task;
    }
}
