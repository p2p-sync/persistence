package org.rmatil.sync.persistence.api.local;

public interface IGetRequestData {

    /**
     * Get the name of the file which should be returned by the other client
     *
     * @return The filename
     */
    String getFilename();

    /**
     * Get the relative path to the root of the synchronized folder
     *
     * @return The relative path
     */
    String getPath();
}
