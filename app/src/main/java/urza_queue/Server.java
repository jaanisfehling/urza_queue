package urza_queue;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Arrays;

import org.checkerframework.checker.units.qual.C;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import com.google.gson.Gson;

public class Server extends WebSocketServer {

    public Server(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection to " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("New batch request");

        // Crawler waiting for a new Crawl Task batch
        final int BATCH_SIZE = 1;
        CrawlTask[] batch = new CrawlTask[BATCH_SIZE];

        // Fill up Array with new Crawl Tasks
        for (int i=0; i<BATCH_SIZE; i++) {
            try {
                batch[i] = Main.crawlTasks.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // Send Crawl Tasks to Crawler
        Gson gson = new Gson();
        String response = gson.toJson(batch);
        conn.send(response);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("An error occurred on connection " + conn.getRemoteSocketAddress()  + ": " + ex);
    }

    @Override
    public void onStart() {
        System.out.println("Server started successfully");
    }
}
