package com.ww.server;

/**
 *
 * @author sandy
 */
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;

public class DefaultSocketHandler extends WebSocketHandler {

    private final Set<DefaultWebSocket> webSockets = new CopyOnWriteArraySet<DefaultWebSocket>();

    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        // TODO validate requests
        return new DefaultWebSocket();
    }

    private class DefaultWebSocket implements WebSocket.OnTextMessage {

        private Connection connection;

        /**
         * Open connection action (preProcessAction)
         * @param connection
         */
        @Override
        public void onOpen(Connection connection) {
            this.connection = connection;
            webSockets.add(this);
        }

        /**
         * Action (processAction)
         * @param data
         */
        @Override
        public void onMessage(String data) {
            try {
                connection.sendMessage("fck u");
            } catch (IOException ex) {
                connection.close();
            }
            System.out.println(data);
        }

        /**
         * End of action (postProcessAction)
         * @param closeCode
         * @param message
         */
        @Override
        public void onClose(int closeCode, String message) {
            connection.close();
            webSockets.remove(this);
        }
    }
}
