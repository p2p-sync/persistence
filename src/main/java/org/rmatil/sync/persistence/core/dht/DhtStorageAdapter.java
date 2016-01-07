package org.rmatil.sync.persistence.core.dht;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.storage.Data;
import org.rmatil.sync.persistence.api.IFileMetaInfo;
import org.rmatil.sync.persistence.api.IPathElement;
import org.rmatil.sync.persistence.api.IStorageAdapter;
import org.rmatil.sync.persistence.api.StorageType;
import org.rmatil.sync.persistence.core.FileMetaInfo;
import org.rmatil.sync.persistence.core.dht.listener.DhtDeleteListener;
import org.rmatil.sync.persistence.core.dht.listener.DhtGetListener;
import org.rmatil.sync.persistence.core.dht.listener.DhtPutListener;
import org.rmatil.sync.persistence.exceptions.InputOutputException;

import java.nio.file.Path;


public class DhtStorageAdapter implements IStorageAdapter {

    protected final PeerDHT dht;

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
     *         ProtectionMode.MASTER_PUBLIC_KEY,
     *         ProtectionEnable.ALL,
     *         ProtectionMode.MASTER_PUBLIC_KEY
     *       );
     * </pre>
     *
     * @param dht A PeerDHT bootstrapped with domain protection
     */
    public DhtStorageAdapter(PeerDHT dht) {
        if (null == dht.peerBean().keyPair().getPublic() ||
                null == dht.peerBean().keyPair().getPrivate()) {
            // we require a public private key pair to protect domains
            throw new IllegalArgumentException("The given peer dht must have a public private keypair set");
        }

        this.dht = dht;
    }

    @Override
    synchronized public void persist(StorageType type, IPathElement path, byte[] bytes)
            throws InputOutputException {

        if (! (path instanceof DhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        if (StorageType.FILE != type) {
            throw new InputOutputException("Only files are allowed to be stored in the DHT");
        }

        Data data = new Data(bytes);

        FuturePut futurePut = this.dht
                .put(((DhtPathElement) path).getLocationKey())
                .data(((DhtPathElement) path).getContentKey(), data)
                .protectDomain()
                .domainKey(((DhtPathElement) path).getDomainKey())
                .start();

        futurePut.addListener(
                new DhtPutListener(this.dht)
        );

        try {
            futurePut.await();
        } catch (InterruptedException e) {
            // rethrow using our exception
            throw new InputOutputException(e);
        }
    }

    @Override
    synchronized public void persist(StorageType type, IPathElement path, long offset, byte[] bytes)
            throws InputOutputException {

        if (! (path instanceof DhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        if (StorageType.FILE != type) {
            throw new InputOutputException("Only files are allowed to be stored in the DHT");
        }

        byte[] existingBytes = this.read(path);

        // TODO: fix writing with "long" offset
        int intOffset = (int) offset;

        int newTotalSize;
        if (intOffset < existingBytes.length) {
            newTotalSize = existingBytes.length - Math.abs(intOffset - existingBytes.length) + bytes.length;
        } else {
            newTotalSize = Math.min(intOffset, existingBytes.length) + bytes.length;
            intOffset = newTotalSize - bytes.length;
        }

        if (newTotalSize < existingBytes.length) {
            newTotalSize = existingBytes.length;
        }

        byte[] targetBytes = new byte[newTotalSize];

        int maxAllowedWriteSize;
        if (existingBytes.length > newTotalSize) {
            maxAllowedWriteSize = newTotalSize;
        } else {
            maxAllowedWriteSize = existingBytes.length;
        }

        System.arraycopy(existingBytes, 0, targetBytes, 0, maxAllowedWriteSize);
        System.arraycopy(bytes, 0, targetBytes, intOffset, bytes.length);


        Data data = new Data(targetBytes);

        FuturePut futurePut = this.dht
                .put(((DhtPathElement) path).getLocationKey())
                .data(((DhtPathElement) path).getContentKey(), data)
                .protectDomain()
                .domainKey(((DhtPathElement) path).getDomainKey())
                .start();

        futurePut.addListener(
                new DhtPutListener(this.dht)
        );

        try {
            futurePut.await();
        } catch (InterruptedException e) {
            // rethrow using our exception
            throw new InputOutputException(e);
        }
    }

    @Override
    synchronized public void delete(IPathElement path)
            throws InputOutputException {

        if (! (path instanceof DhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        FutureRemove futureRemove = this.dht
                .remove(((DhtPathElement) path).getLocationKey())
                .contentKey(((DhtPathElement) path).getContentKey())
                .protectDomain()
                .domainKey(((DhtPathElement) path).getDomainKey())
                .start();

        futureRemove.addListener(
                new DhtDeleteListener(this.dht)
        );

        try {
            futureRemove.await();
        } catch (InterruptedException e) {
            // rethrow using our exception
            throw new InputOutputException(e);
        }

    }

    @Override
    synchronized public byte[] read(IPathElement path)
            throws InputOutputException {

        if (! (path instanceof DhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        FutureGet futureGet = this.dht
                .get(((DhtPathElement) path).getLocationKey())
                .contentKey(((DhtPathElement) path).getContentKey())
                .domainKey(((DhtPathElement) path).getDomainKey())
                .start();

        futureGet.addListener(
                new DhtGetListener(this.dht)
        );

        try {
            futureGet.await();
        } catch (InterruptedException e) {
            // rethrow using our exception
            throw new InputOutputException(e);
        }

        if (null == futureGet.data()) {
            return new byte[0];
        }

        return futureGet.data().toBytes();
    }

    @Override
    synchronized public byte[] read(IPathElement path, long offset, int length)
            throws InputOutputException {

        if (! (path instanceof DhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        FutureGet futureGet = this.dht
                .get(((DhtPathElement) path).getLocationKey())
                .contentKey(((DhtPathElement) path).getContentKey())
                .domainKey(((DhtPathElement) path).getDomainKey())
                .start();

        futureGet.addListener(
                new DhtGetListener(this.dht)
        );

        try {
            futureGet.await();
        } catch (InterruptedException e) {
            // rethrow using our exception
            throw new InputOutputException(e);
        }

        if (null == futureGet.data()) {
            return new byte[0];
        }

        byte[] contents = futureGet.data().toBytes();
        // check offset to be smaller than the fetched content
        // TODO: fix int conversion
        int srcPos = Math.min(contents.length, (int) offset);

        // check length to be smaller than the fetched content
        int maxLength;
        if (length > contents.length) {
            maxLength = contents.length;
        } else {
            maxLength = length;
        }

        // check that when reading from the offset, the length is at max. the length of the content
        if (maxLength + srcPos > contents.length) {
            maxLength = contents.length - srcPos;
        }

        byte[] chunk = new byte[maxLength];
        System.arraycopy(contents, srcPos, chunk, 0, maxLength);

        return chunk;
    }

    /**
     * <i>Note</i>: This implementation of moving uses the combination of
     * persist and remove to emulate a move. Therefore, the data stored in
     * the DHT is completely retransmitted through the network.
     * Due to consistency reasons, contents are first rewritten to the
     * new path and then removed from the old one.
     * <p>
     * {@inheritDoc}
     */
    @Override
    synchronized public void move(StorageType storageType, IPathElement oldPath, IPathElement newPath)
            throws InputOutputException {

        byte[] contents = this.read(oldPath);

        if (this.exists(storageType, newPath)) {
            throw new InputOutputException("Target path " + newPath.getPath() + " already exists");
        }

        // first try to write to new path
        this.persist(StorageType.FILE, newPath, contents);
        this.delete(oldPath);
    }

    @Override
    synchronized public IFileMetaInfo getMetaInformation(IPathElement path)
            throws InputOutputException {
        if (! (path instanceof DhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        if (! this.exists(StorageType.FILE, path)) {
            throw new InputOutputException("Could not get meta information for " + path.getPath() + ". No such file or directory");
        }

        FutureGet futureGet = this.dht
                .get(((DhtPathElement) path).getLocationKey())
                .contentKey(((DhtPathElement) path).getContentKey())
                .domainKey(((DhtPathElement) path).getDomainKey())
                .start();

        futureGet.addListener(
                new DhtGetListener(this.dht)
        );

        try {
            futureGet.await();
        } catch (InterruptedException e) {
            // rethrow using our exception
            throw new InputOutputException(e);
        }

        if (null == futureGet.data()) {
            return new FileMetaInfo(0, true, "");
        }

        return new FileMetaInfo(futureGet.data().toBytes().length, true, "");
    }

    @Override
    synchronized public boolean exists(StorageType storageType, IPathElement path)
            throws InputOutputException {

        if (! (path instanceof DhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        if (StorageType.FILE != storageType) {
            throw new InputOutputException("Only files are allowed to be read from the DHT");
        }

        FutureGet futureGet = this.dht
                .get(((DhtPathElement) path).getLocationKey())
                .contentKey(((DhtPathElement) path).getContentKey())
                .domainKey(((DhtPathElement) path).getDomainKey())
                .start();

        futureGet.addListener(
                new DhtGetListener(this.dht)
        );

        try {
            futureGet.await();
        } catch (InterruptedException e) {
            // rethrow using our exception
            throw new InputOutputException(e);
        }

        return null != futureGet.data();
    }

    @Override
    synchronized public boolean isFile(IPathElement path)
            throws InputOutputException {
        if (! this.exists(StorageType.FILE, path)) {
            throw new InputOutputException("Can not check whether the given path " + path.getPath() + " is a file. The element does not exist in the DHT");
        }

        // we only have "files" in the DHT
        return true;
    }

    @Override
    synchronized public boolean isDir(IPathElement path)
            throws InputOutputException {
        if (! this.exists(StorageType.FILE, path)) {
            throw new InputOutputException("Can not check whether the given path " + path.getPath() + " is a directory. The element does not exist in the DHT");
        }

        // we only have "files" in the DHT
        return false;
    }

    @Override
    public Path getRootDir() {
        return null;
    }
}
