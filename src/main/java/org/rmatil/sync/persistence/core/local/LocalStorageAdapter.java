package org.rmatil.sync.persistence.core.local;

import org.rmatil.sync.persistence.api.IPathElement;
import org.rmatil.sync.persistence.api.IStorageAdapter;
import org.rmatil.sync.persistence.api.StorageType;
import org.rmatil.sync.persistence.exceptions.InputOutputException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * A storage adapter which stores the data on local disk
 * relative to the specified root directory.
 */
public class LocalStorageAdapter implements IStorageAdapter {

    protected Path rootDir;

    protected OpenOption[] optionOptions;

    public LocalStorageAdapter(Path rootDir) {
        this.rootDir = rootDir;
        this.optionOptions = new OpenOption[]{WRITE, CREATE};
    }

    public void persist(StorageType type, IPathElement path, byte[] bytes)
            throws InputOutputException {
        Path filePath = rootDir.resolve(path.getPath());

        switch (type) {
            case FILE:
                writeData(filePath, bytes);
                break;
            case DIRECTORY:
                createDir(filePath);
                break;
        }
    }

    public void delete(IPathElement path)
            throws InputOutputException {
        Path filePath = rootDir.resolve(path.getPath());
        this.delete(filePath);
    }

    public byte[] read(IPathElement path)
            throws InputOutputException {

        Path filePath = rootDir.resolve(path.getPath());

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new InputOutputException(e);
        }
    }

    public boolean exists(StorageType storageType, IPathElement path) {
        Path filePath = rootDir.resolve(path.getPath());

        if (! filePath.toFile().exists()) {
            return false;
        }

        switch (storageType) {
            case FILE:
                return filePath.toFile().isFile();
            case DIRECTORY:
                return filePath.toFile().isDirectory();
        }

        return false;
    }

    public Path getRootDir() {
        return this.rootDir;
    }

    /**
     * Creates a directory on the given file path
     *
     * @param filePath The file path to create as directory
     *
     * @throws InputOutputException If an IOException occurred
     */
    protected void createDir(Path filePath)
            throws InputOutputException {
        try {
            Files.createDirectory(filePath);
        } catch (IOException e) {
            throw new InputOutputException(e);
        }
    }

    /**
     * Writes the given data to the specified file path
     *
     * @param filePath The file to which the data should be written
     * @param bytes    The bytes to write
     *
     * @throws InputOutputException If an IOException occurred
     */
    protected void writeData(Path filePath, byte[] bytes)
            throws InputOutputException {
        try {
            Files.write(filePath, bytes, this.optionOptions);
        } catch (IOException e) {
            throw new InputOutputException(e);
        }
    }

    /**
     * Deletes recursively the given file (if it is a directory)
     * or just removes itself
     *
     * @param path The file or dir to remove
     *
     * @throws InputOutputException If an error occurred during deletion
     */
    protected void delete(Path path)
            throws InputOutputException {

        if (! path.toFile().exists()) {
            throw new InputOutputException(path.toString() + " (No such file or directory)");
        }

        File file = path.toFile();

        if (file.isDirectory()) {
            File[] contents = file.listFiles();

            if (null != contents) {
                for (File child : contents) {
                    delete(child.toPath());
                }
            }
        }

        file.delete();
    }
}
