package org.rmatil.sync.persistence.core.tree;

import org.rmatil.sync.persistence.api.IPathElement;

/**
 * An element which represents a certain element
 * in a tree-like storage adapter
 */
public class TreePathElement implements IPathElement {

    /**
     * The path to the element
     */
    protected String path;

    /**
     * Creates a new path element
     *
     * @param path The path to the element
     */
    public TreePathElement(String path) {
        this.path = path;
    }

    /**
     * Returns the path to element
     *
     * @return The path to the element
     */
    public String getPath() {
        return this.path;
    }
}
