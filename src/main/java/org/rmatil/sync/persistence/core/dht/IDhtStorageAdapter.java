package org.rmatil.sync.persistence.core.dht;

import org.rmatil.sync.persistence.api.IStorageAdapter;

/**
 * An interface for DHT storage adapters
 * which may or may not provide domain protection
 */
public interface IDhtStorageAdapter extends IStorageAdapter<DhtPathElement> {

}
