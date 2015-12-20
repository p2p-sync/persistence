package org.rmatil.sync.persistence.test.core.local;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.rmatil.sync.persistence.api.IFileMetaInfo;
import org.rmatil.sync.persistence.api.IPathElement;
import org.rmatil.sync.persistence.api.StorageType;
import org.rmatil.sync.persistence.core.dht.DhtPathElement;
import org.rmatil.sync.persistence.core.local.LocalStorageAdapter;
import org.rmatil.sync.persistence.core.local.LocalPathElement;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import org.rmatil.sync.persistence.test.config.Config;
import org.rmatil.sync.persistence.test.util.FileUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class LocalStorageAdapterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * The root folder used to test
     */
    public static final Path ROOT_TEST_DIR = Config.DEFAULT.getRootTestDir();

    private static LocalStorageAdapter localStorageAdapter;

    @BeforeClass
    public static void setUp() {
        try {
            // create test dir
            if (! Files.exists(ROOT_TEST_DIR)) {
                Files.createDirectory(ROOT_TEST_DIR);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        localStorageAdapter = new LocalStorageAdapter(ROOT_TEST_DIR);
    }

    @AfterClass
    public static void tearDown() {
        FileUtil.delete(ROOT_TEST_DIR.toFile());
    }

    @Before
    public void before() {
        FileUtil.deleteTestFile(ROOT_TEST_DIR);
        FileUtil.delete(ROOT_TEST_DIR.resolve("testDir").toFile());
    }

    @After
    public void after() {
        FileUtil.deleteTestFile(ROOT_TEST_DIR);
        FileUtil.delete(ROOT_TEST_DIR.resolve("testDir").toFile());
    }


    @Test
    public void testPersist()
            throws InputOutputException, IOException {
        String content = "Feel the rhythm feel the blues, it's bobsled time";
        IPathElement path = new LocalPathElement(Config.DEFAULT.getTestFileName1());

        localStorageAdapter.persist(StorageType.FILE, path, content.getBytes());

        byte[] readContent = null;
        readContent = Files.readAllBytes(ROOT_TEST_DIR.resolve(Config.DEFAULT.getTestFileName1()));

        assertArrayEquals("Content is not equal", content.getBytes(), readContent);
    }

    @Test
    public void testPersistException()
            throws InputOutputException {
        thrown.expect(InputOutputException.class);

        String content = "Feel the rhythm feel the blues, it's bobsled time";
        IPathElement path = new LocalPathElement("testDir2/" + Config.DEFAULT.getTestFileName1());

        localStorageAdapter.persist(StorageType.FILE, path, content.getBytes());
    }

    @Test
    public void testPersistDirectory()
            throws InputOutputException {
        IPathElement path = new LocalPathElement("testDir");

        localStorageAdapter.persist(StorageType.DIRECTORY, path, null);


        assertTrue("Test directory does not exist", Files.exists(ROOT_TEST_DIR.resolve("testDir")));
        assertTrue("Test directory is not a directory", Files.isDirectory(ROOT_TEST_DIR.resolve("testDir")));
    }

    @Test
    public void testPersistDirectoryException()
            throws InputOutputException {
        thrown.expect(InputOutputException.class);

        IPathElement path = new LocalPathElement("testDir/innerTestDir");

        localStorageAdapter.persist(StorageType.DIRECTORY, path, null);
    }

    @Test
    public void testDelete()
            throws InputOutputException {
        FileUtil.createTestFile(ROOT_TEST_DIR);

        IPathElement path = new LocalPathElement(Config.DEFAULT.getTestFileName1());

        localStorageAdapter.delete(path);
    }

    @Test
    public void testDeleteException()
            throws InputOutputException {
        thrown.expect(InputOutputException.class);

        IPathElement path = new LocalPathElement(Config.DEFAULT.getTestFileName1());

        localStorageAdapter.delete(path);
    }

    @Test
    public void testRead()
            throws InputOutputException {
        String content = "Feel the rhythm feel the blues, it's bobsled time";
        IPathElement path = new LocalPathElement(Config.DEFAULT.getTestFileName1());

        localStorageAdapter.persist(StorageType.FILE, path, content.getBytes());

        byte[] readContent;
        readContent = localStorageAdapter.read(path);

        assertArrayEquals("Content is not equal", content.getBytes(), readContent);
    }

    @Test
    public void testReadOffset()
            throws InputOutputException {
        String content = "Feel the rhythm feel the blues, it's bobsled time";
        IPathElement path = new LocalPathElement(Config.DEFAULT.getTestFileName1());
        localStorageAdapter.persist(StorageType.FILE, path, content.getBytes());

        // this should contain the whole string
        byte[] readContent = localStorageAdapter.read(path, 0, 49);
        assertArrayEquals("Content is not equal", content.getBytes(), readContent);

        byte[] readContent2 = localStorageAdapter.read(path, 10, 39);
        byte[] expected = new byte[39];
        System.arraycopy(content.getBytes(), 10, expected, 0, 39);
        assertArrayEquals("Part of the content is not the same", expected, readContent2);

        // we expect the storage adapter to trim the byte array
        byte[] readContent3 = localStorageAdapter.read(path, 10, 50);
        assertArrayEquals("Part of the content is not the same", expected, readContent3);
    }

    @Test
    public void testGetMetaInformation()
            throws InputOutputException {
        String content = "Feel the rhythm feel the blues, it's bobsled time";
        IPathElement path = new LocalPathElement(Config.DEFAULT.getTestFileName1());

        // persist path under protection of the public key of peer1
        localStorageAdapter.persist(StorageType.FILE, path, content.getBytes());

        // this should contain the whole string
        IFileMetaInfo fileMetaInfo = localStorageAdapter.getMetaInformation(path);
        assertEquals("Size should be equal to the length of the content", content.getBytes().length, fileMetaInfo.getTotalFileSize());
    }

    @Test
    public void testReadException()
            throws InputOutputException {
        thrown.expect(InputOutputException.class);

        IPathElement path = new LocalPathElement("someDir/" + Config.DEFAULT.getTestFileName1());

        localStorageAdapter.read(path);

    }

    @Test
    public void testGetRootDir() {
        Path rootDir = localStorageAdapter.getRootDir();

        assertEquals("Root directory is not the same", ROOT_TEST_DIR, rootDir);
    }

    @Test
    public void testExists()
            throws InputOutputException {
        IPathElement path = new LocalPathElement(Config.DEFAULT.getTestFileName1());
        String content = "Content";

        assertFalse("Exist false failed", localStorageAdapter.exists(StorageType.FILE, path));

        localStorageAdapter.persist(StorageType.FILE, path, content.getBytes());

        assertTrue("Exist failed", localStorageAdapter.exists(StorageType.FILE, path));

        localStorageAdapter.delete(path);

        assertFalse("Exist false failed after deletion", localStorageAdapter.exists(StorageType.FILE, path));

        IPathElement dir = new LocalPathElement("testDir");

        assertFalse("Exist false failed before dir creation", localStorageAdapter.exists(StorageType.DIRECTORY, dir));

        localStorageAdapter.persist(StorageType.DIRECTORY, dir, null);

        assertTrue("Exist failed after dir creation", localStorageAdapter.exists(StorageType.DIRECTORY, dir));

        localStorageAdapter.delete(dir);

        assertFalse("Exist failed after dir deletion", localStorageAdapter.exists(StorageType.DIRECTORY, dir));
    }
}
