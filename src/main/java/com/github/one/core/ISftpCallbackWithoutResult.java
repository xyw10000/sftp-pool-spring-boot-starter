package com.github.one.core;

/**
 * @author one.xu
 */
@FunctionalInterface
public interface ISftpCallbackWithoutResult {

    void execute(SftpSession sftpSession);
}
