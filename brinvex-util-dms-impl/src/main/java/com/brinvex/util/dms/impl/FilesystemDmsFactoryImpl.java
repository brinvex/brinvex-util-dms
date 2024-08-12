package com.brinvex.util.dms.impl;

import com.brinvex.util.dms.api.Dms;
import com.brinvex.util.dms.api.DmsFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FilesystemDmsFactoryImpl implements DmsFactory {

    private final Path basePath;

    private final Map<String, FilesystemDmsImpl> workspaceToDmsService = new ConcurrentHashMap<>();

    public FilesystemDmsFactoryImpl(Path basePath) {
        if (basePath == null || !Files.exists(basePath)) {
            throw new IllegalArgumentException("basePath=%s does not exist".formatted(basePath));
        }
        this.basePath = basePath;
    }

    @Override
    public Dms getDms(String workspace) {
        return workspaceToDmsService.computeIfAbsent(workspace, k -> new FilesystemDmsImpl(basePath, workspace));
    }
}
