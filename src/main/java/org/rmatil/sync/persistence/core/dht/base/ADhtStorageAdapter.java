package org.rmatil.sync.persistence.core.dht.base;

import net.tomp2p.dht.*;
import net.tomp2p.storage.Data;
import org.rmatil.sync.commons.hashing.Hash;
import org.rmatil.sync.commons.hashing.HashingAlgorithm;
import org.rmatil.sync.persistence.api.IFileMetaInfo;
import org.rmatil.sync.persistence.api.StorageType;
import org.rmatil.sync.persistence.core.FileMetaInfo;
import org.rmatil.sync.persistence.core.dht.DhtCache;
import org.rmatil.sync.persistence.core.dht.DhtPathElement;
import org.rmatil.sync.persistence.core.dht.listener.DhtDeleteListener;
import org.rmatil.sync.persistence.core.dht.listener.DhtGetListener;
import org.rmatil.sync.persistence.core.dht.listener.DhtPutListener;
import org.rmatil.sync.persistence.exceptions.InputOutputException;

import java.nio.file.Path;

public abstract class ADhtStorageAdapter {

    protected final PeerDHT dht;

    protected final DhtCache cache;

    /**
     * Represents an abstract storage adapter for the DHT. To protect
     * values, domain protection can be enabled while invoking modifiers.
     * Note, that if domain protection is used, values can only be read, if
     * domain protection is also enabled.
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
     *         ProtectionMode.NO_MASTER, // allow to read from other clients too
     *         ProtectionEnable.ALL,
     *         ProtectionMode.NO_MASTER // allow to read from other clients too
     *       );
     * </pre>
     *
     * @param dht A PeerDHT bootstrapped with domain protection
     */
    protected ADhtStorageAdapter(PeerDHT dht) {
        this(dht, 0);
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
     *         ProtectionMode.NO_MASTER, // allow to read from other clients too
     *         ProtectionEnable.ALL,
     *         ProtectionMode.NO_MASTER // allow to read from other clients too
     *       );
     * </pre>
     *
     * @param dht A PeerDHT bootstrapped with domain protection
     */
    protected ADhtStorageAdapter(PeerDHT dht, long timeToLive) {
        this.dht = dht;
        this.cache = new DhtCache(timeToLive);
    }

    synchronized protected void persist(StorageType type, ADhtPathElement path, byte[] bytes, boolean enableProtection)
            throws InputOutputException {

        if (StorageType.FILE != type) {
            throw new InputOutputException("Only files are allowed to be stored in the DHT");
        }

        this.cache.put(path, bytes);

        Data data = new Data(bytes);

        PutBuilder putBuilder = this.dht
                .put(path.getLocationKey())
                .data(path.getContentKey(), data);

        // enable domain protection only if required
        if (enableProtection) {
            putBuilder
                    .protectDomain()
                    .domainKey(((DhtPathElement) path).getDomainKey());
        }

        FuturePut futurePut = putBuilder.start();

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

    synchronized protected void persist(StorageType type, ADhtPathElement path, long offset, byte[] bytes, boolean enabledProtection)
            throws InputOutputException {

        if (StorageType.FILE != type) {
            throw new InputOutputException("Only files are allowed to be stored in the DHT");
        }

        byte[] existingBytes = this.read(path, enabledProtection);

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

        this.cache.put(path, targetBytes);

        Data data = new Data(targetBytes);

        PutBuilder putBuilder = this.dht
                .put(path.getLocationKey())
                .data(path.getContentKey(), data);

        // enable protection only on request
        if (enabledProtection) {
            putBuilder
                    .protectDomain()
                    .domainKey(((DhtPathElement) path).getDomainKey());
        }

        FuturePut futurePut = putBuilder.start();

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

    synchronized protected void delete(ADhtPathElement path, boolean enableProtection)
            throws InputOutputException {

        this.cache.clear(path);

        RemoveBuilder removeBuilder = this.dht
                .remove(path.getLocationKey())
                .contentKey(path.getContentKey());

        // enable protection only on request
        if (enableProtection) {
            removeBuilder
                    .protectDomain()
                    .domainKey(((DhtPathElement) path).getDomainKey());
        }

        FutureRemove futureRemove = removeBuilder.start();

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

    synchronized protected byte[] read(ADhtPathElement path, boolean enableProtection)
            throws InputOutputException {

        byte[] data = this.cache.get(path);

        if (null != data) {
            // we got a cached version of the data
            return data;
        }

        GetBuilder getBuilder = this.dht
                .get(path.getLocationKey())
                .contentKey(path.getContentKey());

        if (enableProtection) {
            getBuilder
                    .domainKey(((DhtPathElement) path).getDomainKey());
        }

        FutureGet futureGet = getBuilder.start();

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

    synchronized protected byte[] read(ADhtPathElement path, long offset, int length, boolean enableProtection)
            throws InputOutputException {

        byte[] cachedData = this.cache.get(path);

        byte[] contents;
        if (null != cachedData) {
            contents = cachedData;
        } else {
            GetBuilder getBuilder = this.dht
                    .get(path.getLocationKey())
                    .contentKey(path.getContentKey());

            if (enableProtection) {
                getBuilder
                        .domainKey(((DhtPathElement) path).getDomainKey());
            }

            FutureGet futureGet = getBuilder.start();

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

            contents = futureGet.data().toBytes();
        }

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
    synchronized protected void move(StorageType storageType, ADhtPathElement oldPath, ADhtPathElement newPath, boolean enableProtection)
            throws InputOutputException {

        byte[] contents = this.read(oldPath, enableProtection);

        if (this.exists(storageType, newPath, enableProtection)) {
            throw new InputOutputException("Target path " + newPath.getPath() + " already exists");
        }

        // first try to write to new path
        this.cache.put(newPath, contents);
        this.persist(StorageType.FILE, newPath, contents, enableProtection);
        this.cache.clear(oldPath);
        this.delete(oldPath, enableProtection);
    }

    synchronized protected IFileMetaInfo getMetaInformation(ADhtPathElement path, boolean enableProtection)
            throws InputOutputException {

        if (! this.exists(StorageType.FILE, path, enableProtection)) {
            throw new InputOutputException("Could not get meta information for " + path.getPath() + ". No such file or directory");
        }

        byte[] cachedContent = this.cache.get(path);

        if (null != cachedContent) {
            return new FileMetaInfo(cachedContent.length, true, "");
        }

        GetBuilder getBuilder = this.dht
                .get(path.getLocationKey())
                .contentKey(path.getContentKey());

        // use protection only on request
        if (enableProtection) {
            getBuilder.domainKey(((DhtPathElement) path).getDomainKey());
        }

        FutureGet futureGet = getBuilder.start();

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

    synchronized protected boolean exists(StorageType storageType, ADhtPathElement path, boolean enableProtection)
            throws InputOutputException {

        if (StorageType.FILE != storageType) {
            throw new InputOutputException("Only files are allowed to be read from the DHT");
        }

        byte[] cachedContent = this.cache.get(path);

        if (null != cachedContent) {
            return true;
        }

        GetBuilder getBuilder = this.dht
                .get(path.getLocationKey())
                .contentKey(path.getContentKey());

        // use protection only on request
        if (enableProtection) {
            getBuilder.domainKey(((DhtPathElement) path).getDomainKey());
        }

        FutureGet futureGet = getBuilder.start();

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

    synchronized protected boolean isFile(ADhtPathElement path, boolean enableProtection)
            throws InputOutputException {
        if (! this.exists(StorageType.FILE, path, enableProtection)) {
            throw new InputOutputException("Can not check whether the given path " + path.getPath() + " is a file. The element does not exist in the DHT");
        }

        // we only have "files" in the DHT
        return true;
    }

    synchronized protected boolean isDir(ADhtPathElement path, boolean enableProtection)
            throws InputOutputException {
        if (! this.exists(StorageType.FILE, path, enableProtection)) {
            throw new InputOutputException("Can not check whether the given path " + path.getPath() + " is a directory. The element does not exist in the DHT");
        }

        // we only have "files" in the DHT
        return false;
    }

    synchronized protected String getChecksum(ADhtPathElement path, boolean enableProtection)
            throws InputOutputException {

        byte[] content = this.read(path, enableProtection);

        return Hash.hash(HashingAlgorithm.MD5, content);
    }

    protected Path getRootDir() {
        return null;
    }
}
