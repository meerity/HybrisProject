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


import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.*

/**
 * The Http client is configured by setting its attributes. Then the client object is used to send multiple requests and get parsed response data.
 * The configured attributes will be set to each request if necessary.
 *
 * Customize response parsing through setting
 * {@link com.epam.training.test.test.groovy.webservicetests.http.SimpleHttpClient#parser}
 *  which can be your own implementation of the interface.
 *  Customizing request body encoding through setting
 * {@link com.epam.training.test.test.groovy.webservicetests.http.SimpleHttpClient#encoder}
 *  which can be your own implementation of the interface.
 */
class SimpleHttpClient {

	private String defaultURI

	private HttpClient client

	Map<String, String> headers

	HttpResponseParser parser = new TypeAwareResponseParser()

	HttpRequestBodyEncoder encoder = new DispatchingHttpRequestBodyEncoder()

	Closure postProcessor = null

	SimpleHttpClient() {
		this(null, null)
	}

	SimpleHttpClient(HttpClient client) {
		this(client, null)
	}

	SimpleHttpClient(HttpClient client, String uri) {
		this.client = client
		this.defaultURI = uri
		this.headers = new HashMap<>()
	}

	void setUri(String uri) {
		this.defaultURI = uri
	}

	void addHeader(String name, String value) {
		this.headers.put(name, value)
	}

	void removeHeader(String key) {
		this.headers.remove(key)
	}

	void setClient(HttpClient client) {
		this.client = client
	}

	void shutdown() {
		this.client.getConnectionManager().shutdown()
	}

	Object get(Map<String, ?> args) {
		return doRequest(args, new HttpGet())
	}

	Object post(Map<String, ?> args) {
		return doRequest(args, new HttpPost())
	}

	Object head(Map<String, ?> args) {
		return doRequest(args, new HttpHead())
	}

	Object put(Map<String, ?> args) {
		return doRequest(args, new HttpPut())
	}

	Object patch(Map<String, ?> args) {
		return doRequest(args, new HttpPatch())
	}

	Object delete(Map<String, ?> args) {
		return doRequest(args, new HttpDelete())
	}

	Object doRequest(Map<String, ?> args, HttpRequest origReq) {
		HttpRequestBase request = buildRequest(origReq, args)

		ResponseHandler<Object> responseHandler = (HttpResponse response) -> {
			def data = null
			if (this.parser != null) {
				data = this.parser.parse(response)
			}
			def resp = new HttpResponseDecorator(response, data)
			resp
		}

		def res = this.client.execute(request, responseHandler)
		if (this.postProcessor != null) {
			res = this.postProcessor.call(res)
		}
		return res
	}

	private HttpRequestBase buildRequest(HttpRequest origReq, Map<String, ?> args) {
		def builder = new HttpRequestBuilder(origReq as HttpRequestBase, this.defaultURI, this.encoder)
		return builder.addHeaders(this.headers)
				.parseMap(args)
				.build()
	}

}
