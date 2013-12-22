package com.ww.server.events;

/**
 *
 * @author sandy
 */
public class AddSelfException extends RuntimeException {

    @Override
    public String getMessage() {
        return "Cannot add self as handler";
    }
}
