/**
 * This file was modified by Amazon:
 * Copyright 2015-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
/*
 * Copyright (c) 2014, Nexage, Inc. All rights reserved.
 * Copyright (C) 2016 Amazon Inc.
 *
 * Provided under BSD-3 license as follows:
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *  and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Nexage nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.amazon.android.ads.vast.util;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XmlTools {
	
	private static String TAG = "XmlTools";
	
	public static void logXmlDocument(Document doc) {
		VASTLog.d(TAG, "logXmlDocument");
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer
					.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "4");

			StringWriter sw = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(sw));

			VASTLog.d(TAG, sw.toString());

		} catch (Exception e) {
			VASTLog.e(TAG, e.getMessage(), e);
		}
	}

	public static String xmlDocumentToString(Document doc) {
		String xml = null;
		VASTLog.d(TAG, "xmlDocumentToString");
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer
					.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "4");

			StringWriter sw = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(sw));

			xml = sw.toString();

		} catch (Exception e) {
			VASTLog.e(TAG, e.getMessage(), e);
		}

		return xml;
	}

	public static String xmlDocumentToString(Node node) {
		String xml = null;
		VASTLog.d(TAG, "xmlDocumentToString");
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer
					.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "4");

			StringWriter sw = new StringWriter();
			transformer.transform(new DOMSource(node), new StreamResult(sw));

			xml = sw.toString();

		} catch (Exception e) {
			VASTLog.e(TAG, e.getMessage(), e);
		}

		return xml;
	}
	public static Document stringToDocument(String doc) {
		VASTLog.d(TAG, "stringToDocument");
	
		DocumentBuilder db;
		Document document = null;
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(doc));

			document = db.parse(is);

		} catch (Exception e) {
			VASTLog.e(TAG, e.getMessage(), e);
		}
		return document;

	}

	public static String stringFromStream(InputStream inputStream)
			throws IOException {
		VASTLog.d(TAG, "stringFromStream");
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;

		while ((length = inputStream.read(buffer)) != -1) {
			baos.write(buffer, 0, length);
		}

		byte[] bytes = baos.toByteArray();

		return new String(bytes, "UTF-8");
	}

	public static String getElementValue(Node node) {
		
		NodeList childNodes = node.getChildNodes();
		Node child;
		String value = null;
		CharacterData cd;

		for (int childIndex = 0; childIndex < childNodes.getLength(); childIndex++) {
			child = childNodes.item(childIndex);
			// value = child.getNodeValue().trim();
			cd = (CharacterData) child;
			value = cd.getData().trim();

			if (value.length() == 0) {
				// this node was whitespace
				continue;
			}
		
			VASTLog.v(TAG, "getElementValue: " + value);
			return value;

		}
		return value;
	}

}
