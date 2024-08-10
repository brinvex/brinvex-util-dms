package com.brinvex.util.dms.api;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collection;

/**
 * The {@code DmsService} interface defines the operations for managing documents in a
 * Document Management System (DMS). This includes storing, retrieving, and deleting
 * both text and binary data within a directory-based structure.
 */
public interface DmsService {

    /**
     * The default charset used for text content operations if none is specified.
     */
    Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * Retrieves all keys within the specified directory.
     *
     * @param directory The directory to search.
     * @return A collection of keys found in the directory.
     */
    Collection<String> getKeys(String directory);

    /**
     * Adds text content to the specified directory using the default charset (UTF-8).
     *
     * @param directory   The directory to add the content to.
     * @param key         The key associated with the content.
     * @param textContent The text content to add.
     */
    default void add(String directory, String key, String textContent) {
        add(directory, key, textContent, StandardCharsets.UTF_8);
    }

    /**
     * Adds text content to the specified directory with the specified charset.
     *
     * @param directory   The directory to add the content to.
     * @param key         The key associated with the content.
     * @param textContent The text content to add.
     * @param charset     The charset for encoding the text.
     */
    void add(String directory, String key, String textContent, Charset charset);

    /**
     * Adds binary content to the specified directory.
     *
     * @param directory     The directory to add the content to.
     * @param key           The key associated with the content.
     * @param binaryContent The binary content to add.
     */
    void add(String directory, String key, byte[] binaryContent);

    /**
     * Replaces or adds text content in the specified directory using the default charset (UTF-8).
     *
     * @param directory   The directory to store the content.
     * @param key         The key associated with the content.
     * @param textContent The text content to store.
     */
    default void put(String directory, String key, String textContent) {
        put(directory, key, textContent, DEFAULT_CHARSET);
    }

    /**
     * Replaces or adds text content in the specified directory with the specified charset.
     *
     * @param directory   The directory to store the content.
     * @param key         The key associated with the content.
     * @param textContent The text content to store.
     * @param charset     The charset for encoding the text.
     */
    void put(String directory, String key, String textContent, Charset charset);

    /**
     * Replaces or adds binary content in the specified directory.
     *
     * @param directory     The directory to store the content.
     * @param key           The key associated with the content.
     * @param binaryContent The binary content to store.
     */
    void put(String directory, String key, byte[] binaryContent);

    /**
     * Checks if the specified key exists in the directory.
     *
     * @param directory The directory to search.
     * @param key       The key to check for existence.
     * @return {@code true} if the key exists, otherwise {@code false}.
     */
    boolean exists(String directory, String key);

    /**
     * Retrieves the text content associated with the specified key and charset.
     *
     * @param directory The directory to search.
     * @param key       The key associated with the content.
     * @param charset   The charset for decoding the text.
     * @return The text content as a {@code String}.
     */
    String getTextContent(String directory, String key, Charset charset);

    /**
     * Retrieves the text content associated with the specified key using the default charset (UTF-8).
     *
     * @param directory The directory to search.
     * @param key       The key associated with the content.
     * @return The text content as a {@code String}.
     */
    default String getTextContent(String directory, String key) {
        return getTextContent(directory, key, StandardCharsets.UTF_8);
    }

    /**
     * Retrieves the binary content associated with the specified key.
     *
     * @param directory The directory to search.
     * @param key       The key associated with the content.
     * @return The binary content as a {@code byte[]}.
     */
    byte[] getBinaryContent(String directory, String key);

    /**
     * Marks the content associated with the specified key for deletion.
     * The content will not be immediately removed but marked as deleted.
     *
     * @param directory The directory containing the content.
     * @param key       The key associated with the content to be soft-deleted.
     */
    void softDelete(String directory, String key);

    /**
     * Permanently removes the content associated with the specified key.
     *
     * @param directory The directory containing the content.
     * @param key       The key associated with the content to be deleted.
     */
    void hardDelete(String directory, String key);

    /**
     * Permanently removes all soft-deleted content in the specified directory
     * that matches the optional parameters.
     *
     * @param directory         The directory to clean up.
     * @param origKey           The key to filter the content by (optional).
     * @param softDeletedBefore The cutoff date and time for soft deletion (optional).
     * @return The number of documents deleted.
     */
    int hardDeleteAllSoftDeleted(String directory, String origKey, LocalDateTime softDeletedBefore);

    /**
     * Permanently removes all soft-deleted content in the specified directory
     * that was deleted before the specified time.
     *
     * @param directory         The directory to clean up.
     * @param softDeletedBefore The cutoff date and time for soft deletion (optional).
     * @return The number of documents deleted.
     */
    default int hardDeleteAllSoftDeleted(String directory, LocalDateTime softDeletedBefore) {
        return hardDeleteAllSoftDeleted(directory, null, softDeletedBefore);
    }

    /**
     * Permanently removes all soft-deleted content in the specified directory.
     *
     * @param directory The directory to clean up.
     * @return The number of documents deleted.
     */
    default int hardDeleteAllSoftDeleted(String directory) {
        return hardDeleteAllSoftDeleted(directory, null, null);
    }

    /**
     * Soft deletes the entire workspace. After invoking this method, no other methods
     * in this interface should be invoked, as the workspace is marked for deletion.
     */
    void softDeleteWorkspace();
}