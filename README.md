# Persistence Component

[![Build Status](https://travis-ci.org/p2p-sync/persistence.svg)](https://travis-ci.org/p2p-sync/persistence)
[![Coverage Status](https://coveralls.io/repos/p2p-sync/persistence/badge.svg?branch=master&service=github)](https://coveralls.io/github/p2p-sync/persistence?branch=master)

# Install

Use Maven to add this component as your dependency:

```xml
<repositories>
  <repository>
    <id>persistence-mvn-repo</id>
    <url>https://raw.github.com/p2p-sync/persistence/mvn-repo/</url>
    <snapshots>
      <enabled>true</enabled>
      <updatePolicy>always</updatePolicy>
    </snapshots>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>org.rmatil.sync.persistence</groupId>
    <artifactId>sync-persistence</artifactId>
    <version>0.1-SNAPSHOT</version>
  </dependency>
</dependencies>
```

# Overview

This component should provide an abstraction for manipulating multiple storage systems. Not only storage adapters to the 
local filesystem should be provided but also adapters to stored data in a distributed hash table (DHT) as well as third party services. 

# StorageAdapters
To make adapters work over multiple systems, it is required to abstract the concept of files and directories (e.g. in a flat storage system like a DHT). Therefore, the enum [StorageType](https://github.com/p2p-sync/persistence/blob/master/src/main/java/org/rmatil/sync/persistence/api/StorageType.java) specifies which type an element represents. 
Furthermore, a particular implementation of an  [IPathElement](https://github.com/p2p-sync/persistence/blob/master/src/main/java/org/rmatil/sync/persistence/api/IPathElement.java) serves as identifier of a blob of data and can be different for each adapter. 

## LocalStorageAdapters
Access, write and remove files and directories within the specified root path on the local filesystem.


# License
```
   Copyright 2015 rmatil

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
