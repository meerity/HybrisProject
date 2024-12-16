package com.epam.training.core.services;

import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.order.impl.DefaultOrderService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import org.apache.log4j.Logger;

import java.util.List;

public class DefaultOrderCancellationService extends DefaultOrderService {

    private static final Logger LOG = Logger.getLogger(DefaultOrderCancellationService.class);

    private ModelService modelService;
    private UserService userService;
    private BaseStoreService baseStoreService;
    private CustomerAccountService customerAccountService;

    public void cancelAllUnshippedOrders() {
        final OrderStatus[] statuses = new OrderStatus[]{OrderStatus.CREATED, OrderStatus.CHECKED_VALID, OrderStatus.ON_VALIDATION,
                OrderStatus.PAYMENT_AUTHORIZED, OrderStatus.PAYMENT_AMOUNT_RESERVED,
                OrderStatus.FRAUD_CHECKED, OrderStatus.WAIT_FRAUD_MANUAL_CHECK};

        LOG.info("Cancelling unshipped orders...");
        CustomerModel currentCustomer = (CustomerModel) userService.getCurrentUser();
        BaseStoreModel currentBaseStore = baseStoreService.getCurrentBaseStore();
        List<OrderModel> unshippedOrders = customerAccountService.getOrderList(currentCustomer, currentBaseStore, statuses);
        LOG.debug("Collected unshipped orders: " + unshippedOrders.size() + " instances");

        for (OrderModel unshippedOrder : unshippedOrders) {
            cancelOrder(unshippedOrder);
        }
        LOG.info("Cancelled " + unshippedOrders.size() + " orders");
    }

    protected void cancelOrder(OrderModel order) {
        order.setStatus(OrderStatus.CANCELLED);
        modelService.save(order);
    }

    @Override
    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setBaseStoreService(BaseStoreService baseStoreService) {
        this.baseStoreService = baseStoreService;
    }

    public void setCustomerAccountService(CustomerAccountService customerAccountService) {
        this.customerAccountService = customerAccountService;
    }
}
