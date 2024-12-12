/*
 * Copyright (c) 2024 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.epam.training.test.test.groovy.webservicetests.v2.spock.forgottenpasswords

import de.hybris.bootstrap.annotations.ManualTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.users.AbstractUserTest
import groovyx.net.http.HttpResponseDecorator
import spock.lang.Unroll

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.ContentType.XML
import static org.apache.http.HttpStatus.SC_ACCEPTED
import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static org.apache.http.HttpStatus.SC_CREATED
import static org.apache.http.HttpStatus.SC_NOT_FOUND

@ManualTest
@Unroll
class ForgottenPasswordsTest extends AbstractUserTest
{
	static final SECURE_BASE_SITE = "/wsSecureTest"
	static final String CUSTOMER_ID = 'customer@test.com'

	def "Anonymous user could generate a token to restore the forgotten password: #format via deprecated API"() {

		given: "a registered user with trusted client"
		authorizeTrustedClient(restClient)

		when:
		HttpResponseDecorator response = restClient.post(
				path: getBasePathWithSite() + '/forgottenpasswordtokens',
				body: ['userId': CUSTOMER_ID],
				contentType: format,
				requestContentType: URLENC
		)

		then:
		with(response) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_ACCEPTED
		}

		where:
		format << [JSON, XML]
	}

	def "Anonymous user could generate a token to restore the forgotten password when requiresAuthentication true: #format via deprecated API"() {
		given: "a registered user with trusted client"
		authorizeTrustedClient(restClient)

		when:
		HttpResponseDecorator response = restClient.post(
				path: getBasePath() + SECURE_BASE_SITE +'/forgottenpasswordtokens',
				body: ['userId': CUSTOMER_ID],
				contentType: format,
				requestContentType: URLENC
		)

		then:
		with(response) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_ACCEPTED
		}

		where:
		format << [JSON, XML]
	}

	def "Anonymous user could generate a token to restore the forgotten password when #scenario"() {
		given: "a registered user with trusted client"
		authorizeTrustedClient(restClient)

		when:
		HttpResponseDecorator response = restClient.post(
				path: getBasePathWithSite() + '/passwordRestoreToken',
				body: ['loginId': loginId],
				contentType: JSON,
				requestContentType: JSON
		)

		then:
		with(response) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_CREATED
		}

		where:
		scenario                 | loginId
		"loginId exists"         | CUSTOMER_ID
		"loginId does not exist" | "not_existing@test.com"
	}

	def "Anonymous user could not generate a token to restore the forgotten password when #scenario"() {
		given: "a registered user with trusted client"
		authorizeTrustedClient(restClient)

		when:
		HttpResponseDecorator response = restClient.post(
				path: getBasePathWithSite() + '/passwordRestoreToken',
				body: requestBody,
				contentType: JSON,
				requestContentType: JSON
		)

		then:
		with(response) {
			status == SC_BAD_REQUEST
			data.errors[0].message == "This field is not a valid email address."
			data.errors[0].type == "ValidationError"
		}

		where:
		scenario               | requestBody
		"loginId is empty"     | ['loginId': '']
		'request body is null' | {}
		"loginId is null"      | ['loginId': null]
	}

	def "Anonymous user could generate a token to restore the forgotten password when requiresAuthentication true"() {
		given: "a registered user with trusted client"
		authorizeTrustedClient(restClient)

		when:
		HttpResponseDecorator response = restClient.post(
				path: getBasePath() + SECURE_BASE_SITE +'/passwordRestoreToken',
				body: ['loginId': CUSTOMER_ID],
				contentType: JSON,
				requestContentType: JSON
		)

		then:
		with(response) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_CREATED
		}
	}

	def "Anonymous user could generate a token to restore the forgotten password when requiresAuthentication true: #format"() {
		given: "a registered user with trusted client"
		authorizeTrustedClient(restClient)

		when:
		HttpResponseDecorator response = restClient.post(
				path: getBasePath() + SECURE_BASE_SITE +'/passwordRestoreToken',
				body: ['loginId': CUSTOMER_ID],
				contentType: format,
				requestContentType: JSON
		)

		then:
		with(response) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_CREATED
		}

		where:
		format << [JSON]
	}

	def "Anonymous user could reset password with token when requiresAuthentication true: #format"() {
		given: "token of a registered user with trusted client"
		authorizeClient(restClient)
		def customer1 = registerCustomer(restClient, format)
		def tokenData = getOAuth2TokenUsingPassword(restClient, getClientId(), getClientSecret(), customer1.id, customer1.password,)

		when:
		HttpResponseDecorator response = restClient.post(
				path: getBasePath() + SECURE_BASE_SITE + '/resetpassword',
				body: [
						'newPassword': 'PAss4321!',
						'token': tokenData.access_token
				],
				contentType: format,
				requestContentType: format
		)

		then:
		with(response) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status != SC_NOT_FOUND
		}

		where:
		format << [JSON]
	}
}
