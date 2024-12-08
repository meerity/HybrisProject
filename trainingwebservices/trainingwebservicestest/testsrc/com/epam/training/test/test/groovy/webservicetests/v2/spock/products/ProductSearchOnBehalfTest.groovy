/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.epam.training.test.test.groovy.webservicetests.v2.spock.products


import static groovyx.net.http.ContentType.*
import static org.apache.http.HttpStatus.*

import de.hybris.bootstrap.annotations.ManualTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.AbstractSpockFlowTest

import groovyx.net.http.HttpResponseDecorator
import spock.lang.Unroll

@ManualTest
@Unroll
class ProductSearchOnBehalfTest extends AbstractSpockFlowTest {

	static final ALL_PRODUCTS_PAGE_SIZE = 50
	static final CATEGORY_QUERY = ':relevance:allCategories:578'
	static final SECURE_BASE_SITE = "/wsSecureTest"

	def "Customer manager searches for products: #format"() {

		authorizeCustomerManager(restClient)
		when: "customer manager searches products for specified category"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + '/products/search',
				contentType: format,
				query: [
					'pageSize': ALL_PRODUCTS_PAGE_SIZE,
					'fields'  : 'products,sorts,pagination',
					'query'   : CATEGORY_QUERY
				],
				requestContentType: URLENC
		)

		then: "he gets an ambiguous identifier error"
		with(response) {
			status == SC_BAD_REQUEST
			data.errors[0].type == 'AmbiguousIdentifierError'
			data.errors[0].message == 'The application has encountered an error'
		}

		where:
		format << [XML, JSON]
	}

	def "Customer manager searches for products on behalf of other user: #format"() {

		authorizeTrustedClient(restClient)
		def customer = registerCustomer(restClient, format)
		authorizeCustomerManager(restClient)

		when: "customer manager searches products for specified category on behalf of other user"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + '/products/search',
				contentType: format,
				query: [
					'pageSize': ALL_PRODUCTS_PAGE_SIZE,
					'fields'  : 'products,sorts,pagination',
					'query'   : CATEGORY_QUERY
				],
				headers: [
					'sap-commerce-cloud-user-id': customer.id
				],
				requestContentType: URLENC
		)

		then: "he gets all the requested fields"
		with(response) {
			status == SC_OK
			data.products.size() > 0
			data.products.eachWithIndex { product, index ->
				println "${index + 1}. Product code: ${product.code}"
			}
			data.sorts.size() > 0
			data.pagination
			data.pagination.currentPage == 0
			data.pagination.pageSize == ALL_PRODUCTS_PAGE_SIZE
			data.pagination.totalResults.toInteger() > 0
			data.pagination.totalPages.toInteger() > 0
			
			response.containsHeader(HEADER_TOTAL_COUNT)
			response.getFirstHeader(HEADER_TOTAL_COUNT).getValue().toInteger() > 0
		}

		where:
		format << [XML, JSON]
	}

	def "Customer manager searches for products on behalf of invalid users: #format"() {

		authorizeCustomerManager(restClient)
		when: "customer manager searches products for specified category on behalf of invalid user"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + '/products/search',
				contentType: format,
				query: [
					'pageSize': ALL_PRODUCTS_PAGE_SIZE,
					'fields'  : 'products,sorts,pagination',
					'query'   : CATEGORY_QUERY
				],
				headers: [
					'sap-commerce-cloud-user-id': userId
				],
				requestContentType: URLENC
		)

		then: "he gets an ambiguous identifier error"
		with(response) {
			status == SC_BAD_REQUEST
			data.errors[0].type == 'UnknownIdentifierError'
			data.errors[0].message == errorMessage
		}

		where:
		format | userId             | errorMessage
		XML    | 'doesnotexist'     | "Cannot find user with propertyValue 'doesnotexist'"
		JSON   | 'doesnotexist'     | "Cannot find user with propertyValue 'doesnotexist'"
		XML    | 'has blanks'       | "Cannot find user with propertyValue 'has blanks'"
		JSON   | 'has blanks'       | "Cannot find user with propertyValue 'has blanks'"
		XML    | 'has/slashes'      | "Cannot find user with propertyValue 'has/slashes'"
		JSON   | 'has/slashes'      | "Cannot find user with propertyValue 'has/slashes'"
		XML    | 'has\\backslashes' | "Cannot find user with propertyValue 'has\\backslashes'"
		JSON   | 'has\\backslashes' | "Cannot find user with propertyValue 'has\\backslashes'"
		XML    | 'has\nnewline'     | "Cannot find user with propertyValue 'has newline'"
		JSON   | 'has\nnewline'     | "Cannot find user with propertyValue 'has newline'"
	}

	def "Customer manager searches for products on behalf of other user when requiresAuthentication true: #format"() {

		given: "Customer manager and secure base site with requiresAuthentication=true"
		authorizeTrustedClient(restClient)
		def customer = registerCustomer(restClient, format)
		authorizeCustomerManager(restClient)

		when: "customer manager searches products for specified category on behalf of other user"
		HttpResponseDecorator response = restClient.get(
				path: getBasePath() + SECURE_BASE_SITE + '/products/search',
				contentType: format,
				query: [
						'pageSize': ALL_PRODUCTS_PAGE_SIZE,
						'fields'  : 'products,sorts,pagination',
						'query'   : CATEGORY_QUERY
				],
				headers: [
						'sap-commerce-cloud-user-id': customer.id
				],
				requestContentType: URLENC
		)

		then: "he gets all the requested fields"
		with(response) {
			status == SC_OK
			data.products.size() > 0
			data.products.eachWithIndex { product, index ->
				println "${index + 1}. Product code: ${product.code}"
			}
			data.sorts.size() > 0
			data.pagination
			data.pagination.currentPage == 0
			data.pagination.pageSize == ALL_PRODUCTS_PAGE_SIZE
			data.pagination.totalResults.toInteger() > 0
			data.pagination.totalPages.toInteger() > 0

			response.containsHeader(HEADER_TOTAL_COUNT)
			response.getFirstHeader(HEADER_TOTAL_COUNT).getValue().toInteger() > 0
		}

		where:
		format << [XML, JSON]
	}

	def "Customer manager searches for products when requiresAuthentication true: #format"() {

		authorizeCustomerManager(restClient)
		when: "customer manager searches products for specified category"
		HttpResponseDecorator response = restClient.get(
				path: getBasePath() + SECURE_BASE_SITE + '/products/search',
				contentType: format,
				query: [
						'pageSize': ALL_PRODUCTS_PAGE_SIZE,
						'fields'  : 'products,sorts,pagination',
						'query'   : CATEGORY_QUERY
				],
				requestContentType: URLENC
		)

		then: "he gets an ambiguous identifier error"
		with(response) {
			status == SC_BAD_REQUEST
			data.errors[0].type == 'AmbiguousIdentifierError'
			data.errors[0].message == 'The application has encountered an error'
		}

		where:
		format << [XML, JSON]
	}
}
