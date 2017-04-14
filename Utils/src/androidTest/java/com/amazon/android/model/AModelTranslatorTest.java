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
package com.amazon.android.model;

import com.amazon.android.model.testresources.DummyModelTranslator;
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
import static org.junit.Assert.assertNull;

/**
 * This class tests the {@link AModelTranslator} interface.
 */
public class AModelTranslatorTest {

    /*
     * The instance of the AModelTranslator implementation to test.
     */
    private DummyModelTranslator mTranslator;
    /*
     * The expected object from the translation.
     */
    private DummyModelTranslator.TestObject mTestObject;
    /*
     * The map to translate into the TestObject
     */
    private HashMap<String, Object> mMap;
    /*
     * The recipe that provides the matchList used in translation.
     */
    private Recipe mRecipe;

    /**
     * Set up the test data.
     */
    @Before
    public void setUp() throws Exception {

        mTranslator = new DummyModelTranslator();

        mTestObject = mTranslator.new TestObject("field1", "field2");
        mMap = new HashMap<>();
        mMap.put("field1", "field1");
        mMap.put("field2", "field2");

        mRecipe = Recipe.newInstance(FileHelper.readFile(InstrumentationRegistry.getContext(),
                                                         "TestObjectRecipe.json"));

    }

    /**
     * Tests the the {@link DummyModelTranslator#mapToModel(Map, Recipe)} method works as expected.
     */
    @Test
    public void testMapToModel() throws Exception {

        assertEquals(mTestObject, mTranslator.mapToModel(mMap, mRecipe));
    }

    /**
     * Tests that the {@link DummyModelTranslator#mapListToModelList(List, Recipe)} method works as
     * expected.
     */
    @Test
    public void testMapToModelList() throws Exception {

        // Create test map list.
        List<Map<String, Object>> mapList = new ArrayList<>();

        // Create expected list of TestObjects

        // Fill the test map and list of TestObjects.
        List<DummyModelTranslator.TestObject> testObjectList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            mapList.add(mMap);
            testObjectList.add(mTestObject);
        }

        assertEquals(testObjectList, mTranslator.mapListToModelList(mapList, mRecipe));

    }

    /**
     * Tests the {@link DummyModelTranslator#mapToModel(Map, Recipe)} method with a bad match list
     * in the {@link Recipe}. A {@link AModelTranslator.TranslationException} is expected.
     */
    @Test(expected = AModelTranslator.TranslationException.class)
    public void testMapToModelWithBadMatchList() throws Exception {

        Recipe badRecipe = Recipe.newInstance(
                FileHelper.readFile(InstrumentationRegistry.getContext(),
                                    "TestObjectRecipeBadMatchList.json"));
        mTranslator.mapToModel(mMap, badRecipe);
    }

    /**
     * Tests the {@link DummyModelTranslator#mapToModel(Map, Recipe)} method with a match list that
     * doesn't match all mandatory fields of {@link com.amazon.android.model.testresources
     * .DummyModelTranslator.TestObject}.
     */
    @Test
    public void testMapToModelWithUnfinishedMatchList() throws Exception {

        Recipe badRecipe = Recipe.newInstance(
                FileHelper.readFile(InstrumentationRegistry.getContext(),
                                    "TestObjectRecipeUnfinishedMatchList.json"));
        DummyModelTranslator.TestObject object = mTranslator.mapToModel(mMap, badRecipe);
        assertNull("Object should be null due to bad translation", object);
    }

    /**
     * Tests the {@link DummyModelTranslator#getName()} method.
     */
    @Test
    public void testGetName() throws Exception {

        assertEquals(DummyModelTranslator.class.getSimpleName(), mTranslator.getName());
    }

}

