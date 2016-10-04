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
package com.amazon.dynamicparser.testResources;

import com.amazon.dynamicparser.DynamicParser;
import com.amazon.dynamicparser.IParser;

import java.util.List;
import java.util.Map;

/**
 * This is a dummy implementation of the {@link IParser} interface used to test {@link
 * DynamicParser}.
 */
public class DummyParser implements IParser {

    /**
     * The format that this parser handles.
     */
    public static final String FORMAT = "dummy";

    /**
     * {@inheritDoc}
     *
     * @param data The data to parse. The format of this data should be recognizable by the type of
     *             parser that implements this method.
     */
    @Override
    public List<Map<String, Object>> parse(String data) throws IllegalArgumentException,
            InvalidDataException {

        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @param data  The data to parse. The format of this data should be recognizable by the type
     *              of parser that implements this method.
     * @param query The parse query. The format of this query should be recognizable by the type of
     *              parser that implements this method.
     */
    @Override
    public List<Map<String, Object>> parseWithQuery(String data, String query) throws
            IllegalArgumentException, InvalidQueryException, InvalidDataException {

        return null;
    }
}
