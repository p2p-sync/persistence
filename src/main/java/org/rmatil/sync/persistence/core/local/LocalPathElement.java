package org.rmatil.sync.persistence.core.local;

import org.rmatil.sync.persistence.api.IPathElement;

/**
 * An element which represents a certain file or directory on disk
 */
public class LocalPathElement implements IPathElement {

    /**
     * The path to the file or directory
     */
    protected String path;

    /**
     * Creates a new path element
     *
     * @param path The path to the file or directory
     */
    public LocalPathElement(String path) {
        this.path = path;
    }

    /**
     * Returns the path to the file or directory
     *
     * @return The path to the file or directory
     */
    public String getPath() {
        return this.path;
    }
}
