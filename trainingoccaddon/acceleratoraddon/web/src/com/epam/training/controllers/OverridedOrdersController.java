package com.epam.training.controllers;

import com.epam.training.core.v2.controller.BaseCommerceController;
import com.epam.training.core.v2.helper.OrdersHelper;
import de.hybris.platform.commerceservices.request.mapping.annotation.RequestMappingOverride;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderHistoryListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderWsDTO;
import de.hybris.platform.webservicescommons.cache.CacheControl;
import de.hybris.platform.webservicescommons.cache.CacheControlDirective;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import java.util.List;


@Controller
@RequestMapping(value = "/{baseSiteId}")
@Tag(name = "Orders")
public class OverridedOrdersController extends BaseCommerceController {

    @Resource(name = "ordersHelper")
    private OrdersHelper ordersHelper;

    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @CacheControl(directive = CacheControlDirective.PRIVATE)
    @RequestMapping(value = "/users/{userId}/orders", method = RequestMethod.GET)
    @RequestMappingOverride
    @ResponseBody
    @Operation(operationId = "getUserOrderHistory", summary = "Retrieves the order history of a customer.", description = "Retrieves the order history of a customer. The response may display the results across multiple pages, when applicable.")
    @ApiBaseSiteIdAndUserIdParam
    public List<OrderWsDTO> getUserOrderHistory(
            @Parameter(description = "Filters orders with specified status(es). For example, statuses = CANCELLED returns CANCELLED orders. To filter multiple statues, separate statues with \",\" and all orders with these statuses will be returned. "
                    + "For example, statuses = CANCELLED,COMPLETED,READY returns all orders with statuses CANCELLED,COMPLETED, and READY.\n"
                    + "Options available are:\nCANCELLING,\nCHECKED_VALID,\nCREATED,\nOPEN,\nREADY,\n"
                    + "CHECKED_INVALID,\nON_HOLD,\nON_VALIDATION,\nPENDING_APPROVAL,\nSUSPENDED,\nCOMPLETED,\n"
                    + "PAYMENT_AUTHORIZED,\nPENDING_APPROVAL_FROM_MERCHANT,\nCANCELLED,\nPAYMENT_NOT_AUTHORIZED,\n"
                    + "PENDING_QUOTE,\nAPPROVED_QUOTE,\nPAYMENT_AMOUNT_RESERVED,\nPAYMENT_AMOUNT_NOT_RESERVED,\n"
                    + "REJECTED_QUOTE,\nAPPROVED,\nPAYMENT_CAPTURED,\nPAYMENT_NOT_CAPTURED,\nREJECTED,\n"
                    + "APPROVED_BY_MERCHANT,\nFRAUD_CHECKED,\nORDER_SPLIT,\nREJECTED_BY_MERCHANT,\nASSIGNED_TO_ADMIN,\n"
                    + "PROCESSING_ERROR,\nB2B_PROCESSING_ERROR,\nWAIT_FRAUD_MANUAL_CHECK,\nPAYMENT_NOT_VOIDED,\n"
                    + "TAX_NOT_VOIDED,\nTAX_NOT_COMMITTED,\nTAX_NOT_REQUOTED") @RequestParam(required = false) final String statuses,
            @Parameter(description = "Current result page. Default value is 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
            @Parameter(description = "The number of results returned per page.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
            @Parameter(description = "Sorting method applied to the return results.") @RequestParam(required = false) final String sort,
            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields, final HttpServletResponse response)
    {
        validateStatusesEnumValue(statuses);

        final OrderHistoryListWsDTO orderHistoryList = ordersHelper.searchOrderHistory(statuses, currentPage, pageSize, sort,
                addPaginationField(fields));

        setTotalCountHeader(response, orderHistoryList.getPagination());

        return getDataMapper().mapAsList(orderHistoryList.getOrders(), OrderWsDTO.class, fields);
    }
}
