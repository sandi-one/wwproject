package com.ww.server.service.authentication;

import com.ww.server.model.Account;
import com.ww.server.persistence.AuthenticationPersistence;
import com.ww.server.service.Service;
import com.ww.server.service.exception.ServiceErrors;
import com.ww.server.service.exception.ServiceException;
import org.eclipse.jetty.websocket.WebSocket;

/**
 *
 * @author sandy
 */
public class AuthenticationService extends Service {

    private Account currentAccount;
    private TokenManager manager = new TokenManager();

    public Token login (WebSocket.Connection connection, String login, String password)
            throws ServiceException {
        Account account;
        // TODO auth password
        try {
            account = AuthenticationPersistence.fetchAccountByName(login);
        } catch (Exception ex) {
            throw new ServiceException(ServiceErrors.INVALID_LOGIN, login);
        }

        if (!account.isActive()) {
            throw new ServiceException(ServiceErrors.BLOCKED_ACCOUNT);
        }

        Token newToken = manager.createToken(account, connection);
        manager.validateToken(newToken);
        currentAccount = account;

        return newToken;
    }

    public void logoff(String tokenId, WebSocket.Connection session) {
        manager.invalidateToken(TokenManager.getToken(tokenId));
        currentAccount = null;
        session.close();
    }

    public void setCurrentAccount(Account account) {
        currentAccount = account;
    }

    public void validateToken(String tokenId) throws ServiceException {
        manager.validateToken(TokenManager.getToken(tokenId));
    }

    public void invalidateSession(String tokenId) {
        manager.invalidateSession(tokenId);
    }
}
