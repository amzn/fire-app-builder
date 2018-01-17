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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

// Notes:
// Json keys MUST start with lower case. Setter and getter is a must, enum is not working.
// Inner class needs to be static

/**
 * Model class which defines how navigator json represented in java world.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NavigatorModel {

    /**
     * Config class.
     */
    private Config config;

    /**
     * Branding class.
     */
    private Branding branding;

    /**
     * Global recipe list.
     */
    private List<GlobalRecipes> globalRecipes;

    /**
     * Recommendation recipe list.
     */
    private List<RecommendationRecipes> recommendationRecipes;

    /**
     * Graph map.
     */
    private Map<String, UINode> graph;

    /**
     * Constructor.
     */
    public NavigatorModel() {

    }

    /**
     * Config class
     */
    public class Config {

        /**
         * Show recommended flag.
         */
        public boolean showRelatedContent;

        /**
         * A flag for using items from the same category as recommended content if similar tags has
         * no results.
         */
        public boolean useCategoryAsDefaultRelatedContent;

        /**
         * Search algorithm name.
         */
        public String searchAlgo;

        /**
         * Enable CEA-608 closed caption flag. If enabled, we prioritize CEA-608 captions.
         */
        public boolean enableCEA608 = false;

        /**
         * Enable the row that displays content to continue watching.
         */
        public boolean enableRecentRow = true;

        /**
         * The maximum number of items displayed in the continue watching row.
         */
        public int maxNumberOfRecentItems = 20;

        /**
         * Enable the watchlist row in the browse screen.
         */
        public boolean enableWatchlistRow = true;

        /**
         * The number of global recommendations that the app should send; assuming there are
         * global recommendation recipes available.
         */
        public int numberOfGlobalRecommendations = -1;

        /**
         * The number of related recommendations that the ap should send; assuming the content feed
         * recipe includes the recommendation item in the match list.
         */
        public int numberOfRelatedRecommendations = -1;
    }

    /**
     * Branding class.
     */
    public class Branding {

        /**
         * Global theme to be used.
         */
        public String globalTheme;

        /**
         * A light font used for body text.
         */
        public String lightFont;

        /**
         * A bold font used for titles.
         */
        public String boldFont;

        /**
         * A regular font used for buttons and subtitles.
         */
        public String regularFont;
    }

    /**
     * Global recipes class.
     */
    public static class GlobalRecipes {

        /**
         * Category recipes.
         */
        private Recipes categories;

        /**
         * Content recipes.
         */
        private Recipes contents;

        /**
         * Configs.
         */
        private RecipeConfig recipeConfig;

        /**
         * Constructor.
         */
        public GlobalRecipes() {

        }

        /**
         * Recipes class.
         */
        public static class Recipes {

            /**
             * Hard coded category name, only applies to categories recipes.
             */
            public String name;

            /**
             * Data loader recipe name.
             */
            public String dataLoader;

            /**
             * Data loader recipe instance.
             */
            public Recipe dataLoaderRecipe;

            /**
             * Dynamic parser recipe name.
             */
            public String dynamicParser;

            /**
             * Dynamic parser recipe instance.
             */
            public Recipe dynamicParserRecipe;

            /**
             * Constructor.
             */
            public Recipes() {

            }
        }

        /**
         * A class for any extra config data that is specific to a category/contents recipe pair.
         */
        public static class RecipeConfig {

            /**
             * A setting of if the content is live or not.
             */
            public boolean liveContent;

            /**
             * Constructor.
             */
            public RecipeConfig() {

            }
        }

        /**
         * Get category recipes.
         *
         * @return Category recipes.
         */
        public Recipes getCategories() {

            return categories;
        }

        /**
         * Set category recipes.
         *
         * @param categories Category recipes.
         */
        public void setCategories(Recipes categories) {

            this.categories = categories;
        }

        /**
         * Get content recipes.
         *
         * @return Recipes.
         */
        public Recipes getContents() {

            return contents;
        }

        /**
         * Set content recipes.
         *
         * @param contents Content recipes.
         */
        public void setContents(Recipes contents) {

            this.contents = contents;
        }

        /**
         * Gets the recipe configuration.
         *
         * @return The recipe configuration.
         */
        public RecipeConfig getRecipeConfig() {

            return recipeConfig;
        }

        /**
         * Sets the recipe configuration.
         *
         * @param recipeConfig The recipe configuration to set.
         */
        public void setRecipeConfig(RecipeConfig recipeConfig) {

            this.recipeConfig = recipeConfig;
        }
    }

    /**
     * Recommendation recipes class.
     */
    public static class RecommendationRecipes {

        /**
         * The recipe for the contents.
         */
        public GlobalRecipes.Recipes contents;

        /**
         * Constructor.
         */
        public RecommendationRecipes() {

        }

        /**
         * Get content recipes.
         *
         * @return Recipes.
         */
        public GlobalRecipes.Recipes getContents() {

            return contents;
        }

        /**
         * Set content recipes.
         *
         * @param contents Content recipes.
         */
        public void setContents(GlobalRecipes.Recipes contents) {

            this.contents = contents;
        }
    }

    /**
     * Get config.
     *
     * @return Config.
     */
    public Config getConfig() {

        return config;
    }

    /**
     * Set config.
     *
     * @param config Config.
     */
    public void setConfig(Config config) {

        this.config = config;
    }

    /**
     * Get branding.
     *
     * @return Branding.
     */
    public Branding getBranding() {

        return branding;
    }

    /**
     * Set branding.
     *
     * @param branding Branding.
     */
    public void setBranding(Branding branding) {

        this.branding = branding;
    }

    /**
     * Get global recipe list.
     *
     * @return List of global recipes.
     */
    public List<GlobalRecipes> getGlobalRecipes() {

        return globalRecipes;
    }

    /**
     * Set global recipe list.
     *
     * @param globalRecipes Global recipe list.
     */
    public void setGlobalRecipes(List<GlobalRecipes> globalRecipes) {

        this.globalRecipes = globalRecipes;
    }

    /**
     * Get graph map.
     *
     * @return Graph map.
     */
    public Map<String, UINode> getGraph() {

        return graph;
    }

    /**
     * Set graph map.
     *
     * @param graph Graph map.
     */
    public void setGraph(Map<String, UINode> graph) {

        this.graph = graph;
    }

    /**
     * Get the recommendation recipes.
     *
     * @return The recommendation recipes.
     */
    public List<RecommendationRecipes> getRecommendationRecipes() {

        return recommendationRecipes;
    }

    /**
     * Set the recommendation recipes.
     *
     * @param recommendationRecipes The recommendation recipes.
     */
    public void setRecommendationRecipes(List<RecommendationRecipes> recommendationRecipes) {

        this.recommendationRecipes = recommendationRecipes;
    }
}
