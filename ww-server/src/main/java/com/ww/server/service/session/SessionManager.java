package com.ww.server.service.session;

import com.ww.server.cache.SessionCache;
import com.ww.server.model.Account;
import com.ww.server.service.Instance;
import com.ww.server.service.WWFactory;
import com.ww.server.service.exception.ServiceErrors;
import com.ww.server.service.exception.ServiceException;
import org.eclipse.jetty.websocket.WebSocket;

/**
 *
 * @author sandy
 */
public class SessionManager {

    private static SessionCache cache = new SessionCache(1000);
    private Session currentSession;

    public Session saveSession(WebSocket.Connection connection, Account currentAccount, boolean forced) {
        if (forced) {
            cache.closeConnection(currentAccount.getAccountId());
        }

        Session session = new Session(connection, currentAccount);
        cache.putObject(currentAccount.getAccountId(), session);
        currentSession = session;

        return session;
    }

    public void validateSession(Session session) throws ServiceException {
        Account account = session.getAccount();

        if (account == null) {
            throw new ServiceException(ServiceErrors.SESSION_EXPIRED);
        }

        Session existedSession = cache.getObject(session.getAccount().getAccountId());

        if (!existedSession.getSession().isOpen()) {
            throw new ServiceException(ServiceErrors.SESSION_EXPIRED);
        }
    }

    public void validateConnection(WebSocket.Connection connection) throws ServiceException {
        WWFactory service = Instance.get();
        Account currentAccount = service.getAuthenticationService().getCurrentAccount();

        if (currentAccount == null) {
            throw new ServiceException(ServiceErrors.SESSION_EXPIRED);
        }

        Session existedSession = cache.getObject(currentAccount.getAccountId());

        if (existedSession == null) {
            cache.deleteObject(currentAccount.getAccountId());
            throw new ServiceException(ServiceErrors.SESSION_EXPIRED);
        }

        if (!existedSession.getSession().isOpen()) {
            throw new ServiceException(ServiceErrors.SESSION_EXPIRED);
        }
    }

    public void invalidateSession(Session session) {
        cache.deleteObject(session.getAccount().getAccountId());
        currentSession = null;
    }

    public Session getCurrentSession() {
        return currentSession;
    }

    public Session getSession(String accountId) {
        return cache.getObject(accountId);
    }
}
