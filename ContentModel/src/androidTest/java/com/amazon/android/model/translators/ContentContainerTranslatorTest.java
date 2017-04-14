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

import com.amazon.android.model.content.ContentContainer;
import com.amazon.android.recipe.Recipe;
import com.amazon.android.model.AModelTranslator;
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
 * This class tests the {@link ContentContainerTranslator} class.
 */
public class ContentContainerTranslatorTest {

    ContentContainerTranslator mTranslator;
    Recipe mGoodRecipe;

    @Before
    public void setUp() throws Exception {

        mTranslator = new ContentContainerTranslator();

        mGoodRecipe = Recipe.newInstance(FileHelper.readFile(InstrumentationRegistry.getContext(),
                                                             "GoodContainerRecipe.json"));
    }

    /**
     * Tests the {@link ContentContainerTranslator#setMemberVariable(ContentContainer, String,
     * Object)} method for the name member variable.
     */
    @Test
    public void testSetMemberVariableName() throws Exception {

        String nameText = "Category";
        ContentContainer container = new ContentContainer();

        assertTrue(mTranslator.setMemberVariable(container, ContentContainer.NAME_FIELD_NAME,
                                                 nameText));
        assertEquals(container.getName(), nameText);
    }

    /**
     * Tests the {@link ContentContainerTranslator#setMemberVariable(ContentContainer, String,
     * Object)} for the extras member variable.
     */
    @Test
    public void testSetMemberVariableExtras() throws Exception {

        String extraKey = "key";
        String extraValue = "value";

        ContentContainer container = new ContentContainer();

        assertTrue(mTranslator.setMemberVariable(container, extraKey, extraValue));

        assertEquals(container.getExtraStringValue(extraKey), extraValue);
    }

    /**
     * Tests the {@link ContentContainerTranslator#setMemberVariable(ContentContainer, String,
     * Object)} with a value that has an unexpected type.
     */
    @Test
    public void testSetMemberVariableNameWithBadValue() throws Exception {

        ContentContainer container = new ContentContainer();

        assertFalse(mTranslator.setMemberVariable(container, ContentContainer.NAME_FIELD_NAME,
                                                  new HashMap<>()));
    }

    /**
     * Tests the {@link ContentContainerTranslator#setMemberVariable(ContentContainer, String,
     * Object)} with null arguments.
     */
    @Test
    public void testSetMemberVariableFalseCases() throws Exception {

        ContentContainer container = new ContentContainer();
        String extraKey = "key";
        String extraValue = "value";

        assertFalse(mTranslator.setMemberVariable(null, extraKey, extraValue));
        assertFalse(mTranslator.setMemberVariable(container, null, extraValue));
        assertFalse(mTranslator.setMemberVariable(container, "", extraValue));
        assertFalse(mTranslator.setMemberVariable(container, extraKey, null));
    }

    /**
     * Tests the {@link ContentContainerTranslator#validateModel(ContentContainer)} method.
     */
    @Test
    public void testValidateModel() throws Exception {

        ContentContainer container = new ContentContainer();

        // Container is empty with no name, shouldn't be valid.
        assertFalse(mTranslator.validateModel(container));

        container.setName("Name");

        // Container has name set, should be valid.
        assertTrue(mTranslator.validateModel(container));

        // A null container is not a valid container.
        assertFalse(mTranslator.validateModel(null));
    }

    /**
     * Tests the {@link ContentContainerTranslator#instantiateModel()} method.
     */
    @Test
    public void testInstantiateModel() throws Exception {

        assertNotNull(mTranslator.instantiateModel());
    }

    /**
     * Tests the {@link ContentContainerTranslator#mapToModel(Map, Recipe)} method with a null map
     * argument.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMapToModelNullMapCase() throws Exception {

        mTranslator.mapToModel(null, mGoodRecipe);
    }

    /**
     * Tests the {@link ContentContainerTranslator#mapToModel(Map, Recipe)} method with a null
     * recipe argument.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMapToModelNullRecipeCase() throws Exception {

        mTranslator.mapToModel(createValidMap(), null);
    }

    /**
     * Tests the {@link ContentContainerTranslator#mapToModel(Map, Recipe)} method with a valid map
     * and recipe to get expected output.
     */
    @Test
    public void testMapToModel() throws Exception {

        ContentContainer expected = createValidContainer();

        ContentContainer result = mTranslator.mapToModel(createValidMap(), mGoodRecipe);

        assertEquals(expected, result);
    }

    /**
     * Tests the {@link ContentContainerTranslator#mapToModel(Map, Recipe)} method with a bad
     * recipe.
     */
    @Test
    public void testMapToModelWithBadRecipe() throws Exception {

        Recipe badRecipe = Recipe.newInstance(FileHelper.readFile(InstrumentationRegistry
                                                                          .getContext(),
                                                                  "BadContainerRecipe.json"));

        ContentContainer container = mTranslator.mapToModel(createValidMap(), badRecipe);
        assertNull("ContentContainer should be null", container);
    }

    /**
     * Tests the {@link ContentContainerTranslator#mapToModel(Map, Recipe)} method with a bad map.
     */
    @Test(expected = AModelTranslator.TranslationException.class)
    public void testMapToModelWithBadMap() throws Exception {

        Map<String, Object> map = createValidMap();
        map.remove("name");

        mTranslator.mapToModel(map, mGoodRecipe);
    }

    /**
     * Tests the {@link ContentContainerTranslator#mapListToModelList(List, Recipe)} with a null
     * map list.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMapToModelListNullMapCase() throws Exception {

        mTranslator.mapListToModelList(null, mGoodRecipe);
    }

    /**
     * Tests the {@link ContentContainerTranslator#mapListToModelList(List, Recipe)} with a null
     * recipe.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMapToModelListNullRecipeCase() throws Exception {

        mTranslator.mapListToModelList(new ArrayList<Map<String, Object>>(), null);
    }

    /**
     * Tests the {@link ContentTranslator#mapListToModelList(List, Recipe)} with good input
     * arguments.
     */
    @Test
    public void testMapToModelList() throws Exception {

        List<Map<String, Object>> mapList = new ArrayList<>();
        List<ContentContainer> expected = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            mapList.add(createValidMap());
            expected.add(createValidContainer());
        }

        List<ContentContainer> result = mTranslator.mapListToModelList(mapList, mGoodRecipe);

        assertEquals(expected, result);
    }

    /**
     * Tests the {@link ContentTranslator#getName()} method.
     */
    @Test
    public void testGetName() throws Exception {

        assertEquals(ContentContainerTranslator.class.getSimpleName(), mTranslator.getName());
    }

    /**
     * Creates a valid map to test translation with. Should produce a {@link ContentContainer} that
     * matches the result of {@link #createValidContainer()}.
     */
    private Map<String, Object> createValidMap() {

        Map<String, Object> map = new HashMap<>();
        map.put("name", "Content Container");
        map.put("id", "100");
        return map;
    }

    /**
     * Creates a valid {@link ContentContainer} model to test with.
     */
    private ContentContainer createValidContainer() {

        ContentContainer container = new ContentContainer("Content Container");
        container.setExtraValue("id", "100");
        return container;
    }
}
