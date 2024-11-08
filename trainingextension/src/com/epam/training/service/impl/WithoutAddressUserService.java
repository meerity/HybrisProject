package com.epam.training.service.impl;

import com.epam.training.model.UserWithoutAddressModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.impl.DefaultUserService;

import java.util.List;

public class WithoutAddressUserService extends DefaultUserService {

    private FlexibleSearchService flexibleSearchService;

    public List<UserWithoutAddressModel> getUsersWithoutAddress() {
        final String query = "SELECT {u:" + UserModel.PK + "}, " +
                "{u:" + UserModel.NAME + "} " +
                "FROM {" + UserModel._TYPECODE + " AS u} " +
                "WHERE NOT EXISTS ({{ " +
                "SELECT {a:" + AddressModel.PK + "} " +
                "FROM {" + AddressModel._TYPECODE + " AS a} " +
                "WHERE {a:" + AddressModel.OWNER + "} = {u:" + UserModel.PK + "}}})";

        SearchResult<Object[]> result = flexibleSearchService.search(query);
        return result.getResult().stream()
                .map(row -> new UserWithoutAddressModel((String) row[0], (String) row[1]))
                .toList();
    }


    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }
}
