package com.epam.training.processes;

import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@IntegrationTest
public class DefaultAddressProcessIntegrationTest extends ServicelayerTransactionalTest {

    private static final String USER_UID = "test@test.com";

    @Resource
    private ModelService modelService;

    private CustomerModel customer;

    @Before
    public void setUp() {
        customer = modelService.create(CustomerModel.class);
        customer.setUid(USER_UID);
        customer.setName("Test Customer");
        modelService.save(customer);
    }

    private AddressModel createAndSaveAddress(boolean billing, boolean shipping) {
        AddressModel address = modelService.create(AddressModel.class);
        address.setOwner(customer);
        address.setStreetname("Test Street");
        address.setTown("Test Town");
        address.setBillingAddress(billing);
        address.setShippingAddress(shipping);
        modelService.save(address);
        return address;
    }

    private void waitForAddressProcessing(AddressModel address) throws InterruptedException {
        // Даем время на запуск процесса
        Thread.sleep(1000);

        // Обновляем модель customer
        modelService.refresh(customer);
    }

    @Test
    public void shouldSetDefaultBillingAddress() throws InterruptedException {
        // given
        AddressModel address = createAndSaveAddress(true, false);

        // when
        waitForAddressProcessing(address);

        // then
        assertEquals("Should set billing address as default",
                address, customer.getDefaultPaymentAddress());
    }

    @Test
    public void shouldSetDefaultShipmentAddress() throws InterruptedException {
        // given
        AddressModel address = createAndSaveAddress(false, true);

        // when
        waitForAddressProcessing(address);

        // then
        assertEquals("Should set shipment address as default",
                address, customer.getDefaultShipmentAddress());
    }

    @Test
    public void shouldHandleBothBillingAndShipmentAddress() throws InterruptedException {
        // given
        AddressModel address = createAndSaveAddress(true, true);

        // when
        waitForAddressProcessing(address);

        // then
        assertEquals("Should set billing address as default",
                address, customer.getDefaultPaymentAddress());
        assertEquals("Should set shipment address as default",
                address, customer.getDefaultShipmentAddress());
    }

    @Test
    public void shouldNotChangeAddressWhenNotBillingOrShipping() throws InterruptedException {
        // given
        AddressModel address = createAndSaveAddress(false, false);

        // when
        waitForAddressProcessing(address);

        // then
        assertNull("Should not set default payment address",
                customer.getDefaultPaymentAddress());
        assertNull("Should not set default shipment address",
                customer.getDefaultShipmentAddress());
    }

    @Test
    public void shouldUpdateDefaultAddress() throws InterruptedException {
        // given
        AddressModel firstAddress = createAndSaveAddress(true, false);
        waitForAddressProcessing(firstAddress);

        // when
        AddressModel secondAddress = createAndSaveAddress(true, false);
        waitForAddressProcessing(secondAddress);

        // then
        assertEquals("Should update default payment address",
                secondAddress, customer.getDefaultPaymentAddress());
    }

    @After
    public void tearDown() {
        if (customer != null) {
            modelService.remove(customer);
        }
    }
}