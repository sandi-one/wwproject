package com.ww.server.data;

import com.ww.server.enums.TagName;
import java.util.TreeMap;

/**
 *
 * @author sandy
 */
public class ResponseMap extends TreeMap<TagName, Object>{

    @Override
    public Object put(TagName key, Object value) {
        if (value != null && value instanceof Double && value.equals(Double.NaN)) {
            value = null;
        }
        return super.put(key, value);
    }
}
