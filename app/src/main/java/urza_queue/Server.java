package urza_queue;

import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import static urza_queue.Main.enqueuedTasks;

public class Server extends WebSocketServer {
    Logger logger = Logger.getLogger("");

    public Server(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.log(Level.INFO, "New connection to " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.log(Level.INFO, "Closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        if (message.equals("INTEREST")) {
            logger.log(Level.INFO, "New interest in tasks");
            Batch batch = new Batch(conn);
            Thread t = new Thread(batch);
            t.start();
        }
        else if (!message.isEmpty()) {
            Gson gson = new Gson();
            try {
                CrawlTask task = gson.fromJson(message, CrawlTask.class);
                logger.log(Level.INFO, "Received updated Crawl Task");
                enqueuedTasks.set(enqueuedTasks.indexOf(task), task);
            }
            catch (JsonSyntaxException e) {
                logger.log(Level.SEVERE, "JSON Syntax Exception when receiving updated Crawl Task from Crawler: " + e.getMessage());
            }
        }
        else {
            logger.log(Level.INFO, "Received unknown message: " + message);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception e) {
        logger.log(Level.SEVERE, "Websocket Exception on " + ((conn != null) ? conn.getRemoteSocketAddress() : "null") + ": " + e);
    }

    @Override
    public void onStart() {
        logger.log(Level.INFO, "Server started successfully");
    }
}
