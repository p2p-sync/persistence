package org.rmatil.sync.persistence.core.dht;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import org.rmatil.sync.persistence.api.IPathElement;
import org.rmatil.sync.persistence.api.IStorageAdapter;
import org.rmatil.sync.persistence.api.StorageType;
import org.rmatil.sync.persistence.core.dht.listener.DhtDeleteListener;
import org.rmatil.sync.persistence.core.dht.listener.DhtGetListener;
import org.rmatil.sync.persistence.core.dht.listener.DhtPutListener;
import org.rmatil.sync.persistence.exceptions.InputOutputException;

import java.nio.file.Path;



public class DhtStorageAdapter implements IStorageAdapter {

    protected PeerDHT dht;

    protected Number160 locationKey;

    public DhtStorageAdapter(PeerDHT dht, String locationKey) {
        this.dht = dht;
        this.locationKey = Number160.createHash(locationKey);
    }

    @Override
    public void persist(StorageType type, IPathElement path, byte[] bytes)
            throws InputOutputException {

        if (! (path instanceof DhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        if (StorageType.FILE != type) {
            throw new InputOutputException("Only files are allowed to be stored in the DHT");
        }

        Data data = new Data(bytes);

        FuturePut futurePut = this.dht
                .put(this.locationKey)
                .data(Number160.createHash(path.getPath()), data)
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
    public void delete(IPathElement path)
            throws InputOutputException {

        if (! (path instanceof DhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        FutureRemove futureRemove = this.dht
                .remove(this.locationKey)
                .contentKey(Number160.createHash(path.getPath()))
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
    public byte[] read(IPathElement path)
            throws InputOutputException {

        if (! (path instanceof DhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        FutureGet futureGet = this.dht
                .get(this.locationKey)
                .contentKey(Number160.createHash(path.getPath()))
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
    public boolean exists(StorageType storageType, IPathElement path)
            throws InputOutputException {

        if (! (path instanceof DhtPathElement)) {
            throw new InputOutputException("Could not use path element " + path.getClass().getName() + " for DHT Storage Adapter");
        }

        if (StorageType.FILE != storageType) {
            throw new InputOutputException("Only files are allowed to be read from the DHT");
        }

        FutureGet futureGet = this.dht
                .get(this.locationKey)
                .contentKey(Number160.createHash(path.getPath()))
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
            return false;
        }

        return true;
    }

    @Override
    public Path getRootDir() {
        return null;
    }
}
