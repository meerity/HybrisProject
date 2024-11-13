package com.epam.training.interceptors;

import com.epam.training.model.CouponModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.RemoveInterceptor;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Collection;

public class ProductCouponsRemoveInterceptor implements RemoveInterceptor<ProductModel> {

    private ModelService modelService;

    @Override
    public void onRemove(ProductModel productModel, InterceptorContext interceptorContext) {
        final Collection<CouponModel> coupons = productModel.getCoupons();
        if (coupons != null && !coupons.isEmpty()) {
            modelService.removeAll(coupons);
        }
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }
}
