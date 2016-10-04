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
package com.amazon.android.uamp;

import android.content.Context;

import com.amazon.android.model.content.Content;

/**
 * Class to provide methods for DRM
 * TODO: This class is work in progress and is expected to change.
 */
public class DrmProvider {

    private Content mContent;
    private Context mContext;

    /**
     * Constructor.
     *
     * @param content Content for which DRM data is required.
     * @param context The context.
     */
    public DrmProvider(Content content, Context context) {

        mContent = content;
        mContext = context;
    }

    /**
     * Fetches the LA URL from resources file.
     * Override this method to provide your custom LA URL.
     *
     * @return The LA URL.
     */
    public String fetchLaUrl() {

        return mContext.getString(R.string.la_url);
    }

    /**
     * Fetches encryption schema from resource file.
     *
     * @return The encryption schema.
     */
    public String getEncryptionSchema() {

        return mContext.getString(R.string.drm_encryption_schema);
    }
}
