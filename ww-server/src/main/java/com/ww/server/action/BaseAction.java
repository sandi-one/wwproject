package com.ww.server.action;

import com.ww.server.data.Parameters;
import com.ww.server.data.ResponseMap;
import com.ww.server.exception.ActionException;

/**
 *
 * @author sandy
 */
public class BaseAction {

    public BaseAction() {
    }

    public String getActionId() {
        Action annotation = this.getClass().getAnnotation(Action.class);
        return (null == annotation) ? null : annotation.value();
    }

    public void validate(Parameters parameters) throws ActionException {

    }

    public void preProcessAction() throws ActionException {

    }

    public ResponseMap processAction(Parameters parameters) throws ActionException {
        return new ResponseMap();
    }
}
