package org.rmatil.sync.persistence.core.dht;

import net.tomp2p.peers.Number160;
import net.tomp2p.utils.Utils;
import org.rmatil.sync.persistence.api.IPathElement;

import java.security.Key;

/**
 * A path element representing data in the DHT
 */
public class DhtPathElement implements IPathElement {

    /**
     * The content key of the path element which should be used
     */
    protected String contentKey;

    /**
     * The domain protection key which should be used
     */
    protected Number160 domainProtectionKey;

    /**
     * @param contentKey A string which gets used as content key in the DHT
     * @param domainProtectionKey A public key which is used to access the path in the DHT
     */
    public DhtPathElement(String contentKey, Key domainProtectionKey) {
        this.contentKey = contentKey;
        this.domainProtectionKey = Utils.makeSHAHash(domainProtectionKey.getEncoded());
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

    /**
     * Returns the domain protection key which is used
     * to access the content in the DHT
     *
     * @return The domain protection key
     */
    public Number160 getDomainProtectionKey() {
        return domainProtectionKey;
    }
}
