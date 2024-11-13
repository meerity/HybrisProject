package com.epam.training.interceptors;

import com.epam.training.model.CouponModel;
import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.ServicelayerTest;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;


import static org.junit.Assert.*;

@IntegrationTest
public class ProductRemoveCouponsInterceptorIntegrationTest extends ServicelayerTest {

    @Resource
    private ModelService modelService;

    @Resource
    private FlexibleSearchService flexibleSearchService;

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

        CouponModel coupon1 = modelService.create(CouponModel.class);
        coupon1.setInfo("COUPON1");
        coupon1.setProduct(product);

        CouponModel coupon2 = modelService.create(CouponModel.class);
        coupon2.setInfo("COUPON2");
        coupon2.setProduct(product);

        product.setCoupons(Arrays.asList(coupon1, coupon2));

        modelService.save(product);
        modelService.saveAll(coupon1, coupon2);
    }

    @Test
    public void shouldRemoveCouponsWhenProductIsRemoved() {
        // when
        modelService.remove(product);

        // then
        assertTrue(flexibleSearchService.search(
                "SELECT {pk} FROM {Coupon} WHERE {info} IN ('COUPON1', 'COUPON2')"
        ).getResult().isEmpty());
    }

    @Test
    public void shouldNotRemoveUnrelatedCoupons() {
        // given
        CouponModel unrelatedCoupon = modelService.create(CouponModel.class);
        unrelatedCoupon.setInfo("UNRELATED");
        modelService.save(unrelatedCoupon);

        // when
        modelService.remove(product);

        // then
        assertEquals(1, flexibleSearchService.search(
                "SELECT {pk} FROM {Coupon} WHERE {info}='UNRELATED'"
        ).getResult().size());

        modelService.remove(unrelatedCoupon);
    }
}