package org.rmatil.sync.persistence.core.dht.secured;

import net.tomp2p.dht.PeerDHT;
import org.rmatil.sync.persistence.api.IFileMetaInfo;
import org.rmatil.sync.persistence.api.StorageType;
import org.rmatil.sync.persistence.core.dht.base.BaseDhtStorageAdapter;
import org.rmatil.sync.persistence.exceptions.InputOutputException;

/**
 * A DHT Storage adapter which uses domain protection
 * of the given {@link PeerDHT} to protect values.
 */
public class SecuredDhtStorageAdapter extends BaseDhtStorageAdapter implements ISecuredDhtStorageAdapter {


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
    public SecuredDhtStorageAdapter(PeerDHT dht) {
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
    public SecuredDhtStorageAdapter(PeerDHT dht, long timeToLive) {
        super(dht, timeToLive);

        if (null == dht.peerBean().keyPair().getPublic() ||
                null == dht.peerBean().keyPair().getPrivate()) {
            // we require a public private key pair to protect domains
            throw new IllegalArgumentException("The given peer dht must have a public private keypair set");
        }

    }

    @Override
    public void persist(StorageType type, SecuredDhtPathElement path, byte[] bytes)
            throws InputOutputException {

        super.persist(type, path, bytes);
    }


    @Override
    public void persist(StorageType type, SecuredDhtPathElement path, long offset, byte[] bytes)
            throws InputOutputException {
        super.persist(type, path, offset, bytes);
    }

    @Override
    public void delete(SecuredDhtPathElement path)
            throws InputOutputException {
        super.delete(path);
    }

    @Override
    public byte[] read(SecuredDhtPathElement path)
            throws InputOutputException {
        return super.read(path);
    }

    @Override
    public byte[] read(SecuredDhtPathElement path, long offset, int length)
            throws InputOutputException {

        return super.read(path, offset, length);
    }

    @Override
    public void move(StorageType storageType, SecuredDhtPathElement oldPath, SecuredDhtPathElement newPath)
            throws InputOutputException {
        super.move(storageType, oldPath, newPath);
    }

    @Override
    public IFileMetaInfo getMetaInformation(SecuredDhtPathElement path)
            throws InputOutputException {
        return super.getMetaInformation(path);
    }

    @Override
    public boolean exists(StorageType storageType, SecuredDhtPathElement path)
            throws InputOutputException {
        return super.exists(storageType, path);
    }

    @Override
    public String getChecksum(SecuredDhtPathElement path)
            throws InputOutputException {
        return super.getChecksum(path);
    }
}
