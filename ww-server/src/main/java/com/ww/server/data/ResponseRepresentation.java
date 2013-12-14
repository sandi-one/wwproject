package com.ww.server.data;

import com.google.gson.Gson;
import com.ww.server.enums.TagName;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author sandy
 */
public class ResponseRepresentation {

    public static String getRepresentation(ResponseMap response) {
        Gson gson = new Gson();
        Map<String, Object> representation = new TreeMap<String, Object>();
        for (TagName tag : response.keySet()) {
            representation.put(tag.toString(), response.get(tag));
        }
        return gson.toJson(representation);
    }
}
