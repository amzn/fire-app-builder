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

import com.amazon.android.utils.FileHelper;
import com.amazon.android.utils.JsonHelper;
import com.amazon.android.utils.PathHelper;

import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.Map;

/**
 * Recipe class is a representation of Json based Recipes which is a set of
 * instructions that describes how to achieve certain task.
 */
public class Recipe {

    /**
     * Debug tag.
     */
    private static final String TAG = Recipe.class.getSimpleName();

    /**
     * Constant tag for key data type recipe field.
     */
    public static final String KEY_DATA_TYPE_TAG = "keyDataType";

    /**
     * Constant to use for stating that all content of a feed are live videos.
     */
    public static final String LIVE_FEED_TAG = "live";

    /**
     * Constant to use for stating that content is a free or premium content.
     */
    public static final String CONTENT_TYPE_TAG = "contentType";

    /**
     * Map for recipe items.
     */
    private Map mMap;

    /**
     * Set map of recipe items.
     *
     * @param map Recipe items map.
     */
    public void setMap(Map map) {

        mMap = map;
    }

    /**
     * Get map of recipe items.
     *
     * @return Recipe items map.
     */
    public Map getMap() {

        return mMap;
    }

    /**
     * Check if recipe is empty or not.
     *
     * @return True if recipe is empty.
     */
    public boolean isEmpty() {

        return mMap.size() == 0;
    }

    /**
     * Get direct item count of the root object.
     *
     * @return Count of root object items.
     */
    public int getItemCountOfRoot() {

        return mMap.size();
    }

    /**
     * Check if item exists in Recipe.
     *
     * @param name Item name.
     * @return True if Recipe contains the item.
     */
    public boolean containsItem(String name) {

        boolean result = false;

        try {
            Map map = PathHelper.getMapByPath(mMap, name);
            result = map.containsKey(name);
        }
        catch (Exception e) {
            // If something goes wrong or item not found we do nothing here.
            // Result default value which is false will be returned.
        }

        return result;
    }

    /**
     * Get item value as boolean.
     * Call contains item before this to avoid exceptions.
     * If item is found but a different type, then a
     * <tt>ClassCastException</tt> will be thrown.
     *
     * @param name Item name.
     * @return Item value.
     */
    public boolean getItemAsBoolean(String name) {

        Map map = PathHelper.getMapByPath(mMap, name);
        return (boolean) map.get(name);
    }

    /**
     * Get item value as int.
     * Call contains item before this to avoid exceptions.
     * If item is found but a different type, then a
     * <tt>ClassCastException</tt> will be thrown.
     *
     * @param name Item name.
     * @return Item value.
     */
    public int getItemAsInt(String name) {

        Map map = PathHelper.getMapByPath(mMap, name);
        return (int) map.get(name);
    }

    /**
     * Get item value as string.
     * Call contains item before this to avoid exceptions.
     * If item is found but a different type, then a
     * <tt>ClassCastException</tt> will be thrown.
     *
     * @param name Item name.
     * @return Item value.
     */
    public String getItemAsString(String name) {

        Map map = PathHelper.getMapByPath(mMap, name);
        return (String) map.get(name);
    }

    /**
     * Get item value as string list.
     * Call contains item before this to avoid exceptions.
     * If item is found but a different type, then a
     * <tt>ClassCastException</tt> will be thrown.
     *
     * @param name Item name.
     * @return Item value as string list.
     */
    public List<String> getItemAsStringList(String name) {

        Map map = PathHelper.getMapByPath(mMap, name);
        return (List<String>) map.get(name);
    }

    /**
     * Get item value.
     * <p>
     * Call contains item before this to avoid exceptions.
     * If item is found but a different type, then a
     * <tt>ClassCastException</tt> will be thrown.
     *
     * @param name Item name or path.
     * @param <T>  Return value type.
     * @return Item value.
     */
    public <T> T getItem(String name) {

        Map map = PathHelper.getMapByPath(mMap, name);
        name = PathHelper.getKeyFromPath(name);
        return (T) map.get(name);
    }

    /**
     * Create new instance of Recipe class.
     *
     * @param jsonRecipeString Json string of Recipe.
     * @return Recipe object.
     */
    public static Recipe newInstance(String jsonRecipeString) {

        Recipe recipe = new Recipe();
        try {
            recipe.setMap(JsonHelper.stringToMap(JsonHelper.escapeComments(jsonRecipeString)));
        }
        catch (Exception e) {
            Log.e(TAG, "Recipe parsing failed!!!", e);
            throw new RuntimeException("Recipe is invalid!", e);
        }
        return recipe;
    }

    /**
     * Reads the file and creates a new recipe based on the file's content.
     *
     * @param context  The application context.
     * @param fileName The recipe file.
     * @return The recipe instance.
     */
    public static Recipe newInstance(Context context, String fileName) {

        Recipe recipe = new Recipe();
        try {
            String jsonRecipeString = FileHelper.readFile(context,
                                                          fileName);
            recipe.setMap(JsonHelper.stringToMap(JsonHelper.escapeComments(jsonRecipeString)));
        }
        catch (Exception e) {
            Log.e(TAG, "Recipe parsing failed!!!", e);
            throw new RuntimeException("Recipe is invalid!", e);
        }
        return recipe;
    }
}
