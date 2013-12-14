package com.ww.server.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ww.server.enums.TagName;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;

/**
 *
 * @author sandy
 */
public class Parameters extends LinkedHashMap<String, Object> {

    public String getAction() {
        return (String) this.get(TagName.ACTION.toString());
    }

    public void setParameters(String request) {
        Gson gson = new Gson();
        Type type = new TypeToken<LinkedHashMap<String, Object>>(){}.getType();
        this.putAll((LinkedHashMap<String, Object>) gson.fromJson(request, type));
    }
}
