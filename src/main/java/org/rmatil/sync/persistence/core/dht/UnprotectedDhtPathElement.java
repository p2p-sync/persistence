package org.rmatil.sync.persistence.core.dht;

import org.rmatil.sync.persistence.core.dht.base.ADhtPathElement;

/**
 * An unprotected dht path element
 * does not contain a domain key
 */
public class UnprotectedDhtPathElement extends ADhtPathElement {

    /**
     * @param locationKey A string which is used as location key in the DHT
     * @param contentKey  A string which gets used as content key in the DHT
     */
    public UnprotectedDhtPathElement(String locationKey, String contentKey) {
        super(locationKey, contentKey);
    }

    /**
     * Returns an unprotected dht path element for the given dht path element
     *
     * @param pathElement The element from which to get its unprotected variant
     *
     * @return The created unprotected dht path element
     */
    public static UnprotectedDhtPathElement fromDhtPathElement(DhtPathElement pathElement) {
        return new UnprotectedDhtPathElement(
                pathElement.getRawLocationKey(),
                pathElement.getRawContentKey()
        );
    }

    /**
     * Returns the content key
     *
     * @return A string representing the path of the element
     */
    @Override
    public String getPath() {
        return this.rawLocationKey + "/" + this.rawContentKey;
    }
}
