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

import com.amazon.android.contentbrowser.database.helpers.RecentDatabaseHelper;
import com.amazon.android.contentbrowser.database.helpers.WatchlistDatabaseHelper;
import com.amazon.android.contentbrowser.database.records.RecentRecord;
import com.amazon.android.contentbrowser.helper.AnalyticsHelper;
import com.amazon.android.contentbrowser.helper.AuthHelper;
import com.amazon.android.contentbrowser.helper.ErrorHelper;
import com.amazon.android.contentbrowser.helper.FontManager;
import com.amazon.android.contentbrowser.helper.LauncherIntegrationManager;
import com.amazon.android.contentbrowser.helper.PurchaseHelper;
import com.amazon.android.contentbrowser.recommendations.RecommendationManager;
import com.amazon.android.interfaces.ICancellableLoad;
import com.amazon.android.interfaces.IContentBrowser;
import com.amazon.android.model.Action;
import com.amazon.android.model.content.Content;
import com.amazon.android.model.content.ContentContainer;
import com.amazon.android.model.content.constants.PreferencesConstants;
import com.amazon.android.model.event.ActionUpdateEvent;
import com.amazon.android.module.ModularApplication;
import com.amazon.android.navigator.Navigator;
import com.amazon.android.navigator.UINode;
import com.amazon.android.recipe.Recipe;
import com.amazon.android.search.ISearchAlgo;
import com.amazon.android.search.ISearchResult;
import com.amazon.android.search.SearchManager;
import com.amazon.android.ui.fragments.AlertDialogFragment;
import com.amazon.android.ui.fragments.LogoutSettingsFragment;
import com.amazon.android.ui.fragments.NoticeSettingsFragment;
import com.amazon.android.ui.fragments.SlideShowSettingFragment;
import com.amazon.android.utils.ErrorUtils;
import com.amazon.android.utils.LeanbackHelpers;
import com.amazon.android.utils.Preferences;
import com.amazon.utils.DateAndTimeHelper;
import com.amazon.utils.StringManipulation;

import org.greenrobot.eventbus.EventBus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.widget.SparseArrayObjectAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.amazon.android.contentbrowser.helper.LauncherIntegrationManager
        .getSourceOfContentPlayRequest;

/**
 * This class is the controller of the content browsing solution.
 */
public class ContentBrowser implements IContentBrowser, ICancellableLoad {

    /**
     * Debug TAG.
     */
    private static final String TAG = ContentBrowser.class.getSimpleName();

    /**
     * Debug recipe chain flag.
     */
    private static final boolean DEBUG_RECIPE_CHAIN = false;

    /**
     * Cause a feed error flag for debugging.
     */
    private static final boolean CAUSE_A_FEED_ERROR_FOR_DEBUGGING = false;

    /**
     * Request from launcher boolean key
     */
    private static final String REQUEST_FROM_LAUNCHER = "REQUEST_FROM_LAUNCHER";

    /**
     * Content will update key.
     */
    public static final String CONTENT_WILL_UPDATE = "CONTENT_WILL_UPDATE";

    /**
     * The splash screen name.
     */
    public static final String CONTENT_SPLASH_SCREEN = "CONTENT_SPLASH_SCREEN";

    /**
     * The login screen name.
     */
    public static final String CONTENT_LOGIN_SCREEN = "CONTENT_LOGIN_SCREEN";

    /**
     * The home screen name.
     */
    public static final String CONTENT_HOME_SCREEN = "CONTENT_HOME_SCREEN";

    /**
     * The search screen name.
     */
    public static final String CONTENT_SEARCH_SCREEN = "CONTENT_SEARCH_SCREEN";

    /**
     * The details screen name.
     */
    public static final String CONTENT_DETAILS_SCREEN = "CONTENT_DETAILS_SCREEN";

    /**
     * The submenu screen name.
     */
    public static final String CONTENT_SUBMENU_SCREEN = "CONTENT_SUBMENU_SCREEN";

    /**
     * The recommended content screen name.
     */
    public static final String CONTENT_RECOMMENDED_SCREEN = "CONTENT_RECOMMENDED_SCREEN";

    /**
     * The content renderer screen name.
     */
    public static final String CONTENT_RENDERER_SCREEN = "CONTENT_RENDERER_SCREEN";

    /**
     * The connectivity screen name.
     */
    public static final String CONTENT_CONNECTIVITY_SCREEN = "CONTENT_CONNECTIVITY_SCREEN";

    /**
     * The slide show screen name.
     */
    public static final String CONTENT_SLIDESHOW_SCREEN = "CONTENT_SLIDESHOW_SCREEN";

    /**
     * Free content constant.
     */
    public static final String FREE_CONTENT = "free";

    /**
     * Search constant.
     */
    public static final String SEARCH = "Search";

    /**
     * Slide show constant.
     */
    public static final String SLIDE_SHOW = "SlideShow";

    /**
     * Login constant.
     */
    public static final String LOGIN_LOGOUT = "LoginLogout";

    /**
     * Terms constant.
     */
    public static final String TERMS = "Terms";

    /**
     * Slide show setting constant.
     */
    public static final String SLIDESHOW_SETTING = "SlideShowSetting";

    /**
     * Constant for the "watch now" action.
     */
    public static final int CONTENT_ACTION_WATCH_NOW = 1;

    /**
     * Constant for the "watch from beginning" action.
     */
    public static final int CONTENT_ACTION_WATCH_FROM_BEGINNING = 2;

    /**
     * Constant for the "resume playback" action.
     */
    public static final int CONTENT_ACTION_RESUME = 3;

    /**
     * Constant for the "watch later" action.
     */
    public static final int CONTENT_ACTION_WATCH_LATER = 4;

    /**
     * Constant for the "purchase subscription" action.
     */
    public static final int CONTENT_ACTION_SUBSCRIPTION = 5;

    /**
     * Constant for the "purchase daily pass" action.
     */
    public static final int CONTENT_ACTION_DAILY_PASS = 6;

    /**
     * Constant for the "trial" action.
     */
    public static final int CONTENT_ACTION_TRIAL = 7;

    /**
     * Constant for the "buy" action.
     */
    public static final int CONTENT_ACTION_BUY = 8;

    /**
     * Constant for the "rent" action.
     */
    public static final int CONTENT_ACTION_RENT = 9;

    /**
     * Constant for the "search" action.
     */
    public static final int CONTENT_ACTION_SEARCH = 10;

    /**
     * Constant for the "login" action.
     */
    public static final int CONTENT_ACTION_LOGIN_LOGOUT = 11;

    /**
     * Constant for the "slide show" action.
     */
    public static final int CONTENT_ACTION_SLIDESHOW = 12;

    /**
     * Constant for the "launcher" action.
     */
    public static final int CONTENT_ACTION_CALL_FROM_LAUNCHER = 13;

    /**
     * Constant for the "add to watchlist" action.
     */
    public static final int CONTENT_ACTION_ADD_WATCHLIST = 14;

    /**
     * Constant for the "remove from watchlist" action.
     */
    public static final int CONTENT_ACTION_REMOVE_WATCHLIST = 15;

    /**
     * The maximum number of actions supported.
     */
    public static final int CONTENT_ACTION_MAX = 100;

    /**
     * Search algorithm name.
     */
    private static final String DEFAULT_SEARCH_ALGO_NAME = "basic";

    /**
     * Content reload timeout in seconds.
     */
    private static final int CONTENT_RELOAD_TIMEOUT = 14400; // Equals 4 hours.

    /**
     * Constant for grace time in milliseconds
     */
    public static final long GRACE_TIME_MS = 5000; // 5 seconds.

    /**
     * Constant to add to intent extras to inform content browser to restore from the last activity.
     */
    public static final String RESTORE_ACTIVITY = "restore_last_activity";

    /**
     * Application context.
     */
    private final Context mAppContext;

    /**
     * Singleton instance.
     */
    private static ContentBrowser sInstance;

    /**
     * Lock object for singleton get instance.
     */
    private static final Object sLock = new Object();

    /**
     * Event bus reference.
     */
    private final EventBus mEventBus = EventBus.getDefault();

    /**
     * Search manager instance.
     */
    private final SearchManager<ContentContainer, Content> mSearchManager = new SearchManager<>();

    /**
     * Custom search handler reference.
     */
    private ICustomSearchHandler mICustomSearchHandler;

    /**
     * Root content container listener.
     */
    private IRootContentContainerListener mIRootContentContainerListener;

    /**
     * Last selected content.
     */
    private Content mLastSelectedContent;

    /**
     * Last selected content container.
     */
    private ContentContainer mLastSelectedContentContainer;

    /**
     * Navigator instance.
     */
    private final Navigator mNavigator;

    /**
     * Actions list.
     */
    private final List<Action> mWidgetActionsList = new ArrayList<>();

    /**
     * Global content action list.
     */
    private final List<Action> mGlobalContentActionList = new ArrayList<>();

    /**
     * Content action listener.
     */
    private final Map<Integer, List<IContentActionListener>> mContentActionListeners = new
            HashMap<>();

    /**
     * Settings actions list.
     */
    private final List<Action> mSettingsActions = new ArrayList<>();

    /**
     * Powered by logo map.
     */
    private final Map<String, String> mPoweredByLogoUrlMap = new HashMap<>();

    /**
     * Auth helper instance.
     */
    private AuthHelper mAuthHelper;

    /**
     * Purchase helper instance.
     */
    private PurchaseHelper mPurchaseHelper;

    /**
     * LauncherIntegrationManager instance.
     */
    private LauncherIntegrationManager mLauncherIntegrationManager;

    /**
     * Flag for whether or not the user is subscribed.
     */
    private boolean mSubscribed = false;

    /**
     * Flag for whether or not in-app purchasing is disabled.
     */
    private boolean mIAPDisabled = false;

    /**
     * When set to true, this flag will override the subscription flag for all the content.
     */
    private boolean mOverrideAllContentsSubscriptionFlag = false;

    /**
     * Composite subscription instance; single use only!!!.
     */
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    /**
     * boolean to read launcher integration status.
     */
    private final boolean mLauncherIntegrationEnabled;

    /**
     * loginLogout action, content browser needs to keep the state updated of this action.
     */
    private Action mLoginAction;

    /**
     * Boolean indicating if {@link #onAllModulesLoaded()} has been called.
     */
    private boolean mModulesLoaded = false;

    /**
     * Content loader instance.
     */
    private ContentLoader mContentLoader;

    /**
     * Recommendation manager instance.
     */
    private RecommendationManager mRecommendationManager;
    
    /**
     * Returns AuthHelper instance.
     *
     * @return authHelper instance
     */
    public AuthHelper getAuthHelper() {

        return mAuthHelper;
    }

    /**
     * Returns if the loading request is cancelled or not.
     * For this class it will never be cancelled.
     *
     * @return True if loading is cancelled
     */
    public boolean isLoadingCancelled() {

        return false;
    }

    /**
     * Sets up the login action. The login action is only added to the settings row if a screen
     * requires verification to access as noted in the Navigator.json configuration file.
     */
    private void setupLogoutAction() {

        mLoginAction = createLogoutButtonSettingsAction();

        if (Navigator.isScreenAccessVerificationRequired(mNavigator.getNavigatorModel())) {
            addSettingsAction(mLoginAction);
        }
    }

    /**
     * Get a list of content that belong in the watchlist.
     *
     * @return List of content.
     */
    public List<Content> getWatchlistContent() {

        List<Content> contentList = new ArrayList<>();
        
        WatchlistDatabaseHelper databaseHelper = WatchlistDatabaseHelper.getInstance();
        if (databaseHelper != null) {


            List<String> contendIds = databaseHelper.getWatchlistContentIds(mAppContext);

            for (String contentId : contendIds) {

                Content content = mContentLoader.getRootContentContainer()
                                                .findContentById(contentId);
                if (content != null) {
                    contentList.add(content);
                }
                // The content is no longer valid so remove from database.
                else {
                    Log.d(TAG, "Content no longer valid");
                    databaseHelper.deleteRecord(mAppContext, contentId);
                }
            }
        }
        return contentList;
    }

    /**
     * Content action listener interface.
     */
    public interface IContentActionListener {

        /**
         * Called when an action happens on a content.
         *
         * @param activity Activity.
         * @param content  Content.
         * @param actionId Action id.
         */
        void onContentAction(Activity activity, Content content, int actionId);

        /**
         * Called when an action is completed.
         *
         * @param activity Activity.
         * @param content  Content.
         * @param actionId Action id.
         */
        void onContentActionCompleted(Activity activity, Content content, int actionId);
    }

    /**
     * Custom search handler interface.
     */
    public interface ICustomSearchHandler {

        /**
         * On search requested callback.
         *
         * @param query         Query string.
         * @param iSearchResult Search result listener.
         */
        void onSearchRequested(String query, ISearchResult iSearchResult);
    }

    /**
     * Root content container listener.
     */
    public interface IRootContentContainerListener {

        /**
         * Root content container populated callback.
         *
         * @param contentContainer Root content container reference.
         */
        void onRootContentContainerPopulated(ContentContainer contentContainer);
    }

    /**
     * Screen switch listener interface.
     */
    public interface IScreenSwitchListener {

        /**
         * On screen switch callback.
         *
         * @param extra Extra bundle.
         */
        void onScreenSwitch(Bundle extra);
    }

    /**
     * Screen switch Error listener interface.
     */
    public interface IScreenSwitchErrorHandler {

        /**
         * Authentication error callback.
         *
         * @param iScreenSwitchListener Screen switch listener interface implementation.
         */
        void onErrorHandler(IScreenSwitchListener iScreenSwitchListener);
    }

    /**
     * Constructor.
     *
     * @param activity The activity that is active when ContentBrowser is created.
     */
    private ContentBrowser(Activity activity) {

        mAppContext = activity.getApplicationContext();
        mNavigator = new Navigator(activity);
        mSubscribed = Preferences.getBoolean(PurchaseHelper.CONFIG_PURCHASE_VERIFIED);

        mContentLoader = ContentLoader.getInstance(mAppContext);

        mIAPDisabled = mAppContext.getResources().getBoolean(R.bool.is_iap_disabled);

        mLauncherIntegrationEnabled =
                mAppContext.getResources().getBoolean(R.bool.is_launcher_integration_enabled);

        mOverrideAllContentsSubscriptionFlag =
                mAppContext.getResources()
                           .getBoolean(R.bool.override_all_contents_subscription_flag);

        addWidgetsAction(createSearchAction());
        //addWidgetsAction(createSlideShowAction());
        addSettingsAction(createTermsOfUseSettingsAction());
        //addSettingsAction(createSlideShowSettingAction());
        setupLogoutAction();
        
        mSearchManager.addSearchAlgo(DEFAULT_SEARCH_ALGO_NAME, new ISearchAlgo<Content>() {
            @Override
            public boolean onCompare(String query, Content content) {

                return content.searchInFields(query, new String[]{
                        Content.TITLE_FIELD_NAME,
                        Content.DESCRIPTION_FIELD_NAME
                });
            }
        });

        mNavigator.setINavigationListener(new Navigator.INavigationListener() {

            @Override
            public void onSetTheme(Activity activity) {

            }

            @Override
            public void onScreenCreate(Activity activity, String screenName) {

                Log.d(TAG, " onScreenCreate for screen " + screenName + " activity " + activity +
                        " intent " + (activity != null ? activity.getIntent() : null));

                if (!mContentLoader.isContentLoaded() &&
                        (screenName == null || !screenName.equals(CONTENT_SPLASH_SCREEN))) {
                    Log.e(TAG, "Immature app, switching to splash");
                    initFromImmatureApp(activity);
                }
            }

            @Override
            public void onScreenGotFocus(Activity activity, String screenName) {

                Log.d(TAG, "onScreenGotFocus for screen " + screenName + " activity " + activity +
                        " intent " + (activity != null ? activity.getIntent() : null));

                if (screenName.equals(CONTENT_HOME_SCREEN)) {
                    if (mContentLoader.isContentReloadRequired() ||
                            !mContentLoader.isContentLoaded()) {
                        Log.d(TAG, "Are modules loaded? " + mModulesLoaded);
                        if (!mModulesLoaded) {
                            initFromImmatureApp(activity);
                        }
                        else {
                            reloadFeed(activity);
                        }

                    }
                    else if (activity != null &&
                            activity.getIntent().hasExtra(REQUEST_FROM_LAUNCHER) &&
                            activity.getIntent().getBooleanExtra(REQUEST_FROM_LAUNCHER, false)) {

                        activity.getIntent().putExtra(REQUEST_FROM_LAUNCHER, false);
                        switchToRendererScreen(activity.getIntent());
                    }
                    // If we're loading from after an app launch, try to restore the state.
                    else if (shouldRestoreLastActivity(activity)) {
                        activity.getIntent().putExtra(RESTORE_ACTIVITY, false);
                        restoreActivityState(screenName);
                    }
                }
                else if (screenName.equals(CONTENT_SPLASH_SCREEN)) {
                    Log.d(TAG, "runGlobalRecipes due to CONTENT_SPLASH_SCREEN focus");
                }
            }

            @Override
            public void onScreenLostFocus(Activity activity, String screenName) {

                Log.d(TAG, "onScreenLostFocus:" + screenName);
                if (mAuthHelper != null) {
                    mAuthHelper.cancelAllRequests();
                }
            }

            @Override
            public void onApplicationGoesToBackground() {

                Log.d(TAG, "onApplicationGoesToBackground:");
                if (mCompositeSubscription.hasSubscriptions()) {
                    Log.d(TAG, "mCompositeSubscription.unsubscribe");
                    mCompositeSubscription.unsubscribe();
                    // CompositeSubscription is a single use, create a new one for next round.
                    mCompositeSubscription = null;
                    mCompositeSubscription = new CompositeSubscription();
                }
                else {
                    Log.d(TAG, "onApplicationGoesToBackground has no subscriptions!!!");
                }
            }
        });
    }

    /**
     * Restores the last active activity that was saved before the app went to the background or
     * was closed.
     *
     * @param screenName The screen name of the activity to resume.
     */
    private void restoreActivityState(String screenName) {

        String lastActivity = Preferences.getString(com.amazon.android.ui.constants
                                                            .PreferencesConstants.LAST_ACTIVITY);
        String lastContent = Preferences.getString(PreferencesConstants.CONTENT_ID);

        Content content = mContentLoader.getRootContentContainer().findContentById(lastContent);
        Log.d(TAG, "Restoring to last activity: " + lastActivity + " with content: " + lastContent);
        // Switch the last activity if its not the current one.
        if (!StringManipulation.isNullOrEmpty(lastActivity) &&
                !lastActivity.equals(screenName) && content != null) {

            setLastSelectedContent(content);
            switchToScreen(lastActivity, content);
        }
    }

    /**
     * Listener method to listen for authentication updates, it sets the status of
     * loginLogoutAction action used by the browse activities
     *
     * @param authenticationStatusUpdateEvent Event for update in authentication status.
     */
    public void onAuthenticationStatusUpdateEvent(AuthHelper.AuthenticationStatusUpdateEvent
                                                          authenticationStatusUpdateEvent) {

        if (mLoginAction != null) {
            mLoginAction.setState(authenticationStatusUpdateEvent.isUserAuthenticated() ?
                                          LogoutSettingsFragment.TYPE_LOGOUT :
                                          LogoutSettingsFragment.TYPE_LOGIN);
        }

    }

    /**
     * Get instance, singleton method.
     *
     * @param activity The activity.
     * @return Content browser singleton instance.
     */
    public static ContentBrowser getInstance(Activity activity) {

        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new ContentBrowser(activity);
            }
            return sInstance;
        }
    }

    /**
     * Gets called after all modules are loaded.
     */
    public void onAllModulesLoaded() {

        FontManager.configureFonts(mAppContext, this);

        mAuthHelper = new AuthHelper(mAppContext, this);
        mAuthHelper.setupMvpdList();

        // Need to force the auth activity to be removed from the mRxLauncher object.
        // This handles the case of resuming app launch after home button press from
        // second-screen auth activity.
        mAuthHelper.handleOnActivityResult(AuthHelper.AUTH_ON_ACTIVITY_RESULT_REQUEST_CODE, 0,
                                           null);

        mPurchaseHelper = new PurchaseHelper(mAppContext, this);
        // Launcher integration requires the content authorization system initialized before this
        // manager is initialized. Otherwise it will incorrectly set that authorization is not
        // required for content playing. Hence initializing this after initializing AuthHelper
        // and PurchaseHelper.
        if (mLauncherIntegrationEnabled) {
            mLauncherIntegrationManager = new LauncherIntegrationManager(mAppContext, this);
        }

        mRecommendationManager = new RecommendationManager(mAppContext);
        // First reading of the database upon app launch. Created off of main thread.
        mRecommendationManager.cleanDatabase();
        
        // The app successfully loaded its modules so clear out the crash number.
        Preferences.setLong(ModularApplication.APP_CRASHES_KEY, 0);

        mModulesLoaded = true;
    }

    /**
     * Get navigator.
     *
     * @return Navigator.
     */
    public Navigator getNavigator() {

        return mNavigator;
    }

    /**
     * Gets the content loader instance.
     *
     * @return The content loader.
     */
    public ContentLoader getContentLoader() {

        return mContentLoader;
    }

    /**
     * Set root content container listener.
     *
     * @param listener Root content container listener.
     * @return Content browser instance.
     */
    public ContentBrowser setRootContentContainerListener(IRootContentContainerListener listener) {

        mIRootContentContainerListener = listener;
        return this;
    }

    /**
     * Set custom search handler.
     *
     * @param customSearchHandler Custom search handler.
     * @return Content browser instance.
     */
    public ContentBrowser setCustomSearchHandler(ICustomSearchHandler customSearchHandler) {

        mICustomSearchHandler = customSearchHandler;
        return this;
    }

    /**
     * Add action to global action list.
     *
     * @param action Action.
     * @return Content browser instance.
     */
    public ContentBrowser addActionToGlobalContentActionList(Action action) {

        mGlobalContentActionList.add(action);
        return this;
    }

    /**
     * Add content action listener.
     *
     * @param actionId               Action id.
     * @param iContentActionListener Content action listener.
     */
    public void addContentActionListener(int actionId, IContentActionListener
            iContentActionListener) {

        List<IContentActionListener> iContentActionListenersList =
                mContentActionListeners.get(actionId);

        if (iContentActionListenersList == null) {
            iContentActionListenersList = new ArrayList<>();
            mContentActionListeners.put(actionId, iContentActionListenersList);
        }

        iContentActionListenersList.add(iContentActionListener);
    }

    /**
     * Get root content container.
     *
     * @return Root content container.
     */
    public ContentContainer getRootContentContainer() {

        return mContentLoader.getRootContentContainer();
    }

    /**
     * Set last selected content.
     *
     * @param content Content.
     * @return Content browser instance.
     */
    public ContentBrowser setLastSelectedContent(Content content) {

        mLastSelectedContent = content;
        Preferences.setString(PreferencesConstants.CONTENT_ID, content.getId());
        return this;
    }

    /**
     * Get last selected content.
     *
     * @return Last selected content.
     */
    public Content getLastSelectedContent() {

        return mLastSelectedContent;
    }

    /**
     * Set last selected content container.
     *
     * @param contentContainer Last selected content container.
     * @return Content browser instance.
     */
    public ContentBrowser setLastSelectedContentContainer(ContentContainer contentContainer) {

        mLastSelectedContentContainer = contentContainer;
        return this;
    }

    /**
     * Get last selected content container.
     *
     * @return Last selected content container.
     */
    public ContentContainer getLastSelectedContentContainer() {

        return mLastSelectedContentContainer;
    }

    /**
     * Get the path for the light font.
     *
     * @return Light font path.
     */
    public String getLightFontPath() {

        return mNavigator.getNavigatorModel().getBranding().lightFont;
    }

    /**
     * Get the path for the bold font.
     *
     * @return Bold font path.
     */
    public String getBoldFontPath() {

        return mNavigator.getNavigatorModel().getBranding().boldFont;
    }

    /**
     * Get the path for the regular font.
     *
     * @return Regular font path.
     */
    public String getRegularFontPath() {

        return mNavigator.getNavigatorModel().getBranding().regularFont;
    }

    /**
     * Get the flag for showing related content.
     *
     * @return True if related content should be shown; false otherwise.
     */
    public boolean isShowRelatedContent() {

        return mNavigator.getNavigatorModel().getConfig().showRelatedContent;
    }

    /**
     * Get the flag for showing content from the same category if similar tags resulted in no
     * related content.
     *
     * @return True if the category's content should be used as default related content; false
     * otherwise.
     */
    public boolean isUseCategoryAsDefaultRelatedContent() {

        return mNavigator.getNavigatorModel().getConfig().useCategoryAsDefaultRelatedContent;
    }

    /**
     * Get the flag for enabling CEA-608 closed captions
     *
     * @return True if CEA-608 closed captions should be enabled and set as priority;
     * false otherwise
     */
    public boolean isEnableCEA608() {

        return mNavigator.getNavigatorModel().getConfig().enableCEA608;
    }

    /**
     * Get the flag for enabling the recent row on the browse screen.
     *
     * @return True if the recent row should be displayed; false otherwise.
     */
    public boolean isRecentRowEnabled() {

        return mNavigator.getNavigatorModel().getConfig().enableRecentRow;
    }

    /**
     * Get the maximum number of items to be displayed in the recent row on the browse screen.
     *
     * @return The max number of items.
     */
    public int getMaxNumberOfRecentItems() {

        return mNavigator.getNavigatorModel().getConfig().maxNumberOfRecentItems;
    }

    /**
     * Get the flag for enabling the watchlist row on the browse screen.
     *
     * @return True if the watchlist row should be displayed; false otherwise.
     */
    public boolean isWatchlistRowEnabled() {

        return mNavigator.getNavigatorModel().getConfig().enableWatchlistRow;
    }

    /**
     * Get powered by logo url by name.
     *
     * @param name Powered by logo name.
     * @return Powered by logo url.
     */
    public String getPoweredByLogoUrlByName(String name) {

        return mPoweredByLogoUrlMap.get(name);
    }

    /**
     * Add powered by logo url.
     *
     * @param name Powered by logo name.
     * @param url  Powered by logo ur.
     */
    public void addPoweredByLogoUrlByName(String name, String url) {

        mPoweredByLogoUrlMap.put(name, url);
    }

    /**
     * Get settings actions.
     *
     * @return List of settings actions.
     */
    public List<Action> getSettingsActions() {

        return mSettingsActions;
    }

    /**
     * Add settings action.
     *
     * @param settingsAction Settings action.
     */
    private void addSettingsAction(Action settingsAction) {

        mSettingsActions.add(settingsAction);
    }

    /**
     * Get terms of use {@link Action}.
     *
     * @return terms of use action.
     */
    private Action createTermsOfUseSettingsAction() {
        // Create the Terms of Use settings action.
        return new Action().setAction(TERMS).setIconResourceId(R.drawable.ic_terms_text)
                           .setLabel1(mAppContext.getString(R.string.terms_title));
    }

    /**
     * Create loginLogoutAction Action with initial state set as login.
     *
     * @return loginLogoutAction action.
     */
    private Action createLogoutButtonSettingsAction() {
        // Create the logout button settings action.
        return new Action().setAction(LOGIN_LOGOUT)
                           .setId(ContentBrowser.CONTENT_ACTION_LOGIN_LOGOUT)
                           .setLabel1(LogoutSettingsFragment.TYPE_LOGOUT,
                                      mAppContext.getString(R.string.logout_label))
                           .setIconResourceId(LogoutSettingsFragment.TYPE_LOGOUT, R
                                   .drawable.ic_login_logout)
                           .setLabel1(LogoutSettingsFragment.TYPE_LOGIN,
                                      mAppContext.getString(R.string.login_label))
                           .setIconResourceId(LogoutSettingsFragment.TYPE_LOGIN, R
                                   .drawable.ic_login_logout)
                           .setState(LogoutSettingsFragment.TYPE_LOGIN);
    }

    /**
     * Add action to widget action list.
     *
     * @param action The action to add.
     */
    public void addWidgetsAction(Action action) {

        mWidgetActionsList.add(action);
    }

    /**
     * Creates search action.
     *
     * @return The search action.
     */
    private Action createSearchAction() {

        Action search = new Action(CONTENT_ACTION_SEARCH, SEARCH, R.drawable
                .lb_ic_in_app_search);
        search.setId(ContentBrowser.CONTENT_ACTION_SEARCH);
        search.setAction(SEARCH);
        return search;
    }

    /**
     * Creates slide show action.
     *
     * @return The slide show action.
     */
    private Action createSlideShowAction() {

        Action slideShow = new Action(CONTENT_ACTION_SLIDESHOW, SLIDE_SHOW, R.drawable.lb_ic_play);
        slideShow.setId(ContentBrowser.CONTENT_ACTION_SLIDESHOW);
        slideShow.setAction(SLIDE_SHOW);
        return slideShow;
    }

    /**
     * Create slide show setting action.
     *
     * @return The slide show setting action.
     */
    private Action createSlideShowSettingAction() {

        return new Action().setAction(SLIDESHOW_SETTING)
                           .setLabel1(mAppContext.getString(R.string.slideshow_title))
                           .setIconResourceId(R.drawable.ic_terms_text);
    }

    /**
     * This method returns the list of actions that are being used.
     *
     * @return A list of actions used for the action widget adapter.
     */
    public ArrayList<Action> getWidgetActionsList() {

        return (ArrayList<Action>) mWidgetActionsList;
    }

    /**
     * Get recommended list of a content as content container.
     * If there are no items with similar tags, this method returns items from the same category.
     *
     * TODO: DEVTECH-2635
     *
     * @param content Content.
     * @return Recommended contents as a content container.
     */
    public ContentContainer getRecommendedListOfAContentAsAContainer(Content content) {

        ContentContainer recommendedContentContainer =
                new ContentContainer(mAppContext.getString(R.string.recommended_contents_header));

        for (Content c : mContentLoader.getRootContentContainer()) {
            if (content.hasSimilarTags(c) && !StringManipulation.areStringsEqual(c.getId(),
                                                                                 content.getId())) {
                recommendedContentContainer.addContent(c);
            }
        }

        // Use items from the same category as recommended contents
        // if there are no contents with similar tags and the config setting is set to true.
        if (recommendedContentContainer.getContents().isEmpty() &&
                isUseCategoryAsDefaultRelatedContent()) {

            ContentContainer parentContainer = getContainerForContent(content);

            if (parentContainer != null) {

                for (Content relatedContent : parentContainer.getContents()) {
                    if (!StringManipulation.areStringsEqual(content.getId(),
                                                            relatedContent.getId())) {
                        recommendedContentContainer.addContent(relatedContent);
                    }
                }

            }
            else {
                Log.w(TAG, "The content's container could not be found! " + content.toString());
            }
        }
        return recommendedContentContainer;
    }

    /**
     * Get the parent content container for the given content.
     *
     * @param content The content to use to find the container.
     * @return The content container of the given content.
     */
    private ContentContainer getContainerForContent(Content content) {

        // Container that contains the current content
        ContentContainer parentContainer = null;

        // Stack of all content containers from root container.
        Stack<ContentContainer> contentContainerStack = new Stack<>();

        contentContainerStack.push(mContentLoader.getRootContentContainer());

        while (!contentContainerStack.isEmpty()) {
            // Get a sub container.
            ContentContainer contentContainer = contentContainerStack.pop();

            for (Content c : contentContainer.getContents()) {
                if (StringManipulation.areStringsEqual(c.getId(), content.getId())) {
                    parentContainer = contentContainer;
                }
            }

            // Add all the sub containers.
            if (contentContainer.hasSubContainers()) {
                for (ContentContainer cc : contentContainer.getContentContainers()) {
                    contentContainerStack.push(cc);
                }
            }
        }
        return parentContainer;
    }

    /**
     * Search content.
     *
     * @param query         Query string.
     * @param iSearchResult Search result listener.
     */
    public void search(String query, ISearchResult iSearchResult) {

        if (mICustomSearchHandler != null) {
            mICustomSearchHandler.onSearchRequested(query, iSearchResult);
        }
        else {
            mSearchManager.syncSearch(DEFAULT_SEARCH_ALGO_NAME,
                                      query,
                                      iSearchResult,
                                      mContentLoader.getRootContentContainer());
        }
    }

    /**
     * Setting action selected.
     *
     * @param activity       Activity.
     * @param settingsAction Selected setting action.
     */
    public void settingsActionTriggered(Activity activity, Action settingsAction) {

        Log.d(TAG, "settingsActionTriggered Selected");

        switch (settingsAction.getAction()) {
            case LOGIN_LOGOUT:
                loginLogoutActionTriggered(activity, settingsAction);
                break;
            case TERMS:
                new NoticeSettingsFragment()
                        .createFragment(activity,
                                        activity.getFragmentManager(),
                                        settingsAction);
                break;
            case SLIDESHOW_SETTING:
                slideShowSettingActionTriggered(activity, settingsAction);
                break;
            default:
                Log.e(TAG, "Unsupported action " + settingsAction);
                break;
        }
    }

    /**
     * Method to trigger the SlideShowSettingFragment on clicking slideshowSetting Action.
     *
     * @param activity       The activity on which fragment needs to be added.
     * @param settingsAction The action instance.
     */
    private void slideShowSettingActionTriggered(Activity activity, Action settingsAction) {

        new SlideShowSettingFragment().createFragment(activity,
                                                      activity.getFragmentManager(),
                                                      settingsAction);
    }

    /**
     * Method to trigger the LogoutSettingsFragment on clicking loginLogout Action.
     *
     * @param activity       The activity on which fragment needs to be added.
     * @param settingsAction The action instance.
     */
    private void loginLogoutActionTriggered(Activity activity, Action settingsAction) {

        mAuthHelper
                .isAuthenticated()
                .subscribe(isAuthenticatedResultBundle -> {
                    if (isAuthenticatedResultBundle.getBoolean(AuthHelper.RESULT)) {
                        settingsAction.setState(LogoutSettingsFragment.TYPE_LOGOUT);
                        new LogoutSettingsFragment()
                                .createFragment(activity,
                                                activity.getFragmentManager(),
                                                settingsAction);
                    }
                    else {
                        settingsAction.setState(LogoutSettingsFragment.TYPE_LOGIN);
                        mAuthHelper.authenticateWithActivity().subscribe(resultBundle -> {
                            if (resultBundle != null &&
                                    !resultBundle.getBoolean(AuthHelper.RESULT)) {
                                getNavigator().runOnUpcomingActivity(() -> mAuthHelper
                                        .handleErrorBundle(resultBundle));
                            }
                        });
                    }
                });
    }

    /**
     * Get content action list.
     *
     * @param content Content.
     * @return List of action for provided content.
     */
    public List<Action> getContentActionList(Content content) {

        List<Action> contentActionList = new ArrayList<>();

        boolean isSubscriptionNotRequired = !content.isSubscriptionRequired();
        if (isSubscriptionNotRequired && mOverrideAllContentsSubscriptionFlag) {
            isSubscriptionNotRequired = false;
        }

        if (mSubscribed || isSubscriptionNotRequired || mIAPDisabled) {

            // Check if the content is meant for live watching. Live content requires only a
            // watch now button.
            boolean liveContent = content.getExtraValue(Recipe.LIVE_FEED_TAG) != null &&
                    Boolean.valueOf(content.getExtraValue(Recipe.LIVE_FEED_TAG).toString());

            // Check database for stored playback position of content.
            if (!liveContent) {
                RecentRecord record = getRecentRecord(content);

                // Add "Resume" button if content playback is not complete.
                if (record != null && !record.isPlaybackComplete()) {
                    contentActionList.add(createActionButton(CONTENT_ACTION_RESUME,
                                                             R.string.resume_1,
                                                             R.string.resume_2));
                    // Add "Watch From Beginning" button to start content over.
                    contentActionList.add(createActionButton(CONTENT_ACTION_WATCH_FROM_BEGINNING,
                                                             R.string.watch_from_beginning_1,
                                                             R.string.watch_from_beginning_2));
                }
                // If the content has not been played yet, add the "Watch Now" button.
                else {
                    contentActionList.add(createActionButton(CONTENT_ACTION_WATCH_NOW,
                                                             R.string.watch_now_1,
                                                             R.string.watch_now_2));
                }
                if (isWatchlistRowEnabled()) {
                    addWatchlistAction(contentActionList, content.getId());
                }
            }
            else {
                contentActionList.add(createActionButton(CONTENT_ACTION_WATCH_NOW,
                                                         R.string.watch_now_1,
                                                         R.string.watch_now_2));
            }
        }
        else {
            contentActionList.add(createActionButton(CONTENT_ACTION_SUBSCRIPTION,
                                                     R.string.premium_1,
                                                     R.string.premium_2));

            contentActionList.add(createActionButton(CONTENT_ACTION_DAILY_PASS,
                                                     R.string.daily_pass_1,
                                                     R.string.daily_pass_2));
        }

        contentActionList.addAll(mGlobalContentActionList);

        return contentActionList;
    }

    /**
     * Create an action button.
     *
     * @param contentActionId The content action id.
     * @param stringId1       The id of the string to be displayed on the first line of text.
     * @param stringId2       The id of the string to be displayed on the second line of text.
     * @return The action.
     */
    private Action createActionButton(int contentActionId, int stringId1, int stringId2) {

        return new Action().setId(contentActionId)
                           .setLabel1(mAppContext.getResources().getString(stringId1))
                           .setLabel2(mAppContext.getResources().getString(stringId2));
    }

    /**
     * Adds the "Add to Watchlist" action if the content is not in the watchlist. If the content
     * is in the watchlist, the "Remove from Watchlist" action will be added instead.
     *
     * @param contentActionList The list of content actions.
     * @param id                The content id.
     */
    private void addWatchlistAction(List<Action> contentActionList, String id) {

        // If the content is already in the watchlist, add a remove button.
        if (isContentInWatchlist(id)) {
            contentActionList.add(createActionButton(CONTENT_ACTION_REMOVE_WATCHLIST,
                                                     R.string.watchlist_2,
                                                     R.string.watchlist_3));
        }
        // Add the add to watchlist button.
        else {
            contentActionList.add(createActionButton(CONTENT_ACTION_ADD_WATCHLIST,
                                                     R.string.watchlist_1,
                                                     R.string.watchlist_3));
        }
    }

    /**
     * Tests whether or not the given content id is in the watchlist.
     *
     * @param id The content id.
     * @return True if the watchlist contains the content; false otherwise.
     */
    private boolean isContentInWatchlist(String id) {
        
        WatchlistDatabaseHelper databaseHelper = WatchlistDatabaseHelper.getInstance();
        if (databaseHelper != null) {
            return databaseHelper.recordExists(mAppContext, id);
        }
        Log.e(TAG, "Unable to load content because database is null");
        return false;
    }

    /**
     * The action for when the watchlist button is clicked.
     *
     * @param contentId     The content id.
     * @param addContent    True if the content should be added to the watchlist, false if it
     *                      shouldn't.
     * @param actionAdapter The action adapter.
     */
    private void watchlistButtonClicked(String contentId, boolean addContent,
                                        SparseArrayObjectAdapter actionAdapter) {
    
        WatchlistDatabaseHelper databaseHelper = WatchlistDatabaseHelper.getInstance();
    
        if (databaseHelper != null) {
            if (addContent) {
                databaseHelper.addRecord(mAppContext, contentId);
            }
            else {
                databaseHelper.deleteRecord(mAppContext, contentId);
            }
        }
        else {
            Log.e(TAG, "Unable to perform watchlist button action because database is null");
        }
        toggleWatchlistButton(addContent, actionAdapter);
    }

    /**
     * Get content time remaining
     *
     * @param content Content.
     * @return Time remaining in ms.
     */
    public long getContentTimeRemaining(Content content) {

        RecentRecord record = getRecentRecord(content);
        if (record != null && !record.isPlaybackComplete()) {

            // Calculate time remaining as duration minus playback location
            long duration = record.getDuration();
            long currentPlaybackPosition = record.getPlaybackLocation();

            if ((duration > 0) && (currentPlaybackPosition > 0)
                    && (duration > currentPlaybackPosition)) {
                return (duration - currentPlaybackPosition);
            }
        }

        return 0;
    }

    /**
     * Get content playback position percentage for progress bar.
     *
     * @param content Content.
     * @return Percentage playback complete.
     */
    public double getContentPlaybackPositionPercentage(Content content) {

        RecentRecord record = getRecentRecord(content);
        // Calculate the playback position percentage as the current playback position
        // over the entire video duration
        if (record != null && !record.isPlaybackComplete()) {

            // Calculate time remaining as duration minus playback location
            long duration = record.getDuration();
            long currentPlaybackPosition = record.getPlaybackLocation();

            if ((duration > 0) && (currentPlaybackPosition > 0)
                    && (duration > currentPlaybackPosition)) {
                return (((double) currentPlaybackPosition) / duration);
            }
        }

        return 0;
    }

    /**
     * Get Recent Record from database based on content id
     *
     * @param content Content.
     * @return Recent Record.
     */
    public RecentRecord getRecentRecord(Content content) {

        RecentRecord record = null;
        RecentDatabaseHelper databaseHelper = RecentDatabaseHelper.getInstance();
        if (databaseHelper != null) {
            if (databaseHelper.recordExists(mAppContext, content.getId())) {
                record = databaseHelper.getRecord(mAppContext, content.getId());
            }
        }
        else {
            Log.e(TAG, "Unable to load content because database is null");
        }

        return record;
    }

    /**
     * Get a list of contents to display in the "Continue Watching" row that have been watched for
     * more than the grace period value located in the custom.xml as recent_grace_period.
     *
     * @return A list of contents.
     */
    public List<Content> getRecentContent() {

        List<Content> contentList = new ArrayList<>();
        RecentDatabaseHelper databaseHelper = RecentDatabaseHelper.getInstance();
        if (databaseHelper != null) {
            List<RecentRecord> records = databaseHelper.getUnfinishedRecords(mAppContext,
                    mAppContext.getResources().getInteger(R.integer.recent_grace_period));

            for (RecentRecord record : records) {
                Content content = mContentLoader.getRootContentContainer()
                                                .findContentById(record.getContentId());
                if (content != null) {
                    contentList.add(content);
                }
            }
        }

        return contentList;
    }

    /**
     * Set subscribed flag.
     *
     * @param flag Subscribed flag.
     */
    public void setSubscribed(boolean flag) {

        mSubscribed = flag;
    }

    /**
     * Update content actions.
     */
    public void updateContentActions() {

        mEventBus.post(new ActionUpdateEvent(true));
    }

    /**
     * Handle on activity result.
     *
     * @param activity    Activity.
     * @param requestCode Request code.
     * @param resultCode  Result code.
     * @param data        Intent.
     */
    public void handleOnActivityResult(Activity activity, int requestCode, int resultCode,
                                       Intent data) {

        Log.d(TAG, "handleOnActivityResult " + requestCode);

        switch (requestCode) {
            case AuthHelper.AUTH_ON_ACTIVITY_RESULT_REQUEST_CODE:
                mAuthHelper.handleOnActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    /**
     * show authentication Error Dialog
     *
     * @param iScreenSwitchListener Screen switch listener.
     */
    public void showAuthenticationErrorDialog(IScreenSwitchListener iScreenSwitchListener) {

        AlertDialogFragment.createAndShowAlertDialogFragment(
                mNavigator.getActiveActivity(),
                mAppContext.getString(R.string.optional_login_dialog_title),
                mAppContext.getString(R.string.optional_login_dialog_message),
                mAppContext.getString(R.string.now),
                mAppContext.getString(R.string.later),
                new AlertDialogFragment.IAlertDialogListener() {

                    @Override
                    public void onDialogPositiveButton(
                            AlertDialogFragment alertDialogFragment) {

                        mAuthHelper.handleAuthChain(
                                iScreenSwitchListener::onScreenSwitch);
                    }

                    @Override
                    public void onDialogNegativeButton
                            (AlertDialogFragment alertDialogFragment) {

                        Preferences.setBoolean(
                                AuthHelper.LOGIN_LATER_PREFERENCES_KEY, true);
                        iScreenSwitchListener.onScreenSwitch(null);
                    }
                });
    }

    /**
     * Verify screen switch.
     *
     * @param screenName                Screen name
     * @param iScreenSwitchListener     Screen switch listener.
     * @param iScreenSwitchErrorHandler Screen switch error handler
     */
    public void verifyScreenSwitch(String screenName,
                                   IScreenSwitchListener iScreenSwitchListener,
                                   IScreenSwitchErrorHandler iScreenSwitchErrorHandler) {

        verifyScreenSwitch(screenName, (Content) null, iScreenSwitchListener,
                           iScreenSwitchErrorHandler);
    }

    /**
     * Verify screen switch with given content.
     *
     * @param screenName                Screen name
     * @param content                   Content
     * @param iScreenSwitchListener     Screen switch listener.
     * @param iScreenSwitchErrorHandler Screen switch error handler
     */
    public void verifyScreenSwitch(String screenName, Content content,
                                   IScreenSwitchListener iScreenSwitchListener,
                                   IScreenSwitchErrorHandler iScreenSwitchErrorHandler) {

        UINode uiNode = (UINode) mNavigator.getNodeObjectByScreenName(screenName);
        // Check if the content is meant for free watching. Free content doesn't need the
        // authentication.
        boolean freeContent = content != null && content.getExtraValue(Recipe.CONTENT_TYPE_TAG)
                != null && (content.getExtraValue(Recipe.CONTENT_TYPE_TAG).toString().equals
                (FREE_CONTENT));

        Log.d(TAG, "verifyScreenSwitch called in:" + screenName);
        Log.d(TAG, "isVerifyScreenAccess needed:" + uiNode.isVerifyScreenAccess() + " and is free" +
                " content:" + freeContent);

        if (uiNode.isVerifyScreenAccess() && !freeContent) {

            if (!mAuthHelper.getIAuthentication().isAuthenticationCanBeDoneLater()) {
                mAuthHelper.handleAuthChain(iScreenSwitchListener::onScreenSwitch);
            }
            else {
                boolean loginLater = Preferences.getBoolean(AuthHelper.LOGIN_LATER_PREFERENCES_KEY);
                if (!loginLater && mAuthHelper.getIAuthentication()
                                              .isAuthenticationCanBeDoneLater()) {

                    mAuthHelper.isAuthenticated().subscribe(extras -> {
                        if (extras.getBoolean(AuthHelper.RESULT)) {
                            mAuthHelper.handleAuthChain(
                                    iScreenSwitchListener::onScreenSwitch, extras);
                        }
                        else {
                            iScreenSwitchErrorHandler.onErrorHandler(iScreenSwitchListener);
                        }
                    });
                }
                else {
                    iScreenSwitchListener.onScreenSwitch(null);
                }
            }
        }
        else {
            iScreenSwitchListener.onScreenSwitch(null);
        }
    }

    /**
     * Switch to screen by name.
     *
     * @param screenName Screen name.
     */
    public void switchToScreen(String screenName) {

        switchToScreen(screenName, (Navigator.ActivitySwitchListener) null);
    }

    /**
     * Switch to screen by name with listener.
     *
     * @param screenName             Screen name.
     * @param activitySwitchListener Activity switch listener.
     */
    public void switchToScreen(String screenName, Navigator.ActivitySwitchListener
            activitySwitchListener) {

        verifyScreenSwitch(screenName, extra ->
                                   mNavigator.startActivity(screenName, activitySwitchListener),
                           errorExtra -> showAuthenticationErrorDialog(errorExtra)
        );
    }

    /**
     * Switch to screen by name with bundle.
     *
     * @param screenName Screen name.
     * @param bundle     Bundle.
     */
    public void switchToScreen(String screenName, Bundle bundle) {

        verifyScreenSwitch(screenName, extra ->
                                   mNavigator.startActivity(screenName, bundle),
                           errorExtra -> showAuthenticationErrorDialog(errorExtra)
        );
    }

    /**
     * Switch to screen by name with bundle for given content.
     *
     * @param screenName Screen name.
     * @param content    Content.
     * @param bundle     Bundle.
     */
    public void switchToScreen(String screenName, Content content, Bundle bundle) {

        verifyScreenSwitch(screenName, content, extra ->
                                   mNavigator.startActivity(screenName, bundle),
                           errorExtra -> showAuthenticationErrorDialog(errorExtra)
        );
    }

    /**
     * Switch to screen by name for given content.
     *
     * @param screenName Screen name.
     * @param content    Content
     */
    public void switchToScreen(String screenName, Content content) {

        switchToScreen(screenName, content, (Navigator.ActivitySwitchListener) null);
    }

    /**
     * Switch to screen by name with listener for given content.
     *
     * @param screenName             Screen name.
     * @param content                Content
     * @param activitySwitchListener Activity switch listener.
     */
    public void switchToScreen(String screenName, Content content, Navigator.ActivitySwitchListener
            activitySwitchListener) {

        verifyScreenSwitch(screenName, content, extra ->
                                   mNavigator.startActivity(screenName, activitySwitchListener),
                           errorExtra -> showAuthenticationErrorDialog(errorExtra)
        );
    }

    /**
     * Action triggered.
     *
     * @param activity Activity.
     * @param action   Action.
     */
    public void actionTriggered(Activity activity, Action action) {

        actionTriggered(activity, action, null);
    }

    /**
     * Action triggered.
     *
     * @param activity Activity.
     * @param action   Action.
     * @param extras   Extras bundle.
     */
    public void actionTriggered(Activity activity, Action action, Bundle extras) {

        switch ((int) action.getId()) {
            case ContentBrowser.CONTENT_ACTION_SEARCH: {
                Log.d(TAG, "actionTriggered -> CONTENT_ACTION_SEARCH");
                switchToScreen(CONTENT_SEARCH_SCREEN);
            }
            break;
            case ContentBrowser.CONTENT_ACTION_SLIDESHOW: {
                Log.d(TAG, "actionTriggered -> CONTENT_ACTION_SLIDESHOW");
                switchToScreen(CONTENT_SLIDESHOW_SCREEN);
            }
            break;
            case ContentBrowser.CONTENT_ACTION_LOGIN_LOGOUT: {
                Log.d(TAG, "actionTriggered -> CONTENT_ACTION_LOGIN_LOGOUT");
                loginLogoutActionTriggered(activity, action);
            }
            break;
        }
    }

    /**
     * Switch to renderer screen.
     *
     * @param content  Content.
     * @param actionId Action id.
     */
    public void switchToRendererScreen(Content content, int actionId) {

        switchToScreen(ContentBrowser.CONTENT_RENDERER_SCREEN, content, intent -> {
            intent.putExtra(Content.class.getSimpleName(), content);

            // Reset saved seek position if watching content from beginning.
            if (actionId == CONTENT_ACTION_WATCH_FROM_BEGINNING) {
                RecentDatabaseHelper databaseHelper = RecentDatabaseHelper.getInstance();
                if (databaseHelper == null) {
                    Log.e(TAG, "Error retrieving database. Recent not saved.");
                    return;
                }
                databaseHelper.addRecord(mAppContext, content.getId(), 0, false,
                                         DateAndTimeHelper.getCurrentDate().getTime(),
                                         content.getDuration());
            }
        });
    }

    /**
     * Switch to renderer screen.
     *
     * @param inputIntent Input intent for launching renderer screen.
     */
    public void switchToRendererScreen(Intent inputIntent) {

        switchToScreen(ContentBrowser.CONTENT_RENDERER_SCREEN, (Content) inputIntent
                .getSerializableExtra(Content.class.getSimpleName()), intent -> {
            intent.putExtras(inputIntent.getExtras());
        });
    }

    /**
     * Handle renderer screen switch.
     *
     * @param activity Activity.
     * @param content  Content.
     * @param actionId Action id.
     */
    public void handleRendererScreenSwitch(Activity activity, Content content, int actionId,
                                           boolean showErrorDialog) {

        if (mIAPDisabled) {
            switchToRendererScreen(content, actionId);
        }
        else {
            Log.d(TAG, "validating purchase while handleRendererScreenSwitch");
            mPurchaseHelper
                    .isSubscriptionValidObservable()
                    .subscribe(resultBundle -> {
                        if (resultBundle.getBoolean(PurchaseHelper.RESULT) &&
                                resultBundle.getBoolean(PurchaseHelper.RESULT_VALIDITY)) {
                            // Switch to renderer screen.
                            switchToRendererScreen(content, actionId);
                        }
                        else if (resultBundle.getBoolean(PurchaseHelper.RESULT) &&
                                !resultBundle.getBoolean(PurchaseHelper.RESULT_VALIDITY)) {

                            if (showErrorDialog) {
                                AlertDialogFragment.createAndShowAlertDialogFragment(
                                        mNavigator.getActiveActivity(),
                                        mAppContext.getString(R.string.iap_error_dialog_title),
                                        mAppContext.getString(R.string.subscription_expired),
                                        null,
                                        mAppContext.getString(R.string.ok),
                                        new AlertDialogFragment.IAlertDialogListener() {

                                            @Override
                                            public void onDialogPositiveButton
                                                    (AlertDialogFragment alertDialogFragment) {

                                            }

                                            @Override
                                            public void onDialogNegativeButton
                                                    (AlertDialogFragment alertDialogFragment) {

                                                alertDialogFragment.dismiss();
                                            }
                                        });
                            }
                            else {
                                Log.e(TAG, "Purchase expired while handleRendererScreenSwitch");
                                ContentBrowser.getInstance(activity).setLastSelectedContent(content)
                                              .switchToScreen(ContentBrowser
                                                                      .CONTENT_DETAILS_SCREEN,
                                                              content);
                            }
                            updateContentActions();
                        }
                        else {
                            // IAP errors are handled by IAP sdk.
                            Log.e(TAG, "IAP error!!!");
                        }
                    }, throwable -> {
                        // IAP errors are handled by IAP sdk.
                        Log.e(TAG, "IAP error!!!", throwable);
                    });
        }
    }

    /**
     * Action triggered.
     *
     * @param activity                Activity.
     * @param content                 Content.
     * @param actionId                Action id.
     * @param actionAdapter           The adapter that holds the actions.
     * @param actionCompletedListener Optional parameter that will be called
     *                                after the action is completed.
     */
    public void actionTriggered(Activity activity, Content content, int actionId,
                                SparseArrayObjectAdapter actionAdapter, IContentActionListener
                                        actionCompletedListener) {

        List<IContentActionListener> iContentActionListenersList =
                mContentActionListeners.get(actionId);

        if (iContentActionListenersList != null && iContentActionListenersList.size() > 0) {
            for (IContentActionListener listener : iContentActionListenersList) {
                listener.onContentAction(activity, content, actionId);
            }
        }

        AnalyticsHelper.trackContentDetailsAction(content, actionId);
        switch (actionId) {
            case CONTENT_ACTION_WATCH_NOW:
            case CONTENT_ACTION_WATCH_FROM_BEGINNING:
            case CONTENT_ACTION_RESUME:
                handleRendererScreenSwitch(activity, content, actionId, true);
                break;
            case CONTENT_ACTION_SUBSCRIPTION:
            case CONTENT_ACTION_DAILY_PASS:
                mPurchaseHelper.handleAction(activity, content, actionId);
                break;
            case CONTENT_ACTION_ADD_WATCHLIST:
                watchlistButtonClicked(content.getId(), true, actionAdapter);
                break;
            case CONTENT_ACTION_REMOVE_WATCHLIST:
                watchlistButtonClicked(content.getId(), false, actionAdapter);
                break;
        }
        if (actionCompletedListener != null) {
            actionCompletedListener.onContentActionCompleted(activity, content, actionId);
        }
    }

    /**
     * Toggles the watch list action button text.
     *
     * @param addToList     True if the text should read "Add to Watchlist"; false if the
     *                      text should read "Remove from Watchlist".
     * @param actionAdapter The array adapter that contains the actions.
     */
    private void toggleWatchlistButton(boolean addToList, SparseArrayObjectAdapter actionAdapter) {

        for (int i = 0; i < actionAdapter.size(); i++) {
            Action action = LeanbackHelpers.translateActionAdapterObjectToAction(actionAdapter
                                                                                         .get(i));
            if (action.getId() == CONTENT_ACTION_ADD_WATCHLIST ||
                    action.getId() == CONTENT_ACTION_REMOVE_WATCHLIST) {

                // Update the button text.
                if (addToList) {
                    action.setLabel1(mAppContext.getResources().getString(R.string.watchlist_2));
                    action.setId(CONTENT_ACTION_REMOVE_WATCHLIST);
                }
                else {
                    action.setLabel1(mAppContext.getResources().getString(R.string.watchlist_1));
                    action.setId(CONTENT_ACTION_ADD_WATCHLIST);
                }
                // Reset the action in the adapter and notify change.
                actionAdapter.set(i, LeanbackHelpers.translateActionToLeanBackAction(action));
                actionAdapter.notifyArrayItemRangeChanged(i, 1);
                break;
            }
        }
    }


    /**
     * Run global recipes.
     */
    public void runGlobalRecipes(Activity activity, ICancellableLoad cancellable) {

        final ContentContainer root = new ContentContainer("Root");
        Subscription subscription =
                Observable.range(0, mNavigator.getNavigatorModel().getGlobalRecipes().size())
                          // Do this first to make sure were running in new thread right a way.
                          .subscribeOn(Schedulers.newThread())
                          .concatMap(index -> mContentLoader.runGlobalRecipeAtIndex(index, root))
                          .onBackpressureBuffer() // This must be right after concatMap.
                          .doOnNext(o -> {
                              if (DEBUG_RECIPE_CHAIN) {
                                  Log.d(TAG, "doOnNext");
                              }
                          })
                          // This should be last so the rest is running on a separate thread.
                          .observeOn(AndroidSchedulers.mainThread())
                          .subscribe(objectPair -> {
                              if (DEBUG_RECIPE_CHAIN) {
                                  Log.d(TAG, "subscriber onNext called");
                              }
                          }, throwable -> {
                              Log.e(TAG, "Recipe chain failed:", throwable);
                              ErrorHelper.injectErrorFragment(
                                      mNavigator.getActiveActivity(),
                                      ErrorUtils.ERROR_CATEGORY.FEED_ERROR,
                                      (errorDialogFragment, errorButtonType,
                                       errorCategory) -> {
                                          if (errorButtonType ==
                                                  ErrorUtils.ERROR_BUTTON_TYPE.EXIT_APP) {
                                              mNavigator.getActiveActivity().finishAffinity();
                                          }
                                      });

                          }, () -> {

                              Log.v(TAG, "Recipe chain completed");
                              // Remove empty sub containers.
                              root.removeEmptySubContainers();

                              mContentLoader.setRootContentContainer(root);
                              if (mIRootContentContainerListener != null) {
                                  mIRootContentContainerListener.onRootContentContainerPopulated
                                          (mContentLoader.getRootContentContainer());
                              }
                              mContentLoader.setContentReloadRequired(false);
                              mContentLoader.setContentLoaded(true);
                              if (cancellable != null && cancellable.isLoadingCancelled()) {
                                  Log.d(TAG, "Content load complete but app has been cancelled, " +
                                          "returning from here");
                                  return;
                              }
                              if (mLauncherIntegrationManager != null && activity != null &&
                                      LauncherIntegrationManager
                                              .isCallFromLauncher(activity.getIntent())) {

                                  Log.d(TAG, "Call from launcher with intent " +
                                          activity.getIntent());
                                  String contentId = null;
                                  try {

                                      contentId = LauncherIntegrationManager
                                              .getContentIdToPlay(mAppContext,
                                                                  activity.getIntent());

                                      Content content =
                                              getRootContentContainer().findContentById(contentId);
                                      if (content == null) {
                                          mRecommendationManager.dismissRecommendation(contentId);
                                          throw new IllegalArgumentException("No content exist " +
                                                                                     "for " +
                                                                                     "contentId "
                                                                                     + contentId);
                                      }
                                      AnalyticsHelper.trackLauncherRequest(contentId, content,
                                                                           getSourceOfContentPlayRequest(activity.getIntent()));
                                      Intent intent = new Intent();
                                      intent.putExtra(Content.class.getSimpleName(), content);
                                      intent.putExtra(REQUEST_FROM_LAUNCHER, true);
                                      intent.putExtra(PreferencesConstants.CONTENT_ID,
                                                      content.getId());
                                      switchToHomeScreen(intent);

                                  }
                                  catch (Exception e) {
                                      Log.e(TAG, e.getLocalizedMessage(), e);
                                      AnalyticsHelper.trackLauncherRequest(contentId, null,
                                                                           getSourceOfContentPlayRequest(activity.getIntent()));
                                      AlertDialogFragment.createAndShowAlertDialogFragment
                                              (mNavigator.getActiveActivity(),
                                               "Error",
                                               "The selected content is no longer available",
                                               null,
                                               mAppContext.getString(R.string.ok),
                                               new AlertDialogFragment.IAlertDialogListener() {

                                                   @Override
                                                   public void onDialogPositiveButton
                                                           (AlertDialogFragment
                                                                    alertDialogFragment) {

                                                   }

                                                   @Override
                                                   public void onDialogNegativeButton
                                                           (AlertDialogFragment
                                                                    alertDialogFragment) {

                                                       alertDialogFragment.dismiss();
                                                       if (cancellable != null &&
                                                               cancellable.isLoadingCancelled()) {
                                                           Log.d(TAG, "switchToHomeScreen after " +
                                                                   "launcher integration " +
                                                                   "exception cancelled");
                                                           return;
                                                       }
                                                       switchToHomeScreen();
                                                   }
                                               });
                                  }
                              }
                              else {
                                  if (cancellable != null &&
                                          cancellable.isLoadingCancelled()) {
                                      Log.d(TAG, "switchToHomeScreen after Splash cancelled");
                                      return;
                                  }

                                  // Send recommendations if authentication is not required, or if
                                  // the user is logged in.
                                  if (!Navigator.isScreenAccessVerificationRequired(
                                          mNavigator.getNavigatorModel()) ||
                                          Preferences.getBoolean(
                                                  LauncherIntegrationManager
                                                          .PREFERENCE_KEY_USER_AUTHENTICATED)) {
                                      mRecommendationManager.cleanDatabase();
                                      mRecommendationManager
                                              .updateGlobalRecommendations(mAppContext);
                                  }
                                  if (shouldRestoreLastActivity(activity)) {
                                      Log.d(TAG, "Ran global recipes from app launch. Will " +
                                              "add intent extra to resume previous activity");
                                      switchToHomeScreen(activity.getIntent());
                                  }
                                  else {
                                      switchToHomeScreen();
                                  }
                              }
                          });

        mCompositeSubscription.add(subscription);
    }

    /**
     * Figures out if we should restore the last activity or not. If the app was opened in the last
     * refresh period (found in resources), it will start from the fresh state instead of restoring.
     * Also, looks at the activity's intent and returns the value for the {@link #RESTORE_ACTIVITY}
     * extra which indicates if we should restore or not.
     *
     * @param activity The activity containing the intent.
     * @return True if we should restore the previous activity; false otherwise.
     */
    private boolean shouldRestoreLastActivity(Activity activity) {

        if (activity != null && activity.getIntent() != null &&
                activity.getIntent().getBooleanExtra(ContentBrowser.RESTORE_ACTIVITY, false)) {

            boolean lessThan24Hours = true;

            long lastTimeMs = Preferences.getLong(com.amazon.android.ui.constants
                                                          .PreferencesConstants.TIME_LAST_SAVED);
            // Check if the app was last opened within the refresh period.
            if (lastTimeMs > 0) {
                long currentTimeMs = DateAndTimeHelper.getCurrentDate().getTime();
                long elapsedTimeMs = (currentTimeMs - lastTimeMs);

                long refreshTimeSec =
                        activity.getResources().getInteger(R.integer.state_refresh_period);
                lessThan24Hours = (elapsedTimeMs > 0 && (elapsedTimeMs / 1000) < refreshTimeSec);
            }
            // If the app was opened within the refresh period and the intent says to restore
            // return true.
            return lessThan24Hours;
        }
        return false;
    }


    /**
     * Switches to home screen.
     *
     * @param inputIntent input intent to be passed to home screen.
     */
    public void switchToHomeScreen(Intent inputIntent) {

        switchToScreen(CONTENT_HOME_SCREEN, intent -> {
            // Make sure we clear activity stack.
            updateIntentToClearActivityStack(intent);
            intent.putExtras(inputIntent.getExtras());
        });
    }

    /**
     * Switches to home screen.
     */
    public void switchToHomeScreen() {

        switchToScreen(CONTENT_HOME_SCREEN, intent -> {
            // Make sure we clear activity stack.
            updateIntentToClearActivityStack(intent);
        });
    }

    /**
     * Add required flags to the intent to clear the activity stack.
     *
     * @param intent Intent to be updated.
     */
    private void updateIntentToClearActivityStack(Intent intent) {

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * Method which returns true if IAP is enabled, false otherwise.
     *
     * @return true if IAP is enabled, false otherwise.
     */
    public boolean isIapDisabled() {

        return mIAPDisabled;
    }

    /**
     * Method which returns true if user authentication is mandatory, false otherwise.
     *
     * @return true if user authentication is mandatory, false otherwise.
     */
    public boolean isUserAuthenticationMandatory() {

        if (mAuthHelper == null || mAuthHelper.getIAuthentication() == null) {
            return false;
        }
        else if (mAuthHelper.getIAuthentication().isAuthenticationCanBeDoneLater()) {
            return false;
        }
        return true;
    }

    /**
     * Get the recommendation manager instance.
     *
     * @return The recommendation manager.
     */
    public RecommendationManager getRecommendationManager() {

        return mRecommendationManager;
    }

    /**
     * Launches the splash activity to properly initialize the app.
     *
     * @param activity The calling activity.
     */
    private void initFromImmatureApp(Activity activity) {

        Log.d(TAG, "init from immature app");
        mNavigator.startActivity(CONTENT_SPLASH_SCREEN, intent -> {
            // Make sure we clear activity stack.
            updateIntentToClearActivityStack(intent);
        });
        if (activity != null) {
            activity.finish();
        }
        mContentLoader.setContentReloadRequired(false);
        mContentLoader.setContentLoaded(false);

    }

    /**
     * Reloads the feed. Launches splash activity to simply display loading text.
     *
     * @param activity The calling activity.
     */
    private void reloadFeed(Activity activity) {

        Log.d(TAG, "Content reload required, switching to splash");
        mNavigator.startActivity(CONTENT_SPLASH_SCREEN, intent -> {
            intent.putExtra(CONTENT_WILL_UPDATE, true);
            // Make sure we clear activity stack.
            updateIntentToClearActivityStack(intent);
        });
        if (activity != null) {
            activity.finish();
        }
        mContentLoader.setContentReloadRequired(false);
        mContentLoader.setContentLoaded(false);

        runGlobalRecipes(activity, ContentBrowser.this);
    }
}
