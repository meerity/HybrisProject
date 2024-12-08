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
package groovyx.net.http;

import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * Enumeration of valid HTTP methods that may be used in a
 * {@link HTTPBuilder#request(Method, groovy.lang.Closure) request} call.
 * @author <a href='mailto:tomstrummer+httpbuilder@gmail.com'>Tom Nichols</a>
 */
@Deprecated(since = "2211.11", forRemoval = true)
public enum Method {
    GET( HttpGet.class ),
    PUT( HttpPut.class ),
    POST( HttpPost.class ),
    DELETE( HttpDelete.class ),
    HEAD( HttpHead.class ),
    PATCH( HttpPatch.class );

    private final Class<? extends HttpRequestBase> requestType;

    /**
     * Get the HttpRequest class that represents this request type.
     * @return a non-abstract class that implements {@link HttpRequest}
     */
    public Class<? extends HttpRequestBase> getRequestType() { return this.requestType; }

    private Method( Class<? extends HttpRequestBase> type ) {
        this.requestType = type;
    }
}
