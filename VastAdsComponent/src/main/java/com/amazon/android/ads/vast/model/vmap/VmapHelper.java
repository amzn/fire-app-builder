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
package com.amazon.android.ads.vast.model.vmap;

import com.amazon.dynamicparser.impl.XmlParser;
import com.amazon.utils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class contains common keys to use while parsing a VMAP/VAST model.
 */
public class VmapHelper {

    /**
     * Key to get the extensions element.
     */
    public static final String EXTENSIONS_KEY = "vmap:Extensions";

    /**
     * Key to get the extension element.
     */
    public static final String EXTENSION_KEY = "vmap:Extension";

    /**
     * Key to get the VAST element.
     */
    public static final String VAST_KEY = "VAST";

    /**
     * Key to get the inline element.
     */
    public static final String INLINE_KEY = "InLine";

    /**
     * Key to get the id attribute.
     */
    public static final String ID_KEY = "id";

    /**
     * Key to get the sequence attribute.
     */
    public static final String SEQUENCE_KEY = "sequence";

    /**
     * Key to get the version attribute.
     */
    public static final String VERSION_KEY = "version";

    /**
     * Key to get the template type attribute.
     */
    public static final String TEMPLATE_TYPE_KEY = "templateType";

    /**
     * Key to get the error element.
     */
    public static final String ERROR_ELEMENT_KEY = "Error";

    /**
     * Key to get the impression element.
     */
    public static final String IMPRESSION_KEY = "Impression";

    /**
     * Key to get Tracking element.
     */
    public static final String TRACKING_KEY = "Tracking";

    /**
     * Creates a list of extensions from the given data map.
     *
     * @param extensionsMap Data map containing the info needed to create the extensions.
     * @return List of extensions.
     */
    public static List<Extension> createExtensions(Map<String, Map> extensionsMap) {

        List<Extension> extensions = new ArrayList<>();

        Object extensionObject = extensionsMap.get(VmapHelper.EXTENSION_KEY);
        if (extensionObject instanceof List) {

            List<Map> extensionList =
                    ListUtils.getValueAsMapList(extensionsMap, VmapHelper.EXTENSION_KEY);

            for (Map<String, Map> extensionMap : extensionList) {
                extensions.add(new Extension(extensionMap));
            }
        }
        else {
            Map<String, Map> extensionMap = (Map<String, Map>) extensionObject;
            if (extensionMap != null) {
                extensions.add(new Extension(extensionMap));
            }
        }
        return extensions;
    }

    /**
     * Tries to get a text value from the map. It first tries to get the value using the cdata key.
     * If no CDATA value is found, it returns the value from the map using the text key.
     *
     * @param map The map containing the text value.
     * @return The text value returned by the cdata or text key.
     */
    public static String getTextValueFromMap(Map map) {

        if (map.containsKey(XmlParser.CDATA_TAG)) {
            return String.valueOf(map.get(XmlParser.CDATA_TAG));
        }
        return String.valueOf(map.get(XmlParser.TEXT_TAG));
    }

    /**
     * Tries to get a list of strings from the value that matches to the key within the map.
     *
     * @param map The map containing the strings.
     * @param key The key.
     * @return List of strings.
     */
    public static List<String> getStringListFromMap(Map map, String key) {

        List<String> strings = new ArrayList<>();
        List<Map> impressionList = ListUtils.getValueAsMapList(map, key);
        for (Map impressionMap : impressionList) {
            String impression = VmapHelper.getTextValueFromMap(impressionMap);
            if (impression != null) {
                strings.add(impression);
            }
        }
        return strings;
    }
}
