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
package com.amazon.adobepass.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.amazon.utils.security.ResourceObfuscator;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class is responsible for all requests made to Adobe pass clientless API.
 */
public class AdobepassRestClient {

    private static final String BASE_URL = "https://api.auth.adobe.com/";

    private static final String GENERATE_REGISTRATION_CODE_URL = "reggie/v1/%s/regcode";
    private static final String AUTHORIZE_URL = "api/v1/authorize";
    private static final String CHECK_AUTHENTICATION_URL = "api/v1/checkauthn";
    private static final String RETRIEVE_AUTHENTICATION_TOKEN_URL = "api/v1/tokens/authn";
    private static final String RETRIEVE_AUTHORIZATION_TOKEN_URL = "api/v1/tokens/authz";
    private static final String RETRIEVE_SHORT_MEDIA_TOKEN_URL = "api/v1/mediatoken";
    private static final String LOGOUT_URL = "api/v1/logout";

    private static final String REQUESTOR_ID_PARAM = "requestor";
    private static final String DEVICE_TYPE = "FireTV";
    private static final String DEVICE_ID_PARAM = "deviceId";
    private static final String DEVICE_TYPE_PARAM = "deviceType";
    private static final String APP_VERSION_PARAM = "appVersion";
    private static final String REGISTRATION_URL_PARAM = "registrationURL";
    private static final String ACCEPT_HEADER_PARAM = "Accept";
    private static final String HEADER_VALUE_APPLICATION_JSON = "application/json";
    private static final String AUTHORIZATION_HEADER_PARAM = "Authorization";
    private static final String PREF_DEVICE_ID = "PREF_DEVICE_ID";
    private static final String HMAC_SHA1 = "HmacSHA1";
    private static final String RESOURCE_ID_PARAM = "resource";
    private static final String TIME_TO_LIVE_PARAM = "ttl";
    private static final String TAG = AdobepassRestClient.class.getName();
    private static String mDeviceId;

    private static final AsyncHttpClient sAsyncClient = new AsyncHttpClient();
    private static final SyncHttpClient sSyncClient = new SyncHttpClient();

    /**
     * Get the proper HTTP client depending on the current thread. If the current thread is the
     * main UI thread, an {@link AsyncHttpClient} will be returned. Otherwise, we know we're on a
     * non-main thread so a {@link SyncHttpClient} should be used.
     *
     * @return The HTTP client.
     */
    private static AsyncHttpClient getHttpClient() {

        // Test for main UI thread.
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            return sAsyncClient;
        }

        return sSyncClient;
    }

    /**
     * A static method to send a GET request to the URL with the request params.
     * The response is handled by the responseHandler.
     *
     * @param url             The relative URL.
     * @param params          The request params.
     * @param responseHandler The handler for the response.
     */
    private static void get(Context paramContext, String url, RequestParams params,
                            AsyncHttpResponseHandler responseHandler) {

        getHttpClient().get(paramContext, getAbsoluteUrl(url), params, responseHandler);
    }

    /**
     * A static method to send a POST request to the URL with the request params.
     * The response will be handled by the responseHandler.
     *
     * @param url             The relative URL.
     * @param params          The request params.
     * @param responseHandler The handler for the response.
     */
    private static void post(Context paramContext, String url, RequestParams params,
                             AsyncHttpResponseHandler responseHandler) {

        getHttpClient().post(paramContext, getAbsoluteUrl(url), params, responseHandler);
    }

    /**
     * A static method to send a POST request to the Adobe Pass registration web service
     * that returns a randomly generated registration code and login page URI.
     *
     * @param paramContext    The context for accessing shared preferences.
     * @param responseHandler The handler for the response.
     * @throws NoSuchAlgorithmException If the requested algorithm could not be found.
     * @throws InvalidKeyException      If an invalid key was used.
     * @throws IOException              If there was an I/O related error.
     */
    public static void getRegistrationCode(Context paramContext,
                                           AsyncHttpResponseHandler responseHandler)
            throws NoSuchAlgorithmException, InvalidKeyException, IOException,
            NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException {

        try {
            putHeader(ACCEPT_HEADER_PARAM, HEADER_VALUE_APPLICATION_JSON);
            generateAuthorizationHeader(paramContext);

            RequestParams params = new RequestParams();
            params.put(DEVICE_ID_PARAM, getDeviceId(paramContext));
            params.put(DEVICE_TYPE_PARAM, DEVICE_TYPE);
            PackageInfo pInfo;
            try {
                pInfo = paramContext.getPackageManager()
                                    .getPackageInfo(paramContext.getPackageName(), 0);
                String version = pInfo.versionName;
                params.put(APP_VERSION_PARAM, version);
            }
            catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "There was an exception when getting the app package info. ", e);
            }
            params.put(REGISTRATION_URL_PARAM,
                       paramContext.getString(R.string.adobe_pass_registration_url));
            params.put(TIME_TO_LIVE_PARAM,
                       paramContext.getString(R.string.adobe_pass_registration_code_ttl));
            post(paramContext, String.format(GENERATE_REGISTRATION_CODE_URL, paramContext
                    .getString(R.string.adobe_pass_requestor_id)), params, responseHandler);
        }
        catch (Resources.NotFoundException e) {
            Log.e(TAG, "Could not find resource ", e);
            throw e;
        }
    }

    /**
     * A static method to send a GET request to the Adobe Pass web service to verify authorization
     * of the resource.
     *
     * @param paramContext    The context to access shared preferences.
     * @param resourceId      The resource id.
     * @param responseHandler The handler for the response.
     * @throws UnsupportedEncodingException If the encoding type is not supported.
     * @throws NoSuchAlgorithmException     If the requested algorithm could not be found.
     * @throws InvalidKeyException          If an invalid key was used.
     */
    public static void generateAuthorizationRequest(Context paramContext, String resourceId,
                                                    AsyncHttpResponseHandler responseHandler)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException,
            NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException {

        try {
            putHeader(ACCEPT_HEADER_PARAM, HEADER_VALUE_APPLICATION_JSON);
            generateAuthorizationHeader(paramContext);

            RequestParams params = new RequestParams();
            params.put(DEVICE_ID_PARAM, getDeviceId(paramContext));
            params.put(REQUESTOR_ID_PARAM,
                       paramContext.getString(R.string.adobe_pass_requestor_id));
            params.put(DEVICE_TYPE_PARAM, DEVICE_TYPE);
            PackageInfo pInfo;
            try {
                pInfo = paramContext.getPackageManager()
                                    .getPackageInfo(paramContext.getPackageName(), 0);
                String version = pInfo.versionName;
                params.put(APP_VERSION_PARAM, version);
            }
            catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "There was an exception when getting the app package info. ", e);
            }
            params.put(RESOURCE_ID_PARAM, resourceId);
            get(paramContext, AUTHORIZE_URL, params, responseHandler);
        }
        catch (Resources.NotFoundException e) {
            Log.e(TAG, "Could not find resource ", e);
            throw e;
        }
    }

    /**
     * A static method to send a GET request to the Adobe Pass web service
     * that checks the authentication for the device.
     *
     * @param paramContext    The context to access shared preferences.
     * @param responseHandler The handler for the response.
     * @throws UnsupportedEncodingException If the encoding type is not supported.
     * @throws NoSuchAlgorithmException     If the requested algorithm could not be found.
     * @throws InvalidKeyException          If an invalid key was used.
     */
    public static void checkAuthenticationRequest(Context paramContext, AsyncHttpResponseHandler
            responseHandler) throws UnsupportedEncodingException, NoSuchAlgorithmException,
            InvalidKeyException, NoSuchPaddingException, BadPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException {

        try {
            putHeader(ACCEPT_HEADER_PARAM, HEADER_VALUE_APPLICATION_JSON);
            generateAuthorizationHeader(paramContext);

            RequestParams params = new RequestParams();
            params.put(DEVICE_ID_PARAM, getDeviceId(paramContext));
            params.put(DEVICE_TYPE_PARAM, DEVICE_TYPE);
            PackageInfo pInfo;
            try {
                pInfo = paramContext.getPackageManager()
                                    .getPackageInfo(paramContext.getPackageName(), 0);
                String version = pInfo.versionName;
                params.put(APP_VERSION_PARAM, version);
            }
            catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "There was an exception when getting the app package info. ", e);
            }
            get(paramContext, CHECK_AUTHENTICATION_URL, params, responseHandler);
        }
        catch (Resources.NotFoundException e) {
            Log.e(TAG, "Could not find resource ", e);
            throw e;
        }
    }

    /**
     * A static method to send a GET request to the Adobe Pass web service that retrieves the
     * authentication token for the device.
     *
     * @param paramContext    The context for accessing shared preferences.
     * @param responseHandler The handler for the response.
     * @throws UnsupportedEncodingException If the encoding type is not supported.
     * @throws NoSuchAlgorithmException     If the requested algorithm could not be found.
     * @throws InvalidKeyException          If an invalid key was used.
     */
    public static void getAuthenticationTokenRequest(Context paramContext,
                                                     AsyncHttpResponseHandler responseHandler)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException,
            NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException {

        try {
            putHeader(ACCEPT_HEADER_PARAM, HEADER_VALUE_APPLICATION_JSON);
            generateAuthorizationHeader(paramContext);

            RequestParams params = new RequestParams();
            params.put(DEVICE_ID_PARAM, getDeviceId(paramContext));
            params.put(REQUESTOR_ID_PARAM,
                       paramContext.getString(R.string.adobe_pass_requestor_id));
            params.put(DEVICE_TYPE_PARAM, DEVICE_TYPE);
            PackageInfo pInfo;
            try {
                pInfo = paramContext.getPackageManager()
                                    .getPackageInfo(paramContext.getPackageName(), 0);
                String version = pInfo.versionName;
                params.put(APP_VERSION_PARAM, version);
            }
            catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "There was an exception when getting the app package info. ", e);
            }
            get(paramContext, RETRIEVE_AUTHENTICATION_TOKEN_URL, params, responseHandler);
        }
        catch (Resources.NotFoundException e) {
            Log.e(TAG, "Could not find resource ", e);
            throw e;
        }
    }

    /**
     * A static method to send a GET request to the Adobe Pass web service that retrieves the
     * authorization token for the resource Id.
     *
     * @param paramContext    The context for accessing shared preferences.
     * @param responseHandler The handler for the response.
     * @throws UnsupportedEncodingException If the encoding type is not supported.
     * @throws NoSuchAlgorithmException     If the requested algorithm could not be found.
     * @throws InvalidKeyException          If an invalid key was used.
     */
    public static void getAuthorizationTokenRequest(Context paramContext,
                                                    AsyncHttpResponseHandler responseHandler)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException,
            NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException {

        try {
            putHeader(ACCEPT_HEADER_PARAM, HEADER_VALUE_APPLICATION_JSON);
            generateAuthorizationHeader(paramContext);

            RequestParams params = new RequestParams();
            params.put(DEVICE_ID_PARAM, getDeviceId(paramContext));
            params.put(REQUESTOR_ID_PARAM,
                       paramContext.getString(R.string.adobe_pass_requestor_id));
            params.put(DEVICE_TYPE_PARAM, DEVICE_TYPE);
            PackageInfo pInfo;
            try {
                pInfo = paramContext.getPackageManager()
                                    .getPackageInfo(paramContext.getPackageName(), 0);
                String version = pInfo.versionName;
                params.put(APP_VERSION_PARAM, version);
            }
            catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "There was an exception when getting the app package info. ", e);
            }
            params.put(RESOURCE_ID_PARAM, paramContext.getString(R.string.adobe_pass_requestor_id));
            get(paramContext, RETRIEVE_AUTHORIZATION_TOKEN_URL, params, responseHandler);
        }
        catch (Resources.NotFoundException e) {
            Log.e(TAG, "Could not find resource ", e);
            throw e;
        }
    }

    /**
     * A static method to send a GET request to the Adobe Pass web service that retrieves a short
     * media token for the resource.
     *
     * @param paramContext    The context for accessing shared preferences.
     * @param responseHandler The handler for the response.
     * @throws UnsupportedEncodingException If the encoding type is not supported.
     * @throws NoSuchAlgorithmException     If the requested algorithm could not be found.
     * @throws InvalidKeyException          If an invalid key was used.
     */
    public static void getShortMediaTokenRequest(Context paramContext, AsyncHttpResponseHandler
            responseHandler) throws UnsupportedEncodingException, NoSuchAlgorithmException,
            InvalidKeyException, NoSuchPaddingException, BadPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException {

        try {
            putHeader(ACCEPT_HEADER_PARAM, HEADER_VALUE_APPLICATION_JSON);
            generateAuthorizationHeader(paramContext);

            RequestParams params = new RequestParams();
            params.put(DEVICE_ID_PARAM, getDeviceId(paramContext));
            params.put(DEVICE_TYPE_PARAM, DEVICE_TYPE);
            PackageInfo pInfo;
            try {
                pInfo = paramContext.getPackageManager()
                                    .getPackageInfo(paramContext.getPackageName(), 0);
                String version = pInfo.versionName;
                params.put(APP_VERSION_PARAM, version);
            }
            catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "There was an exception when getting the app package info. ", e);
            }
            params.put(RESOURCE_ID_PARAM, paramContext.getString(R.string.adobe_pass_requestor_id));
            get(paramContext, RETRIEVE_SHORT_MEDIA_TOKEN_URL, params, responseHandler);
        }
        catch (Resources.NotFoundException e) {
            Log.e(TAG, "Could not find resource ", e);
            throw e;
        }
    }

    /**
     * A static method to send a DELETE request to the Adobe Pass web service that deletes the
     * authN and authZ token for the device.
     *
     * @param paramContext    The context for accessing shared preferences.
     * @param responseHandler The handler for the response.
     * @throws UnsupportedEncodingException If the encoding type is not supported.
     * @throws NoSuchAlgorithmException     If the requested algorithm could not be found.
     * @throws InvalidKeyException          If an invalid key was used.
     */
    public static void logoutRequest(Context paramContext, AsyncHttpResponseHandler
            responseHandler) throws UnsupportedEncodingException, NoSuchAlgorithmException,
            InvalidKeyException, NoSuchPaddingException, BadPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException {

        try {
            putHeader(ACCEPT_HEADER_PARAM, HEADER_VALUE_APPLICATION_JSON);
            generateAuthorizationHeader(paramContext);

            RequestParams params = new RequestParams();
            params.put(DEVICE_ID_PARAM, getDeviceId(paramContext));
            params.put(DEVICE_TYPE_PARAM, DEVICE_TYPE);
            PackageInfo pInfo;
            try {
                pInfo = paramContext.getPackageManager().getPackageInfo(paramContext
                                                                                .getPackageName()
                        , 0);
                String version = pInfo.versionName;
                params.put(APP_VERSION_PARAM, version);
            }
            catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "There was an exception when getting the app package info. ", e);
            }
            getHttpClient().delete(paramContext, getAbsoluteUrl(LOGOUT_URL), null, params,
                                   responseHandler);
        }
        catch (Resources.NotFoundException e) {
            Log.e(TAG, "Could not find resource ", e);
            throw e;
        }
    }

    /**
     * Cancels all pending or potentially active requests associated with any {@link Context}.
     */
    public static void cancelAllRequests() {

        getHttpClient().cancelAllRequests(true);
    }

    /**
     * A static method that generates the authorization header for the Adobe Pass web service
     * requests.
     *
     * @param paramContext The context.
     */
    private static void generateAuthorizationHeader(Context paramContext) throws
            UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
            NoSuchPaddingException {

        String timeMillis = Long.toString(System.currentTimeMillis());
        String publicKey =
                decryptKey(paramContext,
                           paramContext.getString(R.string.encrypted_adobe_pass_public_key));

        String privateKey =
                decryptKey(paramContext,
                           paramContext.getString(R.string.encrypted_adobe_pass_private_key));

        String message = "POST requestor_id=" +
                paramContext.getString(R.string.adobe_pass_requestor_id) + ", nonce=" +
                generateUuid() + ", signature_method=" + "HMAC-SHA1" + ", request_time=" +
                timeMillis + ", request_uri=" + "/regcode";

        putHeader(AUTHORIZATION_HEADER_PARAM, (message + ", public_key=" + publicKey + ", " +
                "signature=" + hmacSha1(message, privateKey)).trim());
    }

    /**
     * Decrypts the given key.
     *
     * @param appContext The application context. We need this to access application resources.
     * @param key        The key to decrypt.
     * @return The decrypted key in plain text.
     */
    private static String decryptKey(Context appContext, String key) throws
            UnsupportedEncodingException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException,
            IllegalBlockSizeException, InvalidKeyException {

        return ResourceObfuscator.unobfuscate(key, getRandomStringsForKey(appContext),
                                              getRandomStringsForIv(appContext));
    }

    /**
     * Get a list of "random" strings for key.
     *
     * @param context Context to access resources.
     * @return List of strings.
     */
    private static String[] getRandomStringsForKey(Context context) {

        return new String[]{
                context.getString(R.string.adobepass_key_1),
                context.getString(R.string.adobepass_key_4),
                context.getString(R.string.adobepass_key_3)
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
                context.getString(R.string.adobepass_key_2),
                context.getString(R.string.adobepass_key_3)
        };
    }

    /**
     * Adds a header to the request.
     *
     * @param key   The key.
     * @param value The value.
     */
    private static void putHeader(String key, String value) {

        getHttpClient().addHeader(key, value);
    }

    /**
     * Appends a relative URL to the {@link #BASE_URL}.
     *
     * @param relativeUrl The URL to append.
     * @return The URL appended to the base URL.
     */
    private static String getAbsoluteUrl(String relativeUrl) {

        return BASE_URL + relativeUrl;
    }

    /**
     * Generates a random UUID.
     *
     * @return A random UUID.
     */
    private static String generateUuid() {

        return UUID.randomUUID().toString();
    }

    /**
     * Gets the device ID. This method either returns an already generated device ID or generates a
     * new one and saves it to shared preferences.
     *
     * @param context The context.
     * @return The device id.
     */
    private static String getDeviceId(Context context) {

        if (mDeviceId == null) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_DEVICE_ID, 0);
            mDeviceId = prefs.getString(PREF_DEVICE_ID, null);
            if (mDeviceId == null) {
                mDeviceId = UUID.randomUUID().toString();
                SharedPreferences.Editor prefsEditor = prefs.edit();
                prefsEditor.putString(PREF_DEVICE_ID, mDeviceId);
                prefsEditor.apply();
            }
        }
        return mDeviceId;
    }

    /**
     * Encodes a string using Base64 by computing the HMAC-SHA1 for the value with the key
     * provided.
     *
     * @param value The value to encode.
     * @param key   The key to use in the encoding.
     * @return The encoded value.
     */
    private static String hmacSha1(String value, String key)
            throws NoSuchAlgorithmException,
            InvalidKeyException {

        String type = HMAC_SHA1;
        SecretKeySpec secret = new SecretKeySpec(key.getBytes(), type);
        Mac mac = Mac.getInstance(type);
        mac.init(secret);
        byte[] bytes = mac.doFinal(value.getBytes());
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

}
