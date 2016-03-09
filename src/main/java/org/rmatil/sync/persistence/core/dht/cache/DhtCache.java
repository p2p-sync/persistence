package org.rmatil.sync.persistence.core.dht.cache;

import org.rmatil.sync.commons.collection.Pair;
import org.rmatil.sync.persistence.core.dht.DhtPathElement;
import org.rmatil.sync.persistence.core.dht.secured.SecuredDhtStorageAdapter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A cache layer for the {@link SecuredDhtStorageAdapter}.
 */
public class DhtCache implements IDhtCache {

    /**
     * The time to live for each element in the cache
     */
    protected long timeToLive;

    /**
     * The "cache"
     */
    protected ConcurrentHashMap<String, Pair<Long, byte[]>> cache;

    /**
     * @param timeToLive The time to live for all path elements
     */
    public DhtCache(long timeToLive) {
        this.timeToLive = timeToLive;
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public synchronized void put(DhtPathElement pathElement, byte[] bytes) {
        this.cache.put(pathElement.getPath(), new Pair<>(System.currentTimeMillis() + timeToLive, bytes));
    }

    @Override
    public synchronized byte[] get(DhtPathElement pathElement) {
        long now = System.currentTimeMillis();

        Pair<Long, byte[]> pair = this.cache.get(pathElement.getPath());

        if (null != pair && pair.getFirst() > now) {
            return pair.getSecond();
        }

        return null;
    }

    @Override
    public synchronized void clear(DhtPathElement pathElement) {
        this.cache.remove(pathElement.getPath());
    }

    @Override
    public synchronized void clear() {
        this.cache.clear();
    }
}
