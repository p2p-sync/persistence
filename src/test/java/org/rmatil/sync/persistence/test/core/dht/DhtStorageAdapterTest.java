package org.rmatil.sync.persistence.test.core.dht;

import net.tomp2p.connection.Bindings;
import net.tomp2p.connection.StandardProtocolFamily;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.dht.StorageLayer;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
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

    protected static IPathElement path1;
    protected static IPathElement path2;
    protected static byte[] data = "Some content".getBytes();

    @BeforeClass
    public static void setup()
            throws IOException, NoSuchAlgorithmException {

        Bindings b = new Bindings().addProtocol(StandardProtocolFamily.INET).addAddress(InetAddress.getByName(Config.DEFAULT.getTestIpV4Address()));

        KeyPairGenerator generator = KeyPairGenerator.getInstance("DSA");
        keyPair1 = generator.genKeyPair();
        keyPair2 = generator.genKeyPair();

        // bootstrap peer
        peer1 = new PeerBuilderDHT(new PeerBuilder(keyPair1).ports(Config.DEFAULT.getTestPort()).bindings(b).start()).start();
        peer1.storageLayer().protection(
                StorageLayer.ProtectionEnable.NONE,
                StorageLayer.ProtectionMode.MASTER_PUBLIC_KEY,
                StorageLayer.ProtectionEnable.ALL,
                StorageLayer.ProtectionMode.MASTER_PUBLIC_KEY
        );

        // connect to bootstrap peer
        peer2 = new PeerBuilderDHT(new PeerBuilder(keyPair2).masterPeer(peer1.peer()).start()).start();
        peer1.storageLayer().protection(
                StorageLayer.ProtectionEnable.NONE,
                StorageLayer.ProtectionMode.MASTER_PUBLIC_KEY,
                StorageLayer.ProtectionEnable.ALL,
                StorageLayer.ProtectionMode.MASTER_PUBLIC_KEY
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

        path1 = new DhtPathElement("content key", keyPair1.getPublic());
        path2 = new DhtPathElement("content key", keyPair2.getPublic());

        dhtStorageAdapter1 = new DhtStorageAdapter(peer1, Config.DEFAULT.getTestLocationKey1());
        dhtStorageAdapter2 = new DhtStorageAdapter(peer2, Config.DEFAULT.getTestLocationKey1());

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
        assertArrayEquals("Content should be empty", data, receivedContent2);

        // since we use another path, i.e. another protection key, no data should be returned
        byte[] receivedContent3 = dhtStorageAdapter2.read(path2);
        assertArrayEquals("Content should be empty", new byte[0], receivedContent3);
    }

    @Test
    public void testStoreWithException()
            throws InputOutputException {
        thrown.expect(InputOutputException.class);

        IPathElement path = new DhtPathElement("some Content Key", keyPair1.getPublic());

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

        dhtStorageAdapter2.delete(path2);

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
        boolean exists3 = dhtStorageAdapter1.exists(StorageType.FILE, path2);
        boolean exists4 = dhtStorageAdapter2.exists(StorageType.FILE, path2);

        assertTrue("File does not exist apparently", exists1);
        assertTrue("File does not exist apparently", exists2);

        assertFalse("File should not exist", exists3);
        assertFalse("File should not exist", exists4);

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

}
