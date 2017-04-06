/**
 * This file was modified by Amazon:
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
/*
 * Copyright (C) 2011 George Yunaev @ Ulduzsoft
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package com.amazon.android.contentbrowser.helper;

import java.io.File;
import java.util.HashMap;

/**
 * Font Manager class.
 *
 * Url: http://www.ulduzsoft.com/2012/01/enumerating-the-fonts-on-android-platform/
 */
public class FontManager {

    /**
     * This function enumerates all fonts on Android system.
     *
     * @return HashMap with the font literal name as key, and the font absolute file name as value.
     */
    public static HashMap<String, String> enumerateFonts() {

        String[] fontdirs = {"/system/fonts", "/system/font", "/data/fonts"};
        HashMap<String, String> fonts = new HashMap();
        TTFAnalyzer analyzer = new TTFAnalyzer();

        for (String fontdir : fontdirs) {
            File dir = new File(fontdir);

            if (!dir.exists())
                continue;

            File[] files = dir.listFiles();

            if (files == null)
                continue;

            for (File file : files) {
                String fontName = analyzer.getTtfFontName(file.getAbsolutePath());

                if (fontName != null)
                    fonts.put(fontName, file.getAbsolutePath());
            }
        }

        return fonts.isEmpty() ? null : fonts;
    }
}