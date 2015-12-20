package org.rmatil.sync.persistence.api;

/**
 * Holds information about a particular file
 */
public interface IFileMetaInfo {

    /**
     * Returns the total file size in bytes
     *
     * @return The total file size in bytes
     */
    long getTotalFileSize();
}
