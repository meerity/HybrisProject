package com.epam.training.service.impl;

import com.epam.training.model.UserWithoutAddressModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.impl.DefaultUserService;

import java.util.List;

public class WithoutAddressUserService extends DefaultUserService {

    private FlexibleSearchService flexibleSearchService;

    public List<UserWithoutAddressModel> getUsersWithoutAddress() {
        String GET_ALL_QUERY =  "SELECT {u:uid}, {u:name} " +
                                "FROM {User AS u} " +
                                "WHERE NOT EXISTS({{ " +
                                    "SELECT {a:pk} " +
                                    "FROM {Address as a} " +
                                    "WHERE {a:owner} = {u:pk} " +
                                "}})";

        SearchResult<String[]> result = flexibleSearchService.search(GET_ALL_QUERY);
        return result.getResult().stream()
                .map(row -> new UserWithoutAddressModel(row[0], row[1]))
                .toList();
    }


    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }
}
