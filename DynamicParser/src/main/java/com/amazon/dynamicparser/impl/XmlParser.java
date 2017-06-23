/**
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
package com.amazon.dynamicparser.impl;

import com.amazon.dynamicparser.IParser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import android.util.Log;

/**
 * Implements the {@link IParser} interface for the XML format. Uses XPath library.
 */
public class XmlParser implements IParser {

    /**
     * String representing the type of format this parser understands.
     */
    public static final String FORMAT = "xml";

    /**
     * Constant name tag for node text field.
     */
    public static final String TEXT_TAG = "#text";

    /**
     * Constant name tag for node cdata field.
     */
    public static final String CDATA_TAG = "#cdata-section";

    /**
     * Constant name tag for node attributes.
     */
    public static final String ATTRIBUTES_TAG = "#attributes";

    /**
     * Debug tag.
     */
    private final String TAG = XmlParser.class.getSimpleName();

    /**
     * This parser implementation requires a query, so if the user doesn't provide one just parse
     * starting at the root node of the XML data.
     */
    private final String DEFAULT_QUERY = "*";

    /**
     * Parses a XML-encoded string into an object.
     *
     * @param data The XML-encoded data string to parse.
     * @return The parsed data represented as an object.
     * @throws IllegalArgumentException If data is null or an empty string.
     * @throws InvalidDataException     If the XML-encoded data string is malformed and cannot be
     *                                  parsed.
     */
    @Override
    public Object parse(String data) throws IllegalArgumentException,
            InvalidDataException {

        try {
            return parseWithQuery(data, DEFAULT_QUERY);
        }
        // This should never happen because the default query should always work, but since
        // parseWithQuery throws this exception we have to at least catch it.
        catch (InvalidQueryException e) {
            Log.e(TAG, "Query was invalid: " + DEFAULT_QUERY, e);
        }
        return null;
    }

    /**
     * Parses a XML-encoded string into an object. Uses the given query
     * to return only items that the query calls for. On parsing failure, null is returned.
     *
     * @param data  The XML-encoded data string to parse.
     * @param query The parse query. An example query may look like: "/node/item[index]".
     *              The example query searches the XML object from 'node' element for an 'item'
     *              element that's value is an array. Within that array, it selects the element
     *              at position 'index'.
     *              <p>
     *              The query can use the following operators:
     *              <ul>
     *              <li>name - Selects all nodes with the name "name".
     *              <li>/ - Selects from the root node.
     *              <li>// - Selects nodes in the document from the current node that match the
     *              selection no matter where they are.
     *              <li>. - Selects the current node.
     *              <li>.. - Selects the parent of the current node.
     *              <li>@ - Selects attributes
     *              <li>* - Matches any element node
     *              <li>@* - Matches any attribute node
     *              <li>text() - Selects the text node
     *              <li>node() - Selects any type of child node
     *              <li>[<number>] - Selects that element node with a position index. Please
     *              note that the index starts from 1.
     *              <li>[@<name>] - Selects element nodes that match an attribute name.
     *              <li>[<expression>] -  Filter expression. Expression must evaluate to a
     *              boolean value.
     *              </ul>
     * @return The parsed data represented as an object.
     * @throws IllegalArgumentException If data is null or an empty string.
     * @throws InvalidQueryException    If the query does not yield a result on the given XML
     *                                  data.
     * @throws InvalidDataException     If the XML-encoded data string is malformed and cannot be
     *                                  parsed.
     */
    @Override
    public Object parseWithQuery(String data, String query) throws
            IllegalArgumentException, InvalidQueryException, InvalidDataException {

        // Null or empty data is not allowed.
        if (data == null || data.isEmpty()) {
            Log.e(TAG, "XML string can not be null or empty");
            throw new IllegalArgumentException("xml string can not be null or empty");
        }

        // Null or empty query is not allowed.
        if (query == null || query.isEmpty()) {
            Log.e(TAG, "Query can not be null or empty");
            throw new IllegalArgumentException("query can not be null or empty");
        }

        // Document to hold XML data.
        Document doc;

        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = docBuilder.parse(new InputSource(new StringReader(data)));
        }
        // Catch and log an exception for malformed XML data, then throw it back so the user can
        // catch it as well.
        catch (Exception e) {
            Log.e(TAG, "Error parsing XML string.", e);
            throw new InvalidDataException("Error parsing XML string.", e);
        }

        // XPath to select target nodes from the document.
        XPath xpath = XPathFactory.newInstance().newXPath();

        // NodeList to hold the selected nodes
        NodeList root;

        try {
            root = (NodeList) xpath.evaluate(query, doc, XPathConstants.NODESET);
        }
        // Catch and log an exception from an invalid query string, then throw it back so the user
        // can catch it as well.
        catch (XPathExpressionException e) {
            Log.e(TAG, "The provided query string is not valid.", e);
            throw new InvalidQueryException("The provided query string is not valid: " + query, e);
        }

        // Translate the NodeList to a map.
        Map<String, Object> map = translateNodeListToMap(root);

        // Object to hold the result.
        Object result;

        // If the map contains more than one entry, add them to a list.
        if (map.size() > 1) {

            List<Object> list = new ArrayList<>();

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                list.add(entry.getValue());
            }
            result = list;
        }
        // Otherwise, get the value of the first entry.
        else {

            try {
                result = map.entrySet().iterator().next().getValue();
            }
            // Catch and log the exception from an empty map, then throw it back so the user can
            // catch it as well.
            catch (NoSuchElementException e) {
                Log.e(TAG, "The provided query string is not valid for the given xml.", e);
                throw new InvalidQueryException("The provided query string is not valid for " +
                                                        "the given xml: " + query, e);
            }
        }

        return result;
    }

    /**
     * This is a private helper method that handles the translation of the NodeList to map.
     *
     * @param root The node list to convert into a Map<String, Object>.
     * @return A new HashMap<String, Object>.
     */
    private Map<String, Object> translateNodeListToMap(NodeList root) {

        // Map to hold the result.
        Map<String, Object> map = new HashMap<>();

        for (int i = 0; i < root.getLength(); i++) {

            Node node = root.item(i);

            // Value to add to the map entry.
            Object value;

            // If the node is a plain text node, assign its text content to 'value'.
            if (node.getNodeName().equals(TEXT_TAG) || node.getNodeName().equals(CDATA_TAG) ) {
                value = node.getTextContent();
            }
            // Otherwise, recursively add its child nodes to the result.
            else {

                // Map to hold the child nodes and attributes.
                Map<String, Object> sub;

                // Traverse the list of its child nodes.
                sub = translateNodeListToMap(node.getChildNodes());

                // If the node does not contain any plain text node, add an empty string
                // to its text content field.
                if (!sub.containsKey(TEXT_TAG)) {
                    sub.put(TEXT_TAG, "");
                }

                // If the node has attributes, get its attributes and add them to a new map tagged
                // with '#attributes'.
                if (node.hasAttributes()) {

                    // Get attributes of node.
                    NamedNodeMap attributes = node.getAttributes();

                    // Map to hold its attributes.
                    Map<String, Object> attributeMap = new HashMap<>();

                    for (int j = 0; j < attributes.getLength(); j++) {

                        Node attribute = attributes.item(j);
                        attributeMap.put(attribute.getNodeName(), attribute.getNodeValue());
                    }

                    sub.put(ATTRIBUTES_TAG, attributeMap);
                }

                value = sub;
            }

            // If the NodeList contains nodes with the same node name, merge them into one list.
            if (map.containsKey(node.getNodeName())) {

                List<Object> list;

                if (map.get(node.getNodeName()) instanceof List) {
                    list = (List<Object>) map.get(node.getNodeName());
                }
                else {

                    list = new ArrayList<>();
                    // Add the first object in the new list.
                    list.add(map.get(node.getNodeName()));
                }

                list.add(value);
                map.put(node.getNodeName(), list);
            }
            else {
                map.put(node.getNodeName(), value);
            }
        }

        return map;
    }
}
