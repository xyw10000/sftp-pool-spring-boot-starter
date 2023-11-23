package com.github.one.core;

import com.jcraft.jsch.ChannelSftp;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author one.xu
 */
public class SftpTemplate {
    private SftpSessionFactory sftpSessionFactory;

    public SftpTemplate(SftpSessionFactory sftpSessionFactory) {
        this.sftpSessionFactory = sftpSessionFactory;
    }

    /**
     * 上传文件
     * from 若为文件则上传单个文件,to 为文件全路径包涵文件名
     * from 若为文件夹则上传文件夹,to 上传路径
     *
     * @param from
     * @param to
     */
    public void upload(String from, String to) {
        this.executeWithoutResult(sftpSession -> {
            SftpTemplate.this.upload(sftpSession, new File(from), to);
        });
    }

    /**
     * 上传单个文件
     *
     * @param from
     * @param to
     */
    public void upload(InputStream from, String to) {
        this.executeWithoutResult(sftpSession -> {
            sftpSession.upload(from, to);
        });
    }

    /**
     * 下载文件
     * from 若为文件则下载单个文件,to 为文件全路径包涵文件名
     * from 若为文件夹则下载文件夹,to 下载路径
     *
     * @param from
     * @param to
     */
    public void download(String from, String to) {
        this.executeWithoutResult(sftpSession -> {
            SftpTemplate.this.download(sftpSession, from, to);
        });
    }

    /**
     * 下载单个文件
     *
     * @param from
     * @param to
     */
    public void download(String from, OutputStream to) {
        this.executeWithoutResult(sftpSession -> {
            sftpSession.download(from, to);
        });
    }

    /**
     * 删除目录及文件
     *
     * @param fromPath
     */
    public void rm(String fromPath) {
        this.executeWithoutResult(sftpSession -> {
            sftpSession.rm(fromPath);
        });
    }

    public void executeWithoutResult(ISftpCallbackWithoutResult action) {
        this.execute(sftpSession -> {
            action.execute(sftpSession);
            return null;
        });
    }

    public <T> T execute(ISftpCallback<T> action) {
        SftpSession sftpSession = null;
        try {
            sftpSession = sftpSessionFactory.getSession();
            return action.execute(sftpSession);
        } catch (Exception e) {
            throw new SftpPoolException("操作sftp失败: " + e.getMessage(), e);
        } finally {
            if (sftpSession != null) {
                sftpSessionFactory.releaseSession(sftpSession);
            }
        }
    }


    private void upload(SftpSession sftpSession, File fromFile, String to) {
        if (fromFile.isFile()) {
            sftpSession.upload(fromFile.getPath(), to);
            return;
        }

        File[] files = fromFile.listFiles();
        if (files.length == 0 && !sftpSession.exists(to)) {
            //空文件夹
            sftpSession.mkdir(to);
            return;
        }

        for (File file : files) {
            upload(sftpSession, file, to + SftpSession.separator + file.getName());
        }
    }

    private void download(SftpSession sftpSession, String from, String to) {
        File file = new File(to);
        if (!file.exists()) {
            file.mkdirs();
        }
        for (ChannelSftp.LsEntry e : sftpSession.listFiles(from)) {
            if (e.getFilename().equals(".") || e.getFilename().equals("..")) {
                continue;
            }
            if (e.getAttrs().isDir()) {
                download(sftpSession, from + SftpSession.separator + e.getFilename(), to + File.separator + e.getFilename());
            } else {
                sftpSession.download(from + SftpSession.separator + e.getFilename(), to);
            }
        }
    }

}
