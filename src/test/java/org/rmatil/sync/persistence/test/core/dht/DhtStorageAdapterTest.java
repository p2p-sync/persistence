package org.rmatil.sync.persistence.test.core.dht;

import net.tomp2p.connection.Bindings;
import net.tomp2p.connection.StandardProtocolFamily;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.rmatil.sync.persistence.api.IPathElement;
import org.rmatil.sync.persistence.api.IStorageAdapter;
import org.rmatil.sync.persistence.api.StorageType;
import org.rmatil.sync.persistence.core.dht.DhtPathElement;
import org.rmatil.sync.persistence.core.dht.DhtStorageAdapter;
import org.rmatil.sync.persistence.core.local.PathElement;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import org.rmatil.sync.persistence.test.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;

import static org.junit.Assert.*;

public class DhtStorageAdapterTest {

    protected final static Logger logger = LoggerFactory.getLogger(DhtStorageAdapterTest.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected static PeerDHT peer1;
    protected static PeerDHT peer2;

    protected static IStorageAdapter dhtStorageAdapter1;
    protected static IStorageAdapter dhtStorageAdapter2;

    protected static IPathElement path = new DhtPathElement("content key");
    protected static byte[]       data = "Some content".getBytes();

    @BeforeClass
    public static void setup()
            throws IOException {

        Bindings b = new Bindings().addProtocol(StandardProtocolFamily.INET).addAddress(InetAddress.getByName(Config.DEFAULT.getTestIpV4Address()));

        // bootstrap peer
        peer1 = new PeerBuilderDHT(new PeerBuilder(new Number160(Config.DEFAULT.getRnd())).ports(Config.DEFAULT.getTestPort()).bindings(b).start()).start();
        // connect to bootstrap peer
        peer2 = new PeerBuilderDHT(new PeerBuilder(new Number160(Config.DEFAULT.getRnd())).masterPeer(peer1.peer()).start()).start();

        InetAddress connectionAddress = Inet4Address.getByName(Config.DEFAULT.getTestIpV4Address());

        // Future Discover
        FutureDiscover futureDiscover = peer2.peer().discover().inetAddress(connectionAddress).ports(Config.DEFAULT.getTestPort()).start();
        futureDiscover.awaitUninterruptibly();

        // Future Bootstrap
        FutureBootstrap futureBootstrap = peer2.peer().bootstrap().inetAddress(connectionAddress).ports(Config.DEFAULT.getTestPort()).start();
        futureBootstrap.awaitUninterruptibly();

        if (futureBootstrap.isFailed()) {
            logger.error("Failed to bootstrap peers. Reason: " + futureBootstrap.failedReason());
        }

        dhtStorageAdapter1 = new DhtStorageAdapter(peer1, Config.DEFAULT.getTestLocationKey());
        dhtStorageAdapter2 = new DhtStorageAdapter(peer2, Config.DEFAULT.getTestLocationKey());

    }

    @AfterClass
    public static void tearDown() {
        peer2.shutdown();
        peer1.shutdown();
    }

    @After
    public void after()
            throws InputOutputException {
        dhtStorageAdapter1.delete(path);
    }

    @Test
    public void testStore()
            throws InputOutputException, InterruptedException {

        dhtStorageAdapter1.persist(StorageType.FILE, path, data);

        // wait to for propagating data among peers
        Thread.sleep(1000L);

        byte[] receivedContent = dhtStorageAdapter2.read(path);

        assertArrayEquals("Content is not the same", data, receivedContent);
    }

    @Test
    public void testStoreWithException()
            throws InputOutputException {
        thrown.expect(InputOutputException.class);

        IPathElement path = new DhtPathElement("some Content Key");

        dhtStorageAdapter1.persist(StorageType.DIRECTORY, path, null);
    }

    @Test
    public void testDelete()
            throws InputOutputException {
        dhtStorageAdapter1.persist(StorageType.FILE, path, data);

        byte[] receivedData = dhtStorageAdapter2.read(path);

        assertArrayEquals("Content is not the same", data, receivedData);

        dhtStorageAdapter2.delete(path);

        byte[] emptyData = dhtStorageAdapter1.read(path);

        assertArrayEquals("Content is not empty after deletion", new byte[0], emptyData);
    }

    @Test
    public void testExists()
            throws InputOutputException {
        dhtStorageAdapter1.persist(StorageType.FILE, path, data);

        byte[] receivedData = dhtStorageAdapter2.read(path);

        assertArrayEquals("Content is not the same", data, receivedData);

        boolean exists1 = dhtStorageAdapter1.exists(StorageType.FILE, path);
        boolean exists2 = dhtStorageAdapter2.exists(StorageType.FILE, path);

        assertTrue("File does apparently not exist", exists1);
        assertTrue("File does apparently not exist", exists2);

        dhtStorageAdapter1.delete(path);

        boolean existsAfterDeletion1 = dhtStorageAdapter1.exists(StorageType.FILE, path);
        boolean existsAfterDeletion2 = dhtStorageAdapter2.exists(StorageType.FILE, path);

        assertFalse("File should not exist anymore after deletion", existsAfterDeletion1);
        assertFalse("File should not exist anymore after deletion", existsAfterDeletion2);

        // check for existing directory should throw an exception
        thrown.expect(InputOutputException.class);
        dhtStorageAdapter1.exists(StorageType.DIRECTORY, path);
    }

    @Test
    public void testRootDir() {
        assertNull("RootDir should be null", dhtStorageAdapter1.getRootDir());
        assertNull("RootDir should be null", dhtStorageAdapter2.getRootDir());
    }

    @Test
    public void testLocalPathElement()
            throws InputOutputException {

        IPathElement pathElement = new PathElement("somePath");

        thrown.expect(InputOutputException.class);
        dhtStorageAdapter1.persist(StorageType.FILE, pathElement, new byte[0]);

        thrown.expect(InputOutputException.class);
        dhtStorageAdapter1.delete(pathElement);

        thrown.expect(InputOutputException.class);
        dhtStorageAdapter1.read(pathElement);

        thrown.expect(InputOutputException.class);
        dhtStorageAdapter1.exists(StorageType.FILE, pathElement);
    }

}
