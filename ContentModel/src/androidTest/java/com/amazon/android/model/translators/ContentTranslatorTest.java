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
package com.amazon.android.model.translators;

import com.amazon.android.model.content.Content;
import com.amazon.android.recipe.Recipe;
import com.amazon.android.utils.FileHelper;

import org.junit.Before;
import org.junit.Test;

import android.support.test.InstrumentationRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * This class tests the {@link ContentTranslator} class.
 */
public class ContentTranslatorTest {

    ContentTranslator mContentTranslator;
    Recipe mGoodRecipe;

    @Before
    public void setUp() throws Exception {

        mContentTranslator = new ContentTranslator();

        mGoodRecipe = Recipe.newInstance(
                FileHelper.readFile(InstrumentationRegistry.getContext(), "GoodContentRecipe" +
                        ".json"));

    }

    /**
     * Tests the {@link ContentTranslator#setMemberVariable(Content, String, Object)} method for
     * the title member variable.
     */
    @Test
    public void testSetMemberVariableTitle() throws Exception {

        String titleText = "Good Content";
        Content content = new Content();
        assertTrue(mContentTranslator.setMemberVariable(content, "mTitle", titleText));
        assertEquals(content.getTitle(), titleText);
    }

    /**
     * Tests the {@link ContentTranslator#setMemberVariable(Content, String, Object)} method for
     * the id member variable.
     */
    @Test
    public void testSetMemberVariableId() throws Exception {

        Content content = new Content();
        assertTrue(mContentTranslator.setMemberVariable(content, "mId", "1"));
        assertEquals(content.getId(), "1");
    }

    /**
     * Tests the {@link ContentTranslator#setMemberVariable(Content, String, Object)} method for
     * the description member variable.
     */
    @Test
    public void testSetMemberVariableDescription() throws Exception {

        String descriptionText = "some description text";
        Content content = new Content();
        assertTrue(mContentTranslator.setMemberVariable(content, "mDescription", descriptionText));
        assertEquals(content.getDescription(), descriptionText);
    }

    /**
     * Tests the {@link ContentTranslator#setMemberVariable(Content, String, Object)} method for
     * the subtitle member variable.
     */
    @Test
    public void testSetMemberVariableSubtitle() throws Exception {

        String subtitleText = "A subtitle";
        Content content = new Content();
        assertTrue(mContentTranslator.setMemberVariable(content, "mSubtitle", subtitleText));
        assertEquals(content.getSubtitle(), subtitleText);
    }

    /**
     * Tests the {@link ContentTranslator#setMemberVariable(Content, String, Object)} method for
     * the url member variable.
     */
    @Test
    public void testSetMemberVariableUrl() throws Exception {

        String urlText = "www.someUrl.com";
        Content content = new Content();
        assertTrue(mContentTranslator.setMemberVariable(content, "mUrl", urlText));
        assertEquals(content.getUrl(), urlText);
    }

    /**
     * Tests the {@link ContentTranslator#setMemberVariable(Content, String, Object)} method for
     * the cardImageUrl member variable.
     */
    @Test
    public void testSetMemberVariableCardImageUrl() throws Exception {

        String urlText = "www.someUrl.com";
        Content content = new Content();
        assertTrue(mContentTranslator.setMemberVariable(content, "mCardImageUrl", urlText));
        assertEquals(content.getCardImageUrl(), urlText);
    }

    /**
     * Tests the {@link ContentTranslator#setMemberVariable(Content, String, Object)} method for
     * the backgroundImageUrl member variable.
     */
    @Test
    public void testSetMemberVariableBackgroundImageUrl() throws Exception {

        String urlText = "www.someUrl.com";
        Content content = new Content();
        assertTrue(mContentTranslator.setMemberVariable(content, "mBackgroundImageUrl", urlText));
        assertEquals(content.getBackgroundImageUrl(), urlText);
    }

    /**
     * Tests the {@link ContentTranslator#setMemberVariable(Content, String, Object)} method for
     * the tags member variable.
     */
    @Test
    public void testSetMemberVariableTags() throws Exception {

        String tags = "['tag1', 'tag2', 'tag3']";
        Content content = new Content();
        assertTrue(mContentTranslator.setMemberVariable(content, "mTags", tags));
        assertEquals(content.getTags().size(), 3);
    }

    /**
     * Tests the {@link ContentTranslator#setMemberVariable(Content, String, Object)} method for
     * the extras member variable.
     */
    @Test
    public void testSetMemberVariableExtras() throws Exception {

        Content content = new Content();
        assertTrue(mContentTranslator.setMemberVariable(content, "key1", "value1"));
        assertEquals(content.getExtraValue("key1"), "value1");
    }

    /**
     * Tests the {@link ContentTranslator#setMemberVariable(Content, String, Object)} method with
     * null input.
     */
    @Test
    public void testSetMemberVariableFalseCase() throws Exception {

        assertFalse(mContentTranslator.setMemberVariable(null, null, null));
        assertFalse(mContentTranslator.setMemberVariable(new Content(), null, "value"));
        assertFalse(mContentTranslator.setMemberVariable(new Content(), "", "value"));
    }

    /**
     * Tests the {@link ContentTranslator#validateModel(Content)} method for the positive case
     * where the model is expected to be valid.
     */
    @Test
    public void testValidateModelPositiveCase() throws Exception {

        assertTrue(mContentTranslator.validateModel(createValidContent()));
    }

    /**
     * Tests the {@link ContentTranslator#validateModel(Content)} method for the false case where
     * the title is invalid.
     */
    @Test
    public void testValidateModelFalseTitleCase() throws Exception {

        Content content = createValidContent();
        // make good content bad
        content.setTitle("");

        assertFalse(mContentTranslator.validateModel(content));
    }

    /**
     * Tests the {@link ContentTranslator#validateModel(Content)} method for the false case where
     * the description is invalid.
     */
    @Test
    public void testValidateModelFalseDescription() throws Exception {

        Content content = createValidContent();
        // make good content bad
        content.setDescription("");

        assertFalse(mContentTranslator.validateModel(content));
    }

    /**
     * Tests the {@link ContentTranslator#validateModel(Content)} method for the false case where
     * the url is invalid.
     */
    @Test
    public void testValidateModelFalseUrl() throws Exception {

        Content content = createValidContent();
        // make good content bad
        content.setUrl("");

        assertFalse(mContentTranslator.validateModel(content));
    }

    /**
     * Tests the {@link ContentTranslator#validateModel(Content)} method for the false case where
     * the cardImageUrl is invalid.
     */
    @Test
    public void testValidateModelFalseCardImageUrl() throws Exception {

        Content content = createValidContent();
        // make good content bad
        content.setCardImageUrl("");

        assertFalse(mContentTranslator.validateModel(content));
    }

    /**
     * Tests the {@link ContentTranslator#validateModel(Content)} method for the false case where
     * the backgroundImageUrl is invalid.
     */
    @Test
    public void testValidateModelFalseBackgroundImageUrl() throws Exception {

        Content content = createValidContent();
        // make good content bad
        content.setBackgroundImageUrl("");

        assertFalse(mContentTranslator.validateModel(content));
    }

    /**
     * Tests the {@link ContentTranslator#validateModel(Content)} method for the false case where
     * the title is null.
     */
    @Test
    public void testValidateModelNullValue() throws Exception {

        Content content = createValidContent();
        // make good content bad
        content.setTitle(null);

        assertFalse(mContentTranslator.validateModel(content));
    }

    /**
     * Tests the {@link ContentTranslator#instantiateModel()} method. The result of the method call
     * should not be null.
     */
    @Test
    public void testInstantiateModel() throws Exception {

        assertNotNull(mContentTranslator.instantiateModel());
    }

    /**
     * Tests the {@link ContentTranslator#mapToModel(Map, Recipe)} with a null map argument.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMapToModelNullMapCase() throws Exception {

        mContentTranslator.mapToModel(null, mGoodRecipe);
    }

    /**
     * Tests the {@link ContentTranslator#mapToModel(Map, Recipe)} with a null recipe argument.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMapToModelNullRecipeCase() throws Exception {

        mContentTranslator.mapToModel(createValidMap(), null);
    }

    /**
     * Tests the {@link ContentTranslator#mapToModel(Map, Recipe)} with valid map and recipe
     * arguments.
     */
    @Test
    public void testMapToModel() throws Exception {

        Content expected = createValidContent();

        Content result = mContentTranslator.mapToModel(createValidMap(), mGoodRecipe);

        assertEquals(expected, result);
    }

    /**
     * Tests the {@link ContentTranslator#mapToModel(Map, Recipe)} with a recipe that's missing the
     * title field.
     */
    @Test
    public void testMapToModelWithBadRecipeTitle() throws Exception {

        Recipe badRecipe = Recipe.newInstance(getBadContentRecipeJsonString("mTitle"));

        Content content = mContentTranslator.mapToModel(createValidMap(), badRecipe);
        assertNull("Content should be null due to bad translation", content);

    }

    /**
     * Tests the {@link ContentTranslator#mapToModel(Map, Recipe)} with a recipe that's missing the
     * description field.
     */
    @Test
    public void testMapToModelWithBadRecipeDescription() throws Exception {

        Recipe badRecipe = Recipe.newInstance(getBadContentRecipeJsonString("mDescription"));

        Content content = mContentTranslator.mapToModel(createValidMap(), badRecipe);
        assertNull("Content should be null due to bad translation", content);

    }

    /**
     * Tests the {@link ContentTranslator#mapToModel(Map, Recipe)} with a recipe that's missing the
     * url field.
     */
    @Test
    public void testMapToModelWithBadRecipeUrl() throws Exception {

        Recipe badRecipe = Recipe.newInstance(getBadContentRecipeJsonString("mUrl"));

        Content content = mContentTranslator.mapToModel(createValidMap(), badRecipe);
        assertNull("Content should be null due to bad translation", content);
    }

    /**
     * Tests the {@link ContentTranslator#mapToModel(Map, Recipe)} with a recipe that's missing the
     * cardImageUrl field.
     */
    @Test
    public void testMapToModelWithBadRecipeCardImageUrl() throws Exception {

        Recipe badRecipe = Recipe.newInstance(getBadContentRecipeJsonString("mCardImageUrl"));

        Content content = mContentTranslator.mapToModel(createValidMap(), badRecipe);
        assertNull("Content should be null due to bad translation", content);

    }

    /**
     * Tests the {@link ContentTranslator#mapToModel(Map, Recipe)} with a recipe that's missing the
     * backgroundImageUrl field.
     */
    @Test
    public void testMapToModelWithBadRecipeBackgroundImageUrl() throws Exception {

        Recipe badRecipe = Recipe.newInstance(getBadContentRecipeJsonString("mBackgroundImageUrl"));

        Content content = mContentTranslator.mapToModel(createValidMap(), badRecipe);
        assertNull("Content should be null due to bad translation", content);

    }

    /**
     *Create a content recipe JSON string that is missing a required field in the matchList.
     *
     * @param skipMatchItem Field that will be missing from the matchList.
     * @return JSON string for the bad content recipe.
     */
    private String getBadContentRecipeJsonString(String skipMatchItem) {

        return getBadContentRecipeJsonString(skipMatchItem, "");
    }

    /**
     * Create a content recipe JSON string that is missing a required field in the matchList and/or
     * has an extra/invalid field in the matchList.
     *
     * @param skipMatchItem Field that will be missing from the matchList.
     * @param extraMatchItem Extra field to add to the matchList (should be formatted as abc@123).
     * @return JSON string for the bad content recipe.
     */
    private String getBadContentRecipeJsonString(String skipMatchItem, String extraMatchItem) {

        final List<String> matchList = new ArrayList<>();
        matchList.add("title@mTitle");
        matchList.add("description@mDescription");
        matchList.add("urls/url@mUrl");
        matchList.add("urls/cardImageUrl@mCardImageUrl");
        matchList.add("urls/backgroundImageUrl@mBackgroundImageUrl");

        // Create the matchList, excluding skipMatchItem
        StringBuilder matchListStringBuilder = new StringBuilder();
        for (String item : matchList) {
            if (!skipMatchItem.isEmpty() && item.contains(skipMatchItem)) {
                continue;
            }
            if (!matchListStringBuilder.toString().isEmpty()) {
                matchListStringBuilder.append(",");
            }
            matchListStringBuilder
                    .append("\"")
                    .append(item)
                    .append("\"");
        }

        // Add extraMatchItem to the matchList
        if (!extraMatchItem.isEmpty()) {
            matchListStringBuilder
                    .append(",\"")
                    .append(extraMatchItem)
                    .append("\"");
        }

        // Construct the recipe JSON string
        return "{\n" +
                "  \"recipeCooker\": \"com.amazon.dynamicparser.DynamicParser\",\n" +
                "  \"format\": \"json\",\n" +
                "  \"model\": \"com.amazon.android.model.content.Content\",\n" +
                "  \"modelType\": \"\",\n" +
                "  \"conversionType\": \"\",\n" +
                "  \"query\": \"\",\n" +
                "  \"matchList\": [" +
                matchListStringBuilder.toString() +
                "],\n" +
                "  \"keyDataPath\": \"\"\n" +
                "}";
    }

    /**
     * Tests the {@link ContentTranslator#mapToModel(Map, Recipe)} with a bad map argument. The map
     * is missing the title field so the model should not be valid at the end of translation.
     */
    @Test
    public void testMapToModelWithBadMap() throws Exception {

        Map<String, Object> map = createValidMap();
        map.remove("title");

        Content content = mContentTranslator.mapToModel(map, mGoodRecipe);
        assertNull("Content should be null due to bad translation", content);

    }

    /**
     * Tests the {@link ContentTranslator#mapListToModelList(List, Recipe)} with a null map list.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMapToModelListNullMapCase() throws Exception {

        mContentTranslator.mapListToModelList(null, mGoodRecipe);
    }

    /**
     * Tests the {@link ContentTranslator#mapListToModelList(List, Recipe)} with a null recipe.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMapToModelListNullRecipeCase() throws Exception {

        mContentTranslator.mapListToModelList(new ArrayList<Map<String, Object>>(), null);
    }

    /**
     * Tests the {@link ContentTranslator#mapListToModelList(List, Recipe)} with good input
     * arguments.
     */
    @Test
    public void testMapToModelList() throws Exception {

        List<Map<String, Object>> mapList = new ArrayList<>();
        List<Content> expected = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            mapList.add(createValidMap());
            expected.add(createValidContent());
        }

        List<Content> result = mContentTranslator.mapListToModelList(mapList, mGoodRecipe);

        assertEquals(expected, result);
    }

    /**
     * Tests the {@link ContentTranslator#getName()} method.
     */
    @Test
    public void testGetName() throws Exception {

        assertEquals(ContentTranslator.class.getSimpleName(), mContentTranslator.getName());
    }

    /**
     * Creates a valid {@link Content} model to test with.
     */
    private Content createValidContent() throws Exception {

        Content content = new Content();
        content.setId("1");
        content.setTitle("Good Content");
        content.setSubtitle("Good Subtitle");
        content.setUrl("www.someUrl.com");
        content.setDescription("Good description of good content");
        content.setCardImageUrl("www.someUrl.com");
        content.setBackgroundImageUrl("www.someUrl.com");
        content.setTags("[tag1, tag2, tag3]");
        content.setExtraValue("key1", "value1");
        content.setExtraValue("key2", "www.someUrl.com");
        return content;
    }

    /**
     * Creates a valid Map to test translation with. Should produce {@link Content} that matches
     * result of createValidContent method.
     */
    private Map<String, Object> createValidMap() {

        Map<String, Object> map = new HashMap<>();
        map.put("id", "1");
        map.put("title", "Good Content");
        map.put("subtitle", "Good Subtitle");
        map.put("description", "Good description of good content");
        Map<String, String> urlMap = new HashMap<>();
        urlMap.put("url", "www.someUrl.com");
        urlMap.put("cardImageUrl", "www.someUrl.com");
        urlMap.put("backgroundImageUrl", "www.someUrl.com");
        map.put("urls", urlMap);
        List<String> tags = new ArrayList<>();
        tags.add("tag1");
        tags.add("tag2");
        tags.add("tag3");
        map.put("tags", tags);
        map.put("key1", "value1");
        map.put("key2", "www.someUrl.com");
        return map;
    }
}