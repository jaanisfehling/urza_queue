package urza_queue;

import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws SQLException {
        System.out.println("Running on JVM version " + System.getProperty("java.version"));

        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/", "postgres", "mysecretpassword");
        List<CrawlTask> crawlTasks = fetchCrawlTargets(conn);

        String host = "localhost";
        int port = 8887;
        System.out.println("Starting Server at " + host + " and port " + port);
        WebSocketServer server = new Server(new InetSocketAddress(host, port));
        server.run();
    }

    public static List<CrawlTask> fetchCrawlTargets(Connection con) throws SQLException {
        List<CrawlTask> result = new LinkedList<CrawlTask>();

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
