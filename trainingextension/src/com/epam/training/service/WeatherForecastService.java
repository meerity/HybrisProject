package com.epam.training.service;

import com.epam.training.model.WeatherForecastModel;

import java.util.Date;

/**
 * Weather forecast interface
 */
public interface WeatherForecastService {

    /**
     * Returns the closest weather forecast for city and date specified.
     *
     * @param cityName name of desired city
     * @param forecastDate date of forecast
     * @return WeatherForecastData - if forecast is found for city and date (city matches and date in range +/- 2 hours)
     * null - if there is no forecast for the city and date found.
     */
    WeatherForecastModel getWeatherForecastByCityNameDate(String cityName, Date forecastDate);
}
