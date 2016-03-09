package org.rmatil.sync.persistence.core.dht.cache;

import org.rmatil.sync.persistence.core.dht.DhtPathElement;
import org.rmatil.sync.persistence.core.dht.secured.SecuredDhtStorageAdapter;

/**
 * The <code>IDhtCache</code> interface should be implemented
 * by classes which provide a cache layer to the {@link SecuredDhtStorageAdapter}
 * to reduce the load in the network.
 * <p>
 * The time to live should be equal for all elements in the cache,
 * whereas it is specified by the implementing class.
 */
public interface IDhtCache {

    /**
     * Puts the given path element in the cache
     *
     * @param pathElement The path element
     * @param bytes       The associated data to cache
     */
    void put(DhtPathElement pathElement, byte[] bytes);

    /**
     * Returns the data stored in the cache for the given
     * path element. If no associated data is found for the
     * path, then null is returned.
     *
     * @param pathElement The path element from which to get the data
     *
     * @return The data or null, if no element is associated with the given path
     */
    byte[] get(DhtPathElement pathElement);

    /**
     * Resets the content of the given path element
     *
     * @param pathElement The path element for which to reset the cache
     */
    void clear(DhtPathElement pathElement);

    /**
     * Clears the whole cache
     */
    void clear();
}
