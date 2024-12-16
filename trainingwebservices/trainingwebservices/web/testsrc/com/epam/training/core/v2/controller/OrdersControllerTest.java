/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.epam.training.core.v2.controller;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.OrderFacade;
import de.hybris.platform.commercewebservicescommons.dto.order.EntryGroupWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderWsDTO;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import com.epam.training.core.skipfield.SkipOrderFieldValueSetter;
import com.epam.training.core.strategies.OrderCodeIdentificationStrategy;
import com.epam.training.core.v2.helper.OrdersHelper;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;


/**
 * Unit test for {@link OrdersController}
 */
@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class OrdersControllerTest
{
	private static final String ORDER_CODE = "00000001";
	private static final String FIELD_ENTRIES = "entries(BASIC)";
	@Mock
	private OrderFacade orderFacade;
	@Mock
	private OrdersHelper ordersHelper;
	@Mock
	private OrderCodeIdentificationStrategy orderCodeIdentificationStrategy;
	@Mock
	private DataMapper dataMapper;
	@Mock
	private SkipOrderFieldValueSetter skipOrderFieldValueSetter;
	@InjectMocks
	private OrdersController ordersController;

	@Test
	public void testGetOrder()
	{
		final OrderWsDTO wsDTO = new OrderWsDTO();
		wsDTO.setEntryGroups(List.of(new EntryGroupWsDTO()));
		given(dataMapper.map(any(), eq(OrderWsDTO.class), anyString())).willReturn(wsDTO);
		ordersController.getOrder(ORDER_CODE, FIELD_ENTRIES);
		verify(skipOrderFieldValueSetter).setValue(FIELD_ENTRIES);
	}


}
