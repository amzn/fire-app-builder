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

package com.amazon.android.auth.facebook;

import com.amazon.android.utils.JsonHelper;
import com.amazon.android.utils.NetworkUtils;
import com.amazon.auth.IAuthentication;
import com.amazon.utils.security.ResourceObfuscator;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * This class handles all the Facebook API calls.
 */
public class FacebookApi {

    /**
     * Constant used to retrieve the user code.
     */
    public static final String USER_CODE = "user_code";

    /**
     * Constant used to retrieve the logged-in user's name.
     */
    public static final String NAME = "name";

    /**
     * Constant used to retrieve the logged-in user's id.
     */
    public static final String ID = "id";

    /**
     * Constant used in HTTP request to define permissions.
     */
    public static final String SCOPE = "scope";

    /**
     * Constant used in HTTP request to define the public profile permission.
     */
    public static final String PUBLIC_PROFILE = "public_profile";

    /**
     * The HTTP request content type.
     */
    public static final String CONTENT_TYPE = "Content-Type";

    /**
     * The HTTP request content type value.
     */
    public static final String CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded";

    /**
     * Debug flag.
     */
    protected static boolean DEBUG = true;

    /**
     * Debug tag.
     */
    private static final String TAG = FacebookApi.class.getSimpleName();

    /**
     * Helper method to make an HTTP call to get the registration and access codes.
     *
     * @param context    The application context.
     * @param accessCode The code needed to request an authentication token.
     * @return A map containing the HTTP response.
     */
    protected static Map makeHttpCallForAuthToken(Context context, String accessCode) {

        String msg = "There was an error authenticating the user entered code.";
        String parameters;
        try {
            parameters = IAuthentication.ACCESS_TOKEN + "="
                    + getAuthenticationAppId(context) + "|" + getAuthenticationClientToken(context)
                    + "&" + IAuthentication.CODE + "=" + accessCode;
        }
        catch (Exception e) {
            return createErrorMap(msg, e);
        }

        return makeHttpCall(context.getString(R.string.facebook_login_status_url),
                            parameters, NetworkUtils.POST, msg);

    }

    /**
     * Creates error map for given message and exception.
     *
     * @param msg message to add to the map.
     * @param e   exception to add to the map.
     * @return error map.
     */
    @NonNull
    private static HashMap<String, Object> createErrorMap(String msg, Exception e) {

        Log.e(TAG, msg, e);
        HashMap<String, Object> errorMap = new HashMap<>();
        errorMap.put(IAuthentication.ResponseHandler.STATUS_CODE, -1);
        errorMap.put(IAuthentication.ResponseHandler.MESSAGE, msg + e.getLocalizedMessage());
        return errorMap;
    }

    /**
     * Makes an HTTP call to get the registration and access codes.
     *
     * @param context The application context.
     * @return A map containing the HTTP response.
     */
    protected static Map makeHttpCallForRegistration(Context context) {

        String parameters;
        String msg = "There was an error retrieving the user code." +
                "Check app id and client token in your configurations";
        try {
            parameters = IAuthentication.ACCESS_TOKEN + "="
                    + getAuthenticationAppId(context) + "|" + getAuthenticationClientToken(context)
                    + "&" + FacebookApi.SCOPE + "=" + FacebookApi.PUBLIC_PROFILE;
        }
        catch (Exception e) {
            return createErrorMap(msg, e);
        }

        return makeHttpCall(context.getString(R.string.facebook_registration_url),
                            parameters, NetworkUtils.POST, msg);
    }

    /**
     * Reads the encrypted authentication token and returns the decrypted version of it.
     *
     * @param context application context
     * @return authentication token in plain text
     * @throws UnsupportedEncodingException       Exception generated during decryption.
     * @throws InvalidAlgorithmParameterException Exception generated during decryption.
     * @throws NoSuchAlgorithmException           Exception generated during decryption.
     * @throws NoSuchPaddingException             Exception generated during decryption.
     * @throws BadPaddingException                Exception generated during decryption.
     * @throws IllegalBlockSizeException          Exception generated during decryption.
     * @throws InvalidKeyException                Exception generated during decryption.
     */
    @NonNull
    private static String getAuthenticationClientToken(Context context) throws
            UnsupportedEncodingException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException,
            IllegalBlockSizeException, InvalidKeyException {

        String encrypted_authentication_client_token =
                context.getString(R.string.encrypted_authentication_client_token);

        return ResourceObfuscator.unobfuscate(encrypted_authentication_client_token,
                                              getRandomStringsForKey(context),
                                              getRandomStringsForIv(context));
    }

    /**
     * Reads the encrypted authentication app Id and returns the decrypted version of it.
     *
     * @param context context application context
     * @return authentication app Id in plain text
     * @throws UnsupportedEncodingException       Exception generated during decryption.
     * @throws InvalidAlgorithmParameterException Exception generated during decryption.
     * @throws NoSuchAlgorithmException           Exception generated during decryption.
     * @throws NoSuchPaddingException             Exception generated during decryption.
     * @throws BadPaddingException                Exception generated during decryption.
     * @throws IllegalBlockSizeException          Exception generated during decryption.
     * @throws InvalidKeyException                Exception generated during decryption.
     */
    @NonNull
    private static String getAuthenticationAppId(Context context) throws
            UnsupportedEncodingException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException,
            IllegalBlockSizeException, InvalidKeyException {

        String encrypted_authentication_app_id =
                context.getString(R.string.encrypted_authentication_app_id);

        return ResourceObfuscator.unobfuscate(encrypted_authentication_app_id,
                                              getRandomStringsForKey(context),
                                              getRandomStringsForIv(context));
    }


    /**
     * Get a list of "random" strings for key.
     *
     * @param context Context to access resources.
     * @return List of strings.
     */
    private static String[] getRandomStringsForKey(Context context) {

        return new String[]{
                context.getString(R.string.fb_key_1),
                context.getString(R.string.fb_key_2),
                context.getString(R.string.fb_key_3)
        };
    }

    /**
     * Get random strings for use with initialization vector.
     *
     * @param context Context to access resources.
     * @return List of strings.
     */
    private static String[] getRandomStringsForIv(Context context) {

        return new String[]{
                context.getString(R.string.fb_key_4),
                context.getString(R.string.fb_key_5),
                context.getString(R.string.fb_key_6)
        };
    }

    /**
     * Checks that the access token is still valid by making a graph API call.
     *
     * @param context     The application context.
     * @param accessToken The access token.
     * @return A map containing the HTTP response.
     */
    protected static Map checkAccessToken(Context context, String accessToken) {

        String url = context.getString(R.string.facebook_check_token_url) + accessToken;

        String msg = "Invalid access token.";

        return makeHttpCall(url, null, NetworkUtils.GET, msg);
    }

    /**
     * Makes an HTTP call to the provided URL using the given parameters and request type.
     *
     * @param url         The URL.
     * @param parameters  The parameters for the request.
     * @param requestType The method type to use for the request. GET, POST, etc.
     * @param errorMsg    The error message to display if the response code is not 200.
     * @return A map containing the response from the HTTP call. If there was an error the map will
     * contain an error message and status code of -1. If the response code was not 200, the map
     * will contain the status code only.
     */
    private static Map makeHttpCall(String url, String parameters, String requestType, String
            errorMsg) {

        HashMap<String, Object> errorMap = new HashMap<>();

        HttpURLConnection urlConnection = null;
        try {
            if (DEBUG) {
                Log.d(TAG, "Making http call to Facebook url: " + url);
            }
            // Create the HTTP request
            urlConnection = NetworkUtils.createHttpConnection(requestType, url, parameters,
                                                              CONTENT_TYPE, CONTENT_TYPE_VALUE);

            // If the status code is not okay then we know then there was an error.
            int statusCode = urlConnection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {

                Log.e(TAG, "Status code was not 200 but was " + statusCode);
                Log.e(TAG, errorMsg);
                errorMap.put(IAuthentication.ResponseHandler.STATUS_CODE, statusCode);
                errorMap.put(IAuthentication.ResponseHandler.MESSAGE, errorMsg);
                return errorMap;
            }

            String response = NetworkUtils.readHttpResult(urlConnection);

            if (DEBUG) {
                Log.d(TAG, "Response from HTTP call: " + response);
            }
            return JsonHelper.stringToMap(response);
        }
        catch (Exception e) {

            String msg = "An error was encountered while making an http call.";

            Log.e(TAG, msg, e);

            errorMap.put(IAuthentication.ResponseHandler.STATUS_CODE, -1);
            errorMap.put(IAuthentication.ResponseHandler.MESSAGE, msg + e.getLocalizedMessage());

            return errorMap;
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

}
