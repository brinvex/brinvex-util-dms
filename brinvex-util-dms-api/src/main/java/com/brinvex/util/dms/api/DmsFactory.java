package com.brinvex.util.dms.api;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

public interface DmsFactory {

    Dms getDms(String workspace);

    static DmsFactory newFilesystemDmsFactory(Path basePath) {
        String factoryImplClassName = "com.brinvex.util.dms.impl.FilesystemDmsFactoryImpl";
        try {
            return (DmsFactory) Class.forName(factoryImplClassName)
                    .getConstructor(Path.class)
                    .newInstance(basePath);
        } catch (ClassNotFoundException
                 | IllegalAccessException
                 | InstantiationException
                 | NoSuchMethodException
                 | InvocationTargetException e
        ) {
            throw new IllegalStateException("Failed to instantiate %s, basePath=%s, %s".formatted(factoryImplClassName, basePath, e), e);
        }
    }
}
