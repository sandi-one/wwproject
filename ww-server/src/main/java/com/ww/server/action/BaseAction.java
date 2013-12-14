package com.ww.server.action;

import com.ww.server.data.Parameters;
import com.ww.server.data.ResponseMap;
import com.ww.server.data.ResponseRepresentation;

/**
 *
 * @author sandy
 */
public class BaseAction {

    protected Parameters parameters;

    public BaseAction(Parameters parameters) {
        this();
        this.parameters = parameters;
    }

    public BaseAction() {
    }

    public String getActionId() {
        Action annotation = this.getClass().getAnnotation(Action.class);
        return (null == annotation) ? null : annotation.value();
    }

    public void validate(Parameters parameters) {

    }

    public void preProcessAction() {

    }

    public ResponseMap processAction(Parameters parameters) {
        return new ResponseMap();
    }
}
