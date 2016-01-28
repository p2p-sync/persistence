package org.rmatil.sync.persistence.core.dht;

import net.tomp2p.peers.Number160;
import net.tomp2p.utils.Utils;
import org.rmatil.sync.persistence.core.dht.base.ADhtPathElement;

/**
 * A path element representing data in the DHT
 */
public class DhtPathElement extends ADhtPathElement {

    /**
     * The domain key which should be used
     */
    protected Number160 domainKey;

    /**
     * The raw domain key
     */
    protected String rawDomainKey;

    /**
     * @param locationKey A string which is used as location key in the DHT
     * @param contentKey  A string which gets used as content key in the DHT
     * @param domainKey   A public key which is used to access the path in the DHT
     */
    public DhtPathElement(String locationKey, String contentKey, String domainKey) {
        super(locationKey, contentKey);
        this.domainKey = Utils.makeSHAHash(domainKey);
        this.rawDomainKey = domainKey;
    }

    /**
     * Returns the content key
     *
     * @return A string representing the path of the element
     */
    @Override
    public String getPath() {
        return this.rawLocationKey + "/" + this.rawContentKey + "/" + this.rawDomainKey;
    }

    /**
     * Returns the domain protection key which is used
     * to access the content in the DHT
     *
     * @return The domain protection key
     */
    public Number160 getDomainKey() {
        return domainKey;
    }

    public String getRawDomainKey() {
        return rawDomainKey;
    }
}
