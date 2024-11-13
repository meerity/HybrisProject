package com.epam.training.interceptors;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;

public class ProductRatingValidateInterceptor implements ValidateInterceptor<ProductModel> {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    @Override
    public void onValidate(ProductModel productModel, InterceptorContext interceptorContext) throws InterceptorException {
        final Integer rating = productModel.getRating();

        if (rating == null){
            return;
        }

        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new InterceptorException("Rating score must be between " + MIN_RATING + " and " + MAX_RATING);
        }
    }
}
