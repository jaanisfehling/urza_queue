package urza_queue;

import com.google.gson.Gson;

import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

import java.util.logging.Level;

import static urza_queue.Main.*;


public class Batch implements Runnable {
    final int BATCH_SIZE = 1;
    final int WAIT_DELAY = 10_000;

    public WebSocket connectedCrawler;

    public Batch(WebSocket connectedCrawler) {
        this.connectedCrawler = connectedCrawler;
    }

    public void run() {
        // Crawler waiting for a new Crawl Task batch
        CrawlTask[] batch = new CrawlTask[BATCH_SIZE];

        // Fill up Array with new Crawl Tasks
        for (int i = 0; i < BATCH_SIZE; i++) {
            try {
                batch[i] = crawlTasks.take();
                logger.log(Level.FINE, "Taking task from queue: " + batch[i].toString());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // Send Crawl Tasks to Crawler
        Gson gson = new Gson();
        String response = gson.toJson(batch);
        logger.log(Level.INFO, "Sending Batch to Crawler " + connectedCrawler.getRemoteSocketAddress());
        try {
            connectedCrawler.send(response);
        } catch (WebsocketNotConnectedException e) {
            logger.log(Level.SEVERE, "WebsocketNotConnectedException: Crawler disconnected: " + e.getMessage());
        }

        // Requeue the Tasks after specified seconds
        try {
            Thread.sleep(WAIT_DELAY);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < BATCH_SIZE; i++) {
            try {
                batch[i] = enqueuedTasks.get(enqueuedTasks.indexOf(batch[i]));
                crawlTasks.put(batch[i]);
                logger.log(Level.FINE, "Putting task into queue: " + batch[i].toString());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
