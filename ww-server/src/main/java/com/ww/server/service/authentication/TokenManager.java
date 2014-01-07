package com.ww.server.service.authentication;

import com.ww.server.cache.TokenCache;
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
    private static TokenCache cache = new TokenCache(1000);

    public Token createToken(Account account) {
        Token token = new Token();
        token.setAccount(account);
        long expiredTime = new Date().getTime() + cache.getExpireTime();
        token.setExpiredDate(new Date(expiredTime));
        token.setClientType(null);
        token.setToken(UUID.randomUUID().toString());

        cache.putObject(token.getFullTokenId(), token);
        currentToken = token;

        return token;
    }

    public void invalidateToken(Token token) {
        cache.deleteObject(token.getFullTokenId());
        currentToken = null;
    }

    public void validateToken(Token token) throws ServiceException {
        Account account = token.getAccount();
        if (account == null) {
            invalidateToken(token);
            throw new ServiceException(ServiceErrors.INVALID_TOKEN, token.getFullTokenId());
        }

        if (token.getExpiredDate().before(new Date())) {
            invalidateToken(token);
            throw new ServiceException(ServiceErrors.TOKEN_EXPIRED);
        }
    }

    public Token getCurrentToken() {
        return currentToken;
    }

    public static Token getToken(String fullTokenId) {
        return cache.getObject(fullTokenId);
    }

    public static String getFullTokenId(String token) {
        return (token != null && !token.startsWith(Token.TOKEN_PREFIX))
                ? Token.TOKEN_PREFIX + token
                : "" + token;
    }

    public static String getTokenId(String fullTokenId) {
        return (fullTokenId != null && fullTokenId.startsWith(Token.TOKEN_PREFIX))
                ? fullTokenId.split(Token.TOKEN_PREFIX)[1]
                : fullTokenId;
    }
}
