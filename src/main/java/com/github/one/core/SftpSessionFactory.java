package com.github.one.core;

import com.github.one.config.SftpConnectionProperties;
import com.github.one.config.SftpPoolProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.DestroyMode;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.DisposableBean;

/**
 * @author one.xu
 */
@Slf4j
public class SftpSessionFactory implements DisposableBean {


    private GenericObjectPool<SftpSession> pool;

    public SftpSessionFactory(SftpConnectionProperties connectionProperties, SftpPoolProperties poolProperties) {
        this.pool = new GenericObjectPool<>(new SftpPooledObjectFactory(connectionProperties), getPoolConfig(poolProperties));
    }

    public SftpSession getSession() {
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            throw new SftpPoolException("获取sftp会话异常", e);
        }
    }

    public void releaseSession(SftpSession sftpSession) {
        try {
            pool.returnObject(sftpSession);
        } catch (Exception e) {
            throw new SftpPoolException("释放sftp会话异常", e);
        }
    }


    @Override
    public void destroy() {
        pool.close();
    }

    @Data
    private static class SftpPooledObjectFactory extends BasePooledObjectFactory<SftpSession> {
        private SftpConnectionProperties connectionProperties;

        public SftpPooledObjectFactory(SftpConnectionProperties connectionProperties) {
            this.connectionProperties = connectionProperties;
        }

        @Override
        public SftpSession create() {
            log.debug("创建sftp会话");
            return new SftpSession(connectionProperties);
        }

        @Override
        public PooledObject<SftpSession> wrap(SftpSession sftpSession) {
            return new DefaultPooledObject<>(sftpSession);
        }

        @Override
        public boolean validateObject(PooledObject<SftpSession> p) {
            return p.getObject().test();
        }

        @Override
        public void destroyObject(PooledObject<SftpSession> p, DestroyMode destroyMode) throws Exception {
            p.getObject().close();
            log.debug("关闭sftp会话");
        }
    }

    private GenericObjectPoolConfig<SftpSession> getPoolConfig(SftpPoolProperties poolProperties) {
        GenericObjectPoolConfig<SftpSession> config = new GenericObjectPoolConfig<>();
        config.setMaxIdle(poolProperties.getMaxIdle());
        config.setTestOnBorrow(poolProperties.isTestOnBorrow());
        config.setTestOnReturn(poolProperties.isTestOnReturn());
        config.setTestWhileIdle(poolProperties.isTestWhileIdle());
        config.setTimeBetweenEvictionRunsMillis(poolProperties.getTimeBetweenEvictionRunsMillis());
        config.setMinEvictableIdleTimeMillis(poolProperties.getMinEvictableIdleTimeMillis());
        config.setMinIdle(poolProperties.getMinIdle());
        config.setMaxTotal(poolProperties.getMaxActive());
        config.setMaxWaitMillis(poolProperties.getMaxWait());
        return config;
    }

}
