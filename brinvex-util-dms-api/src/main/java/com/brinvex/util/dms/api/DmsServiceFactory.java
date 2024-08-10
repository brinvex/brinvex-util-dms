package com.brinvex.util.dms.api;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

public interface DmsServiceFactory {

    DmsService getDmsService(String workspace);

    static DmsServiceFactory createFilesystemDmsServiceFactory(Path basePath) {
        try {
            return (DmsServiceFactory) Class.forName("com.brinvex.util.dms.impl.FilesystemDmsServiceFactoryImpl")
                    .getConstructor(Path.class)
                    .newInstance(basePath);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
