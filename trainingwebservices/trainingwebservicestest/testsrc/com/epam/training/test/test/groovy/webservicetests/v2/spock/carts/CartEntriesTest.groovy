/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.epam.training.test.test.groovy.webservicetests.v2.spock.carts

import de.hybris.bootstrap.annotations.ManualTest
import spock.lang.Unroll

import static groovyx.net.http.ContentType.*
import static org.apache.http.HttpStatus.*

@Unroll
@ManualTest
class CartEntriesTest extends AbstractCartTest {

	def "Customer adds product to cart for shipping when request: #requestFormat and response: #responseFormat"() {
		given: "a registerd and logged in customer with cart"
		def val = createAndAuthorizeCustomerWithCart(restClient, responseFormat)
		def customer = val[0]
		def cart = val[1]

		when: "customer decides to add product to cart"
		def response = restClient.post(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries',
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				contentType: responseFormat,
				requestContentType: requestFormat
		)

		then: "a new entry is added to the cart"
		with(response) {
			status == SC_OK
			data.statusCode == 'success'
			data.quantityAdded == 1;
		}

		where:
		requestFormat | responseFormat | postBody
		JSON          | JSON           | "{\"product\" : {\"code\" : \"${PRODUCT_FLEXI_TRIPOD}\"},\"quantity\" : 1}"
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><product><code>${PRODUCT_FLEXI_TRIPOD}</code></product><quantity>1</quantity></orderEntry>"
	}

	def "Customer tries to add a product to cart for shipping when request: #requestFormat and response: #responseFormat and a B2B base-site is used"() {
		given: "a registered and logged in customer with cart"
		def val = createAndAuthorizeCustomerWithCart(restClient, responseFormat, getBasePathWithB2BSite())
		def customer = val[0]
		def cart = val[1]

		when: "customer decides to add product to cart"
		def response = restClient.post(
				path: getBasePathWithB2BSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries',
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				contentType: responseFormat,
				requestContentType: requestFormat
		)

		then: "request fails because a restricted B2C API endpoint is used from a B2B base-site"
		with(response) {
			status == SC_UNAUTHORIZED
			data.errors.any { it.type == 'AccessDeniedError' }
		}

		where:
		requestFormat | responseFormat | postBody
		JSON          | JSON           | "{\"product\" : {\"code\" : \"${PRODUCT_FLEXI_TRIPOD}\"},\"quantity\" : 1}"
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><product><code>${PRODUCT_FLEXI_TRIPOD}</code></product><quantity>1</quantity></orderEntry>"
	}

	def "Customer tries to add product without required code parameter when request: #requestFormat and response: #responseFormat"() {
		given: "a registerd and logged in customer with cart"
		def val = createAndAuthorizeCustomerWithCart(restClient, responseFormat)
		def customer = val[0]
		def cart = val[1]

		when: "customer tries to add product to cart"
		def response = restClient.post(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries',
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				contentType: responseFormat,
				requestContentType: requestFormat
		)

		then: "error is returned"
		with(response) {
			status == SC_BAD_REQUEST
			data.errors[0].type == errorType
			data.errors[0].message == errorMessage
		}

		where:
		requestFormat | responseFormat | postBody                                                                                                       | errorType         | errorMessage
		JSON          | JSON           | "{\"quantity\" : 1}"                                                                                           | 'ValidationError' | 'This field is required.'
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><product></product><quantity>1</quantity></orderEntry>" | 'ValidationError' | 'This field is required.'
	}

	def "Customer tries to add product with wrong code when request: #requestFormat and response: #responseFormat"() {
		given: "a registerd and logged in customer with cart"
		def val = createAndAuthorizeCustomerWithCart(restClient, responseFormat)
		def customer = val[0]
		def cart = val[1]

		when: "customer tries to add product to cart"
		def response = restClient.post(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries',
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				contentType: responseFormat,
				requestContentType: requestFormat
		)

		then: "UnknownIdentifierError is returned"
		with(response) {
			status == SC_BAD_REQUEST
			println data;
			data.errors[0].type == 'UnknownIdentifierError'
			data.errors[0].message == "Product with code 'notExistingProduct' not found!"
		}

		where:
		requestFormat | responseFormat | postBody
		JSON          | JSON           | "{\"product\" : {\"code\" : \"notExistingProduct\"},\"quantity\" : 1}"
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><product><code>notExistingProduct</code></product><quantity>1</quantity></orderEntry>"
	}

	def "Customer tries to add product with wrong quantity when request: #requestFormat and response: #responseFormat"() {
		given: "a registerd and logged in customer with cart"
		def val = createAndAuthorizeCustomerWithCart(restClient, responseFormat)
		def customer = val[0]
		def cart = val[1]

		when: "customer tries to add product to cart"
		def response = restClient.post(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries',
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				contentType: responseFormat,
				requestContentType: requestFormat
		)

		then: "UnknownIdentifierError is returned"
		with(response) {
			status == SC_BAD_REQUEST
			println data;
			data.errors[0].type == errorType
			data.errors[0].message == errorMessage
		}

		where:
		requestFormat | responseFormat | postBody                                                                                                                                            | errorType         | errorMessage
		JSON          | JSON           | "{\"product\" : {\"code\" : \"${PRODUCT_FLEXI_TRIPOD}\"},\"quantity\" : -1}"                                                                        | 'ValidationError' | 'This field must be greater than 0.'
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><product><code>${PRODUCT_FLEXI_TRIPOD}</code></product><quantity>-1</quantity></orderEntry>" | 'ValidationError' | 'This field must be greater than 0.'
	}

	def "Customer adds product to cart for pickup in store when request: #requestFormat and response: #responseFormat"() {
		given: "a registerd and logged in customer with cart"
		def val = createAndAuthorizeCustomerWithCart(restClient, responseFormat)
		def customer = val[0]
		def cart = val[1]

		when: "customer decides to add product to cart"
		def response = restClient.post(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries',
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				contentType: responseFormat,
				requestContentType: requestFormat
		)

		then: "a new entry is added to the cart"
		with(response) {
			status == SC_OK
			data.statusCode == 'success'
			data.quantityAdded == 1;
			data.entry.product.code.toString() == PRODUCT_FLEXI_TRIPOD.toString()
			data.entry.deliveryPointOfService.name == STORE_NAKANO
		}

		where:
		requestFormat | responseFormat | postBody
		JSON          | JSON           | "{\"product\" : {\"code\" : \"${PRODUCT_FLEXI_TRIPOD}\"},\"quantity\" : 1,\"deliveryPointOfService\": {\"name\":\"${STORE_NAKANO}\"}}"
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><product><code>${PRODUCT_FLEXI_TRIPOD}</code></product><quantity>1</quantity><deliveryPointOfService><name>${STORE_NAKANO}</name></deliveryPointOfService></orderEntry>"
	}

	def "Customer adds product to cart for pickup in non-existing store when request: #requestFormat and response: #responseFormat"() {
		given: "a registerd and logged in customer with cart"
		def val = createAndAuthorizeCustomerWithCart(restClient, responseFormat)
		def customer = val[0]
		def cart = val[1]

		when: "customer decides to add product to cart for not existing store"
		def response = restClient.post(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries',
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				contentType: responseFormat,
				requestContentType: requestFormat
		)

		then: "ValidationError is returned"
		with(response) {
			status == SC_BAD_REQUEST
			data.errors[0].type == 'ValidationError'
			data.errors[0].subject == subject
			data.errors[0].subjectType == 'parameter'
			data.errors[0].reason == 'invalid'
			data.errors[0].message == message
		}

		where:
		requestFormat | responseFormat | postBody                                                                                                                                                                                                                         | subject						  | message
		JSON          | JSON           | "{\"product\" : {\"code\" : \"${PRODUCT_FLEXI_TRIPOD}\"},\"quantity\" : 1,\"deliveryPointOfService\": {\"name\":\"notExistingStore\"}}"                                                                                          | 'deliveryPointOfService.name' | "Store with the name 'notExistingStore' doesn't exist"
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><product><code>${PRODUCT_FLEXI_TRIPOD}</code></product><quantity>1</quantity><deliveryPointOfService><name>notExistingStore</name></deliveryPointOfService></orderEntry>" | 'deliveryPointOfService.name' | "Store with the name 'notExistingStore' doesn't exist"
	}

	def "Customer changes entry from pickup to shipping while item is out of stock online when request: #requestFormat and response: #responseFormat"() {
		given: "a registerd and logged in customer with cart"
		def val = createAndAuthorizeCustomerWithCart(restClient, responseFormat)
		def customer = val[0]
		def cart = val[1]
		addProductToCartPickup(restClient, customer, cart.code, PRODUCT_M340, STORE_SHINBASHI, 2, responseFormat)

		when: "customer decides to change entry to shipping"
		def response = restClient.put(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries/0',
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				contentType: responseFormat,
				requestContentType: requestFormat
		)

		then: "this action is rejected"
		with(response) {
			status == SC_BAD_REQUEST
			data.errors[0].type == 'InsufficientStockError'
			data.errors[0].subjectType == 'entry'
			data.errors[0].subject == '0'
			data.errors[0].reason == 'noStock'
			data.errors[0].message == 'Product [' + PRODUCT_M340 + '] cannot be shipped - out of stock online'
		}

		where:
		requestFormat | responseFormat | postBody
		JSON          | JSON           | "{\"quantity\" : 1}"
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><quantity>1</quantity></orderEntry>"
	}

	def "Customer changes entry from shipping to pickup while item is out of stock in store when request: #requestFormat and response: #responseFormat"() {
		given: "a registerd and logged in customer with cart"
		def val = createAndAuthorizeCustomerWithCart(restClient, responseFormat)
		def customer = val[0]
		def cart = val[1]
		addProductToCartOnline(restClient, customer, cart.code, PRODUCT_POWER_SHOT_A480, 2, responseFormat)

		when: "customer decides to change entry to shipping"
		def response = restClient.patch(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries/0',
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				contentType: responseFormat,
				requestContentType: requestFormat
		)

		then: "this action is rejected"
		with(response) {
			status == SC_BAD_REQUEST
			data.errors[0].type == 'InsufficientStockError'
			data.errors[0].subjectType == 'entry'
			data.errors[0].subject == '0'
			data.errors[0].reason == 'noStock'
			data.errors[0].message == 'Product [' + PRODUCT_POWER_SHOT_A480 + '] is currently out of stock'
		}

		where:
		requestFormat | responseFormat | postBody
		JSON          | JSON           | "{\"quantity\" : 1,\"deliveryPointOfService\": {\"name\":\"${STORE_SHINBASHI}\"}}"
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><quantity>1</quantity><deliveryPointOfService><name>${STORE_SHINBASHI}</name></deliveryPointOfService></orderEntry>"
	}

	def "Customer adds to cart item for pickup while item is out of stock in store when request: #requestFormat and response: #responseFormat"() {
		given: "a registerd and logged in customer with cart"
		def val = createAndAuthorizeCustomerWithCart(restClient, responseFormat)
		def customer = val[0]
		def cart = val[1]

		when: "customer decides to change entry to shipping"
		def response = restClient.post(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries',
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				contentType: responseFormat,
				requestContentType: requestFormat
		)

		then: "this action is rejected"
		with(response) {
			status == SC_BAD_REQUEST
			data.errors[0].type == 'InsufficientStockError'
			data.errors[0].message == 'Product [' + PRODUCT_POWER_SHOT_A480 + '] is currently out of stock'
		}

		where:
		requestFormat | responseFormat | postBody
		JSON          | JSON           | "{\"product\" : {\"code\" : \"${PRODUCT_POWER_SHOT_A480}\"},\"quantity\" : 1,\"deliveryPointOfService\": {\"name\":\"${STORE_SHINBASHI}\"}}"
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><product><code>${PRODUCT_POWER_SHOT_A480}</code></product><quantity>1</quantity><deliveryPointOfService><name>${STORE_SHINBASHI}</name></deliveryPointOfService></orderEntry>"
	}


	def "Out of stock online does not mean out of stock in store when request: #requestFormat and response: #responseFormat"() {
		given: "a registerd and logged in customer with cart"
		def val = createAndAuthorizeCustomerWithCart(restClient, responseFormat)
		def customer = val[0]
		def cart = val[1]

		when: "customer decides to add to cart product for pickup in store and that product is out of stock online"
		def response = restClient.post(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries',
				contentType: responseFormat,
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				requestContentType: requestFormat
		)

		then: "product is correctly added to cart regardless of it's online stock"
		with(response) { status == SC_OK }

		where:
		requestFormat | responseFormat | postBody
		JSON          | JSON           | "{\"product\" : {\"code\" : \"${PRODUCT_M340}\"},\"quantity\" : 1,\"deliveryPointOfService\": {\"name\":\"${STORE_SHINBASHI}\"}}"
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><product><code>${PRODUCT_M340}</code></product><quantity>1</quantity><deliveryPointOfService><name>${STORE_SHINBASHI}</name></deliveryPointOfService></orderEntry>"
	}

	def "Customer updates his cart entry when request: #requestFormat and response: #responseFormat"() {
		given: "a registerd and logged in customer with cart and one cart entry"
		def val = createAndAuthorizeCustomerWithCart(restClient, responseFormat)
		def customer = val[0]
		def cart = val[1]
		addProductToCartOnline(restClient, customer, cart.code, PRODUCT_FLEXI_TRIPOD, 1, responseFormat)

		when: "customer decides to change his cart entry"
		def response = restClient.patch(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries/0',
				contentType: responseFormat,
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				requestContentType: requestFormat
		)

		then: "cart entry is updated"
		with(response) {
			status == SC_OK
			data.quantity == 3
			data.quantityAdded == 2
			isNotEmpty(data.entry)
			data.entry.entryNumber == 0
			data.entry.updateable == true
			data.entry.quantity == 3
			data.entry.basePrice.currencyIso == 'USD'
			data.entry.basePrice.priceType == 'BUY'
			data.entry.basePrice.value == 11.12
			data.entry.basePrice.formattedValue == '$11.12'
			data.entry.totalPrice.currencyIso == 'USD'
			data.entry.totalPrice.priceType == 'BUY'
			data.entry.totalPrice.value == 33.36
			data.entry.totalPrice.formattedValue == '$33.36'
			data.entry.product.code.toString() == PRODUCT_FLEXI_TRIPOD.toString()
		}

		where:
		requestFormat | responseFormat | postBody
		JSON          | JSON           | "{\"quantity\" : \"3\"}}"
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><quantity>3</quantity></orderEntry>"
	}

	def "Customer updates pickup store for cart entry when request: #requestFormat and response: #responseFormat"() {
		given: "a registerd and logged in customer with cart and one cart entry"
		def val = createAndAuthorizeCustomerWithCart(restClient, responseFormat)
		def customer = val[0]
		def cart = val[1]
		addProductToCartOnline(restClient, customer, cart.code, PRODUCT_FLEXI_TRIPOD, 2, responseFormat)

		when: "customer decides to change his cart entry"
		def response = restClient.patch(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries/0',
				contentType: responseFormat,
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				requestContentType: requestFormat
		)

		then: "cart entry is updated"
		with(response) {
			status == SC_OK
			isNotEmpty(data.entry)
			data.entry.entryNumber == 0
			data.entry.quantity == 2
			data.entry.product.code.toString() == PRODUCT_FLEXI_TRIPOD.toString()
			data.entry.deliveryPointOfService.name == STORE_SHINBASHI
		}

		where:
		requestFormat | responseFormat | postBody
		JSON          | JSON           | "{\"deliveryPointOfService\": {\"name\":\"${STORE_SHINBASHI}\"}}"
		XML           | XML            | "<orderEntry><deliveryPointOfService><name>${STORE_SHINBASHI}</name></deliveryPointOfService></orderEntry>"
	}

	def "Customer attempts to update non existing cart entry when request: #requestFormat and response: #responseFormat"() {
		given: "a registered and logged in customer with cart and one cart entry"
		def val = createAndAuthorizeCustomerWithCart(restClient, responseFormat)
		def customer = val[0]
		def cart = val[1]
		addProductToCartOnline(restClient, customer, cart.code, PRODUCT_FLEXI_TRIPOD, 1, responseFormat)

		when: "customer tries to change non existing cart entry"
		def response = restClient.patch(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries/1',
				contentType: responseFormat,
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				requestContentType: requestFormat
		)

		then: "CartEntryError is returned"
		with(response) {
			status == SC_BAD_REQUEST
			data.errors[0].type == 'CartEntryError'
			data.errors[0].message == "Entry not found"
			data.errors[0].subject == '1'
			data.errors[0].subjectType == 'entry'
			data.errors[0].reason == 'notFound'
		}

		where:
		requestFormat | responseFormat | postBody
		JSON          | JSON           | "{\"quantity\" : \"3\"}}"
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><quantity>3</quantity></orderEntry>"
	}


	def "Customer removes entry from his cart: #format"() {
		given: "a registerd and logged in customer with cart and two cart entries"
		def val = createAndAuthorizeCustomerWithCart(restClient, format)
		def customer = val[0]
		def cart = val[1]
		addProductToCartOnline(restClient, customer, cart.code, PRODUCT_FLEXI_TRIPOD, 1, format)
		addProductToCartOnline(restClient, customer, cart.code, PRODUCT_POWER_SHOT_A480, 1, format)

		when: "customer decides to change his cart entry"
		def response = restClient.delete(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries/0',
				contentType: format,
				requestContentType: URLENC
		)

		and: "again gets his cart"
		def response2 = restClient.get(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code,
				contentType: format,
				query: ['fields': FIELD_SET_LEVEL_FULL],
				requestContentType: URLENC
		)

		then: "cart entry is removed"
		with(response) { status == SC_OK }

		and: "old cart entry is at position 0"
		with(response2) {
			status == SC_OK
			data.entries.size() == 1
			data.entries[0].product.code.toString() == PRODUCT_POWER_SHOT_A480.toString()
			data.entries[0].entryNumber == 0
		}

		where:
		format << [XML, JSON]
	}

	def "Customer attempts to replace non existing cart entry when request: #requestFormat and response: #responseFormat"() {
		given: "a registerd and logged in customer with cart and one cart entry"
		def val = createAndAuthorizeCustomerWithCart(restClient, responseFormat)
		def customer = val[0]
		def cart = val[1]
		addProductToCartOnline(restClient, customer, cart.code, PRODUCT_FLEXI_TRIPOD, 1, responseFormat)

		when: "customer decides to change his cart entry"
		def response = restClient.put(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries/1',
				contentType: responseFormat,
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				requestContentType: requestFormat
		)

		then: "CartEntryError is returned"
		with(response) {
			status == SC_BAD_REQUEST
			data.errors[0].type == 'CartEntryError'
			data.errors[0].message == "Entry not found"
			data.errors[0].subject == '1'
			data.errors[0].subjectType == 'entry'
			data.errors[0].reason == 'notFound'
		}

		where:
		requestFormat | responseFormat | postBody
		JSON          | JSON           | "{\"quantity\" : 3}"
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><quantity>1</quantity></orderEntry>"
	}

	def "Customer tries to replace quantity in a cart entry when response: #format and a B2B base-site is used"() {
		given: "a customer with cart"
		def anonymous = ['id': 'anonymous']
		def cart = createAnonymousCart(restClient, JSON, getBasePathWithB2BSite())

		when: "customer decides to replace his cart entry"
		def response = restClient.put(
				path: getBasePathWithB2BSite() + '/users/' + anonymous.id + '/carts/' + cart.guid + '/entries/0',
				body: ['quantity': 3],
				contentType: format,
				requestContentType: JSON
		)

		then: "request fails because a restricted B2C API endpoint is used from a B2B base-site"
		with(response) {
			status == SC_UNAUTHORIZED
			data.errors.any { it.type == 'AccessDeniedError' }
		}
		where:
		format << [XML, JSON]
	}

	def "Customer replace quantity in cart entry when request: #requestFormat and response: #responseFormat"() {
		given: "a customer with cart and one cart entry"
		def anonymous = ['id': 'anonymous']
		def cart = createAnonymousCart(restClient, responseFormat)
		addProductToCartOnline(restClient, anonymous, cart.guid, PRODUCT_FLEXI_TRIPOD, 1, responseFormat)

		when: "customer decides to replace his cart entry"
		def response = restClient.put(
				path: getBasePathWithSite() + '/users/' + anonymous.id + '/carts/' + cart.guid + '/entries/0',
				contentType: responseFormat,
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				requestContentType: requestFormat
		)

		then: "cart entry is replaced"
		with(response) {
			status == SC_OK
			data.entry.product.code.toString() == PRODUCT_FLEXI_TRIPOD.toString();
			data.entry.quantity == 3
			data.quantity == 3
			data.quantityAdded == 2
		}

		where:
		requestFormat | responseFormat | postBody
		JSON          | JSON           | "{\"quantity\" : 3}"
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><quantity>3</quantity></orderEntry>"
	}

	def "Customer replace quantity and store in cart entry when request: #requestFormat and response: #responseFormat"() {
		given: "a customer with cart and one cart entry"
		def anonymous = ['id': 'anonymous']
		def cart = createAnonymousCart(restClient, responseFormat)
		addProductToCartOnline(restClient, anonymous, cart.guid, PRODUCT_FLEXI_TRIPOD, 1, responseFormat)

		when: "customer decides to replace his cart entry"
		def response = restClient.put(
				path: getBasePathWithSite() + '/users/' + anonymous.id + '/carts/' + cart.guid + '/entries/0',
				contentType: responseFormat,
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				requestContentType: requestFormat
		)

		then: "cart entry is replaced"
		with(response) {
			status == SC_OK
			data.entry.product.code.toString() == PRODUCT_FLEXI_TRIPOD.toString();
			data.entry.quantity == 3
			data.entry.deliveryPointOfService.name == STORE_NAKANO
			data.quantity == 3
			data.quantityAdded == 2
		}

		where:
		requestFormat | responseFormat | postBody
		JSON          | JSON           | "{\"product\" : {\"code\" : \"${PRODUCT_FLEXI_TRIPOD}\"},\"quantity\" : 3,\"deliveryPointOfService\": {\"name\":\"${STORE_NAKANO}\"}}"
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><product><code>${PRODUCT_FLEXI_TRIPOD}</code></product><quantity>3</quantity><deliveryPointOfService><name>${STORE_NAKANO}</name></deliveryPointOfService></orderEntry>"
	}

	def "Customer tries to replace product in cart entry when request: #requestFormat and response: #responseFormat"() {
		given: "a customer with cart and one cart entry"
		def anonymous = ['id': 'anonymous']
		def cart = createAnonymousCart(restClient, responseFormat)
		addProductToCartOnline(restClient, anonymous, cart.guid, PRODUCT_FLEXI_TRIPOD, 1, responseFormat)

		when: "customer decides to replace his cart entry"
		def response = restClient.put(
				path: getBasePathWithSite() + '/users/' + anonymous.id + '/carts/' + cart.guid + '/entries/0',
				contentType: responseFormat,
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				requestContentType: requestFormat
		)

		then: "cart entry is replaced"
		with(response) {
			status == SC_BAD_REQUEST
			data.errors[0].type == 'ValidationError'
			data.errors[0].message == "Product code from request body object doesn't match to product code from updated entry (product code cannot be changed)"
			data.errors[0].subject == 'entry'
			data.errors[0].reason == 'invalid'
		}

		where:
		requestFormat | responseFormat | postBody
		JSON          | JSON           | "{\"product\" : {\"code\" : \"${PRODUCT_EOS_40D_BODY}\"},\"quantity\" : 3}"
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><product><code>${PRODUCT_EOS_40D_BODY}</code></product><quantity>3</quantity></orderEntry>"
	}

	def "Customer tries to replace entry with not existing store name when request: #requestFormat and response: #responseFormat"() {
		given: "a registerd and logged in customer with cart"
		def val = createAndAuthorizeCustomerWithCart(restClient, responseFormat)
		def customer = val[0]
		def cart = val[1]
		addProductToCartPickup(restClient, customer, cart.code, PRODUCT_M340, STORE_SHINBASHI, 2, responseFormat)

		when: "customer decides to add product to cart for not existing store"
		def response = restClient.put(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries/0',
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				contentType: responseFormat,
				requestContentType: requestFormat
		)

		then: "ValidationError is returned"
		with(response) {
			status == SC_BAD_REQUEST
			data.errors[0].type == 'ValidationError'
			data.errors[0].subject == subject
			data.errors[0].subjectType == 'parameter'
			data.errors[0].reason == 'invalid'
			data.errors[0].message == message
		}

		where:
		requestFormat | responseFormat | postBody                                                                                                                                                                  | subject					   | message
		JSON          | JSON           | "{\"quantity\" : 2,\"deliveryPointOfService\": {\"name\":\"notExistingStore\"}}"                                                                                          | 'deliveryPointOfService.name' | "Store with the name 'notExistingStore' doesn't exist"
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><quantity>2</quantity><deliveryPointOfService><name>notExistingStore</name></deliveryPointOfService></orderEntry>" | 'deliveryPointOfService.name' | "Store with the name 'notExistingStore' doesn't exist"
	}

	def "Customer tries to replace entry without required quantity parameter when request: #requestFormat and response: #responseFormat"() {
		given: "a registerd and logged in customer with cart"
		def val = createAndAuthorizeCustomerWithCart(restClient, responseFormat)
		def customer = val[0]
		def cart = val[1]
		addProductToCartPickup(restClient, customer, cart.code, PRODUCT_M340, STORE_SHINBASHI, 2, responseFormat)

		when: "customer tries add product to cart"
		def response = restClient.put(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries/0',
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				contentType: responseFormat,
				requestContentType: requestFormat
		)

		then: "error is returned"
		with(response) {
			status == SC_BAD_REQUEST
			data.errors[0].type == errorType
			data.errors[0].message == errorMessage
		}

		where:
		requestFormat | responseFormat | postBody                                                                                                                                           | errorType         | errorMessage
		JSON          | JSON           | "{\"deliveryPointOfService\": {\"name\":\"${STORE_NAKANO}\"}}"                                                                                     | 'ValidationError' | 'This field is required.'
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><deliveryPointOfService><name>${STORE_NAKANO}</name></deliveryPointOfService></orderEntry>" | 'ValidationError' | 'This field is required.'
	}

	def "Customer tries to replace entry with wrong quantity when request: #requestFormat and response: #responseFormat"() {
		given: "a registerd and logged in customer with cart"
		def val = createAndAuthorizeCustomerWithCart(restClient, responseFormat)
		def customer = val[0]
		def cart = val[1]
		addProductToCartOnline(restClient, customer, cart.code, PRODUCT_EOS_40D_BODY, 2, responseFormat)

		when: "customer tries replace product entry with wrong quantity value"
		def response = restClient.put(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries/0',
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				contentType: responseFormat,
				requestContentType: requestFormat
		)

		then: "error is returned"
		with(response) {
			status == SC_BAD_REQUEST
			data.errors[0].type == errorType
			data.errors[0].message == errorMessage
		}

		where:
		requestFormat | responseFormat | postBody                                                                                     | errorType         | errorMessage
		JSON          | JSON           | "{\"quantity\" : -1}"                                                                        | 'ValidationError' | 'This field must be greater than 0.'
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><quantity>-1</quantity></orderEntry>" | 'ValidationError' | 'This field must be greater than 0.'
	}

	def "Customer tries to update entry with not existing store name when request: #requestFormat and response: #responseFormat"() {
		given: "a registerd and logged in customer with cart"
		def val = createAndAuthorizeCustomerWithCart(restClient, responseFormat)
		def customer = val[0]
		def cart = val[1]
		addProductToCartPickup(restClient, customer, cart.code, PRODUCT_M340, STORE_SHINBASHI, 2, responseFormat)

		when: "customer tries update product to cart for not existing store"
		def response = restClient.patch(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries/0',
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				contentType: responseFormat,
				requestContentType: requestFormat
		)

		then: "ValidationError is returned"
		with(response) {
			status == SC_BAD_REQUEST
			data.errors[0].type == 'ValidationError'
			data.errors[0].subject == subject
			data.errors[0].subjectType == 'parameter'
			data.errors[0].reason == 'invalid'
			data.errors[0].message == message
		}

		where:
		requestFormat | responseFormat | postBody                                                                                                                                                                  | subject					   | message
		JSON          | JSON           | "{\"quantity\" : 2,\"deliveryPointOfService\": {\"name\":\"notExistingStore\"}}"                                                                                          | 'deliveryPointOfService.name' | "Store with the name 'notExistingStore' doesn't exist"
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><quantity>2</quantity><deliveryPointOfService><name>notExistingStore</name></deliveryPointOfService></orderEntry>" | 'deliveryPointOfService.name' | "Store with the name 'notExistingStore' doesn't exist"
	}

	def "Customer tries to update entry with wrong quantity when request: #requestFormat and response: #responseFormat"() {
		given: "a registerd and logged in customer with cart"
		def val = createAndAuthorizeCustomerWithCart(restClient, responseFormat)
		def customer = val[0]
		def cart = val[1]
		addProductToCartOnline(restClient, customer, cart.code, PRODUCT_EOS_40D_BODY, 2, responseFormat)

		when: "customer tries replace product entry with wrong quantity value"
		def response = restClient.patch(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries/0',
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				contentType: responseFormat,
				requestContentType: requestFormat
		)

		then: "error is returned"
		with(response) {
			println data;
			status == SC_BAD_REQUEST
			data.errors[0].type == 'ValidationError'
			data.errors[0].message == 'This field must be greater than 0.'
		}

		where:
		requestFormat | responseFormat | postBody
		JSON          | JSON           | "{\"quantity\" : -1}"
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><quantity>-1</quantity></orderEntry>"
	}

	def "Customer tries to update product in cart entry when request: #requestFormat and response: #responseFormat"() {
		given: "a customer with cart and one cart entry"
		def anonymous = ['id': 'anonymous']
		def cart = createAnonymousCart(restClient, responseFormat)
		addProductToCartOnline(restClient, anonymous, cart.guid, PRODUCT_FLEXI_TRIPOD, 1, responseFormat)

		when: "customer decides to replace his cart entry"
		def response = restClient.patch(
				path: getBasePathWithSite() + '/users/' + anonymous.id + '/carts/' + cart.guid + '/entries/0',
				contentType: responseFormat,
				query: ['fields': FIELD_SET_LEVEL_FULL],
				body: postBody,
				requestContentType: requestFormat
		)

		then: "ValidationError is returned"
		with(response) {
			status == SC_BAD_REQUEST
			data.errors[0].type == 'ValidationError'
			data.errors[0].message == "Product code from request body object doesn't match to product code from updated entry (product code cannot be changed)"
			data.errors[0].subject == 'entry'
			data.errors[0].reason == 'invalid'
		}

		where:
		requestFormat | responseFormat | postBody
		JSON          | JSON           | "{\"product\" : {\"code\" : \"${PRODUCT_EOS_40D_BODY}\"}}"
		XML           | XML            | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><orderEntry><product><code>${PRODUCT_EOS_40D_BODY}</code></product></orderEntry>"
	}
}
