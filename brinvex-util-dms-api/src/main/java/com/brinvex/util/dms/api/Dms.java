package com.brinvex.util.dms.api;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;

/**
 * The {@code DmsService} interface defines the operations for managing documents in a
 * Document Management System (DMS). This includes storing, retrieving, and deleting
 * both text and binary data within a directory-based structure.
 */
public interface Dms {

    Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * Retrieves all keys within the specified directory.
     * The returned collection is sorted in ascending order.
     */
    SequencedCollection<String> getKeys(String directory);

    /**
     * Adds a new document under the given key.
     * If a document with the given key already exists, this method will throw an exception.
     */
    default void add(String directory, String key, String textContent) {
        add(directory, key, textContent, StandardCharsets.UTF_8);
    }

    /**
     * Adds a new document under the given key.
     * If a document with the given key already exists, this method will throw an exception.
     */
    void add(String directory, String key, String textContent, Charset charset);

    /**
     * Adds a new document under the given key.
     * If a document with the given key already exists, this method will throw an exception.
     */
    void add(String directory, String key, byte[] binaryContent);

    /**
     * If the key does not already exist, the document is added, and the method returns {@code true}.
     * If the key already exists, the document's content is updated, and the method returns {@code false}.
     */
    default boolean put(String directory, String key, String textContent) {
        return put(directory, key, textContent, DEFAULT_CHARSET);
    }

    /**
     * If the key does not already exist, the document is added, and the method returns {@code true}.
     * If the key already exists, the document's content is updated, and the method returns {@code false}.
     */
    boolean put(String directory, String key, String textContent, Charset charset);

    /**
     * If the key does not already exist, the document is added, and the method returns {@code true}.
     * If the key already exists, the document's content is updated, and the method returns {@code false}.
     */
    boolean put(String directory, String key, byte[] binaryContent);

    /**
     * Stores the {@link Map} object to a file.
     * If the key does not already exist, the document is added, and the method returns {@code true}.
     * If the key already exists, the document's content is updated, and the method returns {@code false}.
     */
    default boolean put(String directory, String key, Map<String, String> propertiesContent) {
        return put(directory, key, propertiesContent, DEFAULT_CHARSET);
    }

    /**
     * Stores the {@link Map} object to a file.
     * If the key does not already exist, the document is added, and the method returns {@code true}.
     * If the key already exists, the document's content is updated, and the method returns {@code false}.
     */
    boolean put(String directory, String key, Map<String, String> propertiesContent, Charset charset);

    /**
     * Checks if the specified key exists in the directory.
     */
    boolean exists(String directory, String key);

    /**
     * Retrieves the text content associated with the specified key and charset.
     */
    String getTextContent(String directory, String key, Charset charset);

    /**
     * Retrieves the text content associated with the specified key using the default charset (UTF-8).
     */
    default String getTextContent(String directory, String key) {
        return getTextContent(directory, key, StandardCharsets.UTF_8);
    }

    /**
     * Retrieves the text lines associated with the specified key and charset.
     */
    List<String> getTextLines(String directory, String key, Charset charset);

    /**
     * Retrieves the text lines associated with the specified key using the default charset (UTF-8).
     */
    default List<String> getTextLines(String directory, String key) {
        return getTextLines(directory, key, StandardCharsets.UTF_8);
    }

    /**
     * Retrieves the binary content associated with the specified key.
     */
    byte[] getBinaryContent(String directory, String key);

    default Map<String, String> getPropertiesContent(String directory, String key) {
        return getPropertiesContent(directory, key, DEFAULT_CHARSET);
    }

    /**
     * Loads the {@link Map} object from a file associated with the specified key.
     */
    Map<String, String> getPropertiesContent(String directory, String key, Charset charset);

    /**
     * Soft-deletes the document associated with the given key.
     */
    void delete(String directory, String key);

    /**
     * Permanently hard-deletes all obsolete(deleted or overridden) documents matching the given criteria.
     */
    int purge(String directory, String origKey, LocalDateTime obsoleteBefore);

    /**
     * Permanently hard-deletes all obsolete(deleted or overridden) documents matching the given criteria.
     */
    default int purge(String directory, LocalDateTime obsoleteBefore) {
        return purge(directory, null, obsoleteBefore);
    }

    /**
     * Permanently hard-deletes all obsolete(deleted or overridden) documents matching the given criteria.
     */
    default int purge(String directory) {
        return purge(directory, null, null);
    }

    /**
     * Soft-deletes the entire workspace and initializes a new one.
     */
    void resetWorkspace();

    /**
     * Soft-deletes the workspace.
     */
    void deleteWorkspace();

    /**
     * Hard-deletes the obsolete(deleted or overridden) workspace versions.
     */
    int purgeWorkspace(LocalDateTime obsoleteBefore);
}