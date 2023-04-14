package urza_queue;

import java.net.InetSocketAddress;
import java.util.logging.Level;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import static urza_queue.Main.logger;

public class Server extends WebSocketServer {

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
        Batch batch = new Batch(conn);
        Thread t = new Thread(batch);
        t.start();
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
