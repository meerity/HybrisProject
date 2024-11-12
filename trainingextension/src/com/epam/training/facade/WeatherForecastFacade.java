package com.epam.training.facade;

import com.epam.training.data.WeatherForecastData;
import com.epam.training.model.WeatherForecastModel;

import java.util.Date;

/**
 * Searching weather forecasts interface
 */
public interface WeatherForecastFacade {
    /**
     * Returns the closest weather forecast for city and date specified.
     *
     * @param cityName name of desired city
     * @param forecastDate date of forecast
     * @return WeatherForecastData - if forecast is found for city and date (city matches and date in range +/- 2 hours)
     * null - if there is no forecast for the city and date found.
     */
    WeatherForecastData findForecastFor(String cityName, Date forecastDate);

}
