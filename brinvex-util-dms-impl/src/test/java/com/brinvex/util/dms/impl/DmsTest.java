package com.brinvex.util.dms.impl;

import com.brinvex.util.dms.api.DmsService;
import com.brinvex.util.dms.api.DmsServiceFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class DmsTest {

    private static final DateTimeFormatter workspaceDtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private static DmsServiceFactory dmsServiceFactory;

    private DmsService dmsService;

    @BeforeAll
    static void beforeAll() throws IOException {
        Path basePath = Path.of("c:/prj/bx-util/bx-util-dms/test-data/");
        hardDeleteOldTestWorkspaces(basePath);
        dmsServiceFactory = DmsServiceFactory.getNewFilesystemDmsServiceFactory(basePath);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void hardDeleteOldTestWorkspaces(Path basePath) throws IOException {
        try (Stream<Path> oldWorkspaces = Files.list(basePath)) {
            oldWorkspaces
                    .filter(ws -> ws.getFileName().toString().startsWith("_DELETED_"))
                    .forEach(ws -> {
                        try (Stream<Path> wsChildStream = Files.walk(ws)) {
                            wsChildStream
                                    .sorted(Comparator.reverseOrder())
                                    .map(Path::toFile)
                                    .peek(f -> System.out.println("Deleting: " + f))
                                    .forEach(File::delete);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
    }

    @BeforeEach
    void setUp(TestInfo testInfo) {
        String workspace = testInfo.getDisplayName() + LocalDateTime.now().format(workspaceDtf);
        dmsService = dmsServiceFactory.getDmsService(workspace);
    }

    @AfterEach
    void tearDown() {
        dmsService.softDeleteWorkspace();
    }

    @Test
    void fileKeys_empty() {
        Collection<String> fileKeys = dmsService.getKeys("some/directory");
        assertTrue(fileKeys.isEmpty());
    }

    @Test
    void add() {
        String content1 = "some_value";
        String directory = "some/directory";
        String key = "some_key";

        dmsService.add(directory, key, content1);
        Collection<String> fileKeys = dmsService.getKeys(directory);
        assertEquals(1, fileKeys.size());
        assertTrue(fileKeys.contains(key));

        boolean exists = dmsService.exists(directory, key);
        assertTrue(exists);

        String content2 = dmsService.getTextContent(directory, key);
        assertEquals(content1, content2);
    }

    @Test
    void add_duplicate() {
        dmsService.add("some/directory", "some_key", "some_value1");
        try {
            dmsService.add("some/directory", "some_key", "some_value2");
            fail("Should fail");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    void softDelete() {
        String content1 = "some_value";
        String directory = "some/directory";
        String key = "some_key";

        dmsService.add(directory, key, content1);
        Collection<String> fileKeys = dmsService.getKeys(directory);
        assertEquals(1, fileKeys.size());
        assertTrue(fileKeys.contains(key));

        boolean exists = dmsService.exists(directory, key);
        assertTrue(exists);

        String content2 = dmsService.getTextContent(directory, key);
        assertEquals(content1, content2);

        dmsService.softDelete(directory, key);

        boolean exists2 = dmsService.exists(directory, key);
        assertFalse(exists2);

        try {
            dmsService.softDelete(directory, key);
            fail("Should fail");
        } catch (IllegalArgumentException expected) {
        }
        try {
            dmsService.getTextContent(directory, key);
            fail("Should fail");
        } catch (IllegalArgumentException expected) {
        }
        try {
            dmsService.hardDelete(directory, key);
            fail("Should fail");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    void hardDelete() {
        String content1 = "some_value";
        String directory = "some/directory";
        String key = "some_key";

        dmsService.add(directory, key, content1);
        Collection<String> fileKeys = dmsService.getKeys(directory);
        assertEquals(1, fileKeys.size());
        assertTrue(fileKeys.contains(key));

        boolean exists = dmsService.exists(directory, key);
        assertTrue(exists);

        String content2 = dmsService.getTextContent(directory, key);
        assertEquals(content1, content2);

        dmsService.hardDelete(directory, key);

        boolean exists2 = dmsService.exists(directory, key);
        assertFalse(exists2);

        try {
            dmsService.softDelete(directory, key);
            fail("Should fail");
        } catch (IllegalArgumentException expected) {
        }
        try {
            dmsService.getTextContent(directory, key);
            fail("Should fail");
        } catch (IllegalArgumentException expected) {
        }
        try {
            dmsService.hardDelete(directory, key);
            fail("Should fail");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    void hardDeleteAll() throws InterruptedException {
        String content = "some_value";
        String directory = "some/directory";
        String key = "some_key";

        dmsService.add(directory, key, content);
        Collection<String> fileKeys = dmsService.getKeys(directory);
        assertEquals(1, fileKeys.size());
        assertTrue(fileKeys.contains(key));

        assertTrue(dmsService.exists(directory, key));

        int hardDeleted = dmsService.hardDeleteAllSoftDeleted(directory);
        assertEquals(0, hardDeleted);

        assertTrue(dmsService.exists(directory, key));

        dmsService.softDelete(directory, key);
        assertFalse(dmsService.exists(directory, key));

        dmsService.add(directory, key, content);
        assertTrue(dmsService.exists(directory, key));

        LocalDateTime timeBeforeSecondSoftDel = LocalDateTime.now();
        Thread.sleep(Duration.ofSeconds(1));

        dmsService.softDelete(directory, key);
        assertFalse(dmsService.exists(directory, key));

        hardDeleted = dmsService.hardDeleteAllSoftDeleted(directory, timeBeforeSecondSoftDel);
        assertEquals(1, hardDeleted);

        hardDeleted = dmsService.hardDeleteAllSoftDeleted(directory);
        assertEquals(1, hardDeleted);

        dmsService.add(directory, key, content);
        assertTrue(dmsService.exists(directory, key));

        dmsService.softDelete(directory, key);
        assertFalse(dmsService.exists(directory, key));

        dmsService.add(directory, key, content);
        assertTrue(dmsService.exists(directory, key));

        dmsService.softDelete(directory, key);
        assertFalse(dmsService.exists(directory, key));

        hardDeleted = dmsService.hardDeleteAllSoftDeleted(directory, timeBeforeSecondSoftDel);
        assertEquals(0, hardDeleted);

        hardDeleted = dmsService.hardDeleteAllSoftDeleted(directory);
        assertEquals(2, hardDeleted);

    }
}
