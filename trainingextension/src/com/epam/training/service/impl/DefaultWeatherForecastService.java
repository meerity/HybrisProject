package com.epam.training.service.impl;

import com.epam.training.model.WeatherForecastModel;
import com.epam.training.service.WeatherForecastService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;

public class DefaultWeatherForecastService implements WeatherForecastService {

    private FlexibleSearchService flexibleSearchService;

    /**
     *
     * @param cityName name of desired city
     * @param forecastDate date of forecast
     * @return always returns first result in list. if list is empty - returns null
     */
    @Override
    public WeatherForecastModel getWeatherForecastByCityNameDate(String cityName, Date forecastDate) {

        final FlexibleSearchQuery searchQuery = getFlexibleSearchQuery();
        searchQuery.addQueryParameter("city", cityName);
        searchQuery.addQueryParameter("forecastDate", forecastDate);

        final SearchResult<WeatherForecastModel> result = flexibleSearchService.search(searchQuery);
        final List<WeatherForecastModel> resultList = result.getResult();
        return resultList.isEmpty() ? null : resultList.get(0);
    }

    private FlexibleSearchQuery getFlexibleSearchQuery() {
        if (flexibleSearchService == null) {
            throw new IllegalStateException("FlexibleSearchService not initialized");
        }

        String query =
                "SELECT {wf:" + WeatherForecastModel.PK + "} " +
                "FROM {" + WeatherForecastModel._TYPECODE + " AS wf} " +
                "WHERE {wf:" + WeatherForecastModel.CITY + "} = ?city " +
                "AND {wf:" + WeatherForecastModel.FORECASTDATE + "} = ?forecastDate";

        final FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query);
        return searchQuery;
    }

    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }
}
