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

import com.amazon.android.configuration.ConfigurationManager;
import com.amazon.android.contentbrowser.ContentBrowser;
import com.amazon.android.contentbrowser.R;
import com.amazon.android.ui.constants.ConfigurationConstants;

import android.content.Context;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Font Manager class.
 *
 * Url: http://www.ulduzsoft.com/2012/01/enumerating-the-fonts-on-android-platform/
 */
public class FontManager {

    /**
     * Configures the fonts using the configuration settings from Navigator.json if present.
     * Otherwise is uses default fonts defined in custom.xml.
     *
     * @param contentBrowser The instance of {@link ContentBrowser}.
     */
    public static void configureFonts(Context context, ContentBrowser contentBrowser) {

        configureFontPath(context, ConfigurationConstants.LIGHT_FONT,
                          contentBrowser.getLightFontPath(),
                          context.getResources().getString(R.string.default_light_font));

        configureFontPath(context, ConfigurationConstants.BOLD_FONT,
                          contentBrowser.getBoldFontPath(),
                          context.getResources().getString(R.string.default_bold_font));

        String regularFontPath =
                configureFontPath(context, ConfigurationConstants.REGULAR_FONT,
                                  contentBrowser.getRegularFontPath(),
                                  context.getResources().getString(R.string.default_regular_font));

        // Set the default font for the app.
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                                              .setDefaultFontPath(regularFontPath)
                                              .setFontAttrId(R.attr.fontPath)
                                              .build());
    }

    /**
     * Adds a font path to the Configuration Manager.
     *
     * @param pathKey         The key to use to retrieve the font path from the Configuration
     *                        Manager.
     * @param settingFontPath The font path that was given from Navigator.json config file.
     * @param defaultFontPath A default font to use if settings was not provided in the config
     *                        file.
     * @return The path that was stored in the Configuration Manager.
     */
    private static String configureFontPath(Context context, String pathKey,
                                            String settingFontPath, String defaultFontPath) {

        ConfigurationManager manager = ConfigurationManager.getInstance(context);

        // Get all device local fonts.
        Map<String, String> fonts = FontManager.enumerateFonts();

        // Figure out if default font path is needed.
        String fontPath = settingFontPath == null ? defaultFontPath : settingFontPath;

        // If the font path specifies a local device font name, replace it with the
        // absolute path of the font. Otherwise, just handle it as a custom font path.
        if (fonts != null && fonts.containsKey(fontPath)) {
            fontPath = fonts.get(fontPath);
        }

        manager.setTypefacePathValue(pathKey, fontPath);

        return fontPath;
    }

    /**
     * This function enumerates all fonts on Android system.
     *
     * @return HashMap with the font literal name as key, and the font absolute file name as value.
     */
    private static HashMap<String, String> enumerateFonts() {

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