package org.rmatil.sync.persistence.test.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public enum Config {
    DEFAULT();

    private Path rootTestDir;

    private String testFileName1 = "file1.txt";

    Config() {
        rootTestDir = Paths.get("./org.rmatil.sync.persistence.test.dir");
    }

    public Path getRootTestDir() {
        return this.rootTestDir;
    }

    public String getTestFileName1() {
        return this.testFileName1;
    }
}
