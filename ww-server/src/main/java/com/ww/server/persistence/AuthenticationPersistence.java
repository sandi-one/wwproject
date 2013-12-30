package com.ww.server.persistence;

import com.ww.server.model.Account;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sandy
 */
public class AuthenticationPersistence extends Persistence {

    private static final Class clazz = Account.class;

    public static Account fetchAccountByName(String name) {
        String query = "FROM Account AS a WHERE a.accountName = :name";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", name);

        return (Account) fetchSingleByQueryWithParams(clazz, query, params);
    }

    public static Account fetchAccountByEmail(String email) {
        String query = "FROM Account AS a WHERE a.accountEmail = :email";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("email", email);

        return (Account) fetchSingleByQueryWithParams(clazz, query, params);
    }
}
