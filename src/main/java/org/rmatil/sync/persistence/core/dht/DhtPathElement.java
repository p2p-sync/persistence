package org.rmatil.sync.persistence.core.dht;

import net.tomp2p.peers.Number160;
import net.tomp2p.utils.Utils;
import org.rmatil.sync.persistence.api.IPathElement;

import java.security.KeyPair;

/**
 * A path element representing data in the DHT
 */
public class DhtPathElement implements IPathElement {

    /**
     * The location key of the path element which should be used
     * (e.g. the hash of the user's name)
     */
    protected Number160 locationKey;

    /**
     * The raw location key
     */
    protected String rawLocationKey;

    /**
     * The content key of the path element which should be used
     * (e.g. the hash of the string "LOCATIONS")
     */
    protected Number160 contentKey;

    /**
     * The raw content key
     */
    protected String rawContentKey;

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
     * @param contentKey A string which gets used as content key in the DHT
     * @param domainKey A public key which is used to access the path in the DHT
     */
    public DhtPathElement(String locationKey, String contentKey, String domainKey) {
        this.locationKey = Utils.makeSHAHash(locationKey);
        this.rawLocationKey = locationKey;
        this.contentKey = Utils.makeSHAHash(contentKey);
        this.rawContentKey = contentKey;
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

    public Number160 getLocationKey() {
        return locationKey;
    }

    public String getRawLocationKey() {
        return rawLocationKey;
    }

    public Number160 getContentKey() {
        return contentKey;
    }

    public String getRawContentKey() {
        return rawContentKey;
    }

    public String getRawDomainKey() {
        return rawDomainKey;
    }
}
