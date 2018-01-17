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
package com.amazon.android.contentbrowser;

import com.amazon.android.model.content.Content;
import com.amazon.android.model.content.ContentContainer;
import com.amazon.android.model.translators.ContentContainerTranslator;
import com.amazon.android.model.translators.ContentTranslator;
import com.amazon.android.navigator.Navigator;
import com.amazon.android.navigator.NavigatorModel;
import com.amazon.android.navigator.NavigatorModelParser;
import com.amazon.android.recipe.Recipe;
import com.amazon.dataloader.dataloadmanager.DataLoadManager;
import com.amazon.dynamicparser.DynamicParser;
import com.amazon.utils.model.Data;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;

/**
 * Class that initializes the content for the app. This includes running the recipes that download
 * the data and parses it into content model objects.
 */
public class ContentLoader {

    /**
     * Debug tag.
     */
    private final String TAG = ContentLoader.class.getSimpleName();

    /**
     * Debug recipe chain flag.
     */
    private static final boolean DEBUG_RECIPE_CHAIN = false;

    /**
     * Cause a feed error flag for debugging.
     */
    private static final boolean CAUSE_A_FEED_ERROR_FOR_DEBUGGING = false;

    /**
     * Singleton instance of ContentLoader.
     */
    private static ContentLoader sInstance;

    /**
     * The NavigatorModel that will contain the recipes.
     */
    private NavigatorModel mNavigatorModel;

    /**
     * The DataloadManager instance.
     */
    private DataLoadManager mDataLoadManager;

    /**
     * The DynamicParser instance.
     */
    private DynamicParser mDynamicParser;

    /**
     * Flag for if content reload is required.
     */
    private boolean mContentReloadRequired = false;

    /**
     * Flag for if the content is loaded.
     */
    private boolean mContentLoaded = false;

    /**
     * Root content container reference.
     */
    private ContentContainer mRootContentContainer = new ContentContainer("Root");

    /**
     * Constructor. Initializes the {@link NavigatorModel}, {@link DataLoadManager}, and
     * {@link DynamicParser} that is required to load data.
     *
     * @param context The application context.
     */
    private ContentLoader(Context context) {

        mNavigatorModel = NavigatorModelParser.parse(context, Navigator.NAVIGATOR_FILE);

        try {
            mDataLoadManager = DataLoadManager.getInstance(context);
            mDynamicParser = new DynamicParser();

            // Register content translator in case parser recipes use translation.
            ContentTranslator contentTranslator = new ContentTranslator();
            mDynamicParser.addTranslatorImpl(contentTranslator.getName(), contentTranslator);

            // Register content container translator in case parser recipes use translation.
            ContentContainerTranslator containerTranslator = new ContentContainerTranslator();
            mDynamicParser.addTranslatorImpl(containerTranslator.getName(),
                                             containerTranslator);

            mDataLoadManager.registerUpdateListener(new DataLoadManager.IDataUpdateListener() {
                @Override
                public void onSuccess(Data data) {

                    if (data != null) {
                        mContentReloadRequired = true;
                    }
                    else {
                        Log.i(TAG, "Data reload not required by data updater");
                    }
                }

                @Override
                public void onFailure(Throwable throwable) {

                    Log.e(TAG, "registerUpdateListener onFailure!!!", throwable);
                }
            });
        }
        catch (Exception e) {
            Log.e(TAG, "DataLoadManager init failed.", e);
        }
    }

    /**
     * Get the singleton instance.
     *
     * @param context The context.
     * @return The content loader instance.
     */
    public static ContentLoader getInstance(Context context) {

        if (sInstance == null) {
            synchronized (ContentLoader.class) {
                if (sInstance == null) {
                    sInstance = new ContentLoader(context);
                }
            }
        }
        return sInstance;
    }

    /**
     * Get categories observable.
     *
     * @param root                             Content container.
     * @param dataLoaderRecipeForCategories    Data loader recipe for getting categories.
     * @param dynamicParserRecipeForCategories Dynamic parser recipe for getting categories.
     * @return RX Observable.
     */
    private Observable<Object> getCategoriesObservable(ContentContainer root,
                                                       Recipe dataLoaderRecipeForCategories,
                                                       Recipe dynamicParserRecipeForCategories) {

        return mDataLoadManager.cookRecipeObservable(
                dataLoaderRecipeForCategories,
                null,
                null,
                null).map(
                feedDataForCategories -> {
                    if (DEBUG_RECIPE_CHAIN) {
                        Log.d(TAG, "Feed download complete");
                    }

                    if (CAUSE_A_FEED_ERROR_FOR_DEBUGGING) {
                        return Observable.error(new Exception());
                    }
                    return feedDataForCategories;
                }).concatMap(
                feedDataForCategories -> mDynamicParser.cookRecipeObservable
                        (dynamicParserRecipeForCategories,
                         feedDataForCategories,
                         null,
                         null)).map(
                contentContainerAsObject -> {
                    ContentContainer contentContainer = (ContentContainer) contentContainerAsObject;

                    ContentContainer alreadyAvailableContentContainer =
                            root.findContentContainerByName(contentContainer.getName());

                    if (alreadyAvailableContentContainer == null) {
                        root.addContentContainer(contentContainer);
                        alreadyAvailableContentContainer = contentContainer;
                    }

                    if (DEBUG_RECIPE_CHAIN) {
                        Log.d(TAG, "Dynamic parser got an container");
                    }
                    return alreadyAvailableContentContainer;
                });
    }

    /**
     * Get contents observable.
     *
     * @param observable                     Rx Observable chain to continue on.
     * @param dataLoaderRecipeForContents    Data loader recipe for getting contents.
     * @param dynamicParserRecipeForContents Dynamic parser  recipe for getting contents.
     * @return RX Observable.
     */
    private Observable<Object> getContentsObservable(Observable<Object> observable,
                                                     Recipe dataLoaderRecipeForContents,
                                                     Recipe dynamicParserRecipeForContents) {

        Map<String, Content> parsedContent = new HashMap();
        return observable.concatMap(contentContainerAsObject -> {
            ContentContainer contentContainer = (ContentContainer) contentContainerAsObject;
            if (DEBUG_RECIPE_CHAIN) {
                Log.d(TAG, "ContentContainer:" + contentContainer.getName());
            }
            return mDataLoadManager.cookRecipeObservable(
                    dataLoaderRecipeForContents,
                    null,
                    null,
                    null).map(
                    feedDataForContent -> {
                        if (DEBUG_RECIPE_CHAIN) {
                            Log.d(TAG, "Feed for container complete");
                        }
                        return Pair.create(contentContainerAsObject, feedDataForContent);
                    });
        }).concatMap(objectPair -> {
            ContentContainer contentContainer = (ContentContainer) objectPair.first;
            String feed = (String) objectPair.second;

            String[] params = new String[]{(String) contentContainer
                    .getExtraStringValue(Recipe.KEY_DATA_TYPE_TAG)
            };

            return mDynamicParser.cookRecipeObservable(
                    dynamicParserRecipeForContents,
                    feed,
                    null,
                    params).map(contentAsObject -> {
                if (DEBUG_RECIPE_CHAIN) {
                    Log.d(TAG, "Parser got an content");
                }
                Content content = (Content) contentAsObject;
                if (content != null) {
                    //check if this content has already been parsed for some other container
                    content = checkForParsedContent(parsedContent, content);
                    //Add information of free content available with container
                    if (contentContainer.getExtraStringValue(Recipe.CONTENT_TYPE_TAG) != null) {
                        content.setExtraValue(Recipe.CONTENT_TYPE_TAG, contentContainer
                                .getExtraStringValue(Recipe.CONTENT_TYPE_TAG));
                    }
                    contentContainer.addContent(content);
                }
                return Pair.create(contentContainer, contentAsObject);
            });
        });
    }

    /**
     * Get content chain observable.
     *
     * @param hardCodedCategoryName            Hard coded category name.
     * @param dataLoaderRecipeForCategories    Data loader recipe for getting categories.
     * @param dataLoaderRecipeForContents      Data loader recipe for getting contents.
     * @param dynamicParserRecipeForCategories Dynamic parser recipe for getting categories.
     * @param dynamicParserRecipeForContents   Dynamic parser  recipe for getting contents.
     * @param root                             Content container.
     * @return RX Observable.
     */
    private Observable<Object> getContentChainObservable(String hardCodedCategoryName,
                                                         Recipe dataLoaderRecipeForCategories,
                                                         Recipe dataLoaderRecipeForContents,
                                                         Recipe dynamicParserRecipeForCategories,
                                                         Recipe dynamicParserRecipeForContents,
                                                         ContentContainer root) {

        Observable<Object> observable;

        if (hardCodedCategoryName == null) {
            observable = getCategoriesObservable(root, dataLoaderRecipeForCategories,
                                                 dynamicParserRecipeForCategories);
        }
        else {
            observable = Observable.just(hardCodedCategoryName)
                                   .map(s -> {
                                       ContentContainer contentContainer =
                                               new ContentContainer(hardCodedCategoryName);
                                       root.addContentContainer(contentContainer);
                                       return contentContainer;
                                   });
        }

        return getContentsObservable(observable, dataLoaderRecipeForContents,
                                     dynamicParserRecipeForContents);
    }

    /**
     * Run global recipes at index.
     *
     * @param index Index.
     * @param root  Content container.
     * @return RX Observable.
     */
    public Observable<Object> runGlobalRecipeAtIndex(int index, ContentContainer root) {


        NavigatorModel.GlobalRecipes recipe = mNavigatorModel.getGlobalRecipes().get(index);

        Recipe dataLoaderRecipeForCategories = recipe.getCategories().dataLoaderRecipe;
        Recipe dataLoaderRecipeForContents = recipe.getContents().dataLoaderRecipe;

        Recipe dynamicParserRecipeForCategories = recipe.getCategories().dynamicParserRecipe;
        Recipe dynamicParserRecipeForContents = recipe.getContents().dynamicParserRecipe;

        // Add any extra configurations that the parser recipe needs from the navigator recipe.
        if (recipe.getRecipeConfig() != null) {
            // Add if the recipe is for live feed data.
            dynamicParserRecipeForContents.getMap().put(Recipe.LIVE_FEED_TAG,
                                                        recipe.getRecipeConfig().liveContent);
        }

        String hardCodedCategoryName = recipe.getCategories().name;

        return getContentChainObservable(hardCodedCategoryName,
                                         dataLoaderRecipeForCategories,
                                         dataLoaderRecipeForContents,
                                         dynamicParserRecipeForCategories,
                                         dynamicParserRecipeForContents,
                                         root);
    }

    /**
     * Run recommendation recipe at index.
     *
     * @param index Index.
     * @param root  Content container.
     * @return RX Observable.
     */
    public Observable<Object> runRecommendationRecipeAtIndex(int index, ArrayList<String> root) {


        NavigatorModel.RecommendationRecipes recipe = mNavigatorModel.getRecommendationRecipes()
                                                                     .get(index);

        Recipe dataLoaderRecipeForContents = recipe.getContents().dataLoaderRecipe;
        Recipe dynamicParserRecipeForContents = recipe.getContents().dynamicParserRecipe;


        return getRecommendationsChainObservable(dataLoaderRecipeForContents,
                                                 dynamicParserRecipeForContents,
                                                 root);
    }

    /**
     * Get recommendations chain observable.
     *
     * @param dataLoaderRecipe    Data loader recipe for getting recommendation content ids.
     * @param dynamicParserRecipe Parser recipe for translating the content ids to strings.
     * @param root                Content container.
     * @return RX Observable.
     */
    public Observable<Object> getRecommendationsChainObservable(Recipe dataLoaderRecipe,
                                                                Recipe dynamicParserRecipe,
                                                                ArrayList<String> root) {

        Observable<Object> observable;

        observable = getRecommendationsObservable(root, dataLoaderRecipe, dynamicParserRecipe);

        return observable;
    }

    /**
     * @param root                Content container.
     * @param dataLoaderRecipe    Data loader recipe for getting recommendation content ids.
     * @param dynamicParserRecipe Parser recipe for translating the content ids to strings.
     * @return RX Observable.
     */
    public Observable<Object> getRecommendationsObservable(ArrayList<String> root,
                                                           Recipe dataLoaderRecipe,
                                                           Recipe dynamicParserRecipe) {

        return mDataLoadManager.cookRecipeObservable(dataLoaderRecipe, null, null, null)
                               .map(feedDataForRecommendations -> {
                                   if (DEBUG_RECIPE_CHAIN) {
                                       Log.d(TAG, "Recommendation Feed download complete");
                                   }
                                   return feedDataForRecommendations;
                               }).concatMap(feedDataForRecommendations -> mDynamicParser
                        .cookRecipeObservable(dynamicParserRecipe,
                                              feedDataForRecommendations,
                                              null, null)).map(contentIdAsObject -> {
                    String contentId = (String) contentIdAsObject;
                    if (!root.contains(contentId)) {
                        root.add(contentId);
                    }
                    if (DEBUG_RECIPE_CHAIN) {
                        Log.d(TAG, "Dynamic parser got a content id: " + contentId);
                    }
                    return contentId;
                });
    }

    /**
     * Check if this content has already been parsed for some other container.
     *
     * @param parsedContent Map of previously parsed content objects.
     * @param content       Content object which need to be checked.
     * @return previously created content object or current object.
     */
    private Content checkForParsedContent(Map<String, Content> parsedContent, Content content) {

        if (parsedContent.containsKey(content.getId())) {
            return parsedContent.get(content.getId());
        }
        //Add current content in parsedContent map
        parsedContent.put(content.getId(), content);
        return content;
    }

    /**
     * Is content reloading required?
     *
     * @return True if content should be reloaded; false otherwise.
     */
    public boolean isContentReloadRequired() {

        return mContentReloadRequired;
    }

    /**
     * Set if content reload is required.
     *
     * @param contentReloadRequired True if content should be reloaded; false otherwise.
     */
    public void setContentReloadRequired(boolean contentReloadRequired) {

        mContentReloadRequired = contentReloadRequired;
    }

    /**
     * Is the content loaded?
     *
     * @return True if the content is loaded; false otherwise.
     */
    public boolean isContentLoaded() {

        return mContentLoaded;
    }

    /**
     * Set if the content has been loaded.
     *
     * @param contentLoaded True if the content is loaded; false otherwise.
     */
    public void setContentLoaded(boolean contentLoaded) {

        mContentLoaded = contentLoaded;
    }

    /**
     * Get the Navigator Model.
     *
     * @return The Navigator Model.
     */
    public NavigatorModel getNavigatorModel() {

        return mNavigatorModel;
    }

    /**
     * Get the root content container.
     *
     * @return The root.
     */
    public ContentContainer getRootContentContainer() {

        return mRootContentContainer;
    }

    /**
     * Set the root content container.
     *
     * @param rootContentContainer The root.
     */
    public void setRootContentContainer(ContentContainer rootContentContainer) {

        mRootContentContainer = rootContentContainer;
    }

    /**
     * Get the number of global recommendations that should be sent, as specified in the
     * navigator configuration file.
     *
     * @return The number of global recommendations to send.
     */
    public int getNumberOfGlobalRecommendations() {

        return mNavigatorModel.getConfig().numberOfGlobalRecommendations;
    }

    /**
     * Get the number of related recommendations that should be sent, as specified in the
     * navigator configuration file.
     *
     * @return The number of related recommendations to send.
     */
    public int getNumberOfRelatedRecommendations() {

        return mNavigatorModel.getConfig().numberOfRelatedRecommendations;
    }
}
