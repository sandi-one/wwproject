package com.ww.server.service.authentication;

import com.ww.server.model.Account;
import java.util.Date;
import java.util.UUID;

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
        long expiredTime = new Date().getTime() + TokenCache.getExpireTime();
        token.setExpiredDate(new Date(expiredTime));
        token.setClientType(null);
        token.setToken(UUID.randomUUID().toString());

        cache.putToken(token);
        currentToken = token;

        return token;
    }

    public void invalidateToken(Token token) {
        cache.deleteToken(token.getToken());
        currentToken = null;
    }

    public void validateToken(Token token) {
        Account account = token.getAccount();
        if (account == null) {
            invalidateToken(token);
            throw new RuntimeException("Invalid token"); // change throws
        }

        if (token.getExpiredDate().before(new Date())) {
            invalidateToken(token);
            throw new RuntimeException("Invalid token"); // change throws
        }
    }

    public Token getCurrentToken() {
        return currentToken;
    }

    public Token getToken(String tokenId) {
        return cache.getToken(tokenId);
    }

    public static String getFullTokenId (String token) {
        return (token != null && !token.startsWith(Token.TOKEN_PREFIX))
                ? Token.TOKEN_PREFIX + token
                : "" + token;
    }
}
