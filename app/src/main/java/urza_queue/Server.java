package urza_queue;

import java.net.InetSocketAddress;
import java.util.Arrays;

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
        // Crawler waiting for a new Crawl Task
        if (message.contains("GET")) {
             Main.waitingScrapers.add(conn);
             Main.on
        }
        // New Crawl Target
        else {
            Gson gson = new Gson();
            CrawlTask[] newTargets = gson.fromJson(message, CrawlTask[].class);
            Main.crawlTasks.addAll(Arrays.asList(newTargets));
        }
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
