package com.epam.training.service.impl;

import com.epam.training.model.UserWithoutAddressModel;
import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.ServicelayerTest;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

@IntegrationTest
public class WithoutAddressUserServiceIntegrationTest extends ServicelayerTest {

    @Resource
    private WithoutAddressUserService userWithoutAddressService;

    @Resource
    private ModelService modelService;

    @Test
    public void testGetUsersWithoutAddresses() {
        // Given
        final UserModel user1 = modelService.create(UserModel.class);
        user1.setName("Test User 1");
        user1.setUid("test1");

        final UserModel user2 = modelService.create(UserModel.class);
        user2.setName("Test User 2");
        user2.setUid("test2");

        final AddressModel address = modelService.create(AddressModel.class);
        address.setOwner(user1);

        modelService.saveAll(user1, user2, address);

        // When
        List<UserWithoutAddressModel> result = userWithoutAddressService.getUsersWithoutAddress();

        // Then
        boolean found = result.stream()
                .anyMatch(user -> user.getName() != null && user.getName().equals("Test User 2"));
        Assert.assertTrue("User 2 should be found in results", found);
    }

}
