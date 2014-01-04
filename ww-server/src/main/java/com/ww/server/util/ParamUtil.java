package com.ww.server.util;

import com.ww.server.data.Parameters;
import com.ww.server.exception.ActionErrors;
import com.ww.server.exception.ActionException;
import java.util.List;

/**
 *
 * @author sandy
 */
public class ParamUtil {

    public static Object getNotNull(Parameters request, String name) throws ActionException {
        Object value = request.get(name);
        if (value == null) {
            throw new ActionException(ActionErrors.PARAM_MUST_NOT_BE_NULL, name);
        }
        return value;
    }

    public static String getNotEmpty(Parameters request, String name) throws ActionException {

        String value = (String) getNotNull(request, name);

        if (value.length() == 0) {
            throw new ActionException(ActionErrors.PARAM_MUST_NOT_BE_EMPTY, name);
        }
        return value;
    }

    public static String getNotEmptyFromArray(Parameters request, String name) throws ActionException {

        String value = ((String[]) getNotNull(request, name))[0];

        if (value.length() == 0) {
            throw new ActionException(ActionErrors.PARAM_MUST_NOT_BE_EMPTY, name);
        }
        return value;
    }

    public static int getInt(Parameters request, String name) throws ActionException {
        String value = getNotEmpty(request, name);
        int result = -1;
        try {
            result = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ActionException(ActionErrors.INCORRECT_INTEGER_VALUE, name);
        }
        return result;
    }

    private static Boolean getBoolean(Parameters request, String name) throws ActionException {
        String value = getNotEmpty(request, name);
        boolean result = false;
        if (value.equalsIgnoreCase(Boolean.TRUE.toString())) {
            result = true;
        } else {
            if (value.equalsIgnoreCase(Boolean.FALSE.toString())) {
                result = false;
            } else {
                throw new ActionException(ActionErrors.INCORRECT_BOOLEAN_VALUE, name);
            }
        }
        return result;
    }

    public static List<String> getStringList(Parameters request, String name) throws ActionException {
        return (List<String>) getNotNull(request, name);
    }

    public static List<Integer> getIntList(Parameters request, String name) throws ActionException {
        return (List<Integer>) getNotNull(request, name);
    }
}
