package org.rmatil.sync.persistence.core.dht.listener;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A listener which is called whenever data is fetched from the DHT.
 */
public class DhtGetListener implements BaseFutureListener<FutureGet> {

    private static final Logger logger = LoggerFactory.getLogger(DhtPutListener.class);

    protected PeerDHT dht;

    /**
     * @param dht The peer DHT which was used for the get operation
     */
    public DhtGetListener(PeerDHT dht) {
        this.dht = dht;
    }

    @Override
    public void operationComplete(FutureGet future)
            throws Exception {
        if (future.isSuccess()) {
            logger.debug("[Peer @ " + this.dht.peerAddress().inetAddress().toString() + "]: Get of data succeeded. ");
        } else if (future.isFailed()) {
            logger.warn("[Peer @ " + this.dht.peerAddress().inetAddress().toString() + "]: Get od data failed. Reason: " + future.failedReason());
        }
    }

    @Override
    public void exceptionCaught(Throwable t)
            throws Exception {
        logger.error("[Peer @ " + this.dht.peerAddress().inetAddress().toString() + "]: Caught exception " + t.getMessage());
    }

}
