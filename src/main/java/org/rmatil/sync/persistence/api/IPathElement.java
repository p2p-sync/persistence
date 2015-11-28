package org.rmatil.sync.persistence.api;

/**
 * A path element used to identify a particular content
 */
public interface IPathElement {

    /**
     * Returns the path (including the filename, if a file)
     * of this path element
     *
     * @return The path to this path element
     */
    String getPath();

}
