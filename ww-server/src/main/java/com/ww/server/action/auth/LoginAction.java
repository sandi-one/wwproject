package com.ww.server.action.auth;

import com.ww.server.action.Action;
import com.ww.server.action.BaseAction;
import com.ww.server.data.Parameters;
import com.ww.server.data.ResponseMap;
import com.ww.server.enums.TagName;
import com.ww.server.exception.ActionException;
import com.ww.server.util.ParamUtil;

/**
 *
 * @author sandy
 */
@Action("login")
public class LoginAction extends BaseAction {

    private String login;
    private String password;

    @Override
    public void validate(Parameters parameters) throws ActionException {
        super.validate(parameters);

        login = ParamUtil.getNotEmpty(parameters, TagName.USER_LOGIN.toString());
        password = ParamUtil.getNotEmpty(parameters, TagName.USER_PASSWORD.toString());
    }

    @Override
    public ResponseMap processAction(Parameters parameters) throws ActionException {
        service.getAuthenticationService().login(connection, login, password);

        ResponseMap result = new ResponseMap();
        return result;
    }
}
