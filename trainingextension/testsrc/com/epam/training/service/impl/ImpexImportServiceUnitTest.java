package com.epam.training.service.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.impex.jalo.ImpExManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class ImpexImportServiceUnitTest {

    @Mock
    private ImpExManager impExManager;

    private String testFolderPath;

    @InjectMocks
    private ImpexImportService impexImportService;

    @Before
    public void setUp() {
        URL resourceUrl = getClass().getClassLoader().getResource("test/impex");
        testFolderPath = new File(Objects.requireNonNull(resourceUrl).getFile()).getAbsolutePath();
    }

    @Test
    public void shouldImportValidImpexFiles() {
        try (MockedStatic<ImpExManager> impExManagerStatic = mockStatic(ImpExManager.class)) {
            // given
            impExManagerStatic.when(ImpExManager::getInstance).thenReturn(impExManager);

            // when
            impexImportService.importData(testFolderPath);

            // then
            verify(impExManager, times(2)).importData(
                    any(InputStream.class),
                    eq("UTF-8"),
                    eq('\n'),
                    eq('"'),
                    eq(true)
            );
        }
    }

    @Test
    public void shouldHandleNonExistentFolder() {
        // when
        impexImportService.importData("/non/existent/path");

        // then
        verify(impExManager, never()).importData(
                any(InputStream.class),
                anyString(),
                any(Character.class),
                any(Character.class),
                any(Boolean.class)
        );
    }

    @Test
    public void shouldHandleEmptyFolder() {
        // given
        String emptyFolderPath = new File(testFolderPath).getParent();

        // when
        impexImportService.importData(emptyFolderPath);

        // then
        verify(impExManager, never()).importData(
                any(InputStream.class),
                anyString(),
                any(Character.class),
                any(Character.class),
                any(Boolean.class)
        );
    }

    @Test
    public void shouldHandleImportException() {
        try (MockedStatic<ImpExManager> impExManagerStatic = mockStatic(ImpExManager.class)) {
            // given
            impExManagerStatic.when(ImpExManager::getInstance).thenReturn(impExManager);
            when(impExManager.importData(
                    any(InputStream.class),
                    anyString(),
                    any(Character.class),
                    any(Character.class),
                    any(Boolean.class)
            )).thenThrow(new RuntimeException("Test exception"));

            // when
            impexImportService.importData(testFolderPath);

            // then
            verify(impExManager, times(2)).importData(
                    any(InputStream.class),
                    eq("UTF-8"),
                    eq('\n'),
                    eq('"'),
                    eq(true)
            );
        }
    }
}