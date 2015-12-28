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

    /**
     * Returns true if the path is a file
     *
     * @return True, if a file, false otherwise
     */
    boolean isFile();

    /**
     * Returns true if the path is a directory
     *
     * @return True, if a directory, false otherwise
     */
    boolean isDirectory();

    /**
     * Returns the file extension of a file without the dot.
     *
     * Returns an empty string if a directory or no dot is
     * present in the filename.
     *
     * @return The file extension of a file
     */
    String getFileExtension();
}
