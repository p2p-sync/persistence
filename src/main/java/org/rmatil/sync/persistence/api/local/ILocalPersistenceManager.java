package org.rmatil.sync.persistence.api.local;

import net.tomp2p.peers.PeerAddress;

public interface ILocalPersistenceManager {

    /**
     * Send some data to another peer specified by the given peer address
     *
     * @param receiverPeerAddress The address of the receiver
     * @param requestData         The data to send to him
     */
    void send(PeerAddress receiverPeerAddress, ISendRequestData requestData);

    /**
     * Request some data specified by request data from a particular peer
     *
     * @param senderPeerAddress The peer which should answer the request
     * @param requestData       The specified request
     *
     * @return The data returned by the sending peer
     */
    IData get(PeerAddress senderPeerAddress, IGetRequestData requestData);
}
