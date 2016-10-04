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
package com.amazon.android.search;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class will act as a stand alone class that will run searches on iteratorable generic
 * objects. It will send out a message on every correct result and send a boolean when the
 * searching is complete. This is designed to be called from the main ui thread.
 */
public class SearchManager<ContainerModel extends Iterable<Model>, Model> {

    // This search algorithm is used for its onCompare method
    @VisibleForTesting
    /*package*/ ISearchAlgo<Model> mSearchAlgo;

    // This Map holds a map of search algorithms
    private ConcurrentHashMap<String, ISearchAlgo> mSearchAlgoMap;

    // This inner class will perform the searches off the main ui thread.
    private SearchTask mSearchTask;

    /**
     * The public constructor of the search manager. A new map is instantiated.
     */
    public SearchManager() {

        mSearchAlgoMap = new ConcurrentHashMap<>();

    }

    /**
     * This method returns the search algorithm map
     *
     * @return The search algorithm map.
     */
    @VisibleForTesting
    /*package*/ Map getSearchAlgoMap() {

        return mSearchAlgoMap;
    }

    /**
     * This method will set the local search algorithm to one found in the map of algorithms.
     * If there is not an algorithm found this method will return false.
     * If there is an algorithm found this method will return true.
     *
     * @param inputAlgo The name of the algorithm that should be used for searching.
     * @return True if the algorithm was found, else false.
     */
    public boolean setSearchAlgoByName(@NonNull final String inputAlgo) {

        if (inputAlgo == null) {
            return false;
        }

        if (mSearchAlgoMap.containsKey(inputAlgo)) {
            // Set the local search algorithm to one from the search algorithm map.
            mSearchAlgo = mSearchAlgoMap.get(inputAlgo);

            return true;
        }

        // The algorithm was not found in the map.
        return false;
    }

    /**
     * This method will add a {@link ISearchAlgo} to the local map {@link #mSearchAlgoMap}.
     *
     * If you plan to update a search algorithm, pass in the original key and its new associated
     * search algorithm.
     *
     * @param searchName The name of the search algorithm that will be added to the map as a key.
     * @param inputAlgo  A concrete class that implements the ISearchAlgo interface. This
     *                   will be added to the map.
     */
    public void addSearchAlgo(@NonNull String searchName, @NonNull final ISearchAlgo inputAlgo) {

        mSearchAlgoMap.put(searchName, inputAlgo);
    }

    /**
     * This method will execute a search off the main ui thread. It uses {@link
     * #checkSearchInputs(String, String, ISearchResult, Iterable)} to verify non null arguments
     * are passed in.
     *
     * If a search is already going, then it will cancel the old one and start a new one.
     *
     * @param searchAlgoName The name of the search algorithm to use.
     * @param queryString    The string that we should search for in the input data.
     * @param resultCallback This is the callback that will be used to send back data.
     * @param inputData      The data object that needs to implement iterator.
     * @throws NullPointerException if any of the inputs are null.
     */
    public void asyncSearch(@NonNull String searchAlgoName,
                            @NonNull final String queryString,
                            @NonNull final ISearchResult resultCallback,
                            @NonNull final ContainerModel inputData) throws NullPointerException {

        checkSearchInputs(searchAlgoName, queryString, resultCallback, inputData);

        // Check to see if we need to cancel an on going search.
        if (mSearchTask != null && mSearchTask.getStatus() != AsyncTask.Status.FINISHED) {
            mSearchTask.cancel(true);
        }

        mSearchTask = new SearchTask(resultCallback);

        // Execute the search off the main ui thread.
        mSearchTask.execute(searchAlgoName, queryString, inputData);

    }

    /**
     * This method will execute to run the search on the main ui thread. It uses {@link
     * #checkSearchInputs(String, String, ISearchResult, Iterable)} to verify non null arguments
     * are passed in.
     *
     * The callback is used to report the search is done.
     *
     * @param searchAlgoName The name of the search algorithm to use.
     * @param queryString    The string that we should search for in the input data.
     * @param resultCallback This is the callback that will be used to send back data.
     * @param inputData      The data object that needs to implement iterator.
     * @throws NullPointerException if any of the inputs are null.
     */
    public void syncSearch(@NonNull final String searchAlgoName,
                           @NonNull final String queryString,
                           @NonNull final ISearchResult resultCallback,
                           @NonNull final ContainerModel inputData) {

        checkSearchInputs(searchAlgoName, queryString, resultCallback, inputData);

        setSearchAlgoByName(searchAlgoName);

        for (Model entry : inputData) {

            // Use the search algorithm that is currently set at mSearchAlgo
            if (mSearchAlgo.onCompare(queryString, entry)) {

                resultCallback.onSearchResult(entry, false);
            }
        }

        // Send a callback with the done flag to true, indicating that the search is complete.
        resultCallback.onSearchResult(null, true);

    }

    /**
     * This helper method checks the input of the searches to ensure none are null.
     *
     * @param searchAlgoName A string that is the name of the search algorithm to use.
     * @param queryString    The query string used for searching.
     * @param resultCallback The callback that is used when searching is done.
     * @param inputData      The content data.
     */
    private void checkSearchInputs(@NonNull final String searchAlgoName,
                                   @NonNull final String queryString,
                                   @NonNull final ISearchResult resultCallback,
                                   @NonNull final ContainerModel inputData) throws
            NullPointerException {

        if (searchAlgoName == null) {
            throw new NullPointerException("searchAlgoName parameter is Null");
        }
        else if (queryString == null) {
            throw new NullPointerException("queryString parameter is Null");
        }
        else if (resultCallback == null) {
            throw new NullPointerException("resultCallback parameter is Null");
        }
        else if (inputData == null) {
            throw new NullPointerException("inputData parameter is Null");
        }
    }

    /**
     * This private inner class will perform a search off the main ui thread.
     */
    private class SearchTask extends AsyncTask<Object, Void, Void> {

        private ISearchResult mISearchResult;

        /**
         * This public constructor will set the results callback
         *
         * @param resultCallback The result callback.
         */
        public SearchTask(ISearchResult resultCallback) {

            mISearchResult = resultCallback;
        }


        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         *
         * This method can call {@link #publishProgress} to publish updates on the UI thread.
         *
         * @param params The parameters of the task.
         *               params[0] should be the search algorithm name.
         *               params[1] should be the query string.
         *               params[2] should be a ContainerModel.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected Void doInBackground(Object... params) {

            setSearchAlgoByName((String) params[0]);

            for (Model entry : (ContainerModel) params[2]) {

                // Check to see if this async task has been cancelled.
                if (this.isCancelled()) {
                    return null;
                }

                if (mSearchAlgo.onCompare((String) params[1], entry)) {

                    mISearchResult.onSearchResult(entry, false);
                }
            }

            return null;
        }

        /**
         * This method is called automatically when the method {@link #doInBackground(Object...)}
         * is finished.
         *
         * @param unused No parameters needed.
         */
        @Override
        protected void onPostExecute(Void unused) {

            // Send a callback with the done flag to true, indicating that the search is complete.
            mISearchResult.onSearchResult(null, true);
        }

        /**
         * This method will be called when {@link #doInBackground(Object...)} finishes and this
         * async task has been cancelled.
         * This needed as a onPostExecute will not be called when the Async task has been
         * cancelled.
         *
         * @param unused No parameters needed.
         */
        @Override
        protected void onCancelled(Void unused) {
            // Send a callback with the done flag to true, indicating that the search is complete.
            mISearchResult.onSearchResult(null, true);
        }


    }

}
