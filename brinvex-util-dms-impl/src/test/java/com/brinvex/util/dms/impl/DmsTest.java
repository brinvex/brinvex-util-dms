package com.brinvex.util.dms.impl;

import com.brinvex.util.dms.api.Dms;
import com.brinvex.util.dms.api.DmsFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static DmsFactory dmsFactory;

    private Dms dms;

    @BeforeAll
    static void beforeAll() {
        Path basePath = Path.of("c:/prj/bx-util/bx-util-dms/test-data/");
        dmsFactory = DmsFactory.createFilesystemDmsFactory(basePath);
    }

    @BeforeEach
    void setUp(TestInfo testInfo) {
        String testName = testInfo.getDisplayName();
        dms = dmsFactory.getDms(testName);
        dms.resetWorkspace();
        LocalDateTime now = LocalDateTime.now();
        LOG.info("setUp {} - invoking dms.purgeWorkspace({})", testName, now);
        int purged = dms.purgeWorkspace(now);
        LOG.info("setUp {} - purged={}", testName, purged);
    }

    @Test
    void fileKeys_empty() {
        Collection<String> fileKeys = dms.getKeys("some/directory");
        assertTrue(fileKeys.isEmpty());
    }

    @Test
    void add() {
        String content1 = "some_value";
        String directory = "some/directory";
        String key = "some_key";

        dms.add(directory, key, content1);
        Collection<String> fileKeys = dms.getKeys(directory);
        assertEquals(1, fileKeys.size());
        assertTrue(fileKeys.contains(key));

        boolean exists = dms.exists(directory, key);
        assertTrue(exists);

        String content2 = dms.getTextContent(directory, key);
        assertEquals(content1, content2);
    }

    @Test
    void add_duplicate() {
        dms.add("some/directory", "some_key", "some_value1");
        try {
            dms.add("some/directory", "some_key", "some_value2");
            fail("Should fail");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    void put() {
        boolean added;
        String directory = "some/directory";
        String key = "some_key";
        added = dms.put(directory, key, "some_value1");
        assertTrue(added);

        added = dms.put(directory, key, "some_value1");
        assertFalse(added);

        dms.delete(directory, key);
        added = dms.put(directory, key, "some_value1");
        assertTrue(added);

        dms.delete(directory, key);
        added = dms.put(directory, key, "some_value1");
        assertTrue(added);

        added = dms.put(directory, key, "some_value1");
        assertFalse(added);

        int purged;
        purged = dms.purge(directory);
        assertEquals(4, purged);
        purged = dms.purge(directory);
        assertEquals(0, purged);


    }


    @Test
    void softDelete() {
        String content1 = "some_value";
        String directory = "some/directory";
        String key = "some_key";

        dms.add(directory, key, content1);
        Collection<String> fileKeys = dms.getKeys(directory);
        assertEquals(1, fileKeys.size());
        assertTrue(fileKeys.contains(key));

        boolean exists = dms.exists(directory, key);
        assertTrue(exists);

        String content2 = dms.getTextContent(directory, key);
        assertEquals(content1, content2);

        dms.delete(directory, key);

        boolean exists2 = dms.exists(directory, key);
        assertFalse(exists2);

        try {
            dms.delete(directory, key);
            fail("Should fail");
        } catch (IllegalArgumentException expected) {
        }
        try {
            dms.getTextContent(directory, key);
            fail("Should fail");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    void hardDelete() {
        String content1 = "some_value";
        String directory = "some/directory";
        String key = "some_key";

        dms.add(directory, key, content1);
        Collection<String> fileKeys = dms.getKeys(directory);
        assertEquals(1, fileKeys.size());
        assertTrue(fileKeys.contains(key));

        boolean exists = dms.exists(directory, key);
        assertTrue(exists);

        String content2 = dms.getTextContent(directory, key);
        assertEquals(content1, content2);

        dms.delete(directory, key);
        dms.purge(directory, key, LocalDateTime.now());

        boolean exists2 = dms.exists(directory, key);
        assertFalse(exists2);

        try {
            dms.delete(directory, key);
            fail("Should fail");
        } catch (IllegalArgumentException expected) {
        }
        try {
            dms.getTextContent(directory, key);
            fail("Should fail");
        } catch (IllegalArgumentException expected) {
        }
        try {
            dms.delete(directory, key);
            dms.purge(directory, key, LocalDateTime.now());
            fail("Should fail");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    void hardDeleteAll() throws InterruptedException {
        String content = "some_value";
        String directory = "some/directory";
        String key = "some_key";

        dms.add(directory, key, content);
        Collection<String> fileKeys = dms.getKeys(directory);
        assertEquals(1, fileKeys.size());
        assertTrue(fileKeys.contains(key));

        assertTrue(dms.exists(directory, key));

        int hardDeleted = dms.purge(directory);
        assertEquals(0, hardDeleted);

        assertTrue(dms.exists(directory, key));

        dms.delete(directory, key);
        assertFalse(dms.exists(directory, key));

        dms.add(directory, key, content);
        assertTrue(dms.exists(directory, key));

        LocalDateTime timeBeforeSecondSoftDel = LocalDateTime.now();
        Thread.sleep(Duration.ofSeconds(1));

        dms.delete(directory, key);
        assertFalse(dms.exists(directory, key));

        hardDeleted = dms.purge(directory, timeBeforeSecondSoftDel);
        assertEquals(1, hardDeleted);

        hardDeleted = dms.purge(directory);
        assertEquals(1, hardDeleted);

        dms.add(directory, key, content);
        assertTrue(dms.exists(directory, key));

        dms.delete(directory, key);
        assertFalse(dms.exists(directory, key));

        dms.add(directory, key, content);
        assertTrue(dms.exists(directory, key));

        dms.delete(directory, key);
        assertFalse(dms.exists(directory, key));

        hardDeleted = dms.purge(directory, timeBeforeSecondSoftDel);
        assertEquals(0, hardDeleted);

        hardDeleted = dms.purge(directory);
        assertEquals(2, hardDeleted);

    }
}
