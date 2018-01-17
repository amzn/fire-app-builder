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

import com.amazon.dynamicparser.impl.JsonParser;
import com.amazon.android.recipe.IRecipeCooker;
import com.amazon.android.recipe.IRecipeCookerCallbacks;
import com.amazon.android.recipe.Recipe;
import com.amazon.android.model.AModelTranslator;
import com.amazon.android.utils.PathHelper;
import com.amazon.dynamicparser.impl.XmlParser;
import com.amazon.utils.ListUtils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import rx.Observable;

/**
 * This class is responsible for reading {@link Recipe}s and parsing data according to the {@link
 * Recipe}'s instruction.
 */
@SuppressWarnings("unchecked")
public class DynamicParser implements IRecipeCooker {

    /**
     * Constant tag for matchList recipe field.
     */
    public static final String MATCH_LIST_TAG = "matchList";

    /**
     * Constant tag for translator recipe field.
     */
    public static final String TRANSLATOR_TAG = "translator";

    /**
     * Constant tag for query recipe field.
     */
    public static final String QUERY_TAG = "query";

    /**
     * Constant tag for cooker recipe field.
     */
    public static final String COOKER_TAG = "cooker";

    /**
     * Constant tag for model recipe field.
     */
    public static final String MODEL_TAG = "model";

    /**
     * Constant tag for model type recipe field.
     */
    public static final String MODEL_TYPE_TAG = "modelType";

    /**
     * Constant tag for key data path recipe field.
     */
    public static final String KEY_DATA_PATH_TAG = "keyDataType";

    /**
     * Constant tag for format recipe field.
     */
    public static final String FORMAT_TAG = "format";

    /**
     * Constant tag for query result type recipe field.
     */
    public static final String QUERY_RESULT_TAG = "queryResultType";

    /**
     * Constant tag for specifying in the match list that the value is the model object, for
     * example item/id@ModelValue would mean that this should translate into a String with the
     * value found by evaluating the path item/id.
     */
    public static final String MODEL_VALUE_TAG = "ModelValue";

    /**
     * Debug tag.
     */
    private static final String TAG = DynamicParser.class.getSimpleName();

    /**
     * Constant used to separate path and name in a match list.
     */
    private final String PATH_NAME_SEPARATOR = "@";

    /**
     * Constant used to dictate an Array class.
     */
    private final String ARRAY_CLASS_TOKEN = "[]";

    /**
     * Constant used to dictate a Map class.
     */
    private final String MAP_CLASS_TOKEN = "{}";

    /**
     * Map of IParser implementations. The key to retrieve the parser should be the parser's data
     * format.
     */
    private final Map<String, IParser> mParsers;

    /**
     * Map of AModelTranslator implementations.
     */
    private final Map<String, AModelTranslator> mTranslators;

    /**
     * List of ParseAsyncTasks to handle translating tasks.
     */
    private final List<AsyncTask> mAsyncTasks;

    /**
     * True if the parser should send all translated items at once. False if parser should send
     * items as soon as they finish translation. Default: false.
     */
    private boolean mBatchMode;

    /**
     * True if the parser should use multithreading during translation. False if the all items
     * should be translated on the main thread. Default: false.
     */
    private boolean mAsyncMode;

    /**
     * The executor to execute async tasks in parallel.
     */
    private static final ThreadPoolExecutor EXECUTOR = (ThreadPoolExecutor) AsyncTask
            .THREAD_POOL_EXECUTOR;

    /**
     * Constructs a dynamic parser. Adds all known parsers into a map for later use. By default the
     * parser does not operate in batch mode or async mode.
     */
    public DynamicParser() {

        mParsers = new HashMap<>();
        addParserImpl(JsonParser.FORMAT, new JsonParser());
        addParserImpl(XmlParser.FORMAT, new XmlParser());

        mTranslators = new HashMap<>();

        mAsyncTasks = Collections.synchronizedList(new ArrayList<>());

        setBatchMode(false);
        setAsyncMode(false);
    }

    /**
     * Adds an {@link AModelTranslator} implementation to the map of translators. This method will
     * not add a null implementation and will not override a translator if a translator with
     * {@code translatorName} already exists in the map. This method is not thread safe.
     *
     * @param translatorName The name of translator.
     * @param translator     The {@link AModelTranslator} implementation.
     * @return True if the translator was added, false otherwise.
     */
    public boolean addTranslatorImpl(String translatorName, AModelTranslator translator) {

        if (translatorName != null && !mTranslators.containsKey(translatorName)
                && translator != null) {
            mTranslators.put(translatorName, translator);
            return true;
        }
        return false;
    }

    /**
     * Adds a {@link IParser} implementation to the map of parsers. This method will not add a
     * null implementation and will not override a parser for a given format if the format of
     * {@code type} already exists in the map. This method is not thread safe.
     *
     * @param type       The type of the parser implementation.
     * @param parserImpl The {@link IParser} implementation.
     * @return True if the parser was added, false otherwise.
     */
    public boolean addParserImpl(String type, IParser parserImpl) {

        if (parserImpl != null && !mParsers.containsKey(type)) {
            mParsers.put(type, parserImpl);
            return true;
        }
        return false;
    }

    /**
     * Retrieves a {@link IParser} implementation of the given type from the map of parsers.
     *
     * @param type The type of the parser to retrieve.
     * @return The {@link IParser} implementation.
     * @throws com.amazon.dynamicparser.DynamicParser.ParserNotFoundException If the requested
     *                                                                        parser isn't found.
     */
    public IParser getParserImpl(String type) throws ParserNotFoundException {

        IParser parser = mParsers.get(type);
        if (parser == null) {
            throw new ParserNotFoundException("Dynamic parser has no parser implementation for " +
                                                      "requested type " + type);
        }
        return parser;
    }

    /**
     * Configures the dynamic parser's batch processing and multithreading settings.
     *
     * @param batch True if items should be sent only after all items have been processed;
     *              false if items should be sent individually after they have been processed.
     * @param async True if items should be processed on their own thread; false if they
     *              should be processed on the main thread.
     */
    public void configureSettings(boolean batch, boolean async) {

        setBatchMode(batch);
        setAsyncMode(async);
    }

    /**
     * Tests whether the dynamic parser is set to translate all models at once as a batch, or one
     * at a time.
     *
     * @return True if all models are translated and then returned as one result; false
     * if the translated models are returned one at a time, once translation completes.
     */
    public boolean isBatchMode() {

        return mBatchMode;
    }

    /**
     * Sets whether the dynamic parser is set to translate all models at once as a batch, or
     * one at a time.
     *
     * @param batchMode True if all models should be translated and then returned as one result;
     *                  false if the translated models should be returned one at a time, once
     *                  translation completes.
     */
    public void setBatchMode(boolean batchMode) {

        mBatchMode = batchMode;
    }

    /**
     * Tests whether the dynamic parser is set to spawn async tasks during translation.
     *
     * @return True if multiple threads should be used; false if only the main thread
     * is necessary.
     */
    public boolean isAsyncMode() {

        return mAsyncMode;
    }

    /**
     * Sets whether or not to spawn async tasks during translation.
     *
     * @param asyncMode True if multiple threads should be used; false if only the main thread
     *                  is necessary.
     */
    public void setAsyncMode(boolean asyncMode) {

        mAsyncMode = asyncMode;
    }

    /**
     * Cancels all translation tasks.
     */
    public void cancelTranslationTasks() {

        synchronized (mAsyncTasks) {
            for (AsyncTask task : mAsyncTasks) {
                if (!task.getStatus().equals(AsyncTask.Status.FINISHED)) {
                    task.cancel(true);
                }
            }
            mAsyncTasks.clear();
        }

    }

    /**
     * Returns true if there are tasks still in progress; false if there are no running Async
     * tasks. This is determined by looking at the size of the task list.
     *
     * @return Returns true if there are tasks still in progress; false if there are no running
     * Async tasks.
     */
    public boolean areTasksInProgress() {

        return !mAsyncTasks.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Object> cookRecipeObservable(Recipe recipe, Object input, Bundle bundle,
                                                   String[] params) {

        Observable<Object> dynamicParserObservable = Observable.create(subscriber -> {

            try {
                // Make sure recipe and input is valid.
                checkCookRecipeInput(recipe, input);

                // Parse input into a list of maps for translation
                List<Map<String, Object>> resultList = parseInput(recipe, input.toString(), params);

                // Translate each map in the result list into the model object defined in the
                // recipe. Return each model once it completes translation via subscriber.
                translateMapsToObjects(false, recipe, resultList, new IRecipeCookerCallbacks() {

                    @Override
                    public void onPreRecipeCook(Recipe recipe, Object output, Bundle bundle) {

                    }

                    @Override
                    public void onRecipeCooked(Recipe recipe, Object output, Bundle bundle, boolean
                            done) {

                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(output);
                            if (done) {
                                subscriber.onCompleted();
                            }
                        }
                    }

                    @Override
                    public void onPostRecipeCooked(Recipe recipe, Object output, Bundle bundle) {

                    }

                    @Override
                    public void onRecipeError(Recipe recipe, Exception e, String msg) {

                        if (e instanceof ValueNotFoundException) {
                            Log.e(TAG, "Error during parsing, skipping an item:", e);
                        }
                        else {
                            subscriber.onError(e);
                        }
                    }
                }, bundle);

            }
            catch (Exception e) {
                subscriber.onError(e);
            }

        });

        return dynamicParserObservable;
    }

    /**
     * This method processes the instructions in the given {@link Recipe} that has been formatted
     * specifically for parsing data.
     *
     * @param recipe      List of instructions to achieve certain cooking. The parsing
     *                    recipe should include the following:
     *
     *                    <ul><li>recipeCooker : Name of parser handling the overall parsing
     *                    process; in this case DynamicParser.</li>
     *                    <li>format : The data format to be parsed.</li>
     *                    <li>model : The full class name of the objects to be parsed.</li>
     *                    <li>modelType : The type of data being parsed: an {@link Object}, a
     *                    {@link Map}, or a {@link List}.</li>
     *                    <li>query : What to look for in the data feed; may contain parameters.
     *                    See {@link PathHelper#injectParameters(String,
     *                    String[])} for more info.</li>
     *                    <li>queryResultType : {optional} The expected type of the query result.
     *                    If this field is left out, the default value is a {@link List} of
     *                    {@link Map}s.</li>
     *                    <li>translator : {optional} How the data should be converted into the
     *                    model class. If translation should be used, the translation
     *                    class name must be provided and the class must implement the
     *                    {@link AModelTranslator} interface. The translator must also be
     *                    registered to the dynamic parser using the {@link
     *                    DynamicParser#addTranslatorImpl(String, AModelTranslator)}
     *                    method. If this field is not included, objects will be translated
     *                    using reflection by default.</li>
     *                    <li>matchList : A list of strings that provide the path to the
     *                    given property of the model object.
     *                    Example: ["book/author/name@name"] If the models are expected to be
     *                    created via reflection, all property names in the match list must match
     *                    the model property names exactly.</li>
     *                    <li>keyDataPath : {optional} A string of the same format of the strings
     *                    of matchList, that points to the location of the main content.</li></ul>
     * @param input       The data to be parsed. A {@link String} is expected.
     * @param rcCallbacks Recipe cooker callbacks.
     * @param bundle      Extra data.
     * @param params      If the query from recipe requires parameters, pass them here.
     * @return True if data was parsed without error, false otherwise.
     */
    @Override
    public boolean cookRecipe(Recipe recipe, Object input, IRecipeCookerCallbacks
            rcCallbacks, Bundle bundle, String[] params) {

        try {
            // Check the recipe and input argument.
            checkCookRecipeInput(recipe, input);

            // Parse input into a list of maps for translation
            List<Map<String, Object>> resultList = parseInput(recipe, input.toString(), params);

            // If async mode, create an async task to do the translation.
            if (isAsyncMode()) {
                TranslateAsyncTask translateAsyncTask =
                        new TranslateAsyncTask(isBatchMode(),
                                               recipe,
                                               resultList,
                                               rcCallbacks,
                                               bundle);
                mAsyncTasks.add(translateAsyncTask);
                translateAsyncTask.executeOnExecutor(EXECUTOR);
            }
            // Otherwise, do translation on the main thread.
            else {
                translateMapsToObjects(isBatchMode(), recipe, resultList, rcCallbacks,
                                       bundle);
            }
        }
        catch (Exception e) {
            rcCallbacks.onRecipeError(recipe, e, e.getMessage());
            return false;
        }

        return true;

    }

    /**
     * Parses the input string to a list of maps to be used during model translation.
     *
     * @param recipe The parser recipe.
     * @param input  The data to be parsed.
     * @param params List of parameters that the recipe requires.
     * @return A {@link List} of {@link Map}s.
     */
    private List<Map<String, Object>> parseInput(Recipe recipe, String input, String[] params)
            throws ParserNotFoundException, IParser.InvalidDataException,
            IParser.InvalidQueryException, PathHelper.MalformedInjectionStringException {

        // Get the query from recipe.
        String query = prepareQuery(recipe, params);
        if (query == null) {
            return null;
        }

        // Get the parser type from recipe.
        String parserType = recipe.getItemAsString(FORMAT_TAG);

        // Get the query result type from the recipe or use default List<Map> type.
        // If recipe does not contain queryResultType, sending null is okay.
        Class<?> queryResultType = getQueryResultType(recipe.getItemAsString(QUERY_RESULT_TAG));


        // Parse the data and cast it to the expected result type.
        Object parseResult = queryResultType.cast(getParserImpl(parserType).parseWithQuery
                (input, query));

        // We need the final result list to be a List of Maps to use during model creation.
        return convertQueryResultToListMap(parseResult);

    }

    /**
     * Checks if the query requires any parameters. If it requires parameters, it injects them into
     * the query string and returns the query.
     *
     * @param recipe The parser recipe.
     * @param params List of parameters that the recipe requires.
     * @return The query string with the parameters in place.
     */
    private String prepareQuery(Recipe recipe,
                                String[] params) throws
            PathHelper.MalformedInjectionStringException {

        String query = recipe.getItemAsString(QUERY_TAG);

        // If there are any params, inject them into the query string.
        if (PathHelper.containsParameterMatchingRegex(query)) {

            query = PathHelper.injectParameters(query, params);

        }
        return query;
    }

    /**
     * Checks that the arguments aren't null and that the recipe is a valid parser recipe.
     *
     * @param recipe The parser recipe.
     * @param input  The data to be parsed.
     */
    private void checkCookRecipeInput(Recipe recipe, Object input) throws
            InvalidParserRecipeException, IllegalArgumentException {

        // Make sure the recipe is valid.
        validateRecipe(recipe);

        // Check input
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }

        // Check the recipe cooker type. If the type isn't DynamicParser, log and throw an error.
        if (!recipe.getItemAsString(COOKER_TAG).equals(DynamicParser.class.getSimpleName())) {
            throw new InvalidParserRecipeException("cookerType does not match DynamicParser, but " +
                                                           "is " +
                                                           recipe.getItemAsString(COOKER_TAG));
        }
    }

    /**
     * Converts an Object to a {@link List} of {@link Map}s to be used for model translation. If
     * the Object is already a List<Map> then just cast the Object. If the Object is a List of
     * something other than a map, create a map for each item in the list and add that map to the
     * result list. The key used is composed of the item's class type and the word "Key".
     *
     * Example:
     * A parseResult of ["str1", "str2", "str3"] will be turned into the following list of maps:
     * [ {"StringKey":"str1"}, {"StringKey":"str2"}, {"StringKey":"str3"} ]
     *
     * @param parseResult The Object to convert into a List<Map>
     * @return The new List<Map<String, Object>>
     */
    private List<Map<String, Object>> convertQueryResultToListMap(Object parseResult) {

        List<Map<String, Object>> resultList = new ArrayList<>();

        if (parseResult == null) {
            return resultList;
        }

        // Turn the parseResult into a List<Map> for model translation/creation
        if (!(parseResult instanceof List<?>)) {
            // If the parseResult is not a List, it must be a single Map and must be added to
            // the result list for model translation/creation as well.
            resultList.add((Map<String, Object>) parseResult);
            return resultList;
        }

        List parseResultList = (List) parseResult;

        if (parseResultList.size() <= 0) {
            return resultList;
        }

        // If it already is a List<Map> directly cast the parseResult
        if (parseResultList.get(0) instanceof Map) {
            resultList = (List<Map<String, Object>>) parseResult;
            return ListUtils.removeDuplicates(resultList);
        }

        // Create a key to index into the map with.
        // This key will be used in the match list.
        String key = parseResultList.get(0).getClass().getSimpleName() + "Key";

        // For each value, create a Map, add the (key, value) pair, and add the map
        // to the result list.
        for (Object value : parseResultList) {
            HashMap<String, Object> map = new HashMap<>();
            map.put(key, value);
            ListUtils.safeAdd(resultList, map);
        }

        return resultList;
    }

    /**
     * This is a private helper method that handles the translation of the map list to objects.
     *
     * @param batch                 Whether or not to return items all together as a list or as
     *                              single objects.
     * @param recipe                The parser recipe.
     * @param translationMapList    The list of maps containing the values that are needed by the
     *                              match list.
     * @param recipeCookerCallbacks Recipe cooking callbacks.
     * @param bundle                Extra data
     */
    private void translateMapsToObjects(boolean batch, Recipe recipe, List<Map<String, Object>>
            translationMapList, IRecipeCookerCallbacks recipeCookerCallbacks, Bundle bundle) {

        List<Object> translatedObjects = new ArrayList<>();

        if (translationMapList.size() == 0) {
            recipeCookerCallbacks.onRecipeCooked(recipe, null, bundle, true);
        }

        for (int index = 0; index < translationMapList.size(); index++) {

            // Is this the last map to translate?
            boolean done = index + 1 == translationMapList.size();

            Object model = translateMapToModel(recipe, recipeCookerCallbacks,
                                               translationMapList.get(index));
            if (model != null) {

                // If batch mode, add model to list
                if (batch) {
                    translatedObjects.add(model);
                }
                // Otherwise, return the translated model via the recipe callback but only if the
                // model isn't null.
                else {
                    recipeCookerCallbacks.onRecipeCooked(recipe, model, bundle, done);
                }
            }
            // Even if there was an error during translation, we need to state that the cooking
            // is completed.
            else if (done) {
                recipeCookerCallbacks.onRecipeCooked(recipe, null, bundle, true);
            }
        }

        // If batch mode, return all the translated objects via recipe callback at once
        if (batch) {
            recipeCookerCallbacks.onRecipeCooked(recipe, translatedObjects, bundle, true);
        }
    }

    /**
     * This is a private helper method to create a single model object. This method decides
     * whether to use a translator or reflection depending on the recipe.
     *
     * @param recipe    The recipe containing the match list to create the object with.
     * @param callbacks Recipe cooking callbacks.
     * @param map       The data map containing the values that are needed by the match list.
     * @return The newly created object.
     */
    private Object translateMapToModel(Recipe recipe, IRecipeCookerCallbacks callbacks,
                                       Map<String, Object> map) {

        // If the recipe specifies a translator, create the model using translation.
        if (recipe.containsItem(TRANSLATOR_TAG)) {

            // Get the translator.
            AModelTranslator translator = mTranslators.get(recipe.getItemAsString
                    (TRANSLATOR_TAG));

            // If the translator wasn't found, report the error.
            if (translator == null) {
                String message = "Translator named " + recipe.getItemAsString(TRANSLATOR_TAG)
                        + " not registered with dynamic parser.";
                callbacks.onRecipeError(recipe, new TranslatorNotFoundException(message), message);
                return null;
            }

            try {
                // Translate the map to the model.
                return translator.mapToModel(map, recipe);
            }
            catch (AModelTranslator.TranslationException e) {
                callbacks.onRecipeError(recipe, e, "Error translating objects.");
                return null;
            }
        }
        // If no translator was specified, use reflection to create the model.
        else {
            // Create a model object with reflection.
            return translateObjectWithReflection(recipe.getItemAsString(MODEL_TAG),
                                                 recipe,
                                                 map,
                                                 callbacks);
        }
    }

    /**
     * This is a private helper method to create an object from a data map using reflection.
     *
     * @param className The class name of the object to create.
     * @param recipe    The recipe containing the match list to create the object with.
     * @param map       The data map containing the values that are needed by the match list.
     * @return The newly created object.
     */
    private Object translateObjectWithReflection(String className, Recipe recipe, Map<String,
            Object> map, IRecipeCookerCallbacks callbacks) {

        Object instance;
        try {
            Class<?> clazz = Class.forName(className);
            instance = clazz.newInstance();

            // For each item in the match list set that property on the model object using the
            // data from the map.
            List<String> matchList = recipe.getItemAsStringList(MATCH_LIST_TAG);
            for (String match : matchList) {

                // The match specifies the value is the whole model object, such as a String.
                if (match.contains(DynamicParser.MODEL_VALUE_TAG)) {
                    // Create a new instance of the model with the value and return.
                    return createClassInstanceWithValue(map, clazz, match);
                }
                setClazzFieldByMatchingPathFromMap(map, match, clazz, instance, false);
            }
            // Fill KeyDataPath to mExtras HashMap which needs to be Map<String, Object>
            if (recipe.containsItem(Recipe.KEY_DATA_TYPE_TAG)) {

                String keyDataPath = recipe.getItemAsString(Recipe.KEY_DATA_TYPE_TAG);

                String fieldPath = keyDataPath.substring(0,
                                                         keyDataPath.indexOf(PATH_NAME_SEPARATOR));

                Object value = PathHelper.getValueByPath(map, fieldPath);

                if (value != null) {

                    addValueToExtrasWithReflection(Recipe.KEY_DATA_TYPE_TAG, value.toString(),
                                                   instance, clazz);

                }
            }
            // Fill ContentType to mExtras HashMap which needs to be Map<String, Object>
            if (recipe.containsItem(Recipe.CONTENT_TYPE_TAG)) {

                String keyDataPath = recipe.getItemAsString(Recipe.CONTENT_TYPE_TAG);
                String fieldPath = keyDataPath.substring(0,
                                                         keyDataPath.indexOf(PATH_NAME_SEPARATOR));
                Object value = PathHelper.getValueByPath(map, fieldPath);
                if (value != null) {

                    addValueToExtrasWithReflection(Recipe.CONTENT_TYPE_TAG, value.toString(),
                                                   instance, clazz);
                }
            }
            // Check if the recipe states that this content is live and add to object if so.
            // @TODO: Improve configuration handling with DEVTECH-2618.
            if (recipe.containsItem(Recipe.LIVE_FEED_TAG)) {

                addValueToExtrasWithReflection(Recipe.LIVE_FEED_TAG,
                                               recipe.getItemAsBoolean(Recipe.LIVE_FEED_TAG),
                                               instance, clazz);
            }
            return instance;
        }
        catch (InstantiationException | IllegalAccessException
                | NoSuchMethodException | InvocationTargetException e) {
            throwParserError(callbacks, recipe, e, "Error while creating object with reflection");
        }
        catch (ClassNotFoundException e) {
            throwParserError(callbacks, recipe, e, "Could not find expected model class" +
                    className);
        }
        catch (NoSuchFieldException e) {
            throwParserError(callbacks, recipe, e, "Could not find specified field while creating" +
                    "object with reflection");
        }
        catch (ValueNotFoundException e) {
            throwParserError(callbacks, recipe, e, "Could not find value by following path while " +
                    "creating object with reflection");
        }

        return null;
    }

    /**
     * Private helper method to throw parser error.
     *
     * @param callbacks    The callbacks to use to report the error.
     * @param recipe       The recipe that caused the error.
     * @param error        The error.
     * @param errorMessage A custom error message for the specific error that occurred.
     */
    private void throwParserError(IRecipeCookerCallbacks callbacks, Recipe recipe,
                                  Exception error, String errorMessage) {

        if (callbacks != null) {
            callbacks.onRecipeError(recipe, error, errorMessage);
        }
        else {
            throw new RuntimeException(error);
        }
    }

    /**
     * Creates an instance of the class passed in with the value that is found by following the
     * path in the match.
     *
     * @param map   The map to use to find the value of the match.
     * @param clazz The class to instantiate.
     * @param match The match containing the path to the value.
     * @return An instantiated Object of the value from evaluating the match.
     */
    private Object createClassInstanceWithValue(Map<String, Object> map, Class<?> clazz, String
            match) throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {

        String fieldPath = match.substring(0, match.indexOf(PATH_NAME_SEPARATOR));
        Object value = PathHelper.getValueByPath(map, fieldPath);
        return clazz.getConstructor(String.class).newInstance(value);
    }

    /**
     * Adds a name/value pair to the extras map of the object instance. This method expects the
     * object instance to have a field called "mExtras" that's a Map.
     *
     * @param fieldName The name of the value that will be added to the map.
     * @param value     The value associated with the name that will be added to the map.
     * @param instance  The object instance.
     * @param clazz     The class of the object instance.
     */
    private void addValueToExtrasWithReflection(String fieldName, Object value, Object instance,
                                                Class<?> clazz)
            throws NoSuchFieldException, IllegalAccessException {

        Field field = clazz.getDeclaredField("mExtras");
        field.setAccessible(true);
        Map<String, Object> extras = (Map<String, Object>) field.get(instance);
        if (extras == null) {
            extras = new HashMap<>();
        }
        extras.put(fieldName, value);
        field.set(instance, extras);
        field.setAccessible(false);
    }

    /**
     * This is a private helper method that follows a path through a map to reach a value. The
     * value is then set on the object instance given the field that is part of the path.
     *
     * @param map      The data map. This map is traversed using the path argument to reach a value
     *                 that maps to the field in the path argument.
     * @param path     The path string. It is composed of the following components: the path, the
     *                 path separator, the field that the path maps to. Example: path1/path2@field
     * @param clazz    The class of the object instance.
     * @param instance The object instance to set the field on.
     */
    private void setClazzFieldByMatchingPathFromMap(Map<String, Object> map, String path, Class
            clazz, Object instance, boolean keyDataPath)
            throws NoSuchFieldException, IllegalAccessException, ValueNotFoundException {

        String fieldName = path.substring(path.indexOf(PATH_NAME_SEPARATOR) + 1, path.length());
        String fieldPath = path.substring(0, path.indexOf(PATH_NAME_SEPARATOR));
        Object value = PathHelper.getValueByPath(map, fieldPath);

        if (value != null) {
            Field field;
            try {
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
            }
            catch (NoSuchFieldException e) {
                Log.d(TAG, "Could not find specified field " + fieldName + " while creating " +
                        "object with reflection. Adding it to extras.");
                addValueToExtrasWithReflection(fieldName, value, instance, clazz);
                return;
            }
            // If we are setting the key data path, get the map from the field. This must be a
            // Map<String, Object> and put the value in the map with the keyDataPath tag.
            if (keyDataPath) {
                Map<String, Object> extras = (Map<String, Object>) field.get(instance);
                if (extras == null) {
                    extras = new HashMap<>();
                }
                extras.put(KEY_DATA_PATH_TAG, value.toString());
                field.set(instance, extras);
                field.setAccessible(false);
            }
            else {
                // Was the value represented as a primitive data type or as a string in the map?
                // If a string, then we can not simply cast the value to its proper primitive
                // type. We have to parse it directly instead of casting.
                boolean isValueString = value instanceof String;

                // field of type int
                if (field.getType().equals(Integer.TYPE)) {
                    int result = isValueString ? Integer.parseInt(value.toString()) : (int) value;
                    field.setInt(instance, result);
                }
                // field of type double
                else if (field.getType().equals(Double.TYPE)) {
                    double result = isValueString ? Double.parseDouble(value.toString()) : (double)
                            value;
                    field.setDouble(instance, result);
                }
                // field of type long
                else if (field.getType().equals(Long.TYPE)) {
                    long result = isValueString ? Long.parseLong(value.toString()) : (long) value;
                    field.setLong(instance, result);
                }
                // field of type boolean
                else if (field.getType().equals(Boolean.TYPE)) {
                    boolean result = isValueString ? Boolean.parseBoolean(value.toString()) :
                            (boolean) value;
                    field.setBoolean(instance, result);
                }
                // field of type byte
                else if (field.getType().equals(Byte.TYPE)) {
                    field.setByte(instance, Byte.parseByte(value.toString()));
                }
                // field of type short
                else if (field.getType().equals(Short.TYPE)) {
                    field.setShort(instance, Short.parseShort(value.toString()));
                }
                // field of type char
                else if (field.getType().equals(Character.TYPE)) {
                    field.setChar(instance, value.toString().charAt(0));
                }
                // field of type float
                else if (field.getType().equals(Float.TYPE)) {
                    field.setFloat(instance, Float.parseFloat(value.toString()));
                }
                // field is a List
                else if (field.getType().equals(List.class)) {
                    field.set(instance, value);
                }
                // non-primitive field
                else {
                    field.set(instance, value.toString());
                }
                field.setAccessible(false);
            }
        }
        else {
            Log.w(TAG, "Value for " + fieldName + " was null so not set for Content, this may be " +
                    "intentional.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {

        return DynamicParser.class.getSimpleName();
    }

    /**
     * Validates the given recipe for the parser. If the recipe contains all the mandatory fields,
     * it is considered valid. However, this method does not check the format of the field values.
     *
     * @param recipe The recipe to validate.
     * @return True if the recipe is valid according to parser standards.
     * @throws InvalidParserRecipeException The recipe is not valid.
     */
    public boolean validateRecipe(Recipe recipe) throws InvalidParserRecipeException {

        if (recipe == null || recipe.isEmpty()) {
            throw new InvalidParserRecipeException("Recipe cannot be null or empty");
        }

        String[] fields = getMandatoryRecipeFields();

        // Check that the recipe contains each necessary field and the value isn't null or empty.
        for (String field : fields) {

            // Check the field that should have a list value.
            if (field.equals(MATCH_LIST_TAG)) {
                List<String> value = recipe.getItemAsStringList(field);
                if (value == null) {
                    throw new InvalidParserRecipeException("Recipe is missing field " + field);
                }
            }
            // Check the rest of the fields that should have a String value.
            else {
                String value = recipe.getItemAsString(field);
                if (value == null || value.isEmpty()) {
                    throw new InvalidParserRecipeException("Recipe is missing field " + field);
                }
            }
        }

        return true;
    }

    /**
     * Returns a list of the mandatory fields that a parser recipe must contain. If a recipe does
     * not contain a key/value pair for each of these strings, the recipe is not a valid parser
     * recipe. The mandatory fields are "format", "matchList", "model", "modelType", "cooker", and
     * "query."
     *
     * @return A list of Strings that describe a parser recipe.
     */
    public String[] getMandatoryRecipeFields() {

        return new String[]{FORMAT_TAG, MATCH_LIST_TAG, MODEL_TAG, MODEL_TYPE_TAG, COOKER_TAG,
                QUERY_TAG};
    }

    /**
     * Tries to match the argument typeString to an array or an object. If the argument contains
     * "[]", this indicates the query result type is an array. If the argument contains "{}", this
     * indicates the query result type is a map. If the argument does not match either of these,
     * the default return value is an array of maps.
     *
     * @param typeString The string containing the query result type
     * @return The {@link Class} of the query result type.
     */
    private Class<?> getQueryResultType(String typeString) {

        if (typeString != null) {
            // Result should be an array.
            if (typeString.contains(ARRAY_CLASS_TOKEN)) {
                return (new ArrayList<String>()).getClass();
            }
            // Result should be a map.
            else if (typeString.contains(MAP_CLASS_TOKEN)) {
                return (new HashMap<String, Object>()).getClass();
            }
        }

        // Use the type List<Map> as the default query result type.
        return (new ArrayList<Map<String, Object>>()).getClass();
    }

    /**
     * This task handles translating maps to models asynchronously.
     */
    private class TranslateAsyncTask extends AsyncTask<Void, Void, Void> {

        private final IRecipeCookerCallbacks mIRecipeCookerCallbacks;
        private final Recipe mRecipe;
        private final Bundle mBundle;
        private final List<Map<String, Object>> mTranslationMapList;
        private final boolean mBatch;

        /**
         * Constructor of the async translation task.
         *
         * @param batch                 Whether or not to return items all together as a list or as
         *                              single objects.
         * @param recipe                The parser recipe.
         * @param translationMapList    The list of maps containing the values that are needed by
         *                              the match list.
         * @param recipeCookerCallbacks Recipe cooking callbacks.
         * @param bundle                Extra data
         */
        public TranslateAsyncTask(boolean batch, Recipe recipe,
                                  List<Map<String, Object>> translationMapList,
                                  IRecipeCookerCallbacks recipeCookerCallbacks,
                                  Bundle bundle) {

            mBatch = batch;
            mRecipe = recipe;
            mTranslationMapList = translationMapList;
            mIRecipeCookerCallbacks = recipeCookerCallbacks;
            mBundle = bundle;
        }

        /**
         * {@inheritDoc}
         *
         * Remove this task from {@link DynamicParser}'s list of active tasks.
         */
        @Override
        protected void onPostExecute(Void arg) {

            mAsyncTasks.remove(this);
        }

        /**
         * {@inheritDoc}
         *
         * Translates maps to models as specified by parameters given when this task was created.
         *
         * @param params This params are not used.
         * @return Returns null.
         */
        @Override
        protected Void doInBackground(Void... params) {

            translateMapsToObjects(mBatch, mRecipe, mTranslationMapList, mIRecipeCookerCallbacks,
                                   mBundle);

            return null;
        }

        /**
         * {@inheritDoc}
         *
         * Removes this task from {@link DynamicParser}'s list of active tasks.
         */
        @Override
        protected void onCancelled() {

            mAsyncTasks.remove(this);
        }
    }

    /**
     * An exception class for invalid parser recipes.
     */
    public class InvalidParserRecipeException extends Exception {

        /**
         * Constructor for the exception.
         *
         * @param message The custom message to display.
         */
        public InvalidParserRecipeException(String message) {

            super(message);
        }
    }

    /**
     * An exception class to be used when the requested implementation of {@link IParser} isn't
     * found.
     */
    public class ParserNotFoundException extends Exception {

        /**
         * Constructor for the exception.
         *
         * @param message The custom message to display.
         */
        public ParserNotFoundException(String message) {

            super(message);
        }
    }

    /**
     * An exception class to be used when the requested translator isn't registered with the
     * dynamic parser.
     */
    public class TranslatorNotFoundException extends Exception {

        /**
         * Constructor for the exception.
         *
         * @param message The custom message to display.
         */
        public TranslatorNotFoundException(String message) {

            super(message);
        }
    }

    /**
     * An exception class to be used when the given path and data map did not produce a value.
     */
    public class ValueNotFoundException extends Exception {

        /**
         * Constructor for the exception.
         *
         * @param message The custom message to display.
         */
        public ValueNotFoundException(String message) {

            super(message);
        }
    }

}

