package com.ww.server.action;

/**
 *
 * @author sandy
 */
@Action("auth")
public class AuthAction extends BaseAction {

    @Override
    public String processAction(String data) {
        return "fuck yeah baby!";
    }
}
