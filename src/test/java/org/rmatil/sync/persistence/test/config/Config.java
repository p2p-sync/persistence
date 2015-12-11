package org.rmatil.sync.persistence.test.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public enum Config {
    DEFAULT();

    private Path rootTestDir;

    private       String testFileName1   = "file1.txt";
    private       String testIpV4Address = "127.0.0.1";
    private       String testLocationKey = "testLocation";
    private       int    testPort        = 4001;
    private final Random RND             = new Random(42L);


    Config() {
        rootTestDir = Paths.get("./org.rmatil.sync.persistence.test.dir");
    }

    public Path getRootTestDir() {
        return this.rootTestDir;
    }

    public String getTestFileName1() {
        return this.testFileName1;
    }

    public String getTestIpV4Address() {
        return testIpV4Address;
    }

    public int getTestPort() {
        return testPort;
    }

    public Random getRnd() {
        return this.RND;
    }

    public String getTestLocationKey() {
        return testLocationKey;
    }

}
