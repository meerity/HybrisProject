package com.epam.training.service;

/**
 * Service import interface
 */
public interface ImportService {

    /**
     * Imports data from specified path
     *
     * @param path path to folder with files to import
     */
    void importData(String path);
}
