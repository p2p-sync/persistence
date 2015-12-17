package org.rmatil.sync.persistence.test.core.dht;

import net.tomp2p.connection.Bindings;
import net.tomp2p.connection.StandardProtocolFamily;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.dht.StorageLayer;
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
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.*;

public class DhtStorageAdapterTest {

    protected final static Logger logger = LoggerFactory.getLogger(DhtStorageAdapterTest.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected static PeerDHT peer1;
    protected static PeerDHT peer2;

    protected static IStorageAdapter dhtStorageAdapter1;
    protected static IStorageAdapter dhtStorageAdapter2;

    protected static KeyPair keyPair1;
    protected static KeyPair keyPair2;

    protected static IPathElement path1;protected static byte[] data = "Some content".getBytes();

    @BeforeClass
    public static void setup()
            throws IOException, NoSuchAlgorithmException {

        Bindings b = new Bindings().addProtocol(StandardProtocolFamily.INET).addAddress(InetAddress.getByName(Config.DEFAULT.getTestIpV4Address()));

        KeyPairGenerator generator = KeyPairGenerator.getInstance("DSA");
        keyPair1 = generator.genKeyPair();
        keyPair2 = generator.genKeyPair();

        // bootstrap peer
        // set keypair for domain protection
        peer1 = new PeerBuilderDHT(new PeerBuilder(Number160.ONE).keyPair(keyPair1).ports(Config.DEFAULT.getTestPort()).bindings(b).start()).start();
        peer1.storageLayer().protection(
                StorageLayer.ProtectionEnable.ALL,
                StorageLayer.ProtectionMode.NO_MASTER,
                StorageLayer.ProtectionEnable.ALL,
                StorageLayer.ProtectionMode.NO_MASTER
        );

        // connect to bootstrap peer
        // set keypair for domain protection
        peer2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).keyPair(keyPair2).masterPeer(peer1.peer()).start()).start();
        peer2.storageLayer().protection(
                StorageLayer.ProtectionEnable.ALL,
                StorageLayer.ProtectionMode.NO_MASTER,
                StorageLayer.ProtectionEnable.ALL,
                StorageLayer.ProtectionMode.NO_MASTER
        );

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

        path1 = new DhtPathElement("location key", "content key", "domain-key");

        dhtStorageAdapter1 = new DhtStorageAdapter(peer1);
        dhtStorageAdapter2 = new DhtStorageAdapter(peer2);

    }

    @AfterClass
    public static void tearDown() {
        peer2.shutdown();
        peer1.shutdown();
    }

    @After
    public void after()
            throws InputOutputException {
        dhtStorageAdapter1.delete(path1);
    }

    @Test
    public void testStore()
            throws InputOutputException, InterruptedException {

        // persist path under protection of the public key of peer1
        dhtStorageAdapter1.persist(StorageType.FILE, path1, data);

        // wait to for propagating data among peers
        Thread.sleep(1000L);

        byte[] receivedContent = dhtStorageAdapter1.read(path1);
        assertArrayEquals("Content is not the same", data, receivedContent);

        // should be the same since path has the same protection key
        byte[] receivedContent2 = dhtStorageAdapter2.read(path1);
        assertArrayEquals("Content should not be empty", data, receivedContent2);
    }

    @Test
    public void testStoreWithException()
            throws InputOutputException {
        thrown.expect(InputOutputException.class);

        IPathElement path = new DhtPathElement("username1", "content key 2", "domain key 2");

        dhtStorageAdapter1.persist(StorageType.DIRECTORY, path, null);
    }

    @Test
    public void testDelete()
            throws InputOutputException {
        dhtStorageAdapter1.persist(StorageType.FILE, path1, data);

        byte[] receivedData1 = dhtStorageAdapter1.read(path1);
        assertArrayEquals("Content is not the same", data, receivedData1);

        byte[] receivedData2 = dhtStorageAdapter2.read(path1);
        assertArrayEquals("Content is not the same", data, receivedData2);

        // try to delete path which is on the location key & content
        // key of path1 but has a different key pair: This should fail
        dhtStorageAdapter2.delete(path1);

        byte[] receivedData3 = dhtStorageAdapter2.read(path1);
        assertArrayEquals("Content should not be empty after deleting path2", data, receivedData3);

        // Only the domain owner can delete a path
        dhtStorageAdapter2.delete(path1);
        byte[] receivedData4 = dhtStorageAdapter1.read(path1);
        assertArrayEquals("Content should not be empty after unauthorized deleting", data, receivedData4);

        dhtStorageAdapter1.delete(path1);

        byte[] emptyData = dhtStorageAdapter1.read(path1);
        assertArrayEquals("Content is not empty after deletion", new byte[0], emptyData);
    }

    @Test
    public void testExists()
            throws InputOutputException {
        dhtStorageAdapter1.persist(StorageType.FILE, path1, data);

        byte[] receivedData1 = dhtStorageAdapter2.read(path1);
        assertArrayEquals("Content is not the same", data, receivedData1);

        byte[] receivedData2 = dhtStorageAdapter1.read(path1);
        assertArrayEquals("Content is not the same", data, receivedData2);

        boolean exists1 = dhtStorageAdapter1.exists(StorageType.FILE, path1);
        boolean exists2 = dhtStorageAdapter2.exists(StorageType.FILE, path1);

        assertTrue("File does not exist apparently", exists1);
        assertTrue("File does not exist apparently", exists2);

        dhtStorageAdapter1.delete(path1);

        boolean existsAfterDeletion1 = dhtStorageAdapter1.exists(StorageType.FILE, path1);
        boolean existsAfterDeletion2 = dhtStorageAdapter2.exists(StorageType.FILE, path1);

        assertFalse("File should not exist anymore after deletion", existsAfterDeletion1);
        assertFalse("File should not exist anymore after deletion", existsAfterDeletion2);

        // check for existing directory should throw an exception
        thrown.expect(InputOutputException.class);
        dhtStorageAdapter1.exists(StorageType.DIRECTORY, path1);
    }

    @Test
    public void testRootDir() {
        assertNull("RootDir should be null", dhtStorageAdapter1.getRootDir());
        assertNull("RootDir should be null", dhtStorageAdapter2.getRootDir());
    }

    @Test
    public void testLocalPathElement1()
            throws InputOutputException {

        IPathElement pathElement = new PathElement("somePath");

        thrown.expect(InputOutputException.class);
        dhtStorageAdapter1.persist(StorageType.FILE, pathElement, new byte[0]);

        dhtStorageAdapter1.delete(pathElement);

        dhtStorageAdapter1.read(pathElement);

        dhtStorageAdapter1.exists(StorageType.FILE, pathElement);
    }

    @Test
    public void testLocalPathElement2()
            throws InputOutputException {

        IPathElement pathElement = new PathElement("somePath");

        thrown.expect(InputOutputException.class);

        dhtStorageAdapter1.delete(pathElement);
    }

    @Test
    public void testLocalPathElement3()
            throws InputOutputException {

        IPathElement pathElement = new PathElement("somePath");

        thrown.expect(InputOutputException.class);

        dhtStorageAdapter1.read(pathElement);
    }

    @Test
    public void testLocalPathElement4()
            throws InputOutputException {

        IPathElement pathElement = new PathElement("somePath");

        thrown.expect(InputOutputException.class);
        dhtStorageAdapter1.exists(StorageType.FILE, pathElement);
    }

    @Test
    public void testIllegalArgumentException()
            throws IOException {
        thrown.expect(IllegalArgumentException.class);

        PeerDHT tmpPeer = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(50)).start()).start();
        DhtStorageAdapter tmpAdapter = new DhtStorageAdapter(tmpPeer);
    }

}
