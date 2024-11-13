package com.epam.training.interceptors;

import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.ServicelayerTest;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.fail;

@IntegrationTest
public class ProductRatingValidationInterceptorIntegrationTest extends ServicelayerTest {

    @Resource
    private ModelService modelService;

    private ProductModel product;

    @Before
    public void setUp() {
        CatalogModel catalog = modelService.create(CatalogModel.class);
        catalog.setId("testCatalog");

        CatalogVersionModel catalogVersion = modelService.create(CatalogVersionModel.class);
        catalogVersion.setCatalog(catalog);
        catalogVersion.setVersion("Online");

        modelService.save(catalog);
        modelService.save(catalogVersion);

        product = modelService.create(ProductModel.class);
        product.setCode("testProduct");
        product.setCatalogVersion(catalogVersion);
    }

    @Test
    public void shouldSaveProductWithValidRating() {
        // given
        product.setRating(3);

        // when
        modelService.save(product);

        // then - should pass without exceptions
    }

    @Test
    public void shouldNotSaveProductWithZeroRating() {
        // given
        product.setRating(0);
        try {
            // when
            modelService.save(product);

            //then
            fail("ModelSavingException to be thrown");

        } catch (ModelSavingException e) {
            //success!
        } catch (Exception e){
            fail("Caught exception: " + e);
        }
    }

    @Test
    public void shouldNotSaveProductWithNegativeRating() {
        // given
        product.setRating(-1);
        try {
            // when
            modelService.save(product);

            //then
            fail("ModelSavingException to be thrown");

        } catch (ModelSavingException e) {
            //success!
        } catch (Exception e){
            fail("Caught exception: " + e);
        }
    }

    @Test
    public void shouldNotSaveProductWithRatingGreaterThanFive() {
        // given
        product.setRating(6);
        try {
            // when
            modelService.save(product);

            //then
            fail("ModelSavingException to be thrown");

        } catch (ModelSavingException e) {
            //success!
        } catch (Exception e){
            fail("Caught exception: " + e);
        }
    }

    @Test
    public void shouldSaveProductWithRatingOne() {
        // given
        product.setRating(1);

        // when
        modelService.save(product);

        // then - should pass without exceptions
    }

    @Test
    public void shouldSaveProductWithRatingFive() {
        // given
        product.setRating(5);

        // when
        modelService.save(product);

        // then - should pass without exceptions
    }
}
