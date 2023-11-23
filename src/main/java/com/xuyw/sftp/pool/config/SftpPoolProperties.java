package com.xuyw.sftp.pool.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author one.xu
 */
@ConfigurationProperties("sftp.pool.config")
@Data
public class SftpPoolProperties {

    private int minIdle = 1;


    private int maxIdle = 8;

    private int maxActive = 8;

    private int maxWait = 60000;

    private boolean testOnBorrow = true;


    private boolean testOnReturn = false;


    private boolean testWhileIdle = true;


    private long timeBetweenEvictionRunsMillis = 1000L * 60L * 10L;


    private long minEvictableIdleTimeMillis = 1000L * 60L * 30L;


    private int minIdlePerKey = 1;


    private int maxIdlePerKey = 8;


    private int maxActivePerKey = 8;
}
