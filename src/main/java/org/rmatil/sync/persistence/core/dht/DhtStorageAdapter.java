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
 * A DHT Storage adapter which uses domain protection
 * of the given {@link PeerDHT} to protect values.
 */
public class DhtStorageAdapter extends ADhtStorageAdapter implements IStorageAdapter {


    /**
     * Creates a storage adapter for the DHT. To protect each peers domain, the domain
     * keys of a path element are used
     * <p>
     * Note: To make use of domain protection, the given PeerDHT must be bootstrapped like
     * in the following example:
     * <p>
     * <pre>
     *     KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");
     *     KeyPair keyPair = gen.generateKeyPair();
     *
     *     PeerDHT peer = new PeerBuilderDHT(new PeerBuilder(keyPair).ports(4003).start()).start();
     *     peer
     *       .storageLayer()
     *       .protection(
     *         ProtectionEnable.ALL,
     *         ProtectionMode.NO_MASTER, // allows other clients to read from the storage
     *         ProtectionEnable.ALL,
     *         ProtectionMode.NO_MASTER // allows other clients to read from the storage
     *       );
     * </pre>
     *
     * @param dht A PeerDHT bootstrapped with domain protection
     */
    public DhtStorageAdapter(PeerDHT dht) {
        super(dht);
        if (null == dht.peerBean().keyPair().getPublic() ||
                null == dht.peerBean().keyPair().getPrivate()) {
            // we require a public private key pair to protect domains
            throw new IllegalArgumentException("The given peer dht must have a public private keypair set");
        }

    }

    /**
     * Creates a storage adapter for the DHT. This storage adapter is dedicated
     * to the given location key. To protect each peers domain, the given domain
     * protection key is used.
     * <p>
     * Note: To make use of domain protection, the given PeerDHT must be bootstrapped like
     * in the following example:
     * <p>
     * <pre>
     *     KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");
     *     KeyPair keyPair = gen.generateKeyPair();
     *
     *     PeerDHT peer = new PeerBuilderDHT(new PeerBuilder(keyPair).ports(4003).start()).start();
     *     peer
     *       .storageLayer()
     *       .protection(
     *         ProtectionEnable.ALL,
     *         ProtectionMode.NO_MASTER // allows other clients to read from the storage,
     *         ProtectionEnable.ALL,
     *         ProtectionMode.NO_MASTER // allows other clients to read from the storage
     *       );
     * </pre>
     *
     * @param dht A PeerDHT bootstrapped with domain protection
     */
    public DhtStorageAdapter(PeerDHT dht, long timeToLive) {
        super(dht, timeToLive);

        if (null == dht.peerBean().keyPair().getPublic() ||
                null == dht.peerBean().keyPair().getPrivate()) {
            // we require a public private key pair to protect domains
            throw new IllegalArgumentException("The given peer dht must have a public private keypair set");
        }

    }

    @Override
    public void persist(StorageType type, IPathElement path, byte[] bytes)
            throws InputOutputException {

        if (! (path instanceof DhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        super.persist(type, (DhtPathElement) path, bytes, true);
    }

    @Override
    public void persist(StorageType type, IPathElement path, long offset, byte[] bytes)
            throws InputOutputException {

        if (! (path instanceof DhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        super.persist(type, (DhtPathElement) path, offset, bytes, true);
    }

    @Override
    public void delete(IPathElement path)
            throws InputOutputException {

        if (! (path instanceof DhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        super.delete((DhtPathElement) path, true);
    }

    @Override
    public byte[] read(IPathElement path)
            throws InputOutputException {

        if (! (path instanceof DhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        return super.read((DhtPathElement) path, true);
    }

    @Override
    public byte[] read(IPathElement path, long offset, int length)
            throws InputOutputException {

        if (! (path instanceof DhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        return super.read((DhtPathElement) path, offset, length, true);
    }

    @Override
    public void move(StorageType storageType, IPathElement oldPath, IPathElement newPath)
            throws InputOutputException {

        if (! (oldPath instanceof DhtPathElement && newPath instanceof DhtPathElement)) {
            throw new InputOutputException("Could not use path element (" + oldPath.getClass().getName() + " or " + newPath.getClass().getName() + ") for DHT Storage Adapter");
        }

        super.move(storageType, (DhtPathElement) oldPath, (DhtPathElement) newPath, true);
    }

    @Override
    public IFileMetaInfo getMetaInformation(IPathElement path)
            throws InputOutputException {

        if (! (path instanceof DhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        return super.getMetaInformation((DhtPathElement) path, true);
    }

    @Override
    public boolean exists(StorageType storageType, IPathElement path)
            throws InputOutputException {

        if (! (path instanceof DhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        return super.exists(storageType, (DhtPathElement) path, true);
    }

    @Override
    public boolean isFile(IPathElement path)
            throws InputOutputException {

        if (! (path instanceof DhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        return super.isFile((DhtPathElement) path, true);
    }

    @Override
    public boolean isDir(IPathElement path)
            throws InputOutputException {

        if (! (path instanceof DhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        return super.isDir((DhtPathElement) path, true);
    }

    @Override
    public List<IPathElement> getDirectoryContents(IPathElement directory)
            throws InputOutputException {

        if (! (directory instanceof DhtPathElement)) {
            throw new InputOutputException("Could not use path element " + directory.getClass().getName() + " for DHT Storage Adapter");
        }

        throw new InputOutputException("Directories are not supported");
    }

    @Override
    public Path getRootDir() {
        return super.getRootDir();
    }
}
