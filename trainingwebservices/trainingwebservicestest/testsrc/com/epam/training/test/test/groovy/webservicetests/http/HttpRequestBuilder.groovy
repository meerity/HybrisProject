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

import org.apache.http.HttpEntityEnclosingRequest
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.client.utils.URIBuilder

/**
 * The class to build a {@link HttpRequestBase}. The input is a map of strings, set by the client code.
 * The instance of this class should be created every time when a request is needed. In other words the base property should not be re-used.
 * The encoder property is set by client code thus customizable there.
 */
class HttpRequestBuilder {

	HttpRequestBase base

	URIBuilder uriBuilder

	HttpRequestBodyEncoder encoder

	HttpRequestBuilder() {
		this(null)
	}

	HttpRequestBuilder(HttpRequestBase base) {
		this(base, "", new DefaultHttpRequestBodyEncoder())
	}

	HttpRequestBuilder(HttpRequestBase base, String uri, HttpRequestBodyEncoder encoder) {
		this.base = base
		this.uriBuilder = new URIBuilder(uri)
		this.encoder = encoder
	}

	HttpRequestBuilder addHeaders(Map<String, String> headers) {
		headers.each {
			this.base.addHeader(it.key, it.value)
		}
		return this
	}

	def void removeHeader(String key) {
		this.base.removeHeaders(key)
	}

	@SuppressWarnings("unchecked")
	HttpRequestBuilder parseMap(Map<String, ?> args) {
		if (args == null) return;
		Object uri = args.remove("uri")
		if (uri != null) {
			this.uriBuilder = new URIBuilder(uri)
		}

		Map query = (Map) args.remove("params");
		if (query != null) {
			log.warn("'params' argument is deprecated; use 'query' instead.");
			query.each {
				this.uriBuilder.addParameter(it.key as String, it.value as String)
			}
		}

		query = (Map) args.remove("query");
		if (query != null) {
			query.each {
				this.uriBuilder.addParameter(it.key as String, it.value as String)
			}
		}

		Map headers = (Map) args.remove("headers");
		if (headers != null) {
			headers.each {
				this.base.addHeader(it.key as String, it.value as String)
			}
		}

		Object path = args.remove("path");
		if (path != null) {
			this.uriBuilder.setPath(path.toString())
		}

		String contentType = args.remove("contentType");
		if (contentType != null) {
			this.base.setHeader(HttpHeaders.ACCEPT, contentType);
		}

		String requestContentType = args.remove("requestContentType")
		if (requestContentType == null) {
			requestContentType = contentType
		}

		Object body = args.remove("body")
		encodeBody(body, requestContentType as String)

		if (args.size() > 0) {
			String invalidArgs = "";
			for (String k : args.keySet()) invalidArgs += k + ",";
			throw new IllegalArgumentException("Unexpected keyword args: " + invalidArgs);
		}
		return this
	}

	void encodeBody(def body, String contentType) {
		def encodedBody = this.encoder.encodeBody(body, contentType)
		if (encodedBody == null) {
			return
		}
		def enclosingRequest = (HttpEntityEnclosingRequest) this.base
		enclosingRequest.setEntity(encodedBody)
	}

	HttpRequestBase build() {
		base.setURI(this.uriBuilder.build())
		return base
	}
}
