package com.ww.server.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author sandy
 */
public class WWService extends ServiceFactory implements WWFactory {

    @Override
    public void init() {
        // invoke accessor methods for all services
        for (Method method : Factory.class.getMethods()) {
            if (method.getParameterTypes().length == 0) {
                try {
                    method.invoke(this, new Object[] {});
                } catch (IllegalAccessException ex) {
                    //_log.error(ex, ex);
                } catch (IllegalArgumentException ex) {
                    //_log.error(ex, ex);
                } catch (InvocationTargetException ex) {
                    //_log.error(ex, ex);
                }
            }
        }
    }
}
