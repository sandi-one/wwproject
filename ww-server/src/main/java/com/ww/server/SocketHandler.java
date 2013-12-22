package com.ww.server;

/**
 *
 * @author sandy
 */
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;

public class SocketHandler extends WebSocketHandler {

    private final static Set<ActionHandler> webSockets = new ConcurrentHashSet<ActionHandler>();

    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        return new ActionHandler();
    }

    public static Set<ActionHandler> getWebSockets() {
        return webSockets;
    }
}
