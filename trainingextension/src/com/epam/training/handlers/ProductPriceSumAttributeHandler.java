package com.epam.training.handlers;

import com.epam.training.model.ProductBundleModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.model.attribute.AbstractDynamicAttributeHandler;

import java.util.Set;

public class ProductPriceSumAttributeHandler extends AbstractDynamicAttributeHandler<Double, ProductBundleModel> {

    @Override
    public Double get(ProductBundleModel model) {
        if (model == null) {
            return null;
        }

        final Set<ProductModel> products = model.getProducts();
        if (products == null){
            return 0.0;
        }

        return products.stream()
                .map(ProductModel::getPriceQuantity)
                .reduce(0.0, Double::sum);
    }

    @Override
    public void set(ProductBundleModel model, Double aDouble) {
        throw new UnsupportedOperationException("Not supported");
    }
}
