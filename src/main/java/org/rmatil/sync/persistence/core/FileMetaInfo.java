package org.rmatil.sync.persistence.core;

import org.rmatil.sync.persistence.api.IFileMetaInfo;

public class FileMetaInfo implements IFileMetaInfo {

    /**
     * The total file size in bytes
     */
    protected long totalFileSize;

    /**
     * @param totalFileSize The total file size in bytes
     */
    public FileMetaInfo(long totalFileSize) {
        this.totalFileSize = totalFileSize;
    }

    @Override
    public long getTotalFileSize() {
        return this.totalFileSize;
    }
}
