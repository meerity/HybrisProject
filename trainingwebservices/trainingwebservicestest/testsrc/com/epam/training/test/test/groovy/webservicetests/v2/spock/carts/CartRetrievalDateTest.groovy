/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.epam.training.test.test.groovy.webservicetests.v2.spock.carts

import static groovyx.net.http.ContentType.*
import static org.apache.http.HttpStatus.*

import de.hybris.bootstrap.annotations.ManualTest
import com.epam.training.test.setup.TestSetupUtils

import groovyx.net.http.HttpResponseDecorator
import spock.lang.Unroll

@Unroll
@ManualTest
class CartRetrievalDateTest extends AbstractCartTest {

	def "Customer sets retrieval date for the cart: #format"() {

		given: "a customer with cart that has a product, address set and requestedRetrievalDateEnabled is enabled on base store"
		def val = createAndAuthorizeCustomerWithCart(restClient, format)
		def customer = val[0]
		def cart = val[1]
		def address = createAddress(restClient, customer)
		addProductToCartOnline(restClient, customer, cart.code, PRODUCT_FLEXI_TRIPOD)
		setDeliveryAddressForCart(restClient, customer, cart.code, address.id, format)
		TestSetupUtils.updateCartRerievalDateCheckEnabled(true)

		when: "customer selects a retrieval date for cart"
		HttpResponseDecorator response = restClient.put(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/requestedretrievaldate',
				query: ['requestedRetrievalAt': '9999-12-31'],
				contentType: format,
				requestContentType: URLENC
				)
		cart = getCart(restClient, customer, cart.code, format)

		then: "his selection should be stored"
		with(response) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
		}

		with(cart) {
			cart.requestedRetrievalAt == '9999-12-31'
		}

		TestSetupUtils.updateCartRerievalDateCheckEnabled(false)

		where:
		format << [XML, JSON]
	}
}
