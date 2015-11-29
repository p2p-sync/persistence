package org.rmatil.sync.persistence.test.core.local;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.rmatil.sync.persistence.api.IPathElement;
import org.rmatil.sync.persistence.api.StorageType;
import org.rmatil.sync.persistence.core.local.LocalStorageAdapter;
import org.rmatil.sync.persistence.core.local.PathElement;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import org.rmatil.sync.persistence.test.config.Config;
import org.rmatil.sync.persistence.test.util.FileUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        IPathElement path = new PathElement(Config.DEFAULT.getTestFileName1());

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
        IPathElement path = new PathElement("testDir2/" + Config.DEFAULT.getTestFileName1());

        localStorageAdapter.persist(StorageType.FILE, path, content.getBytes());
    }

    @Test
    public void testPersistDirectory()
            throws InputOutputException {
        IPathElement path = new PathElement("testDir");

        localStorageAdapter.persist(StorageType.DIRECTORY, path, null);


        assertTrue("Test directory does not exist", Files.exists(ROOT_TEST_DIR.resolve("testDir")));
        assertTrue("Test directory is not a directory", Files.isDirectory(ROOT_TEST_DIR.resolve("testDir")));
    }

    @Test
    public void testPersistDirectoryException()
            throws InputOutputException {
        thrown.expect(InputOutputException.class);

        IPathElement path = new PathElement("testDir/innerTestDir");

        localStorageAdapter.persist(StorageType.DIRECTORY, path, null);
    }

    @Test
    public void testDelete()
            throws InputOutputException {
        FileUtil.createTestFile(ROOT_TEST_DIR);

        IPathElement path = new PathElement(Config.DEFAULT.getTestFileName1());

        localStorageAdapter.delete(path);
    }

    @Test
    public void testDeleteException()
            throws InputOutputException {
        thrown.expect(InputOutputException.class);

        IPathElement path = new PathElement(Config.DEFAULT.getTestFileName1());

        localStorageAdapter.delete(path);
    }

    @Test
    public void testRead()
            throws InputOutputException {
        String content = "Feel the rhythm feel the blues, it's bobsled time";
        IPathElement path = new PathElement(Config.DEFAULT.getTestFileName1());

        localStorageAdapter.persist(StorageType.FILE, path, content.getBytes());

        byte[] readContent = null;
        readContent = localStorageAdapter.read(path);

        assertArrayEquals("Content is not equal", content.getBytes(), readContent);
    }

    @Test
    public void testReadException()
            throws InputOutputException {
        thrown.expect(InputOutputException.class);

        IPathElement path = new PathElement("someDir/" + Config.DEFAULT.getTestFileName1());

        localStorageAdapter.read(path);

    }

    @Test
    public void testGetRootDir() {
        Path rootDir = localStorageAdapter.getRootDir();

        assertEquals("Root directory is not the same", ROOT_TEST_DIR, rootDir);
    }
}
