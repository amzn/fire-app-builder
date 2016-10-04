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
package uk.co.chrisjenx.calligraphy;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * A helper loading {@link android.graphics.Typeface} avoiding the leak of the font when loaded
 * by multiple calls to {@link android.graphics.Typeface#createFromAsset(android.content.res.AssetManager, String)}
 * on pre-ICS versions.
 * <p>
 * More details can be found here https://code.google.com/p/android/issues/detail?id=9904
 * <p>
 * Created by Chris Jenkins on 04/09/13.
 */
public final class TypefaceUtils {

    private static final Map<String, Typeface> sCachedFonts = new HashMap<String, Typeface>();
    private static final Map<Typeface, CalligraphyTypefaceSpan> sCachedSpans = new HashMap<Typeface, CalligraphyTypefaceSpan>();

    /**
     * A helper loading a custom font.
     *
     * @param assetManager App's asset manager.
     * @param filePath     The path of the file.
     * @return Return {@link android.graphics.Typeface} or null if the path is invalid.
     */
    public static Typeface load(final AssetManager assetManager, final String filePath) {
        synchronized (sCachedFonts) {
            try {
                if (!sCachedFonts.containsKey(filePath)) {

                    // START AMAZON MODIFICATION

                    // If the given path is an absolute path, then load font from the given path,
                    // otherwise, load font from the asset.
                    final Typeface typeface = filePath.startsWith("/") ? Typeface.createFromFile
                            (filePath) : Typeface.createFromAsset(assetManager, filePath);

                    // END AMAZON MODIFICATION

                    sCachedFonts.put(filePath, typeface);
                    return typeface;
                }
            } catch (Exception e) {
                Log.w("Calligraphy", "Can't create asset from " + filePath + ". Make sure you have passed in the correct path and file name.", e);
                sCachedFonts.put(filePath, null);
                return null;
            }
            return sCachedFonts.get(filePath);
        }
    }

    /**
     * A helper loading custom spans so we don't have to keep creating hundreds of spans.
     *
     * @param typeface not null typeface
     * @return will return null of typeface passed in is null.
     */
    public static CalligraphyTypefaceSpan getSpan(final Typeface typeface) {
        if (typeface == null) return null;
        synchronized (sCachedSpans) {
            if (!sCachedSpans.containsKey(typeface)) {
                final CalligraphyTypefaceSpan span = new CalligraphyTypefaceSpan(typeface);
                sCachedSpans.put(typeface, span);
                return span;
            }
            return sCachedSpans.get(typeface);
        }
    }

    /**
     * Is the passed in typeface one of ours?
     *
     * @param typeface nullable, the typeface to check if ours.
     * @return true if we have loaded it false otherwise.
     */
    public static boolean isLoaded(Typeface typeface) {
        return typeface != null && sCachedFonts.containsValue(typeface);
    }

    private TypefaceUtils() {
    }
}
