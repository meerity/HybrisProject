package com.epam.training.populators;

import com.epam.training.data.WeatherForecastData;
import com.epam.training.model.WeatherForecastModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class WeatherForecastPopulator implements Populator<WeatherForecastModel, WeatherForecastData> {

    @Override
    public void populate(WeatherForecastModel source, WeatherForecastData target) throws ConversionException {
        target.setCity(source.getCity());
        target.setForecastDate(source.getForecastDate());
        target.setTemperature(source.getTemperature());
        target.setPrecipitation(source.getPrecipitation());
        target.setStatus(source.getStatus().toString());
    }
}
