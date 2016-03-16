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

This component provides an abstraction for manipulating multiple storage systems. Not only storage adapters to filesystems using a tree-like approach to store data are be provided but also adapters to persist data in a distributed hash table (DHT).

# Storage Adapters
A storage adapter abstracts the access to the underlying file system strucutre resp. implementation.
Its specification is provided in [`IStorageAdapter`](https://github.com/p2p-sync/persistence/blob/master/src/main/java/org/rmatil/sync/persistence/api/IStorageAdapter.java). Due to these different structures, each interface specifying a type of a storage adapter must also provide
an implementation of [`IPathElement`](https://github.com/p2p-sync/persistence/blob/master/src/main/java/org/rmatil/sync/persistence/api/IPathElement.java), defining how values can be retrieved.

**Currently Specified Adapters**

* [Tree Storage Adapter](https://github.com/p2p-sync/persistence#tree-storage-adapter)
  * [Local Storage Adapter](https://github.com/p2p-sync/persistence#local-storage-adapter)
* [Dht Storage Adapter](https://github.com/p2p-sync/persistence#dht-storage-adapter)
  * [Unsecured Dht Storage Adapter](https://github.com/p2p-sync/persistence#unsecured-dht-storage-adapter)
  * [Secured Dht Storage Adapter](https://github.com/p2p-sync/persistence#secured-dht-storage-adapter)


## Tree Storage Adapter
An `ITreeStorageAdapter` extends from the general `IStorageAdapter` and uses the concept of files and directories (implementations could also resolve sym-/hardlinks) to organise persisted data. It is specified in [`ITreeStorageAdapter`](https://github.com/p2p-sync/persistence/blob/master/src/main/java/org/rmatil/sync/persistence/core/tree/ITreeStorageAdapter.java). Additionally to the basic CRUD operations (Create, Read, Update, Delete), implementations of this interface must also provide methods to check whether a particular element is a file resp. a directory and accessor to retrieve contents of a directory.

### Local Storage Adapter
Currently, one implementation of an `ITreeStorageAdapter` is integrated, specified in [`ILocalStorageAdapter`](https://github.com/p2p-sync/persistence/blob/master/src/main/java/org/rmatil/sync/persistence/core/tree/local/ILocalStorageAdapter.java). It provides the access to a local file system, using Java's `io` functionality.
Such an adapter is always relative to a particular root directory, path elements are then resolved to this root before
their contents are fetched from the file system

## Dht Storage Adapter
Besides the tree-like storage adapters, an interface for accessing data in distributed hash tables is specified in [`IDhtStorageAdapter`](https://github.com/p2p-sync/persistence/blob/master/src/main/java/org/rmatil/sync/persistence/core/dht/IDhtStorageAdapter.java)

### Unsecured Dht Storage Adapter
The `IUnsecuredDhtStorageAdapter` defines access to data stored in the `Distributed Hash Table` using only a `LocationKey` and a `ContentKey`. Data maintained by such an adapter is not protected against overwrites from other nodes at all.
Its interface is specified in [`IUnsecuredDhtStorageAdapter`](https://github.com/p2p-sync/persistence/blob/master/src/main/java/org/rmatil/sync/persistence/core/dht/unsecured/IUnsecuredDhtStorageAdapter.java)

### Secured Dht Storage Adapter
In contrast, an `ISecuredDhtStorageAdapter` is also responsible to provide a protection mechanism of its stored values by
using a third dimension besides the `LocationKey` and the `ContentKey`: the `DomainKey`. Values stored by using such 
an adapter are protected against overwrites from other clients by signing messages. Its interface can be found in [`ISecuredDhtStorageAdapter`](https://github.com/p2p-sync/persistence/blob/master/src/main/java/org/rmatil/sync/persistence/core/dht/secured/ISecuredDhtStorageAdapter.java)


## Example

```java

import org.rmatil.sync.persistence.core.tree.ITreeStorageAdapter;
import org.rmatil.sync.persistence.core.tree.TreePathElement;
import org.rmatil.sync.persistence.core.tree.local.LocalStorageAdapter;

import java.nio.file.Paths;
import java.util.List;

// ...

  ITreeStorageAdapter treeStorageAdapter = new LocalStorageAdapter(
    Paths.get("path/to/root/dir")
  );

  // get the directory contents at path/to/root/dir/directory
  TreePathElement directoryElement = new TreePathElement("directory");
  List<TreePathElement> directoryContents = treeStorageAdapter.getDirectoryContents(directoryElement);
  
// ...


```


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
