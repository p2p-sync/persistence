package org.rmatil.sync.persistence.test.core.dht;

import org.junit.Test;
import org.rmatil.sync.persistence.core.dht.DhtCache;
import org.rmatil.sync.persistence.core.dht.DhtPathElement;
import org.rmatil.sync.persistence.core.dht.IDhtCache;

import static org.junit.Assert.*;

public class DhtCacheTest {

    protected static DhtPathElement dhtPathElement  = new DhtPathElement(
            "locationKey",
            "contentKey",
            "domainKey"
    );
    protected static DhtPathElement dhtPathElement2 = new DhtPathElement(
            "locationKey2",
            "contentKey2",
            "domainKey2"
    );
    protected static byte[]         content1        = "This is the content for the first element".getBytes();
    protected static byte[]         content2        = "This is the content for the second element".getBytes();


    @Test
    public void test() {
        // time to live is 2000ms
        IDhtCache dhtCache = new DhtCache(2000L);

        dhtCache.put(dhtPathElement, content1);

        assertArrayEquals("Content should be equal", content1, dhtCache.get(dhtPathElement));
        assertNull("Content should be null", dhtCache.get(dhtPathElement2));

        dhtCache.put(dhtPathElement, content2);

        assertArrayEquals("Content should be equal", content2, dhtCache.get(dhtPathElement));
        assertNull("Content should be null", dhtCache.get(dhtPathElement2));

        dhtCache.clear();

        assertNull("Content should be null", dhtCache.get(dhtPathElement));
        assertNull("Content should be null", dhtCache.get(dhtPathElement2));
    }
}
