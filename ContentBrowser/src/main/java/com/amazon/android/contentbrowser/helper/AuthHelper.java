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
import com.amazon.android.module.ModuleManager;
import com.amazon.android.ui.fragments.ErrorDialogFragment;
import com.amazon.android.ui.fragments.LogoutSettingsFragment;
import com.amazon.android.utils.ErrorUtils;
import com.amazon.auth.AuthenticationConstants;
import com.amazon.auth.IAuthentication;
import com.github.droibit.rxactivitylauncher.RxLauncher;

import org.greenrobot.eventbus.EventBus;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import rx.Observable;
import rx.Subscriber;
import rx.operators.OperatorIfThen;

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
     * Powered by logo URL preference's key.
     */
    public static final String AUTH_POWERED_BY_LOGO_URL_PREFERENCES_KEY = "powered_by_logo_url";

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
     * Logout observable.
     *
     * @return RX Observable.
     */
    public Observable<Bundle> logout() {

        Log.v(TAG, "logout called.");

        return Observable.create(subscriber -> {
            mIAuthentication.logout(mAppContext, new IAuthentication.ResponseHandler() {
                @Override
                public void onSuccess(Bundle extras) {

                    broadcastAuthenticationStatus(false);
                    Log.d(TAG, "Account logout success");
                    handleSuccessCase(subscriber, extras);
                }

                @Override
                public void onFailure(Bundle extras) {

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

                    Log.d(TAG, "Account login success");
                    broadcastAuthenticationStatus(true);
                    handleSuccessCase(subscriber, extras);
                }

                @Override
                public void onFailure(Bundle extras) {

                    Log.e(TAG, "Account login failed");
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

        return Observable.create(subscriber -> {
            // Check if user is logged in. If not, show authentication activity.
            mIAuthentication.isResourceAuthorized(mAppContext, "",
                                                  new IAuthentication.ResponseHandler() {
                                                      @Override
                                                      public void onSuccess(Bundle extras) {

                                                          Log.d(TAG, "Resource Authorization " +
                                                                  "success");
                                                          handleSuccessCase(subscriber, extras);
                                                      }

                                                      @Override
                                                      public void onFailure(Bundle extras) {

                                                          Log.e(TAG, "Resource Authorization " +
                                                                  "failed");
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
        // TODO: Handle MVPD logo here.
    }

    /**
     * Authenticate With Activity Observable.
     *
     * @return RX Observable.
     */
    private Observable<Bundle> authenticateWithActivity() {

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
                              Bundle resultBundle;
                              if (activityResult.isOk()) {
                                  resultBundle = new Bundle();
                              }
                              else {
                                  resultBundle = null;
                                  if (activityResult.data != null) {
                                      resultBundle = activityResult.data.getExtras();
                                  }
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
                isAuthenticatedResultBundle ->
                        Observable.create(new OperatorIfThen<>(
                                                  // If isAuthenticated success then do
                                                  // isAuthorized.
                                                  () -> isAuthenticatedResultBundle.getBoolean
                                                          (RESULT),
                                                  isAuthorized(),
                                                  // If isAuthenticated failed then do
                                                  // authenticateWithActivity.
                                                  // Warning!!! After this point all the upcoming
                                                  // tasks needs to be handled
                                                  // in upcoming activity!!!
                                                  authenticateWithActivity()
                                          )
                        )
        );
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
                    // If we got a login screen and login was success
                    if (resultBundle.getBoolean(RESULT_FROM_ACTIVITY)) {
                        // Check if we are authorized in upcoming activity context.
                        mContentBrowser
                                .getNavigator()
                                .runOnUpcomingActivity(() -> isAuthorized().subscribe(bundle -> {
                                    // If we were authorized return success.
                                    if (resultBundle.getBoolean(RESULT)) {
                                        iAuthorizedHandler.onAuthorized(resultBundle);
                                    }
                                    else {
                                        // If we were not authorized return show error.
                                        handleErrorBundle(resultBundle);
                                    }
                                }));
                    }
                    else if (resultBundle.getBoolean(RESULT)) {
                        // If we were logged in and authorized return success.
                        iAuthorizedHandler.onAuthorized(resultBundle);
                    }
                    else {
                        // If everything failed then show error.
                        mContentBrowser.getNavigator()
                                       .runOnUpcomingActivity(() -> handleErrorBundle(resultBundle)
                                       );
                    }
                }, throwable -> Log.e(TAG, "handleAuthChain failed:", throwable));
    }

    /**
     * Handle on activity result.
     *
     * @param contentBrowser Content Browser.
     * @param activity       Activity.
     * @param requestCode    Request code.
     * @param resultCode     Result code.
     * @param data           Intent.
     */
    public void handleOnActivityResult(ContentBrowser contentBrowser, Activity activity,
                                       int requestCode, int resultCode, Intent data) {

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
        mIAuthentication.logout(context, new IAuthentication.ResponseHandler() {
            @Override
            public void onSuccess(Bundle extras) {

                broadcastAuthenticationStatus(false);
                Log.d(TAG, "Account logout success");
            }

            @Override
            public void onFailure(Bundle extras) {

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
    public static ErrorUtils.ERROR_CATEGORY convertAuthErrorToErrorUtils(Bundle bundle) {

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
    private void handleErrorBundle(Bundle extras) {

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
                    }
                    else if (ErrorUtils.ERROR_BUTTON_TYPE.LOGOUT == errorButtonType) {
                        mIAuthentication.logout(activity, new IAuthentication.ResponseHandler() {
                            @Override
                            public void onSuccess(Bundle extras) {

                                broadcastAuthenticationStatus(false);
                                fragment.dismiss();
                            }

                            @Override
                            public void onFailure(Bundle extras) {

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


}
