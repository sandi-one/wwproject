package com.ww.server.service.authentication;

import com.ww.server.model.Account;
import com.ww.server.persistence.AuthenticationPersistence;
import com.ww.server.service.Service;
import com.ww.server.service.exception.ServiceErrors;
import com.ww.server.service.exception.ServiceException;
import com.ww.server.service.session.Session;
import com.ww.server.service.session.SessionManager;
import com.ww.server.util.SHA1;
import java.security.NoSuchAlgorithmException;
import org.eclipse.jetty.websocket.WebSocket;

/**
 *
 * @author sandy
 */
public class AuthenticationService extends Service {

    private Account currentAccount;
    private Session currentSession;
    private TokenManager tokenManager = new TokenManager();
    private SessionManager sessionManager = new SessionManager();

    public Token login(WebSocket.Connection connection, String login, String password,
            boolean remember, boolean forced) throws ServiceException {
        Account account;
        try {
            account = AuthenticationPersistence.fetchAccountByName(login);
        } catch (Exception ex) {
            throw new ServiceException(ServiceErrors.INVALID_LOGIN, login);
        }

        authenticatePassword(account, password);

        if (!account.isActive()) {
            throw new ServiceException(ServiceErrors.BLOCKED_ACCOUNT);
        }

        Session session = sessionManager.getSession(account.getAccountId());

        if (session != null && !forced) {
            throw new ServiceException(ServiceErrors.CONNECTION_ALREADY_OPENED);
        }

        Token newToken = null;
        if (remember) {
            newToken = tokenManager.createToken(account);
            tokenManager.validateToken(newToken);
        }

        currentSession = sessionManager.saveSession(connection, account, forced);
        currentAccount = account;

        sessionManager.validateConnection(connection);
        return newToken;
    }

    public Token login(WebSocket.Connection connection, String fullTokenId) throws ServiceException {
        Token token = TokenManager.getToken(fullTokenId);

        tokenManager.validateToken(token);
        currentSession = sessionManager.saveSession(connection, token.getAccount(), true);
        currentAccount = token.getAccount();

        sessionManager.validateConnection(connection);
        return token;
    }

    public void logoff() {
        if (currentSession != null) {
            sessionManager.invalidateSession(currentSession);
        }
        currentAccount = null;
    }

    public Account getCurrentAccount() {
        return currentAccount;
    }

    public void setCurrentAccount(Account account) {
        currentAccount = account;
    }

    public Session getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(Session currentSession) {
        this.currentSession = currentSession;
    }

    public void validateToken(String tokenId) throws ServiceException {
        tokenManager.validateToken(TokenManager.getToken(tokenId));
    }

    public void authenticatePassword(Account account, String password) throws ServiceException {
        String hash = SHA1.sha1(password); // TODO implement more secure algorithm
        if (!hash.equals(account.getAccountPassword())) {
            throw new ServiceException(ServiceErrors.INVALID_PASSWORD);
        }
    }
}
