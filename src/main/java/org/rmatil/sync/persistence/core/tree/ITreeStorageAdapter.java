package org.rmatil.sync.persistence.core.tree;

import org.rmatil.sync.persistence.api.IStorageAdapter;
import org.rmatil.sync.persistence.exceptions.InputOutputException;

import java.util.List;

/**
 * An interface for storage adapters managing
 * their data in a tree like data structure.
 */
public interface ITreeStorageAdapter extends IStorageAdapter<TreePathElement> {

    /**
     * Checks whether the given path element represents a file
     *
     * @param path The path to check
     *
     * @return True, if the given path is a file, false otherwise
     *
     * @throws InputOutputException If the path does not exist
     */
    boolean isFile(TreePathElement path)
            throws InputOutputException;

    /**
     * Checks whether the givne path element represents a directory
     *
     * @param path The path to check
     *
     * @return True, if the given path is a directory, false otherwise
     *
     * @throws InputOutputException If the path does not exists
     */
    boolean isDir(TreePathElement path)
            throws InputOutputException;


    /**
     * Returns all contents of the given directory relative to the root directory
     * of the given object store
     *
     * @param directory The directory of which to get its contents
     *
     * @return A list of all directory contents
     *
     * @throws InputOutputException If the given path is not a directory
     */
    List<TreePathElement> getDirectoryContents(TreePathElement directory)
            throws InputOutputException;

    /**
     * Returns the root directory of this storage adapter
     *
     * @return The root directory of this storage adapter
     */
    TreePathElement getRootDir();
}
