package org.rmatil.sync.persistence.api;

import net.tomp2p.dht.Storage;
import org.rmatil.sync.persistence.exceptions.InputOutputException;

import java.nio.file.Path;

/**
 * An adapter for various storage implementations.
 * This could include implementations for local storage, Dropbox, ...
 */
public interface IStorageAdapter {

    /**
     * Persists the given bytes at the given path
     *
     * @param type  The type of path which should be created
     * @param path  The path used to identify the data
     * @param bytes The bytes to store (may be null if storageType is a directory)
     *
     * @throws InputOutputException If an error occurred during persisting
     */
    void persist(StorageType type, IPathElement path, byte[] bytes)
            throws InputOutputException;

    /**
     * Deletes the content stored at path
     *
     * @param path The path to remove
     *
     * @throws InputOutputException If an error occurred during deletion
     */
    void delete(IPathElement path)
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
    byte[] read(IPathElement path)
            throws InputOutputException;

    /**
     * Checks whether the given path already exists
     *
     * @param storageType The storage type to check for
     * @param path        The path to check
     *
     * @return Returns true, if existing, false otherwise
     */
    boolean exists(StorageType storageType, IPathElement path);

    /**
     * Returns the root directory of this storage adapter
     *
     * @return The root directory of this storage adapter
     */
    Path getRootDir();
}
