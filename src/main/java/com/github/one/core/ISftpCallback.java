package com.github.one.core;

/**
 * @author one.xu
 */
@FunctionalInterface
public interface ISftpCallback<T>{

    T execute(SftpSession sftpSession);
}
