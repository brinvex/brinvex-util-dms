package com.brinvex.util.dms.api;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

public interface DmsServiceFactory {

    DmsService getDmsService(String workspace);

    @SuppressWarnings({"unchecked"})
    static DmsServiceFactory getNewFilesystemDmsServiceFactory(Path basePath) {
        try {
            String implClassName = "com.brinvex.util.dms.impl.FilesystemDmsServiceFactoryImpl";
            Class<? extends DmsServiceFactory> implClass = (Class<? extends DmsServiceFactory>) Class.forName(implClassName);
            Constructor<? extends DmsServiceFactory> implConstructor = implClass.getConstructor(Path.class);
            return implConstructor.newInstance(basePath);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
