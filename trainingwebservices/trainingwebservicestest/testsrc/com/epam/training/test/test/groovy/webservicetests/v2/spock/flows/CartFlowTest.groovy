/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.epam.training.test.test.groovy.webservicetests.v2.spock.flows

import de.hybris.bootstrap.annotations.ManualTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.AbstractSpockFlowTest
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

import static groovyx.net.http.ContentType.*
import static org.apache.http.HttpStatus.SC_CREATED
import static org.apache.http.HttpStatus.SC_OK

@ManualTest
@Unroll
class CartFlowTest extends AbstractSpockFlowTest {

	def "Cart flow : #format"() {
		given: "a trusted client"
		authorizeTrustedClient(restClient)

		and: "a new customer"
		def customer = registerCustomer(restClient, format)

		and: "a new address"
		def address = createAddress(restClient, customer, format);

		expect: "create a new cart"
		def cart = returningWith(restClient.post(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts',
				body: [
						'code': '3429337'
				],
				contentType: format,
				requestContentType: URLENC), {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
		}
		).data

		and: "add a product to the cart"
		with(restClient.post(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries',
				body: ['product': ['code': '3429337']],
				contentType: format,
				requestContentType: JSON)) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
			data.quantityAdded == 1
			isNotEmpty(data.entry)
			data.entry.entryNumber == 0
		}

		and: "add another product with quantity to the cart"
		with(restClient.post(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries',
				body: [
						'product' : ['code': '1934795'],
						'quantity': 2
				],
				contentType: format,
				requestContentType: JSON)
		) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
			data.quantityAdded == 2
			isNotEmpty(data.entry)
			data.entry.entryNumber == 1
		}

		and: "get cart with 2 entries"
		with(restClient.get(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code,
				query: ['fields': 'DEFAULT,totalUnitCount'],
				contentType: format)
		) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
			data.totalItems == 2
			data.totalUnitCount == 3
			isNotEmpty(data.totalPrice)
			data.totalPrice.value == 234.8
		}

		and: "update quantity of first product"
		with(restClient.patch(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries/0',
				body: ['quantity': 3],
				contentType: format,
				requestContentType: JSON)
		) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
			data.quantityAdded == 2
			data.quantity == 3
		}

		and: "get cart again"
		with(restClient.get(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code,
				query: ['fields': 'DEFAULT,totalUnitCount'],
				contentType: format)
		) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
			data.totalItems == 2
			data.totalUnitCount == 5
			isNotEmpty(data.totalPrice)
			data.totalPrice.value == 257.04
		}

		and: "remove first product"
		with(restClient.delete(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/entries/0',
				contentType: format)
		) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
		}

		and: "get cart with 1 entry"
		with(restClient.get(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code,
				query: ['fields': 'DEFAULT,totalUnitCount'],
				contentType: format)
		) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
			data.totalItems == 1
			data.totalUnitCount == 2
			isNotEmpty(data.totalPrice)
			data.totalPrice.value == 223.68
		}

		and: "authorize the customer"
		authorizeCustomer(restClient, customer)

		and: "set the delivery address"
		with(restClient.put(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/addresses/delivery',
				body: [
						'addressId': address.id,
				],
				contentType: format,
				requestContentType: URLENC)
		) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
		}

		and: "remove the delivery address"
		with(restClient.delete(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/addresses/delivery',
				contentType: format)
		) { status == SC_OK }

		and: "get cart again"
		with(restClient.get(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code,
				query: ['fields': 'DEFAULT,totalUnitCount'],
				contentType: format)
		) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
			data.totalItems == 1
			data.totalUnitCount == 2
			data.totalPrice.value == 223.68
			!isNotEmpty(data.deliveryAddress)
		}

		and: "check deliverymodes (if there is no delivery address, there should be no deliverymodes)"
		with(restClient.get(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/deliverymodes',
				contentType: format)
		) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
			!isNotEmpty(data.deliveryModes)
		}

		and: "set the delivery address again"
		with(restClient.put(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/addresses/delivery',
				body: [
						'addressId': address.id,
				],
				contentType: format,
				requestContentType: URLENC)
		) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
		}

		and: "get cart again and check if the delivery address is in the response"
		with(restClient.get(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code,
				query: ['fields': 'DEFAULT,totalUnitCount,deliveryAddress'],
				contentType: format)
		) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
			data.totalItems == 1
			data.totalUnitCount == 2
			data.totalPrice.value == 223.68
			isNotEmpty(data.deliveryAddress)
		}

		and: "get the available delivery modes"
		with(restClient.get(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/deliverymodes',
				contentType: format)
		) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
			isNotEmpty(data.deliveryModes)
			data.deliveryModes.collect { (String) it.code }.containsAll([
					'standard-gross',
					'premium-gross'
			])
		}

		and: "set the delivery mode"
		with(restClient.put(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/deliverymode',
				body: [
						'deliveryModeId': 'standard-gross',
				],
				contentType: format,
				requestContentType: URLENC)
		) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
		}

		and: "remove the delivery mode"
		with(restClient.delete(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/deliverymode',
				contentType: format)
		) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
		}

		and: "set the delivery mode again"
		with(restClient.put(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/deliverymode',
				body: [
						'deliveryModeId': 'standard-gross',
				],
				contentType: format,
				requestContentType: URLENC)) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
		}

		and: "create payment details"
		def paymentDetails = returningWith(restClient.post(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/paymentdetails',
				body: [
						'accountHolderName' : 'Sven Haiges',
						'cardNumber'        : '4111111111111111',
						'cardType'          : ['code': 'visa'],
						'expiryMonth'       : '01',
						'expiryYear'        : '2013',
						'saved'             : true,
						'defaultPaymentInfo': true,
						'billingAddress'    : [
								'titleCode' : 'mr',
								'firstName' : 'sven',
								'lastName'  : 'haiges',
								'line1'     : 'test1',
								'line2'     : 'test2',
								'postalCode': '12345',
								'town'      : 'somecity',
								'country'   : ['isocode': 'US'],
								'region'    : ['isocode': 'US-NY']
						]
				],
				contentType: format,
				requestContentType: JSON), {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_CREATED
			isNotEmpty(data.id)
			data.accountHolderName == 'Sven Haiges'
			isNotEmpty(data.cardType)
			data.cardType.code == 'visa'
			data.cardType.name == 'Visa'
		}).data

		and: "get all payment details of current customer"
		with(restClient.get(
				path: getBasePathWithSite() + '/users/' + customer.id + '/paymentdetails',
				contentType: format)) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
			isNotEmpty(data.payments)
			data.payments.size() == 1
		}

		and: "set the payment details"
		with(restClient.put(
				path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/paymentdetails',
				body: [
						'paymentDetailsId': paymentDetails.id,
				],
				contentType: format,
				requestContentType: URLENC)) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
		}

		and: "place order"
		def order = returningWith(restClient.post(
				path: getBasePathWithSite() + '/users/' + customer.id + '/orders',
				body: [
						'cartId'      : cart.code,
						'securityCode': '123'
				],
				contentType: format,
				requestContentType: URLENC), {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			if (isEmpty(data.code)) {
				println("[" + System.currentTimeMillis() + "]. Data object after placing order and no code returned: " + data.dump())
			}
			status == SC_OK
			isNotEmpty(data.code)
		}).data

		and: "get all the orders"
		def conditions = new PollingConditions(timeout: 10, initialDelay: 1.5, factor: 1.25)
		conditions.eventually {
			with(restClient.get(
					path: getBasePathWithSite() + '/users/' + customer.id + '/orders',
					contentType: format)) {
				if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
				if (isEmpty(data.orders)) {
					println("[" + System.currentTimeMillis() + "]. Content of data, when orders array is empty: " + data.dump())
				}
				assert status == SC_OK
				assert isNotEmpty(data.orders)
				assert data.orders.size() == 1
				assert data.orders[0].code == order.code
			}
		}

		and: "get the specific order"
		with(restClient.get(
				path: getBasePathWithSite() + '/users/' + customer.id + '/orders/' + order.code,
				contentType: format)) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
			data.code == order.code
			data.totalItems == 1
			data.totalPrice.value == 232.67 // changed, due to delivery cost +8.99
			isNotEmpty(data.deliveryMode)
			data.deliveryMode.code == 'standard-gross'
		}

		where:
		format << [JSON, XML]
	}
}
