package com.epam.training.service;

/**
 * Service import interface
 */
public interface ImportService {

    /**
     * Imports data from specified path
     *
     * @param path path to folder with files to import
     * @return if import was successful or not
     */
    boolean importData(String path);
}
