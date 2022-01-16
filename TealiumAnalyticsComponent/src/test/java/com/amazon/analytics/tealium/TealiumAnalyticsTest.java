package com.amazon.analytics.tealium;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;

import com.amazon.analytics.AnalyticsTags;
import com.tealium.library.Tealium;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test suite for the {@link TealiumAnalytics} class
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*", "org.json.*"})
@PrepareForTest({com.tealium.library.Tealium.Config.class, com.tealium.library.Tealium.class})
public class TealiumAnalyticsTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Mock
    AssetManager mAssetManager;

    @Mock
    Context mContext;

    @Mock
    Application mApplication;

    @Mock
    Tealium mTealium;

    @Mock
    com.tealium.library.Tealium.Config mConfig;

    private TealiumAnalytics mTealiumAnalytics;

    @Before
    public void setUp() {
        mTealiumAnalytics = new TealiumAnalytics();
        initMocks(this);
        PowerMockito.mockStatic(com.tealium.library.Tealium.Config.class);
        PowerMockito.mockStatic(com.tealium.library.Tealium.class);
        doReturn(mApplication).when(mContext).getApplicationContext();
        when(mContext.getAssets()).thenReturn(mAssetManager);
        when(Tealium.Config.create(any(Application.class), anyString(), anyString(), anyString())).thenReturn(mConfig);
        when(Tealium.createInstance(anyString(), any(Tealium.Config.class))).thenReturn(mTealium);
    }

    @After
    public void tearDown() {
        mTealiumAnalytics = null;
    }

    @Test
    public void configure() {
        mTealiumAnalytics.configure(mContext);

        assertNotNull(mTealiumAnalytics);
    }

    @Test
    public void trackAction() {
        mTealiumAnalytics.configure(mContext);
        final String TEST_ACTION = "testAction";
        HashMap<String, Object> dummyMap = new HashMap<>();
        HashMap contextData = new HashMap<String, Object>();

        dummyMap.put(AnalyticsTags.ACTION_NAME, TEST_ACTION);
        contextData.put(AnalyticsTags.ACTION_NAME, TEST_ACTION);

        contextData.put("key", "value");
        dummyMap.put(AnalyticsTags.ATTRIBUTES, contextData);

        mTealiumAnalytics.trackAction(dummyMap);

        verify(mTealium, times(1)).trackEvent(TEST_ACTION, contextData);
    }

    @Test
    public void trackState() {
        mTealiumAnalytics.configure(mContext);
        String screenName = "screenName";
        mTealiumAnalytics.trackState(screenName);

        verify(mTealium, times(1)).trackView(screenName, null);
    }

    @Test
    public void trackCaughtError() {
        mTealiumAnalytics.configure(mContext);
        mTealiumAnalytics.trackCaughtError("error", new Exception());

        verify(mTealium, times(1)).trackEvent("error", null);
    }

}