/*
 * Original code for http-builder functions is taken from http-builder and we re-implemented those functions.
 * Http-builder version 0.7.1, Repo URL: https://github.com/jgritman/httpbuilder.git
 * Copyright 2008-2011 Thomas Nichols.  http://blog.thomnichols.org    tomstrummer+httpbuilder@gmail.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.epam.training.test.test.groovy.webservicetests.http

import groovy.json.JsonOutput
import org.apache.http.HttpEntity
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.entity.StringEntity
import org.apache.http.message.BasicNameValuePair

import java.nio.charset.Charset

/**
 * Default request body encoder supporting URLENC, JSON, or String.
 */
class DefaultHttpRequestBodyEncoder implements HttpRequestBodyEncoder {

	@Override
	HttpEntity encodeBody(final Object body, final String contentType) {
		if (body == null) {
			return null
		}
		if (ContentType.URLENC == contentType) {
			return this.encodeForm(body)
		} else {
			String encodedBody
			if (body instanceof String || body instanceof GString) {
				encodedBody = body
			} else {
				encodedBody = JsonOutput.toJson(body)
			}
			StringEntity entity = this.createStringEntity(encodedBody, contentType)
			return entity
		}
	}

	private StringEntity createStringEntity(String encodedBody, String contentType) {
		def StringEntity entity = new StringEntity(encodedBody, Charset.defaultCharset().toString())
		if (contentType != null) {
			entity.setContentType(contentType)
		}
		entity
	}

	private UrlEncodedFormEntity encodeForm(Map<?, ?> params) {
		return encodeForm(params, ContentType.URLENC);
	}

	private HttpEntity encodeForm(String formData) {
		return this.createStringEntity(formData, ContentType.URLENC)
	}

	private UrlEncodedFormEntity encodeForm(Map<?, ?> params, String contentType) {
		List<NameValuePair> paramList = new ArrayList<NameValuePair>();

		for (Object key : params.keySet()) {
			Object val = params.get(key);
			if (val instanceof List<?>) {
				for (Object subVal : (List<?>) val)
					paramList.add(new BasicNameValuePair(key.toString(),
							(subVal == null) ? "" : subVal.toString()))

			} else {
				paramList.add(new BasicNameValuePair(key.toString(),
						(val == null) ? "" : val.toString()))
			}
		}

		UrlEncodedFormEntity e = new UrlEncodedFormEntity(paramList, Charset.defaultCharset().toString());
		if (contentType != null) {
			e.setContentType(contentType.toString())
		}
		return e
	}
}
