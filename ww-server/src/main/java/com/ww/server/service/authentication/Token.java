package com.ww.server.service.authentication;

import com.ww.server.model.Account;
import java.util.Date;

/**
 *
 * @author sandy
 */
public class Token implements Cloneable {

    protected static final String TOKEN_PREFIX = "WW+";

    private String token;
    private Date expiredDate;
    private String clientType;
    private Account account;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(Date expiredDate) {
        this.expiredDate = expiredDate;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getFullTokenId() {
        return (token != null && !token.startsWith(TOKEN_PREFIX))
                ? TOKEN_PREFIX + token
                : "" + token;
    }

    @Override
    public String toString() {
        return getFullTokenId();
    }

    @Override
    public Token clone() {
        Token clone;
        try {
            clone = (Token) super.clone();
            clone.setAccount(null);
        } catch (CloneNotSupportedException ex) {
            clone = new Token();
            clone.setExpiredDate(expiredDate);
            clone.setToken(token);
            clone.setClientType(clientType);
        }
        return clone;
    }
}
