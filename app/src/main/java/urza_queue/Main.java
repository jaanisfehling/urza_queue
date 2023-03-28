package urza_queue;

import org.java_websocket.WebSocket;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.LinkedHashSet;

public class Main {
    public static LinkedHashSet<CrawlTask> crawlTasks;
    public static LinkedHashSet<WebSocket> waitingScrapers;

    public static void main(String[] args) throws SQLException {
        System.out.println("Running on JVM version " + System.getProperty("java.version"));

        // Setup Crawl Target List
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:32768/", "postgres", "mysecretpassword");
        crawlTasks = fetchCrawlTargets(conn);

        // Setup available Scraper List
        waitingScrapers = new LinkedHashSet<WebSocket>();

        // Expose Websocket
        String host = "localhost";
        int port = 8887;
        System.out.println("Starting Server at " + host + " and port " + port);
        WebSocketServer server = new Server(new InetSocketAddress(host, port));
        server.run();
    }

    public static void onAvailableCrawler() {
        for (CrawlTask crawlTask : crawlTasks) {
            if (crawlTask.isReady()) {

            }
        }
    }

    public static LinkedHashSet<CrawlTask> fetchCrawlTargets(Connection con) {
        LinkedHashSet<CrawlTask> result = new LinkedHashSet<>();

        String query = "SELECT * from \"target\"";
        try (Statement stmt = con.createStatement()) {
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
}
