package urza_queue;

import org.java_websocket.server.WebSocketServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

public class Main {
    public static Logger logger = Logger.getLogger("");
    public static Connection conn;
    public final static LinkedBlockingQueue<CrawlTask> crawlTasks = new LinkedBlockingQueue<>();
    public final static List<CrawlTask> enqueuedTasks = new ArrayList<>();

    public static void main(String[] args) throws SQLException {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("logging.properties"));
        } catch (SecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        logger.log(Level.INFO, "Running on JVM version " + System.getProperty("java.version"));
        logger.log(Level.INFO, "Number of Available Processors: " + Runtime.getRuntime().availableProcessors());

//        String dbUrl = System.getenv("DB_URL");
//        String dbUser = System.getenv("DB_USER");
//        String dbPassword = System.getenv("DB_PASSWORD");
//        conn = DriverManager.getConnection("jdbc:postgresql://" + dbUrl, dbUser, dbPassword);
        conn = DriverManager.getConnection("jdbc:postgresql://localhost:32768/", "postgres", "mysecretpassword");
        queryCrawlTasks();

        // Update the Crawl Tasks every once in a while
        ScheduledExecutorService executorService;
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(Main::queryCrawlTasks, 30, 30, TimeUnit.SECONDS);

        // Expose Websocket
        String host = "localhost";
        int port = 9000;
        logger.log(Level.INFO, "Starting Server at " + host + " and port " + port);
        WebSocketServer server = new Server(new InetSocketAddress(host, port));
        server.run();
    }

    private static void queryCrawlTasks() {
        logger.log(Level.INFO, "Querying the Database for Crawl Tasks");
        String query = "SELECT * from target";

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String ticker = rs.getString("ticker");
                String listViewUrl = rs.getString("list_view_url");
                String articleSelector = rs.getString("article_selector");
                String mostRecentArticleUrl = rs.getString("most_recent_article_url");
                String nextPageSelector = rs.getString("next_page_selector");
                boolean oldArticlesScraped = rs.getBoolean("old_articles_scraped");
                int maxPageDepth = rs.getInt("max_page_depth");
                CrawlTask task = new CrawlTask(ticker, listViewUrl, articleSelector, mostRecentArticleUrl, nextPageSelector, oldArticlesScraped, maxPageDepth);
                if (!enqueuedTasks.contains(task)) {
                    logger.log(Level.FINE, "Putting Task " + task + "into Queue");
                    enqueuedTasks.add(task);
                    crawlTasks.put(task);
                } else {
                    enqueuedTasks.set(enqueuedTasks.indexOf(task), task);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL Exception: Cannot query Crawl Tasks " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
