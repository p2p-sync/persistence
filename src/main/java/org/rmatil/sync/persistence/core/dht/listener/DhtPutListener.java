package org.rmatil.sync.persistence.core.dht.listener;

import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A listener which is called whenever a put operation to the
 * DHT is made
 */
public class DhtPutListener implements BaseFutureListener<FuturePut> {

    private static final Logger logger = LoggerFactory.getLogger(DhtPutListener.class);

    protected PeerDHT dht;

    /**
     * @param dht The peer DHT which was used for the put operation
     */
    public DhtPutListener(PeerDHT dht) {
        this.dht = dht;
    }

    @Override
    public void operationComplete(FuturePut future)
            throws Exception {
        if (future.isSuccess()) {
            logger.debug("[Peer @ " + this.dht.peerAddress().inetAddress().toString() + "]: Put of data succeeded. ");
        } else if (future.isSuccessPartially()) {
            logger.debug("[Peer @ " + this.dht.peerAddress().inetAddress().toString() + "]: Put of data was only partially successful");
        } else if (future.isFailed()) {
            logger.warn("[Peer @ " + this.dht.peerAddress().inetAddress().toString() + "]: Put od data failed. Reason: " + future.failedReason());
        }
    }

    @Override
    public void exceptionCaught(Throwable t)
            throws Exception {
        logger.error("[Peer @ " + this.dht.peerAddress().inetAddress().toString() + "]: Caught exception " + t.getMessage());
    }
}
