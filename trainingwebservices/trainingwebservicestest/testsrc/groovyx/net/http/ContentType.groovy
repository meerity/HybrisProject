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

import org.springframework.http.MediaType;

/**
 * Content type constant values used for Spock tests.
 * Raw string type is used for extendable. New type value string can be directly used in test cases.
 */
@Deprecated(since = "2211.11", forRemoval = true)
public enum ContentType {
    JSON (MediaType.APPLICATION_JSON_VALUE),
    XML(MediaType.APPLICATION_XML_VALUE),
    HTML(MediaType.TEXT_HTML_VALUE),
    URLENC(MediaType.APPLICATION_FORM_URLENCODED_VALUE),
    TEXT(MediaType.TEXT_PLAIN_VALUE),
    BINARY(MediaType.APPLICATION_OCTET_STREAM_VALUE);

    private String type;

    private ContentType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
