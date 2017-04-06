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
package com.amazon.android.recipe;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * RecipeTest class is a test for Recipe class.
 * Code coverage is used to make sure test touches all lines and methods.
 */
public class RecipeTest {

    private final long mExpectedNumberValue = 16500000;
    /**
     * Example recipe json string.
     */
    private final String mRecipeExample = "{/* c 0 m m 3 n t*/" +
            "\"recipeCooker\":\"DynamicParser\", " +
            "\"format\":\"json\", /*testComment*/ " +
            "\"flag\":true, /**/" +
            "\"number\":" + mExpectedNumberValue + ",/*\n*/" +
            "\"model\":\"Content\", /* */" +
            "\"modelType\":\"array\", " +
            "\"stringArray\":[\"1\", \"2\",\"3\"]," +
            "\"level1\":{ \"level1String\":\"level1String\", \"level2\": { \"level2String" +
            "\":\"level2String\" } }" +
            "}";

    /**
     * Count of elements in recipe example.
     */
    private static final int ITEM_COUNT_OF_ROOT_IN_RECIPE_EXAMPLE = 8;

    /**
     * Test set map method.
     */
    @Test
    public void testSetMap() throws Exception {

        Recipe recipe = new Recipe();
        HashMap<String, String> map = new HashMap<>();
        map.put("key", "value");
        recipe.setMap(map);

        // Test if we manage to set map correctly.
        assertEquals("value", recipe.getItem("key"));
    }

    /**
     * Test contains item method.
     */
    @Test
    public void testContainsItem() throws Exception {

        Recipe recipe = Recipe.newInstance(mRecipeExample);

        // Positive test case.
        assertTrue(recipe.containsItem("model"));

        // Negative test case.
        assertFalse(recipe.containsItem("unknownKey"));
    }

    /**
     * Test get item as string method.
     */
    @Test
    public void testGetItemAsString() throws Exception {

        Recipe recipe = new Recipe();
        HashMap<String, String> map = new HashMap<>();
        map.put("key", "value");
        recipe.setMap(map);

        // Positive test case.
        assertEquals("value", recipe.getItemAsString("key"));
    }

    /**
     * Test get item as string list method.
     */
    @Test
    public void testGetItemAsStringList() throws Exception {

        Recipe recipe = Recipe.newInstance(mRecipeExample);

        // Test if stringArray exists
        assertNotNull(recipe.getItemAsStringList("stringArray"));

        // Test string list items.
        List<String> array = recipe.getItemAsStringList("stringArray");

        assertEquals("1", array.get(0));
        assertEquals("2", array.get(1));
        assertEquals("3", array.get(2));
    }

    /**
     * Test new instance method.
     */
    @Test
    public void testNewInstance() throws Exception {
        // Test if manage to create the object.
        assertNotNull(Recipe.newInstance("{}"));
    }

    /**
     * Test is empty method.
     */
    @Test
    public void testIsEmpty() throws Exception {

        Recipe recipe = Recipe.newInstance(mRecipeExample);

        // Positive test case.
        assertFalse(recipe.isEmpty());

        // Create an empty recipe.
        Recipe emptyRecipe = Recipe.newInstance("{}");

        // Negative test case.
        assertTrue(emptyRecipe.isEmpty());
    }

    /**
     * Test get item count of root method.
     */
    @Test
    public void testGetItemCountOfRoot() throws Exception {
        // Create an empty recipe.
        Recipe emptyRecipe = Recipe.newInstance("{}");

        // Test case without items.
        assertEquals(0, emptyRecipe.getItemCountOfRoot());

        // Create an recipe with items.
        Recipe recipe = Recipe.newInstance(mRecipeExample);

        // Test case with items.
        assertEquals(ITEM_COUNT_OF_ROOT_IN_RECIPE_EXAMPLE, recipe.getItemCountOfRoot());
    }

    /**
     * Test get item as boolean method.
     */
    @Test
    public void testGetItemAsBoolean() throws Exception {

        Recipe recipe = Recipe.newInstance(mRecipeExample);

        // Positive test case.
        boolean flag = recipe.getItemAsBoolean("flag");
        assertTrue(flag);
    }

    /**
     * Test get item as int method.
     */
    @Test
    public void testGetItemAsInt() throws Exception {

        Recipe recipe = Recipe.newInstance(mRecipeExample);

        System.out.println(mRecipeExample);
        // Positive test case.
        assertEquals(mExpectedNumberValue, recipe.getItemAsInt("number"));
    }

    /**
     * Test get item method.
     */
    @Test
    public void testGetItem() throws Exception {

        Recipe recipe = Recipe.newInstance(mRecipeExample);

        // Test get boolean.
        boolean flag = recipe.getItem("flag");
        assertTrue(flag);

        // Test get int.
        int number = recipe.getItem("number");
        assertEquals(mExpectedNumberValue, number);

        // Test get string.
        String str = recipe.getItem("model");
        assertEquals("Content", str);

        // Test get with path.
        String mapStr = recipe.getItem("level1/level2/level2String");
        assertEquals("level2String", mapStr);
    }
}