package org.rmatil.sync.persistence.core;

import org.rmatil.sync.persistence.api.IFileMetaInfo;

public class FileMetaInfo implements IFileMetaInfo {

    /**
     * The total file size in bytes
     */
    protected long totalFileSize;

    /**
     * Whether the path is a file
     */
    protected boolean isFile;

    /**
     * The paths file extension
     */
    protected String fileExtension;

    /**
     * @param totalFileSize The total file size in bytes
     */
    public FileMetaInfo(long totalFileSize, boolean isFile, String fileExtension) {
        this.totalFileSize = totalFileSize;
        this.isFile = isFile;
        this.fileExtension = fileExtension;
    }

    @Override
    public long getTotalFileSize() {
        return this.totalFileSize;
    }

    @Override
    public boolean isFile() {
        return this.isFile;
    }

    @Override
    public boolean isDirectory() {
        return ! this.isFile;
    }

    @Override
    public String getFileExtension() {
        return this.fileExtension;
    }
}
