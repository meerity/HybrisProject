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
package groovyx.net.http

import org.apache.http.Header
import org.apache.http.HttpResponse

/**
 * @Deprecated. Use {@link com.epam.training.test.test.groovy.webservicetests.http.HttpResponseDecorator} instead.
 */
@Deprecated(since = "2211.11", forRemoval = true)
class HttpResponseDecorator implements HttpResponse {

    @Delegate
    HttpResponse responseBase;

    HeadersDecorator headers = null;

    Object responseData;

    HttpResponseDecorator( HttpResponse base, Object data ) {
        this.responseBase = base;
        this.responseData = data;
    }

    int getStatus() {
        return responseBase.getStatusLine().getStatusCode();
    }

    String getContentType() {
        return responseBase.getEntity().getContentType().getElements()[0].getName()
    }

    Object getData() { return this.responseData; }

    void setData( Object responseData ) { this.responseData = responseData; }

    HeadersDecorator getHeaders() {
        if ( headers == null ) headers = new HeadersDecorator(this.responseBase);
        return headers;
    }

    final class HeadersDecorator implements Iterable<Header> {

        private HttpResponse baseResponse;

        public HeadersDecorator(HttpResponse httpResponse)
        {
            this.baseResponse = httpResponse;
        }

        /**
         * Access the named header value, using bracket form.  For example,
         * <code>response.headers['Content-Encoding']</code>
         * @see HttpResponse#getFirstHeader(String)
         * @param name header name, e.g. <code>Content-Type<code>
         * @return the {@link Header}, or <code>null</code> if it does not exist
         *  in this response
         */
        public Header getAt( String name ) {
            return this.baseResponse.getFirstHeader( name );
        }

        /**
         * Allow property-style access to header values.  This is the same as
         * {@link #getAt(String)}, except it simply returns the header's String
         * value, instead of the Header object.
         *
         * @param name header name, e.g. <code>Content-Type<code>
         * @return the {@link Header}, or <code>null</code> if it does not exist
         *  in this response
         */
        protected String propertyMissing( String name ) {
            Header h = this.getAt(name)
            return h != null ? h.getValue() : null
        }

        /**
         * Used to allow Groovy iteration methods over the response headers.
         * For example:
         * <pre>response.headers.each {
         *   println "${it.name} : ${it.value}"
         * }</pre>
         */
        @SuppressWarnings("unchecked")
        public Iterator iterator() {
            return this.baseResponse.headerIterator();
        }
    }

}
