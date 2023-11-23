package com.github.one.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;


/**
 * @author one.xu
 */
@ConfigurationProperties("sftp.pool.connection")
@Data
public class SftpConnectionProperties {

    private String host;

    private int port;

    private String username;

    private String password;

    private int connectTimeout = 0;

    private String privateKey;

    private Map<String, Object> session;

}
