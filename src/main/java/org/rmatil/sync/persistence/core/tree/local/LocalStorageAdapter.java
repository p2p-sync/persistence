package org.rmatil.sync.persistence.core.tree.local;

import org.rmatil.sync.commons.hashing.Hash;
import org.rmatil.sync.commons.hashing.HashingAlgorithm;
import org.rmatil.sync.persistence.api.IFileMetaInfo;
import org.rmatil.sync.persistence.api.StorageType;
import org.rmatil.sync.persistence.core.FileMetaInfo;
import org.rmatil.sync.persistence.core.tree.TreePathElement;
import org.rmatil.sync.persistence.exceptions.InputOutputException;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardOpenOption.*;

/**
 * A storage adapter which stores the data on the local disk
 * relative to the specified root directory.
 */
public class LocalStorageAdapter implements ILocalStorageAdapter {

    protected Path rootDir;

    protected OpenOption[] optionOptions;

    public LocalStorageAdapter(Path rootDir) {
        this.rootDir = rootDir;
        this.optionOptions = new OpenOption[]{WRITE, CREATE, TRUNCATE_EXISTING};
    }

    @Override
    synchronized public void persist(StorageType type, TreePathElement path, byte[] bytes)
            throws InputOutputException {
        this.persist(type, path, 0, bytes);
    }

    @Override
    synchronized public void persist(StorageType type, TreePathElement path, long offset, byte[] bytes)
            throws InputOutputException {
        Path filePath = rootDir.resolve(path.getPath());

        switch (type) {
            case FILE:
                writeData(filePath, offset, bytes);
                break;
            case DIRECTORY:
                createDir(filePath);
                break;
        }
    }

    @Override
    synchronized public void delete(TreePathElement path)
            throws InputOutputException {
        Path filePath = rootDir.resolve(path.getPath());
        this.delete(filePath);
    }

    @Override
    synchronized public byte[] read(TreePathElement path)
            throws InputOutputException {

        Path filePath = rootDir.resolve(path.getPath());

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new InputOutputException(e);
        }
    }

    @Override
    synchronized public byte[] read(TreePathElement path, long offset, int length)
            throws InputOutputException {

        Path filePath = rootDir.resolve(path.getPath());

        byte[] chunk = new byte[length];
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(filePath.toFile());
            BufferedInputStream buffer = new BufferedInputStream(fileInputStream);
            // skip to offset
            buffer.skip(offset);
            int bytesRead = buffer.read(chunk, 0, length);
            buffer.close();

            if (bytesRead < length && bytesRead > - 1) {
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
    synchronized public void move(StorageType storageType, TreePathElement oldPath, TreePathElement newPath)
            throws InputOutputException {

        Path oldFilePath = rootDir.resolve(oldPath.getPath());
        Path newFilePath = rootDir.resolve(newPath.getPath());

        if (this.exists(storageType, newPath)) {
            throw new InputOutputException("Target path " + newFilePath.toString() + " does already exist");
        }

        switch (storageType) {
            case FILE:
                try {
                    Files.move(oldFilePath, newFilePath, StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException e) {
                    throw new InputOutputException(e);
                }
                break;
            case DIRECTORY:
                // https://github.com/carlspring/commons-io/blob/master/src/main/java/org/carlspring/commons/io/FileUtils.java
                // https://github.com/carlspring/commons-io/blob/master/src/test/java/org/carlspring/commons/io/FileUtilsTest.java
                if (! oldFilePath.toFile().isDirectory()) {
                    throw new InputOutputException(oldFilePath.toAbsolutePath().toString() + " is not a directory!");
                }

                if (! newFilePath.toFile().exists()) {
                    newFilePath.toFile().mkdirs();
                }

                try {
                    // merge files
                    Files.move(oldFilePath, newFilePath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new InputOutputException(e);
                }

                break;
        }

    }

    @Override
    synchronized public IFileMetaInfo getMetaInformation(TreePathElement path)
            throws InputOutputException {
        Path filePath = rootDir.resolve(path.getPath());

        File file = filePath.toFile();
        if (! file.exists()) {
            throw new InputOutputException("Could not get meta information for path " + path.getPath() + ". No such file or directory");
        }

        if (file.isDirectory()) {
            return new FileMetaInfo(0, false, "");
        }

        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(filePath.toFile());

            int fileExtDot = filePath.getFileName().toString().lastIndexOf('.');
            String fileExt = (fileExtDot == - 1) ? "" : filePath.getFileName().toString().substring(fileExtDot + 1);

            return new FileMetaInfo(fileInputStream.getChannel().size(), true, fileExt);
        } catch (IOException e) {
            throw new InputOutputException(e);
        }
    }

    @Override
    synchronized public boolean exists(StorageType storageType, TreePathElement path) {
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

    @Override
    synchronized public boolean isFile(TreePathElement path)
            throws InputOutputException {
        Path filePath = rootDir.resolve(path.getPath());

        if (! filePath.toFile().exists()) {
            throw new InputOutputException("Can not check whether element on path " + path.getPath() + " is a file: No such file or directory");
        }

        return filePath.toFile().isFile();
    }

    @Override
    synchronized public boolean isDir(TreePathElement path)
            throws InputOutputException {
        Path filePath = rootDir.resolve(path.getPath());

        if (! filePath.toFile().exists()) {
            throw new InputOutputException("Can not check whether element on path " + path.getPath() + " is a directory: No such file or directory");
        }

        return filePath.toFile().isDirectory();
    }

    @Override
    public List<TreePathElement> getDirectoryContents(TreePathElement directory)
            throws InputOutputException {

        if ("/".equals(directory.getPath())) {
            // prevent resolving to actual root
            // use root of this root dir instead
            directory = new TreePathElement("");
        }

        Path filePath = rootDir.resolve(directory.getPath());

        if (! filePath.toFile().exists()) {
            throw new InputOutputException("No such file or directory");
        }

        if (! filePath.toFile().isDirectory()) {
            throw new InputOutputException("Path must be a directory");
        }

        File canonicalFile;
        Path canonicalRootPath;
        try {
            canonicalFile = filePath.toFile().getCanonicalFile();
            canonicalRootPath = Paths.get(rootDir.toFile().getCanonicalPath());
        } catch (IOException e) {
            throw new InputOutputException("Can not convert to canonical path. Message: " + e.getMessage());
        }

        List<TreePathElement> pathElements = new ArrayList<>();
        File[] files = canonicalFile.listFiles();

        if (null == files) {
            throw new InputOutputException("Path must be a directory");
        }

        for (File file : files) {
            pathElements.add(new TreePathElement(canonicalRootPath.relativize(file.toPath()).toString()));

            // add all subdirs too
            if (file.isDirectory()) {
                pathElements.addAll(this.getDirectoryContents(
                        new TreePathElement(canonicalRootPath.relativize(file.toPath()).toString())
                ));
            }
        }

        return pathElements;
    }

    @Override
    public String getChecksum(TreePathElement path)
            throws InputOutputException {
        Path filePath = this.rootDir.resolve(path.getPath());

        if (! this.isFile(path)) {
            throw new InputOutputException("Failed to generate checksum. Only files can have a checksum");
        }

        try {
            return Hash.hash(HashingAlgorithm.MD5, filePath.toFile());
        } catch (IOException e) {
            throw new InputOutputException(e);
        }
    }

    @Override
    public TreePathElement getRootDir() {
        return new TreePathElement(this.rootDir.toString());
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
     * @param offset   The offset where to start writing data
     * @param bytes    The bytes to write
     *
     * @throws InputOutputException If an IOException occurred
     */
    protected void writeData(Path filePath, long offset, byte[] bytes)
            throws InputOutputException {

        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(filePath.toString(), "rw");

            long maxAllowedOffset;
            if (offset > randomAccessFile.getChannel().size()) {
                maxAllowedOffset = randomAccessFile.getChannel().size();
            } else {
                maxAllowedOffset = offset;
            }

            if (maxAllowedOffset == 0) {
                // truncate the file to zero length
                randomAccessFile.setLength(0);
            }

            randomAccessFile.seek(maxAllowedOffset);
            randomAccessFile.write(bytes);
            randomAccessFile.close();
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

        File file;
        try {
            file = path.toFile().getCanonicalFile();
        } catch (IOException e) {
            throw new InputOutputException("Could not get canonical file from path " + path.toString());
        }

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
