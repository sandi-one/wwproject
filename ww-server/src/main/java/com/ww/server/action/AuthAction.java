package com.ww.server.action;

import com.ww.server.data.Parameters;
import com.ww.server.data.ResponseMap;
import com.ww.server.enums.TagName;

/**
 *
 * @author sandy
 */
@Action("auth")
public class AuthAction extends BaseAction {

    @Override
    public ResponseMap processAction(Parameters parameters) {
        ResponseMap response = new ResponseMap();

        response.put(TagName.USER_LOGIN, "bitch");
        response.put(TagName.USER_PASSWORD, "fuckin");

        return response;
    }
}
