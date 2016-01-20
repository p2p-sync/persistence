package org.rmatil.sync.persistence.test.core.local;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.rmatil.sync.persistence.api.IFileMetaInfo;
import org.rmatil.sync.persistence.api.IPathElement;
import org.rmatil.sync.persistence.api.StorageType;
import org.rmatil.sync.persistence.core.local.LocalPathElement;
import org.rmatil.sync.persistence.core.local.LocalStorageAdapter;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import org.rmatil.sync.persistence.test.config.Config;
import org.rmatil.sync.persistence.test.util.FileUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

        byte[] readContent;
        readContent = Files.readAllBytes(ROOT_TEST_DIR.resolve(Config.DEFAULT.getTestFileName1()));

        assertArrayEquals("Content is not equal", content.getBytes(), readContent);
    }

    @Test
    public void testStoreAtOffset()
            throws InputOutputException, InterruptedException {
        String content = "Some content";

        IPathElement path = new LocalPathElement(Config.DEFAULT.getTestFileName1());
        localStorageAdapter.persist(StorageType.FILE, path, content.getBytes());

        // wait to for propagating data among peers
        Thread.sleep(1000L);

        byte[] receivedContent = localStorageAdapter.read(path);
        assertArrayEquals("Content is not the same", content.getBytes(), receivedContent);

        String data2 = "content blub blub";
        localStorageAdapter.persist(StorageType.FILE, path, 5, data2.getBytes());
        byte[] receivedContentAfterModify = localStorageAdapter.read(path);
        assertEquals("String is not equals", "Some content blub blub", new String(receivedContentAfterModify));

        String data3 = "ab";
        localStorageAdapter.persist(StorageType.FILE, path, 5, data3.getBytes());
        byte[] receivedContentAfterModify2 = localStorageAdapter.read(path);
        assertEquals("String is not equals", "Some abntent blub blub", new String(receivedContentAfterModify2));

        // write beyond end of file
        String data4 = "cd";
        localStorageAdapter.persist(StorageType.FILE, path, 25, data4.getBytes());
        byte[] receivedContentAfterModify3 = localStorageAdapter.read(path);
        assertEquals("String is not equals", "Some abntent blub blubcd", new String(receivedContentAfterModify3));

        // we expect to truncate the rest of the file if we start writing at offset 0 again
        String data5 = "abcde";
        localStorageAdapter.persist(StorageType.FILE, path, 0, data5.getBytes());
        byte[] receivedContentAfterModify4 = localStorageAdapter.read(path);
        assertEquals("String is not equal", data5, new String(receivedContentAfterModify4));
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
            throws InputOutputException, IOException {
        FileUtil.createTestFile(ROOT_TEST_DIR);

        IPathElement path = new LocalPathElement(Config.DEFAULT.getTestFileName1());

        localStorageAdapter.delete(path);

        assertFalse("File should not exist anymore", Files.exists(Config.DEFAULT.getRootTestDir().resolve(Config.DEFAULT.getTestFileName1())));

        FileUtil.createTestFile(ROOT_TEST_DIR);
        localStorageAdapter.delete(new LocalPathElement("./"));

        assertFalse("TestDir should not exist anymore", Files.exists(Config.DEFAULT.getRootTestDir()));

        // recreate root test dir
        Files.createDirectory(ROOT_TEST_DIR);
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
        assertEquals("File ext should be txt", "txt", fileMetaInfo.getFileExtension());
        assertEquals("File size is not the same", content.getBytes().length, fileMetaInfo.getTotalFileSize());
        assertFalse("File should not be a directory", fileMetaInfo.isDirectory());
        assertTrue("File should be a file", fileMetaInfo.isFile());

        IFileMetaInfo fileMetaInfo1 = localStorageAdapter.getMetaInformation(new LocalPathElement("/"));
        assertEquals("A dir should have an empty file ext", "", fileMetaInfo1.getFileExtension());
        assertEquals("A dir should have size of 0", 0, fileMetaInfo1.getTotalFileSize());
        assertTrue("A dir should be a directory", fileMetaInfo1.isDirectory());
        assertFalse("A directory should not be file", fileMetaInfo1.isFile());

        IPathElement notExistingFile = new LocalPathElement("someUnexistingFile.exe");
        thrown.expect(InputOutputException.class);
        localStorageAdapter.getMetaInformation(notExistingFile);
    }

    @Test
    public void testReadException()
            throws InputOutputException {
        thrown.expect(InputOutputException.class);

        IPathElement path = new LocalPathElement("someDir/" + Config.DEFAULT.getTestFileName1());

        localStorageAdapter.read(path);

    }

    @Test
    public void testMove()
            throws InputOutputException {
        IPathElement path = new LocalPathElement("testDir");
        IPathElement path1 = new LocalPathElement("testDir/innerTestDir");
        IPathElement path2 = new LocalPathElement("testDir/innerTestDir/testMyFile.txt");

        localStorageAdapter.persist(StorageType.DIRECTORY, path, null);
        localStorageAdapter.persist(StorageType.DIRECTORY, path1, null);
        localStorageAdapter.persist(StorageType.FILE, path2, new byte[0]);

        assertTrue("innerDir does not exist", localStorageAdapter.exists(StorageType.DIRECTORY, path1));
        assertTrue("testMyFile.txt does not exist", localStorageAdapter.exists(StorageType.FILE, path2));

        IPathElement newPath = new LocalPathElement("testDir2");

        Path txtFile = Paths.get(newPath.getPath()).resolve("testMyFile.txt");
        IPathElement newPathTextFile = new LocalPathElement(txtFile.toString());

        localStorageAdapter.move(StorageType.DIRECTORY, path1, newPath);

        assertTrue("innerDir does not exist after moving", localStorageAdapter.exists(StorageType.DIRECTORY, newPath));
        assertTrue("testMyFile does not exist after moving", localStorageAdapter.exists(StorageType.FILE, newPathTextFile));
        assertFalse("old dir should not exist after moving anymore", localStorageAdapter.exists(StorageType.DIRECTORY, path1));

        localStorageAdapter.delete(newPath);
    }

    @Test
    public void testMoveWithException()
            throws InputOutputException {
        IPathElement path = new LocalPathElement("testDir");
        IPathElement path1 = new LocalPathElement("testDir/innerTestDir");
        IPathElement path2 = new LocalPathElement("testDir/innerTestDir/testMyFile.txt");

        localStorageAdapter.persist(StorageType.DIRECTORY, path, null);
        localStorageAdapter.persist(StorageType.DIRECTORY, path1, null);
        localStorageAdapter.persist(StorageType.FILE, path2, new byte[0]);

        assertTrue("innerDir does not exist", localStorageAdapter.exists(StorageType.DIRECTORY, path1));
        assertTrue("testMyFile.txt does not exist", localStorageAdapter.exists(StorageType.FILE, path2));

        IPathElement newPath = new LocalPathElement("testMyFile.txt");

        // now create the target
        localStorageAdapter.persist(StorageType.FILE, newPath, new byte[0]);

        assertTrue("newPath should exist", localStorageAdapter.exists(StorageType.FILE, newPath));

        thrown.expect(InputOutputException.class);
        localStorageAdapter.move(StorageType.FILE, path2, newPath);

        assertFalse("testMyFile should still exist after moving", localStorageAdapter.exists(StorageType.FILE, newPath));
        assertTrue("old testMyFile should still exist after moving", localStorageAdapter.exists(StorageType.FILE, path2));

        localStorageAdapter.delete(newPath);
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

    @Test
    public void testIsFile()
            throws InputOutputException, IOException {
        String content = "Feel the rhythm feel the blues, it's bobsled time";
        IPathElement path = new LocalPathElement(Config.DEFAULT.getTestFileName1());

        localStorageAdapter.persist(StorageType.FILE, path, content.getBytes());

        byte[] readContent;
        readContent = Files.readAllBytes(ROOT_TEST_DIR.resolve(Config.DEFAULT.getTestFileName1()));

        assertArrayEquals("Content is not equal", content.getBytes(), readContent);

        assertTrue("File should be a file", localStorageAdapter.isFile(path));
        assertFalse("File should not be a dir", localStorageAdapter.isDir(path));

        thrown.expect(InputOutputException.class);
        // this should throw an exception since the file does not exist
        localStorageAdapter.isDir(new LocalPathElement("someRandomFile.txt"));

        thrown.expect(InputOutputException.class);
        localStorageAdapter.isFile(new LocalPathElement("myRandomDir/myRandomFile.txt"));

        // remove the file
        localStorageAdapter.delete(path);
    }

    @Test
    public void testIsDir()
            throws InputOutputException {
        IPathElement path = new LocalPathElement("someDir");
        localStorageAdapter.persist(StorageType.DIRECTORY, path, null);

        assertTrue("Dir should exist after creating", localStorageAdapter.exists(StorageType.DIRECTORY, path));

        assertTrue("Dir should be a directory", localStorageAdapter.isDir(path));
        assertFalse("Dir should not be a file", localStorageAdapter.isFile(path));

        localStorageAdapter.delete(path);

        thrown.expect(InputOutputException.class);
        localStorageAdapter.isDir(new LocalPathElement("someDir/at/someRandom/Place"));

    }

    @Test
    public void testGetDirContents()
            throws InputOutputException {
        IPathElement path = new LocalPathElement("someDir_dircontents");
        localStorageAdapter.persist(StorageType.DIRECTORY, path, null);
        IPathElement path2 = new LocalPathElement("someDir_dircontents/blubDir");
        localStorageAdapter.persist(StorageType.DIRECTORY, path2, null);
        IPathElement path3 = new LocalPathElement("someDir_dircontents/blubDir/myFile.txt");
        localStorageAdapter.persist(StorageType.DIRECTORY, path3, "Blub blub".getBytes());

        List<IPathElement> dirContents = localStorageAdapter.getDirectoryContents(path);
        assertEquals("Should only contain the contents, not itself", 2, dirContents.size());

        assertEquals("First element should be path2", path2.getPath(), dirContents.get(0).getPath());
        assertEquals("2nd element should be path3", path3.getPath(), dirContents.get(1).getPath());


        IPathElement relPath = new LocalPathElement(".");
        IPathElement relPath2 = new LocalPathElement("./");

        List<IPathElement> dirContents2 = localStorageAdapter.getDirectoryContents(relPath);
        List<IPathElement> dirContents3 = localStorageAdapter.getDirectoryContents(relPath2);

        assertEquals("Should contain all 3 elements", 3, dirContents2.size());
        assertEquals("Should contain all 3 elements", 3, dirContents3.size());

        assertEquals("1st item should be someDir_dircontents", path.getPath(), dirContents2.get(0).getPath());
        assertEquals("1st item should be someDir_dircontents", path.getPath(), dirContents3.get(0).getPath());

        assertEquals("2nd item should be someDir", path2.getPath(), dirContents2.get(1).getPath());
        assertEquals("2nd item should be someDir", path2.getPath(), dirContents3.get(1).getPath());

        assertEquals("3rd item should be someDir", path3.getPath(), dirContents2.get(2).getPath());
        assertEquals("3rd item should be someDir", path3.getPath(), dirContents3.get(2).getPath());


        localStorageAdapter.delete(path3);
        localStorageAdapter.delete(path2);
        localStorageAdapter.delete(path);
    }
}
