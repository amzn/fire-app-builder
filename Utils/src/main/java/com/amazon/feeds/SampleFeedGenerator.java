/**
 * Copyright 2015-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazon.feeds;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

/**
 * This is a class for generating sample feeds given a format specification class.
 */
public class SampleFeedGenerator {

    // default directory for output files
    private final String SAMPLE_PATH = "samples/feeds/";

    /**
     * The method for generating sample feeds.
     *
     * @param format The class containing the format specifications.
     * @param items The number of items to generate.
     * @param ext File extension.
     */
    public void createSampleFeed(IFeedFormat format, int items, String ext) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        mapper.getFactory().setCharacterEscapes(format.getEscapeRules());

        // create output file
        String out = format.getFeedFormat() + "-" + items + "." + ext;
        // TODO: add XML support

        File outFile = new File(SAMPLE_PATH, out);
        if (!outFile.exists()) {
            outFile.getParentFile().mkdirs();
        }

        // populate sample feed
        System.out.println("Generating " + items + (items == 1 ? " item" : " items") + " for " +
                                   format.getProvider() + " feed at " + outFile.getAbsolutePath());
        format.populate(items);

        // write JSON to file
        if (format.usePrettyPrint()) {

            DefaultPrettyPrinter.Indenter indenter =
                    new DefaultIndenter("   ", DefaultIndenter.SYS_LF);
            DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
            printer.indentObjectsWith(indenter);
            printer.indentArraysWith(indenter);
            mapper.writer(printer).writeValue(outFile, format);
        } else {
            mapper.writeValue(outFile, format);
        }
    }

    /**
     * The method for generating sample feeds. Uses JSON as default format.
     *
     * @param format The class containing the format specifications.
     * @param items The number of items to generate.
     */
    public void createSampleFeed(IFeedFormat format, int items) throws Exception {
        createSampleFeed(format, items, "json");
    }
}
