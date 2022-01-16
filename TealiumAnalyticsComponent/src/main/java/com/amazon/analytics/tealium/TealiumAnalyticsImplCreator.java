package com.amazon.analytics.tealium;

import com.amazon.analytics.IAnalytics;
import com.amazon.android.module.IImplCreator;

public class TealiumAnalyticsImplCreator implements IImplCreator<IAnalytics> {

    /**
     * {@inheritDoc}
     */
    @Override
    public IAnalytics createImpl() {
        return new TealiumAnalytics();
    }
}
