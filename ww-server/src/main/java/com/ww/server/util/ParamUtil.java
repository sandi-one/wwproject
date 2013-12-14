package com.ww.server.util;

import com.ww.server.data.Parameters;
import java.util.List;

/**
 *
 * @author sandy
 */
public class ParamUtil {

    public static Object getNotNull(Parameters request, String name) {
        Object value = request.get(name);
        if (value == null) {
            //throw exception
        }
        return value;
    }

    public static String getNotEmpty(Parameters request, String name) {

        String value = (String) getNotNull(request, name);

        if (value.length() == 0) {
            //throw exception
        }
        return value;
    }

    public static int getInt(Parameters request, String name) {
        String value = getNotEmpty(request, name);
        int result = -1;
        try {
            result = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // log and throw
        }
        return result;
    }

    private static Boolean getBoolean(Parameters request, String name) {
        String value = getNotEmpty(request, name);
        boolean result = false;
        if (value.equalsIgnoreCase(Boolean.TRUE.toString())) {
            result = true;
        } else {
            if (value.equalsIgnoreCase(Boolean.FALSE.toString())) {
                result = false;
            } else {
                //throw exception
            }
        }
        return result;
    }

    public static List<String> getStringList(Parameters request, String name) {
        return (List<String>) getNotNull(request, name);
    }

    public static List<Integer> getIntList(Parameters request, String name) {
        return (List<Integer>) getNotNull(request, name);
    }
}
