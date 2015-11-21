package org.rmatil.sync.persistence.api.local;

import net.tomp2p.storage.Data;

public interface ISendRequestData {

    /**
     * Get the name of the file
     *
     * @return The filename
     */
    String getFilename();

    /**
     * Get the relative path to the root of the synchronized folder
     *
     * @return The relative path to the synchronized folder
     */
    String getPath();

    /**
     * Get the actual data of the path element
     *
     * @return The actual content of the path element
     */
    Data getData();
}
