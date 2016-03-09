package org.rmatil.sync.persistence.core.dht.unsecured;

import org.rmatil.sync.persistence.core.dht.DhtPathElement;

/**
 * An unprotected dht path element
 * does not contain a domain key
 */
public class UnsecuredDhtPathElement extends DhtPathElement {

    /**
     * @param locationKey A string which is used as location key in the DHT
     * @param contentKey  A string which gets used as content key in the DHT
     */
    public UnsecuredDhtPathElement(String locationKey, String contentKey) {
        super(locationKey, contentKey);
    }
}
