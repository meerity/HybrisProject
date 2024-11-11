package com.epam.training.service.impl;

import com.epam.training.data.UserWithoutAddressData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@UnitTest
public class WithoutAddressUserServiceUnitTest {
    @Mock
    private FlexibleSearchService flexibleSearchService;

    @InjectMocks
    private WithoutAddressUserService withoutAddressUserService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetUsersWithoutAddress() {
        // Given
        final SearchResult<String[]> mockResult = mock(SearchResult.class);
        final List<String[]> resultList = new ArrayList<>();
        resultList.add(new String[]{ "test1", "Test User 1" });

        when(flexibleSearchService.<String[]>search(anyString())).thenReturn(mockResult);
        when(mockResult.getResult()).thenReturn(resultList);

        // When
        List<UserWithoutAddressData> result = withoutAddressUserService.getUsersWithoutAddress();

        // Then
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("test1", result.get(0).getUid());
        verify(flexibleSearchService).search(anyString());
    }
}