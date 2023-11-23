package com.xuyw.sftp.pool.autoconfig;

import com.xuyw.sftp.pool.config.SftpConnectionProperties;
import com.xuyw.sftp.pool.config.SftpPoolProperties;
import com.xuyw.sftp.pool.core.SftpSessionFactory;
import com.xuyw.sftp.pool.core.SftpTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author one.xu
 */
@ConditionalOnProperty(name = "sftp.pool.enabled", havingValue = "true")
@EnableConfigurationProperties({SftpConnectionProperties.class, SftpPoolProperties.class})
public class SftpPoolAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SftpSessionFactory.class)
    public SftpSessionFactory sftpSessionFactory(SftpConnectionProperties connectionProperties,
                                                 SftpPoolProperties poolProperties) {
        return new SftpSessionFactory(connectionProperties, poolProperties);
    }

    @Bean
    @ConditionalOnMissingBean(SftpTemplate.class)
    public SftpTemplate sftpTemplate(SftpSessionFactory sftpSessionFactory) {
        return new SftpTemplate(sftpSessionFactory);
    }
}
