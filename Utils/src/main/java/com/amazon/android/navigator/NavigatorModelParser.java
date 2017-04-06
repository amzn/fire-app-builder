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
package com.amazon.android.navigator;

import com.amazon.android.recipe.Recipe;
import com.amazon.android.utils.FileHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import android.content.Context;
import android.util.Log;

/**
 * Parses the a file into a {@link NavigatorModel} by use of an {@link ObjectMapper}. Preloads the
 * recipes into the model to be used later.
 */
public class NavigatorModelParser {

    private static final String TAG = NavigatorModelParser.class.getSimpleName();

    /**
     * Parses the Navigator JSON file into a {@link NavigatorModel} object. The JSON file is
     * defined
     * by the {@link Navigator#NAVIGATOR_FILE} string.
     *
     * @param context The context.
     * @return A NavigatorModel object.
     */
    public static NavigatorModel parse(Context context, String navigatorFile) {

        NavigatorModel navigatorModel = null;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String navigatorFileString = FileHelper.readFile(context, navigatorFile);
            navigatorModel = objectMapper.readValue(navigatorFileString,
                                                    NavigatorModel.class);

            Log.v(TAG, "Navigator Model: " + navigatorModel.toString());

            // Preload recipes
            for (NavigatorModel.GlobalRecipes globalRecipes : navigatorModel.getGlobalRecipes()) {

                // Load category recipes if there is no hard coded name defined.
                if (globalRecipes.getCategories() != null &&
                        globalRecipes.getCategories().name == null) {

                    globalRecipes.getCategories().dataLoaderRecipe =
                            Recipe.newInstance(context, globalRecipes.getCategories().dataLoader);

                    globalRecipes.getCategories().dynamicParserRecipe =
                            Recipe.newInstance(context,
                                               globalRecipes.getCategories().dynamicParser);
                }

                if (globalRecipes.getContents() != null) {
                    globalRecipes.getContents().dataLoaderRecipe =
                            Recipe.newInstance(context, globalRecipes.getContents().dataLoader);

                    globalRecipes.getContents().dynamicParserRecipe =
                            Recipe.newInstance(context, globalRecipes.getContents().dynamicParser);
                }
            }
            // Preload Recommendation recipes
            if (navigatorModel.getRecommendationRecipes() != null) {
                for (NavigatorModel.RecommendationRecipes recommendationRecipes :
                        navigatorModel.getRecommendationRecipes()) {

                    if (recommendationRecipes.getContents() != null) {
                        recommendationRecipes.getContents().dataLoaderRecipe =
                                Recipe.newInstance(context,
                                                   recommendationRecipes.getContents().dataLoader);

                        recommendationRecipes.getContents().dynamicParserRecipe =
                                Recipe.newInstance(context,
                                                   recommendationRecipes.getContents()
                                                           .dynamicParser);

                    }
                }
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Navigator parsing failed!!! ", e);
        }
        return navigatorModel;
    }
}
