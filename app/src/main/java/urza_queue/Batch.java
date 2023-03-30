package urza_queue;

import com.google.gson.Gson;

import org.java_websocket.WebSocket;

public class Batch implements Runnable {
    final int BATCH_SIZE = 1;
    public WebSocket connectedCrawler;

    public Batch(WebSocket connectedCrawler) {
        this.connectedCrawler = connectedCrawler;
    }

    public void run() {
        // Crawler waiting for a new Crawl Task batch
        CrawlTask[] batch = new CrawlTask[BATCH_SIZE];

        synchronized (Main.crawlTasks) {
            // Fill up Array with new Crawl Tasks
            for (int i = 0; i < BATCH_SIZE; i++) {
                try {
                    batch[i] = Main.crawlTasks.take();
                } catch (InterruptedException e) {
                    System.out.println("Error when taking tasks from the queue: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }

        // Send Crawl Tasks to Crawler
        Gson gson = new Gson();
        String response = gson.toJson(batch);
        connectedCrawler.send(response);

        // Requeue the Tasks after specified seconds
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        for (int i = 0; i < BATCH_SIZE; i++) {
                            try {
                                Main.crawlTasks.put(batch[i]);
                            } catch (InterruptedException e) {
                                System.out.println("Error when putting tasks in the queue: " + e.getMessage());
                                throw new RuntimeException(e);
                            }
                        }
                    }
                },
                5000
        );
    }
}
