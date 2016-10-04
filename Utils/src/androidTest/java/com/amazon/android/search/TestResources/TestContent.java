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
package com.amazon.android.search.TestResources;

import java.util.Locale;

/**
 * This class acts as test content for the tests.
 */
public class TestContent {

    private String mTitle;
    private String mSubtitle;
    private String mUrl;
    private String mDescription;

    public static final String TITLE_FIELD_NAME = "title";
    public static final String DESCRIPTION_FIELD_NAME = "description";

    public TestContent() {

    }

    public TestContent(String title) {

        this.mTitle = title;
        mDescription = "";
    }


    public boolean searchInFields(String query, String[] fieldNames) {

        boolean result = false;
        for (String fieldName : fieldNames) {
            result |= getStringFieldByName(fieldName)
                    .toLowerCase(Locale.ENGLISH).contains(query.toLowerCase(Locale.ENGLISH));
        }
        return result;
    }

    public String getStringFieldByName(String name) {

        if (name.equals(TITLE_FIELD_NAME)) {
            return mTitle;
        }
        else if (name.equals(DESCRIPTION_FIELD_NAME)) {
            return mDescription;
        }
        return null;
    }
}
