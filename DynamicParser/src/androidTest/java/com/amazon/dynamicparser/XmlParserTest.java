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

import com.amazon.dynamicparser.impl.XmlParser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * This class tests the XmlParser class; an implementation of the IParser interface. The
 * following conditions are tested: null data string, empty data string, null query string, empty
 * query string, good data string, malformed data string, invalid query string and malformed
 * query string.
 */
@SuppressWarnings("unchecked")
public class XmlParserTest {
    // Create an instance of XmlParser.
    private XmlParser parser;

    private String xml1;

    private String xml2;

    private String badXml;

    @Before
    public void setUp() throws Exception {

        // Instantiate a new instance of the XML parser.
        parser = new XmlParser();

        xml1 = "<doc><title>xml example</title><p>paragraph1</p><p>paragraph2</p></doc>";

        xml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
                "<rss version=\"2.0\">" +
                "<channel>" +
                "  <title>Song title</title>" +
                "  <link>http://www.foo.com/</link>" +
                "  <description>Media RSS example</description>" +
                "  <item>" +
                "    <title>Cool song by an artist</title>" +
                "    <link>http://www.foo.com/item1.html</link>" +
                "    <media:group>" +
                "      <media:content url=\"http://www.foo.com/song64kbps.mp3\"" +
                "        fileSize=\"1000\" bitrate=\"64\" type=\"audio/mpeg\"" +
                "        isDefault=\"true\" expression=\"full\" />" +
                "      <media:content url=\"http://www.foo.com/song128kbps.mp3\"" +
                "        fileSize=\"2000\" bitrate=\"128\" type=\"audio/mpeg\"" +
                "        expression=\"full\" />" +
                "      <media:content url=\"http://www.foo.com/song256kbps.mp3\"" +
                "        fileSize=\"4000\" bitrate=\"256\" type=\"audio/mpeg\"" +
                "        expression=\"full\" />" +
                "      <media:content url=\"http://www.foo.com/song.wav\"" +
                "        fileSize=\"16000\" type=\"audio/x-wav\" expression=\"full\" />" +
                "      <media:credit role=\"musician\">band member 1</media:credit>" +
                "      <media:credit role=\"musician\">band member 2</media:credit>" +
                "      <media:category>music</media:category>" +
                "      <media:rating>non-adult</media:rating>" +
                "    </media:group>" +
                "  </item>" +
                "</channel>" +
                "</rss>";

        badXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
                "<rss version=\"2.0\">" +
                "<channel>" +
                "    <media:content>bad xml</content>" +
                "</channel>" +
                "</rss>";
    }

    @After
    public void tearDown() throws Exception {

        parser = null;
    }

    /**
     * Test the good data case for {@link XmlParser#parse(String)}
     */
    @Test
    public void testParse() throws Exception {

        // Test good XML string
        Map<String, Object> result = (Map<String, Object>) parser.parse(xml1);
        List paragraphs = (List) result.get("p");
        assertTrue("The result should contains a list with 2 items", paragraphs.size() == 2);
    }

    /**
     * Test the null case for {@link XmlParser#parse(String)}
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseWithNull() throws Exception {

        parser.parse(null);
    }

    /**
     * Test the empty input case for {@link XmlParser#parse(String)}
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseWithEmptyInput() throws Exception {

        parser.parse("");
    }

    /**
     * Test the malformed input case for {@link XmlParser#parse(String)}
     */
    @Test(expected = IParser.InvalidDataException.class)
    public void testParseMalformedString() throws Exception {

        parser.parse(badXml);
    }

    /**
     * This method tests the {@link XmlParser#parseWithQuery(String, String)} method.
     */
    @Test
    public void testParseWithQuery() throws Exception {

        // Test good XML string, good query
        final String query1 = "rss/channel/item/group/content[1]";
        Map<String, Object> result = (Map<String, Object>) parser.parseWithQuery(xml2, query1);
        Map<String, String> attributes = (Map<String, String>) result.get("#attributes");
        String type = attributes.get("type");
        assertTrue("The type of the content should be \"audio/mpeg\"", type.equals("audio/mpeg"));

        final String query2 = "rss/channel/item/group/category";
        result = (Map<String, Object>) parser.parseWithQuery(xml2, query2);
        assertTrue("The category should be \"music\"", result.get("#text").equals("music"));


    }
    /**
     * Test the null case for both inputs for {@link XmlParser#parseWithQuery(String, String)}
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseQueryWithNullInputs() throws Exception {

        parser.parseWithQuery(null, null);
    }

    /**
     * Test the null case for the query input for {@link XmlParser#parseWithQuery(String, String)}
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseQueryWithNullQuery() throws Exception {

        parser.parseWithQuery(xml1, null);
    }

    /**
     * Test the empty query case for {@link XmlParser#parseWithQuery(String, String)}
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseQueryWithEmptyInput() throws Exception {

        parser.parseWithQuery(xml1, "");
    }

    /**
     * Test the bad query case for {@link XmlParser#parseWithQuery(String, String)}
     */
    @Test(expected = IParser.InvalidQueryException.class)
    public void testParseQueryWithBadQuery() throws Exception {

        parser.parseWithQuery(xml1, ".item");
    }

    /**
     * Test the empty result case for {@link XmlParser#parseWithQuery(String, String)}
     */
    @Test(expected = IParser.InvalidQueryException.class)
    public void testParseWithInvalidQuery() throws  Exception {
        parser.parseWithQuery(xml1, "doc/p[3]");
    }
}
