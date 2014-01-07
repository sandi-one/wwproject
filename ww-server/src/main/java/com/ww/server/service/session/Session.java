package com.ww.server.service.session;

import com.ww.server.model.Account;
import org.eclipse.jetty.websocket.WebSocket;

/**
 *
 * @author sandy
 */
public class Session {

    private WebSocket.Connection session;
    private Account account;

    public Session(WebSocket.Connection session, Account account) {
        this.session = session;
        this.account = account;
    }

    public WebSocket.Connection getSession() {
        return session;
    }

    public void setSession(WebSocket.Connection session) {
        this.session = session;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
