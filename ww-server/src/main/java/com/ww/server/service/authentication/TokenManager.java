package com.ww.server.service.authentication;

import com.ww.server.model.Account;
import com.ww.server.service.exception.ServiceErrors;
import com.ww.server.service.exception.ServiceException;
import java.util.Date;
import java.util.UUID;
import org.eclipse.jetty.websocket.WebSocket;

/**
 *
 * @author sandy
 */
public class TokenManager {

    private Token currentToken;
    private WebSocket.Connection currentConnection;
    private static TokenCache cache = new TokenCache(1000);

    public Token createToken(Account account, WebSocket.Connection connection) {
        Token token = new Token();
        token.setAccount(account);
        long expiredTime = new Date().getTime() + TokenCache.getExpireTime();
        token.setExpiredDate(new Date(expiredTime));
        token.setClientType(null);
        token.setToken(UUID.randomUUID().toString());

        cache.putToken(token, connection);
        currentToken = token;
        currentConnection = connection;

        return token;
    }

    public void invalidateToken(Token token) {
        cache.deleteToken(token.getToken());
        currentToken = null;
        currentConnection = null;
    }

    public void validateToken(Token token) throws ServiceException {
        Account account = token.getAccount();
        if (account == null) {
            invalidateToken(token);
            throw new ServiceException(ServiceErrors.INVALID_TOKEN, token.getFullTokenId());
        }

        if (token.getExpiredDate().before(new Date())) {
            invalidateToken(token);
            throw new ServiceException(ServiceErrors.INVALID_TOKEN, token.getFullTokenId());
        }
    }

    public void invalidateSession(String tokenId) {
        cache.deleteToken(tokenId);
        currentConnection = null;
    }

    public Token getCurrentToken() {
        return currentToken;
    }

    public WebSocket.Connection getCurrentSession() {
        return currentConnection;
    }

    public static Token getToken(String tokenId) {
        return cache.getToken(tokenId);
    }

    public static WebSocket.Connection getSession(String tokenId) {
        return cache.getSession(tokenId);
    }

    public static String getFullTokenId(String token) {
        return (token != null && !token.startsWith(Token.TOKEN_PREFIX))
                ? Token.TOKEN_PREFIX + token
                : "" + token;
    }
}
