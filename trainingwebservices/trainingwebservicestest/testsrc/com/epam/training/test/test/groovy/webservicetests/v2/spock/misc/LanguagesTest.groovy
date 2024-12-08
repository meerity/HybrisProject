/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.epam.training.test.test.groovy.webservicetests.v2.spock.misc

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.ContentType.XML
import static org.apache.http.HttpStatus.SC_OK

import de.hybris.bootstrap.annotations.ManualTest
import com.epam.training.test.test.groovy.webservicetests.v2.spock.AbstractSpockFlowTest

import spock.lang.Unroll
import groovyx.net.http.HttpResponseDecorator

@ManualTest
@Unroll
class LanguagesTest extends AbstractSpockFlowTest {

	private languageList = ['ja', 'en', 'de', 'zh']

	static final SECURE_BASE_SITE = "/wsSecureTest"


	def "Client retrieves supported languages: #format"() {

		when:
		HttpResponseDecorator response = restClient.get(path: getBasePathWithSite() + '/languages', contentType: format)

		then:
		with(response) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
			isNotEmpty(data.languages)
			data.languages.size() == languageList.size()
			data.languages.findAll { language ->
				language.isocode in languageList
			}.size() == languageList.size()
		}

		where:
		format << [JSON, XML]
	}

	def "Client retrieves supported languages when requiresAuthentication true: #format"() {

		when:
		HttpResponseDecorator response = restClient.get(path: getBasePath() + SECURE_BASE_SITE +'/languages', contentType: format)

		then:
		with(response) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
			isNotEmpty(data.languages)
			data.languages.size() == languageList.size()
			data.languages.findAll { language ->
				language.isocode in languageList
			}.size() == languageList.size()
		}

		where:
		format << [JSON, XML]
	}
}
