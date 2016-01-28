package org.rmatil.sync.persistence.core.dht;

import net.tomp2p.dht.PeerDHT;
import org.rmatil.sync.persistence.api.IFileMetaInfo;
import org.rmatil.sync.persistence.api.IPathElement;
import org.rmatil.sync.persistence.api.IStorageAdapter;
import org.rmatil.sync.persistence.api.StorageType;
import org.rmatil.sync.persistence.core.dht.base.ADhtStorageAdapter;
import org.rmatil.sync.persistence.exceptions.InputOutputException;

import java.nio.file.Path;
import java.util.List;

/**
 * A DHT Storage adapter which does not use domain protection
 * for storing values.
 * <p>
 * See {@link DhtStorageAdapter} for storing values with domain protection
 */
public class UnprotectedDhtStorageAdapter extends ADhtStorageAdapter implements IStorageAdapter {


    /**
     * Creates a storage adapter for the DHT which does not protect
     * values in the DHT so that each peer can modify them.
     *
     * @param dht A PeerDHT bootstrapped with domain protection
     */
    public UnprotectedDhtStorageAdapter(PeerDHT dht) {
        super(dht);
    }

    /**
     * Creates a storage adapter for the DHT which does not protect
     * values in the DHT so that each peer can modify them.
     *
     * @param dht A PeerDHT bootstrapped with domain protection
     */
    public UnprotectedDhtStorageAdapter(PeerDHT dht, long timeToLive) {
        super(dht, timeToLive);
    }

    @Override
    public void persist(StorageType type, IPathElement path, byte[] bytes)
            throws InputOutputException {

        if (! (path instanceof UnprotectedDhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        super.persist(type, (UnprotectedDhtPathElement) path, bytes, false);
    }

    @Override
    public void persist(StorageType type, IPathElement path, long offset, byte[] bytes)
            throws InputOutputException {

        if (! (path instanceof UnprotectedDhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        super.persist(type, (UnprotectedDhtPathElement) path, offset, bytes, false);
    }

    @Override
    public void delete(IPathElement path)
            throws InputOutputException {

        if (! (path instanceof UnprotectedDhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        super.delete((UnprotectedDhtPathElement) path, false);
    }

    @Override
    public byte[] read(IPathElement path)
            throws InputOutputException {

        if (! (path instanceof UnprotectedDhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        return super.read((UnprotectedDhtPathElement) path, false);
    }

    @Override
    public byte[] read(IPathElement path, long offset, int length)
            throws InputOutputException {

        if (! (path instanceof UnprotectedDhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        return super.read((UnprotectedDhtPathElement) path, offset, length, false);
    }

    @Override
    public void move(StorageType storageType, IPathElement oldPath, IPathElement newPath)
            throws InputOutputException {

        if (! (oldPath instanceof UnprotectedDhtPathElement && newPath instanceof UnprotectedDhtPathElement)) {
            throw new InputOutputException("Could not use path element (" + oldPath.getClass().getName() + " or " + newPath.getClass().getName() + ") for DHT Storage Adapter");
        }

        super.move(storageType, (UnprotectedDhtPathElement) oldPath, (UnprotectedDhtPathElement) newPath, false);
    }

    @Override
    public IFileMetaInfo getMetaInformation(IPathElement path)
            throws InputOutputException {

        if (! (path instanceof UnprotectedDhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        return super.getMetaInformation((UnprotectedDhtPathElement) path, false);
    }

    @Override
    public boolean exists(StorageType storageType, IPathElement path)
            throws InputOutputException {

        if (! (path instanceof UnprotectedDhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        return super.exists(storageType, (UnprotectedDhtPathElement) path, false);
    }

    @Override
    public boolean isFile(IPathElement path)
            throws InputOutputException {

        if (! (path instanceof UnprotectedDhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        return super.isFile((UnprotectedDhtPathElement) path, false);
    }

    @Override
    public boolean isDir(IPathElement path)
            throws InputOutputException {

        if (! (path instanceof UnprotectedDhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        return super.isDir((UnprotectedDhtPathElement) path, false);
    }

    @Override
    public List<IPathElement> getDirectoryContents(IPathElement directory)
            throws InputOutputException {

        if (! (directory instanceof UnprotectedDhtPathElement)) {
            throw new InputOutputException("Could not use path element " + directory.getClass().getName() + " for DHT Storage Adapter");
        }

        throw new InputOutputException("Directories are not supported");
    }

    @Override
    public Path getRootDir() {
        return super.getRootDir();
    }
}
