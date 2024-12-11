package com.epam.training.controllers;

import com.epam.training.core.v2.controller.BaseCommerceController;
import de.hybris.platform.commercefacades.order.data.OrderHistoriesData;
import de.hybris.platform.commercefacades.order.data.OrderHistoryData;
import de.hybris.platform.commerceservices.request.mapping.annotation.RequestMappingOverride;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderHistoryListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderWsDTO;
import de.hybris.platform.core.enums.OrderStatus;
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
import de.hybris.platform.commercefacades.order.OrderFacade;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.epam.training.core.constants.YcommercewebservicesConstants.ENUM_VALUES_SEPARATOR;


@Controller
@RequestMapping(value = "/{baseSiteId}")
@Tag(name = "Orders")
public class OverridedOrdersController extends BaseCommerceController {

    @Resource(name = "orderFacade")
    private OrderFacade orderFacade;

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

        final OrderHistoryListWsDTO orderHistoryList = searchOrderHistory(statuses, currentPage, pageSize, sort,
                addPaginationField(fields));

        setTotalCountHeader(response, orderHistoryList.getPagination());

        return getDataMapper().mapAsList(orderHistoryList.getOrders(), OrderWsDTO.class, fields);
    }

    @Cacheable(value = "orderCache", key = "T(de.hybris.platform.commercewebservicescommons.cache.CommerceCacheKeyGenerator).generateKey(true,true,'DTO',#statuses,#currentPage,#pageSize,#sort,#fields)")
    public OrderHistoryListWsDTO searchOrderHistory(final String statuses, final int currentPage, final int pageSize,
                                                    final String sort, final String fields)
    {
        final OrderHistoriesData orderHistoriesData = searchOrderHistory(statuses, currentPage, pageSize, sort);
        return getDataMapper().map(orderHistoriesData, OrderHistoryListWsDTO.class, fields);
    }

    @Cacheable(value = "orderCache", key = "T(de.hybris.platform.commercewebservicescommons.cache.CommerceCacheKeyGenerator).generateKey(true,true,'Data',#statuses,#currentPage,#pageSize,#sort)")
    public OrderHistoriesData searchOrderHistory(final String statuses, final int currentPage, final int pageSize,
                                                 final String sort)
    {
        final PageableData pageableData = createPageableData(currentPage, pageSize, sort);

        final OrderHistoriesData orderHistoriesData;
        if (statuses != null)
        {
            final Set<OrderStatus> statusSet = extractOrderStatuses(statuses);
            orderHistoriesData = createOrderHistoriesData(
                    orderFacade.getPagedOrderHistoryForStatuses(pageableData, statusSet.toArray(new OrderStatus[statusSet.size()])));
        }
        else
        {
            orderHistoriesData = createOrderHistoriesData(orderFacade.getPagedOrderHistoryForStatuses(pageableData));
        }
        return orderHistoriesData;
    }

    protected Set<OrderStatus> extractOrderStatuses(final String statuses)
    {
        final String[] statusesStrings = statuses.split(ENUM_VALUES_SEPARATOR);

        final Set<OrderStatus> statusesEnum = new HashSet<>();
        for (final String status : statusesStrings)
        {
            statusesEnum.add(OrderStatus.valueOf(status));
        }
        return statusesEnum;
    }

    protected OrderHistoriesData createOrderHistoriesData(final SearchPageData<OrderHistoryData> result)
    {
        final OrderHistoriesData orderHistoriesData = new OrderHistoriesData();

        orderHistoriesData.setOrders(result.getResults());
        orderHistoriesData.setSorts(result.getSorts());
        orderHistoriesData.setPagination(result.getPagination());

        return orderHistoriesData;
    }

    protected PageableData createPageableData(final int currentPage, final int pageSize, final String sort)
    {
        final PageableData pageable = new PageableData();
        pageable.setCurrentPage(currentPage);
        pageable.setPageSize(pageSize);
        pageable.setSort(sort);
        return pageable;
    }
}
