package com.ww.server.service;

import com.ww.server.service.exception.ServiceException;

/**
 *
 * @author sandy
 */
public interface WWFactory extends Factory {

    public void init();

    public void beginTransaction() throws ServiceException;

    public void commit() throws ServiceException;

    public void rollback() throws ServiceException;
}
