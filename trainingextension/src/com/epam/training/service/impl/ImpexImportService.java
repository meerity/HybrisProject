package com.epam.training.service.impl;

import com.epam.training.service.ImportService;
import de.hybris.platform.impex.jalo.ImpExManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Service for importing ImpEx files
 */
public class ImpexImportService implements ImportService {

    private static final Logger LOG = Logger.getLogger(ImpexImportService.class.getName());

    /**
     * Imports ImpEx files from specified folder
     *
     * @param path path to folder with files to import
     */
    @Override
    public void importData(String path) {
        File folder = new File(path);
        if (!folder.exists() || !folder.isDirectory()) {
            LOG.error("Invalid path. Folder does not exist or it is not a folder: " + path);
            return;
        }
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".impex"));
        if (files == null || files.length == 0) {
            LOG.error("Folder does not contain .impex files: " + path);
            return;
        }
        LOG.info("Starting import");
        for (File file : files) {
            try (InputStream is = new FileInputStream(file)) {
                LOG.info("Importing file: " + file.getName());

                boolean enableCodeExecution = true;

                ImpExManager.getInstance().importData(is, "UTF-8",
                        '\n',
                        '"',
                        enableCodeExecution);

                LOG.info("Import successful: " + file.getName());
            } catch (Exception e) {
                LOG.error("Error while importing file: " + file.getName(), e);
            }

        }
    }
}
