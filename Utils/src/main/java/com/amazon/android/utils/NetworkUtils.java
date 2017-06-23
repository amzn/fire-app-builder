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
package com.amazon.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility to fetch data from network
 */
public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getName();


    /**
     * Constant used for POST requests.
     */
    public static final String POST = "POST";

    /**
     * Constant used for GET requests.
     */
    public static final String GET = "GET";

    /**
     * Fetch the contents located at the given URL.
     *
     * @param urlString URL to fetch.
     * @return Data located at the URL.
     */
    public static String getDataLocatedAtUrl(String urlString) throws IOException {

        InputStream inputStream = null;

        try {
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            inputStream = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream(), Helpers.getDefaultAppCharset()), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                    inputStream = null;
                }
                catch (IOException e) {
                    Log.e(TAG, "Closing input stream failed", e);
                }
            }
        }
    }

    /**
     * Checks for network connectivity.
     *
     * @param context The context.
     * @return True if connected; false otherwise.
     */
    public static boolean isConnectedToNetwork(Context context) {

        final NetworkInfo networkInfo = ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        // Only care if there is no network info at all or no network connectivity detected.
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Creates an HTTP connection using the given arguments and returns the opened connection.
     *
     * @param type             The request method. POST, GET, etc.
     * @param urlString        The url of the desired connection.
     * @param parameters       A string of parameters to send with the request, if applicable.
     * @param contentType      The content type.
     * @param contentTypeValue The content type value.
     * @return The opened HTTP request.
     */
    public static HttpURLConnection createHttpConnection(String type, String
            urlString, String parameters, String contentType, String contentTypeValue)
            throws IOException {

        HttpURLConnection urlConnection;

        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod(type);
        urlConnection.setRequestProperty(contentType, contentTypeValue);

        if (type.equals(POST)) {
            urlConnection.setDoOutput(true);
            if (parameters != null) {
                PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                out.print(parameters);
                out.close();
            }
        }

        return urlConnection;
    }

    /**
     * Reads the result of the HTTP connection.
     *
     * @param urlConnection The connection to read from.
     * @return The response from the HTTP connection's input stream.
     */
    public static String readHttpResult(HttpURLConnection urlConnection) throws IOException {

        if (urlConnection == null) {
            return null;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                urlConnection.getInputStream(), Helpers.getDefaultAppCharset()), 8);

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * Tests for the existence of the given parameter in the URL.
     *
     * @param urlString The URL.
     * @param parameter The parameter to test for.
     * @return True if the URL contains a value for the given parameter, false otherwise.
     */
    public static boolean urlContainsParameter(String urlString, String parameter) {

        try {
            Map<String, String> parameters = getUrlQueryParameters(urlString);
            return parameters.get(parameter) != null && !parameters.get(parameter).isEmpty();
        }
        catch (MalformedURLException e) {
            Log.e(TAG, "Could not test URL for parameter due to malformed URL.", e);
        }
        return false;
    }

    /**
     * Adds a parameter with the given value to the query of a URL. It will not add the parameter
     * if a value already exists for the parameter in the query or if the URL has no query.
     *
     * @param urlString The URL.
     * @param parameter The parameter to add.
     * @param value     The value to add.
     * @return The URL with the value added, or the original URL if the parameter already exists.
     */
    public static String addParameterToUrl(String urlString, String parameter, String value) {

        String newUrlString = urlString;
        Map<String, String> queryParams;

        try {
            queryParams = getUrlQueryParameters(urlString);
        }
        catch (MalformedURLException e) {
            Log.e(TAG, "Could not add parameter to URL due to malformed URL.", e);
            return newUrlString;
        }

        // Only add the parameter if the original URL contains a query.
        if (!queryParams.isEmpty()) {
            // If the url doesn't have the parameter then just add it to the end.
            if (queryParams.get(parameter) == null) {
                newUrlString = urlString + "&" + parameter + "=" + value;
            }
            // If the url contains the parameter but with no value, insert the value.
            else if (queryParams.get(parameter).isEmpty()) {
                String[] split = urlString.split(parameter + "=");
                if (split.length > 0) {
                    newUrlString = split[0] + parameter + "=" + value;
                    if (split.length > 1) {
                        newUrlString += split[1];
                    }
                }
            }
            else {
                Log.d(TAG, "Cannot add parameter to URL because it already exists");
            }
        }
        return newUrlString;
    }

    /**
     * Gets a map representation of the query parameters and their values for the given URL.
     *
     * @param urlString The URL.
     * @return A map of the query parameters and their values.
     */
    private static Map<String, String> getUrlQueryParameters(String urlString) throws
            MalformedURLException {

        Map<String, String> queryParams = new HashMap<>();
        URL url = new URL(urlString);
        String query = url.getQuery();
        if (query != null) {
            String[] strParams = query.split("&");

            for (String param : strParams) {
                String[] split = param.split("=");
                // Get the parameter name.
                if (split.length > 0) {
                    String name = split[0];
                    // Get the parameter value.
                    if (split.length > 1) {
                        String value = split[1];
                        queryParams.put(name, value);
                    }
                    // If there is no value just put an empty string as placeholder.
                    else {
                        queryParams.put(name, "");
                    }
                }
            }
        }

        return queryParams;
    }
}
