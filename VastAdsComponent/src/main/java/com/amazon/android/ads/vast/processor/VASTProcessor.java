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
package com.amazon.android.ads.vast.processor;

import com.amazon.android.ads.vast.VASTAdsPlayer;
import com.amazon.android.ads.vast.model.VASTModel;
import com.amazon.android.ads.vast.model.VAST_DOC_ELEMENTS;
import com.amazon.android.ads.vast.util.VASTLog;
import com.amazon.android.ads.vast.util.XmlTools;
import com.amazon.android.ads.vast.util.XmlValidation;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * This class is responsible for taking a VAST 2.0 XML file, parsing it,
 * validating it, and creating a valid VASTModel object corresponding to it.
 *
 * It can handle "regular" VAST XML files as well as VAST wrapper files.
 */
public final class VASTProcessor {

    private static final String TAG = "VASTProcessor";

    // Maximum number of VAST files that can be read (wrapper file(s) + actual
    // target file)
    private static final int MAX_VAST_LEVELS = 5;
    private static final boolean IS_VALIDATION_ON = false;

    private VASTMediaPicker mediaPicker;
    private VASTModel vastModel;
    private StringBuilder mergedVastDocs = new StringBuilder(500);

    public VASTProcessor(VASTMediaPicker mediaPicker) {

        this.mediaPicker = mediaPicker;
    }

    public VASTModel getModel() {

        return vastModel;
    }

    public int process(String xmlData) {

        VASTLog.d(TAG, "process");
        vastModel = null;
        InputStream is;


        try {
            is = new ByteArrayInputStream(xmlData.getBytes(Charset
                                                                   .defaultCharset().name()));
        }
        catch (UnsupportedEncodingException e) {
            VASTLog.e(TAG, e.getMessage(), e);
            return VASTAdsPlayer.ERROR_XML_PARSE;
        }

        int error = processUri(is, 0);
        try {
            is.close();
        }
        catch (IOException e) {
            VASTLog.e(TAG, "Failed to close input stream", e);
        }
        if (error != VASTAdsPlayer.ERROR_NONE) {
            return error;
        }

        Document mainDoc = wrapMergedVastDocWithVasts();
        vastModel = new VASTModel(mainDoc);

        if (mainDoc == null) {
            return VASTAdsPlayer.ERROR_XML_PARSE;
        }


        if (!VASTModelPostValidator.validate(vastModel, mediaPicker)) {
            return VASTAdsPlayer.ERROR_POST_VALIDATION;
        }

        return VASTAdsPlayer.ERROR_NONE;
    }

    private Document wrapMergedVastDocWithVasts() {

        VASTLog.d(TAG, "wrapmergedVastDocWithVasts");
        mergedVastDocs.insert(0, "<VASTS>");
        mergedVastDocs.append("</VASTS>");

        String merged = mergedVastDocs.toString();
        VASTLog.v(TAG, "Merged VAST doc:\n" + merged);

        return XmlTools.stringToDocument(merged);
    }

    private int processUri(InputStream is, int depth) {

        VASTLog.d(TAG, "processUri");

        if (depth >= MAX_VAST_LEVELS) {
            String message = "VAST wrapping exceeded max limit of "
                    + MAX_VAST_LEVELS + ".";
            VASTLog.e(TAG, message);
            return VASTAdsPlayer.ERROR_EXCEEDED_WRAPPER_LIMIT;
        }

        Document doc = createDoc(is);
        if (doc == null) {
            return VASTAdsPlayer.ERROR_XML_PARSE;
        }

        if (IS_VALIDATION_ON) {
            if (!validateAgainstSchema(doc)) {
                return VASTAdsPlayer.ERROR_SCHEMA_VALIDATION;
            }
        }

        merge(doc);

        // check to see if this is a VAST wrapper ad
        NodeList uriToNextDoc = doc
                .getElementsByTagName(VAST_DOC_ELEMENTS.vastAdTagURI.getValue());
        if (uriToNextDoc == null || uriToNextDoc.getLength() == 0) {
            // This isn't a wrapper ad, so we're done.
            return VASTAdsPlayer.ERROR_NONE;
        }
        else {
            // This is a wrapper ad, so move on to the wrapped ad and process
            // it.
            VASTLog.d(TAG, "Doc is a wrapper. ");
            Node node = uriToNextDoc.item(0);
            String nextUri = XmlTools.getElementValue(node);
            VASTLog.d(TAG, "Wrapper URL: " + nextUri);
            InputStream nextInputStream;
            try {
                URL nextUrl = new URL(nextUri);
                nextInputStream = nextUrl.openStream();
            }
            catch (Exception e) {
                VASTLog.e(TAG, e.getMessage(), e);
                return VASTAdsPlayer.ERROR_XML_OPEN_OR_READ;
            }
            int error = processUri(nextInputStream, depth + 1);
            try {
                nextInputStream.close();
            }
            catch (IOException e) {
                VASTLog.e(TAG, "Failed to close input stream", e);
            }
            return error;
        }
    }


    private Document createDoc(InputStream is) {

        VASTLog.d(TAG, "About to create doc from InputStream");
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                                                 .newDocumentBuilder().parse(is);
            doc.getDocumentElement().normalize();
            VASTLog.d(TAG, "Doc successfully created.");
            return doc;
        }
        catch (Exception e) {
            VASTLog.e(TAG, e.getMessage(), e);
            return null;
        }
    }

    private void merge(Document newDoc) {

        VASTLog.d(TAG, "About to merge doc into main doc.");

        NodeList nl = newDoc.getElementsByTagName("VAST");

        Node newDocElement = nl.item(0);

        String doc = XmlTools.xmlDocumentToString(newDocElement);
        mergedVastDocs.append(doc);

        VASTLog.d(TAG, "Merge successful.");
    }

    // Validator using mfXerces.....
    private boolean validateAgainstSchema(Document doc) {

        VASTLog.d(TAG, "About to validate doc against schema.");
        InputStream stream = VASTProcessor.class
                .getResourceAsStream("assets/vast_2_0_1_schema.xsd");
        String xml = XmlTools.xmlDocumentToString(doc);
        boolean isValid = XmlValidation.validate(stream, xml);
        try {
            stream.close();
        }
        catch (IOException e) {
            VASTLog.e(TAG, "Failed to close input stream", e);
        }
        return isValid;
    }

}