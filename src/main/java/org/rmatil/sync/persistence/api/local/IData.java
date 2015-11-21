package org.rmatil.sync.persistence.api.local;

import net.tomp2p.storage.Data;

public interface IData {

    /**
     * Returns the actual data
     *
     * @return Data The data
     */
    Data getData();
}
