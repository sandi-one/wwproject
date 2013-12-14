package com.ww.server.action;

import com.ww.server.data.Parameters;
import com.ww.server.data.ResponseMap;
import com.ww.server.enums.TagName;
import com.ww.server.util.ParamUtil;

/**
 *
 * @author sandy
 */
@Action("auth")
public class AuthAction extends BaseAction {

    private String login;
    private int pass;

    @Override
    public void validate(Parameters parameters) {
        super.validate(parameters);
        login = ParamUtil.getNotEmpty(parameters, TagName.USER_LOGIN.toString());
        pass = ParamUtil.getInt(parameters, TagName.USER_PASSWORD.toString());
    }

    @Override
    public ResponseMap processAction(Parameters parameters) {
        ResponseMap response = new ResponseMap();

        response.put(TagName.USER_LOGIN, "bitch");
        response.put(TagName.USER_PASSWORD, "fuckin");

        return response;
    }
}
