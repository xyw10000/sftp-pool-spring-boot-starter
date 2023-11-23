package com.xuyw.sftp.pool.core;

/**
 * @author one.xu
 */
@FunctionalInterface
public interface ISftpCallbackWithoutResult {

    void execute(SftpSession sftpSession);
}
