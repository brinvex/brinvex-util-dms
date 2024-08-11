package com.brinvex.util.dms.impl;

import com.brinvex.util.dms.api.DmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SuppressWarnings("DuplicatedCode")
public class FilesystemDmsServiceImpl implements DmsService {

    private static final Logger LOG = LoggerFactory.getLogger(FilesystemDmsServiceImpl.class);

    private final String workspace;

    private final Path workspacePath;

    @SuppressWarnings("SpellCheckingInspection")
    private static class SoftDeleteHelper {
        private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");
        private static final Pattern softDelPrefixPattern = Pattern.compile("^_DELETED_(\\d{8}_\\d{6}_\\d{3})_!@#-$");
        private static final int softDelPrefixPatternLength = "_DELETED_yyyyMMdd_HHmmss_SSS_!@#-".length();

        private static Path contructSoftDeletedPath(Path oldPath, LocalDateTime timestamp) {
            String softDelPrefix = "_DELETED_" + dtf.format(timestamp) + "_!@#-";
            return oldPath.getParent().resolve(softDelPrefix + oldPath.getFileName());
        }

        private static boolean isSoftDeleted(String filename, String origKey, LocalDateTime softDeletedBefore) {
            int filenameLength = filename.length();
            if (filenameLength <= softDelPrefixPatternLength) {
                return false;
            }
            String left = filename.substring(0, softDelPrefixPatternLength);
            boolean result = true;
            if (origKey != null) {
                String right = filename.substring(softDelPrefixPatternLength);
                result = right.equals(origKey);
            }
            if (result) {
                Matcher m = softDelPrefixPattern.matcher(left);
                if (m.find()) {
                    if (softDeletedBefore != null) {
                        LocalDateTime softDelDate = LocalDateTime.parse(m.group(1), dtf);
                        result = softDelDate.isBefore(softDeletedBefore);
                    }
                } else {
                    result = false;
                }
            }
            return result;
        }

        private static boolean isSoftDeleted(String filename) {
            int filenameLength = filename.length();
            if (filenameLength <= softDelPrefixPatternLength) {
                return false;
            }
            String left = filename.substring(0, softDelPrefixPatternLength);
            return softDelPrefixPattern.matcher(left).matches();
        }
    }

    public FilesystemDmsServiceImpl(Path basePath, String workspace) {
        validateWorkspaceSyntax(workspace);
        this.workspace = workspace;
        this.workspacePath = basePath.resolve(workspace);
        if (!Files.exists(workspacePath)) {
            try {
                Files.createDirectories(workspacePath);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to create workspace: %s".formatted(workspacePath), e);
            }
        } else if (!Files.isDirectory(workspacePath)) {
            throw new IllegalArgumentException("Workspace is not a directory: %s".formatted(workspace));
        }
    }

    @Override
    public void add(String directory, String key, String textContent, Charset charset) {
        validateDirectorySyntax(directory);
        validateKeySyntax(key);
        Path directoryPath = getOrCreateDirectory(directory);
        Path filePath = directoryPath.resolve(key);
        if (Files.exists(filePath)) {
            throw new IllegalArgumentException("Document already exists: workspace='%s', directory='%s', key='%s'".formatted(workspace, directory, key));
        }
        try {
            Files.writeString(filePath, textContent, charset);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write to the file: %s".formatted(filePath), e);
        }
    }

    @Override
    public void add(String directory, String key, byte[] binaryContent) {
        validateDirectorySyntax(directory);
        validateKeySyntax(key);
        Path directoryPath = getOrCreateDirectory(directory);
        Path filePath = directoryPath.resolve(key);
        if (Files.exists(filePath)) {
            throw new IllegalArgumentException("Document already exists: workspace='%s', directory='%s', key='%s'".formatted(workspace, directory, key));
        }
        try {
            Files.write(filePath, binaryContent);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write to the file: %s".formatted(filePath), e);
        }
    }

    @Override
    public void put(String directory, String key, String textContent, Charset charset) {
        validateDirectorySyntax(directory);
        validateKeySyntax(key);
        Path directoryPath = getOrCreateDirectory(directory);
        Path filePath = directoryPath.resolve(key);
        try {
            Files.writeString(filePath, textContent, charset);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write to the file: %s".formatted(filePath), e);
        }
    }

    @Override
    public void put(String directory, String key, byte[] binaryContent) {
        validateDirectorySyntax(directory);
        validateKeySyntax(key);
        Path directoryPath = getOrCreateDirectory(directory);
        Path filePath = directoryPath.resolve(key);
        try {
            Files.write(filePath, binaryContent);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write to the file: %s".formatted(filePath), e);
        }
    }

    @Override
    public boolean exists(String directory, String key) {
        validateDirectorySyntax(directory);
        validateKeySyntax(key);
        Path directoryPath = workspacePath.resolve(directory);
        if (!Files.exists(directoryPath)) {
            return false;
        } else if (!Files.isDirectory(directoryPath)) {
            throw new IllegalArgumentException("Not a directory: %s, workspace=%s".formatted(directoryPath, workspace));
        }
        return Files.exists(directoryPath.resolve(key));
    }

    @Override
    public Collection<String> getKeys(String directory) {
        validateDirectorySyntax(directory);
        Path directoryPath = workspacePath.resolve(directory);
        if (!Files.exists(directoryPath)) {
            return Collections.emptyList();
        } else if (!Files.isDirectory(directoryPath)) {
            throw new IllegalArgumentException("Not a directory: %s, workspace=%s".formatted(directoryPath, workspace));
        }
        try (Stream<Path> fileStream = Files.list(directoryPath)) {
            return fileStream
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(Predicate.not(SoftDeleteHelper::isSoftDeleted))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to list files at path: %s".formatted(directoryPath), e);
        }
    }

    @Override
    public String getTextContent(String directory, String key, Charset charset) {
        validateDirectorySyntax(directory);
        validateKeySyntax(key);
        Path filePath = workspacePath.resolve(directory).resolve(key);
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("Document doesn't exist: workspace='%s', directory='%s', key='%s'".formatted(workspace, directory, key));
        }
        try {
            return Files.readString(filePath, charset);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read the file %s".formatted(filePath), e);
        }
    }

    @Override
    public byte[] getBinaryContent(String directory, String key) {
        validateDirectorySyntax(directory);
        validateKeySyntax(key);
        Path filePath = workspacePath.resolve(directory).resolve(key);
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("Document doesn't exist: workspace='%s', directory='%s', key='%s'".formatted(workspace, directory, key));
        }
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read the file %s".formatted(filePath), e);
        }
    }

    @Override
    public void softDelete(String directory, String key) {
        validateDirectorySyntax(directory);
        validateKeySyntax(key);
        Path filePath = workspacePath.resolve(directory).resolve(key);
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("Document doesn't exist: workspace='%s', directory='%s', key='%s'".formatted(workspace, directory, key));
        }
        Path newSoftDelPath = SoftDeleteHelper.contructSoftDeletedPath(filePath, LocalDateTime.now());
        try {
            Files.move(filePath, newSoftDelPath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to move %s -> %s".formatted(filePath, newSoftDelPath), e);
        }
    }

    @Override
    public void hardDelete(String directory, String key) {
        validateDirectorySyntax(directory);
        validateKeySyntax(key);
        Path directoryPath = workspacePath.resolve(directory);
        Path filePath = directoryPath.resolve(key);
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("Document doesn't exist: workspace='%s', directory='%s', key='%s'".formatted(workspace, directory, key));
        }
        try {
            Files.delete(filePath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete %s".formatted(filePath), e);
        }
    }

    @Override
    public int hardDeleteAllSoftDeleted(String directory, String origKey, LocalDateTime softDeletedBefore) {
        validateDirectorySyntax(directory);
        if (origKey != null) {
            validateKeySyntax(origKey);
        }
        Path directoryPath = workspacePath.resolve(directory);
        if (!Files.exists(directoryPath)) {
            return 0;
        } else if (!Files.isDirectory(directoryPath)) {
            throw new IllegalArgumentException("Not a directory: %s, workspace=%s".formatted(directoryPath, workspace));
        }
        List<Path> filesToHardDelete;
        try (Stream<Path> fileStream = Files.list(directoryPath)) {
            filesToHardDelete = fileStream
                    .filter(p -> SoftDeleteHelper.isSoftDeleted(p.getFileName().toString(), origKey, softDeletedBefore))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to list files at path: %s".formatted(directoryPath), e);
        }
        for (Path fileToHardDelete : filesToHardDelete) {
            try {
                LOG.info("Hard deleting: {}", fileToHardDelete);
                Files.delete(fileToHardDelete);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to delete: %s".formatted(fileToHardDelete), e);
            }
        }
        return filesToHardDelete.size();
    }

    @Override
    public void softDeleteAndResetWorkspace() {
        Path newSoftDelWorkspacePath = SoftDeleteHelper.contructSoftDeletedPath(workspacePath, LocalDateTime.now());
        try {
            Files.move(workspacePath, newSoftDelWorkspacePath);
            Files.createDirectory(workspacePath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to move %s -> %s".formatted(workspacePath, newSoftDelWorkspacePath), e);
        }
    }

    @Override
    public int hardDeleteSoftDeletedWorkspace(LocalDateTime softDeletedBefore) {
        try (Stream<Path> workspaces = Files.list(workspacePath.getParent())) {
            List<Path> softDeletedWorkspaces = workspaces
                    .filter(ws -> SoftDeleteHelper.isSoftDeleted(ws.getFileName().toString(), null, softDeletedBefore))
                    .toList();
            for (Path softDeletedWorkspace : softDeletedWorkspaces) {
                try (Stream<Path> wsChildStream = Files.walk(softDeletedWorkspace)) {
                    wsChildStream
                            .sorted(Comparator.reverseOrder())
                            .peek(f -> LOG.info("Recursively hard-deleting: {} ", f))
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                } catch (IOException e) {
                                    throw new UncheckedIOException(e);
                                }
                            });
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            return softDeletedWorkspaces.size();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path getOrCreateDirectory(String directory) {
        Path directoryPath = workspacePath.resolve(directory);
        if (!Files.exists(directoryPath)) {
            try {
                Files.createDirectories(directoryPath);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to create directory: %s".formatted(directoryPath), e);
            }
        } else if (!Files.isDirectory(directoryPath)) {
            throw new IllegalArgumentException("Not a directory: %s, workspace=%s".formatted(directoryPath, workspace));
        }
        return directoryPath;
    }

    private void validateWorkspaceSyntax(String workspaceName) {
        if (workspaceName == null || workspaceName.isBlank()) {
            throw new IllegalArgumentException("Invalid workspace: %s".formatted(workspaceName));
        }
    }

    private void validateDirectorySyntax(String directoryName) {
        if (directoryName == null || directoryName.isBlank()) {
            throw new IllegalArgumentException("Invalid directory: %s".formatted(directoryName));
        }
    }

    private void validateKeySyntax(String keyName) {
        if (keyName == null || keyName.isBlank()) {
            throw new IllegalArgumentException("Invalid key: %s".formatted(keyName));
        }
    }

}
