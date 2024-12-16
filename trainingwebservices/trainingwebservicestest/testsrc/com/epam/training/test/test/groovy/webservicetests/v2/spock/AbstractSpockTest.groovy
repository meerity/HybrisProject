/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.epam.training.test.test.groovy.webservicetests.v2.spock

import de.hybris.platform.core.Registry
import de.hybris.platform.servicelayer.config.ConfigurationService
import de.hybris.platform.testframework.JUnitPlatformSpecification
import de.hybris.platform.util.Config
import com.epam.training.test.setup.TestSetupUtils
import com.epam.training.test.test.groovy.webservicetests.SSLIssuesIgnoringHttpClientFactory
import com.epam.training.test.test.groovy.webservicetests.config.TestConfigFactory
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.apache.http.client.HttpClient
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.URLENC
import static org.apache.http.HttpStatus.SC_OK
import static org.junit.Assume.assumeTrue

@Ignore
abstract class AbstractSpockTest extends JUnitPlatformSpecification {
	private static String LOG4J_PROPERTIES_CLASS_PATH = "trainingwebservicestest/log4j.properties";
	private static String COMMONS_LOGGING_LOGGER_ATTRIBUTE_NAME = "org.apache.commons.logging.Log";
	private static String COMMONS_LOGGING_LOGGER_ATTRIBUTE_VALUE = "org.apache.commons.logging.impl.Log4JLogger";

	protected RESTClient restClient
	protected static ConfigObject config = TestConfigFactory.createConfig("v2", "/trainingwebservicestest/groovytests-property-file.groovy");
	private static final ThreadLocal<Boolean> SERVER_NEEDS_SHUTDOWN = new ThreadLocal<Boolean>();

	private final ConfigurationService configurationService = Registry.getApplicationContext()
			.getBean("configurationService", ConfigurationService.class)

	protected String getConfigurationProperty(final String propertyName) {
		return configurationService.getConfiguration().getString(propertyName)
	}

	@Test
	public void testing() {
		//dummy test class necessary for the test class to be considered
	}

	@BeforeClass
	public static void startServerIfNeeded() {
		if (!TestSetupUtils.isServerStarted()) {
			SERVER_NEEDS_SHUTDOWN.set(true);
			TestSetupUtils.startServer();
		}
	}

	@AfterClass
	public static void stopServerIfNeeded() {
		if (SERVER_NEEDS_SHUTDOWN.get()) {
			TestSetupUtils.stopServer();
			SERVER_NEEDS_SHUTDOWN.set(false);
		}
	}

	def setup() {
		restClient = createRestClient()
	}

	def cleanup() {
		restClient.shutdown()
	}

	protected static final String getDefaultHttpUri() {
		return config.DEFAULT_HTTP_URI
	}

	protected static final String getDefaultHttpsUri() {
		return config.DEFAULT_HTTPS_URI
	}

	protected static final String getBasePath() {
		return config.BASE_PATH
	}

	protected static final String getBasePathWithSite() {
		return config.BASE_PATH_WITH_SITE
	}

	protected static final String getBasePathWithIntegrationSite() {
		def enableWsIntegrationTest = Config.getBoolean("trainingwebservicestest.enableWsIntegrationTest", false);
		return enableWsIntegrationTest ? config.BASE_PATH_WITH_INTEGRATION_SITE : config.BASE_PATH_WITH_SITE
	}

	protected static final String getBasePathWithB2BSite() {
		return config.BASE_PATH_WITH_B2B_SITE
	}

	protected static final String getBasePathWithStandaloneSite() {
		return config.BASE_PATH_WITH_STANDALONE_SITE
	}

	protected static final String getOAuth2TokenUri() {
		return config.OAUTH2_TOKEN_URI
	}

	protected static final String getOAuth2TokenPath() {
		return config.OAUTH2_TOKEN_ENDPOINT_PATH
	}

	protected static final String getClientId() {
		return config.CLIENT_ID
	}

	protected static final String getClientSecret() {
		return config.CLIENT_SECRET
	}

	protected static final String getClientRedirectUri() {
		return config.OAUTH2_CALLBACK_URI
	}

	protected static final String getTrustedClientId() {
		return config.TRUSTED_CLIENT_ID
	}

	protected static final String getTrustedClientSecret() {
		return config.TRUSTED_CLIENT_SECRET
	}

	protected boolean isUUID(String name) {
		try {
			UUID.fromString(name);
			true
		}
		catch (IllegalArgumentException ignored) {
			false
		}
	}

	protected String getUserId(boolean useCustomerId, Map customer) {
		assumeTrue(!useCustomerId || isUUID(customer.customerId.toString()))
		useCustomerId ? customer.customerId : customer.id
	}

	protected RESTClient createRestClient(uri = config.DEFAULT_HTTPS_URI) {
		def restClient = new CustomRESTClient(uri);

		// makes sure we can access the services even without a valid SSL certificate
		HttpClient httpClient = SSLIssuesIgnoringHttpClientFactory.createHttpClient();
		restClient.setClient(httpClient);

		return restClient;
	}

	protected RESTClient createRestClientWithoutParsing(uri = config.DEFAULT_HTTPS_URI) {
		def restClient = createRestClient(uri);

		// replace the response handler to prenvent all parsing and therefore the use of xerces for parsing html
		// avoid parsing by setting the 'parser' property.
		restClient.parser = null
		return restClient;
	}

	protected void addAuthorization(RESTClient client, token) {
		client.addHeader("Authorization", " Bearer " + (token.access_token as String))
	}

	protected void removeAuthorization(RESTClient client) {
		client.removeHeader('Authorization')
	}

	protected getOAuth2TokenUsingClientCredentials(RESTClient client, clientId, clientSecret) {
		HttpResponseDecorator response = client.post(
				uri: getOAuth2TokenUri(),
				path: getOAuth2TokenPath(),
				body: [
						'grant_type'   : 'client_credentials',
						'client_id'    : clientId,
						'client_secret': clientSecret
				],
				contentType: JSON,
				requestContentType: URLENC)

		with(response) {
			if (isNotEmpty(data) && isNotEmpty(data.error)) println(data)
			assert status == SC_OK
			assert data.token_type == 'bearer'
			assert data.access_token
			assert data.expires_in
		}

		return response.data
	}

	protected getOAuth2TokenUsingPassword(RESTClient client, clientId, clientSecret, username, password, boolean doAssert = true) {
		HttpResponseDecorator response = client.post(
				uri: getOAuth2TokenUri(),
				path: getOAuth2TokenPath(),
				body: [
						'grant_type'   : 'password',
						'client_id'    : clientId,
						'client_secret': clientSecret,
						'username'     : username,
						'password'     : password
				],
				contentType: JSON,
				requestContentType: URLENC)

		if (doAssert) {
			with(response) {
				if (isNotEmpty(data) && isNotEmpty(data.error)) println(data)
				assert status == SC_OK
				assert data.token_type == 'bearer'
				assert data.access_token
				assert data.expires_in
				assert data.refresh_token
			}
		}

		return response.data
	}

	protected refreshOAuth2Token(RESTClient client, refreshToken, clientId, clientSecret, redirectUri) {
		def bodyParams = [
				'grant_type'   : 'refresh_token',
				'refresh_token': refreshToken
		]

		if (clientId) {
			bodyParams['client_id'] = clientId
		}

		if (clientSecret) {
			bodyParams['client_secret'] = clientSecret
		}

		if (redirectUri) {
			bodyParams['redirect_uri'] = URLEncoder.encode(redirectUri, 'UTF-8')
		}

		HttpResponseDecorator response = client.post(
				uri: getOAuth2TokenUri(),
				path: getOAuth2TokenPath(),
				body: bodyParams,
				contentType: JSON,
				requestContentType: URLENC)

		with(response) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			assert status == SC_OK
			assert data.token_type == 'bearer'
			assert data.access_token
			assert data.expires_in
			assert data.refresh_token
		}

		return response.data
	}

	protected void authorizeClient(RESTClient client) {
		def token = getOAuth2TokenUsingClientCredentials(client, getClientId(), getClientSecret())
		addAuthorization(client, token)
	}

	protected void authorizeTrustedClient(RESTClient client) {
		def token = getOAuth2TokenUsingClientCredentials(client, getTrustedClientId(), getTrustedClientSecret())
		addAuthorization(client, token)
	}

	/**
	 * Checks if a node exists and is not empty. Works for JSON and XML formats.
	 *
	 * @param the node to check
	 * @return {@code true} if the node is not empty, {@code false} otherwise
	 */
	protected isNotEmpty(node) {
		(node != null) && (node.size() > 0)
	}

	/**
	 * Checks if a node doesn't exist or is empty. Works for JSON and XML formats.
	 *
	 * @param the node to check
	 * @return {@code true} if the node is not empty, {@code false} otherwise
	 */
	protected isEmpty(node) {
		(node == null) || (node.size() == 0)
	}


	/**
	 * Same as {@link spock.lang.Specification#with(Object, groovy.lang.Closure)}, the only difference is that it returns the target object.
	 *
	 * @param target an implicit target for conditions and/or interactions
	 * @param closure a code block containing top-level conditions and/or interactions
	 * @return the target object
	 */
	def returningWith(target, closure) {
		with(target, closure)
		return target
	}

	/**
	 * @Deprecated use {@link com.epam.training.test.test.groovy.webservicetests.http.SimpleHttpClient} instead.
	 *  The customized behaviour is already the default behavior of the new class.
	 */
	@Deprecated
	protected static class CustomRESTClient extends RESTClient {
		public CustomRESTClient(Object defaultURI) throws URISyntaxException {
			super(defaultURI)
		}
	}
}
