package com.github.one.core;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.github.one.config.SftpConnectionProperties;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Vector;

/**
 * @author one.xu
 */
@Slf4j
@Data
public class SftpSession {
    private final Session session;
    private final ChannelSftp channelSftp;

    public static final String separator = "/";

    @SneakyThrows
    public SftpSession(SftpConnectionProperties connectionProperties) {
        this.session = createSession(connectionProperties);
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        this.channelSftp = channel;
    }

    /**
     * @param fromPath 源文件路径
     * @param toPath   存储位置(包涵文件名)
     */
    @SneakyThrows
    public void upload(String fromPath, String toPath) {
        this.cdAndMkDir(getPath(toPath));
        channelSftp.put(fromPath, getFileName(toPath));
    }

    /**
     * @param src    源文件文件流
     * @param toPath 存储位置(包涵文件名)
     */
    @SneakyThrows
    public void upload(InputStream src, String toPath) {
        this.cdAndMkDir(getPath(toPath));
        channelSftp.put(src, getFileName(toPath));
    }

    @SneakyThrows
    public void download(String from, String to) {
        channelSftp.get(from, to);
    }

    @SneakyThrows
    public void download(String from, OutputStream to) {
        channelSftp.get(from, to);
    }

    @SneakyThrows
    public Vector<ChannelSftp.LsEntry> listFiles(String directory) {
        return channelSftp.ls(directory);
    }

    /**
     * 进入指定目录,不存在则创建
     *
     * @param path
     */
    @SneakyThrows
    public void cdAndMkDir(String path) {
        try {
            cd(path);
        } catch (SftpException e) {
            if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                throw e;
            }
            if (path.startsWith(separator)) {
                cd(separator);
            }

            for (String dir : path.split(separator)) {
                if (dir.isEmpty()) {
                    continue;
                }
                if (!isDir(dir)) {
                    mkdir(dir);
                }
                cd(dir);
            }
        }
    }


    public void cd(String path) throws SftpException {
        channelSftp.cd(path);
    }

    public boolean isDir(String dir) {
        try {
            return channelSftp.lstat(dir).isDir();
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return false;
            }
        }
        return false;
    }

    @SneakyThrows
    public void mkdir(String path) {
        this.channelSftp.mkdir(path);
    }


    public boolean exists(String path) {
        try {
            this.channelSftp.lstat(path);
            return true;
        } catch (SftpException e) {
            return false;
        }
    }

    @SneakyThrows
    public void rm(String path) {
        for (ChannelSftp.LsEntry e : listFiles(path)) {
            if (e.getFilename().equals(".") || e.getFilename().equals("..")) {
                continue;
            }
            if (e.getAttrs().isDir()) {
                rm(path + separator + e.getFilename());
            } else {
                this.channelSftp.rm(path + separator + e.getFilename());
            }
        }
        this.channelSftp.rmdir(path);
    }

    public boolean test() {
        try {
            return channelSftp.isConnected();
        } catch (Exception ignored) {
            return false;
        }

    }

    public void close() {
        if (channelSftp != null && channelSftp.isConnected()) {
            channelSftp.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    @SneakyThrows
    private Session createSession(SftpConnectionProperties connectionProperties) {
        JSch jsch = new JSch();
        if (StringUtils.hasLength(connectionProperties.getPrivateKey())) {
            jsch.addIdentity(connectionProperties.getPrivateKey());
        }
        Session session = jsch.getSession(connectionProperties.getUsername(),
                connectionProperties.getHost(), connectionProperties.getPort());

        if (StringUtils.hasLength(connectionProperties.getPassword())) {
            session.setPassword(connectionProperties.getPassword());
        }
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");

        session.setConfig(config);
        session.connect(connectionProperties.getConnectTimeout());
        return session;
    }

    /**
     * 获取路径,移除文件名
     *
     * @param path
     * @return
     */
    private String getPath(String path) {
        return path.substring(0, path.lastIndexOf(separator) + 1);
    }

    /**
     * 返回文件名
     *
     * @param path
     * @return
     */
    private String getFileName(String path) {
        return path.substring(path.lastIndexOf(separator) + 1);
    }
}
