package com.epam.training.handlers;

import com.epam.training.model.ProductBundleModel;
import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.ServicelayerTest;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

@IntegrationTest
public class ProductPriceSumAttributeHandlerIntegrationTest extends ServicelayerTest {

    @Resource
    private ModelService modelService;

    @Test
    public void testGetProductPriceSumNoProducts() {
        //Given
        final ProductBundleModel model = modelService.create(ProductBundleModel.class);
        model.setProducts(null);

        final ProductBundleModel model1 = modelService.create(ProductBundleModel.class);
        model1.setProducts(new HashSet<>());

        //When
        Double result = model.getProductPriceSum();
        Double result1 = model1.getProductPriceSum();

        //Then
        Assert.assertEquals("When products set is null, result must be 0", result, Double.valueOf(0.0));
        Assert.assertEquals("When bundle is empty, price sum must be 0", result1, Double.valueOf(0.0));
    }

    @Test
    public void testGetProductPriceSumWithSomeProducts() {
        //Given
        final ProductBundleModel model = modelService.create(ProductBundleModel.class);

        ProductModel productModel = new ProductModel();
        productModel.setPriceQuantity(100.0);
        ProductModel productModel1 = new ProductModel();
        productModel1.setPriceQuantity(200.50);

        Set<ProductModel> products = new HashSet<>();
        products.add(productModel);
        products.add(productModel1);
        model.setProducts(products);

        //When
        Double result = model.getProductPriceSum();

        //Then
        Assert.assertNotNull(result);
        Assert.assertEquals(result, Double.valueOf(300.50));

    }
}
