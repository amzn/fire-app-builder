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
package com.amazon.dynamicparser;

import java.util.List;
import java.util.Map;

/**
 * Parser interface. Classes should implement this interface using parsers for a given data type,
 * such as JSON or XML.
 */
public interface IParser {

    /**
     * Parses a data string into an object.
     *
     * @param data The data to parse. The format of this data should be recognizable by the type of
     *             parser that implements this method.
     * @return The parsed data represented as an object.
     * @throws IllegalArgumentException if data is null or an empty string.
     * @throws InvalidDataException     if the data string is malformed and cannot be parsed.
     */
    Object parse(String data) throws IllegalArgumentException,
            InvalidDataException;

    /**
     * Parses a data string into an object. Uses the given query to return only
     * items that the query calls for.
     *
     * @param data  The data to parse. The format of this data should be recognizable by the type
     *              of parser that implements this method.
     * @param query The parse query. The format of this query should be recognizable by the type of
     *              parser that implements this method.
     * @return The parsed data represented as a {@link List} of {@link Map}s.
     * @throws IllegalArgumentException if data is null or an empty string.
     * @throws InvalidQueryException    if the query does not yield a result on the given json
     *                                  data.
     * @throws InvalidDataException     if the data string is malformed and cannot be parsed.
     */
    Object parseWithQuery(String data, String query) throws
            IllegalArgumentException, InvalidQueryException, InvalidDataException;


    /**
     * An exception class to for invalid query strings.
     */
    class InvalidQueryException extends Exception {

        /**
         * Constructor for the exception.
         *
         * @param message The custom message to display.
         * @param e       The throwable exception that took place.
         */
        public InvalidQueryException(String message, Throwable e) {

            super(message, e);
        }
    }

    /**
     * An exception class for invalid data strings.
     */
    class InvalidDataException extends Exception {

        /**
         * Constructor for the exception.
         *
         * @param message The custom message to display.
         * @param e       The throwable exception that took place.
         */
        public InvalidDataException(String message, Throwable e) {

            super(message, e);
        }
    }
}
