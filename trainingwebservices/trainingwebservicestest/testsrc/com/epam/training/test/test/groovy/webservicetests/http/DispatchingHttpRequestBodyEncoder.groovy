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

import org.apache.http.HttpEntity

class DispatchingHttpRequestBodyEncoder implements HttpRequestBodyEncoder {

	Map<String, Object> customEncoders = new HashMap<>()

	HttpRequestBodyEncoder defaultEncoder = new DefaultHttpRequestBodyEncoder()

	@Override
	HttpEntity encodeBody(final Object body, final String contentType) {
		def customEncoder = customEncoders.get(contentType)

		/**
		 * accept closure as encoder For backward compatibility since there are test cases that register their encoder as closure.
		 */
		if (customEncoder instanceof Closure) {
			def closure = (Closure) customEncoder
			HttpEntity entity = closure.getMaximumNumberOfParameters() == 2
					? (HttpEntity) closure.call(new Object[]{body, contentType})
					: (HttpEntity) closure.call(body)
			return entity
		} else if (customEncoder instanceof HttpRequestBodyEncoder) {
			return ((HttpRequestBodyEncoder) customEncoder).encodeBody(body, contentType)
		} else {
			return defaultEncoder.encodeBody(body, contentType)
		}
	}

	public void putAt(String contentType, Object encoder) {
		customEncoders.put(contentType, encoder)
	}

	public Object getAt(String contentType) {
		return customEncoders.get(contentType)
	}
}
