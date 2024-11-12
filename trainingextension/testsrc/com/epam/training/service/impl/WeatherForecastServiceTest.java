package com.epam.training.service.impl;

import com.epam.training.model.WeatherForecastModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

@UnitTest
public class WeatherForecastServiceTest {

    private static final String TEST_CITY = "TestCity";
    private static final Date TEST_DATE = new Date();

    private FlexibleSearchService flexibleSearchService;
    private SearchResult searchResult; // убрали дженерик
    private WeatherForecastModel weatherForecastModel;
    private DefaultWeatherForecastService weatherForecastService;

    @Before
    public void setUp() {
        flexibleSearchService = createMock(FlexibleSearchService.class);
        searchResult = createMock(SearchResult.class); // создаем без дженерика
        weatherForecastModel = createMock(WeatherForecastModel.class);

        weatherForecastService = new DefaultWeatherForecastService();
        weatherForecastService.setFlexibleSearchService(flexibleSearchService);
    }

    @Test
    public void testGetWeatherForecastByCityNameDate_WhenForecastExists() {
        // given
        expect(flexibleSearchService.search(anyObject(FlexibleSearchQuery.class))).andReturn(searchResult);
        expect(searchResult.getResult()).andReturn(Collections.singletonList(weatherForecastModel));
        replay(flexibleSearchService, searchResult);

        // when
        WeatherForecastModel result = weatherForecastService.getWeatherForecastByCityNameDate(TEST_CITY, TEST_DATE);

        // then
        assertNotNull("Result should not be null", result);
        assertEquals("Should return the mock model", weatherForecastModel, result);
        verify(flexibleSearchService, searchResult);
    }

    @Test
    public void testGetWeatherForecastByCityNameDate_WhenNoForecastExists() {
        // given
        expect(flexibleSearchService.search(anyObject(FlexibleSearchQuery.class))).andReturn(searchResult);
        expect(searchResult.getResult()).andReturn(Collections.emptyList());
        replay(flexibleSearchService, searchResult);

        // when
        WeatherForecastModel result = weatherForecastService.getWeatherForecastByCityNameDate(TEST_CITY, TEST_DATE);

        // then
        assertNull("Result should be null when no forecast exists", result);
        verify(flexibleSearchService, searchResult);
    }

    @Test
    public void testGetWeatherForecastByCityNameDate_WhenMultipleForecasts() {
        // given
        WeatherForecastModel secondModel = createMock(WeatherForecastModel.class);
        expect(flexibleSearchService.search(anyObject(FlexibleSearchQuery.class))).andReturn(searchResult);
        expect(searchResult.getResult()).andReturn(Arrays.asList(weatherForecastModel, secondModel));
        replay(flexibleSearchService, searchResult, secondModel);

        // when
        WeatherForecastModel result = weatherForecastService.getWeatherForecastByCityNameDate(TEST_CITY, TEST_DATE);

        // then
        assertNotNull("Result should not be null", result);
        assertEquals("Should return the first model", weatherForecastModel, result);
        verify(flexibleSearchService, searchResult, secondModel);
    }

    @Test
    public void testGetWeatherForecastByCityNameDate_WhenFlexibleSearchServiceNotSet() {
        // given
        weatherForecastService = new DefaultWeatherForecastService();

        try {
            // when
            weatherForecastService.getWeatherForecastByCityNameDate(TEST_CITY, TEST_DATE);

            // then
            fail("Should throw IllegalStateException when FlexibleSearchService is not set");
        }
        catch (IllegalStateException expected) {
            // ok - expected exception
            assertNotNull("Exception message should not be null", expected.getMessage());
            assertTrue("Exception message should mention FlexibleSearchService",
                    expected.getMessage().contains("FlexibleSearchService"));
        }
        catch (Exception e) {
            fail("Caught unexpected exception: " + e);
        }
    }
}