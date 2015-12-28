package org.rmatil.sync.persistence.core.local;

import org.rmatil.sync.persistence.api.IFileMetaInfo;
import org.rmatil.sync.persistence.api.IPathElement;
import org.rmatil.sync.persistence.api.IStorageAdapter;
import org.rmatil.sync.persistence.api.StorageType;
import org.rmatil.sync.persistence.core.FileMetaInfo;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import sun.security.x509.IPAddressName;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
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
        this.optionOptions = new OpenOption[]{WRITE, CREATE, TRUNCATE_EXISTING};
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

    @Override
    public byte[] read(IPathElement path, int offset, int length)
            throws InputOutputException {

        Path filePath = rootDir.resolve(path.getPath());

        byte[] chunk = new byte[length];
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(filePath.toFile());
            BufferedInputStream buffer = new BufferedInputStream(fileInputStream);
            // skip to offset
            buffer.skip(offset);
            int bytesRead = buffer.read(chunk, 0, length);
            buffer.close();

            if (bytesRead < length && bytesRead > -1) {
                // we have to truncate the byte array to not return null bytes
                byte[] trimmedChunk = new byte[bytesRead];
                System.arraycopy(chunk, 0, trimmedChunk, 0, bytesRead);
                return trimmedChunk;
            }

            return chunk;
        } catch (IOException e) {
            throw new InputOutputException(e);
        }
    }

    @Override
    public void move(StorageType storageType, IPathElement oldPath, IPathElement newPath)
            throws InputOutputException {

        Path oldFilePath = rootDir.resolve(oldPath.getPath());
        Path newFilePath = rootDir.resolve(newPath.getPath());

        if (this.exists(storageType, newPath)) {
            throw new InputOutputException("Target path " + newFilePath.toString() + " does already exist");
        }

        try {
            Files.move(oldFilePath, newFilePath, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new InputOutputException(e);
        }
    }

    @Override
    public IFileMetaInfo getMetaInformation(IPathElement path)
            throws InputOutputException {
        Path filePath = rootDir.resolve(path.getPath());

        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(filePath.toFile());

            return new FileMetaInfo(fileInputStream.getChannel().size());
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
