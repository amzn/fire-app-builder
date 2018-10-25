package com.amazon.analytics.tealium;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.amazon.analytics.AnalyticsTags;
import com.amazon.analytics.CustomAnalyticsTags;
import com.amazon.analytics.IAnalytics;
import com.tealium.library.Tealium;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.tealium.library.DataSources.Key.VIDEO_ID;
import static com.tealium.library.DataSources.Key.VIDEO_LENGTH;
import static com.tealium.library.DataSources.Key.VIDEO_MILESTONE;
import static com.tealium.library.DataSources.Key.VIDEO_NAME;
import static com.tealium.library.DataSources.Key.VIDEO_PLAYHEAD;

public class TealiumAnalytics implements IAnalytics {

    private static final String TAG = TealiumAnalytics.class.getSimpleName();
    private Tealium mTealium;
    private CustomAnalyticsTags mCustomAnalyticsTags = new CustomAnalyticsTags();

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(Context context) {
        Application application = (Application) context.getApplicationContext();
        Tealium.Config config = Tealium.Config.create(application, "<account-name>", "<profile-name>", "<environment>");
        mTealium = Tealium.createInstance("<instance-name>", config);

        Log.d(TAG, "Tealium Analytics initialized");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void collectLifeCycleData(Activity activity, boolean active) {
        Log.d(TAG, "Lifecycle is not supported for this platform.");
    }

    /**
     * {@inheritDoc}
     *
     * @param data Map of Strings to Objects that represent data that is necessary for the tracked
     */
    @Override
    public void trackAction(HashMap<String, Object> data) {
        HashMap<String, Object> contextData = new HashMap<>();

        // Get the action name
        String action = String.valueOf(data.get(AnalyticsTags.ACTION_NAME));
        contextData.put(AnalyticsTags.ACTION_NAME, action);

        // Get the attributes map
        HashMap<String, Object> contextDataObjectMap = (HashMap<String, Object>) data.get(AnalyticsTags.ATTRIBUTES);

        if (action != null && contextDataObjectMap != null) {
            for (String key : contextDataObjectMap.keySet()) {
                String value = String.valueOf(contextDataObjectMap.get(key));
                Long videoDuration;

                switch (key) {
                    case AnalyticsTags.ATTRIBUTE_VIDEO_CURRENT_POSITION:
                        contextData.put(VIDEO_PLAYHEAD, value);
                        break;
                    case AnalyticsTags.ATTRIBUTE_VIDEO_ID:
                        contextData.put(VIDEO_ID, value);
                        break;
                    case AnalyticsTags.ATTRIBUTE_VIDEO_DURATION:
                        videoDuration = (Long) contextDataObjectMap.get(AnalyticsTags.ATTRIBUTE_VIDEO_DURATION);
                        contextData.put(VIDEO_LENGTH, getVideoDuration(videoDuration));
                        break;
                    case AnalyticsTags.ATTRIBUTE_VIDEO_SECONDS_WATCHED:
                        videoDuration = (Long) contextDataObjectMap.get(AnalyticsTags.ATTRIBUTE_VIDEO_DURATION);
                        if (videoDuration != null) {
                            contextData.put(VIDEO_MILESTONE, getMilestone(videoDuration, Long.valueOf(value)));
                        }
                        break;
                    case AnalyticsTags.ATTRIBUTE_TITLE:
                        contextData.put(VIDEO_NAME, value);
                        break;
                    default:
                        contextData.put(key, value);
                        break;
                }
            }

            mTealium.trackEvent(mCustomAnalyticsTags.getCustomTag(action),
                    mCustomAnalyticsTags.getCustomTags(contextData));
            Log.d(TAG, "Track action " + action + "with attributes " + contextData);
        }
    }

    @Override
    public void trackState(String screen) {
        mTealium.trackView(screen, null);
        Log.d(TAG, "Track screen: " + screen);
    }

    @Override
    public void trackCaughtError(String errorMessage, Throwable t) {
        mTealium.trackEvent(errorMessage, null);
        Log.d(TAG, "Tracking caught error: " + errorMessage);
    }

    private String getVideoDuration(Long videoDuration) {
        return String.valueOf(TimeUnit.MILLISECONDS.toSeconds(videoDuration));
    }

    private String getMilestone(Long videoDuration, Long secondsWatched) {
        int percentage = (int) ((double) secondsWatched / videoDuration * 100);
        return String.valueOf(percentage);
    }
}
