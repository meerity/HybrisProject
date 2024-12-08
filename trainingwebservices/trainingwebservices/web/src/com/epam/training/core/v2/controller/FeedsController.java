/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.epam.training.core.v2.controller;

import de.hybris.platform.commercewebservicescommons.dto.queues.OrderStatusUpdateElementListWsDTO;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import com.epam.training.core.formatters.WsDateFormatter;
import com.epam.training.core.queues.data.OrderStatusUpdateElementData;
import com.epam.training.core.queues.data.OrderStatusUpdateElementDataList;
import com.epam.training.core.queues.impl.OrderStatusUpdateQueue;

import javax.annotation.Resource;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;


@Controller
@RequestMapping(value = "/{baseSiteId}/feeds")
@Tag(name = "Feeds")
public class FeedsController extends BaseController
{
	@Resource(name = "wsDateFormatter")
	private WsDateFormatter wsDateFormatter;
	@Resource(name = "orderStatusUpdateQueue")
	private OrderStatusUpdateQueue orderStatusUpdateQueue;

	@Secured("ROLE_TRUSTED_CLIENT")
	@GetMapping(value = "/orders/statusfeed")
	@ResponseBody
	@Operation(operationId = "getOrderStatusFeed", summary = "Retrieves a list of orders with status updates.", description =
			"Retrieves the orders that have changed status. Only the properties from the current baseSite"
					+ " that have been updated after the specified timestamp are returned.")
	public OrderStatusUpdateElementListWsDTO getOrderStatusFeed(
			@Parameter(description = "Only items newer than the given parameter are retrieved. This parameter should be in ISO-8601 format (for example, 2018-01-09T16:28:45+0000).", required = true) @RequestParam final String timestamp,
			@Parameter(description = "Base site identifier", required = true) @PathVariable final String baseSiteId,
			@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		final Date timestampDate = wsDateFormatter.toDate(timestamp);
		final List<OrderStatusUpdateElementData> orderStatusUpdateElements = orderStatusUpdateQueue.getItems(timestampDate);
		filterOrderStatusQueue(orderStatusUpdateElements, baseSiteId);
		final OrderStatusUpdateElementDataList dataList = new OrderStatusUpdateElementDataList();
		dataList.setOrderStatusUpdateElements(orderStatusUpdateElements);
		return getDataMapper().map(dataList, OrderStatusUpdateElementListWsDTO.class, fields);
	}

	protected void filterOrderStatusQueue(final List<OrderStatusUpdateElementData> orders, final String baseSiteId)
	{
		final Iterator<OrderStatusUpdateElementData> dataIterator = orders.iterator();
		while (dataIterator.hasNext())
		{
			final OrderStatusUpdateElementData orderStatusUpdateData = dataIterator.next();
			if (!baseSiteId.equals(orderStatusUpdateData.getBaseSiteId()))
			{
				dataIterator.remove();
			}
		}
	}
}
