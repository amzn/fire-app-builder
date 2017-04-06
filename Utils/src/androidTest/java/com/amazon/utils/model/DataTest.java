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
package com.amazon.utils.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;

/**
 * Test cases for {@link Data}
 */
public class DataTest {

    /**
     * Tests equals, toString and hashcode of Data.
     *
     * @throws JSONException if there's a problem with creating a {@link JSONObject}.
     */
    @Test
    public void testDataEqualsAndHashCode() throws JSONException {

        Data data = new Data();
        assertTrue(data.equals(data));
        assertEquals(data.hashCode(), data.hashCode());
        new JSONObject(data.toString());

        Data data2 = new Data();
        assertTrue(data.equals(data2));
        assertEquals(data.hashCode(), data2.hashCode());
        new JSONObject(data.toString());

        Data.Record record = new Data.Record();
        record.setPayload("testString");
        data.setContent(record);
        assertFalse(data.equals(data2));
        assertFalse(data.hashCode() == data2.hashCode());

        data2.setContent(record);
        assertTrue(data.equals(data2));
        assertEquals(data.hashCode(), data2.hashCode());
        new JSONObject(data.toString());

        data.setIsComplete(true);
        assertFalse(data.equals(data2));
        assertNotSame(data.hashCode(), data2.hashCode());

        data2.setIsComplete(true);
        assertTrue(data.equals(data2));
        assertEquals(data.hashCode(), data2.hashCode());
        new JSONObject(data.toString());

        data.setMetadata(record);
        assertFalse(data.equals(data2));
        assertNotSame(data.hashCode(), data2.hashCode());

        data2.setMetadata(record);
        assertTrue(data.equals(data2));
        assertEquals(data.hashCode(), data2.hashCode());
        new JSONObject(data.toString());

        data.setRequestId("testString");
        assertFalse(data.equals(data2));
        assertNotSame(data.hashCode(), data2.hashCode());

        data2.setRequestId("testString");
        assertTrue(data.equals(data2));
        assertEquals(data.hashCode(), data2.hashCode());
        new JSONObject(data.toString());

        data.setDownloadedTimeInMs(10000);
        assertTrue(data.equals(data2));
        assertEquals(data.hashCode(), data2.hashCode());
        new JSONObject(data.toString());
    }

    /**
     * Tests equals, toString and hashcode of Record.
     *
     * @throws JSONException if there's a problem with creating a {@link JSONObject}.
     */
    @Test
    public void testRecordEqualsAndHashCode() throws JSONException {

        Data.Record record = new Data.Record();
        assertTrue(record.equals(record));
        assertTrue(record.hashCode() == record.hashCode());
        new JSONObject(record.toString());

        Data.Record record2 = new Data.Record();
        assertTrue(record.equals(record2));
        assertTrue(record.hashCode() == record2.hashCode());
        new JSONObject(record.toString());

        record.setPayload("testString");
        assertFalse(record.equals(record2));
        assertFalse(record.hashCode() == record2.hashCode());

        record2.setPayload("testString");
        assertTrue(record.equals(record2));
        assertTrue(record.hashCode() == record2.hashCode());
        new JSONObject(record.toString());

        record.setDataType(Data.DataType.JSON);
        assertFalse(record.equals(record2));
        assertFalse(record.hashCode() == record2.hashCode());

        record2.setDataType(Data.DataType.JSON);
        assertTrue(record.equals(record2));
        assertTrue(record.hashCode() == record2.hashCode());
        new JSONObject(record.toString());

        record.setHashValue("randomValue");
        assertFalse(record.equals(record2));
        assertFalse(record.hashCode() == record2.hashCode());

        record2.setHashValue("randomValue");
        assertTrue(record.equals(record2));
        assertTrue(record.hashCode() == record2.hashCode());
        new JSONObject(record.toString());

        record.setPayloadSizeInBytes(10000);
        assertFalse(record.equals(record2));
        assertFalse(record.hashCode() == record2.hashCode());

        record2.setPayloadSizeInBytes(10000);
        assertTrue(record.equals(record2));
        assertTrue(record.hashCode() == record2.hashCode());
        new JSONObject(record.toString());
    }

}