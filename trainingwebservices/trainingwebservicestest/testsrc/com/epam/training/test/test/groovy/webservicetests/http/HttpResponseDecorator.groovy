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

import org.apache.http.HttpResponse

/**
 * Decorator of raw HttpResponse. It contains responseData which can be parsed as Object so that it can be accessed directly as "response.data" attribute.
 * In some case e.g. with text/plain content type, the responseData would be just the original response entity body text.
 * Usually when no parser is provided, the responseData property will be null since there is no way to get parsed data.
 * It also provides the "status", "contentType" attribute access through getter methods.
 */
class HttpResponseDecorator implements HttpResponse {

	@Delegate
	HttpResponse responseBase;

	Object responseData;

	HttpResponseDecorator(HttpResponse base, Object data) {
		this.responseBase = base;
		this.responseData = data;
	}

	int getStatus() {
		return responseBase.getStatusLine().getStatusCode();
	}

	String getContentType() {
		return responseBase.getEntity().getContentType().getElements()[0].getName()
	}

	HttpResponse getBase() {
		return this.responseBase
	}

	Object getData() { return this.responseData; }

	void setData(Object responseData) { this.responseData = responseData; }
}
