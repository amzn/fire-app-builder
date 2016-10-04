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
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;

import android.util.Log;

/**
 * Implements the {@link IParser} interface for the JSON format. Uses the <a
 * href="https://github.com/jayway/JsonPath">JsonPath</a> library by Jayway.
 */
public class JsonParser implements IParser {

    /**
     * String representing the type of format this parser understands.
     */
    public static final String FORMAT = "json";

    /**
     * Debug tag.
     */
    private final String TAG = JsonParser.class.getSimpleName();

    /**
     * This parser implementation requires a query, so if the user doesn't provide one just parse
     * starting at the root of the JSON data. The root is represented by '$'.
     */
    private final String DEFAULT_QUERY = "$";

    /**
     * Parses a JSON-encoded string into an object.
     *
     * @param data The JSON-encoded data string to parse.
     * @return The parsed data represented as an object.
     * @throws IllegalArgumentException If data is null or an empty string.
     * @throws InvalidDataException     If the JSON-encoded data string is malformed and cannot be
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
     * Parses a JSON-encoded string into an object. Uses the given query
     * to return only items that the query calls for. On parsing failure, null is returned.
     *
     * @param data  The JSON-encoded data string to parse.
     * @param query The parse query. An example query may look like: "$.node[?(@.type == 'item')]".
     *              The example query searches the JSON object starting at the root for a 'node'
     *              key that's value is an array. Within that array, it checks each item's 'type'
     *              value. If the value equals 'item' then its a hit, as will be returned as part
     *              of the result.
     *              <p>
     *              The query can use the following operators:
     *              <ul>
     *              <li>{@code $} - The root element to query.
     *              <li>{@code @} - The current node being processed by a filter predicate.
     *              <li>{@code *} - Wildcard. Available anywhere a name or numeric are required.
     *              <li>{@code ..} - Deep scan. Available anywhere a name is required.
     *              <li>{@code ..<name>} - Dot-notated child.
     *              <li>{@code ['<name>' (, '<name>')]} - Bracket-notated child or children.
     *              <li>{@code [<number> (, <number>)]} - Array index or indexes.
     *              <li>{@code [start:end]} - Array slice operator.
     *              <li>{@code [?(<expression>)]} - Filter expression. Expression must evaluate to a
     *              boolean value.
     *              </ul>
     * @return The parsed data represented as an object.
     * @throws IllegalArgumentException If data is null or an empty string.
     * @throws InvalidQueryException    If the query does not yield a result on the given json
     *                                  data.
     * @throws InvalidDataException     If the JSON-encoded data string is malformed and cannot be
     *                                  parsed.
     */
    @Override
    public Object parseWithQuery(String data, String query) throws
            IllegalArgumentException, InvalidQueryException, InvalidDataException {

        // Null or empty data is not allowed.
        if (data == null || data.isEmpty()) {
            Log.e(TAG, "JSON string can not be null or empty");
            throw new IllegalArgumentException("json string can not be null or empty");
        }

        // Null or empty query is not allowed.
        if (query == null || query.isEmpty()) {
            Log.e(TAG, "Query can not be null or empty");
            throw new IllegalArgumentException("query can not be null or empty");
        }

        // List to hold the results.
        Object result;

        try {
            // Try to parse the data.
            ReadContext ctx = JsonPath.parse(data);

            try {
                result = ctx.read(query);
            }
            // Catch and log an exception for an invalid query, but throw it back so the user
            // can catch it as well.
            catch (PathNotFoundException e) {
                Log.e(TAG, "The provided query string is not valid for the given json.", e);
                throw new InvalidQueryException("The provided query string is not valid for " +
                                                        "the given json: " + query, e);
            }

        }
        // Catch and log an exception for malformed JSON, but then throw it back so the user can
        // catch it as well.
        catch (InvalidJsonException e) {
            Log.e(TAG, "Error parsing JSON string.", e);
            throw new InvalidDataException("Error parsing JSON string.", e);
        }

        return result;
    }
}
