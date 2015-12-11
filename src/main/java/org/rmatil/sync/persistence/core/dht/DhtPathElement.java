package org.rmatil.sync.persistence.core.dht;

import org.rmatil.sync.persistence.api.IPathElement;

/**
 * A path element representing data in the DHT
 */
public class DhtPathElement implements IPathElement {

    /**
     * The content key of the path element which should be used
     */
    protected String contentKey;

    /**
     * @param contentKey A string which gets used as content key in the DHT
     */
    public DhtPathElement(String contentKey) {
        this.contentKey = contentKey;
    }

    /**
     * Returns the content key
     *
     * @return A string representing the path of the element
     */
    @Override
    public String getPath() {
        return this.contentKey;
    }
}
