package com.epam.training.interceptors;

import com.epam.training.model.CouponModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

@UnitTest
public class ProductRemoveCouponsInterceptorUnitTest {

    @InjectMocks
    private ProductCouponsRemoveInterceptor interceptor;

    @Mock
    private ModelService modelService;

    @Mock
    private ProductModel product;

    @Mock
    private InterceptorContext interceptorContext;

    @Mock
    private CouponModel coupon1;

    @Mock
    private CouponModel coupon2;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldRemoveAllCouponsWhenProductIsRemoved() {
        // given
        when(product.getCoupons()).thenReturn(Arrays.asList(coupon1, coupon2));

        // when
        interceptor.onRemove(product, interceptorContext);

        // then
        verify(modelService).removeAll(Arrays.asList(coupon1, coupon2));
    }

    @Test
    public void shouldNotRemoveAnythingWhenProductHasNoCoupons() {
        // given
        when(product.getCoupons()).thenReturn(Collections.emptyList());

        // when
        interceptor.onRemove(product, interceptorContext);

        // then
        verify(modelService, never()).removeAll(anyCollection());
    }

    @Test
    public void shouldHandleNullCouponsCollection() {
        // given
        when(product.getCoupons()).thenReturn(null);

        // when
        interceptor.onRemove(product, interceptorContext);

        // then
        verify(modelService, never()).removeAll(anyCollection());
    }
}