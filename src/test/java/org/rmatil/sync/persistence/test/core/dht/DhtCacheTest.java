package org.rmatil.sync.persistence.test.core.dht;

import org.junit.Test;
import org.rmatil.sync.persistence.core.dht.cache.DhtCache;
import org.rmatil.sync.persistence.core.dht.secured.SecuredDhtPathElement;
import org.rmatil.sync.persistence.core.dht.cache.IDhtCache;

import static org.junit.Assert.*;

public class DhtCacheTest {

    protected static SecuredDhtPathElement securedDhtPathElement  = new SecuredDhtPathElement(
            "locationKey",
            "contentKey",
            "domainKey"
    );
    protected static SecuredDhtPathElement securedDhtPathElement2 = new SecuredDhtPathElement(
            "locationKey2",
            "contentKey2",
            "domainKey2"
    );
    protected static byte[]                content1               = "This is the content for the first element".getBytes();
    protected static byte[]                content2               = "This is the content for the second element".getBytes();


    @Test
    public void test() {
        // time to live is 2000ms
        IDhtCache dhtCache = new DhtCache(2000L);

        dhtCache.put(securedDhtPathElement, content1);

        assertArrayEquals("Content should be equal", content1, dhtCache.get(securedDhtPathElement));
        assertNull("Content should be null", dhtCache.get(securedDhtPathElement2));

        dhtCache.put(securedDhtPathElement, content2);

        assertArrayEquals("Content should be equal", content2, dhtCache.get(securedDhtPathElement));
        assertNull("Content should be null", dhtCache.get(securedDhtPathElement2));

        dhtCache.clear();

        assertNull("Content should be null", dhtCache.get(securedDhtPathElement));
        assertNull("Content should be null", dhtCache.get(securedDhtPathElement2));
    }
}
