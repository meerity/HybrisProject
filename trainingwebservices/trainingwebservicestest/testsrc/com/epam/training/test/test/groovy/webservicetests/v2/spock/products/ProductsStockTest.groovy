/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.epam.training.test.test.groovy.webservicetests.v2.spock.products

import de.hybris.bootstrap.annotations.ManualTest;
import com.epam.training.test.test.groovy.webservicetests.v2.spock.AbstractSpockFlowTest
import groovyx.net.http.HttpResponseDecorator

import static groovyx.net.http.ContentType.*
import static org.apache.http.HttpStatus.SC_OK
import static org.apache.http.HttpStatus.SC_NOT_FOUND

/**
 *
 */
@ManualTest
class ProductsStockTest extends AbstractSpockFlowTest {
	static final SECURE_BASE_SITE = "/wsSecureTest"

	def "Get total number of product's stock levels : #format"() {

		when: "user search for product's stock levels"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + '/products/3429337/stock',
				contentType: format,
				query: ['location': 'tokio'],
				requestContentType: URLENC
		)

		then: "he gets all the requested fields"
		with(response) {
			status == SC_OK
			response.getFirstHeader(HEADER_TOTAL_COUNT).getValue() == '49'
		}

		where:
		format << [XML, JSON]
	}

	def "An anonymous user should be failed to get total number of product's stock levels when requiresAuthentication true: #format"() {

		given: "an anonymous user"
		removeAuthorization(restClient)

		and: "secure base site with requiresAuthentication=true"

		when: "user search for product's stock levels"
		HttpResponseDecorator response = restClient.get(
				path: getBasePath() + SECURE_BASE_SITE + '/products/3429337/stock',
				contentType: format,
				query: ['location': 'tokio'],
				requestContentType: URLENC
		)

		then: "the user fail to get the stock levels"
		with(response) {
			status == SC_NOT_FOUND
		}

		where:
		format << [XML, JSON]
	}
}
