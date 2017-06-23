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
package com.amazon.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link ListUtils} class.
 */
public class ListUtilsTest {

    /**
     * Tests the {@link ListUtils#getValueAsMapList(Map, String)} method.
     */
    @Test
    public void testGetValueAsMapList() {

        Map map = new HashMap<>();
        Map innerMap1 = new HashMap();
        innerMap1.put("key1", "value1");

        Map innerMap2 = new HashMap();
        innerMap2.put("key1", "value2");

        List<Map> list = new ArrayList<>();
        list.add(innerMap1);
        list.add(innerMap2);

        map.put("mapKey", innerMap1);
        map.put("listKey", list);

        List<Map<String, Map>> expected = ListUtils.getValueAsMapList(map, "mapKey");
        assertEquals(1, expected.size());

        expected = ListUtils.getValueAsMapList(map, "listKey");
        assertEquals(2, expected.size());

        expected = ListUtils.getValueAsMapList(map, "badKey");
        assertEquals(0, expected.size());

        expected = ListUtils.getValueAsMapList((Map) null, "key");
        assertEquals(0, expected.size());

        expected = ListUtils.getValueAsMapList(map, null);
        assertEquals(0, expected.size());

        expected = ListUtils.getValueAsMapList(map, "");
        assertEquals(0, expected.size());
    }

}
