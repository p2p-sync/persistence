package org.rmatil.sync.persistence.api;

import net.tomp2p.dht.*;
import net.tomp2p.futures.BaseFutureAdapter;

public interface IStorageAdapter {

    /**
     * Get some data from the DHT asynchronously, specified by the properties in the getBuilder.
     * <i>Note, that the builder must not have invoked start yet.</i>
     *
     * @param getBuilder  The builder to specify the data to get
     * @param getListener The listener, which should be called on completion of the future
     *
     * @return FutureGet The future returned by the builder
     */
    FutureGet getData(GetBuilder getBuilder, BaseFutureAdapter<FutureGet> getListener);

    /**
     * Put some data to the DHT asynchronously.
     * <i>Note, that the builder must not have invoked start yet.</i>
     *
     * @param putBuilder  The builder containing the data.
     * @param putListener The listener which should called on completion of the future
     *
     * @return FuturePut The future returned by the builder
     */
    FuturePut putData(PutBuilder putBuilder, BaseFutureAdapter<FuturePut> putListener);

    /**
     * Remove some data asynchronously from the DHT.
     * <i>Note, that the builder must not have invoked start yet.</i>
     *
     * @param removeBuilder  The builder which specifies the object to remove
     * @param removeListener The listener which should be called on completion of the future
     *
     * @return FutureRemove The future returned by the builder
     */
    FutureRemove removeData(RemoveBuilder removeBuilder, BaseFutureAdapter<FutureRemove> removeListener);
}
