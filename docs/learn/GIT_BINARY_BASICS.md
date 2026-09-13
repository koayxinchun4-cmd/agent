# Git & Binary Basics

> Beginner-friendly notes for English-first self-study.

## 1. Binary

**Binary — 二进制：a number system that uses only `0` and `1`.**

In computing, information is ultimately represented as bits.

- **bit — 二进制位：a single `0` or `1`.**
- **byte — 字节：a group of 8 bits.**
- **binary data — 二进制数据：data represented as raw bytes rather than human-readable text.**
- **binary file — 二进制文件：a file whose contents are intended to be interpreted as bytes rather than plain text.**

Examples of commonly treated binary files:

- JPG / PNG images
- APK files
- compiled programs
- ZIP archives

A text file is also stored as bytes, but its bytes are normally interpreted using a text encoding such as UTF-8.

## 2. Git Blob

**Git Blob — Git 的文件内容对象：the Git object that stores the contents of a file.**

The word **blob** comes from **Binary Large Object**.

A Git Blob does not normally store the file path or filename. Git uses other objects to connect the content to a path in a particular version of the repository.

Conceptually:

```text
File content
    ↓
Git Blob
    ↓
Tree
    ↓
Commit
```

## 3. Git object model

### Blob
**Blob — 文件内容本体：stores file contents.**

For example, the bytes of `nexus-ui-mockup.jpg` can be stored in a Blob.

### Tree
**Tree — 目录结构对象：records directory entries and points to files/subdirectories.**

A Tree connects a path such as:

```text
`docs/images/nexus-ui-mockup.jpg`
```

to the corresponding Blob.

### Commit
**Commit — 提交版本：records a snapshot of the repository state and points to a root Tree.**

A Commit also records metadata such as the author, message, parent commit(s), and timestamp.

## 4. Easy mental model

Think of Git like this:

```text
Blob   = the actual file contents
Tree   = the folder/path structure
Commit = a version of the project
```

This is a simplified learning model, but it is useful when first understanding Git internals.

## 5. Why this matters in Nexus

Nexus contains source code, documentation, images, APKs, and other artifacts. Understanding the difference between a file path and the stored content helps explain how Git tracks project versions.

For example:

```text
`docs/images/nexus-ui-mockup.jpg`
        ↓
Tree entry
        ↓
Blob containing the image bytes
```

The filename is not the Blob itself. The Blob is the stored content.

## 6. Related English vocabulary

| English | 中文 | Meaning |
|---|---|---|
| binary | 二进制 | using or relating to `0` and `1` |
| bit | 二进制位 | one binary digit |
| byte | 字节 | 8 bits |
| data | 数据 | information processed or stored by a computer |
| file | 文件 | a named unit of stored data |
| content | 内容 | the actual information inside something |
| object | 对象 | a stored Git data object |
| blob | 文件内容对象 | Git object containing file contents |
| tree | 目录结构对象 | Git object describing paths and entries |
| commit | 提交版本 | a recorded repository snapshot |
| repository | 代码仓库 | a Git project and its history |

## 7. Learning rule

When learning Git, do not memorize the object model as isolated vocabulary. Connect each term to a concrete operation:

```text
Edit a file
   ↓
git diff
   ↓
git add
   ↓
Blob is created/updated in Git's object database
   ↓
git commit
   ↓
Commit records the new repository snapshot
```

The exact internal object lifecycle is more detailed than this simplified model. The goal here is to build the correct mental model before studying Git internals in depth.
