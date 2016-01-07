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
import org.rmatil.sync.persistence.api.IFileMetaInfo;
import org.rmatil.sync.persistence.api.IPathElement;
import org.rmatil.sync.persistence.api.IStorageAdapter;
import org.rmatil.sync.persistence.api.StorageType;
import org.rmatil.sync.persistence.core.dht.DhtPathElement;
import org.rmatil.sync.persistence.core.dht.DhtStorageAdapter;
import org.rmatil.sync.persistence.core.local.LocalPathElement;
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
    protected static byte[] data = "Some content".getBytes();

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
    public void testStoreAtOffset()
            throws InputOutputException, InterruptedException {
        // persist path under protection of the public key of peer1
        dhtStorageAdapter1.persist(StorageType.FILE, path1, data);

        // wait to for propagating data among peers
        Thread.sleep(1000L);

        byte[] receivedContent = dhtStorageAdapter1.read(path1);
        assertArrayEquals("Content is not the same", data, receivedContent);

        String data2 = "content blub blub";
        dhtStorageAdapter1.persist(StorageType.FILE, path1, 5, data2.getBytes());
        byte[] receivedContentAfterModify = dhtStorageAdapter1.read(path1);
        assertEquals("String is not equals", "Some content blub blub", new String(receivedContentAfterModify));

        // should be the same since path has the same protection key
        byte[] receivedContent2 = dhtStorageAdapter2.read(path1);
        assertEquals("Content should not be empty", "Some content blub blub", new String(receivedContent2));

        String data3 = "ab";
        dhtStorageAdapter1.persist(StorageType.FILE, path1, 5, data3.getBytes());
        byte[] receivedContentAfterModify2 = dhtStorageAdapter1.read(path1);
        assertEquals("String is not equals", "Some abntent blub blub", new String(receivedContentAfterModify2));

        // write beyond end of file
        String data4 = "cd";
        dhtStorageAdapter1.persist(StorageType.FILE, path1, 25, data4.getBytes());
        byte[] receivedContentAfterModify3 = dhtStorageAdapter1.read(path1);
        assertEquals("String is not equals", "Some abntent blub blubcd", new String(receivedContentAfterModify3));
    }

    @Test
    public void testStoreWithException()
            throws InputOutputException {
        thrown.expect(InputOutputException.class);

        IPathElement path = new DhtPathElement("username1", "content key 2", "domain key 2");

        dhtStorageAdapter1.persist(StorageType.DIRECTORY, path, null);
    }

    @Test
    public void testReadOffset()
            throws InputOutputException {
        String content = "Feel the rhythm feel the blues, it's bobsled time";
        IPathElement path = new DhtPathElement(
                "location key",
                "content key",
                "domain key"
        );

        // persist path under protection of the public key of peer1
        dhtStorageAdapter1.persist(StorageType.FILE, path, content.getBytes());

        // this should contain the whole string
        byte[] readContent = dhtStorageAdapter1.read(path, 0, 49);
        assertArrayEquals("Content is not equal", content.getBytes(), readContent);

        byte[] readContent2 = dhtStorageAdapter1.read(path, 10, 39);
        byte[] expected = new byte[39];
        System.arraycopy(content.getBytes(), 10, expected, 0, 39);
        assertArrayEquals("Part of the content is not the same", expected, readContent2);

        // we expect the storage adapter to trim the byte array
        byte[] readContent3 = dhtStorageAdapter1.read(path, 10, 50);
        assertArrayEquals("Part of the content is not the same", expected, readContent3);
    }

    @Test
    public void testMove()
            throws InputOutputException {
        dhtStorageAdapter1.persist(StorageType.FILE, path1, new byte[0]);

        assertTrue("path1 does not exist", dhtStorageAdapter1.exists(StorageType.FILE, path1));

        IPathElement newPath = new DhtPathElement(
                "location key 2",
                "content key 2",
                "domain key 2"
        );

        dhtStorageAdapter1.move(StorageType.FILE, path1, newPath);

        assertTrue("new Path should exist after moving", dhtStorageAdapter1.exists(StorageType.FILE, newPath));
        assertFalse("old path should not exist after moving anymore", dhtStorageAdapter1.exists(StorageType.FILE, path1));

        dhtStorageAdapter1.delete(newPath);
    }

    @Test
    public void testMoveWithException()
            throws InputOutputException {
        dhtStorageAdapter1.persist(StorageType.FILE, path1, new byte[0]);

        assertTrue("path1 does not exist", dhtStorageAdapter1.exists(StorageType.FILE, path1));
        IPathElement newPath = new DhtPathElement(
                "location key 2",
                "content key 2",
                "domain key 2"
        );

        // now create first the target path
        dhtStorageAdapter1.persist(StorageType.FILE, newPath, new byte[0]);

        thrown.expect(InputOutputException.class);
        // this should then throw the exception
        dhtStorageAdapter1.move(StorageType.FILE, path1, newPath);

        assertTrue("new Path should exist after moving", dhtStorageAdapter1.exists(StorageType.FILE, newPath));
        assertFalse("old path should not exist after moving anymore", dhtStorageAdapter1.exists(StorageType.FILE, path1));
    }

    @Test
    public void testGetMetaInformation()
            throws InputOutputException {
        String content = "Feel the rhythm feel the blues, it's bobsled time";
        IPathElement path = new DhtPathElement(
                "location key",
                "content key",
                "domain key"
        );

        // persist path under protection of the public key of peer1
        dhtStorageAdapter1.persist(StorageType.FILE, path, content.getBytes());

        // this should contain the whole string
        IFileMetaInfo fileMetaInfo = dhtStorageAdapter1.getMetaInformation(path);
        assertEquals("Size should be equal to the length of the content", content.getBytes().length, fileMetaInfo.getTotalFileSize());
        assertEquals("File ext should be empty", "", fileMetaInfo.getFileExtension());
        assertEquals("File size is not the same", content.getBytes().length, fileMetaInfo.getTotalFileSize());
        assertFalse("File should not be a directory", fileMetaInfo.isDirectory());
        assertTrue("File should be a file", fileMetaInfo.isFile());

        thrown.expect(InputOutputException.class);
        IPathElement notExistingPath = new DhtPathElement(
                "winter",
                "summer",
                "spring"
        );

        dhtStorageAdapter1.getMetaInformation(notExistingPath);
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

        IPathElement pathElement = new LocalPathElement("somePath");

        thrown.expect(InputOutputException.class);
        dhtStorageAdapter1.persist(StorageType.FILE, pathElement, new byte[0]);

        dhtStorageAdapter1.delete(pathElement);

        dhtStorageAdapter1.read(pathElement);

        dhtStorageAdapter1.exists(StorageType.FILE, pathElement);
    }

    @Test
    public void testLocalPathElement2()
            throws InputOutputException {

        IPathElement pathElement = new LocalPathElement("somePath");

        thrown.expect(InputOutputException.class);

        dhtStorageAdapter1.delete(pathElement);
    }

    @Test
    public void testLocalPathElement3()
            throws InputOutputException {

        IPathElement pathElement = new LocalPathElement("somePath");

        thrown.expect(InputOutputException.class);

        dhtStorageAdapter1.read(pathElement);
    }

    @Test
    public void testLocalPathElement4()
            throws InputOutputException {

        IPathElement pathElement = new LocalPathElement("somePath");

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

    @Test
    public void testIsFile()
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

        assertTrue("File should exist", dhtStorageAdapter1.isFile(path1));
        assertFalse("File should not be a directory", dhtStorageAdapter1.isDir(path1));

        assertTrue("File should exist", dhtStorageAdapter2.isFile(path1));
        assertFalse("File should not be a directory", dhtStorageAdapter2.isDir(path1));

        thrown.expect(InputOutputException.class);
        dhtStorageAdapter1.isFile(new DhtPathElement("blib", "blob", "blub"));
    }

    @Test
    public void testIsDir()
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

        assertFalse("File should not be a dir", dhtStorageAdapter1.isDir(path1));
        assertTrue("Dir should not be a file", dhtStorageAdapter1.isFile(path1));

        assertFalse("File should not be a dir", dhtStorageAdapter2.isDir(path1));
        assertTrue("Dir should not be a file", dhtStorageAdapter2.isFile(path1));

        thrown.expect(InputOutputException.class);
        dhtStorageAdapter1.isFile(new DhtPathElement("blib", "blob", "blub"));
    }
}
