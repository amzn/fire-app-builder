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
package com.amazon.android.model.content;

import org.junit.Test;

import java.lang.Exception;
import java.util.Locale;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * ContentTest class is a test for Content class.
 * This class will exercise all the methods.
 */
public class ContentTest {

    /**
     * Test all getters and setters in single place.
     * Used code coverage to make sure we are touching all methods and lines.
     */
    @Test
    public void testGetterSetter() throws Exception {
        // Create object for setters and getters test.
        Content getterSetterTestContent = new Content();

        // Test constructor
        Content content = new Content("testTitle");
        assertEquals("testTitle", content.getTitle());

        // Test getId before setId.
        assertEquals("0", getterSetterTestContent.getId());

        // Test set/get id.
        getterSetterTestContent.setId("123");
        assertEquals("123", getterSetterTestContent.getId());

        // Test set/get title.
        getterSetterTestContent.setTitle("testTitle");
        assertEquals("testTitle", getterSetterTestContent.getTitle());

        // Test set/get subtitle.
        getterSetterTestContent.setSubtitle("testSubTitle");
        assertEquals("testSubTitle", getterSetterTestContent.getSubtitle());

        // Test set/get url.
        getterSetterTestContent.setUrl("testUrl");
        assertEquals("testUrl", getterSetterTestContent.getUrl());

        // Test set/get description.
        getterSetterTestContent.setDescription("testDesc");
        assertEquals("testDesc", getterSetterTestContent.getDescription());

        // Test set/get backgroundImageUrl.
        getterSetterTestContent.setBackgroundImageUrl("testBGImageUrl");
        assertEquals("testBGImageUrl", getterSetterTestContent.getBackgroundImageUrl());

        // Test set/get cardImageUrl.
        getterSetterTestContent.setCardImageUrl("testCardImageUrl");
        assertEquals("testCardImageUrl", getterSetterTestContent.getCardImageUrl());

        // Test set/get locale.
        getterSetterTestContent.setLocale(Locale.CANADA);
        assertEquals(Locale.CANADA, getterSetterTestContent.getLocale());

        // Test set/get tags.
        getterSetterTestContent.setTags("[\"1\",\"2\",\"3\"]");
        assertEquals("1", getterSetterTestContent.getTags().get(0));
        assertEquals("2", getterSetterTestContent.getTags().get(1));
        assertEquals("3", getterSetterTestContent.getTags().get(2));

        // Malformed Json string case
        getterSetterTestContent.setTags("{a");
        assertEquals(0, getterSetterTestContent.getTags().size());

        // Make sure toString return some text.
        assertNotNull(getterSetterTestContent.toString());
    }

    /**
     * Test set/get extra string value methods.
     */
    @Test
    public void testSetGetExtraStringValue() throws Exception {

        Content content = new Content();

        // Test for null extra case.
        assertNull(content.getExtraValue(null));

        // Test for known key case.
        content.setExtraValue("key", "test");
        assertEquals("test", content.getExtraValue("key"));

        // Test for unknown key case.
        assertNull(content.getExtraValue("randomKey"));
    }

    /**
     * Test has similar tags method
     */
    @Test
    public void testHasSimilarTag() throws Exception {

        // Create test contents.
        Content content1 = new Content();
        Content content2 = new Content();

        // Set test tag arrays.
        content1.setTags("[\"1\",\"2\",\"3\"]");
        content2.setTags("[\"3\"]");

        // Normal use case.
        assertTrue(content1.hasSimilarTags(content2));

        // Empty array case.
        content2.setTags("[]");
        assertFalse(content1.hasSimilarTags(content2));

        // Null string case.
        content2.setTags(null);
        assertFalse(content1.hasSimilarTags(content2));

        // No match case.
        content2.setTags("[\"4\"]");
        assertFalse(content1.hasSimilarTags(content2));
    }

    /**
     * Test get string field by name method.
     */
    @Test
    public void testGetStringFieldByName() throws Exception {

        // Create a test content.
        Content content = new Content();

        // Set some field values.
        content.setDescription("testDescription");
        content.setTitle("testTitle");

        // Test with known fields.
        assertEquals("testTitle", content.getStringFieldByName(Content.TITLE_FIELD_NAME));
        assertEquals("testDescription", content.getStringFieldByName(Content.DESCRIPTION_FIELD_NAME));

        // Test with unknown field name.
        assertNull(content.getStringFieldByName("randomKey"));

        // Test with null field name.
        assertNull(content.getStringFieldByName(null));
    }

    /**
     * Test search in fields method.
     */
    @Test
    public void testSearchInFields() throws Exception {

        // Create a test content.
        Content content = new Content();

        // Set some field values.
        content.setDescription("testDescription");
        content.setTitle("testTitle");

        // Positive match case.
        assertTrue(content.searchInFields("test", new String[]{Content.TITLE_FIELD_NAME,
                Content.DESCRIPTION_FIELD_NAME}));

        // No match case.
        assertFalse(content.searchInFields("123", new String[]{Content.TITLE_FIELD_NAME,
                Content.DESCRIPTION_FIELD_NAME}));

        // Null field array case.
        assertFalse(content.searchInFields("123", null));

        // Empty field array case.
        assertFalse(content.searchInFields("123", new String[]{}));
    }

    /**
     * Test {@link Content#equals(Object)}} method.
     * @throws Exception
     */
    @Test
    public void testEquals() throws Exception {

        // Create two test contents.
        Content content1 = new Content();
        Content content2 = new Content();

        // The newly created contents should be equal.
        assertTrue(content1.equals(content2));

        // Change one content
        content1.setTitle("Content1");

        // Now the contents should not be equal.
        assertFalse(content1.equals(content2));
    }
}
