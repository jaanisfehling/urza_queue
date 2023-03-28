package urza_queue;

import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.LinkedHashSet;

public class Main {
    public static LinkedHashSet<CrawlTask> crawlTasks;

    public static void main(String[] args) throws SQLException {
        System.out.println("Running on JVM version " + System.getProperty("java.version"));

        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:32768/", "postgres", "mysecretpassword");
        crawlTasks = fetchCrawlTargets(conn);

        String host = "localhost";
        int port = 8887;
        System.out.println("Starting Server at " + host + " and port " + port);
        WebSocketServer server = new Server(new InetSocketAddress(host, port));
        server.run();
    }

    public static LinkedHashSet<CrawlTask> fetchCrawlTargets(Connection con) throws SQLException {
        LinkedHashSet<CrawlTask> result = new LinkedHashSet<>();


        String query = "SELECT * from \"target\"";
        try (Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String listViewURL = rs.getString("list_view_url");
                String articleSelector = rs.getString("article_selector");
                String mostRecentArticleURL = rs.getString("most_recent_article_url");
                CrawlTask task = new CrawlTask(listViewURL, articleSelector, mostRecentArticleURL);
                result.add(task);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }
}
