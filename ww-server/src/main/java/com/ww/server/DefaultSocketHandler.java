package com.ww.server;

/**
 *
 * @author sandy
 */
import com.ww.server.action.ActionHandler;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;

public class DefaultSocketHandler extends WebSocketHandler {

    private final static Set<ActionHandler> webSockets = new CopyOnWriteArraySet<ActionHandler>();

    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        return new ActionHandler();
    }

    public static Set<ActionHandler> getWebSockets() {
        return webSockets;
    }
}
