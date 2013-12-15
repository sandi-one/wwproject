package com.ww.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;

/**
 *
 * @author sandy
 */

public class WWServer {
    public static final int DEFAULT_WWSERVER_PORT = 8080;

    public static void main(String[] args) throws Exception {
        Server wwServer = new Server(DEFAULT_WWSERVER_PORT);

        SocketHandler socketHandler = new SocketHandler();
        socketHandler.setHandler(new DefaultHandler());
        // set listening mode
        wwServer.setHandler(socketHandler);
        wwServer.start();
        wwServer.join();
    }
}
