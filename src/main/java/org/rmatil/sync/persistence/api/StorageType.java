package org.rmatil.sync.persistence.api;

/**
 * Storage types used.
 * Could also include symbolic links, hard links and more.
 */
public enum StorageType {

    /**
     * The type used for a file
     */
    FILE,

    /**
     * The type used for a directory
     */
    DIRECTORY
}
