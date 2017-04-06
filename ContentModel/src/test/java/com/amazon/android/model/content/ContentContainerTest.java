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
import java.util.Iterator;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * ContentContainerTest class is a test for ContentContainer class.
 * This class will exercise setters, getters and content iterator.
 * Code coverage is used to make sure test touches all lines and methods.
 */
public class ContentContainerTest {

    @Test
    public void testIterator() throws Exception {

        // Expected flatten content list.
        String[] expectedContentNameResultList = {
                "cr1",
                "cr2",
                "cr3",
                "cr4",
                "c41",
                "c42",
                "c43",
                "c44",
                "c45",
                "c46",
                "c3221",
                "c3222",
                "c3223",
                "c3224",
                "c3225",
                "c3211",
                "c321",
                "c322",
                "c323",
                "c311",
                "c312",
                "c221",
                "c222",
                "c223",
                "c224",
                "c225",
                "c226",
                "c227",
                "c211",
                "c212",
                "c213",
        };

        // Create a test content container.
        ContentContainer cc = new ContentContainer("root");

        // Create a sub container which has different possible layouts.
        cc.addContentContainer(ContentContainer.newInstance("level2")
                                               .addContentContainer(
                                                       ContentContainer.newInstance("level21")
                                                                       .addContent(new Content
                                                                                           ("c211"))
                                                                       .addContent(new Content
                                                                                           ("c212"))
                                                                       .addContent(new Content
                                                                                           ("c213"))
                                               )

                                               .addContentContainer(
                                                       ContentContainer.newInstance("level22")
                                                                       .addContent(new Content
                                                                                           ("c221"))
                                                                       .addContent(new Content
                                                                                           ("c222"))
                                                                       .addContent(new Content
                                                                                           ("c223"))
                                                                       .addContent(new Content
                                                                                           ("c224"))
                                                                       .addContent(new Content
                                                                                           ("c225"))
                                                                       .addContent(new Content
                                                                                           ("c226"))
                                                                       .addContent(new Content
                                                                                           ("c227"))
                                               )
        );

        // Create a sub container which has different possible layouts.
        cc.addContentContainer(ContentContainer.newInstance("level3")
                                               .addContentContainer(
                                                       ContentContainer.newInstance("level31")
                                                                       .addContent(new Content
                                                                                           ("c311"))
                                                                       .addContent(new Content
                                                                                           ("c312"))
                                               )

                                               .addContentContainer(
                                                       ContentContainer.newInstance("level32")
                                                                       .addContentContainer(
                                                                               ContentContainer
                                                                                       .newInstance("level321")
                                                                                       .addContent(new Content
                                                                                                           ("c3211"))
                                                                       )
                                                                       .addContentContainer(
                                                                               ContentContainer
                                                                                       .newInstance("level322")
                                                                                       .addContent(new Content
                                                                                                           ("c3221"))
                                                                                       .addContent(new Content
                                                                                                           ("c3222"))
                                                                                       .addContent(new Content
                                                                                                           ("c3223"))
                                                                                       .addContent(new Content
                                                                                                           ("c3224"))
                                                                                       .addContent(new Content
                                                                                                           ("c3225"))
                                                                       )
                                                                       .addContent(new Content
                                                                                           ("c321"))
                                                                       .addContent(new Content
                                                                                           ("c322"))
                                                                       .addContent(new Content
                                                                                           ("c323"))
                                               )
        );

        // Create a sub container which has different possible layouts.
        cc.addContentContainer(ContentContainer.newInstance("level4")
                                               .addContent(new Content("c41"))
                                               .addContent(new Content("c42"))
                                               .addContent(new Content("c43"))
                                               .addContent(new Content("c44"))
                                               .addContent(new Content("c45"))
                                               .addContent(new Content("c46"))
        );

        // Create a sub container which has different possible layouts.
        cc.addContentContainer(ContentContainer.newInstance("level5"));

        // Add contents under root container.
        cc.addContent(new Content("cr1"))
          .addContent(new Content("cr2"))
          .addContent(new Content("cr3"))
          .addContent(new Content("cr4"));

        //Verify results.
        int i = 0;
        for (Content c : cc) {
            // Test if value is equal to hand written array.
            assertEquals(expectedContentNameResultList[i++], c.getTitle());
        }
    }

    /**
     * Test new instance method.
     */
    @Test
    public void testNewInstance() throws Exception {
        // Test if manage to create the object.
        assertNotNull(ContentContainer.newInstance("test"));

        // Make sure toString returns some text.
        assertNotNull(ContentContainer.newInstance("test").toString());
    }

    /**
     * Test add content method.
     */
    @Test
    public void testAddContent() throws Exception {

        ContentContainer cc = new ContentContainer();
        cc.addContent(new Content("test1"));

        // Test if content added to internal list.
        assertEquals(1, cc.getContentCount());
    }

    /**
     * Test get content count method.
     */
    @Test
    public void testGetContentCount() throws Exception {

        ContentContainer cc = new ContentContainer();
        cc.addContent(new Content("test1"));
        cc.addContent(new Content("test2"));

        // Test if content count is equal to what we added.
        assertEquals(2, cc.getContentCount());
    }

    /**
     * Test get content containers method.
     */
    @Test
    public void testGetContentContainers() throws Exception {

        ContentContainer cc = new ContentContainer();
        cc.addContentContainer(ContentContainer.newInstance("subTest1"));
        cc.addContentContainer(ContentContainer.newInstance("subTest2"));
        cc.addContentContainer(ContentContainer.newInstance("subTest3"));

        // Test object is not null.
        assertNotNull(cc.getContentContainers());
        // Test get content container is giving a valid object.
        assertEquals("subTest2", cc.getContentContainers().get(1).getName());
    }

    /**
     * Test get content container count method.
     */
    @Test
    public void testGetContentContainerCount() throws Exception {

        ContentContainer cc = new ContentContainer();
        cc.addContentContainer(ContentContainer.newInstance("subTest1"));
        cc.addContentContainer(ContentContainer.newInstance("subTest2"));
        cc.addContentContainer(ContentContainer.newInstance("subTest3"));

        // Test if content container count is correct after adds.
        assertEquals(3, cc.getContentContainerCount());
    }

    /**
     * Test get child content container at index method.
     */
    @Test
    public void testGetChildContentContainerAtIndex() throws Exception {

        ContentContainer cc = new ContentContainer();
        cc.addContentContainer(ContentContainer.newInstance("subTest1"));
        cc.addContentContainer(ContentContainer.newInstance("subTest2"));
        cc.addContentContainer(ContentContainer.newInstance("subTest3"));

        // Test if container atIndex matches what we set.
        assertEquals("subTest2", cc.getChildContentContainerAtIndex(1).getName());
    }

    /**
     * Test has sub-containers method.
     */
    @Test
    public void testHasSubContainers() throws Exception {

        ContentContainer cc = new ContentContainer();
        cc.addContentContainer(ContentContainer.newInstance("subTest"));

        // Test if container has sub containers.
        assertTrue(cc.hasSubContainers());
    }

    /**
     * Test set/get name methods.
     */
    @Test
    public void testSetGetName() throws Exception {

        ContentContainer cc = new ContentContainer();
        cc.setName("test");

        // Test if name set correctly.
        assertEquals("test", cc.getName());
    }

    /**
     * Test set/get extra string value methods.
     */
    @Test
    public void testSetGetExtraStringValue() throws Exception {

        ContentContainer cc = new ContentContainer();

        // Test for null extra case.
        assertNull(cc.getExtraStringValue("key"));

        // Test for known key case.
        cc.setExtraValue("key", "test");
        assertEquals("test", cc.getExtraStringValue("key"));

        // Test for unknown key case.
        assertNull(cc.getExtraStringValue("randomKey"));
    }

    /**
     * Test iterator remove method.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testIteratorRemove() throws Exception {
        // Iterator does not support remove thus should throw an exception.
        Iterator iter = ContentContainer.newInstance("test").iterator();
        iter.remove();
    }

    /**
     * Tests the equals method.
     */
    @Test
    public void testEqualsMethod() throws Exception {

        // Create two equal containers.
        ContentContainer cc1 = new ContentContainer("name");
        cc1.setExtraValue("key", "value");
        ContentContainer cc2 = new ContentContainer("name");
        cc2.setExtraValue("key", "value");

        // Containers are equal.
        assertEquals(cc1, cc2);

        // Add a content to cc1
        cc1.addContent(new Content("content"));

        // Containers should not long be equal.
        assertFalse(cc1.equals(cc2));

        // Add a content and a content container to cc2
        cc2.addContent(new Content("content"));
        cc2.addContentContainer(new ContentContainer("sub-container"));

        // Containers should still not be equal
        assertFalse(cc1.equals(cc2));

        // Add a content container to cc1
        cc1.addContentContainer(new ContentContainer("sub-container"));

        // Now containers should be equal again.
        assertEquals(cc1, cc2);
    }
}