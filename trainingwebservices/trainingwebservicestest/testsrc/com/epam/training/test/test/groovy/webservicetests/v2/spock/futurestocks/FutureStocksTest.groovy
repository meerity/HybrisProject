/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.epam.training.test.test.groovy.webservicetests.v2.spock.futurestocks

import de.hybris.bootstrap.annotations.ManualTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.users.AbstractUserTest
import groovyx.net.http.HttpResponseDecorator
import spock.lang.Unroll

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

import static groovyx.net.http.ContentType.JSON
import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static org.apache.http.HttpStatus.SC_FORBIDDEN
import static org.apache.http.HttpStatus.SC_NOT_FOUND
import static org.apache.http.HttpStatus.SC_OK
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED

@Unroll
@ManualTest
class FutureStocksTest extends AbstractUserTest
{
	static final String SECURE_BASE_SITE = "/wsSecureTest"
	static final String USERS_PATH = "/users"
	static final String PRODUCT_CODE1 = "137220"
	static final Integer PRODUCT_CODE1_STOCK_LEVEL = 85
	static final String PRODUCT_CODE2 = "278688"
	static final Integer PRODUCT_CODE2_STOCK_LEVEL = 50
	static final String PRODUCT_CODE3 = "3557133"
	static final Integer PRODUCT_CODE3_STOCK_LEVEL = 85
	static final String MULTID_PRODUCT_CODE = "72399000_55"
	static final Integer MULTID_PRODUCT_CODE_STOCK_LEVEL = 35
	static final String PRODUCT_DATE1 = initializeProductDate(2050, Month.JUNE, 15)
	static final String PRODUCT_FORMATTED_DATE1 = initializeFormattedDate(2050, Month.JUNE, 15)
	static final String PRODUCT_DATE2 = initializeProductDate(2050, Month.MARCH, 10)
	static final String PRODUCT_FORMATTED_DATE2 = initializeFormattedDate(2050, Month.MARCH, 10)

	def "A customer as a TRUSTED_CLIENT should be able to view future product availability for a specified product"() {
		given: "a user as TRUSTED_CLIENT"
		authorizeTrustedClient(restClient)

		when: "he requests to view future product availability for a specified product"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + USERS_PATH + "/anonymous/futureStocks/" + PRODUCT_CODE1,
				contentType: JSON,
				requestContentType: JSON)

		then: "he gets the future product availability for the specified product"
		with(response) {
			status == SC_OK
			data.productCode == PRODUCT_CODE1
			data.futureStocks.size() == 1
		}
	}

	def "A registered customer should be able to view future product availability for a specified product"() {
		given: "a registered and logged in customer"
		authorizeTrustedClient(restClient)
		def customer = registerCustomer(restClient)
		authorizeCustomer(restClient, customer)

		when: "he requests to view future product availability for a specified product"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + USERS_PATH + "/current/futureStocks/" + PRODUCT_CODE1,
				contentType: JSON,
				requestContentType: JSON)

		then: "he gets the future product availability for the specified product"
		with(response) {
			status == SC_OK
			data.productCode == PRODUCT_CODE1
			data.futureStocks.size() == 1
			data.futureStocks.get(0).formattedDate == PRODUCT_FORMATTED_DATE1
			data.futureStocks.get(0).date == PRODUCT_DATE1
			data.futureStocks.get(0).stock.stockLevel == PRODUCT_CODE1_STOCK_LEVEL
			data.futureStocks.get(0).stock.isValueRounded == false
		}
	}

	def "A registered customer should be able to view empty future product availability for a specified product"() {
		given: "a registered and logged in customer"
		authorizeTrustedClient(restClient)
		def customer = registerCustomer(restClient)
		authorizeCustomer(restClient, customer)

		when: "he requests to view future product availability for a specified product"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + USERS_PATH + "/current/futureStocks/" + PRODUCT_CODE2,
				contentType: JSON,
				requestContentType: JSON)

		then: "he gets the future product availability for the specified product"
		with(response) {
			status == SC_OK
			data.productCode == PRODUCT_CODE2
			isEmpty(data.futureStocks)
		}
	}

	def "A customer manager should be able to view future product availability for a specified product"() {
		given: "a logged in customer manager"
		authorizeCustomerManager(restClient)

		when: "he requests to view future product availability for a specified product"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + USERS_PATH + "/democustomer/futureStocks/" + PRODUCT_CODE1,
				contentType: JSON,
				requestContentType: JSON)

		then: "he gets the future product availability for the specified product"
		with(response) {
			status == SC_OK
			data.productCode == PRODUCT_CODE1
			data.futureStocks.size() == 1
			data.futureStocks.get(0).formattedDate == PRODUCT_FORMATTED_DATE1
			data.futureStocks.get(0).date == PRODUCT_DATE1
			data.futureStocks.get(0).stock.stockLevel == PRODUCT_CODE1_STOCK_LEVEL
			data.futureStocks.get(0).stock.isValueRounded == false
		}
	}

	def "A customer should be able to get future product availability with fields value #fields to specify the content he wants"() {
		given: "a registered and logged in customer"
		authorizeTrustedClient(restClient)
		def customer = registerCustomer(restClient)
		authorizeCustomer(restClient, customer)

		and: "he requests to view future product availability with specified fields"
		def expectedResponse = restClient.get(
				path: getBasePathWithSite() + USERS_PATH + "/current/futureStocks/" + PRODUCT_CODE1,
				query: [
						'fields': fields
				],
				contentType: JSON,
				requestContentType: JSON)

		def expectedData = expectedResponse.data

		when: "he requests to view future product availability with given fields"
		def response = restClient.get(
				path: getBasePathWithSite() + USERS_PATH + "/current/futureStocks/" + PRODUCT_CODE1,
				query: scope,
				contentType: JSON,
				requestContentType: JSON)

		then: "future product availability with specified field is returned"
		with(response) {
			status == SC_OK
			data.equals(expectedData)
			data.futureStocks.get(0).formattedDate == PRODUCT_FORMATTED_DATE1
			data.futureStocks.get(0).date == PRODUCT_DATE1
			data.futureStocks.get(0).stock.stockLevel == PRODUCT_CODE1_STOCK_LEVEL
		}

		//when fields is empty, will use DEFAULT as default value for fields
		where:
		scope                 | fields
		["fields": "BASIC"]   | "productCode,futureStocks(stock,date,formattedDate)"
		null                  | "productCode,futureStocks(stock(DEFAULT),date,formattedDate)"
		["fields": "DEFAULT"] | "productCode,futureStocks(stock(DEFAULT),date,formattedDate)"
		["fields": "FULL"]    | "productCode,futureStocks(stock(FULL),date,formattedDate)"

	}

	def "A customer should fail to view future product availability when providing #scenario baseSite"() {
		given: "a registered and logged in customer"
		authorizeTrustedClient(restClient)
		def customer = registerCustomer(restClient)
		authorizeCustomer(restClient, customer)

		when: "he requests to view future product availability"
		def response = restClient.get(
				path: getBasePath() + baseSite + USERS_PATH + "/current/futureStocks/" + PRODUCT_CODE1,
				contentType: JSON,
				requestContentType: JSON)

		then: "fail to get future product availability"
		with(response) {
			status == statusCode
			data.errors[0].message == errorMessage
			data.errors[0].type == errorType
		}

		where:
		scenario       | baseSite                  | statusCode     | errorType              | errorMessage
		"empty"        | ""                        | SC_BAD_REQUEST | "InvalidResourceError" | "Base site users doesn't exist"
		"non existing" | "/NON_EXISTING_BASE_SITE" | SC_BAD_REQUEST | "InvalidResourceError" | "Base site NON_EXISTING_BASE_SITE doesn't exist"
	}

	def "A customer should fail to view future product availability for a specified product when #senario"() {
		given: "a registered and logged in customer"
		authorizeTrustedClient(restClient)
		def customer = registerCustomer(restClient)
		authorizeCustomer(restClient, customer)

		when: "he requests to view future product availability for a specified product"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + USERS_PATH + "/" + userId + "/futureStocks/" + productCode,
				contentType: JSON,
				requestContentType: JSON)

		then: "fail to get future product availability"
		with(response) {
			status == status
			data.errors[0].message == errorMessage
			data.errors[0].type == errorType
		}

		where:
		senario                                 | userId                    | productCode          | status         | errorType                | errorMessage
		"user id is not provided"               | ""                        | PRODUCT_CODE1        | SC_FORBIDDEN   | "ForbiddenError"         | "Access is denied"
		"non-existing user id is provided"      | "nonexistinguser"         | PRODUCT_CODE1        | SC_FORBIDDEN   | "ForbiddenError"         | "Access is denied"
		"user id does not match the user token" | CUSTOMER_MANAGER_USERNAME | PRODUCT_CODE1        | SC_FORBIDDEN   | "ForbiddenError"         | "Access is denied"
		"invalid produce code is provided"      | "current"                 | "nonexsitingproduct" | SC_BAD_REQUEST | "UnknownIdentifierError" | "Product with code 'nonexsitingproduct' not found!"
	}

	def "An anonymous user (#scenario) should not be able to view future product availability for user #userId"() {
		given: "an anonymous user"
		authorizationMethod(restClient)

		when: "he requests to get future product availability"
		def response = restClient.get(
				path: getBasePathWithSite() + USERS_PATH + "/" + userId + "/futureStocks/" + PRODUCT_CODE1,
				contentType: JSON,
				requestContentType: JSON)

		then: "fail to get future product availability"
		with(response) {
			status == status
			data.errors[0].message == errorMessage
			data.errors[0].type == errorType
		}

		where:
		scenario                              | authorizationMethod          | userId                    | status          | errorType                  | errorMessage
		"not sending any Authorization Token" | this.&removeAuthorization    | "current"                 | SC_UNAUTHORIZED | "UnauthorizedError"        | "Full authentication is required to access this resource"
		"not sending any Authorization Token" | this.&removeAuthorization    | "anonymous"               | SC_UNAUTHORIZED | "AccessDeniedError"        | "Access is denied"
		"not sending any Authorization Token" | this.&removeAuthorization    | CUSTOMER_MANAGER_USERNAME | SC_UNAUTHORIZED | "UnauthorizedError"        | "Full authentication is required to access this resource"
		"as a TRUSTED_CLIENT"                 | this.&authorizeTrustedClient | CUSTOMER_MANAGER_USERNAME | SC_BAD_REQUEST  | "AmbiguousIdentifierError" | "The application has encountered an error"
		"as a TRUSTED_CLIENT"                 | this.&authorizeTrustedClient | "current"                 | SC_BAD_REQUEST  | "UnknownIdentifierError"   | "Cannot find user with propertyValue 'current'"
	}

	def "A customer as a TRUSTED_CLIENT should be able to view future stocks for a set of products"() {
		given: "a user as TRUSTED_CLIENT"
		authorizeTrustedClient(restClient)

		when: "he requests to view future stocks for a set of products"
		def response = restClient.get(
				path: getBasePathWithSite() + USERS_PATH + "/anonymous/futureStocks/",
				query: [
						productCodes: PRODUCT_CODE1 + "," + PRODUCT_CODE3
				],
				contentType: JSON,
				requestContentType: JSON)

		then: "he gets future stock data for the set"
		with(response) {
			status == SC_OK
			data.productFutureStocks.futureStocks.size() == 2
			data.productFutureStocks.productCode.containsAll(PRODUCT_CODE1, PRODUCT_CODE3)
			assert data.productFutureStocks.any { futureStock ->
				futureStock.productCode == PRODUCT_CODE1 &&
						futureStock.futureStocks[0].date == PRODUCT_DATE1 &&
						futureStock.futureStocks[0].formattedDate == PRODUCT_FORMATTED_DATE1 &&
						futureStock.futureStocks[0].stock.stockLevel == PRODUCT_CODE1_STOCK_LEVEL
			}
			assert data.productFutureStocks.any { futureStock ->
				futureStock.productCode == PRODUCT_CODE3 &&
						futureStock.futureStocks[0].date == PRODUCT_DATE2 &&
						futureStock.futureStocks[0].formattedDate == PRODUCT_FORMATTED_DATE2 &&
						futureStock.futureStocks[0].stock.stockLevel == PRODUCT_CODE2_STOCK_LEVEL &&
						futureStock.futureStocks[1].date == PRODUCT_DATE1 &&
						futureStock.futureStocks[1].formattedDate == PRODUCT_FORMATTED_DATE1 &&
						futureStock.futureStocks[1].stock.stockLevel == PRODUCT_CODE3_STOCK_LEVEL
			}
		}
	}

	def "A registered customer should be able to view future stocks for a set of products"() {
		given: "a registered and logged in customer"
		authorizeTrustedClient(restClient)
		def customer = registerCustomer(restClient)
		authorizeCustomer(restClient, customer)

		when: "he requests to view future stocks for a set of products"
		def response = restClient.get(
				path: getBasePathWithSite() + USERS_PATH + "/current/futureStocks",
				query: [
						productCodes: PRODUCT_CODE1 + "," + PRODUCT_CODE3
				],
				contentType: JSON,
				requestContentType: JSON)

		then: "he gets future stock data for the set"
		with(response) {
			status == SC_OK
			data.productFutureStocks.futureStocks.size() == 2
			data.productFutureStocks.productCode.containsAll(PRODUCT_CODE1, PRODUCT_CODE3)
			assert data.productFutureStocks.any { futureStock ->
				futureStock.productCode == PRODUCT_CODE1 &&
						futureStock.futureStocks[0].date == PRODUCT_DATE1 &&
						futureStock.futureStocks[0].formattedDate == PRODUCT_FORMATTED_DATE1 &&
						futureStock.futureStocks[0].stock.stockLevel == PRODUCT_CODE1_STOCK_LEVEL
			}
			assert data.productFutureStocks.any { futureStock ->
				futureStock.productCode == PRODUCT_CODE3 &&
						futureStock.futureStocks[0].date == PRODUCT_DATE2 &&
						futureStock.futureStocks[0].formattedDate == PRODUCT_FORMATTED_DATE2 &&
						futureStock.futureStocks[0].stock.stockLevel == PRODUCT_CODE2_STOCK_LEVEL &&
						futureStock.futureStocks[1].date == PRODUCT_DATE1 &&
						futureStock.futureStocks[1].formattedDate == PRODUCT_FORMATTED_DATE1 &&
						futureStock.futureStocks[1].stock.stockLevel == PRODUCT_CODE3_STOCK_LEVEL
			}
		}
	}

	def "A customer manager should be able to view future stocks for a set of products"() {
		given: "a logged in customer manager"
		authorizeCustomerManager(restClient)

		when: "he requests to view future stocks for a set of products"
		def response = restClient.get(
				path: getBasePathWithSite() + USERS_PATH + "/democustomer/futureStocks",
				query: [
						productCodes: PRODUCT_CODE1 + "," + PRODUCT_CODE3
				],
				contentType: JSON,
				requestContentType: JSON)

		then: "he gets future stock data for the set"
		with(response) {
			status == SC_OK
			data.productFutureStocks.futureStocks.size() == 2
			data.productFutureStocks.productCode.containsAll(PRODUCT_CODE1, PRODUCT_CODE3)
			assert data.productFutureStocks.any { futureStock ->
				futureStock.productCode == PRODUCT_CODE1 &&
						futureStock.futureStocks[0].date == PRODUCT_DATE1 &&
						futureStock.futureStocks[0].formattedDate == PRODUCT_FORMATTED_DATE1 &&
						futureStock.futureStocks[0].stock.stockLevel == PRODUCT_CODE1_STOCK_LEVEL
			}
			assert data.productFutureStocks.any { futureStock ->
				futureStock.productCode == PRODUCT_CODE3 &&
						futureStock.futureStocks[0].date == PRODUCT_DATE2 &&
						futureStock.futureStocks[0].formattedDate == PRODUCT_FORMATTED_DATE2 &&
						futureStock.futureStocks[0].stock.stockLevel == PRODUCT_CODE2_STOCK_LEVEL &&
						futureStock.futureStocks[1].date == PRODUCT_DATE1 &&
						futureStock.futureStocks[1].formattedDate == PRODUCT_FORMATTED_DATE1 &&
						futureStock.futureStocks[1].stock.stockLevel == PRODUCT_CODE3_STOCK_LEVEL
			}
		}
	}

	def "A customer should be able to view future stocks for the same set of products with same response when specific fields parameter value as #fields"() {
		given: "a registered and logged in customer"
		authorizeTrustedClient(restClient)
		def customer = registerCustomer(restClient)
		authorizeCustomer(restClient, customer)


		def expectedResponse = restClient.get(
				path: getBasePathWithSite() + USERS_PATH + "/current/futureStocks",
				query: [
						productCodes: PRODUCT_CODE1 + "," + PRODUCT_CODE3,
						fields      : fields
				],
				contentType: JSON,
				requestContentType: JSON)
		def expectedData = expectedResponse.data

		when: "he requests to view future stocks with specific fields"
		def response = restClient.get(
				path: getBasePathWithSite() + USERS_PATH + "/current/futureStocks",
				query: [
						productCodes: PRODUCT_CODE1 + "," + PRODUCT_CODE3,
						fields      : scope
				],
				contentType: JSON,
				requestContentType: JSON)

		then: "future stocks with specific fields are returned"
		with(response) {
			status == SC_OK
			data.equals(expectedData)
			data.productFutureStocks.futureStocks.size() == 2
			data.productFutureStocks.productCode.containsAll(PRODUCT_CODE1, PRODUCT_CODE3)
			assert data.productFutureStocks.any { futureStock ->
				futureStock.productCode == PRODUCT_CODE1 &&
						futureStock.futureStocks[0].date == PRODUCT_DATE1 &&
						futureStock.futureStocks[0].formattedDate == PRODUCT_FORMATTED_DATE1 &&
						futureStock.futureStocks[0].stock.stockLevel == PRODUCT_CODE1_STOCK_LEVEL
			}
			assert data.productFutureStocks.any { futureStock ->
				futureStock.productCode == PRODUCT_CODE3 &&
						futureStock.futureStocks[0].date == PRODUCT_DATE2 &&
						futureStock.futureStocks[0].formattedDate == PRODUCT_FORMATTED_DATE2 &&
						futureStock.futureStocks[0].stock.stockLevel == PRODUCT_CODE2_STOCK_LEVEL &&
						futureStock.futureStocks[1].date == PRODUCT_DATE1 &&
						futureStock.futureStocks[1].formattedDate == PRODUCT_FORMATTED_DATE1 &&
						futureStock.futureStocks[1].stock.stockLevel == PRODUCT_CODE3_STOCK_LEVEL
			}
		}

		//when fields is empty, will use DEFAULT as default value for fields
		where:
		scope     | fields
		"BASIC"   | "productFutureStocks(productCode,futureStocks(stock,date,formattedDate))"
		"DEFAULT" | "productFutureStocks(productCode,futureStocks(stock(DEFAULT),date,formattedDate))"
		"FULL"    | "productFutureStocks(productCode,futureStocks(stock(FULL),date,formattedDate))"
		null      | "productFutureStocks(productCode,futureStocks(stock(DEFAULT),date,formattedDate))"

	}

	def "A customer should be able to get future stocks corresponding to the fields parameter he specified"() {
		given: "a registered and logged in customer"
		authorizeTrustedClient(restClient)
		def customer = registerCustomer(restClient)
		authorizeCustomer(restClient, customer)

		when: "he requests to view future stocks with specific fields"
		def response = restClient.get(
				path: getBasePathWithSite() + USERS_PATH + "/current/futureStocks",
				query: [
						productCodes: PRODUCT_CODE1 + "," + PRODUCT_CODE3,
						fields      : "productFutureStocks(futureStocks(stock))"
				],
				contentType: JSON,
				requestContentType: JSON)

		then: "future stocks with specific fields are returned"
		with(response) {
			status == SC_OK
			data.productFutureStocks[0].futureStocks[0].keySet().size() == 1
			data.productFutureStocks[0].futureStocks[0].keySet().contains("stock")
		}
	}
	
	def "A customer should get response with the same productCode appears twice if the productCode appears twice in the input "() {
		given: "a registered and logged in customer"
		authorizeTrustedClient(restClient)
		def customer = registerCustomer(restClient)
		authorizeCustomer(restClient, customer)

		when: "he requests to get future stocks for those products which feature stock is missing"
		def response = restClient.get(
				path: getBasePathWithSite() + USERS_PATH + "/current/futureStocks",
				query: [
						productCodes: PRODUCT_CODE1 + "," + PRODUCT_CODE1
				],
				contentType: JSON,
				requestContentType: JSON)

		then: "he gets the response with the same productCode appears twice"
		with(response) {
			status == SC_OK
			data.productFutureStocks.size() == 2
			data.productFutureStocks[0].productCode == PRODUCT_CODE1
			data.productFutureStocks[1].productCode == PRODUCT_CODE1
		}
	}

	def "A customer should get empty response when request to view a set of products which future stocks is missing"() {
		given: "a registered and logged in customer"
		authorizeTrustedClient(restClient)
		def customer = registerCustomer(restClient)
		authorizeCustomer(restClient, customer)

		final String PRODUCT_CODE_MISSING_FUTURE_STOCK = "1225694"

		when: "he requests to get future stocks for those products which future stock is missing"
		def response = restClient.get(
				path: getBasePathWithSite() + USERS_PATH + "/current/futureStocks",
				query: [
						productCodes: PRODUCT_CODE2 + "," + PRODUCT_CODE_MISSING_FUTURE_STOCK
				],
				contentType: JSON,
				requestContentType: JSON)

		then: "he gets empty response for future stock"
		with(response) {
			status == SC_OK
			data.productFutureStocks.futureStocks.size() == 2
			data.productFutureStocks.productCode.containsAll(PRODUCT_CODE2, PRODUCT_CODE_MISSING_FUTURE_STOCK)
			assert data.productFutureStocks.any { futureStock ->
				futureStock.productCode == PRODUCT_CODE2 && isEmpty(futureStock.futureStocks)
			}
			assert data.productFutureStocks.any { futureStock ->
				futureStock.productCode == PRODUCT_CODE_MISSING_FUTURE_STOCK && isEmpty(futureStock.futureStocks)
			}
		}
	}

	def "A customer should be able to get response including every valid products when he requests to view future stocks for a set of product and part of the products' future stocks is missing"() {
		given: "a registered and logged in customer"
		authorizeTrustedClient(restClient)
		def customer = registerCustomer(restClient)
		authorizeCustomer(restClient, customer)

		when: "he requests to get future stocks for products"
		def response = restClient.get(
				path: getBasePathWithSite() + USERS_PATH + "/current/futureStocks",
				query: [
						productCodes: PRODUCT_CODE2 + "," + PRODUCT_CODE1
				],
				contentType: JSON,
				requestContentType: JSON)

		then: "he gets response as expected"
		with(response) {
			status == SC_OK
			data.productFutureStocks.futureStocks.size() == 2
			data.productFutureStocks.productCode.containsAll(PRODUCT_CODE2, PRODUCT_CODE1)
			assert data.productFutureStocks.any { futureStock ->
				futureStock.productCode == PRODUCT_CODE2 && isEmpty(futureStock.futureStocks)
			}
			assert data.productFutureStocks.any { futureStock ->
				futureStock.productCode == PRODUCT_CODE1 &&
						futureStock.futureStocks[0].date == PRODUCT_DATE1 &&
						futureStock.futureStocks[0].formattedDate == PRODUCT_FORMATTED_DATE1 &&
						futureStock.futureStocks[0].stock.stockLevel == PRODUCT_CODE1_STOCK_LEVEL
			}
		}
	}

	def "A customer should fail to view future stocks for a set of products when userId is #senario"() {
		given: "a registered and logged in customer"
		authorizeTrustedClient(restClient)
		def customer = registerCustomer(restClient)
		authorizeCustomer(restClient, customer)

		when: "he requests to view future stocks for a set of product"
		def response = restClient.get(
				path: getBasePathWithSite() + USERS_PATH + "/" + userId + "/futureStocks",
				query: [
						productCodes: PRODUCT_CODE1 + ","  + PRODUCT_CODE3,
				],
				contentType: JSON,
				requestContentType: JSON)

		then: "fail to get future stocks"
		with(response) {
			status == SC_FORBIDDEN
			data.errors[0].message == "Access is denied"
			data.errors[0].type == "ForbiddenError"
		}

		where:
		senario        | userId
		"not provided" | ""
		"invalid"      | "invalidUserId"
	}

	def "A customer should fail to view future stocks for a set of products when productCode is #senario"() {
		given: "a registered and logged in customer"
		authorizeTrustedClient(restClient)
		def customer = registerCustomer(restClient)
		authorizeCustomer(restClient, customer)

		when: "he requests to get future stocks"
		def response = restClient.get(
				path: getBasePathWithSite() + USERS_PATH + "/current/futureStocks",
				query: [
						productCodes: productCodes
				],
				contentType: JSON,
				requestContentType: JSON)

		then: "fail to get future product availability"
		with(response) {
			status == SC_BAD_REQUEST
			data.errors[0].message == errorMessage
			data.errors[0].type == "UnknownIdentifierError"
		}

		where:
		senario       | productCodes | errorMessage
		"a empty set" | ""           | "Product with code '' not found!"
		"invalid"     | "999999"     | "Product with code '999999' not found!"
	}

	def "A customer should fail to view future stocks for a set of products when he doesn't pass any query parameter"() {
		given: "a registered and logged in customer"
		authorizeTrustedClient(restClient)
		def customer = registerCustomer(restClient)
		authorizeCustomer(restClient, customer)

		when: "he requests to get future stocks"
		def response = restClient.get(
				path: getBasePathWithSite() + USERS_PATH + "/current/futureStocks",
				contentType: JSON,
				requestContentType: JSON)

		then: "fail to get future product availability"
		with(response) {
			status == SC_BAD_REQUEST
			data.errors[0].message == "Required request parameter 'productCodes' for method parameter type String is not present"
			data.errors[0].type == "MissingServletRequestParameterError"
		}
	}

	def "A registered customer should be able to view future stocks for a set of products which includes multi-d products"() {
		given: "a registered and logged in customer"
		authorizeTrustedClient(restClient)
		def customer = registerCustomer(restClient)
		authorizeCustomer(restClient, customer)

		when: "he requests to view future stocks for a set of products"
		def response = restClient.get(
				path: getBasePathWithSite() + USERS_PATH + "/current/futureStocks",
				query: [
						productCodes: PRODUCT_CODE1 + "," + MULTID_PRODUCT_CODE
				],
				contentType: JSON,
				requestContentType: JSON)

		then: "he gets future stock data for the set"
		with(response) {
			status == SC_OK
			data.productFutureStocks.futureStocks.size() == 2
			data.productFutureStocks.productCode.containsAll(PRODUCT_CODE1, MULTID_PRODUCT_CODE)
			assert data.productFutureStocks.any { futureStock ->
				futureStock.productCode == PRODUCT_CODE1 &&
						futureStock.futureStocks[0].date == PRODUCT_DATE1 &&
						futureStock.futureStocks[0].formattedDate == PRODUCT_FORMATTED_DATE1 &&
						futureStock.futureStocks[0].stock.stockLevel == PRODUCT_CODE1_STOCK_LEVEL
			}
			assert data.productFutureStocks.any { futureStock ->
				futureStock.productCode == MULTID_PRODUCT_CODE &&
						futureStock.futureStocks[0].date == PRODUCT_DATE2 &&
						futureStock.futureStocks[0].formattedDate == PRODUCT_FORMATTED_DATE2 &&
						futureStock.futureStocks[0].stock.stockLevel == MULTID_PRODUCT_CODE_STOCK_LEVEL
			}
		}
	}

	def "A registered customer should fail to view future stocks for a set of products which includes both valid and invalid products"() {
		given: "a registered and logged in customer"
		authorizeTrustedClient(restClient)
		def customer = registerCustomer(restClient)
		authorizeCustomer(restClient, customer)

		when: "he requests to view future stocks for a set of products"
		def response = restClient.get(
				path: getBasePathWithSite() + USERS_PATH + "/current/futureStocks",
				query: [
						productCodes: PRODUCT_CODE1 + ",999999"
				],
				contentType: JSON,
				requestContentType: JSON)

		then: "he gets future stock data for the set"
		with(response) {
			status == SC_BAD_REQUEST
			data.errors[0].message == "Product with code '999999' not found!"
			data.errors[0].type == "UnknownIdentifierError"
		}
	}

	def "An anonymous as a TRUSTED_CLIENT should fail to view future product availability for a specified product when enable requiresAuthentication"() {
		given: "a user as TRUSTED_CLIENT"
		authorizeTrustedClient(restClient)

		when: "he requests to view future product availability for a specified product"
		HttpResponseDecorator response = restClient.get(
				path: getBasePath() + SECURE_BASE_SITE + USERS_PATH + "/anonymous/futureStocks/" + PRODUCT_CODE1,
				contentType: JSON,
				requestContentType: JSON)

		then: "he gets the future product availability for the specified product"
		with(response) {
			status == SC_NOT_FOUND
		}
	}

	def "An anonymous as a TRUSTED_CLIENT should fail able to view future stocks for a set of products when enable requiresAuthentication"() {
		given: "a user as TRUSTED_CLIENT"
		authorizeTrustedClient(restClient)

		when: "he requests to view future stocks for a set of products"
		def response = restClient.get(
				path: getBasePath() + SECURE_BASE_SITE + USERS_PATH + "/anonymous/futureStocks/",
				query: [
						productCodes: PRODUCT_CODE1 + "," + PRODUCT_CODE3
				],
				contentType: JSON,
				requestContentType: JSON)

		then: "he gets future stock data for the set"
		with(response) {
			status == SC_NOT_FOUND

		}

	}

	def static initializeProductDate(int year, Month month, int dayOfMonth) {
		def localDateTime = LocalDateTime.of(year, month, dayOfMonth, 0, 0, 0)
		def zoneDateTime = localDateTime.atZone(ZoneId.systemDefault())
		def utcZoneDateTime = zoneDateTime.withZoneSameInstant(ZoneId.of("UTC"))
		def formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
		formatter.format(utcZoneDateTime)
	}

	def static initializeFormattedDate(int year, Month month, int dayOfMonth) {
		def date = LocalDate.of(year, month, dayOfMonth)
		def siteLocale = new Locale("en", "GB")
		def formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(siteLocale)
		formatter.format(date)
	}
}
