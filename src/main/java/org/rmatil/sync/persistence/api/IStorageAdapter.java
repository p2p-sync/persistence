package org.rmatil.sync.persistence.api;

import org.rmatil.sync.persistence.exceptions.InputOutputException;

/**
 * An adapter for various storage implementations.
 * This could include implementations for local storage, Dropbox, ...
 */
public interface IStorageAdapter<T extends IPathElement> {

    /**
     * Persists the given bytes at the given path
     *
     * @param type  The type of path which should be created
     * @param path  The path used to identify the data
     * @param bytes The bytes to store (may be null if storageType is a directory)
     *
     * @throws InputOutputException If an error occurred during persisting
     */
    void persist(StorageType type, T path, byte[] bytes)
            throws InputOutputException;

    /**
     * Persists the given bytes of the given path at the given offset.
     * Note, that data is overwritten and not appended, if the offset is smaller
     * than the total size of the file!
     *
     * @param type   The type of path which should be created
     * @param path   The path used to identify the data
     * @param offset The offset where to start writing
     * @param bytes  The bytes to store (may be null if storageType is a directory)
     *
     * @throws InputOutputException If an error occurred during persisting
     */
    void persist(StorageType type, T path, long offset, byte[] bytes)
            throws InputOutputException;

    /**
     * Deletes the content stored at path
     *
     * @param path The path to remove
     *
     * @throws InputOutputException If an error occurred during deletion
     */
    void delete(T path)
            throws InputOutputException;

    /**
     * Reads the contents stored at path
     *
     * @param path The path from which to read
     *
     * @return The content as byte array
     *
     * @throws InputOutputException If an error occurred during reading
     */
    byte[] read(T path)
            throws InputOutputException;

    /**
     * Reads the contents stored at the given path and specified by the offset and length
     *
     * @param path   The path from which to read
     * @param offset The offset where to start reading
     * @param length The length to read
     *
     * @return The read content. If length is exceeding the file's content, then the returned array will be shorter than the given length
     *
     * @throws InputOutputException If an error occurred during reading
     */
    byte[] read(T path, long offset, int length)
            throws InputOutputException;

    /**
     * Moves the contents stored at oldPath to newPath.
     *
     * @param storageType The storage type of the old and new path
     * @param oldPath     The old path
     * @param newPath     The new path to move the contents
     *
     * @throws InputOutputException If the target path already exists
     */
    void move(StorageType storageType, T oldPath, T newPath)
            throws InputOutputException;

    /**
     * Returns some meta information about the given path
     *
     * @param path The path element of which to get the meta information
     *
     * @return The meta information
     *
     * @throws InputOutputException If an error occurred during fetching the meta information
     */
    IFileMetaInfo getMetaInformation(T path)
            throws InputOutputException;

    /**
     * Checks whether the given path already exists
     *
     * @param storageType The storage type to check for
     * @param path        The path to check
     *
     * @return Returns true, if existing, false otherwise
     *
     * @throws InputOutputException If an error occurred during checking for existence
     */
    boolean exists(StorageType storageType, T path)
            throws InputOutputException;

    /**
     * Returns the checksum of a particular directory or file
     *
     * @param path The path from which to get the checksum
     *
     * @return A string having the checksum in it
     *
     * @throws InputOutputException If generating the checksum failed
     */
    String getChecksum(T path)
            throws InputOutputException;
}
