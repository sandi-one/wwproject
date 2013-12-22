package com.ww.server.action;

import com.ww.server.data.Parameters;
import com.ww.server.data.ResponseMap;
import com.ww.server.enums.TagName;
import com.ww.server.exception.ActionErrors;
import com.ww.server.exception.ActionException;
import com.ww.server.model.Account;
import com.ww.server.util.ParamUtil;
import com.ww.server.util.Validator;

/**
 *
 * @author sandy
 */
@Action("auth")
public class AuthAction extends BaseAction {

    private String login;
    private String password;
    protected Account actionAccount;

    @Override
    public void validate(Parameters parameters) throws ActionException {
        super.validate(parameters);
        login = ParamUtil.getNotEmpty(parameters, TagName.USER_LOGIN.toString());
        password = ParamUtil.getNotEmpty(parameters, TagName.USER_PASSWORD.toString());
    }

    @Override
    public void preProcessAction() throws ActionException {
        super.preProcessAction();
        actionAccount = Validator.validateId(login, null, ActionErrors.BAD_REQUEST);
    }

    @Override
    public ResponseMap processAction(Parameters parameters) throws ActionException {
        ResponseMap response = new ResponseMap();

        response.put(TagName.USER_LOGIN, "bitch");
        response.put(TagName.USER_PASSWORD, "fuckin");

        throw new ActionException(ActionErrors.UNKNOWN_SERVER_ERROR);

        //return response;
    }
}
