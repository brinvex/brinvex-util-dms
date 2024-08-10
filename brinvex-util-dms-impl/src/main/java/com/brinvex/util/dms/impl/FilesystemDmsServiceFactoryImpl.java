package com.brinvex.util.dms.impl;

import com.brinvex.util.dms.api.DmsService;
import com.brinvex.util.dms.api.DmsServiceFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FilesystemDmsServiceFactoryImpl implements DmsServiceFactory {

    private final Path basePath;

    private final Map<String, FilesystemDmsServiceImpl> workspaceToDmsService = new ConcurrentHashMap<>();

    public FilesystemDmsServiceFactoryImpl(Path basePath) {
        if (basePath == null || !Files.exists(basePath)) {
            throw new IllegalArgumentException("basePath=%s does not exist".formatted(basePath));
        }
        this.basePath = basePath;
    }

    @Override
    public DmsService getDmsService(String workspace) {
        return workspaceToDmsService.computeIfAbsent(workspace, k -> new FilesystemDmsServiceImpl(basePath, workspace));
    }
}
