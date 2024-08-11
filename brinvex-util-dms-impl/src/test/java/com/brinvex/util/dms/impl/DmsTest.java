package com.brinvex.util.dms.impl;

import com.brinvex.util.dms.api.DmsService;
import com.brinvex.util.dms.api.DmsServiceFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class DmsTest {

    private static final Logger LOG = LoggerFactory.getLogger(DmsTest.class);

    private static final DateTimeFormatter workspaceDtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private static DmsServiceFactory dmsServiceFactory;

    private DmsService dmsService;

    @BeforeAll
    static void beforeAll() {
        Path basePath = Path.of("c:/prj/bx-util/bx-util-dms/test-data/");
        dmsServiceFactory = DmsServiceFactory.createFilesystemDmsServiceFactory(basePath);
    }

    @BeforeEach
    void setUp(TestInfo testInfo) {
        String testName = testInfo.getDisplayName();
        dmsService = dmsServiceFactory.getDmsService(testName);
        dmsService.softDeleteAndResetWorkspace();
        LocalDateTime now = LocalDateTime.now();
        LOG.info("setUp {} - invoking dmsService.hardDeleteSoftDeletedWorkspace({})", testName, now);
        dmsService.hardDeleteSoftDeletedWorkspace(now);
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
