package com.xuyw.sftp.pool.core;

import org.springframework.core.NestedRuntimeException;
import org.springframework.lang.Nullable;


/**
 * @author one.xu
 */
public class SftpPoolException extends NestedRuntimeException {


    public SftpPoolException(@Nullable String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }

}
