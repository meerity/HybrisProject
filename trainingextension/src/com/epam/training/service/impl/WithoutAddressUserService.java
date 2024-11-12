package com.epam.training.service.impl;

import com.epam.training.data.UserWithoutAddressData;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.impl.DefaultUserService;

import java.util.Arrays;
import java.util.List;

public class WithoutAddressUserService extends DefaultUserService {

    private FlexibleSearchService flexibleSearchService;

    public List<UserWithoutAddressData> getUsersWithoutAddress() {
        final String query = "SELECT {u:" + UserModel.PK + "}, " +
                "{u:" + UserModel.NAME + "} " +
                "FROM {" + UserModel._TYPECODE + " AS u} " +
                "WHERE NOT EXISTS ({{ " +
                "SELECT {a:" + AddressModel.PK + "} " +
                "FROM {" + AddressModel._TYPECODE + " AS a} " +
                "WHERE {a:" + AddressModel.OWNER + "} = {u:" + UserModel.PK + "}}})";

        FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query);
        searchQuery.setResultClassList(Arrays.asList(String.class, String.class));

        SearchResult<List<String>> result = flexibleSearchService.search(searchQuery);
        return result.getResult().stream()
                .map(list -> new UserWithoutAddressData(list.get(0), list.get(1)))
                .toList();
    }


    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }
}
