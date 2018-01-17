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

import com.amazon.android.recipe.Recipe;
import com.amazon.android.utils.PathHelper;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The purpose of this class is to provide a method of translating a {@link Map} to a generic
 * object type. Given a map or a list of maps, the methods will return the the object or a list of
 * objects. This class also provides three abstract methods for validating the translated object
 * and setting its member variables.
 */
public abstract class AModelTranslator<E> {

    private static final String TAG = AModelTranslator.class.getSimpleName();

    private static final String PATH_NAME_SEPARATOR = "@";

    private static final String MATCH_LIST = "matchList";

    /**
     * Given a {@link List} of {@link Map}s and a {@link Recipe}, translate each {@link Map} into
     * an {@link Object} and return it as a {@link List}.
     *
     * @param mapList Contains a list of maps that have all the values necessary for the objects.
     * @param recipe  The recipe contains the match list used to map the values in the map to the
     *                member variables of the object to be created.
     * @return The {@link List} of translated {@link Object}s.
     * @throws AModelTranslator.TranslationException if there was a problem
     *                                               during translation.
     * @throws IllegalArgumentException              if input arguments are null.
     */
    public List<E> mapListToModelList(List<Map<String, Object>> mapList, Recipe recipe) throws
            TranslationException {

        if (mapList == null || recipe == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        ArrayList<E> results = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            results.add(mapToModel(map, recipe));
        }
        return results;
    }

    /**
     * Given a {@link Map} and a {@link Recipe}, translate the {@link Map} into an {@link Object}.
     *
     * @param map    Contains a map that has all the values necessary for the object to be created.
     * @param recipe The recipe contains the match list used to map the values in the map to the
     *               member variables of the object to be created. The match list is a list of
     *               strings that provide the path to the given member variable of the model
     *               object.
     *               Example:
     *               ["path1/path2/key@memberVariable", "path3/path4/key2@otherMemberVariable"]
     * @return The translated {@link Object}
     * @throws AModelTranslator.TranslationException if there was a problem
     *                                               during translation.
     * @throws IllegalArgumentException              if input arguments are null.
     */
    public E mapToModel(Map<String, Object> map, Recipe recipe) throws TranslationException {

        if (map == null || recipe == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }

        E object = instantiateModel();
        List<String> matchList = recipe.getItemAsStringList(MATCH_LIST);

        for (String path : matchList) {

            // Extract the member variable field name from the path, which is located after
            // the path name separator token.
            String fieldName = path.substring(path.indexOf(PATH_NAME_SEPARATOR) + 1, path.length());
            // Extract the path which is located before the path name separator token.
            String fieldPath = path.substring(0, path.indexOf(PATH_NAME_SEPARATOR));
            // Follow the path to get the value
            Object value = PathHelper.getValueByPath(map, fieldPath);
            // Try setting the member variable with the value of fieldName to the value found
            // at the end of the path.
            if (!setMemberVariable(object, fieldName, value)) {
                Log.e(TAG, "Tried to set an invalid member variable during translation: " +
                        fieldName);
                throw new TranslationException("Tried to set an invalid member variable during " +
                                                       "translation: " + fieldName);
            }
        }

        // Fill KeyDataPath to mExtras HashMap which needs to be Map<String, Object>
        if (recipe.containsItem(Recipe.KEY_DATA_TYPE_TAG)) {
            String keyDataPath = recipe.getItemAsString(Recipe.KEY_DATA_TYPE_TAG);

            String fieldPath = keyDataPath.substring(0, keyDataPath.indexOf(PATH_NAME_SEPARATOR));

            Object value = PathHelper.getValueByPath(map, fieldPath);
            if (!setMemberVariable(object, Recipe.KEY_DATA_TYPE_TAG, value)) {
                Log.e(TAG, "KeyDataPath value was not parsed properly, check recipe.");
                throw new TranslationException("Tried to set an invalid member variable during " +
                                                       "translation: " + Recipe.KEY_DATA_TYPE_TAG);
            }
        }
        // Check if the recipe states that this content is live and add to object if so.
        // @TODO: Expand configuration handling with DEVTECH-2618.
        if (recipe.containsItem(Recipe.LIVE_FEED_TAG)) {
            setMemberVariable(object, Recipe.LIVE_FEED_TAG,
                              recipe.getItemAsBoolean(Recipe.LIVE_FEED_TAG));
        }

        // Check if the recipe states that this content is free and add to object if so.
        if (recipe.containsItem(Recipe.CONTENT_TYPE_TAG)) {

            String contentType = recipe.getItemAsString(Recipe.CONTENT_TYPE_TAG);
            String fieldPath = contentType.substring(0, contentType.indexOf(PATH_NAME_SEPARATOR));
            Object value = PathHelper.getValueByPath(map, fieldPath);
            if (value != null && !setMemberVariable(object, Recipe.CONTENT_TYPE_TAG, value)) {
                Log.e(TAG, "contentType value was not parsed properly, check recipe.");
                throw new TranslationException("Tried to set an invalid member variable during " +
                                                       "translation: " + Recipe.CONTENT_TYPE_TAG);
            }
        }

        // Check that the model was properly translated.
        if (!validateModel(object)) {
            Log.e(TAG, "Model object not valid: " + object.toString());
            return null;
        }

        return object;
    }

    /**
     * Return an instantiation of the model, the type of object that will be translated.
     *
     * @return The model.
     */
    public abstract E instantiateModel();

    /**
     * Explicitly sets a member variable named field to the given value.
     *
     * @param model The {@link Object} to set the field on.
     * @param field The {@link String} describing what member variable to set.
     * @param value The {@link Object} value to set the member variable.
     * @return True if the field was valid and the value was set; false otherwise.
     */
    public abstract boolean setMemberVariable(E model, String field, Object value);

    /**
     * Valid if all the necessary member variables are set for the object of the implementing
     * class.
     *
     * @param model The model to validate.
     * @return True if all validations are met; false otherwise.
     */
    public abstract boolean validateModel(E model);

    /**
     * Get the name of the translator.
     *
     * @return The name.
     */
    public abstract String getName();

    /**
     * An exception class for translation errors.
     */
    public static class TranslationException extends Exception {

        /**
         * Constructor for the exception.
         *
         * @param message The custom message to display.
         */
        public TranslationException(String message) {

            super(message);
        }
    }
}
