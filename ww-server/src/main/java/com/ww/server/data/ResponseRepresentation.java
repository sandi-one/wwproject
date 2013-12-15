package com.ww.server.data;

import com.google.gson.Gson;
import com.ww.server.enums.TagName;
import com.ww.server.exception.ActionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author sandy
 */
public class ResponseRepresentation {

    public static String getRepresentation(ResponseMap response, boolean success) {
        response.put(TagName.SUCCESS, success);

        Gson gson = new Gson();
        Map<String, Object> representation = new TreeMap<String, Object>();
        for (TagName tag : response.keySet()) {
            representation.put(tag.toString(), response.get(tag));
        }

        return gson.toJson(representation);
    }

    public static String handleError(ActionException ex) {
        ResponseMap response = new ResponseMap();
        response.put(TagName.ERROR_ID, ex.getErrorCode());
        response.put(TagName.ERROR_MESSAGE, ex.getMessage());
        String[] exFields = ex.getFields();
        if (exFields != null && exFields.length != 0) {
            response.put(TagName.EXCEPTION_FIELDS, exFields);
        }

        return getRepresentation(response, false);
    }

    public static String handleError(Throwable exc) {
        ResponseMap response = new ResponseMap();
        response.put(TagName.ERROR_ID, "9999");
        response.put(TagName.ERROR_MESSAGE, "INTERNAL ERROR");
        response.put(TagName.EXCEPTION, formatException(exc));

        return getRepresentation(response, false);
    }

    private static ResponseMap formatException(Throwable e) {
        ResponseMap response = new ResponseMap();
        List<StackTraceElement> elements = new ArrayList<StackTraceElement>();
        elements.addAll(Arrays.asList(e.getStackTrace()));

        response.put(TagName.TRACE, elements);
        response.put(TagName.CLASS, e.getClass().getName());
        response.put(TagName.ERROR_MESSAGE, e.getMessage());

        return response;
    }
}
