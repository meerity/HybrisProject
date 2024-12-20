/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.epam.training.test.test.groovy.webservicetests.v2.spock.products

import de.hybris.bootstrap.annotations.ManualTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.AbstractSpockFlowTest
import groovyx.net.http.HttpResponseDecorator
import spock.lang.Unroll

import static groovyx.net.http.ContentType.*
import static org.apache.http.HttpStatus.*

@ManualTest
@Unroll
class ProductResourceTest extends AbstractSpockFlowTest {
	static final DEFAULT_PAGE_SIZE = 20
	static final ALL_PRODUCTS_PAGE_SIZE = 50
	static final SECURE_BASE_SITE = "/wsSecureTest"

	static final MULTI_D_BASE_PRODUCT_CODE = '26002000'
	static final MULTI_D_VARIANT_PRODUCT_CODE = '26002000_1'

	static final LANGUAGE_GERMAN = 'de'
	static final PRODUCT_NAME = 'EOS 500D + EF-S 18-55 IS, kit'
	//for export reference
	static final PRODUCT_CODE = '2053226'
	static final REFERENCES_COUNT = 7
	static final ALL_REFERENCES_COUNT = 9

	static final PRODUCT_WITH_IMAGES_CODE = '872912a'

	def "Search for all products: #format"() {

		when: "user search for specified product attributes"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + '/products/search',
				contentType: format,
				query: [
						'pageSize': ALL_PRODUCTS_PAGE_SIZE,
						'fields'  : 'products,sorts,pagination'
				],
				requestContentType: URLENC
		)

		then: "he gets all the requested fields"
		with(response) {
			ALL_PRODUCTS_PAGE_SIZE > 0
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

	def "Get number of all products: #format"() {

		when: "user search for any products (empty query)"
		HttpResponseDecorator response = restClient.head(
				path: getBasePathWithSite() + '/products/search',
				contentType: format,
				requestContentType: URLENC
		)

		then: "he gets proper header"
		with(response) {
			status == SC_OK
			response.containsHeader(HEADER_TOTAL_COUNT)
			response.getFirstHeader(HEADER_TOTAL_COUNT).getValue().toInteger() > 0
		}

		where:
		format << [XML, JSON]
	}

	def "Check spelling suggestion: #format"() {

		when: "search input contains a typo"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + '/products/search',
				contentType: format,
				query: ['query': 'somy'],
				requestContentType: URLENC
		)

		then: "correct spelling suggestion will be returned"
		with(response) {
			status == SC_OK;
			data.currentQuery.query.value == 'somy:relevance'
			data.spellingSuggestion
			data.spellingSuggestion.suggestion == 'sony'
			data.spellingSuggestion.query == 'sony:relevance' || data.spellingSuggestion.query == 'sony:topRated'
		}
		where:
		format << [XML, JSON]
	}

	def "Check context default: #format"() {

		when: "search input contains a typo"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + '/products/search',
				contentType: format,
				query: [
						'query'             : '',
						'fields'            : 'products, facets',
						'searchQueryContext': 'DEFAULT'
				],
				requestContentType: URLENC
		)

		then: "results using default search query template"
		with(response) {
			status == SC_OK;
			data.pagination
			data.pagination.pageSize == DEFAULT_PAGE_SIZE
			isNotEmpty(data.facets)
			data.facets.size() == 6
		}

		where:
		format << [XML, JSON]
	}

	def "Check context suggestion: #format"() {

		when: "search input contains a typo"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + '/products/search',
				contentType: format,
				query: [
						'query'             : 'sony',
						'fields'            : 'products, facets',
						'searchQueryContext': 'SUGGESTIONS'
				],
				requestContentType: URLENC
		)

		then: "results using suggestions search query template"
		with(response) {
			status == SC_OK;
			data.pagination
			data.pagination.pageSize == DEFAULT_PAGE_SIZE
			isEmpty(data.facets)
		}

		where:
		format << [XML, JSON]
	}

	def "Check autosuggestion: #format"() {

		when: "search text is incomplete"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + '/products/suggestions',
				contentType: format,
				query: ['term': 'can'],
				requestContentType: URLENC
		)
		then: "set of possible autosuggested fields will be returned"
		with(response) {
			status == SC_OK;
			isNotEmpty(data.suggestions)
			data.suggestions.size() == 2
			data.suggestions.value[0] == 'canon'
			data.suggestions.value[1] == 'canyon'
		}
		where:
		format << [XML, JSON]
	}

	def "Check autosuggestion required parameter: #format"() {

		when: "required search text parameter is missing"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + '/products/suggestions',
				contentType: format,
				query: ['noneterm': 'can'],
				requestContentType: URLENC
		)
		then: "error with message 'required parameter missing' should be returned"
		with(response) {
			status == SC_BAD_REQUEST;
			isNotEmpty(data)
			data.errors[0].type == 'MissingServletRequestParameterError'
			data.errors[0].message ==~ /Required \w+ parameter.*/
		}
		where:
		format << [XML, JSON]
	}

	def "Check autosuggest with limit: #format"() {

		when: "search text is incomplete and the expected outcome is limmited"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + '/products/suggestions',
				contentType: format,
				query: [
						'max' : 1,
						'term': 'can'
				],
				requestContentType: URLENC
		)
		then: "limited set of possible autosuggested fields will be returned"
		with(response) {
			status == SC_OK;
			isNotEmpty(data.suggestions)
			data.suggestions.size() == 1
			data.suggestions.value[0] == 'canon'
		}
		where:
		format << [XML, JSON]
	}

	def "Check product sorting: #format"() {

		expect: "all camaras sorted by default sort order"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + '/products/search',
				contentType: format,
				query: ['query': 'camera'],
				requestContentType: URLENC
		)

		with(response) {
			status == SC_OK
			data.pagination.sort == 'relevance'
			isNotEmpty(data.sorts)
			data.sorts.find { it.code == response.data.pagination.sort }.selected == true
		}

		def allSorts = response.data.sorts

		and: "check all possible sort orders"

		allSorts.each { sortOrder ->
			HttpResponseDecorator sortResponse = restClient.get(
					path: getBasePathWithSite() + '/products/search',
					contentType: format,
					query: ['sort': sortOrder.code],
					requestContentType: URLENC
			)

			with(sortResponse) {
				assert status == SC_OK
				assert data.pagination.sort == sortOrder.code
			}
		}

		where:
		format << [XML, JSON]
	}

	def "Check product details: #format"() {

		expect: "all camaras sorted by default sort order"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + '/products/search',
				contentType: format,
				query: ['query': 'camera'],
				requestContentType: URLENC
		)

		with(response) {
			status == SC_OK
			isNotEmpty(data.products)
		}

		def productsList = response.data.products

		and: "check products details for all camaras"

		productsList.each { product ->
			HttpResponseDecorator productResponse = restClient.get(
					path: getBasePathWithSite() + '/products/' + product.code,
					contentType: format,
					requestContentType: URLENC
			)

			with(productResponse) {
				assert status == SC_OK
				assert data.code == product.code
				assert isNotEmpty(data.name)
				assert isNotEmpty(data.url)
			}
		}
		where:
		format << [XML, JSON]
	}

	def "Multi-d check base product and volume pricing: #format"() {

		expect: "verify that all needed fields are populated"

		HttpResponseDecorator productResponse = restClient.get(
				path: getBasePathWithSite() + '/products/' + MULTI_D_BASE_PRODUCT_CODE,
				query: ['fields': 'FULL'],
				contentType: format,
				requestContentType: URLENC
		)

		with(productResponse) {
			status == SC_OK
			data.code == MULTI_D_BASE_PRODUCT_CODE
			data.variantType == 'GenericVariantProduct'
			data.categories.size() == 3
			data.multidimensional == true
			data.variantMatrix.size() == 1
			data.variantMatrix[0].elements.size() == 14
			def variantMatrixOption = data.variantMatrix[0].variantOption
			isNotEmpty(variantMatrixOption)
			def variantMatrixOptionQualifiers = variantMatrixOption.variantOptionQualifiers
			variantMatrixOptionQualifiers.size() == 9
			variantMatrixOptionQualifiers.each { qual ->
				assert isNotEmpty(qual.image)
			}
			data.variantOptions.size() == 14
			def variantOption = data.variantOptions[0]
			isNotEmpty(variantOption)
			isNotEmpty(variantOption.code)
			isNotEmpty(variantOption.priceData)
			isNotEmpty(variantOption.stock)
			isNotEmpty(variantOption.url)
			def variantOptionQualifiers = variantOption.variantOptionQualifiers
			isNotEmpty(variantOptionQualifiers)
			variantOptionQualifiers.size() == 2
			variantOptionQualifiers.each { qual ->
				assert isNotEmpty(qual.name)
				assert isNotEmpty(qual.qualifier)
				assert isNotEmpty(qual.value)
			}
			data.volumePrices.size() == 2
			data.volumePrices[0].currencyIso == 'USD'
			data.volumePrices[0].formattedValue == '$85.00'
			data.volumePrices[0].maxQuantity == 9
			data.volumePrices[0].minQuantity == 1
			data.volumePrices[0].value == 85.0
			data.volumePrices[1].currencyIso == 'USD'
			data.volumePrices[1].formattedValue == '$80.00'
			data.volumePrices[1].minQuantity == 10
			data.volumePrices[1].value == 80.0
		}

		where:
		format << [XML, JSON]
	}

	def "Multi-d check product variants: #format"() {

		expect: "verify that all needed fields are populated"

		HttpResponseDecorator productResponse = restClient.get(
				path: getBasePathWithSite() + '/products/' + MULTI_D_VARIANT_PRODUCT_CODE,
				query: ['fields': 'FULL'],
				contentType: format,
				requestContentType: URLENC
		)

		with(productResponse) {
			status == SC_OK
			data.code == MULTI_D_VARIANT_PRODUCT_CODE
			data.baseProduct == MULTI_D_BASE_PRODUCT_CODE
			data.categories.size() == 2
			data.baseOptions.size() == 1
			data.baseOptions[0].variantType == 'GenericVariantProduct'
			def selected = data.baseOptions[0].selected
			isNotEmpty(selected)
			selected.code == MULTI_D_VARIANT_PRODUCT_CODE
			isNotEmpty(selected.priceData)
			isNotEmpty(selected.stock)
			isNotEmpty(selected.url)
			def baseOptions = data.baseOptions[0].options
			baseOptions.size() == 14
			def variantOption = baseOptions[0]
			isNotEmpty(variantOption)
			isNotEmpty(variantOption.code)
			isNotEmpty(variantOption.priceData)
			isNotEmpty(variantOption.stock)
			isNotEmpty(variantOption.url)
			def variantOptionQualifiers = variantOption.variantOptionQualifiers
			isNotEmpty(variantOptionQualifiers)
			variantOptionQualifiers.size() == 2
			variantOptionQualifiers.each { qual ->
				assert isNotEmpty(qual.name)
				assert isNotEmpty(qual.qualifier)
				assert isNotEmpty(qual.value)
			}
		}

		where:
		format << [XML, JSON]
	}

	def "Return product stockLevel: #format"() {

		when: "stockLevel <= stock inventory threshold"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + '/products/816780',
				contentType: format,
				requestContentType: URLENC
		)

		then: "stockLevel is returned, and isValueRounded is false"
		with(response) {
			status == SC_OK;
			isNotEmpty(data.stock)
			data.stock.stockLevel == 2
			data.stock.isValueRounded == false
		}
		where:
		format << [XML, JSON]
	}

	def "Hide product stockLevel: #format"() {

		when: "stockLevel > stock inventory threshold"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + '/products/' + PRODUCT_CODE,
				contentType: format,
				requestContentType: URLENC
		)

		then: "stockLevel is the rounded value (stock inventory threshold), isValueRounded is true"
		with(response) {
			println '---' + data.stock.stockLevel
			println '---' + data.stock.isValueRounded
			status == SC_OK;
			isNotEmpty(data.stock)
			data.stock.stockLevel == 5
			data.stock.isValueRounded == true
		}
		where:
		format << [XML, JSON]
	}


	def "Export similar product references: #format"() {
		when: "client requests an export of similar product references"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + '/products/' + PRODUCT_CODE + '/references',
				contentType: format,
				query: ['referenceType': 'SIMILAR'],
				requestContentType: URLENC
		)

		then: "all similar products are returned"
		with(response) {
			if (isNotEmpty(data.errors)) println data.errors
			status == SC_OK
			isNotEmpty(data.references)
			data.references.size() == REFERENCES_COUNT
		}

		where:
		format << [XML, JSON]
	}

	def "Export invalid product references: #format"() {
		when: "client requests an export of similar product references"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + '/products/' + PRODUCT_CODE + '/references',
				contentType: format,
				query: ['referenceType': referenceType],
				requestContentType: URLENC
		)

		then: "he receives proper header"
		with(response) {
			status == SC_BAD_REQUEST
			data.errors.size() > 0
		}
		and: "error is properly wrapped"
		with(response) {
			data.errors[0].type == "IllegalArgumentError"
		}

		where:
		format | referenceType
		XML    | 'SIMILAR' + ',' + 'INVALID_REFERENCE_TYPE'
		JSON   | 'SIMILAR' + ',' + 'INVALID_REFERENCE_TYPE'
		XML    | 'INVALID_REFERENCE_TYPE'
		JSON   | 'INVALID_REFERENCE_TYPE'
		XML    | ' '
		JSON   | ' '
		XML    | 'SIMILAR' + ',' + ' '
		JSON   | 'SIMILAR' + ',' + ' '
	}

	def "Export product references for two types of product references: #format"() {
		when: "client requests an export of product references without giving reference type"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + '/products/' + PRODUCT_CODE + '/references',
				contentType: format,
				query: ['referenceType': 'SIMILAR' + ',' + 'ACCESSORIES'],
				requestContentType: URLENC
		)

		then: "all products for two types of product references are returned"
		with(response) {
			if (isNotEmpty(data.errors)) println data.errors
			status == SC_OK
			isNotEmpty(data.references)
			data.references.size() == ALL_REFERENCES_COUNT
		}

		where:
		format << [XML, JSON]
	}

	def "Export product references for all type of product references: #format"() {
		when: "client requests an export of product references without giving reference type"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + '/products/' + PRODUCT_CODE + '/references',
				contentType: format,
				requestContentType: URLENC
		)

		then: "all products for all types of product references are returned"
		with(response) {
			if (isNotEmpty(data.errors)) println data.errors
			status == SC_OK
			isNotEmpty(data.references)
			data.references.size() == ALL_REFERENCES_COUNT
		}

		where:
		format << [XML, JSON]
	}

	def "Create anonymous review : #format"() {
		given: "review data"
		println 'Review data : ' + postBody

		when: "create review request is send"
		HttpResponseDecorator response = restClient.post(
				path: getBasePathWithSite() + '/products/' + PRODUCT_CODE + '/reviews',
				body: postBody,
				contentType: format,
				requestContentType: format)

		then: "review is created"
		with(response) {
			if (isNotEmpty(data.errors)) println data.errors
			status == SC_CREATED
			data.alias == "krzys"
			data.rating == 4.0
			data.comment == "perfect"
			data.headline == "samplereview"
			data.principal.uid == "anonymous"
		}
		where:
		format | postBody
		JSON   | '{\"alias\": \"krzys\", \"rating\": 4, \"comment\": \"perfect\", \"headline\": \"samplereview\"}'
		XML    | "<review><alias>krzys</alias><rating>4</rating><comment>perfect</comment><headline>samplereview</headline></review>"
	}

	def "Get product with name in fallback language: #format"() {
		when: "user retrieves product without name in current language"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + '/products/' + PRODUCT_CODE,
				contentType: format,
				query: [
						'lang'  : LANGUAGE_GERMAN,
						'fields': FIELD_SET_LEVEL_FULL
				],
				requestContentType: URLENC)

		then: "product is retrieved with name localized in fallback language"
		with(response) {
			status == SC_OK
			data.code == PRODUCT_CODE
			data.name == PRODUCT_NAME
		}
		where:
		format << [XML, JSON]
	}

	def "Check product images: #format"() {
		when: "user retrieves product by code"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + '/products/' + PRODUCT_WITH_IMAGES_CODE,
				query: ['fields': 'FULL'],
				contentType: format,
				requestContentType: URLENC
		)

		then: "product with primary and gallery images will be returned"
		with(response) {
			status == SC_OK
			data.code == PRODUCT_WITH_IMAGES_CODE
			isNotEmpty(data.images)
			data.images.any { it.imageType == 'PRIMARY' }
			data.images.any { it.imageType == 'GALLERY' }
		}

		where:
		format << [XML, JSON]
	}

	def "An anonymous user should be failed to retrieve product when requiresAuthentication ture: #format"() {
		given: "an anonymous user"
		removeAuthorization(restClient)

		and: "secure base site with requiresAuthentication=true"

		when: "user retrieves product by code"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + SECURE_BASE_SITE + '/products/' + PRODUCT_WITH_IMAGES_CODE,
				query: ['fields': 'FULL'],
				contentType: format,
				requestContentType: URLENC
		)
		then: "the request is failed"
		with(response) {
			status == SC_NOT_FOUND
		}

		where:
		format << [XML, JSON]
	}

	def "An anonymous user should be failed to Search for all products when requiresAuthentication ture: #format"() {
		given: "an anonymous user"
		removeAuthorization(restClient)

		and: "secure base site with requiresAuthentication=true"

		when: "user search product "
		HttpResponseDecorator response = restClient.get(
				path: getBasePath() + SECURE_BASE_SITE + '/products/search',
				contentType: format,
				query: [
						'pageSize': ALL_PRODUCTS_PAGE_SIZE,
						'fields'  : 'products,sorts,pagination'
				],
				requestContentType: URLENC
		)
		then: "the request is failed"
		with(response) {
			status == SC_NOT_FOUND
		}

		where:
		format << [XML, JSON]
	}
}
