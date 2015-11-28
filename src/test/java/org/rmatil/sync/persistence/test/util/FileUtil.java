package org.rmatil.sync.persistence.test.util;


import org.rmatil.sync.persistence.test.config.Config;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class FileUtil {

    /**
     * Creates the testFile in the given rootDirectory
     *
     * @param rootDir The root directory in which the test file is created
     *
     * @return The path of the created test file
     */
    public static Path createTestFile(Path rootDir) {
        Path testFilePath = rootDir.resolve(Config.DEFAULT.getTestFileName1());

        try {
            testFilePath = Files.createFile(testFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return testFilePath;
    }

    /**
     * Modifies the test file in the given root directory
     *
     * @param rootDir The root directory in which the file should be modified
     *
     * @return The path of the modified file
     */
    public static Path modifyTestFile(Path rootDir) {
        Path testFilePath = rootDir.resolve(Config.DEFAULT.getTestFileName1());

        try {
            String s = "Hello World! ";
            byte data[] = s.getBytes();
            OutputStream out = new BufferedOutputStream(Files.newOutputStream(testFilePath, CREATE, APPEND));
            out.write(data, 0, data.length);
            out.close();
        } catch (IOException x) {
            System.err.println(x);
        }

        return testFilePath;
    }

    /**
     * Deletes the test file from the given root directory
     *
     * @param rootDir The root directory from which the test file is removed
     *
     * @return The path of the deleted test file
     */
    public static Path deleteTestFile(Path rootDir) {
        Path testFilePath = rootDir.resolve(Config.DEFAULT.getTestFileName1());

        File file = testFilePath.toFile();

        if (file.exists()) {
            file.delete();
        }

        return testFilePath;
    }

    /**
     * Deletes recursively the given file (if it is a directory)
     * or just removes itself
     *
     * @param file The file or dir to remove
     *
     * @return True if the deletion was successful
     */
    public static boolean delete(File file) {
        if (file.isDirectory()) {
            File[] contents = file.listFiles();

            if (null != contents) {
                for (File child : contents) {
                    delete(child);
                }
            }

            file.delete();

            return true;
        } else {
            return file.delete();
        }
    }
}
