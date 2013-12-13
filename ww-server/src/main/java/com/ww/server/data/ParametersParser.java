package com.ww.server.data;

import com.ww.server.enums.TagName;
import java.util.Map;

/**
 *
 * @author sandy
 */
public class ParametersParser {

    private Parameters parameters;

    public ParametersParser() {
        parameters = new Parameters();
    }



    public void setParameters(Map<String, String[]> request) {
        for (String key : request.keySet()) {
            try {
                parameters.put(key, request.get(key)[0]);
            } catch (IllegalArgumentException ex) {
                //TODO throw custom exception and log in debug mode
            }
        }
    }

    public Parameters getParameters() {
        return parameters;
    }
}
