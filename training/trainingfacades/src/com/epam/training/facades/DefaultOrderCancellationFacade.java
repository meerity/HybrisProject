package com.epam.training.facades;

import com.epam.training.core.services.DefaultOrderCancellationService;
import de.hybris.platform.commercefacades.order.impl.DefaultOrderFacade;

public class DefaultOrderCancellationFacade extends DefaultOrderFacade {

    DefaultOrderCancellationService defaultOrderCancellationService;

    public void cancelAllUnshippedOrders()
    {
        defaultOrderCancellationService.cancelAllUnshippedOrders();
    }

    public void setDefaultOrderCancellationService(DefaultOrderCancellationService defaultOrderCancellationService) {
        this.defaultOrderCancellationService = defaultOrderCancellationService;
    }
}
