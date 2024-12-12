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

import groovy.json.JsonSlurper
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils
import org.codehaus.groovy.runtime.MethodClosure
import org.xml.sax.SAXException

import javax.xml.parsers.ParserConfigurationException

/**
 * Type aware http response parser implementation.
 * Customize by composition and delegates to this class, instead of subclass.
 */
class TypeAwareResponseParser implements HttpResponseParser {

	protected static final Log log = LogFactory.getLog(TypeAwareResponseParser.class)
	public static final String DEFAULT_CHARSET = "UTF-8";

	private String defaultCharset = DEFAULT_CHARSET;

	private Map<String, Closure> registeredParsers

	TypeAwareResponseParser() {
		registeredParsers = buildDefaultParserMap()
	}

	@Override
	Object parse(final HttpResponse resp) {
		def data = null
		if (resp.getEntity() != null && resp.getEntity().getContentLength() != 0) {
			data = this.parseData(resp)
		}
		data
	}

	Object parseData(final HttpResponse resp) {
		String contentType = getContentType(resp)
		Closure parser = this.getByType(contentType);
		if (parser == null) {
			log.warn("No parser found for " + contentType)
			return null
		} else {
			return parser.call(resp)
		}
	}

	void setDefaultCharset(String charset) {
		defaultCharset = charset == null ? DEFAULT_CHARSET : charset;
	}

	String getCharset(HttpResponse resp) {
		def type = org.apache.http.entity.ContentType.getOrDefault(resp.getEntity())
		return type.getCharset().toString()
	}

	String getContentType(HttpResponse resp) {
		def type = org.apache.http.entity.ContentType.getOrDefault(resp.getEntity())
		return type.getMimeType()
	}

	String parseTextToString(HttpResponse resp) throws IOException {
		def buffer = new StringWriter()
		def reader = parseTextToReader(resp)
		buffer << reader
		return buffer.toString()
	}

	StringReader parseTextToReader(HttpResponse resp) {
		def reader = new InputStreamReader(resp.getEntity().getContent(),
				this.getCharset(resp))
		def buffer = new StringWriter()
		buffer << reader
		return new StringReader(buffer.toString())
	}

	InputStream parseStream(HttpResponse resp) throws IOException {
		def data = resp.getEntity().getContent()
		def buffer = new ByteArrayOutputStream()
		buffer << (InputStream) data
		return new ByteArrayInputStream(buffer.toByteArray())
	}

	Map<String, String> parseForm(final HttpResponse resp) throws IOException {
		HttpEntity entity = resp.getEntity();
		List<NameValuePair> params = URLEncodedUtils.parse(entity)
		Map<String, String> paramMap = new HashMap<>(params.size())
		params.each {
			paramMap.put(it.getName(), it.getValue())
		}
		return paramMap;
	}

	GPathResult parseXML(HttpResponse resp) throws IOException, SAXException, ParserConfigurationException {
		def parsedXml = new XmlSlurper().parseText(parseTextToString(resp))
		return parsedXml
	}

	Object parseJSON(HttpResponse resp) throws IOException {
		return new JsonSlurper().parseText(parseTextToString(resp));
	}

	protected Map<String, Closure> buildDefaultParserMap() {
		Map<String, Closure> parsers = new HashMap<String, Closure>();

		parsers.put(ContentType.TEXT, new MethodClosure(this, "parseTextToReader"));
		parsers.put(ContentType.HTML, new MethodClosure(this, "parseTextToReader"));
		parsers.put(ContentType.URLENC, new MethodClosure(this, "parseForm"));
		parsers.put(ContentType.XML, new MethodClosure(this, "parseXML"));
		parsers.put(ContentType.JSON, new MethodClosure(this, "parseJSON"));
		parsers.put(ContentType.BINARY, new MethodClosure(this, "parseStream"));
		return parsers;
	}

	Closure getByType(String contentType) {

		String ct = contentType
		int idx = ct.indexOf(';');
		if (idx > 0) ct = ct.substring(0, idx);

		Closure parser = registeredParsers.get(ct);
		if (parser != null) return parser;

		log.warn("Cannot find parser for content-type: " + ct)
		return null;
	}

	void putAt(Object contentType, Closure value) {
		this.registeredParsers.put(contentType.toString(), value);
	}

	void propertyMissing(String key, Closure value) {
		this.registeredParsers.put(key, value);
	}

	Iterator<Map.Entry<String, Closure>> iterator() {
		return this.registeredParsers.entrySet().iterator();
	}
}
