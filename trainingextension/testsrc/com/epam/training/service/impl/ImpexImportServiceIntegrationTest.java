package com.epam.training.service.impl;

import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.impex.jalo.ImpExException;
import de.hybris.platform.servicelayer.ServicelayerTest;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import static org.junit.Assert.*;


@IntegrationTest
public class ImpexImportServiceIntegrationTest extends ServicelayerTest {

    @Resource
    private ImpexImportService impexImportService;

    @Resource
    private ModelService modelService;

    private final String testDirPath = "temp/impex_test";

    private static final Logger LOG = Logger.getLogger(ImpexImportServiceIntegrationTest.class);

    @Before
    public void setUp() {
        File testDir = new File(testDirPath);
        if (!testDir.exists()) {
            boolean dirsCreated = testDir.mkdirs();
            assertTrue("Cannot create test directory", dirsCreated);
        }
    }

    @After
    public void tearDown() {
        deleteDirectory(new File(testDirPath));
    }

    @Test
    public void importData_WithValidImpexFiles_ShouldImportSuccessfully() throws IOException, ImpExException {
        File impexFile = new File(testDirPath + "/test_valid.impex");
        try (FileWriter writer = new FileWriter(impexFile)) {
            writer.write(
                    "INSERT_UPDATE Product;code[unique=true];name[lang=en]\n" +
                            ";testProduct;Test Product"
            );
        }

        impexImportService.importData(testDirPath);

        final de.hybris.platform.core.model.product.ProductModel importedProduct =
                modelService.get("testProduct");
        assertNotNull("Product hasn't imported", importedProduct);
        assertEquals("Product name isn't correct", "Test Product", importedProduct.getName(Locale.forLanguageTag("en")));
    }

    private void deleteDirectory(File dir) {
        if (dir.exists()) {
            if (dir.isDirectory()) {
                for (File sub : Objects.requireNonNull(dir.listFiles())) {
                    deleteDirectory(sub);
                }
            }
            boolean deleted = dir.delete();
            if (!deleted) {
                LOG.warn("cannot delete directory" + dir.getAbsolutePath());
            }
        }
    }
}