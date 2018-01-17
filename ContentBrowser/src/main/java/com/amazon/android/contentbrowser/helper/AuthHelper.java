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

package com.amazon.android.contentbrowser.helper;

import com.amazon.android.contentbrowser.ContentBrowser;
import com.amazon.android.contentbrowser.R;
import com.amazon.android.model.content.Content;
import com.amazon.android.module.ModuleManager;
import com.amazon.android.ui.constants.PreferencesConstants;
import com.amazon.android.ui.fragments.ErrorDialogFragment;
import com.amazon.android.ui.fragments.LogoutSettingsFragment;
import com.amazon.android.utils.ErrorUtils;
import com.amazon.android.utils.GlideHelper;
import com.amazon.android.utils.NetworkUtils;
import com.amazon.android.utils.Preferences;
import com.amazon.auth.AuthenticationConstants;
import com.amazon.auth.IAuthentication;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.droibit.rxactivitylauncher.RxLauncher;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import rx.Observable;
import rx.Subscriber;

/**
 * Authentication helper class.
 */
public class AuthHelper {

    /**
     * Event class to represent Authorization events.
     */
    public static class AuthenticationStatusUpdateEvent {

        /**
         * User authentication flag.
         */
        private boolean mUserAuthenticated = false;

        /**
         * Constructor
         *
         * @param flag User authentication flag.
         */
        public AuthenticationStatusUpdateEvent(boolean flag) {

            mUserAuthenticated = flag;
        }

        /**
         * Returns true if the user is authentication after this event happened, false otherwise
         *
         * @return true if the user is authentication after this event happened, false otherwise
         */
        public boolean isUserAuthenticated() {

            return mUserAuthenticated;
        }
    }

    /**
     * Debug TAG.
     */
    private static final String TAG = AuthHelper.class.getSimpleName();

    /**
     * Request code MUST be a positive value, otherwise the result callback will not be
     * triggered!!!
     * So no 0xCAFEBABE form good old days :(
     */
    public static final int AUTH_ON_ACTIVITY_RESULT_REQUEST_CODE = 0x0AFEBABE;

    /**
     * The key to retrieve the "login later" preferences.
     */
    public static final String LOGIN_LATER_PREFERENCES_KEY = "LOGIN_LATER_PREFERENCES_KEY";

    /**
     * Result key.
     */
    public static final String RESULT = "RESULT";

    /**
     * Result from activity key.
     */
    public static final String RESULT_FROM_ACTIVITY = "RESULT_FROM_ACTIVITY";

    /**
     * String constant to get the white list object from the MVPD JSONObject.
     */
    private static final String MVPD_WHITE_LIST = "mvpdWhitelist";

    /**
     * String constant to get the logged-in image URL for the MVPD JSONObject.
     */
    private static final String LOGGED_IN_IMAGE = "loggedInImage";

    /**
     * String constant to compare the default MVPD string value in custom.xml to.
     */
    private static final String DEFAULT_MVPD_URL = "DEFAULT_MVPD_URL";

    /**
     * String constant to be used as default error in authentication events.
     */
    private static final String DEFAULT_AUTH_ERROR = "Unknown";

    /**
     * Authentication implementation reference.
     */
    private IAuthentication mIAuthentication;

    /**
     * Application context.
     */
    private final Context mAppContext;

    /**
     * Content browser reference.
     */
    private final ContentBrowser mContentBrowser;

    /**
     * RX Launcher instance.
     */
    private final RxLauncher mRxLauncher = RxLauncher.getInstance();

    /**
     * Authorized handler interface.
     */
    public interface IAuthorizedHandler {

        /**
         * On authorized listener.
         *
         * @param extra Extra result bundle.
         */
        void onAuthorized(Bundle extra);
    }

    /**
     * Constructor.
     *
     * @param context        Context.
     * @param contentBrowser Content browser.
     */
    public AuthHelper(Context context, ContentBrowser contentBrowser) {

        mAppContext = context.getApplicationContext();
        mContentBrowser = contentBrowser;

        // Get default Auth interface without creating a new one.
        try {
            mIAuthentication =
                    (IAuthentication) ModuleManager.getInstance()
                                                   .getModule(IAuthentication.class.getSimpleName())
                                                   .getImpl(true);
        }
        catch (Exception e) {
            Log.e(TAG, "No Auth Interface interface attached.", e);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LogoutSettingsFragment.LOGOUT_BUTTON_BROADCAST_INTENT_ACTION);
        LocalBroadcastManager.getInstance(mAppContext)
                             .registerReceiver(mLocalBroadcastReceiver, intentFilter);
        //AuthHelper is initialized, broadcast initial authentication status
        isAuthenticated().subscribe(isAuthenticatedResultBundle -> {
            boolean result = isAuthenticatedResultBundle.getBoolean(AuthHelper.RESULT);
            broadcastAuthenticationStatus(result);
        });
    }

    /**
     * Method to broadcast authentication status, this includes informing content browser about the
     * new status and sending an Event bus event.
     *
     * @param authenticationStatus authentication status to broadcast
     */
    private void broadcastAuthenticationStatus(boolean authenticationStatus) {

        mContentBrowser.onAuthenticationStatusUpdateEvent(
                new AuthenticationStatusUpdateEvent(authenticationStatus));
        EventBus.getDefault().post(new AuthenticationStatusUpdateEvent(authenticationStatus));
    }

    /**
     * Get authentication interface.
     *
     * @return Authentication interface.
     */
    public IAuthentication getIAuthentication() {

        return mIAuthentication;
    }

    /**
     * Local broadcast receiver to listen to analytics events, useful for interface decoupled
     * access.
     */
    private final BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction()
                      .equals(LogoutSettingsFragment.LOGOUT_BUTTON_BROADCAST_INTENT_ACTION)) {
                Log.d(TAG, "Got logout broadcast!!! : " + intent);
                logoutFromAccount(mAppContext);
            }
        }
    };

    /**
     * Handle success case of subscriber.
     *
     * @param subscriber Subscriber.
     * @param extras     Result bundle.
     */
    private void handleSuccessCase(Subscriber subscriber, Bundle extras) {

        extras.putBoolean(RESULT, true);
        if (!subscriber.isUnsubscribed()) {
            subscriber.onNext(extras);
        }
        subscriber.onCompleted();
    }

    /**
     * Handle failure case of subscriber.
     *
     * @param subscriber Subscriber.
     * @param extras     Result bundle.
     */
    private void handleFailureCase(Subscriber subscriber, Bundle extras) {

        extras.putBoolean(RESULT, false);
        if (!subscriber.isUnsubscribed()) {
            subscriber.onNext(extras);
        }
        subscriber.onCompleted();
    }

    /**
     * retrieve Error Category.
     *
     * @param extras Bundle.
     * @return Error category.
     */
    String retrieveErrorCategory(Bundle extras) {

        Bundle bundle = extras.getBundle(AuthenticationConstants.ERROR_BUNDLE);
        String authErrorCategory = DEFAULT_AUTH_ERROR;
        if (bundle != null) {
            authErrorCategory = bundle.getString(AuthenticationConstants.ERROR_CATEGORY,
                                                 DEFAULT_AUTH_ERROR);
        }
        return authErrorCategory;
    }

    /**
     * Logout observable.
     *
     * @return RX Observable.
     */
    public Observable<Bundle> logout() {

        Log.v(TAG, "logout called.");
        AnalyticsHelper.trackLogOutRequest();
        return Observable.create(subscriber -> {
            mIAuthentication.logout(mAppContext, new IAuthentication.ResponseHandler() {
                @Override
                public void onSuccess(Bundle extras) {

                    AnalyticsHelper.trackLogOutResultSuccess();
                    broadcastAuthenticationStatus(false);
                    Log.d(TAG, "Account logout success");
                    handleSuccessCase(subscriber, extras);
                }

                @Override
                public void onFailure(Bundle extras) {

                    AnalyticsHelper.trackLogOutResultFailure(retrieveErrorCategory(extras));
                    Log.e(TAG, "Account logout failure");
                    handleFailureCase(subscriber, extras);
                }
            });
        });
    }

    /**
     * Is authenticated observable.
     *
     * @return RX Observable.
     */
    public Observable<Bundle> isAuthenticated() {

        return Observable.create(subscriber -> {
            // Check if user is logged in. If not, show authentication activity.
            mIAuthentication.isUserLoggedIn(mAppContext, new IAuthentication.ResponseHandler() {
                @Override
                public void onSuccess(Bundle extras) {

                    Log.d(TAG, "User is authenticated");
                    broadcastAuthenticationStatus(true);
                    handleSuccessCase(subscriber, extras);
                    // Try getting the MVPD provider name from extras to set the display logo.
                    String mvpdName = extras.getString(PreferencesConstants.MVPD_DISPLAY_NAME);
                    if (mvpdName != null) {
                        Preferences.setString(PreferencesConstants.MVPD_LOGO_URL,
                                              mContentBrowser.getPoweredByLogoUrlByName(mvpdName));
                    }
                }

                @Override
                public void onFailure(Bundle extras) {

                    Log.e(TAG, "User is not authenticated");
                    // Clear the MVPD logo from preferences since user is not logged in.
                    Preferences.setString(PreferencesConstants.MVPD_LOGO_URL, "");
                    broadcastAuthenticationStatus(false);
                    handleFailureCase(subscriber, extras);
                }
            });
        });
    }

    /**
     * Is authorized Observable.
     *
     * @return RX Observable.
     */
    public Observable<Bundle> isAuthorized() {

        //get the requested content
        Content content = mContentBrowser.getLastSelectedContent();
        AnalyticsHelper.trackAuthorizationRequest(content);
        return Observable.create(subscriber -> {
            // Check if user is logged in. If not, show authentication activity.
            mIAuthentication.isResourceAuthorized(mAppContext, "",
                                                  new IAuthentication.ResponseHandler() {
                                                      @Override
                                                      public void onSuccess(Bundle extras) {

                                                          Log.d(TAG, "Resource Authorization " +
                                                                  "success");
                                                          AnalyticsHelper
                                                                  .trackAuthorizationResultSuccess(content);
                                                          handleSuccessCase(subscriber, extras);
                                                      }

                                                      @Override
                                                      public void onFailure(Bundle extras) {

                                                          Log.e(TAG, "Resource Authorization " +
                                                                  "failed");
                                                          AnalyticsHelper
                                                                  .trackAuthorizationResultFailure(content, retrieveErrorCategory(extras));
                                                          handleFailureCase(subscriber, extras);
                                                      }
                                                  });
        });
    }

    /**
     * Handle authentication activity result bundle.
     *
     * @param bundle Activity result bundle.
     */
    private void handleAuthenticationActivityResultBundle(Bundle bundle) {

        Bundle mvpdBundle = null;
        if (bundle != null) {
            mvpdBundle = (Bundle) bundle.get(AuthenticationConstants.MVPD_BUNDLE);
        }

        if (mvpdBundle == null) {
            Log.w(TAG, "No MVPD bundle found when handling authentication result");
            return;
        }
        String mvpd = mvpdBundle.getString(AuthenticationConstants.MVPD);

        String mvpdLogoUrl = mContentBrowser.getPoweredByLogoUrlByName(mvpd);
        if (mvpdLogoUrl == null || mvpdLogoUrl.isEmpty()) {
            Log.d(TAG, "MVPD url not found for:" + mvpd);
        }
        Preferences.setString(PreferencesConstants.MVPD_LOGO_URL, mvpdLogoUrl);
        Log.d(TAG, "MVPD in details:" + mvpd + " logo url:" + mvpdLogoUrl);

    }

    /**
     * Authenticate With Activity Observable.
     *
     * @return RX Observable.
     */
    public Observable<Bundle> authenticateWithActivity() {

        AnalyticsHelper.trackAuthenticationRequest();
        return mRxLauncher.from(mContentBrowser.getNavigator()
                                               .getActiveActivity())
                          .startActivityForResult(getIAuthentication()
                                                          .getAuthenticationActivityIntent
                                                                  (mContentBrowser
                                                                           .getNavigator()
                                                                           .getActiveActivity()),
                                                  AuthHelper.AUTH_ON_ACTIVITY_RESULT_REQUEST_CODE,
                                                  null)
                          .map(activityResult -> {
                              Bundle resultBundle = null;
                              if (activityResult.isOk() && activityResult.data == null) {
                                  resultBundle = new Bundle();
                              }
                              else if (activityResult.data != null) {
                                  resultBundle = activityResult.data.getExtras();
                              }
                              else {
                                  // Cancel auth request.
                                  cancelAllRequests();
                                  return resultBundle;
                              }

                              //Check if authentication succeeded.
                              if (activityResult.isOk()) {
                                  AnalyticsHelper.trackAuthenticationResultSuccess();
                              }
                              else {
                                  AnalyticsHelper.trackAuthenticationResultFailure
                                          (retrieveErrorCategory(resultBundle));
                              }

                              handleAuthenticationActivityResultBundle(resultBundle);

                              if (resultBundle != null) {
                                  resultBundle.putBoolean(RESULT, activityResult.isOk());
                                  broadcastAuthenticationStatus(activityResult.isOk());
                                  resultBundle.putBoolean(RESULT_FROM_ACTIVITY, true);
                              }
                              return resultBundle;
                          });
    }

    /**
     * Authenticate Observable.
     *
     * @return RX Observable.
     */
    private Observable<Bundle> authenticate() {

        return isAuthenticated().flatMap(
                // With isAuthenticated result bundle do
                isAuthenticatedResultBundle -> {
                    if (isAuthenticatedResultBundle.getBoolean(RESULT)) {
                        // If isAuthenticated success then do isAuthorized.
                        return isAuthorized();
                    }
                    else {
                        // If isAuthenticated failed then do
                        // authenticateWithActivity.
                        // Warning!!! After this point all the
                        // upcoming tasks needs to be handled
                        // in upcoming activity!!!
                        return authenticateWithActivity();
                    }
                }
        );
    }

    /**
     * Handle Authentication Chain.
     *
     * @param iAuthorizedHandler   Authorized handler.
     * @param authenticationResult Bundle
     */
    public void handleAuthChain(IAuthorizedHandler iAuthorizedHandler, Bundle
            authenticationResult) {

        if (authenticationResult == null) {
            Log.w(TAG, "resultBundle is null, user probably pressed back on login screen");
        }
        // If we got Authentication Result success
        else if (authenticationResult.getBoolean(RESULT)) {
            // Check if we are authorized in current activity context.
            isAuthorized().subscribe(bundle -> {
                // If we were authorized return success.
                if (bundle.getBoolean(RESULT)) {
                    iAuthorizedHandler.onAuthorized(bundle);
                }
                else {
                    // If we were not authorized return show error.
                    handleErrorBundle(bundle);
                }
            });
        }
        else {
            // If everything failed then show error.
            handleErrorBundle(authenticationResult);
        }
    }

    /**
     * Handle Authentication Chain.
     *
     * @param iAuthorizedHandler Authorized handler.
     */

    public void handleAuthChain(IAuthorizedHandler iAuthorizedHandler) {

        // Check authentication first.
        authenticate()
                .subscribe(resultBundle -> {
                    if (resultBundle == null) {
                        Log.w(TAG, "resultBundle is null, user probably pressed back on login " +
                                "screen");
                    }
                    // If we got a login screen and login was success
                    else if (resultBundle.getBoolean(RESULT_FROM_ACTIVITY)) {
                        //If authentication succeeded
                        if (resultBundle.getBoolean(RESULT)) {
                            // Check if we are authorized in upcoming activity context.
                            mContentBrowser
                                    .getNavigator()
                                    .runOnUpcomingActivity(() -> isAuthorized().subscribe(bundle -> {
                                        // If we were authorized return success.
                                        if (bundle.getBoolean(RESULT)) {
                                            iAuthorizedHandler.onAuthorized(bundle);
                                        }
                                        else {
                                            // If we were not authorized return show error.
                                            handleErrorBundle(bundle);
                                        }
                                    }));
                        }
                        else {
                            // If we were not authenticated return show error.
                            mContentBrowser.getNavigator()
                                           .runOnUpcomingActivity(() -> handleErrorBundle
                                                   (resultBundle));
                        }
                    }
                    else if (resultBundle.getBoolean(RESULT)) {
                        // If we were logged in and authorized return success.
                        iAuthorizedHandler.onAuthorized(resultBundle);
                    }
                    else {
                        // If everything failed then show error.
                        handleErrorBundle(resultBundle);
                    }
                }, throwable -> Log.e(TAG, "handleAuthChain failed:", throwable));
    }

    /**
     * Handle on activity result.
     *
     * @param requestCode Request code.
     * @param resultCode  Result code.
     * @param data        Intent.
     */
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(TAG, "handleOnActivityResult " + requestCode);
        mRxLauncher.activityResult(requestCode, resultCode, data);
    }

    /**
     * Cancel all ongoing requests.
     */
    public void cancelAllRequests() {

        if (mIAuthentication != null) {
            Log.d(TAG, "cancelAllRequests");
            mIAuthentication.cancelAllRequests();
        }
    }

    /**
     * Attempt to logout from account.
     * TODO: Devtech 2447: Utilize logout method implemented above.
     *
     * @param context Context object.
     */
    private void logoutFromAccount(Context context) {

        Log.v(TAG, "logoutFromAccount called.");
        AnalyticsHelper.trackLogOutRequest();
        mIAuthentication.logout(context, new IAuthentication.ResponseHandler() {
            @Override
            public void onSuccess(Bundle extras) {

                AnalyticsHelper.trackLogOutResultSuccess();
                broadcastAuthenticationStatus(false);
                Log.d(TAG, "Account logout success");
            }

            @Override
            public void onFailure(Bundle extras) {

                AnalyticsHelper.trackLogOutResultFailure(retrieveErrorCategory(extras));
                Log.e(TAG, "Account logout failure");
            }
        });
    }

    /**
     * Convert auth error to utils error.
     *
     * @param bundle Auth error bundle.
     * @return Error category.
     */
    private static ErrorUtils.ERROR_CATEGORY convertAuthErrorToErrorUtils(Bundle bundle) {

        switch (bundle.getString(AuthenticationConstants.ERROR_CATEGORY)) {
            case AuthenticationConstants.REGISTRATION_ERROR_CATEGORY:
                return ErrorUtils.ERROR_CATEGORY.REGISTRATION_CODE_ERROR;
            case AuthenticationConstants.NETWORK_ERROR_CATEGORY:
                return ErrorUtils.ERROR_CATEGORY.NETWORK_ERROR;
            case AuthenticationConstants.AUTHENTICATION_ERROR_CATEGORY:
                return ErrorUtils.ERROR_CATEGORY.AUTHENTICATION_ERROR;
            case AuthenticationConstants.AUTHORIZATION_ERROR_CATEGORY:
                return ErrorUtils.ERROR_CATEGORY.AUTHORIZATION_ERROR;
        }
        return ErrorUtils.ERROR_CATEGORY.NETWORK_ERROR;
    }

    /**
     * Handle error bundle.
     * TODO: Devtech 2447: Utilize logout method implemented above.
     *
     * @param extras Extras bundle.
     */
    public void handleErrorBundle(Bundle extras) {

        Bundle bundle = extras.getBundle(AuthenticationConstants.ERROR_BUNDLE);
        Activity activity = mContentBrowser.getNavigator()
                                           .getActiveActivity();
        Log.d(TAG, "handleErrorBundle called" + activity.getLocalClassName());
        ErrorHelper.injectErrorFragment(
                activity,
                convertAuthErrorToErrorUtils(bundle),
                (fragment, errorButtonType, errorCategory) -> {
                    if (ErrorUtils.ERROR_BUTTON_TYPE.DISMISS == errorButtonType) {
                        fragment.dismiss();
                        mContentBrowser.updateContentActions();
                    }
                    else if (ErrorUtils.ERROR_BUTTON_TYPE.LOGOUT == errorButtonType) {
                        AnalyticsHelper.trackLogOutRequest();
                        mIAuthentication.logout(activity, new IAuthentication.ResponseHandler() {
                            @Override
                            public void onSuccess(Bundle extras) {

                                AnalyticsHelper.trackLogOutResultSuccess();
                                broadcastAuthenticationStatus(false);
                                fragment.dismiss();
                                mContentBrowser.updateContentActions();
                            }

                            @Override
                            public void onFailure(Bundle extras) {

                                AnalyticsHelper.trackLogOutResultFailure(retrieveErrorCategory
                                                                                 (extras));
                                fragment.getArguments()
                                        .putString(ErrorDialogFragment.ARG_ERROR_MESSAGE,
                                                   activity.getResources().getString(
                                                           R.string.logout_failure_message));
                            }
                        });
                    }
                }
        );
    }

    /**
     * Retrieves the list of possible MVPD providers from the URL found at R.string.mvpd_url in
     * custom.xml and gives them to ContentBrowser.
     */
    public void setupMvpdList() {

        try {

            String mvpdUrl = mAppContext.getResources().getString(R.string.mvpd_url);

            // The user has no MVPD URL set up.
            if (mvpdUrl.equals(DEFAULT_MVPD_URL)) {
                Log.d(TAG, "MVPD feature not used.");
                return;
            }
            String jsonStr = NetworkUtils.getDataLocatedAtUrl(mvpdUrl);

            JSONObject json = new JSONObject(jsonStr);
            JSONArray mvpdWhiteList = json.getJSONArray(MVPD_WHITE_LIST);

            for (int i = 0; i < mvpdWhiteList.length(); i++) {

                JSONObject mvpdItem = mvpdWhiteList.getJSONObject(i);
                mContentBrowser.addPoweredByLogoUrlByName(
                        mvpdItem.getString(PreferencesConstants.MVPD_LOGO_URL),
                        mvpdItem.getString(LOGGED_IN_IMAGE));
            }

        }
        catch (Exception e) {
            Log.e(TAG, "Get MVPD logo urls failed!!!", e);

        }
    }

    /**
     * Load powered by logo from preferences with Glide.
     *
     * @param context                The context to use.
     * @param poweredByLogoImageView The image view to use.
     */
    public void loadPoweredByLogo(Context context, ImageView poweredByLogoImageView) {

        try {
            String mvpdUrl = Preferences.getString(PreferencesConstants.MVPD_LOGO_URL);

            if (poweredByLogoImageView == null || mvpdUrl.isEmpty()) {
                Log.d(TAG, "No MVPD image view or URL found.");
                return;
            }
            poweredByLogoImageView.setVisibility(View.INVISIBLE);

            RequestListener listener = new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable>
                        target, boolean isFirstResource) {

                    poweredByLogoImageView.setVisibility(View.INVISIBLE);
                    Log.e(TAG, "Get image with glide failed for powered by logo!!!", e);
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model,
                                               Target<GlideDrawable> target, boolean
                                                       isFromMemoryCache, boolean isFirstResource) {

                    poweredByLogoImageView.setVisibility(View.VISIBLE);
                    return false;
                }
            };

            GlideHelper.loadImageIntoView(
                    poweredByLogoImageView, context, mvpdUrl, listener, android.R.color.transparent,
                    new ColorDrawable(ContextCompat.getColor(context,
                                                             android.R.color.transparent)));
        }
        catch (Exception e) {
            Log.e(TAG, "loadPoweredByLogo failed, activity may not include it.", e);
        }
    }
}
