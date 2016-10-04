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
package com.amazon.dataloader.datadownloader;

import com.amazon.android.recipe.Recipe;
import com.amazon.android.utils.FileHelper;
import com.amazon.utils.ObjectVerification;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * An abstract class that implements the base structure of object creators. This class provides
 * static methods like {@link #createInstance(Context)}, {@link #getInstance(Context)} and {@link
 * #isSingleton()} to fetch an instance of the class. {@link #isSingleton()} is used to figure out
 * if the inherited class is a Singleton or not. If its a singleton, use {@link
 * #getInstance(Context)} to get the instance, otherwise use {@link #createInstance(Context)}.
 * Factories call these methods via reflection, so its important to update the static tags
 * associated with the methods if the method names change.
 */
public abstract class AObjectCreator {

    /**
     * The method name for {@link #createInstance(Context)}. This is used by factories to call the
     * actual method via reflection. If the method name changes, this needs to be updated.
     */
    public static final String CREATE_INSTANCE = "createInstance";

    /**
     * The method name for {@link #getInstance(Context)}. This is used by factories to call the
     * actual method via reflection. If the method name changes, this needs to be updated.
     */
    public static final String GET_INSTANCE = "getInstance";

    /**
     * The method name for {@link #isSingleton()}. This is used by factories to call the
     * actual method via reflection. If the method name changes, this needs to be updated.
     */
    public static final String IS_SINGLETON = "isSingleton";

    /**
     * Debug tag.
     */
    private static final String TAG = AObjectCreator.class.getName();

    /**
     * The application context.
     */
    protected final Context mContext;

    /**
     * The configuration for this class.
     */
    protected final Recipe mConfiguration;

    /**
     * An exception class created for {@link AObjectCreator} instances.
     */
    public static class ObjectCreatorException extends Exception {

        /**
         * {@inheritDoc}
         *
         * Constructs an {@link com.amazon.dataloader.datadownloader.AObjectCreator
         * .ObjectCreatorException}.
         */
        public ObjectCreatorException(String msg, Throwable cause) {

            super(msg, cause);
        }

    }

    /**
     * Constructor for an {@link AObjectCreator} instance. The application context is retrieved
     * from the context parameter and stored as the {@link #mContext} to be used later. The {@link
     * #mConfiguration} is also initialized here.
     *
     * @param context The context to use to get the application context.
     * @throws ObjectCreatorException Any exception generated during construction is wrapped in
     *                                this exception
     */
    public AObjectCreator(Context context) throws ObjectCreatorException {

        try {
            this.mContext = ObjectVerification.notNull(context.getApplicationContext(), "Could " +
                    "not extract application context from context");
            this.mConfiguration = Recipe.newInstance(FileHelper.readFile(mContext,
                                                                         getConfigFilePath
                                                                                 (mContext)));
        }
        catch (Exception e) {
            throw new ObjectCreatorException("Could not read configuration from file ", e);
        }
    }

    /**
     * Returns the configuration file path for this class relative to the assets folder.
     *
     * @param context The application context.
     * @return The path of the config file.
     */
    protected abstract String getConfigFilePath(Context context);

    /**
     * This method is used to create an instance of this class.
     *
     * @param context The application context.
     * @return An instance of {@link AObjectCreator}.
     * @throws ObjectCreatorException Thrown for any exception generated while fetching this
     *                                instance.
     */
    public static AObjectCreator createInstance(Context context) throws ObjectCreatorException {

        throw new RuntimeException("createInstance not implemented by AObjectCreator");
    }

    /**
     * This method is used when {@link #isSingleton()} is true. This method should provide the
     * Singleton instance of this class. All implementations must override this method to provide
     * the singleton instance if they have overridden {@link #isSingleton()} to return true. If
     * {@link #isSingleton()} is false this method output can be null, and the {@link
     * #createInstance(Context)} method should be used to create an instance.
     *
     * @param context The application context.
     * @return An instance of this class.
     * @throws ObjectCreatorException Thrown for any exception generated while fetching this
     *                                instance.
     */
    public static AObjectCreator getInstance(Context context) throws ObjectCreatorException {

        if (isSingleton()) {
            throw new RuntimeException("getInstance is not implemented by AObjectCreator");
        }
        else {
            return null;
        }
    }

    /**
     * Determine if this class follows the Singleton pattern. If this class is a singleton, this
     * method should return true and {@link #getInstance(Context)} should be used to access the
     * instance of this class. Otherwise, {@link #createInstance(Context)} should be used to get an
     * instance of this class.
     *
     * The default value returned is false.
     *
     * @return True if this class is a singleton; false otherwise.
     */
    public static boolean isSingleton() {

        return false;
    }

    /**
     * Utility method to create an instance of a class that inherits from {@link AObjectCreator}.
     * This method identifies if the class is a singleton by calling {@link #isSingleton()}
     * and calls {@link #createInstance(Context)} or {@link #getInstance(Context)} accordingly to
     * fetch an instance of that class.
     *
     * @param context   The application context.
     * @param classPath The fully qualified path of the class.
     * @param <T>       The class for which this method is called.
     * @return The instance of the class.
     * @throws ClassNotFoundException           if the class was not found.
     * @throws java.util.NoSuchElementException if the method was not found on the reflection
     *                                          object.
     * @throws IllegalAccessException           if an illegal access attempt was made.
     * @throws InvocationTargetException        if there was an error invoking the method with
     *                                          reflection.
     * @throws NoSuchMethodException            if the method was not found.
     */
    public static <T> T fetchInstanceViaReflection(Context context, String
            classPath) throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {

        Log.i(TAG, "Creating instance of " + classPath);
        long startTime = System.currentTimeMillis();
        Class clazz = Class.forName(classPath);
        Method method = clazz.getMethod(IS_SINGLETON, null);
        boolean isSingleton = (boolean) method.invoke(null, null);

        // If the class is not a singleton, call createInstance.
        if (!isSingleton) {
            method = clazz.getMethod(CREATE_INSTANCE, Context.class);
            Log.d(TAG, classPath + " is not singleton, creating with UrlGeneratorFactory");
        }
        // The class is a singleton, call getInstance.
        else {
            method = clazz.getMethod(GET_INSTANCE, Context.class);
            Log.d(TAG, classPath + " is singleton, fetching with UrlGeneratorFactory");
        }
        T t = (T) method.invoke(null, context);
        long endTime = System.currentTimeMillis();
        Log.d(TAG, "Time taken in fetchInstanceViaReflection " + (endTime - startTime));
        return t;
    }
}
