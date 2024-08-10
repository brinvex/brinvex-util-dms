## Brinvex-Util-DMS

### Introduction

_Brinvex-Util-DMS_ is a very simple and minimalistic Document Management System.
It provides methods to store, retrieve, and delete text and binary data in a directory-based structure. 
The interface supports both soft and hard deletion of documents, allowing for flexibility in how data is managed and retained.

### Features 
- **Document Storage:**
    - Store text and binary content in a directory-based structure.
    - Supports customizable text encoding with a default of `UTF-8`.

- **Document Retrieval:**
    - Retrieve text or binary content using a key-based lookup

- **Key Management:**
    - Retrieve a collection of all keys within a specific directory.
    - Check for the existence of a document using its key.

- **Soft & Hard Deletion:**
    - **Soft Deletion:** Marks documents for deletion without immediately removing them.
    - **Hard Deletion:** Permanently removes documents, either individually or in bulk.


### Maven dependency declaration
To use _Brinvex-Util-DMS_ in your Maven project, declare the following dependency in your project's pom file. 
No transitive dependencies are required during compilation or at runtime.
````

<repository>
    <id>repository.brinvex</id>
    <name>Brinvex Repository</name>
    <url>https://github.com/brinvex/brinvex-repo/raw/main/</url>
    <snapshots>
        <enabled>false</enabled>
    </snapshots>
</repository>

<dependency>
    <groupId>com.brinvex.util</groupId>
    <artifactId>brinvex-util-dms-api</artifactId>
    <version>1.0.0</version>
</dependency>
<dependency>
    <groupId>com.brinvex.util</groupId>
    <artifactId>brinvex-util-dms-impl</artifactId>
    <version>1.0.0</version>
    <scope>runtime</scope>
</dependency>
````

### Requirements
- Java 21 or above

### License

- The _Brinvex-Util-DMS_ is released under version 2.0 of the Apache License.

