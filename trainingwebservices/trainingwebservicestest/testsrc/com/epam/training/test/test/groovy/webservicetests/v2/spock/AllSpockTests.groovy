/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.epam.training.test.test.groovy.webservicetests.v2.spock

import de.hybris.bootstrap.annotations.IntegrationTest
import com.epam.training.test.setup.TestSetupUtils
import com.epam.training.test.test.groovy.webservicetests.v2.spock.access.AccessRightsTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.access.OAuth2Test
import com.epam.training.test.test.groovy.webservicetests.v2.spock.address.AddressTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.basesites.BaseSitesTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.basestores.BaseStoresTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.carts.*
import com.epam.training.test.test.groovy.webservicetests.v2.spock.catalogs.CatalogsResourceTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.consents.ConsentResourcesTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.countries.CountryResourcesTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.customergroups.CustomerGroupsTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.errors.ErrorTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.export.ExportTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.filters.CartMatchingFilterTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.filters.UserMatchingFilterTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.flows.AddressBookFlow
import com.epam.training.test.test.groovy.webservicetests.v2.spock.flows.CartFlowTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.forgottenpasswords.ForgottenPasswordsTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.futurestocks.FutureStocksTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.general.CacheTests
import com.epam.training.test.test.groovy.webservicetests.v2.spock.general.HeaderTests
import com.epam.training.test.test.groovy.webservicetests.v2.spock.general.StateTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.misc.*
import com.epam.training.test.test.groovy.webservicetests.v2.spock.orders.OrderReturnsTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.orders.OrdersTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.paymentdetails.PaymentsTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.paymentmodes.PaymentModesTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.products.ProductResourceTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.products.ProductSearchOnBehalfTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.products.ProductsStockTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.promotions.PromotionsTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.stores.StoresTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.users.LoginNotificationTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.users.UserAccountTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.users.UserOrdersTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.users.UsersResourceTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.invoices.UserOrderInvoicesTest

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.slf4j.LoggerFactory

@RunWith(Suite.class)
@Suite.SuiteClasses([
	AccessRightsTest, OAuth2Test, StateTest, CartDeliveryTest, CartMergeTest, CartEntriesTest, CartEntryGroupsTest, CartPromotionsTest,
	CartResourceTest, CartValidationTest, CartVouchersTest, GuestsTest, OrderPlacementTest, CatalogsResourceTest, CustomerGroupsTest, ErrorTest, ExportTest,
	AddressBookFlow, CartFlowTest, CardTypesTest, CurrenciesTest, DeliveryCountriesTest, LanguagesTest, LocalizationRequestTest, TitlesTest,
	OrdersTest, ProductResourceTest, ProductSearchOnBehalfTest, ProductsStockTest, PromotionsTest, SavedCartTest, SavedCartFullScenarioTest, StoresTest, UserAccountTest,
	AddressTest, UserOrdersTest, PaymentsTest, PaymentModesTest, UsersResourceTest, CartMatchingFilterTest, UserMatchingFilterTest, HeaderTests,
	ConsentResourcesTest, CountryResourcesTest, BaseStoresTest, BaseSitesTest, OrderReturnsTest, LoginNotificationTest,
	CacheTests, RequestPathSuffixMatchingTest, FutureStocksTest, ForgottenPasswordsTest, CartRetrievalDateTest,UserOrderInvoicesTest])

@IntegrationTest
class AllSpockTests {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AllSpockTests.class)

	@BeforeClass
	public static void setUpClass() {
		TestSetupUtils.loadData();
		TestSetupUtils.startServer();
	}

	@AfterClass
	public static void tearDown() {
		TestSetupUtils.stopServer();
		TestSetupUtils.cleanData();
	}

	@Test
	public static void testing() {
		//dummy test class
	}
}
