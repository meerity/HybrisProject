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

import com.epam.training.test.test.groovy.webservicetests.http.SimpleHttpClient

/**
 * REST client that holds client configuration for re-use across different requests.
 *
 *  @Deprecated use {@link com.epam.training.test.test.groovy.webservicetests.http.SimpleHttpClient} instead.
 */
@Deprecated(since = "2211.11", forRemoval = true)
public class RESTClient {

    @Delegate
    SimpleHttpClient simpleHttpClient

    public RESTClient() {
        this(null)
    }

    /**
     *
     * @param defaultURI default request URI. Only String is supported.
     * @throws URISyntaxException
     */
    public RESTClient(Object defaultURI ) throws URISyntaxException {
        simpleHttpClient = new SimpleHttpClient( null, defaultURI as String );
        simpleHttpClient.postProcessor = resp -> {
            return new HttpResponseDecorator(resp.base, resp.data)
        }
    }

}
