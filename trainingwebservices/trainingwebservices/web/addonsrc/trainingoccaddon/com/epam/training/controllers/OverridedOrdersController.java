package com.epam.training.controllers;

import com.epam.training.core.skipfield.SkipOrderFieldValueSetter;
import com.epam.training.core.v2.controller.BaseCommerceController;
import de.hybris.platform.commercefacades.order.OrderFacade;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commerceservices.request.mapping.annotation.RequestMappingOverride;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderWsDTO;
import de.hybris.platform.util.Config;
import de.hybris.platform.webservicescommons.cache.CacheControl;
import de.hybris.platform.webservicescommons.cache.CacheControlDirective;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import java.util.Collections;
import java.util.List;

import static com.epam.training.core.v2.controller.OrdersController.TOGGLE_GET_ORDER_BY_CODE_OR_GUID_ENABLED;


@Controller
@RequestMapping(value = "/{baseSiteId}")
@Tag(name = "Orders")
public class OverridedOrdersController extends BaseCommerceController {

    @Resource(name = "orderFacade")
    private OrderFacade orderFacade;

    @Resource(name = "skipOrderFieldValueSetter")
    private SkipOrderFieldValueSetter skipOrderFieldValueSetter;

    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/users/{userId}/orders/{code}", method = RequestMethod.GET)
    @RequestMappingOverride
    @CacheControl(directive = CacheControlDirective.PRIVATE)
    @Cacheable(value = "orderCache", key = "T(de.hybris.platform.commercewebservicescommons.cache.CommerceCacheKeyGenerator).generateKey(true,true,'getOrderForUserByCode',#code,#fields)")
    @ResponseBody
    @Operation(operationId = "getUserOrders", summary = "Retrieves the order.", description = "Retrieves the details of the order. To get entryGroup information, set fields value as follows: fields=entryGroups(BASIC), fields=entryGroups(DEFAULT), or fields=entryGroups(FULL).")
    @ApiBaseSiteIdAndUserIdParam
    public List<OrderWsDTO> getUserOrders(
            @Parameter(description = "Order GUID or order code.", required = true) @PathVariable final String code,
            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        skipOrderFieldValueSetter.setValue(fields);
        OrderData orderData;
        if(Config.getBoolean(TOGGLE_GET_ORDER_BY_CODE_OR_GUID_ENABLED, false))
        {
            // get order by code or guid
            orderData = orderFacade.getOrderDetailsForPotentialId(code);
        } else {
            // get order only by code
            orderData = orderFacade.getOrderDetailsForCode(code);
        }
        return Collections.singletonList(getDataMapper().map(orderData, OrderWsDTO.class, fields));
    }
}
