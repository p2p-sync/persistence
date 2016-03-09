package org.rmatil.sync.persistence.core.dht.unsecured;

import net.tomp2p.dht.PeerDHT;
import org.rmatil.sync.persistence.api.IFileMetaInfo;
import org.rmatil.sync.persistence.api.StorageType;
import org.rmatil.sync.persistence.core.dht.base.BaseDhtStorageAdapter;
import org.rmatil.sync.persistence.core.dht.secured.SecuredDhtStorageAdapter;
import org.rmatil.sync.persistence.exceptions.InputOutputException;

/**
 * A DHT Storage adapter which does not use domain protection
 * for storing values.
 * <p>
 * See {@link SecuredDhtStorageAdapter} for storing values with domain protection
 */
public class UnsecuredDhtStorageAdapter extends BaseDhtStorageAdapter implements IUnsecuredDhtStorageAdapter {


    /**
     * Creates a storage adapter for the DHT which does not protect
     * values in the DHT so that each peer can modify them.
     *
     * @param dht A PeerDHT bootstrapped with domain protection
     */
    public UnsecuredDhtStorageAdapter(PeerDHT dht) {
        super(dht);
    }

    /**
     * Creates a storage adapter for the DHT which does not protect
     * values in the DHT so that each peer can modify them.
     *
     * @param dht A PeerDHT bootstrapped with domain protection
     */
    public UnsecuredDhtStorageAdapter(PeerDHT dht, long timeToLive) {
        super(dht, timeToLive);
    }

    @Override
    public void persist(StorageType type, UnsecuredDhtPathElement path, byte[] bytes)
            throws InputOutputException {
        super.persist(type, path, bytes);
    }

    @Override
    public void persist(StorageType type, UnsecuredDhtPathElement path, long offset, byte[] bytes)
            throws InputOutputException {
        super.persist(type, path, offset, bytes);
    }

    @Override
    public void delete(UnsecuredDhtPathElement path)
            throws InputOutputException {
        super.delete(path);
    }

    @Override
    public byte[] read(UnsecuredDhtPathElement path)
            throws InputOutputException {
        return super.read(path);
    }

    @Override
    public byte[] read(UnsecuredDhtPathElement path, long offset, int length)
            throws InputOutputException {
        return super.read(path, offset, length);
    }

    @Override
    public void move(StorageType storageType, UnsecuredDhtPathElement oldPath, UnsecuredDhtPathElement newPath)
            throws InputOutputException {
        super.move(storageType, oldPath, newPath);
    }

    @Override
    public IFileMetaInfo getMetaInformation(UnsecuredDhtPathElement path)
            throws InputOutputException {
        return super.getMetaInformation(path);
    }

    @Override
    public boolean exists(StorageType storageType, UnsecuredDhtPathElement path)
            throws InputOutputException {
        return super.exists(storageType, path);
    }

    @Override
    public String getChecksum(UnsecuredDhtPathElement path)
            throws InputOutputException {
        return super.getChecksum(path);
    }

}