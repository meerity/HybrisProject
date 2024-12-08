/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.epam.training.test.test.groovy.webservicetests.v2.spock.users

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.core.Registry
import com.epam.training.test.setup.TestSetupUtils
import groovyx.net.http.HttpResponseDecorator
import org.apache.http.ProtocolVersion
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.entity.BasicHttpEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.message.BasicStatusLine
import org.mockito.Mockito
import spock.lang.Unroll

import static groovyx.net.http.ContentType.*
import static org.apache.http.HttpStatus.*
import static org.mockito.ArgumentMatchers.any

/**
 *
 * This class focuses on tests more related to resource like registering users
 *
 */
@ManualTest
@Unroll
class UsersResourceTest extends AbstractUserTest {

	static final SECURE_BASE_SITE = "/wsSecureTest"
	static final String MOCK_ON_HTTP_CLIENT = "mockOnHttpClient"

	def "Register user with all required data when captchaCheckEnabled is true and token is valid and request: #requestFormat and response: #responseFormat"() {
		given: "authenticated client"
		authorizeClient(restClient)

		and: "set captchaCheckEnabled=true"
		TestSetupUtils.updateCaptchaCheckEnabled(true);

		and: "captcha validation is passed"
		mockRecaptchaCheckResult(200, true, null, "localhost");

		when: "user attempts to register"
		def response = restClient.post(
				path: getBasePathWithSite() + '/users',
				headers: [
						"sap-commerce-cloud-captcha-token": "mock_token"
				],
				body: postBody,
				contentType: responseFormat,
				requestContentType: requestFormat)

		then: "account is created"
		with(response) {
			if (isNotEmpty(data)) println data
			status == SC_CREATED
			1 == responseBase.original.headergroup.headers.findAll {
				"${it}".contains("Location: ") && "${it}".contains(getBasePathWithSite() + '/users/' + data.uid)
			}.size()
		}

        TestSetupUtils.updateCaptchaCheckEnabled(true);

		where:
		requestFormat | responseFormat | postBody
		URLENC        | JSON           | ['login': System.currentTimeMillis() + '@urlenc2json.pl', 'password': CUSTOMER_PASSWORD_STRONG, 'firstName': 'Jan', 'lastName': 'Kowalski', 'titleCode': 'mr']
		URLENC        | XML            | ['login': System.currentTimeMillis() + '@urlenc2xml.pl', 'password': CUSTOMER_PASSWORD_STRONG, 'firstName': 'Jan', 'lastName': 'Kowalski', 'titleCode': 'mr']
		JSON          | JSON           | '{"uid": "' + System.currentTimeMillis() + '@json2json.pl", "password": "' + CUSTOMER_PASSWORD_STRONG + '", "firstName": "Jan", "lastName": "Kowalski", "titleCode": "mr"}'
		XML           | XML            | "<user><uid>${System.currentTimeMillis()}@xml2xml.pl</uid><password>${CUSTOMER_PASSWORD_STRONG}</password><firstName>Jan</firstName><lastName>Kowalski</lastName><titleCode>mr</titleCode></user>"
	}

	def "Register user with all required data when requiresAuthentication true: #requestFormat and response: #responseFormat"() {
		given: "authenticated client"
		authorizeClient(restClient)

		and: "secure base site with requiresAuthentication=true"

		when: "user attempts to register"
		def response = restClient.post(
				path: getBasePath() + SECURE_BASE_SITE + '/users',
				body: postBody,
				contentType: responseFormat,
				requestContentType: requestFormat)

		then: "account is created"
		with(response) {
			if (isNotEmpty(data)) println data
			status == SC_CREATED
			1 == responseBase.original.headergroup.headers.findAll {
				"${it}".contains("Location: ") && "${it}".contains(getBasePath() + SECURE_BASE_SITE + '/users/' + data.uid)
			}.size()
		}

		where:
		requestFormat | responseFormat | postBody
		URLENC        | JSON           | ['login': System.currentTimeMillis() + '@urlenc2json02.pl', 'password': CUSTOMER_PASSWORD_STRONG, 'firstName': 'Jan02', 'lastName': 'Kowalski', 'titleCode': 'mr']
		JSON          | JSON           | '{"uid": "' + System.currentTimeMillis() + '@json2json02.pl", "password": "' + CUSTOMER_PASSWORD_STRONG + '", "firstName": "Jan02", "lastName": "Kowalski", "titleCode": "mr"}'
		XML           | XML            | "<user><uid>${System.currentTimeMillis()}@xml2xml02.pl</uid><password>${CUSTOMER_PASSWORD_STRONG}</password><firstName>Jan02</firstName><lastName>Kowalski</lastName><titleCode>mr</titleCode></user>"
	}


	def "Register user should be failed when captchaCheckEnable is true and #scenario"() {
		given: "authenticated client"
		authorizeClient(restClient)

		and: "set captchaCheckEnabled=true"
		TestSetupUtils.updateCaptchaCheckEnabled(true);

        and: "captcha validation is failed"
        mockRecaptchaCheckResult(inputStatus, inputSuccess, inputReason, hostname);

		when: "he requests to create registration request"
		HttpResponseDecorator response = restClient.post(
				path: getBasePathWithSite() + '/users',
				headers: [
						"sap-commerce-cloud-captcha-token": inputHeader
				],
				body: [
						'uid'      : System.currentTimeMillis() + '@urlenc2json.pl',
						'password' : CUSTOMER_PASSWORD_STRONG,
						'firstName': 'Jan',
						'lastName' : 'Kowalski',
						'titleCode': 'mr'
				],
				contentType: JSON,
				requestContentType: JSON)
		then: "the request is successful"
		with(response) {
			status == responseStatus
			data.errors[0].message == errorMessage
			data.errors[0].type == errorType
			data.errors[0].reason == errorReason
		}

		//set captchaCheckEnabled=false
		TestSetupUtils.updateCaptchaCheckEnabled(false);

        where:
        scenario                    | inputHeader | inputStatus | inputSuccess | inputReason              | responseStatus | errorMessage                                               | errorType                  | errorReason              | hostname
        "header is missing"         | null        | 400         | false        | null                     | 400            | "The captcha response token is required but not provided." | "CaptchaTokenMissingError" | null                     | "localhost"
        "privateKey is missing"     | "mockToken" | 400         | false        | "missing-input-secret"   | 400            | "Invalid answer to captcha challenge."                     | "CaptchaValidationError"   | "missing-input-secret"   | "localhost"
        "privateKey is invalid"     | "mockToken" | 400         | false        | "invalid-input-secret"   | 400            | "Invalid answer to captcha challenge."                     | "CaptchaValidationError"   | "invalid-input-secret"   | "localhost"
        "header is invalid"         | "mockToken" | 400         | false        | "invalid-input-response" | 400            | "Invalid answer to captcha challenge."                     | "CaptchaValidationError"   | "invalid-input-response" | "localhost"
        "header timeout/duplicated" | "mockToken" | 400         | false        | "timeout-or-duplicated"  | 400            | "Invalid answer to captcha challenge."                     | "CaptchaValidationError"   | "timeout-or-duplicated"  | "localhost"
        "exceed quota limits"       | "mockToken" | 429         | false        | "exceed-quota-limits"    | 400            | "Invalid answer to captcha challenge."                     | "CaptchaValidationError"   | "exceed-quota-limits"    | "localhost"
        "header format is invalid"  | "<html>"    | 400         | false        | "format-invalid"         | 400            | "Invalid answer to captcha challenge."                     | "CaptchaValidationError"   | "format-invalid"         | "localhost"
        "invalid keys"              | "mockToken" | 400         | false        | "invalid-keys"           | 400            | "Invalid answer to captcha challenge."                     | "CaptchaValidationError"   | "invalid-keys"           | "localhost"
        "invalid hostname"          | "mockToken" | 400         | true         | "invalid-hostname"       | 400            | "Invalid answer to captcha challenge."                     | "CaptchaValidationError"   | "invalid-hostname"       | "electronics.local"
    }

	def "Register user using HTTP"() {
		given: "a trusted client"
		authorizeTrustedClient(restClient)
		// YTODO : find a better way to disable automatic redirects
		restClient.setUri(getDefaultHttpUri())

		when: "user attempts to register using HTTP"
		def randomUID = System.currentTimeMillis()
		HttpResponseDecorator response = restClient.post(
				path: getBasePathWithSite() + '/users/',
				body: [
						'login'    : randomUID + '@test.v2.com',
						'password' : CUSTOMER_PASSWORD_STRONG,
						'firstName': CUSTOMER_FIRST_NAME,
						'lastName' : CUSTOMER_LAST_NAME,
						'titleCode': CUSTOMER_TITLE_CODE
				],
				requestContentType: URLENC)

		then: "he is not allowed to do so"
		with(response) { status == SC_MOVED_TEMPORARILY }

	}

	def "Register user with duplicate ID : #format"() {
		given: "a trusted client"
		authorizeTrustedClient(restClient)

		when: "user registers account"
		def randomUID = System.currentTimeMillis()
		with(restClient.post(
				path: getBasePathWithSite() + '/users/',
				body: [
						'login'    : randomUID + '@test.v2.com',
						'password' : CUSTOMER_PASSWORD_STRONG,
						'firstName': CUSTOMER_FIRST_NAME,
						'lastName' : CUSTOMER_LAST_NAME,
						'titleCode': CUSTOMER_TITLE_CODE
				],
				contentType: format,
				requestContentType: URLENC)) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_CREATED
		}

		and: "tries to register account with the same login"
		HttpResponseDecorator response = restClient.post(
				path: getBasePathWithSite() + '/users/',
				body: [
						'login'    : randomUID + '@test.v2.com',
						'password' : CUSTOMER_PASSWORD_STRONG,
						'firstName': CUSTOMER_FIRST_NAME,
						'lastName' : CUSTOMER_LAST_NAME,
						'titleCode': CUSTOMER_TITLE_CODE
				],
				contentType: format,
				requestContentType: URLENC)

		then: "it is not allowed to register same login twice"
		with(response) {
			status == SC_BAD_REQUEST
			isNotEmpty(data.errors)
			data.errors[0].type == 'DuplicateUidError'
		}

		where:
		format << [XML, JSON]
	}

	def "Register user with duplicate ID (UserSignUpWsDTO) : #format"() {

		def randomUID = System.currentTimeMillis()

		given: "a trusted client"
		authorizeTrustedClient(restClient)

		when: "user registers account"
		def requestBody = """{
						|"uid" : "${randomUID}@test.v2.com",
						|"password" : "${CUSTOMER_PASSWORD_STRONG}",
						|"firstName" : "${CUSTOMER_FIRST_NAME}",
						|"lastName" : "${CUSTOMER_LAST_NAME}",
						|"titleCode" : "${CUSTOMER_TITLE_CODE}" 
				|}""".stripMargin();

		with(restClient.post(
				path: getBasePathWithStandaloneSite() + '/users/',
				body: requestBody,
				contentType: format,
				requestContentType: format)) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_CREATED
		}

		and: "tries to register account with the same login"
		HttpResponseDecorator response = restClient.post(
				path: getBasePathWithStandaloneSite() + '/users/',
				body: requestBody,
				contentType: format,
				requestContentType: format)

		then: "it return the dummy account"
		with(response) {
			status == SC_CREATED
			isNotEmpty(data.uid)
			data.uid == "${randomUID}@test.v2.com|wsStandaloneTest"
		}

		where:
		format << [JSON]

	}

	def "Register user with capital letters in the uid or login when request: #requestFormat and response: #responseFormat"() {
		given: "authenticated client"
		authorizeClient(restClient)

		def validUid = login.toLowerCase()
		when: "user attempts to register"
		def response = restClient.post(
				path: getBasePathWithSite() + '/users',
				body: postBody,
				contentType: responseFormat,
				requestContentType: requestFormat)

		then: "Account is created and has no capital letters in the uid"
		with(response) {
			if (isNotEmpty(data)) println data
			status == SC_CREATED
			1 == responseBase.original.headergroup.headers.findAll {
				"${it}".contains("Location: ") && "${it}".contains(getBasePathWithSite() + '/users/' + data.uid)
			}.size()
			data.uid == validUid
		}

		where:
		requestFormat | responseFormat | login                                              | postBody
		URLENC        | JSON           | System.currentTimeMillis() + 'USER@urlenc2json.pl' | ['login': login, 'password': CUSTOMER_PASSWORD_STRONG, 'firstName': 'Jan', 'lastName': 'Kowalski', 'titleCode': 'mr']
		URLENC        | XML            | System.currentTimeMillis() + 'USER@urlenc2xml.pl'  | ['login': login, 'password': CUSTOMER_PASSWORD_STRONG, 'firstName': 'Jan', 'lastName': 'Kowalski', 'titleCode': 'mr']
		XML           | XML            | System.currentTimeMillis() + 'USER@xml2xml.pl'     | "<user><uid>${login}</uid><password>${CUSTOMER_PASSWORD_STRONG}</password><firstName>Jan</firstName><lastName>Kowalski</lastName><titleCode>mr</titleCode></user>"
		JSON          | JSON           | System.currentTimeMillis() + 'USER@json2json.pl'   | '{"uid": "' + login + '", "password": "' + CUSTOMER_PASSWORD_STRONG + '", "firstName": "Jan", "lastName": "Kowalski", "titleCode": "mr"}'
	}

	def "Login a non-existing user : #format"() {
		given: "a trusted client"
		authorizeTrustedClient(restClient)

		when: "a non-existing customer tries to log in"
		HttpResponseDecorator response = restClient.post(
				uri: getOAuth2TokenUri(),
				path: getOAuth2TokenPath(),
				body: [
						'grant_type'   : 'password',
						'client_id'    : clientId,
						'client_secret': clientSecret,
						'username'     : 'nonExistingUser@hybris.com',
						'password'     : 'password'
				],
				contentType: JSON,
				requestContentType: URLENC)

		then: "he gets an error"
		with(response) {
			status == SC_BAD_REQUEST
			data.error == 'invalid_grant'
			data.error_description == 'Bad credentials'
		}
		where:
		format << [XML, JSON]
	}

	def "Login with wrong password : #format"() {
		given: "a trusted client"
		authorizeTrustedClient(restClient)

		when: "a non-existing customer tries to log in"
		HttpResponseDecorator response = restClient.post(
				uri: getOAuth2TokenUri(),
				path: getOAuth2TokenPath(),
				body: [
						'grant_type'   : 'password',
						'client_id'    : clientId,
						'client_secret': clientSecret,
						'username'     : CUSTOMER_USERNAME,
						'password'     : 'this_is_wrong'
				],
				contentType: JSON,
				requestContentType: URLENC)

		then: "he gets an error"
		with(response) {
			status == SC_BAD_REQUEST
			data.error == 'invalid_grant'
			data.error_description == 'Bad credentials'
		}
		where:
		format << [XML, JSON]
	}

	def "Deactivate given user: #format"() {
		given: "a trusted client"
		authorizeTrustedClient(restClient)

		def randomUID = System.currentTimeMillis() + '-deact@test.v2.com'
		when: "user registers account"
		with(restClient.post(
				path: getBasePathWithSite() + '/users/',
				body: [
						'login'    : randomUID,
						'password' : CUSTOMER_PASSWORD_STRONG,
						'firstName': CUSTOMER_FIRST_NAME,
						'lastName' : CUSTOMER_LAST_NAME,
						'titleCode': CUSTOMER_TITLE_CODE
				],
				contentType: format,
				requestContentType: URLENC)) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_CREATED
		}

		and: "deactivates new user"
		def response = restClient.delete(
				path: getBasePathWithSite() + '/users/' + randomUID,
				contentType: format,
				requestContentType: URLENC
		)
		with(response) { status == SC_OK }

		then: "user is deactivated"
		def errorData = getOAuth2TokenUsingPassword(restClient, getClientId(), getClientSecret(), randomUID, CUSTOMER_PASSWORD_STRONG, false);
		errorData.error == 'invalid_grant'
		errorData.error_description == 'User is disabled'

		where:
		format << [XML, JSON]
	}


    private void mockRecaptchaCheckResult(int httpStatus, boolean success, String reason, String hostname) {
        final CloseableHttpClient httpClient = Registry.getApplicationContext().getBean(MOCK_ON_HTTP_CLIENT, HttpClient.class);
        // it should be an spy of defaultCartFacade

        final CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
        final ProtocolVersion protocolVersion = Mockito.mock(ProtocolVersion.class);

        Mockito.when(httpClient.execute(any())).thenReturn(httpResponse);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(new BasicStatusLine(protocolVersion, httpStatus, reason));
        if (200 == httpStatus) {
            String response = String.format("{\"success\":%s,\"error-codes\":[\"%s\"],\"hostname\":\"%s\"}", success, reason, hostname);
            InputStream inputStreamRoute = new ByteArrayInputStream(response.getBytes());

            final BasicHttpEntity httpEntity = new BasicHttpEntity();
            httpEntity.setContent(inputStreamRoute);
            Mockito.when(httpResponse.getEntity()).thenReturn(httpEntity);
        }
    }

}
