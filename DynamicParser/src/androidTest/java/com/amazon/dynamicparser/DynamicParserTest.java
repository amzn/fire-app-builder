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

import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.amazon.dynamicparser.testResources.AlbumModel;
import com.amazon.dynamicparser.testResources.AlbumTranslator;
import com.amazon.dynamicparser.testResources.CommentModel;
import com.amazon.dynamicparser.testResources.CommentTranslator;
import com.amazon.dynamicparser.testResources.DummyContainer;
import com.amazon.dynamicparser.testResources.DummyContainerTranslator;
import com.amazon.dynamicparser.testResources.DummyContent;
import com.amazon.dynamicparser.testResources.DummyContentTranslator;
import com.amazon.dynamicparser.testResources.DummyLightCastContainer;
import com.amazon.dynamicparser.testResources.DummyParser;
import com.amazon.dynamicparser.testResources.ScreenFeedContent;
import com.amazon.dynamicparser.testResources.ScreenFeedContentTranslator;
import com.amazon.dynamicparser.testResources.ScreenFeedContainer;
import com.amazon.dynamicparser.testResources.ScreenFeedContainerTranslator;
import com.amazon.dynamicparser.impl.JsonParser;
import com.amazon.dynamicparser.impl.XmlParser;
import com.amazon.dynamicparser.testResources.PhotoTranslator;
import com.amazon.dynamicparser.testResources.PhotoModel;
import com.amazon.dynamicparser.testResources.UserModel;
import com.amazon.dynamicparser.testResources.UserTranslator;
import com.amazon.android.recipe.IRecipeCookerCallbacks;
import com.amazon.android.recipe.Recipe;
import com.amazon.android.model.AModelTranslator;
import com.amazon.android.utils.FileHelper;
import com.amazon.android.utils.PathHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * A test class for the {@link DynamicParser} class.
 */
@SuppressWarnings("unchecked")
@RunWith(AndroidJUnit4.class)
public class DynamicParserTest {


    /*
     * A json format sample video feed that is used in several tests.
     */
    private String mJsonSampleFeed;

    /*
     * A xml format sample video feed that is used in several tests.
     */
    private String mXmlSampleFeed;

    /**
     * Test set up. This method instantiates the member variables that are used by more than one
     * test.
     */
    @Before
    public void setUp() throws Exception {

        // Get the json format sample video feed feed as a string.
        mJsonSampleFeed = FileHelper.readFile(InstrumentationRegistry.getContext(),
                                              "feeds/SampleVideoFeed.json");

        // Get the xml format sample video feed as a string.
        mXmlSampleFeed = FileHelper.readFile(InstrumentationRegistry.getContext(),
                                             "feeds/SampleVideoFeed.xml");
    }

    /**
     * Tests the {@link DynamicParser#DynamicParser()} constructor. The dynamic parser should not
     * be null and it should contain {@link JsonParser} and {@link XmlParser} in its parser
     * implementation map.
     */
    @Test
    public void testDynamicParserConstructor() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        assertNotNull("Dynamic parser was created in setUp method and should not be null.",
                      dynamicParser);

        assertTrue("Dynamic parser should have a JsonParser that can be retrieved with the key " +
                           "'json'", dynamicParser.getParserImpl(JsonParser.FORMAT) instanceof
                           JsonParser);

        assertTrue("Dynamic parser should have a XmlParser that can be retrieved with the key" +
                           "'xml'", dynamicParser.getParserImpl(XmlParser.FORMAT) instanceof
                           XmlParser);
    }

    /**
     * Tests the {@link DynamicParser#addParserImpl(String, IParser)} method.
     */
    @Test
    public void testAddParserImpl() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        // Test adding a null IParser implementation.
        assertFalse("Null IParser implementations should not be added.", dynamicParser
                .addParserImpl("key", null));

        // Test adding a duplicate IParser implementation.
        assertFalse("Duplicate IParser implementations should not be added.", dynamicParser
                .addParserImpl(JsonParser.FORMAT, new JsonParser()));

        assertFalse("Duplicate IParser implementations should not be added.", dynamicParser
                .addParserImpl(XmlParser.FORMAT, new XmlParser()));

        // Test adding a new IParser implementation.
        assertTrue("Adding a new IParser implementation should return true", dynamicParser
                .addParserImpl(DummyParser.FORMAT, new DummyParser()));

    }

    /**
     * Tests the {@link DynamicParser#getParserImpl(String)} method for the positive case.
     */
    @Test
    public void testGetParserImplPositiveCase() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        // Test getting a parser with a valid key.
        assertTrue("JsonParser should be returned for key 'json.'", dynamicParser.getParserImpl
                (JsonParser.FORMAT) instanceof JsonParser);

        assertTrue("XmlParser should be returned for key 'xml.'", dynamicParser.getParserImpl
                (XmlParser.FORMAT) instanceof XmlParser);

    }

    /**
     * Tests the {@link DynamicParser#getParserImpl(String)} method for the negative case.
     * Should throw {@link com.amazon.dynamicparser.DynamicParser.ParserNotFoundException}
     */
    @Test(expected = DynamicParser.ParserNotFoundException.class)
    public void testGetParserImplNegativeCase() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        // No parser implementation should exist with the given key.
        dynamicParser.getParserImpl("key");
    }

    /**
     * Tests the {@link DynamicParser#addTranslatorImpl(String, AModelTranslator)} method for the
     * positive and negative cases.
     */
    @Test
    public void testAddTranslatorImpl() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        UserTranslator translator = new UserTranslator();

        assertTrue("Translator impl should have been successfully added",
                   dynamicParser.addTranslatorImpl(translator.getName(), translator));

        assertFalse("Translator should not have been overridden",
                    dynamicParser.addTranslatorImpl(translator.getName(), translator));

        assertFalse("A null translator should not have been added",
                    dynamicParser.addTranslatorImpl("nullTranslator", null));

        assertFalse("A translator with a null name should not have been added",
                    dynamicParser.addTranslatorImpl(null, translator));
    }

    /**
     * Tests the {@link DynamicParser#validateRecipe(Recipe)} class with a valid translation {@link
     * Recipe}.
     */
    @Test
    public void testValidateRecipeWithValidTranslationRecipe() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        // Create a translation recipe for json sample feed.
        Recipe recipe = createJsonSampleVideoContainerReflectionRecipe();
        recipe.getMap().put(DynamicParser.TRANSLATOR_TAG, "ContainerTranslator");

        // Validate the translation recipe.
        assertTrue(dynamicParser.validateRecipe(recipe));

        // Create a translation recipe for xml sample feed.
        Recipe recipe2 = createXmlSampleVideoContainterReflectionRecipe();
        recipe2.getMap().put(DynamicParser.TRANSLATOR_TAG, "ContainerTranslator");

        // Validate the translation recipe.
        assertTrue(dynamicParser.validateRecipe(recipe2));
    }

    /**
     * Tests the {@link DynamicParser#validateRecipe(Recipe)} class with a {@link Recipe} that's
     * missing the "conversionType" field. This is still a valid Recipe.
     */
    @Test
    public void testValidateRecipeWithValidReflectionRecipe() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        // Validate the reflection recipes.
        assertTrue(dynamicParser.validateRecipe(createJsonSampleVideoContainerReflectionRecipe()));

        assertTrue(dynamicParser.validateRecipe(createXmlSampleVideoContainterReflectionRecipe()));
    }

    /**
     * Tests the {@link DynamicParser#validateRecipe(Recipe)} class with a {@link Recipe} that's
     * missing the "format" field.
     */
    @Test(expected = DynamicParser.InvalidParserRecipeException.class)
    public void testValidateRecipeWithMissingFormat() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        // Remove the format field from recipes.
        Recipe recipe = createJsonSampleVideoContainerReflectionRecipe();
        recipe.getMap().remove(DynamicParser.FORMAT_TAG);

        // Recipe should not be valid.
        dynamicParser.validateRecipe(recipe);
    }

    /**
     * Tests the {@link DynamicParser#validateRecipe(Recipe)} class with a {@link Recipe} that's
     * missing the "recipeCooker" field.
     */
    @Test(expected = DynamicParser.InvalidParserRecipeException.class)
    public void testValidateRecipeWithMissingRecipeCooker() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        // Remove the recipeCooker field from recipe.
        Recipe recipe = createJsonSampleVideoContainerReflectionRecipe();
        recipe.getMap().remove(DynamicParser.COOKER_TAG);

        // Recipe should not be valid.
        dynamicParser.validateRecipe(recipe);
    }

    /**
     * Tests the {@link DynamicParser#validateRecipe(Recipe)} class with a {@link Recipe} that's
     * missing the "model" field.
     */
    @Test(expected = DynamicParser.InvalidParserRecipeException.class)
    public void testValidateRecipeWithMissingModel() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();


        // Remove the model field from recipe.
        Recipe recipe = createJsonSampleVideoContainerReflectionRecipe();
        recipe.getMap().remove(DynamicParser.MODEL_TAG);

        // Recipe should not be valid.
        dynamicParser.validateRecipe(recipe);
    }

    /**
     * Tests the {@link DynamicParser#validateRecipe(Recipe)} class with a {@link Recipe} that's
     * missing the "modelType" field.
     */
    @Test(expected = DynamicParser.InvalidParserRecipeException.class)
    public void testValidateRecipeWithMissingModelType() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        // Remove the modelType field from recipe.
        Recipe recipe = createJsonSampleVideoContainerReflectionRecipe();
        recipe.getMap().remove(DynamicParser.MODEL_TYPE_TAG);

        // Recipe should not be valid.
        dynamicParser.validateRecipe(recipe);
    }

    /**
     * Tests the {@link DynamicParser#validateRecipe(Recipe)} class with a {@link Recipe} that's
     * missing the "query" field.
     */
    @Test(expected = DynamicParser.InvalidParserRecipeException.class)
    public void testValidateRecipeWithMissingQuery() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        // Remove the query field from recipe.
        Recipe recipe = createJsonSampleVideoContainerReflectionRecipe();
        recipe.getMap().remove(DynamicParser.QUERY_TAG);

        // Recipe should not be valid.
        dynamicParser.validateRecipe(recipe);
    }

    /**
     * Tests the {@link DynamicParser#validateRecipe(Recipe)} class with a {@link Recipe} that's
     * missing the "matchList" field.
     */
    @Test(expected = DynamicParser.InvalidParserRecipeException.class)
    public void testValidateRecipeWithMissingMatchList() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        // Remove the matchList field from recipe.
        Recipe recipe = createJsonSampleVideoContainerReflectionRecipe();
        recipe.getMap().remove(DynamicParser.MATCH_LIST_TAG);

        // Recipe should not be valid.
        dynamicParser.validateRecipe(recipe);
    }

    /**
     * Tests the {@link DynamicParser#validateRecipe(Recipe)} class with a {@link Recipe} that's
     * missing the "keyDataPath" field.
     */
    @Test
    public void testValidateRecipeWithMissingKeyDataPath() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        // Remove the keyDataPath field from recipes.
        Recipe recipe = createJsonSampleVideoContainerReflectionRecipe();
        recipe.getMap().remove(DynamicParser.KEY_DATA_PATH_TAG);

        Recipe recipe2 = createXmlSampleVideoContainterReflectionRecipe();
        recipe2.getMap().remove(DynamicParser.KEY_DATA_PATH_TAG);

        // Recipes are still valid.
        assertTrue(dynamicParser.validateRecipe(recipe));
        assertTrue(dynamicParser.validateRecipe(recipe2));
    }

    /**
     * Tests the {@link DynamicParser#validateRecipe(Recipe)} class with a null {@link Recipe}.
     */
    @Test(expected = DynamicParser.InvalidParserRecipeException.class)
    public void testValidateRecipeWithNullRecipe() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        dynamicParser.validateRecipe(null);
    }

    /**
     * Tests the {@link DynamicParser#validateRecipe(Recipe)} class with an empty {@link Recipe}.
     */
    @Test(expected = DynamicParser.InvalidParserRecipeException.class)
    public void testValidateRecipeWithEmptyRecipe() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        dynamicParser.validateRecipe(Recipe.newInstance("{}"));
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method using reflection with valid input parameters using the sample video feed.
     * This feed contains 3 categories.
     */
    @Test
    public void testCookRecipeWithReflectionForSampleVideoFeed() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        dynamicParser.cookRecipe(createJsonSampleVideoContainerReflectionRecipe(), mJsonSampleFeed,
                                 new IRecipeCookerCallbacks() {
                                     int cookedCount = 0;

                                     @Override
                                     public void onPreRecipeCook(Recipe recipe, Object output,
                                                                 Bundle bundle) {

                                     }

                                     @Override
                                     public void onRecipeCooked(Recipe recipe, Object output,
                                                                Bundle bundle, boolean done) {

                                         cookedCount++;
                                         assertNotNull(output);
                                         assertEquals(output.getClass(), DummyContainer.class);
                                         if (done) {
                                             assertEquals(3, cookedCount);
                                         }
                                     }

                                     @Override
                                     public void onPostRecipeCooked(Recipe recipe, Object
                                             output, Bundle bundle) {

                                     }

                                     @Override
                                     public void onRecipeError(Recipe recipe, Exception e,
                                                               String msg) {
                                         // Force failure if this happens.
                                         assertTrue("Recipe should have been cooked without " +
                                                            "error", false);
                                     }
                                 },
                                 null, null);

        dynamicParser.cookRecipe(createXmlSampleVideoContainterReflectionRecipe(), mXmlSampleFeed,
                                 new IRecipeCookerCallbacks() {
                                     int cookedCount = 0;

                                     @Override
                                     public void onPreRecipeCook(Recipe recipe, Object output,
                                                                 Bundle bundle) {

                                     }

                                     @Override
                                     public void onRecipeCooked(Recipe recipe, Object output,
                                                                Bundle bundle, boolean done) {

                                         cookedCount++;
                                         assertNotNull(output);
                                         assertEquals(output.getClass(), DummyContainer.class);
                                         if (done) {
                                             assertEquals(3, cookedCount);
                                         }
                                     }

                                     @Override
                                     public void onPostRecipeCooked(Recipe recipe, Object
                                             output, Bundle bundle) {

                                     }

                                     @Override
                                     public void onRecipeError(Recipe recipe, Exception e,
                                                               String msg) {
                                         // Force failure if this happens.
                                         assertTrue("Recipe should have been cooked without " +
                                                            "error", false);
                                     }
                                 },
                                 null, null);
    }


    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method using translation with valid input parameters using the sample video feed.
     */
    @Test
    public void testCookRecipeWithTranslationForSampleVideoFeed() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        DummyContainerTranslator translator = new DummyContainerTranslator();

        dynamicParser.addTranslatorImpl(translator.getName(), translator);

        // Make the recipe use translation.
        Recipe recipe = createJsonSampleVideoContainerReflectionRecipe();
        recipe.getMap().put(DynamicParser.TRANSLATOR_TAG, translator.getName());

        dynamicParser.cookRecipe(recipe, mJsonSampleFeed,
                                 getCallbackForExpectedResult(DummyContainer.class),
                                 null, null);

        Recipe recipe2 = createXmlSampleVideoContainterReflectionRecipe();
        recipe2.getMap().put(DynamicParser.TRANSLATOR_TAG, translator.getName());

        dynamicParser.cookRecipe(recipe2, mXmlSampleFeed,
                                 getCallbackForExpectedResult(DummyContainer.class),
                                 null, null);
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with null input.
     */
    @Test
    public void testCookRecipeWithNullInput() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        dynamicParser.cookRecipe(createJsonSampleVideoContainerReflectionRecipe(), null,
                                 getCallbackWithExpectedException(IllegalArgumentException.class,
                                                                  false),
                                 null, null);

        dynamicParser.cookRecipe(createXmlSampleVideoContainterReflectionRecipe(), null,
                                 getCallbackWithExpectedException(IllegalArgumentException.class,
                                                                  false),
                                 null, null);
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with an invalid recipe.
     */
    @Test
    public void testCookRecipeWithInvalidRecipe() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        dynamicParser.cookRecipe(Recipe.newInstance("{}"), mJsonSampleFeed,
                                 getCallbackWithExpectedException(
                                         DynamicParser.InvalidParserRecipeException.class, false),
                                 null, null);

        dynamicParser.cookRecipe(Recipe.newInstance("{}"), mXmlSampleFeed,
                                 getCallbackWithExpectedException(
                                         DynamicParser.InvalidParserRecipeException.class, false),
                                 null, null);
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with an invalid query.
     */
    @Test
    public void testCookRecipeWithInvalidQuery() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        Recipe recipe = createJsonSampleVideoContainerReflectionRecipe();
        recipe.getMap().put(DynamicParser.QUERY_TAG, "badQuery");

        dynamicParser.cookRecipe(recipe, mJsonSampleFeed,
                                 getCallbackWithExpectedException(IParser.InvalidQueryException
                                                                          .class, false),
                                 null, null);
        dynamicParser.cookRecipe(recipe, mXmlSampleFeed,
                                 getCallbackWithExpectedException(IParser.InvalidQueryException
                                                                          .class, false),
                                 null, null);
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with invalid data.
     */
    @Test
    public void testCookRecipeWithInvalidData() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        dynamicParser.cookRecipe(createJsonSampleVideoContainerReflectionRecipe(), "{\"json\"}",
                                 getCallbackWithExpectedException(IParser.InvalidDataException
                                                                          .class, false),
                                 null, null);

        dynamicParser.cookRecipe(createXmlSampleVideoContainterReflectionRecipe(), "<xml>",
                                 getCallbackWithExpectedException(IParser.InvalidDataException
                                                                          .class, false),
                                 null, null);
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with an invalid parser.
     */
    @Test
    public void testCookRecipeWithInvalidParser() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        Recipe recipe = createJsonSampleVideoContainerReflectionRecipe();
        recipe.getMap().put(DynamicParser.FORMAT_TAG, "invalidParser");

        dynamicParser.cookRecipe(recipe, mJsonSampleFeed,
                                 getCallbackWithExpectedException(
                                         DynamicParser.ParserNotFoundException.class, false),
                                 null, null);

        Recipe recipe2 = createXmlSampleVideoContainterReflectionRecipe();
        recipe2.getMap().put(DynamicParser.FORMAT_TAG, "invalidParser");

        dynamicParser.cookRecipe(recipe2, mXmlSampleFeed,
                                 getCallbackWithExpectedException(
                                         DynamicParser.ParserNotFoundException.class, false),
                                 null, null);
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method using reflection, parameters and the sample video feed.
     */
    @Test
    public void testCookRecipeWithParamsUsingReflection() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        String[] params = new String[]{"video"};

        // Create the recipe for reflection that uses a parameter.
        Recipe recipe =
                createParserRecipe("DynamicParser", // cooker
                                   "json", // format
                                   "com.amazon.dynamicparser.testResources.DummyContent", // model
                                   "array", // model type
                                   null, // translator
                                   "$.videos[?(@.type == '$$par0$$')]", // query
                                   null, // query result type
                                   null, // key data path
                                   Arrays.asList("videoInfo/title@mTitle", // match list
                                                 "videoId@mId",
                                                 "videoInfo/subtitle@mSubtitle",
                                                 "videoInfo/description@mDescription",
                                                 "videoInfo/imageUrls/cardImage@mCardImageUrl",
                                                 "videoInfo/imageUrls/backgroundImage@mBackgroundImageUrl",
                                                 "videoInfo/tags@mTags",
                                                 "videoInfo/videoURL@mUrl"));

        dynamicParser.cookRecipe(recipe, mJsonSampleFeed,
                                 getCallbackForExpectedResult(DummyContent.class), null,
                                 params);

        recipe =
                createParserRecipe("DynamicParser", // cooker
                                   "xml", // format
                                   "com.amazon.dynamicparser.testResources.DummyContent", // model
                                   "array", // model type
                                   null, // translator
                                   "sample/videos[type='$$par0$$']", // query
                                   null, // query result type
                                   null, // key data path
                                   Arrays.asList("videoInfo/title/#text@mTitle", // match list
                                                 "videoId/#text@mId",
                                                 "videoInfo/subtitle/#text@mSubtitle",
                                                 "videoInfo/description/#text@mDescription",
                                                 "videoInfo/imageUrls/cardImage/#text@mCardImageUrl",
                                                 "videoInfo/imageUrls/backgroundImage/#text@mBackgroundImageUrl",
                                                 "videoInfo/tags/#text@mTags",
                                                 "videoInfo/videoURL/#text@mUrl"));

        dynamicParser.cookRecipe(recipe, mXmlSampleFeed,
                                 getCallbackForExpectedResult(DummyContent.class), null,
                                 params);
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method using translation, parameters and the sample video feed.
     */
    @Test
    public void testCookRecipeWithParamsUsingTranslation() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();
        DummyContentTranslator translator = new DummyContentTranslator();
        dynamicParser.addTranslatorImpl(translator.getName(), translator);

        String[] params = new String[]{"video"};

        // Create the recipe for translation that uses a parameter in the query.
        Recipe recipe =
                createParserRecipe("DynamicParser", // cooker
                                   "json", // format
                                   "com.amazon.dynamicparser.testResources.DummyContent", // model
                                   "array", // model type
                                   translator.getName(), // translator
                                   "$.videos[?(@.type == '$$par0$$')]", // query
                                   null, // query result type
                                   null, // key data path
                                   Arrays.asList("videoInfo/title@mTitle", // match list
                                                 "videoId@mId",
                                                 "videoInfo/subtitle@mSubtitle",
                                                 "videoInfo/description@mDescription",
                                                 "videoInfo/imageUrls/cardImage@mCardImageUrl",
                                                 "videoInfo/imageUrls/backgroundImage@mBackgroundImageUrl",
                                                 "videoInfo/tags@mTags",
                                                 "videoInfo/videoURL@mUrl"));

        dynamicParser.cookRecipe(recipe, mJsonSampleFeed,
                                 getCallbackForExpectedResult(DummyContent.class), null,
                                 params);

        recipe =
                createParserRecipe("DynamicParser", // cooker
                                   "xml", // format
                                   "com.amazon.dynamicparser.testResources.DummyContent", // model
                                   "array", // model type
                                   translator.getName(), // translator
                                   "sample/videos", // query
                                   null, // query result type
                                   null, // key data path
                                   Arrays.asList("videoInfo/title/#text@mTitle", // match list
                                                 "videoId/#text@mId",
                                                 "videoInfo/subtitle/#text@mSubtitle",
                                                 "videoInfo/description/#text@mDescription",
                                                 "videoInfo/imageUrls/cardImage/#text@mCardImageUrl",
                                                 "videoInfo/imageUrls/backgroundImage/#text@mBackgroundImageUrl",
                                                 "videoInfo/tags/#text@mTags",
                                                 "videoInfo/videoURL/#text@mUrl"));

        dynamicParser.cookRecipe(recipe, mXmlSampleFeed,
                                 getCallbackForExpectedResult(DummyContent.class), null,
                                 params);
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method using bad parameters.
     */
    @Test
    public void testCookRecipeWithBadParams() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        // Create the recipe for reflection that uses a parameter in the query.
        Recipe recipe =
                createParserRecipe("DynamicParser", // cooker
                                   "json", // format
                                   "com.amazon.dynamicparser.testResources.DummyContent", // model
                                   "array", // model type
                                   null, // translator
                                   "$.videos[?(@.type == '$$par0$$')]", // query
                                   null, // query result type
                                   null, // key data path
                                   Arrays.asList("videoInfo/title@mTitle", // match list
                                                 "videoId@mId",
                                                 "videoInfo/subtitle@mSubtitle",
                                                 "videoInfo/description@mDescription",
                                                 "videoInfo/imageUrls/cardImage@mCardImageUrl",
                                                 "videoInfo/imageUrls/backgroundImage@mBackgroundImageUrl",
                                                 "videoInfo/tags@mTags",
                                                 "videoInfo/videoURL@mUrl"));

        dynamicParser.cookRecipe(recipe, mJsonSampleFeed,
                                 getCallbackWithExpectedException(
                                         PathHelper.MalformedInjectionStringException.class, false),
                                 null, new String[]{});
        recipe =
                createParserRecipe("DynamicParser", // cooker
                                   "xml", // format
                                   "com.amazon.dynamicparser.testResources.DummyContent", // model
                                   "array", // model type
                                   null, // translator
                                   "sample/videos[type='$$par0$$']", // query
                                   null, // query result type
                                   null, // key data path
                                   Arrays.asList("videoInfo/title/#text@mTitle", // match list
                                                 "videoId/#text@mId",
                                                 "videoInfo/subtitle/#text@mSubtitle",
                                                 "videoInfo/description/#text@mDescription",
                                                 "videoInfo/imageUrls/cardImage/#text@mCardImageUrl",
                                                 "videoInfo/imageUrls/backgroundImage/#text@mBackgroundImageUrl",
                                                 "videoInfo/tags/#text@mTags",
                                                 "videoInfo/videoURL/#text@mUrl"));

        dynamicParser.cookRecipe(recipe, mXmlSampleFeed,
                                 getCallbackWithExpectedException(
                                         PathHelper.MalformedInjectionStringException.class, false),
                                 null, new String[]{});

    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method using an unknown cooker in the {@link Recipe}.
     */
    @Test
    public void testCookRecipeWithBadCookerType() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        Recipe recipe = createJsonSampleVideoContainerReflectionRecipe();
        recipe.getMap().put(DynamicParser.COOKER_TAG, "someCooker");

        dynamicParser.cookRecipe(recipe, mJsonSampleFeed, getCallbackWithExpectedException(
                DynamicParser.InvalidParserRecipeException.class, false),
                                 null, null);

        Recipe recipe2 = createXmlSampleVideoContainterReflectionRecipe();
        recipe2.getMap().put(DynamicParser.COOKER_TAG, "someCooker");

        dynamicParser.cookRecipe(recipe2, mXmlSampleFeed, getCallbackWithExpectedException(
                DynamicParser.InvalidParserRecipeException.class, false),
                                 null, null);
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method using an unregistered translator.
     */
    @Test
    public void testCookRecipeWithBadTranslator() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        // Change the recipe to use an unknown translator.
        Recipe recipe = createJsonSampleVideoContainerReflectionRecipe();
        recipe.getMap().put(DynamicParser.TRANSLATOR_TAG, "fakeTranslator");

        Recipe recipe2 = createXmlSampleVideoContainterReflectionRecipe();
        recipe2.getMap().put(DynamicParser.TRANSLATOR_TAG, "fakeTranslator");

        // Expecting an translator not found error.
        dynamicParser.cookRecipe(recipe, mJsonSampleFeed,
                                 getCallbackWithExpectedException(
                                         DynamicParser.TranslatorNotFoundException.class, true),
                                 null, null);

        dynamicParser.cookRecipe(recipe2, mXmlSampleFeed,
                                 getCallbackWithExpectedException(
                                         DynamicParser.TranslatorNotFoundException.class, true),
                                 null, null);


    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method using a reflection recipe with a match list that contains an invalid path.
     */
    @Test
    public void testCookRecipeWithBadRecipeMatchListUsingReflection() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        // Add an invalid path to the recipe's match list.
        Recipe recipe = createJsonSampleVideoContainerReflectionRecipe();
        ArrayList<String> matchList =
                (ArrayList<String>) recipe.getItemAsStringList(DynamicParser.MATCH_LIST_TAG);
        matchList.add("fakePath@value");

        Recipe recipe2 = createXmlSampleVideoContainterReflectionRecipe();
        ArrayList<String> matchList2 =
                (ArrayList<String>) recipe2.getItemAsStringList(DynamicParser.MATCH_LIST_TAG);
        matchList2.add("fakePath@value");

        // Expecting a value not found exception.
        dynamicParser.cookRecipe(recipe, mJsonSampleFeed, getCallbackWithExpectedException(
                DynamicParser.ValueNotFoundException.class, true), null, null);

        dynamicParser.cookRecipe(recipe2, mXmlSampleFeed, getCallbackWithExpectedException(
                DynamicParser.ValueNotFoundException.class, true), null, null);
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method using a reflection recipe with a match list that contains an unknown
     * field.
     */
    @Test
    public void testCookRecipeWithBadRecipeUnknownFieldUsingReflection() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        // Add an invalid path to the recipe's match list.
        Recipe recipe = createJsonSampleVideoContainerReflectionRecipe();
        ArrayList<String> matchList =
                (ArrayList<String>) recipe.getItemAsStringList(DynamicParser.MATCH_LIST_TAG);
        matchList.add("info/title@unknownId");

        Recipe recipe2 = createXmlSampleVideoContainterReflectionRecipe();
        ArrayList<String> matchList2 =
                (ArrayList<String>) recipe2.getItemAsStringList(DynamicParser.MATCH_LIST_TAG);
        matchList2.add("info/title/#text@unknownId");

        // Expecting a value not found exception.
        dynamicParser.cookRecipe(recipe, mJsonSampleFeed, getCallbackWithExpectedException(
                NoSuchFieldException.class, true), null, null);

        dynamicParser.cookRecipe(recipe2, mXmlSampleFeed, getCallbackWithExpectedException(
                NoSuchFieldException.class, true), null, null);
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method using a translation recipe with a match list that contains an invalid
     * path.
     */
    @Test
    public void testCookRecipeWithBadRecipeMatchListUsingTranslation() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();
        UserTranslator translator = new UserTranslator();
        dynamicParser.addTranslatorImpl(translator.getName(), translator);

        // Add an invalid path to the recipe's match list.
        Recipe recipe = createJsonSampleVideoContainerReflectionRecipe();
        ArrayList<String> matchList =
                (ArrayList<String>) recipe.getItemAsStringList(DynamicParser.MATCH_LIST_TAG);
        matchList.add("fakePath@value");
        recipe.getMap().put(DynamicParser.TRANSLATOR_TAG, translator.getName());

        Recipe recipe2 = createXmlSampleVideoContainterReflectionRecipe();
        ArrayList<String> matchList2 =
                (ArrayList<String>) recipe2.getItemAsStringList(DynamicParser.MATCH_LIST_TAG);
        matchList2.add("fakePath@value");
        recipe2.getMap().put(DynamicParser.TRANSLATOR_TAG, translator.getName());

        // Expecting a value not found exception.
        dynamicParser.cookRecipe(recipe, mJsonSampleFeed, getCallbackWithExpectedException(
                AModelTranslator.TranslationException.class, true), null, null);
        dynamicParser.cookRecipe(recipe2, mXmlSampleFeed, getCallbackWithExpectedException(
                AModelTranslator.TranslationException.class, true), null, null);
    }

    /**
     * Tests {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method using reflection and an unknown model.
     */
    @Test
    public void testCookRecipeWithBadModelClassUsingReflection() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        Recipe recipe = createJsonSampleVideoContainerReflectionRecipe();
        recipe.getMap().put(DynamicParser.MODEL_TAG, "unknownModel");

        dynamicParser.cookRecipe(recipe, mJsonSampleFeed, getCallbackWithExpectedException(
                ClassNotFoundException.class, true), null, null);

        Recipe recipe2 = createXmlSampleVideoContainterReflectionRecipe();
        recipe2.getMap().put(DynamicParser.MODEL_TAG, "unknownModel");

        dynamicParser.cookRecipe(recipe2, mXmlSampleFeed, getCallbackWithExpectedException(
                ClassNotFoundException.class, true), null, null);
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with valid input parameters using the 10 users JSON feed.
     */
    @Test
    public void testCookRecipeWith10UsersJsonFeed() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        UserTranslator translator = new UserTranslator();

        String feed = FileHelper.readFile(InstrumentationRegistry.getContext(),
                                          "feeds/10UsersFeed.json");

        // Create translation recipe
        Recipe translationRecipe =
                createParserRecipe("DynamicParser", // cooker
                                   "json", // format
                                   "com.amazon.dynamicparser.testResources.UserModel", // model
                                   "array", // model type
                                   translator.getName(), // translator
                                   "$.users", // query
                                   null, // query result type
                                   null,  // key data path
                                   new ArrayList<>(Arrays.asList("name@name", // match list
                                                                 "email@email",
                                                                 "address/city@address",
                                                                 "company/name@company")));

        // Create reflection recipe
        Recipe reflectionRecipe = new Recipe();
        reflectionRecipe.setMap(translationRecipe.getMap());
        reflectionRecipe.getMap().remove(DynamicParser.TRANSLATOR_TAG);

        dynamicParser.addTranslatorImpl(translator.getName(), translator);

        // Test cook recipe using translation
        dynamicParser.cookRecipe(translationRecipe, feed,
                                 getCallbackForExpectedResult(UserModel.class), null, null);

        // Test cook recipe using reflection
        dynamicParser.cookRecipe(reflectionRecipe, feed,
                                 getCallbackForExpectedResult(UserModel.class), null, null);

        // Test cook recipe using translation and reflection, but with batch and multithread
        // settings set to true.
        dynamicParser.configureSettings(true, true);
        dynamicParser.cookRecipe(translationRecipe, feed,
                                 getCallbackForExpectedResultList(10, UserModel.class), null,
                                 null);
        dynamicParser.cookRecipe(reflectionRecipe, feed,
                                 getCallbackForExpectedResultList(10, UserModel.class), null,
                                 null);

        // Test cook recipe using translation and reflection, but with batch and multithread
        // settings set to false.
        dynamicParser.configureSettings(false, false);
        dynamicParser.cookRecipe(translationRecipe, feed,
                                 getCallbackForExpectedResult(UserModel.class), null, null);
        dynamicParser.cookRecipe(reflectionRecipe, feed,
                                 getCallbackForExpectedResult(UserModel.class), null, null);

        // Test cook recipe using translation and reflection, but with batch true and
        // multithread false.
        dynamicParser.configureSettings(true, false);
        dynamicParser.cookRecipe(translationRecipe, feed,
                                 getCallbackForExpectedResultList(10, UserModel.class), null,
                                 null);
        dynamicParser.cookRecipe(reflectionRecipe, feed,
                                 getCallbackForExpectedResultList(10, UserModel.class), null,
                                 null);
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with valid input parameters using the 10 users XML feed.
     */
    @Test
    public void testCookRecipeWith10UsersXmlFeed() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        UserTranslator translator = new UserTranslator();

        String feed = FileHelper.readFile(InstrumentationRegistry.getContext(),
                                          "feeds/10UsersFeed.xml");

        // Create translation recipe
        Recipe translationRecipe =
                createParserRecipe("DynamicParser", // cooker
                                   "xml", // format
                                   "com.amazon.dynamicparser.testResources.UserModel", // model
                                   "array", // model type
                                   translator.getName(), // translator
                                   "users/user", // query
                                   null, // query result type
                                   null,  // key data path
                                   new ArrayList<>(Arrays.asList("name/#text@name", // match list
                                                                 "email/#text@email",
                                                                 "address/city/#text@address",
                                                                 "company/name/#text@company")));

        // Create reflection recipe
        Recipe reflectionRecipe = new Recipe();
        reflectionRecipe.setMap(translationRecipe.getMap());
        reflectionRecipe.getMap().remove(DynamicParser.TRANSLATOR_TAG);

        dynamicParser.addTranslatorImpl(translator.getName(), translator);

        // Test cook recipe using translation
        dynamicParser.cookRecipe(translationRecipe, feed,
                                 getCallbackForExpectedResult(UserModel.class), null, null);

        // Test cook recipe using reflection
        dynamicParser.cookRecipe(reflectionRecipe, feed,
                                 getCallbackForExpectedResult(UserModel.class), null, null);

        // Test cook recipe using translation and reflection, but with batch and multithread
        // settings set to true.
        dynamicParser.configureSettings(true, true);
        dynamicParser.cookRecipe(translationRecipe, feed,
                                 getCallbackForExpectedResultList(10, UserModel.class), null,
                                 null);
        dynamicParser.cookRecipe(reflectionRecipe, feed,
                                 getCallbackForExpectedResultList(10, UserModel.class), null,
                                 null);

        // Test cook recipe using translation and reflection, but with batch and multithread
        // settings set to false.
        dynamicParser.configureSettings(false, false);
        dynamicParser.cookRecipe(translationRecipe, feed,
                                 getCallbackForExpectedResult(UserModel.class), null, null);
        dynamicParser.cookRecipe(reflectionRecipe, feed,
                                 getCallbackForExpectedResult(UserModel.class), null, null);

        // Test cook recipe using translation and reflection, but with batch true and
        // multithread false.
        dynamicParser.configureSettings(true, false);
        dynamicParser.cookRecipe(translationRecipe, feed,
                                 getCallbackForExpectedResultList(10, UserModel.class), null,
                                 null);
        dynamicParser.cookRecipe(reflectionRecipe, feed,
                                 getCallbackForExpectedResultList(10, UserModel.class), null,
                                 null);
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with valid input parameters using the 100 albums JSON feed.
     */
    @Test
    public void testCookRecipeWith100AlbumsJsonFeed() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();
        dynamicParser.setAsyncMode(false);

        AlbumTranslator translator = new AlbumTranslator();

        String feed = FileHelper.readFile(InstrumentationRegistry.getContext(),
                                          "feeds/100AlbumsFeed.json");

        Recipe reflectionRecipe =
                createParserRecipe("DynamicParser", // cooker
                                   "json", // format
                                   "com.amazon.dynamicparser.testResources.AlbumModel", // model
                                   "array", // model type
                                   null, // translator
                                   "$.albums", // query
                                   null, // query result type
                                   null, // key data path
                                   new ArrayList<>(Arrays.asList("userId@userId", // match list
                                                                 "id@id",
                                                                 "title@title")));

        Recipe translationRecipe = new Recipe();
        translationRecipe.setMap(reflectionRecipe.getMap());
        translationRecipe.getMap().put(DynamicParser.TRANSLATOR_TAG, translator.getName());


        dynamicParser.addTranslatorImpl(translator.getName(), translator);

        // Test cook recipe using translation
        dynamicParser.cookRecipe(translationRecipe, feed,
                                 getCallbackForExpectedResult(AlbumModel.class), null, null);

        // Test cook recipe using reflection
        dynamicParser.cookRecipe(reflectionRecipe, feed,
                                 getCallbackForExpectedResult(AlbumModel.class), null, null);


    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with valid input parameters using the 100 albums XML feed.
     */
    @Test
    public void testCookRecipeWith100AlbumsXmlFeed() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();
        dynamicParser.setAsyncMode(false);

        AlbumTranslator translator = new AlbumTranslator();

        String feed = FileHelper.readFile(InstrumentationRegistry.getContext(),
                                          "feeds/100AlbumsFeed.xml");

        Recipe reflectionRecipe =
                createParserRecipe("DynamicParser", // cooker
                                   "xml", // format
                                   "com.amazon.dynamicparser.testResources.AlbumModel", // model
                                   "array", // model type
                                   null, // translator
                                   "albums/album", // query
                                   null, // query result type
                                   null, // key data path
                                   new ArrayList<>(Arrays.asList("userId/#text@userId", // match
                                                                 // list
                                                                 "id/#text@id",
                                                                 "title/#text@title")));

        Recipe translationRecipe = new Recipe();
        translationRecipe.setMap(reflectionRecipe.getMap());
        translationRecipe.getMap().put(DynamicParser.TRANSLATOR_TAG, translator.getName());


        dynamicParser.addTranslatorImpl(translator.getName(), translator);

        // Test cook recipe using translation
        dynamicParser.cookRecipe(translationRecipe, feed,
                                 getCallbackForExpectedResult(AlbumModel.class), null, null);

        // Test cook recipe using reflection
        dynamicParser.cookRecipe(reflectionRecipe, feed,
                                 getCallbackForExpectedResult(AlbumModel.class), null, null);


    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with valid input parameters using the 500 comments JSON feed.
     */
    @Test
    public void testCookRecipeWith500CommentsJsonFeed() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();
        dynamicParser.setAsyncMode(false);
        CommentTranslator translator = new CommentTranslator();

        String feed = FileHelper.readFile(InstrumentationRegistry.getContext(),
                                          "feeds/500CommentsFeed.json");

        Recipe reflectionRecipe =
                createParserRecipe("DynamicParser", // cooker
                                   "json", // format
                                   "com.amazon.dynamicparser.testResources.CommentModel", // model
                                   "array", // model type
                                   null, // translator
                                   "$.comments", // query
                                   null, // query result type
                                   null, // key data path
                                   new ArrayList<>(Arrays.asList("postId@mPostId", // match list
                                                                 "id@mId",
                                                                 "name@mName",
                                                                 "email@mEmail",
                                                                 "body@mBody")));

        Recipe translationRecipe = new Recipe();
        translationRecipe.setMap(reflectionRecipe.getMap());
        translationRecipe.getMap().put(DynamicParser.TRANSLATOR_TAG, translator.getName());

        dynamicParser.addTranslatorImpl(translator.getName(), translator);

        dynamicParser.cookRecipe(translationRecipe, feed,
                                 getCallbackForExpectedResult(CommentModel.class), null,
                                 null);

        // Test cook recipe using reflection
        dynamicParser.cookRecipe(reflectionRecipe, feed,
                                 getCallbackForExpectedResult(CommentModel.class), null,
                                 null);

    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with valid input parameters using the 500 comments XML feed.
     */
    @Test
    public void testCookRecipeWith500CommentsXmlFeed() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();
        dynamicParser.setAsyncMode(false);
        CommentTranslator translator = new CommentTranslator();

        String feed = FileHelper.readFile(InstrumentationRegistry.getContext(),
                                          "feeds/500CommentsFeed.xml");

        Recipe reflectionRecipe =
                createParserRecipe("DynamicParser", // cooker
                                   "xml", // format
                                   "com.amazon.dynamicparser.testResources.CommentModel", // model
                                   "array", // model type
                                   null, // translator
                                   "comments/comment", // query
                                   null, // query result type
                                   null, // key data path
                                   new ArrayList<>(Arrays.asList("postId/#text@mPostId", // match
                                                                 // list
                                                                 "id/#text@mId",
                                                                 "name/#text@mName",
                                                                 "email/#text@mEmail",
                                                                 "body/#text@mBody")));

        Recipe translationRecipe = new Recipe();
        translationRecipe.setMap(reflectionRecipe.getMap());
        translationRecipe.getMap().put(DynamicParser.TRANSLATOR_TAG, translator.getName());

        dynamicParser.addTranslatorImpl(translator.getName(), translator);

        dynamicParser.cookRecipe(translationRecipe, feed,
                                 getCallbackForExpectedResult(CommentModel.class), null, null);

        // Test cook recipe using reflection
        dynamicParser.cookRecipe(reflectionRecipe, feed,
                                 getCallbackForExpectedResult(CommentModel.class), null, null);

    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with valid input parameters using the 5000 photos JSON feed.
     */
    @Test
    public void testCookRecipeWith5000PhotosJsonFeed() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        PhotoTranslator translator = new PhotoTranslator();

        String feed = FileHelper.readFile(InstrumentationRegistry.getContext(),
                                          "feeds/5000PhotosFeed.json");

        Recipe reflectionRecipe =
                createParserRecipe("DynamicParser", //cooker
                                   "json", // format
                                   "com.amazon.dynamicparser.testResources.PhotoModel", // model
                                   "array", // model type
                                   null, // translator
                                   "$.photos", // query
                                   null, // query result type
                                   null, // key data path
                                   new ArrayList<>(Arrays.asList( // match list
                                                                  "albumId@albumId",
                                                                  "id@id",
                                                                  "title@title",
                                                                  "url@url",
                                                                  "thumbnailUrl@thumbnailUrl")));

        Recipe translationRecipe = new Recipe();
        translationRecipe.setMap(reflectionRecipe.getMap());
        translationRecipe.getMap().put(DynamicParser.TRANSLATOR_TAG, translator.getName());

        dynamicParser.addTranslatorImpl(translator.getName(), translator);

        dynamicParser.cookRecipe(translationRecipe, feed,
                                 getCallbackForExpectedResult(PhotoModel.class), null,
                                 null);

        dynamicParser.cookRecipe(reflectionRecipe, feed,
                                 getCallbackForExpectedResult(PhotoModel.class), null,
                                 null);
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with valid input parameters using the 5000 photos XML feed.
     */
    @Test
    public void testCookRecipeWith5000PhotosXmlFeed() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        PhotoTranslator translator = new PhotoTranslator();

        String feed = FileHelper.readFile(InstrumentationRegistry.getContext(),
                                          "feeds/5000PhotosFeed.xml");

        Recipe reflectionRecipe =
                createParserRecipe("DynamicParser", //cooker
                                   "xml", // format
                                   "com.amazon.dynamicparser.testResources.PhotoModel", // model
                                   "array", // model type
                                   null, // translator
                                   "photos/photo", // query
                                   null, // query result type
                                   null, // key data path
                                   new ArrayList<>(Arrays.asList( // match list
                                                                  "albumId/#text@albumId",
                                                                  "id/#text@id",
                                                                  "title/#text@title",
                                                                  "url/#text@url",
                                                                  "thumbnailUrl/#text@thumbnailUrl")));

        Recipe translationRecipe = new Recipe();
        translationRecipe.setMap(reflectionRecipe.getMap());
        translationRecipe.getMap().put(DynamicParser.TRANSLATOR_TAG, translator.getName());

        dynamicParser.addTranslatorImpl(translator.getName(), translator);

        dynamicParser.cookRecipe(translationRecipe, feed,
                                 getCallbackForExpectedResult(PhotoModel.class), null, null);

        dynamicParser.cookRecipe(reflectionRecipe, feed,
                                 getCallbackForExpectedResult(PhotoModel.class), null, null);
    }

    /**
     * Tests the {@link DynamicParser#cancelTranslationTasks()} method using a large feed of 5000
     * items. The feed should be large enough that we can prove canceling the task works before the
     * task has a chance to finish.
     */
    @Test
    public void testCancelTranslationTasks() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();
        dynamicParser.setAsyncMode(true);

        // Test the method using a large JSON feed.
        String feed = FileHelper.readFile(InstrumentationRegistry.getContext(),
                                          "feeds/5000PhotosFeed.json");

        Recipe reflectionRecipe =
                createParserRecipe("DynamicParser", //cooker
                                   "json", // format
                                   "com.amazon.dynamicparser.testResources.PhotoModel", // model
                                   "array", // model type
                                   null, // translator
                                   "$.photos", // query
                                   null, // query result type
                                   null, // key data path
                                   new ArrayList<>(Arrays.asList( // match list
                                                                  "albumId@albumId",
                                                                  "id@id",
                                                                  "title@title",
                                                                  "url@url",
                                                                  "thumbnailUrl@thumbnailUrl")));

        dynamicParser.cookRecipe(reflectionRecipe, feed,
                                 new IRecipeCookerCallbacks() {
                                     @Override
                                     public void onPreRecipeCook(Recipe recipe, Object output,
                                                                 Bundle bundle) {

                                     }

                                     @Override
                                     public void onRecipeCooked(Recipe recipe, Object output,
                                                                Bundle bundle, boolean done) {

                                         assertTrue("Should not have reached this", false);
                                     }

                                     @Override
                                     public void onPostRecipeCooked(Recipe recipe, Object output,
                                                                    Bundle bundle) {

                                     }

                                     @Override
                                     public void onRecipeError(Recipe recipe, Exception e, String
                                             msg) {

                                         assertTrue("Should not have reached this", false);

                                     }
                                 }, null,
                                 null);

        dynamicParser.cancelTranslationTasks();
        assertFalse(dynamicParser.areTasksInProgress());

        // Test the method using a large XML feed.
        feed = FileHelper.readFile(InstrumentationRegistry.getContext(),
                                   "feeds/5000PhotosFeed.xml");

        reflectionRecipe =
                createParserRecipe("DynamicParser", //cooker
                                   "xml", // format
                                   "com.amazon.dynamicparser.testResources.PhotoModel", // model
                                   "array", // model type
                                   null, // translator
                                   "photos/photo", // query
                                   null, // query result type
                                   null, // key data path
                                   new ArrayList<>(Arrays.asList( // match list
                                                                  "albumId/#text@albumId",
                                                                  "id/#text@id",
                                                                  "title/#text@title",
                                                                  "url/#text@url",
                                                                  "thumbnailUrl/#text@thumbnailUrl")));

        dynamicParser.cookRecipe(reflectionRecipe, feed,
                                 new IRecipeCookerCallbacks() {
                                     @Override
                                     public void onPreRecipeCook(Recipe recipe, Object output,
                                                                 Bundle bundle) {

                                     }

                                     @Override
                                     public void onRecipeCooked(Recipe recipe, Object output,
                                                                Bundle bundle, boolean done) {

                                         assertTrue("Should not have reached this", false);
                                     }

                                     @Override
                                     public void onPostRecipeCooked(Recipe recipe, Object output,
                                                                    Bundle bundle) {

                                     }

                                     @Override
                                     public void onRecipeError(Recipe recipe, Exception e, String
                                             msg) {

                                         assertTrue("Should not have reached this", false);

                                     }
                                 }, null,
                                 null);

        dynamicParser.cancelTranslationTasks();
        assertFalse(dynamicParser.areTasksInProgress());
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with valid input parameters using {@link ScreenFeedContainer} model.
     */
    @Test
    public void testScreenFeedContainerRecipe() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        ScreenFeedContainerTranslator translator = new ScreenFeedContainerTranslator();

        // Read Media-rss file
        String feed = FileHelper.readFile(InstrumentationRegistry.getContext(),
                                          "feeds/ActionSports.xml");

        Recipe reflectionRecipe =
                createParserRecipe("DynamicParser", // cooker
                                   "xml", // format
                                   "com.amazon.dynamicparser.testResources.ScreenFeedContainer",
                                   // model
                                   "array", // model type
                                   null, // translator
                                   "rss/channel", // query
                                   "{}", // query result type
                                   null, // key data path
                                   new ArrayList<>(Arrays.asList("title/#text@mTitle", // match list
                                                                 "link/#text@mLink",
                                                                 "description/#text@mDescription",
                                                                 "docs/#text@mDocs",
                                                                 "generator/#text@mGenerator",
                                                                 "lastBuildDate/#text@mLastBuildDate",
                                                                 "ttl/#text@mTtl")));

        Recipe translationRecipe = new Recipe();
        translationRecipe.setMap(reflectionRecipe.getMap());
        translationRecipe.getMap().put(DynamicParser.TRANSLATOR_TAG, translator.getName());

        dynamicParser.addTranslatorImpl(translator.getName(), translator);

        // Test cook recipe using translation
        dynamicParser.cookRecipe(translationRecipe, feed,
                                 getCallbackForExpectedResult(ScreenFeedContainer.class), null,
                                 null);

        // Test cook recipe using reflection
        dynamicParser.cookRecipe(reflectionRecipe, feed,
                                 getCallbackForExpectedResult(ScreenFeedContainer.class), null,
                                 null);
    }

    /**
     * Tests parsing json file containing a list of strings to a String object.
     * @throws Exception
     */
    @Test
    public void testParseToString() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();
        dynamicParser.setBatchMode(true);

        Recipe recipe = Recipe.newInstance(InstrumentationRegistry.getContext(),
                                           "recipes/ParseStringObjectRecipe.json");

        String feed = FileHelper.readFile(InstrumentationRegistry.getContext(),
                                          "feeds/ContentIdFeed.json");

        dynamicParser.cookRecipe(
                recipe, feed,
                new IRecipeCookerCallbacks() {
                    @Override
                    public void onPreRecipeCook(Recipe recipe, Object output,
                                                Bundle bundle) {

                    }

                    @Override
                    public void onRecipeCooked(Recipe recipe, Object output,
                                               Bundle bundle, boolean done) {

                        assertNotNull(output);

                        List<String> ids = (List<String>) output;
                        assertEquals("Should have returned 3 items", 3, ids.size());
                    }

                    @Override
                    public void onPostRecipeCooked(Recipe recipe, Object output, Bundle bundle) {

                    }

                    @Override
                    public void onRecipeError(Recipe recipe, Exception e, String msg) {

                        // Force failure if this happens.
                        assertTrue("Recipe should have been cooked without error", false);
                    }
                }, null, null);
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with valid input parameters using ActionSports.xml feed.
     */
    @Test
    public void testActionSportsFeed() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        ScreenFeedContentTranslator translator = new ScreenFeedContentTranslator();

        String feed = FileHelper.readFile(InstrumentationRegistry.getContext(),
                                          "feeds/ActionSports.xml");

        Recipe reflectionRecipe =
                createParserRecipe("DynamicParser", // cooker
                                   "xml", // format
                                   "com.amazon.dynamicparser.testResources.ScreenFeedContent", // model
                                   "array", // model type
                                   null, // translator
                                   "rss/channel/item", // query
                                   null, // query result type
                                   null, // key data path
                                   new ArrayList<>(Arrays.asList("title/#text@mTitle", // match list
                                                                 "guid/#text@mGuid",
                                                                 "category/#text@mCategory",
                                                                 "pubDate/#text@mPubDate",
                                                                 "media:content/#attributes/url@mUrl",
                                                                 "media:content/#attributes/fileSize@mFileSize",
                                                                 "media:content/#attributes/type@mType",
                                                                 "media:content/#attributes/medium@mMedium",
                                                                 "media:content/#attributes/duration@mDuration",
                                                                 "media:content/#attributes/height@mHeight",
                                                                 "media:content/#attributes/width@mWidth",
                                                                 "media:credit/#text@mCredit")));

        Recipe translationRecipe = new Recipe();
        translationRecipe.setMap(reflectionRecipe.getMap());
        translationRecipe.getMap().put(DynamicParser.TRANSLATOR_TAG, translator.getName());

        dynamicParser.addTranslatorImpl(translator.getName(), translator);

        // Test cook recipe using translation
        dynamicParser.cookRecipe(translationRecipe, feed,
                                 getCallbackForExpectedResult(ScreenFeedContent.class), null, null);

        // Test cook recipe using reflection
        dynamicParser.cookRecipe(reflectionRecipe, feed,
                                 getCallbackForExpectedResult(ScreenFeedContent.class), null, null);
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with valid input parameters using Destinations.xml feed.
     */
    @Test
    public void testDestinationsFeed() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        ScreenFeedContentTranslator translator = new ScreenFeedContentTranslator();

        String feed = FileHelper.readFile(InstrumentationRegistry.getContext(),
                                          "feeds/Destinations.xml");

        Recipe reflectionRecipe =
                createParserRecipe("DynamicParser", // cooker
                                   "xml", // format
                                   "com.amazon.dynamicparser.testResources.ScreenFeedContent", //
                                   // model
                                   "array", // model type
                                   null, // translator
                                   "rss/channel/item", // query
                                   null, // query result type
                                   null, // key data path
                                   new ArrayList<>(Arrays.asList("title/#text@mTitle", // match list
                                                                 "guid/#text@mGuid",
                                                                 "category/#text@mCategory",
                                                                 "pubDate/#text@mPubDate",
                                                                 "media:content/#attributes/url@mUrl",
                                                                 "media:content/#attributes/fileSize@mFileSize",
                                                                 "media:content/#attributes/type@mType",
                                                                 "media:content/#attributes/medium@mMedium",
                                                                 "media:content/#attributes/duration@mDuration",
                                                                 "media:content/#attributes/height@mHeight",
                                                                 "media:content/#attributes/width@mWidth")));

        Recipe translationRecipe = new Recipe();
        translationRecipe.setMap(reflectionRecipe.getMap());
        translationRecipe.getMap().put(DynamicParser.TRANSLATOR_TAG, translator.getName());

        dynamicParser.addTranslatorImpl(translator.getName(), translator);

        // Test cook recipe using translation
        dynamicParser.cookRecipe(translationRecipe, feed,
                                 getCallbackForExpectedResult(ScreenFeedContent.class),
                                 null, null);

        // Test cook recipe using reflection
        dynamicParser.cookRecipe(reflectionRecipe, feed,
                                 getCallbackForExpectedResult(ScreenFeedContent.class), null, null);
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with valid input parameters using NewsBites.xml feed.
     */
    @Test
    public void testNewsBitesFeed() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        ScreenFeedContentTranslator translator = new ScreenFeedContentTranslator();

        String feed = FileHelper.readFile(InstrumentationRegistry.getContext(),
                                          "feeds/NewsBites.xml");

        Recipe reflectionRecipe =
                createParserRecipe("DynamicParser", // cooker
                                   "xml", // format
                                   "com.amazon.dynamicparser.testResources.ScreenFeedContent", //
                                   // model
                                   "array", // model type
                                   null, // translator
                                   "rss/channel/item", // query
                                   null, // query result type
                                   null, // key data path
                                   new ArrayList<>(Arrays.asList("title/#text@mTitle", // match list
                                                                 "description/#text@mDescription",
                                                                 "guid/#text@mGuid",
                                                                 "category/#text@mCategory",
                                                                 "pubDate/#text@mPubDate",
                                                                 "media:content/#attributes/url@mUrl",
                                                                 "media:content/#attributes/fileSize@mFileSize",
                                                                 "media:content/#attributes/type@mType",
                                                                 "media:content/#attributes/medium@mMedium",
                                                                 "media:content/#attributes/duration@mDuration",
                                                                 "media:content/#attributes/height@mHeight",
                                                                 "media:content/#attributes/width@mWidth",
                                                                 "media:credit/#text@mCredit")));

        Recipe translationRecipe = new Recipe();
        translationRecipe.setMap(reflectionRecipe.getMap());
        translationRecipe.getMap().put(DynamicParser.TRANSLATOR_TAG, translator.getName());

        dynamicParser.addTranslatorImpl(translator.getName(), translator);

        // Test cook recipe using translation
        dynamicParser.cookRecipe(translationRecipe, feed,
                                 getCallbackForExpectedResult(ScreenFeedContent.class), null, null);

        // Test cook recipe using reflection
        dynamicParser.cookRecipe(reflectionRecipe, feed,
                                 getCallbackForExpectedResult(ScreenFeedContent.class), null, null);
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with valid input parameters using TopNews.xml feed.
     */
    @Test
    public void testTopNewsFeed() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        ScreenFeedContentTranslator translator = new ScreenFeedContentTranslator();

        String feed = FileHelper.readFile(InstrumentationRegistry.getContext(),
                                          "feeds/TopNews.xml");

        Recipe reflectionRecipe =
                createParserRecipe("DynamicParser", // cooker
                                   "xml", // format
                                   "com.amazon.dynamicparser.testResources.ScreenFeedContent", //
                                   // model
                                   "array", // model type
                                   null, // translator
                                   "rss/channel/item", // query
                                   null, // query result type
                                   null, // key data path
                                   new ArrayList<>(Arrays.asList("title/#text@mTitle", // match list
                                                                 "description/#text@mDescription",
                                                                 "guid/#text@mGuid",
                                                                 "category/#text@mCategory",
                                                                 "pubDate/#text@mPubDate",
                                                                 "media:content/#attributes/url@mUrl",
                                                                 "media:content/#attributes/fileSize@mFileSize",
                                                                 "media:content/#attributes/type@mType",
                                                                 "media:content/#attributes/medium@mMedium",
                                                                 "media:content/#attributes/duration@mDuration",
                                                                 "media:content/#attributes/height@mHeight",
                                                                 "media:content/#attributes/width@mWidth",
                                                                 "media:credit/#text@mCredit")));

        Recipe translationRecipe = new Recipe();
        translationRecipe.setMap(reflectionRecipe.getMap());
        translationRecipe.getMap().put(DynamicParser.TRANSLATOR_TAG, translator.getName());

        dynamicParser.addTranslatorImpl(translator.getName(), translator);

        // Test cook recipe using translation
        dynamicParser.cookRecipe(translationRecipe, feed,
                                 getCallbackForExpectedResult(ScreenFeedContent.class), null, null);

        // Test cook recipe using reflection
        dynamicParser.cookRecipe(reflectionRecipe, feed,
                                 getCallbackForExpectedResult(ScreenFeedContent.class), null, null);
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with valid input parameters using WeatherForecast.xml feed.
     */
    @Test
    public void testWeatherForecastFeed() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        ScreenFeedContentTranslator translator = new ScreenFeedContentTranslator();

        String feed = FileHelper.readFile(InstrumentationRegistry.getContext(),
                                          "feeds/WeatherForecast.xml");

        Recipe reflectionRecipe =
                createParserRecipe("DynamicParser", // cooker
                                   "xml", // format
                                   "com.amazon.dynamicparser.testResources.ScreenFeedContent", //
                                   // model
                                   "array", // model type
                                   null, // translator
                                   "rss/channel/item", // query
                                   null, // query result type
                                   null, // key data path
                                   new ArrayList<>(Arrays.asList("title/#text@mTitle", // match list
                                                                 "guid/#text@mGuid",
                                                                 "category/#text@mCategory",
                                                                 "pubDate/#text@mPubDate",
                                                                 "media:content/#attributes/url@mUrl",
                                                                 "media:content/#attributes/fileSize@mFileSize",
                                                                 "media:content/#attributes/type@mType",
                                                                 "media:content/#attributes/medium@mMedium",
                                                                 "media:content/#attributes/duration@mDuration",
                                                                 "media:content/#attributes/height@mHeight",
                                                                 "media:content/#attributes/width@mWidth",
                                                                 "media:credit/#text@mCredit")));

        Recipe translationRecipe = new Recipe();
        translationRecipe.setMap(reflectionRecipe.getMap());
        translationRecipe.getMap().put(DynamicParser.TRANSLATOR_TAG, translator.getName());

        dynamicParser.addTranslatorImpl(translator.getName(), translator);

        // Test cook recipe using translation
        dynamicParser.cookRecipe(translationRecipe, feed,
                                 getCallbackForExpectedResult(ScreenFeedContent.class), null, null);

        // Test cook recipe using reflection
        dynamicParser.cookRecipe(reflectionRecipe, feed,
                                 getCallbackForExpectedResult(ScreenFeedContent.class), null, null);
    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with a json feed that is a single list.
     */
    @Test
    public void testSingleListJsonFeed() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        String feed = "[ \"cat1\", \"cat2\", \"cat3\" ]";

        Recipe recipe =
                createParserRecipe("DynamicParser", // cooker
                                   "json", // format
                                   "com.amazon.dynamicparser.testResources" +
                                           ".DummyLightCastContainer", // model
                                   "array", // model type
                                   null, // translator
                                   "$", // query
                                   "[]$", // query result type
                                   null, // key data path
                                   new ArrayList<>(Collections.singletonList
                                           ("StringKey@name"))); // match list

        dynamicParser.cookRecipe(recipe, feed,
                                 getCallbackForExpectedResult(DummyLightCastContainer.class),
                                 null, null);

    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with a xml feed that is a single list.
     */
    @Test
    public void testSingleListXmlFeed() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        String feed = "<list><item>cat1</item><item>cat2</item><item>cat3</item></list>";

        Recipe recipe =
                createParserRecipe("DynamicParser", // cooker
                                   "xml", // format
                                   "com.amazon.dynamicparser.testResources" +
                                           ".DummyLightCastContainer", // model
                                   "array", // model type
                                   null, // translator
                                   "list/item/text()", // query
                                   "[]$", // query result type
                                   null, // key data path
                                   new ArrayList<>(Collections.singletonList
                                           ("StringKey@name"))); // match list

        dynamicParser.cookRecipe(recipe, feed,
                                 getCallbackForExpectedResult(DummyLightCastContainer.class),
                                 null, null);

    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with a json feed that is a single object.
     */
    @Test
    public void testSingleObjectJsonFeed() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        String feed = "{ \"name\" : \"category1\" }";
        Recipe recipe =
                createParserRecipe("DynamicParser", // cooker
                                   "json", // format
                                   "com.amazon.dynamicparser.testResources" +
                                           ".DummyLightCastContainer", // model
                                   "array", // model type
                                   null, // translator
                                   "$", // query
                                   "{}", // query result type
                                   null, // key data path
                                   new ArrayList<>(Collections.singletonList
                                           ("name@name"))); // match list

        dynamicParser.cookRecipe(recipe, feed,
                                 getCallbackForExpectedResult(DummyLightCastContainer.class),
                                 null, null);

    }

    /**
     * Tests the {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle,
     * String[])} method with a xml feed that is a single object.
     */
    @Test
    public void testSingleObjectXmlFeed() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        String feed = "<name>category1</name>";
        Recipe recipe =
                createParserRecipe("DynamicParser", // cooker
                                   "xml", // format
                                   "com.amazon.dynamicparser.testResources" +
                                           ".DummyLightCastContainer", // model
                                   "array", // model type
                                   null, // translator
                                   "*", // query
                                   "{}", // query result type
                                   null, // key data path
                                   new ArrayList<>(Collections.singletonList
                                           ("#text@name"))); // match list

        dynamicParser.cookRecipe(recipe, feed,
                                 getCallbackForExpectedResult(DummyLightCastContainer.class),
                                 null, null);

    }

    /**
     * Tests the {@link DynamicParser#getName()} method.
     */
    @Test
    public void testGetName() throws Exception {

        DynamicParser dynamicParser = new DynamicParser();

        assertEquals("Expected name should be DynamicParser", DynamicParser.class.getSimpleName()
                , dynamicParser.getName());
    }

    /**
     * Creates an {@link IRecipeCookerCallbacks} implementation to use to verify the results of the
     * {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle, String[])}
     * method. Specially this implementation is expecting {@link
     * IRecipeCookerCallbacks#onRecipeCooked(Recipe, Object, Bundle, boolean)} to be called.
     *
     * @param type The expected class of the items.
     * @return An {@link IRecipeCookerCallbacks} that checks for the expected output.
     */
    private IRecipeCookerCallbacks getCallbackForExpectedResult(final Class type) {

        return new IRecipeCookerCallbacks() {
            @Override
            public void onPreRecipeCook(Recipe recipe, Object output, Bundle bundle) {

            }

            @Override
            public void onRecipeCooked(Recipe recipe, Object output, Bundle bundle, boolean done) {

                assertNotNull(output);

                assertEquals(output.getClass(), type);
            }

            @Override
            public void onPostRecipeCooked(Recipe recipe, Object output, Bundle bundle) {

            }

            @Override
            public void onRecipeError(Recipe recipe, Exception e, String msg) {

                // Force failure if this happens.
                assertTrue("Recipe should have been cooked without error", false);
            }
        };
    }

    /**
     * Creates an {@link IRecipeCookerCallbacks} implementation to use to verify the results of the
     * {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle, String[])}
     * method. Specially this implementation is expecting {@link
     * IRecipeCookerCallbacks#onRecipeCooked(Recipe, Object, Bundle, boolean)} to be called.
     *
     * @param expected The expected number of items to be parsed.
     * @param type     The expected class of the items.
     * @return An {@link IRecipeCookerCallbacks} that checks for the expected output.
     */
    private IRecipeCookerCallbacks getCallbackForExpectedResultList(final int expected, final Class
            type) {

        return new IRecipeCookerCallbacks() {
            @Override
            public void onPreRecipeCook(Recipe recipe, Object output, Bundle bundle) {

            }

            @Override
            public void onRecipeCooked(Recipe recipe, Object output, Bundle bundle, boolean done) {

                assertNotNull(output);

                List<Object> objects = (List<Object>) output;
                if (objects.size() > 0) {
                    assertEquals(objects.get(0).getClass(), type);
                }
                assertEquals("The output should contain a list of " + expected + " objects",
                             expected, objects.size());
            }

            @Override
            public void onPostRecipeCooked(Recipe recipe, Object output, Bundle bundle) {

            }

            @Override
            public void onRecipeError(Recipe recipe, Exception e, String msg) {

                // Force failure if this happens.
                assertTrue("Recipe should have been cooked without error", false);
            }
        };
    }

    /**
     * Creates an {@link IRecipeCookerCallbacks} implementation to use to verify the results of the
     * {@link DynamicParser#cookRecipe(Recipe, Object, IRecipeCookerCallbacks, Bundle, String[])}
     * method. Specifically this implementation is expecting {@link
     * IRecipeCookerCallbacks#onRecipeError(Recipe, Exception, String)} to be called.
     *
     * @param expected The Class of the expected Exception.
     * @return An {@link IRecipeCookerCallbacks} that checks for the expected error.
     */
    private IRecipeCookerCallbacks getCallbackWithExpectedException(final Class expected,
                                                                    boolean callCooked) {

        return new IRecipeCookerCallbacks() {
            @Override
            public void onPreRecipeCook(Recipe recipe, Object output, Bundle bundle) {

            }

            @Override
            public void onRecipeCooked(Recipe recipe, Object output, Bundle bundle, boolean done) {

                // Force failure if this happens.
                assertTrue("Recipe shouldn't be properly cooked", callCooked);
            }

            @Override
            public void onPostRecipeCooked(Recipe recipe, Object output, Bundle bundle) {

            }

            @Override
            public void onRecipeError(Recipe recipe, Exception e, String msg) {

                assertNotNull(e);
                assertTrue(e.getClass().equals(expected));

            }
        };
    }

    /**
     * Private helper method to create a reflection recipe for the "SampleVideoFeed.json" feed.
     * This
     * recipe is used often enough to justify this method.
     *
     * @return The recipe.
     */
    private Recipe createJsonSampleVideoContainerReflectionRecipe() {

        return createParserRecipe("DynamicParser", // cooker
                                  "json", // format
                                  "com.amazon.dynamicparser.testResources.DummyContainer", // model
                                  "array", // model type
                                  null, // translator
                                  "$.categories[?(@.type == 'category')]", // query
                                  null, // query result type
                                  null, // key data path
                                  new ArrayList<>(Arrays.asList("info/title@mName", // match list
                                                                "categoryId@mId",
                                                                "info/long@mLong",
                                                                "info/char@mChar",
                                                                "info/double@mDouble",
                                                                "info/boolean@mBoolean",
                                                                "info/float@mFloat",
                                                                "info/short@mShort",
                                                                "info/byte@mByte")));
    }

    /**
     * Private helper method to create a reflection recipe for the "SampleVideoFeed.xml" feed.
     * This
     * recipe is used often to justify this method.
     *
     * @return The recipe.
     */
    private Recipe createXmlSampleVideoContainterReflectionRecipe() {

        return createParserRecipe("DynamicParser", // cooker
                                  "xml", //format
                                  "com.amazon.dynamicparser.testResources.DummyContainer", // model
                                  "array", // model type
                                  null, // translator
                                  "sample/categories[type='category']", // query
                                  null, // query result type
                                  null, // key data path
                                  new ArrayList<>(Arrays.asList( // match list
                                                                 "info/title/#text@mName",
                                                                 "categoryId/#text@mId",
                                                                 "info/long/#text@mLong",
                                                                 "info/char/#text@mChar",
                                                                 "info/double/#text@mDouble",
                                                                 "info/boolean/#text@mBoolean",
                                                                 "info/float/#text@mFloat",
                                                                 "info/short/#text@mShort",
                                                                 "info/byte/#text@mByte")));
    }

    /**
     * Create a parser recipe to use with testing.
     *
     * @param recipeCooker The recipe cooker type.
     * @param format       The data format.
     * @param model        The model to translate the data in to.
     * @param modelType    The type of model.
     * @param translator   The name of the translator to use, or null to use reflection.
     * @param query        The parse query.
     * @param keyDataPath  The data entry point.
     * @param matchList    The list of match strings between the data feed and model object
     *                     properties.
     * @return The parser recipe.
     */
    private Recipe createParserRecipe(String recipeCooker, String format, String model,
                                      String modelType, String translator, String query,
                                      String queryResultType,
                                      String keyDataPath, List<String> matchList) {

        Map<String, Object> map = new HashMap<>();

        if (recipeCooker != null) {
            map.put(DynamicParser.COOKER_TAG, recipeCooker);
        }
        if (format != null) {
            map.put(DynamicParser.FORMAT_TAG, format);
        }
        if (model != null) {
            map.put(DynamicParser.MODEL_TAG, model);
        }
        if (modelType != null) {
            map.put(DynamicParser.MODEL_TYPE_TAG, modelType);
        }
        if (translator != null) {
            map.put(DynamicParser.TRANSLATOR_TAG, translator);
        }
        if (query != null) {
            map.put(DynamicParser.QUERY_TAG, query);
        }
        if (queryResultType != null) {
            map.put(DynamicParser.QUERY_RESULT_TAG, queryResultType);
        }
        if (matchList != null) {
            map.put(DynamicParser.MATCH_LIST_TAG, matchList);
        }
        if (keyDataPath != null) {
            map.put(DynamicParser.KEY_DATA_PATH_TAG, keyDataPath);
        }

        Recipe recipe = new Recipe();
        recipe.setMap(map);

        return recipe;
    }

}
