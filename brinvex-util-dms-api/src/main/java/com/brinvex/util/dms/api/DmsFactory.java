package com.brinvex.util.dms.api;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

public interface DmsFactory {

    Dms getDms(String workspace);

    static DmsFactory createFilesystemDmsFactory(Path basePath) {
        try {
            return (DmsFactory) Class.forName("com.brinvex.util.dms.impl.FilesystemDmsFactoryImpl")
                    .getConstructor(Path.class)
                    .newInstance(basePath);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
