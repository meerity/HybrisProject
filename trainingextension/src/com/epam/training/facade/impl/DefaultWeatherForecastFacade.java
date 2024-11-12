package com.epam.training.facade.impl;

import com.epam.training.data.WeatherForecastData;
import com.epam.training.facade.WeatherForecastFacade;
import com.epam.training.model.WeatherForecastModel;
import com.epam.training.service.WeatherForecastService;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Date;

public class DefaultWeatherForecastFacade implements WeatherForecastFacade {

    private WeatherForecastService weatherForecastService;
    private Converter<WeatherForecastModel, WeatherForecastData> weatherForecastConverter;

    @Override
    public WeatherForecastData findForecastFor(String city, Date forecastDate) {
        WeatherForecastModel model =  weatherForecastService.getWeatherForecastByCityNameDate(city, forecastDate);
        return model != null ? weatherForecastConverter.convert(model) : null;
    }

    public void setWeatherForecastService(WeatherForecastService weatherForecastService) {
        this.weatherForecastService = weatherForecastService;
    }

    public void setWeatherForecastConverter(Converter<WeatherForecastModel, WeatherForecastData> weatherForecastConverter) {
        this.weatherForecastConverter = weatherForecastConverter;
    }
}
