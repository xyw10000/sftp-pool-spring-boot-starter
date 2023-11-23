package com.xuyw.sftp.pool.core;

/**
 * @author one.xu
 */
@FunctionalInterface
public interface ISftpCallback<T>{

    T execute(SftpSession sftpSession);
}
