package com.ww.server.util;

import com.ww.server.exception.ActionErrors;
import com.ww.server.exception.ActionException;
import com.ww.server.persistence.Persistence;
import com.ww.server.persistence.exception.PersistenceException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 * @author sandy
 */
public class Validator {

    private static final String MASK_ATOM = "[\\*\\?\\p{Alnum}-_]+";
    private static final Pattern DOMAIN_PATTERN = Pattern.compile("^[^@]+\\.[^@\\.]+$");
    private static final Pattern LOCAL_NAME_PATTERN = Pattern.compile("^[^@]+$");
    private static final Pattern LOCAL_NAME_MASK_PATTERN = Pattern.compile("^" + MASK_ATOM + "(\\." + MASK_ATOM + ")*$");
    private static final Pattern DOMAIN_MASK_PATTERN = Pattern.compile("^" + MASK_ATOM + "(\\." + MASK_ATOM + ")*$");

    public static <T> T validateId(String uuid, Class<T> model, ActionErrors error) throws ActionException {
        if (uuid == null || uuid.isEmpty()) {
            throw new ActionException(error, uuid);
        }
    	T result = null;
        try {
            result = Persistence.fetchById(model, uuid);
        } catch (PersistenceException e) {
            // nothing to do
        }
        if (result == null) {
            throw new ActionException(error, uuid);
        }
        return result;
    }

    public static <T> Set<T> validateIds(Collection<String> uuids, Class<T> model, ActionErrors error) throws ActionException {
        Set<String> uuidsSet = new HashSet<String>(uuids);
        List<T> result = Persistence.fetchByIds(model, uuidsSet);
        if (result.size() < uuidsSet.size()) {
            throw new ActionException(error);
        }
        return new HashSet(result);
    }

    public static boolean isEmail(String string) {
        return isEmail(LOCAL_NAME_PATTERN, DOMAIN_PATTERN, string);
    }

    public static boolean isEmailMask(String string) {
        return isEmail(LOCAL_NAME_MASK_PATTERN, DOMAIN_MASK_PATTERN, string);
    }

    private static boolean isEmail(Pattern localnamePat, Pattern domainPat, String email) {
        if (null == email) {
            return false;
        }

        String[] emailParts = email.split("@", -1);
        if (emailParts.length != 2) {
            return false;
        }

        String localname = emailParts[0];
        if (!localnamePat.matcher(localname).matches()) {
            return false;
        }

        String hostname = emailParts[1];
        if (!domainPat.matcher(hostname).matches()) {
            return false;
        }

        return true;
    }
}
