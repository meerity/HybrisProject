package com.epam.training.interceptors;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@UnitTest
public class ProductRatingValidateInterceptorUnitTest {

    private ProductRatingValidateInterceptor interceptor;

    @Mock
    private ProductModel productModel;

    @Mock
    private InterceptorContext interceptorContext;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        interceptor = new ProductRatingValidateInterceptor();
    }

    @Test
    public void shouldPassValidationWhenRatingIsValid() throws InterceptorException {
        // given
        when(productModel.getRating()).thenReturn(3);

        // when & then
        interceptor.onValidate(productModel, interceptorContext);
    }

    @Test
    public void shouldPassValidationWhenRatingIsOne() throws InterceptorException {
        // given
        when(productModel.getRating()).thenReturn(1);

        // when & then
        interceptor.onValidate(productModel, interceptorContext);
    }

    @Test
    public void shouldPassValidationWhenRatingIsFive() throws InterceptorException {
        // given
        when(productModel.getRating()).thenReturn(5);

        // when & then
        interceptor.onValidate(productModel, interceptorContext);
    }

    @Test
    public void shouldPassValidationWhenRatingIsNull() throws InterceptorException {
        // given
        when(productModel.getRating()).thenReturn(null);

        // when & then
        interceptor.onValidate(productModel, interceptorContext);
    }

    @Test
    public void shouldThrowExceptionWhenRatingIsZero() {
        // given
        when(productModel.getRating()).thenReturn(0);

        try {
            // when & then
            interceptor.onValidate(productModel, interceptorContext);
        } catch (InterceptorException e) {
            //success!
        } catch (Exception e){
            fail("Caught exception: " + e);
        }
    }

    @Test
    public void shouldThrowExceptionWhenRatingIsGreaterThanFive() {
        // given
        when(productModel.getRating()).thenReturn(6);

        try {
            // when & then
            interceptor.onValidate(productModel, interceptorContext);
        } catch (InterceptorException e) {
            //success!
        } catch (Exception e){
            fail("Caught exception: " + e);
        }
    }

    @Test
    public void shouldThrowExceptionWhenRatingIsNegative() {
        // given
        when(productModel.getRating()).thenReturn(-1);

        try {
            // when & then
            interceptor.onValidate(productModel, interceptorContext);
        } catch (InterceptorException e) {
            //success!
        } catch (Exception e){
            fail("Caught exception: " + e);
        }
    }
}
