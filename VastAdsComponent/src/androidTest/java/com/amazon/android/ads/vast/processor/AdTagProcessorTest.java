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
package com.amazon.android.ads.vast.processor;

import com.amazon.android.ads.vast.model.vast.AdElement;
import com.amazon.android.ads.vast.model.vast.ClickElement;
import com.amazon.android.ads.vast.model.vast.Companion;
import com.amazon.android.ads.vast.model.vast.CompanionAd;
import com.amazon.android.ads.vast.model.vast.Creative;
import com.amazon.android.ads.vast.model.vast.Inline;
import com.amazon.android.ads.vast.model.vast.LinearAd;
import com.amazon.android.ads.vast.model.vast.MediaFile;
import com.amazon.android.ads.vast.model.vast.VastResponse;
import com.amazon.android.ads.vast.model.vast.VastAd;
import com.amazon.android.ads.vast.model.vmap.AdBreak;
import com.amazon.android.ads.vast.model.vmap.AdSource;
import com.amazon.android.ads.vast.model.vmap.AdTagURI;
import com.amazon.android.ads.vast.model.vmap.Extension;
import com.amazon.android.ads.vast.model.vmap.Tracking;
import com.amazon.android.ads.vast.model.vmap.VmapResponse;
import com.amazon.android.ads.vast.model.vast.VideoClicks;
import com.amazon.android.ads.vast.test.R;
import com.amazon.android.ads.vast.util.DefaultMediaPicker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

/**
 * Tests the {@link AdTagProcessor} class. Makes sure it can process the VMAP tag urls from
 * https://developers.google.com/interactive-media-ads/docs/sdks/html5/tags.
 */
@RunWith(AndroidJUnit4.class)
public class AdTagProcessorTest {

    private AdTagProcessor mAdTagProcessor;
    private long fakeDuration = 60000;

    @Before
    public void setUp() {

        mAdTagProcessor = new AdTagProcessor(new DefaultMediaPicker(InstrumentationRegistry
                                                                            .getContext()));
    }

    /**
     * Tests the {@link AdTagProcessor#process(String)} method with an invalid tag url. The result
     * should be an error value.
     */
    @Test
    public void testProcessError() throws Exception {

        assertTrue(mAdTagProcessor.process("some bad text") == AdTagProcessor.AdTagType.error);

    }

    /**
     * Tests the {@link AdTagProcessor#process(String)} method with a VAST 2.0 tag.
     */
    @Test
    public void testProcessVast2Ad() throws Exception {

        String vast2Ad = InstrumentationRegistry.getContext().getString(R.string.vast_2_tag);
        assertTrue(mAdTagProcessor.process(vast2Ad) == AdTagProcessor.AdTagType.vast);
        assertEquals(1, mAdTagProcessor.getAdResponse().getPreRollAdBreaks(0).size());
        assertEquals(0, mAdTagProcessor.getAdResponse().getMidRollAdBreaks(fakeDuration).size());
        assertEquals(0, mAdTagProcessor.getAdResponse().getPostRollAdBreaks(fakeDuration).size());

    }

    /**
     * Tests the {@link AdTagProcessor#process(String)} method with a VAST 2.0 wrapper tag.
     */
    @Test
    public void testProcessVast2WrapperAd() throws Exception {

        String vast2Ad =
                InstrumentationRegistry.getContext().getString(R.string.vast_2_wrapper_tag);

        assertTrue(mAdTagProcessor.process(vast2Ad) == AdTagProcessor.AdTagType.vast);

        VmapResponse vmapResponse = mAdTagProcessor.getAdResponse();

        assertNotNull(vmapResponse);
        assertEquals(1, vmapResponse.getPreRollAdBreaks(0).size());
        assertEquals(0, vmapResponse.getMidRollAdBreaks(fakeDuration).size());
        assertEquals(0, vmapResponse.getPostRollAdBreaks(fakeDuration).size());


        VastResponse vastResponse = vmapResponse.getAdBreaks().get(0).getAdSource()
                                                .getVastResponse();

        assertNotNull(vastResponse);
        assertEquals("2.0", vastResponse.getVersion());

        Inline inline = vastResponse.getAdElements().get(0).getInlineAd();
        assertNotNull(inline);


        assertEquals(2, inline.getImpressions().size());
        assertEquals("http://www.target.com/impression?impression",
                     inline.getImpressions().get(0));
        assertEquals("http://www.wrapper1.com/impression?impression",
                     inline.getImpressions().get(1));

        assertEquals(1, inline.getErrorUrls().size());
        assertEquals("http://www.wrapper1.com/error", inline.getErrorUrls().get(0));

        assertEquals(1, inline.getCreatives().size());
        Creative creative = inline.getCreatives().get(0);
        assertEquals("00:00:15", ((LinearAd) creative.getVastAd()).getDuration());
        assertEquals(9, creative.getVastAd().getTrackingEvents().size());
        assertEquals(1, creative.getVastAd().getMediaFiles().size());

        HashMap<String, List<String>> trackingEvents = inline.getTrackingEvents();
        assertEquals(2, trackingEvents.get(Tracking.FIRST_QUARTILE_TYPE).size());
        assertTrue(trackingEvents.get(Tracking.FIRST_QUARTILE_TYPE)
                                 .contains("http://www.target.com/event?firstQuartile"));
        assertTrue(trackingEvents.get(Tracking.FIRST_QUARTILE_TYPE)
                                 .contains("http://www.wrapper1.com/event?firstQuartile"));
        assertEquals(2, trackingEvents.get(Tracking.MIDPOINT_TYPE).size());
        assertEquals(2, trackingEvents.get(Tracking.THIRD_QUARTILE_TYPE).size());
        assertEquals(2, trackingEvents.get(Tracking.COMPLETE_TYPE).size());
        assertEquals(1, trackingEvents.get(Tracking.MUTE_TYPE).size());

        checkVideoClickElement(getVideoClicks(inline), "http://www.nexage.com", null,
                               VideoClicks.CLICK_THROUGH_KEY);
    }

    /**
     * Tests the {@link AdTagProcessor#process(String)} method with a VAST 3.0 wrapper tag.
     */
    @Test
    public void testProcessVast3WrapperAd() throws Exception {

        String vast3Ad =
                InstrumentationRegistry.getContext().getString(R.string.vast_3_wrapper_tag);
        String clickThroughUri =
                InstrumentationRegistry.getContext().getString(R.string.vast_3_wrapper_click_through_uri);

        assertTrue(mAdTagProcessor.process(vast3Ad) == AdTagProcessor.AdTagType.vast);

        VmapResponse vmapResponse = mAdTagProcessor.getAdResponse();

        assertNotNull(vmapResponse);
        assertEquals(1, vmapResponse.getPreRollAdBreaks(0).size());
        assertEquals(0, vmapResponse.getMidRollAdBreaks(fakeDuration).size());
        assertEquals(0, vmapResponse.getPostRollAdBreaks(fakeDuration).size());

        VastResponse vastResponse = vmapResponse.getAdBreaks().get(0)
                                                .getAdSource().getVastResponse();
        assertNotNull(vastResponse);
        assertEquals("3.0", vastResponse.getVersion());
        Inline inline = vastResponse.getAdElements().get(0).getInlineAd();
        assertNotNull(inline);


        assertEquals(2, inline.getImpressions().size());

        assertEquals(2, inline.getErrorUrls().size());

        assertEquals(2, inline.getCreatives().size());
        Creative creative = inline.getCreatives().get(0);
        assertEquals("00:00:10", ((LinearAd) creative.getVastAd()).getDuration());
        assertEquals(32, creative.getVastAd().getTrackingEvents().size());
        assertEquals(8, creative.getVastAd().getMediaFiles().size());

        checkVideoClickElement(getVideoClicks(inline), clickThroughUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);
    }

    /**
     * Tests the {@link AdTagProcessor#process(String)} method with a VAST 3.0 tag.
     */
    @Test
    public void testProcessVastPreRoll() throws Exception {

        String vastPreRollTag =
                InstrumentationRegistry.getContext().getString(R.string.vast_preroll_tag);
        String vastInspectorUri =
                InstrumentationRegistry.getContext().getString(R.string.vast_inspector_uri);

        assertTrue(mAdTagProcessor.process(vastPreRollTag) == AdTagProcessor.AdTagType.vast);

        VmapResponse vmapResponse = mAdTagProcessor.getAdResponse();

        assertNotNull(vmapResponse);
        assertEquals(1, vmapResponse.getPreRollAdBreaks(0).size());
        assertEquals(0, vmapResponse.getMidRollAdBreaks(fakeDuration).size());
        assertEquals(0, vmapResponse.getPostRollAdBreaks(fakeDuration).size());

        VastResponse vastResponse = vmapResponse.getAdBreaks().get(0)
                                                .getAdSource().getVastResponse();

        assertNotNull(vastResponse);

        assertEquals("3.0", vastResponse.getVersion());
        Inline inline = vastResponse.getAdElements().get(0).getInlineAd();
        assertNotNull(inline);
        List<Creative> creativeList = inline.getCreatives();
        assertEquals(2, creativeList.size());

        Creative creative = creativeList.get(0);
        assertEquals("57859154776", creative.getId());
        assertEquals("1", creative.getSequence());

        VastAd vastAd = creative.getVastAd();
        assertNotNull(vastAd);
        assertTrue(vastAd instanceof LinearAd);
        LinearAd linearAd = (LinearAd) vastAd;

        List<MediaFile> mediaFiles = linearAd.getMediaFiles();
        assertEquals(8, mediaFiles.size());

        creative = creativeList.get(1);
        assertEquals("57857370976", creative.getId());
        assertEquals("1", creative.getSequence());

        vastAd = creative.getVastAd();
        assertNotNull(vastAd);
        assertTrue(vastAd instanceof CompanionAd);
        List<Companion> companionList = ((CompanionAd) vastAd).getCompanions();
        assertEquals(1, companionList.size());
        Companion companion = companionList.get(0);
        assertEquals("250", companion.getHeight());
        assertEquals("300", companion.getWidth());
        assertEquals("57857370976", companion.getId());
        assertEquals(1, companion.getTrackings().size());
        assertEquals("creativeView", companion.getTrackings().get(0).getEvent());

        checkVideoClickElement(getVideoClicks(linearAd), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);
    }

    /**
     * Tests the {@link AdTagProcessor#process(String)} method with a bad ad tag url.
     */
    @Test
    public void testProcessAdTagFailure() throws Exception {

        assertEquals(AdTagProcessor.AdTagType.error, mAdTagProcessor.process("bad url"));
    }

    /**
     * Tests the {@link AdTagProcessor#process(String)} method with a VMAP pre-roll tag.
     */
    @Test
    public void testProcessVmapPreRoll() throws Exception {


        String vmapPreRollTag =
                InstrumentationRegistry.getContext().getString(R.string.vmap_preroll_tag);
        String vastInspectorUri =
                InstrumentationRegistry.getContext().getString(R.string.vast_inspector_uri);

        assertEquals(AdTagProcessor.AdTagType.vmap, mAdTagProcessor.process(vmapPreRollTag));

        VmapResponse vmapResponse = mAdTagProcessor.getAdResponse();

        assertNotNull(vmapResponse);
        assertEquals(1, vmapResponse.getPreRollAdBreaks(0).size());
        assertEquals(0, vmapResponse.getMidRollAdBreaks(fakeDuration).size());
        assertEquals(0, vmapResponse.getPostRollAdBreaks(fakeDuration).size());

        assertEquals(1, vmapResponse.getAdBreaks().size());

        AdBreak adBreak = vmapResponse.getAdBreaks().get(0);
        checkAdBreakAttributes(adBreak, "start", "linear", "preroll");

        AdSource adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "preroll-ad-1", false, true);

        VastResponse vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);

        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);
    }

    /**
     * Tests the {@link AdTagProcessor#process(String)} method with a VMAP pre-roll tag with
     * ad bumpers.
     */
    @Test
    public void testProcessVmapPreRollBumper() throws Exception {

        String vmapPreRollBumperTag =
                InstrumentationRegistry.getContext().getString(R.string.vmap_preroll_bumper_tag);
        String vastInspectorUri =
                InstrumentationRegistry.getContext().getString(R.string.vast_inspector_uri);

        assertEquals(AdTagProcessor.AdTagType.vmap, mAdTagProcessor.process(vmapPreRollBumperTag));

        VmapResponse vmapResponse = mAdTagProcessor.getAdResponse();

        assertNotNull(vmapResponse);
        assertEquals(2, vmapResponse.getPreRollAdBreaks(0).size());
        assertEquals(0, vmapResponse.getMidRollAdBreaks(fakeDuration).size());
        assertEquals(0, vmapResponse.getPostRollAdBreaks(fakeDuration).size());

        List<AdBreak> adBreakList = vmapResponse.getAdBreaks();
        assertEquals(2, adBreakList.size());

        // Check first ad break.
        AdBreak adBreak = adBreakList.get(0);
        checkAdBreakAttributes(adBreak, "start", "linear", "preroll");

        AdSource adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "preroll-ad-1", false, true);

        VastResponse vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);

        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check second ad break.
        adBreak = adBreakList.get(1);
        checkAdBreakAttributes(adBreak, "start", "linear", "preroll");

        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "preroll-post-bumper", false, true);

        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);

        assertEquals(1, adBreak.getExtensions().size());
        Extension extension = adBreak.getExtensions().get(0);
        checkExtensionAttributes(extension, "bumper", true);

        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);
    }

    /**
     * Tests the {@link AdTagProcessor#process(String)} method with a VMAP post-roll tag.
     */
    @Test
    public void testVmapPostRoll() throws Exception {

        String vmapPostRollTag =
                InstrumentationRegistry.getContext().getString(R.string.vmap_postroll_tag);
        String vastInspectorUri =
                InstrumentationRegistry.getContext().getString(R.string.vast_inspector_uri);

        assertEquals(AdTagProcessor.AdTagType.vmap, mAdTagProcessor.process(vmapPostRollTag));

        VmapResponse vmapResponse = mAdTagProcessor.getAdResponse();

        assertNotNull(vmapResponse);
        assertEquals(0, vmapResponse.getPreRollAdBreaks(0).size());
        assertEquals(0, vmapResponse.getMidRollAdBreaks(fakeDuration).size());
        assertEquals(1, vmapResponse.getPostRollAdBreaks(fakeDuration).size());

        List<AdBreak> adBreakList = vmapResponse.getAdBreaks();
        assertEquals(1, adBreakList.size());

        // Check first ad break.
        AdBreak adBreak = adBreakList.get(0);
        checkAdBreakAttributes(adBreak, "end", "linear", "postroll");
        AdSource adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "postroll-ad-1", false, true);
        VastResponse vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);

        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);
    }

    /**
     * Tests the {@link AdTagProcessor#process(String)} method with a VMAP post-roll tag with ad
     * bumpers.
     */
    @Test
    public void testVmapPostRollBumper() throws Exception {

        String vmapPreMidPostSingleAdTag = InstrumentationRegistry
                .getContext().getString(R.string.vmap_postroll_bumper_tag);
        String vastInspectorUri =
                InstrumentationRegistry.getContext().getString(R.string.vast_inspector_uri);

        assertEquals(AdTagProcessor.AdTagType.vmap,
                     mAdTagProcessor.process(vmapPreMidPostSingleAdTag));

        VmapResponse vmapResponse = mAdTagProcessor.getAdResponse();

        assertNotNull(vmapResponse);
        assertEquals(0, vmapResponse.getPreRollAdBreaks(0).size());
        assertEquals(0, vmapResponse.getMidRollAdBreaks(fakeDuration).size());
        assertEquals(2, vmapResponse.getPostRollAdBreaks(fakeDuration).size());

        List<AdBreak> adBreakList = vmapResponse.getAdBreaks();
        assertEquals(2, adBreakList.size());

        // Check first ad break.
        AdBreak adBreak = adBreakList.get(0);
        checkAdBreakAttributes(adBreak, "end", "linear", "postroll");
        AdSource adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "postroll-pre-bumper", false, true);
        VastResponse vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        assertEquals(1, adBreak.getExtensions().size());
        Extension extension = adBreak.getExtensions().get(0);
        checkExtensionAttributes(extension, "bumper", true);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check second ad break.
        adBreak = adBreakList.get(1);
        checkAdBreakAttributes(adBreak, "end", "linear", "postroll");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "postroll-ad-1", false, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);
    }

    /**
     * Tests the {@link AdTagProcessor#process(String)} method with a VMAP tag that has pre-, mid-,
     * and post-roll single ads.
     */
    @Test
    public void testVmapPreMidPostRollsSingleAds() throws Exception {

        String vmapPreMidPostSingleAdTag = InstrumentationRegistry
                .getContext().getString(R.string.vmap_pre_mid_post_rolls_single_ads_tag);
        String vastInspectorUri =
                InstrumentationRegistry.getContext().getString(R.string.vast_inspector_uri);

        assertEquals(AdTagProcessor.AdTagType.vmap,
                     mAdTagProcessor.process(vmapPreMidPostSingleAdTag));

        VmapResponse vmapResponse = mAdTagProcessor.getAdResponse();

        assertNotNull(vmapResponse);
        assertEquals(1, vmapResponse.getPreRollAdBreaks(0).size());
        assertEquals(1, vmapResponse.getMidRollAdBreaks(fakeDuration).size());
        assertEquals(1, vmapResponse.getPostRollAdBreaks(fakeDuration).size());

        List<AdBreak> adBreakList = vmapResponse.getAdBreaks();
        assertEquals(3, adBreakList.size());

        // Check first ad break.
        AdBreak adBreak = adBreakList.get(0);
        checkAdBreakAttributes(adBreak, "start", "linear", "preroll");
        AdSource adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "preroll-ad-1", false, true);
        VastResponse vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check second ad break.
        adBreak = adBreakList.get(1);
        checkAdBreakAttributes(adBreak, "00:00:15.000", "linear", "midroll-1");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "midroll-1-ad-1", false, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check third ad break.
        adBreak = adBreakList.get(2);
        checkAdBreakAttributes(adBreak, "end", "linear", "postroll");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "postroll-ad-1", false, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);
    }

    /**
     * Tests the {@link AdTagProcessor#process(String)} method with a VMAP tag that has a single
     * pre-roll ad, a mid-roll standard ad pod with 3 ads, and a single post-roll ad.
     */
    @Test
    public void testVmapStandardPodWith3Ads() throws Exception {

        String vmapPreMidPostSingleAdTag = InstrumentationRegistry
                .getContext().getString(R.string.vmap_standard_pod_with_3_ads);
        String vastInspectorUri =
                InstrumentationRegistry.getContext().getString(R.string.vast_inspector_uri);

        assertEquals(AdTagProcessor.AdTagType.vmap,
                     mAdTagProcessor.process(vmapPreMidPostSingleAdTag));

        VmapResponse vmapResponse = mAdTagProcessor.getAdResponse();

        assertNotNull(vmapResponse);
        assertEquals(1, vmapResponse.getPreRollAdBreaks(0).size());
        assertEquals(3, vmapResponse.getMidRollAdBreaks(fakeDuration).size());
        assertEquals(1, vmapResponse.getPostRollAdBreaks(fakeDuration).size());

        List<AdBreak> adBreakList = vmapResponse.getAdBreaks();
        assertEquals(5, adBreakList.size());

        // Check first ad break.
        AdBreak adBreak = adBreakList.get(0);
        checkAdBreakAttributes(adBreak, "start", "linear", "preroll");
        AdSource adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "preroll-ad-1", false, true);
        VastResponse vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check second ad break.
        adBreak = adBreakList.get(1);
        checkAdBreakAttributes(adBreak, "00:00:15.000", "linear", "midroll-1");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "midroll-1-ad-1", false, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check third ad break.
        adBreak = adBreakList.get(2);
        checkAdBreakAttributes(adBreak, "00:00:15.000", "linear", "midroll-1");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "midroll-1-ad-2", false, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check fourth ad break.
        adBreak = adBreakList.get(3);
        checkAdBreakAttributes(adBreak, "00:00:15.000", "linear", "midroll-1");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "midroll-1-ad-3", false, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check fifth ad break.
        adBreak = adBreakList.get(4);
        checkAdBreakAttributes(adBreak, "end", "linear", "postroll");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "postroll-ad-1", false, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);
    }

    /**
     * Tests the {@link AdTagProcessor#process(String)} method with a VMAP tag that has a single
     * pre-roll ad, a mid-roll optimized ad pod with 3 ads, and a single post-roll ad.
     */
    @Test
    public void testVmapOptimizedPodWith3Ads() throws Exception {

        String vmapPreMidPostSingleAdTag = InstrumentationRegistry
                .getContext().getString(R.string.vmap_optimized_pod_with_3_ads);
        String vastInspectorUri =
                InstrumentationRegistry.getContext().getString(R.string.vast_inspector_uri);

        assertEquals(AdTagProcessor.AdTagType.vmap,
                     mAdTagProcessor.process(vmapPreMidPostSingleAdTag));

        VmapResponse vmapResponse = mAdTagProcessor.getAdResponse();

        assertNotNull(vmapResponse);
        assertEquals(1, vmapResponse.getPreRollAdBreaks(0).size());
        assertEquals(1, vmapResponse.getMidRollAdBreaks(fakeDuration).size());
        assertEquals(1, vmapResponse.getPostRollAdBreaks(fakeDuration).size());

        List<AdBreak> adBreakList = vmapResponse.getAdBreaks();
        assertEquals(3, adBreakList.size());

        // Check first ad break.
        AdBreak adBreak = adBreakList.get(0);
        checkAdBreakAttributes(adBreak, "start", "linear", "preroll");
        AdSource adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "preroll-ad-1", false, true);
        VastResponse vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check second ad break.
        adBreak = adBreakList.get(1);
        checkAdBreakAttributes(adBreak, "00:00:15.000", "linear", "midroll-1");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "midroll-1-ads", true, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);

        List<AdElement> adElements = vastResponse.getAdElements();
        assertEquals(3, adElements.size());
        AdElement ad1 = adElements.get(0);
        AdElement ad2 = adElements.get(1);
        AdElement ad3 = adElements.get(2);
        assertEquals(1, ad1.getAdSequence());
        assertEquals(2, ad2.getAdSequence());
        assertEquals(3, ad3.getAdSequence());

        checkVideoClickElement(getVideoClicks(ad1.getInlineAd()), vastInspectorUri, null,
                               VideoClicks.CLICK_THROUGH_KEY);
        checkVideoClickElement(getVideoClicks(ad2.getInlineAd()), vastInspectorUri, null,
                               VideoClicks.CLICK_THROUGH_KEY);
        checkVideoClickElement(getVideoClicks(ad3.getInlineAd()), vastInspectorUri, null,
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check third ad break.
        adBreak = adBreakList.get(2);
        checkAdBreakAttributes(adBreak, "end", "linear", "postroll");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "postroll-ad-1", false, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);
    }

    /**
     * Tests the {@link AdTagProcessor#process(String)} method with a VMAP tag that has a single
     * pre-roll ad, a mid-roll standard ad pod with 3 ads, and a single post-roll ad. All ad
     * breaks have bumpers around them.
     */
    @Test
    public void testVmapStandardPodWith3AdsWithBumpers() throws Exception {

        String vmapPreMidPostSingleAdTag = InstrumentationRegistry
                .getContext().getString(R.string.vmap_standard_pod_with_3_ads_bumpers);
        String vastInspectorUri =
                InstrumentationRegistry.getContext().getString(R.string.vast_inspector_uri);

        assertEquals(AdTagProcessor.AdTagType.vmap,
                     mAdTagProcessor.process(vmapPreMidPostSingleAdTag));

        VmapResponse vmapResponse = mAdTagProcessor.getAdResponse();

        assertNotNull(vmapResponse);
        assertEquals(2, vmapResponse.getPreRollAdBreaks(0).size());
        assertEquals(5, vmapResponse.getMidRollAdBreaks(fakeDuration).size());
        assertEquals(2, vmapResponse.getPostRollAdBreaks(fakeDuration).size());

        List<AdBreak> adBreakList = vmapResponse.getAdBreaks();
        assertEquals(9, adBreakList.size());

        // Check first ad break.
        AdBreak adBreak = adBreakList.get(0);
        checkAdBreakAttributes(adBreak, "start", "linear", "preroll");
        AdSource adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "preroll-ad-1", false, true);
        VastResponse vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check second ad break.
        adBreak = adBreakList.get(1);
        checkAdBreakAttributes(adBreak, "start", "linear", "preroll");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "preroll-post-bumper", false, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        Extension extension = adBreak.getExtensions().get(0);
        checkExtensionAttributes(extension, "bumper", true);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check third ad break.
        adBreak = adBreakList.get(2);
        checkAdBreakAttributes(adBreak, "00:00:15.000", "linear", "midroll-1");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "midroll-1-pre-bumper", false, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        extension = adBreak.getExtensions().get(0);
        checkExtensionAttributes(extension, "bumper", true);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check fourth ad break.
        adBreak = adBreakList.get(3);
        checkAdBreakAttributes(adBreak, "00:00:15.000", "linear", "midroll-1");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "midroll-1-ad-1", false, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check fifth ad break.
        adBreak = adBreakList.get(4);
        checkAdBreakAttributes(adBreak, "00:00:15.000", "linear", "midroll-1");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "midroll-1-ad-2", false, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check sixth ad break.
        adBreak = adBreakList.get(5);
        checkAdBreakAttributes(adBreak, "00:00:15.000", "linear", "midroll-1");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "midroll-1-ad-3", false, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check seventh ad break.
        adBreak = adBreakList.get(6);
        checkAdBreakAttributes(adBreak, "00:00:15.000", "linear", "midroll-1");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "midroll-1-post-bumper", false, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check eighth ad break.
        adBreak = adBreakList.get(7);
        checkAdBreakAttributes(adBreak, "end", "linear", "postroll");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "postroll-pre-bumper", false, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        extension = adBreak.getExtensions().get(0);
        checkExtensionAttributes(extension, "bumper", true);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check ninth ad break.
        adBreak = adBreakList.get(8);
        checkAdBreakAttributes(adBreak, "end", "linear", "postroll");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "postroll-ad-1", false, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);
    }

    /**
     * Tests the {@link AdTagProcessor#process(String)} method with a VMAP tag that has a single
     * pre-roll ad, a mid-roll optimzied ad pod with 3 ads, and a single post-roll ad. All ad
     * breaks have bumpers around them.
     */
    @Test
    public void testVmapOptimizedPodWith3AdsWithBumpers() throws Exception {

        String vmapPreMidPostSingleAdTag = InstrumentationRegistry
                .getContext().getString(R.string.vmap_optimized_pod_with_3_ads_bumpers);
        String vastInspectorUri =
                InstrumentationRegistry.getContext().getString(R.string.vast_inspector_uri);

        assertEquals(AdTagProcessor.AdTagType.vmap,
                     mAdTagProcessor.process(vmapPreMidPostSingleAdTag));

        VmapResponse vmapResponse = mAdTagProcessor.getAdResponse();

        assertNotNull(vmapResponse);
        assertEquals(2, vmapResponse.getPreRollAdBreaks(0).size());
        assertEquals(3, vmapResponse.getMidRollAdBreaks(fakeDuration).size());
        assertEquals(2, vmapResponse.getPostRollAdBreaks(fakeDuration).size());

        List<AdBreak> adBreakList = vmapResponse.getAdBreaks();
        assertEquals(7, adBreakList.size());

        // Check first ad break.
        AdBreak adBreak = adBreakList.get(0);
        checkAdBreakAttributes(adBreak, "start", "linear", "preroll");
        AdSource adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "preroll-ad-1", false, true);
        VastResponse vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check second ad break.
        adBreak = adBreakList.get(1);
        checkAdBreakAttributes(adBreak, "start", "linear", "preroll");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "preroll-post-bumper", false, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        Extension extension = adBreak.getExtensions().get(0);
        checkExtensionAttributes(extension, "bumper", true);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check third ad break.
        adBreak = adBreakList.get(2);
        checkAdBreakAttributes(adBreak, "00:00:15.000", "linear", "midroll-1");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "midroll-1-pre-bumper", false, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        extension = adBreak.getExtensions().get(0);
        checkExtensionAttributes(extension, "bumper", true);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check fourth ad break.
        adBreak = adBreakList.get(3);
        checkAdBreakAttributes(adBreak, "00:00:15.000", "linear", "midroll-1");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "midroll-1-ads", true, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check fifth ad break.
        adBreak = adBreakList.get(4);
        checkAdBreakAttributes(adBreak, "00:00:15.000", "linear", "midroll-1");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "midroll-1-post-bumper", false, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        extension = adBreak.getExtensions().get(0);
        checkExtensionAttributes(extension, "bumper", true);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check sixth ad break.
        adBreak = adBreakList.get(5);
        checkAdBreakAttributes(adBreak, "end", "linear", "postroll");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "postroll-pre-bumper", false, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        extension = adBreak.getExtensions().get(0);
        checkExtensionAttributes(extension, "bumper", true);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);

        // Check seventh ad break.
        adBreak = adBreakList.get(6);
        checkAdBreakAttributes(adBreak, "end", "linear", "postroll");
        adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, "postroll-ad-1", false, true);
        vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);
        checkVideoClickElement(getVideoClicks(vastResponse), vastInspectorUri, "GDFP",
                               VideoClicks.CLICK_THROUGH_KEY);
    }

    /**
     * Tests the {@link AdTagProcessor#process(String)} method with a VMAP tag that has a single
     * VMAP with ad pod of 2 VAST ads and with VMAP tracking event breakStart
     */
    @Test
    public void testVmapVastAdPodWithVMAPTracking() throws Exception {

        String vmapVastAdPodWithVmapTracking = InstrumentationRegistry
                .getContext().getString(R.string.vmap_vast_ad_pod_with_vmap_tracking_event);
        String clickThroughUri =
                InstrumentationRegistry.getContext().getString(R.string.vmap_vast_ad_pod_with_vmap_tracking_event_click_through);

        assertEquals(AdTagProcessor.AdTagType.vmap,
                     mAdTagProcessor.process(vmapVastAdPodWithVmapTracking));

        VmapResponse vmapResponse = mAdTagProcessor.getAdResponse();

        assertNotNull(vmapResponse);
        assertEquals(1, vmapResponse.getPreRollAdBreaks(0).size());
        assertEquals(0, vmapResponse.getMidRollAdBreaks(fakeDuration).size());
        assertEquals(0, vmapResponse.getPostRollAdBreaks(fakeDuration).size());

        List<AdBreak> adBreakList = vmapResponse.getAdBreaks();
        assertEquals(1, adBreakList.size());

        // Check first ad break.
        AdBreak adBreak = adBreakList.get(0);
        assertNotNull(adBreak.getTrackingEvents());
        assertEquals(1, adBreak.getTrackingEvents().size());
        AdSource adSource = adBreak.getAdSource();
        checkAdSourceAttributes(adSource, null, true, false);
        VastResponse vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);

        List<AdElement> adElements = vastResponse.getAdElements();
        assertEquals(2, adElements.size());
        AdElement ad1 = adElements.get(0);
        AdElement ad2 = adElements.get(1);
        assertEquals(1, ad1.getAdSequence());
        assertEquals(2, ad2.getAdSequence());

        checkVideoClickElement(getVideoClicks(ad1.getInlineAd()), clickThroughUri, null,
                               VideoClicks.CLICK_THROUGH_KEY);
        checkVideoClickElement(getVideoClicks(ad2.getInlineAd()), clickThroughUri, null,
                               VideoClicks.CLICK_THROUGH_KEY);
    }

    /**
     * Tests the {@link AdTagProcessor#process(String)} method with a VMAP tag that has a single
     * VMAP with ad pod of 2 VAST ads out of sequence
     */
    @Test
    public void testVmapVastAdPodOutOfSequence() throws Exception {

        String vmapVastAdPodOutOfSequence = InstrumentationRegistry
                .getContext().getString(R.string.vast_ad_pod_out_of_sequence);

        assertEquals(AdTagProcessor.AdTagType.vast,
                     mAdTagProcessor.process(vmapVastAdPodOutOfSequence));

        VmapResponse vmapResponse = mAdTagProcessor.getAdResponse();

        assertNotNull(vmapResponse);
        assertEquals(1, vmapResponse.getPreRollAdBreaks(0).size());
        assertEquals(0, vmapResponse.getMidRollAdBreaks(fakeDuration).size());
        assertEquals(0, vmapResponse.getPostRollAdBreaks(fakeDuration).size());

        List<AdBreak> adBreakList = vmapResponse.getAdBreaks();
        assertEquals(1, adBreakList.size());

        // Check first ad break.
        AdBreak adBreak = adBreakList.get(0);
        checkAdBreakAttributes(adBreak, "start", "linear", "preroll");
        AdSource adSource = adBreak.getAdSource();
        VastResponse vastResponse = adSource.getVastResponse();
        assertNotNull(vastResponse);

        List<AdElement> adElements = vastResponse.getAdElements();
        assertEquals(2, adElements.size());
        AdElement ad1 = adElements.get(0);
        checkAdElementAttributes(ad1, "preroll-1", 2);
        AdElement ad2 = adElements.get(1);
        checkAdElementAttributes(ad2, "preroll-1", 1);

        checkVideoClickElement(getVideoClicks(ad1.getInlineAd()),
                               "http://www.jwplayer.com/", null, VideoClicks.CLICK_THROUGH_KEY);
        checkVideoClickElement(getVideoClicks(ad1.getInlineAd()),
                               "http://qa.jwplayer.com/~alex/pixel.gif?10", "Alex", VideoClicks
                                       .CLICK_TRACKING_KEY);
        checkVideoClickElement(getVideoClicks(ad2.getInlineAd()),
                               "http://www.jwplayer.com/", null, VideoClicks.CLICK_THROUGH_KEY);
        checkVideoClickElement(getVideoClicks(ad2.getInlineAd()),
                               "http://qa.jwplayer.com/~alex/pixel.gif?10", "Alex", VideoClicks
                                       .CLICK_TRACKING_KEY);
    }

    /**
     * Checks the ad break for the expected attribute values.
     *
     * @param adBreak    The ad break.
     * @param timeOffset The expected time offset.
     * @param breakType  The expected break type.
     * @param breakId    The expected break id.
     */
    private void checkAdBreakAttributes(AdBreak adBreak, String timeOffset, String breakType,
                                        String breakId) {

        assertNotNull(adBreak);
        assertEquals(timeOffset, adBreak.getTimeOffset());
        assertEquals(breakType, adBreak.getBreakType());
        assertEquals(breakId, adBreak.getBreakId());
    }

    /**
     * Checks the ad source for the expected attribute values.
     *
     * @param adSource         Teh ad source.
     * @param id               The expected id.
     * @param allowMultipleAds The expected value for allow multiple ads.
     * @param followRedirects  The expected value for follow redirects.
     */
    private void checkAdSourceAttributes(AdSource adSource, String id, boolean allowMultipleAds,
                                         boolean followRedirects) {

        assertNotNull(adSource);
        assertEquals(id, adSource.getId());
        assertEquals(allowMultipleAds, adSource.isAllowMultipleAds());
        assertEquals(followRedirects, adSource.isFollowRedirects());
    }

    /**
     * Checks the ad tag URI for the expected attribute values. Also checks that the URI is not an
     * empty string.
     *
     * @param adTagURI     The ad tag URI.
     * @param templateType The expected template type.
     */
    private void checkAdTagUriAttributes(AdTagURI adTagURI, String templateType) {

        assertNotNull(adTagURI);
        assertEquals(templateType, adTagURI.getTemplateType());
        assertFalse(adTagURI.getUri().isEmpty());
    }

    /**
     * Check the ad element for ad id and sequence.
     *
     * @param adElement The ad element.
     * @param adId      The expected ad id.
     * @param sequence  The expected ad sequence.
     */
    private void checkAdElementAttributes(AdElement adElement, String adId, int sequence) {

        assertNotNull(adElement);
        assertEquals(adId, adElement.getId());
        assertEquals(sequence, adElement.getAdSequence());
    }

    /**
     * Checks the extension for the expected attribute values.
     *
     * @param extension      The extension.
     * @param type           The expected type.
     * @param suppressBumper The expected value for suppress bumper.
     */
    private void checkExtensionAttributes(Extension extension, String type, boolean
            suppressBumper) {

        assertNotNull(extension);
        assertEquals(type, extension.getType());
        assertEquals(suppressBumper, extension.isSuppressBumper());
    }

    /**
     * Get the videoClicks object.
     *
     * @param vastResponse vastResponse object.
     * @return return the associated videoClicks object with this ad response.
     */
    private VideoClicks getVideoClicks(VastResponse vastResponse) {

        Inline inline = vastResponse.getAdElements().get(0).getInlineAd();
        assertNotNull(inline);
        return getVideoClicks(inline);
    }

    /**
     * Get the videoClicks object.
     *
     * @param inline inline ad object.
     * @return return the associated videoClicks object with this inline ad.
     */
    private VideoClicks getVideoClicks(Inline inline) {

        VastAd vastAd = inline.getCreatives().get(0).getVastAd();
        assertNotNull(vastAd);
        assertTrue(vastAd instanceof LinearAd);
        return getVideoClicks((LinearAd) vastAd);
    }

    /**
     * Get the videoClicks object.
     *
     * @param linearAd linearAd object.
     * @return return the associated videoClicks object with this linear ad.
     */
    private VideoClicks getVideoClicks(LinearAd linearAd) {

        VideoClicks videoClicks = linearAd.getVideoClicks();
        assertNotNull(videoClicks);
        return videoClicks;
    }

    /**
     * Get the right clickElement object and request for check.
     *
     * @param videoClicks click element object.
     * @param uri         uri of click element.
     * @param id          id of click element.
     * @param clickType   type of the VideoClick object need to use
     */
    private void checkVideoClickElement(VideoClicks videoClicks, String uri, String id,
                                        String clickType) {

        ClickElement clickElement = null;
        switch (clickType) {
            case VideoClicks.CLICK_THROUGH_KEY:
                clickElement = videoClicks.getVideoClickElements().get(VideoClicks
                                                                               .CLICK_THROUGH_KEY);

                break;
            case VideoClicks.CLICK_TRACKING_KEY:
                clickElement = videoClicks.getVideoClickElements().get(VideoClicks
                                                                               .CLICK_TRACKING_KEY);
                break;
            case VideoClicks.CUSTOM_CLICK_KEY:
                clickElement = videoClicks.getVideoClickElements().get(VideoClicks
                                                                               .CUSTOM_CLICK_KEY);
                break;
        }
        assertNotNull(clickElement);
        checkclickElement(clickElement, uri, id);
    }

    /**
     * Checks for matching uri and id in clickElement object.
     *
     * @param clickElement click element object.
     * @param uri          uri of click element.
     * @param id           id of click element.
     */
    private void checkclickElement(ClickElement clickElement, String uri, String id) {

        if (uri != null) {
            assertThat(clickElement.getUri(), containsString(uri));
        }
        if (id != null) {
            assertEquals(id, clickElement.getId());
        }
    }
}