package com.ww.server.action;

import com.ww.server.data.Parameters;

/**
 *
 * @author sandy
 */
public class BaseAction {

    protected Parameters parameters;

    public String getActionId() {
        Action annotation = this.getClass().getAnnotation(Action.class);
        return (null == annotation) ? null : annotation.value();
    }

    public String processAction(String data) {
        return ""; // default value
    }
}
