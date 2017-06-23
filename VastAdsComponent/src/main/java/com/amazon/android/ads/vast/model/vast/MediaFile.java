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
package com.amazon.android.ads.vast.model.vast;

import com.amazon.android.ads.vast.model.vmap.VmapHelper;
import com.amazon.dynamicparser.impl.XmlParser;

import java.math.BigInteger;
import java.util.Map;

/**
 * A representation of the MediaFile element of a VAST response.
 */
public class MediaFile {

    /**
     * Key to get the maintain aspect ratio attribute.
     */
    private static final String MAINTAIN_ASPECT_RATIO_KEY = "maintainAspectRatio";

    /**
     * Key to get the height attribute.
     */
    private static final String HEIGHT_KEY = "height";

    /**
     * Key to get the width attribute.
     */
    private static final String WIDTH_KEY = "width";

    /**
     * Key to get the bitrate attribute.
     */
    private static final String BITRATE_KEY = "bitrate";

    /**
     * Key to get the scalable attribute.
     */
    private static final String SCALABLE_KEY = "scalable";

    /**
     * Key to get type attribute.
     */
    private static final String TYPE_KEY = "type";

    /**
     * Key to get the delivery attribute.
     */
    private static final String DELIVERY_KEY = "delivery";

    /**
     * Key to get the api framework attribute.
     */
    private static final String API_FRAMEWORK_KEY = "apiFramework";

    /**
     * The ad URL value.
     */
    private String mValue;

    /**
     * The id.
     */
    private String mId;

    /**
     * The delivery protocol.
     */
    private String mDelivery;

    /**
     * The MIME type for the file container.
     */
    private String mType;

    /**
     * The average bitrate for the media file.
     */
    private BigInteger mBitrate;

    /**
     * The native width of the video file, in pixels.
     */
    private BigInteger mWidth;

    /**
     * The native height of the video file, in pixels.
     */
    private BigInteger mHeight;

    /**
     * Whether the media file is meant to scale to larger dimensions.
     */
    private boolean mScalable;

    /**
     * Whether the aspect ratio for media file dimensions should be maintained when scaled to
     * larger dimensions.
     */
    private boolean mMaintainAspectRatio;

    /**
     * The API needed to execute an interactive media file.
     */
    private String mApiFramework;

    /**
     * Constructor.
     *
     * @param mediaFileMap A map containing the data needed to create the media file.
     */
    public MediaFile(Map<String, Map> mediaFileMap) {

        if (mediaFileMap != null) {

            setValue(VmapHelper.getTextValueFromMap(mediaFileMap));
            Map<String, String> attributes = mediaFileMap.get(XmlParser.ATTRIBUTES_TAG);
            if (attributes != null) {
                setId(attributes.get(VmapHelper.ID_KEY));
                setDelivery(attributes.get(DELIVERY_KEY));
                setType(attributes.get(TYPE_KEY));
                setBitrate(BigInteger.valueOf(Long.valueOf(attributes.get(BITRATE_KEY))));
                setWidth(BigInteger.valueOf(Long.valueOf(attributes.get(WIDTH_KEY))));
                setHeight(BigInteger.valueOf(Long.valueOf(attributes.get(HEIGHT_KEY))));
                setScalable(Boolean.valueOf(attributes.get(SCALABLE_KEY)));
                setMaintainAspectRatio(Boolean.valueOf(attributes.get(MAINTAIN_ASPECT_RATIO_KEY)));
                setApiFramework(attributes.get(API_FRAMEWORK_KEY));
            }
        }
    }

    /**
     * Get the ad URL value.
     *
     * @return The value.
     */
    public String getValue() {

        return mValue;
    }

    /**
     * Set the value.
     *
     * @param value The value.
     */
    public void setValue(String value) {

        mValue = value;
    }

    /**
     * Get the id.
     *
     * @return The id.
     */
    public String getId() {

        return mId;
    }

    /**
     * Set the id.
     *
     * @param id The id.
     */
    public void setId(String id) {

        mId = id;
    }

    /**
     * Get the delivery.
     *
     * @return The delivery.
     */
    public String getDelivery() {

        return mDelivery;
    }

    /**
     * Set the delivery.
     *
     * @param delivery The delivery.
     */
    public void setDelivery(String delivery) {

        mDelivery = delivery;
    }

    /**
     * Get the type.
     *
     * @return The type.
     */
    public String getType() {

        return mType;
    }

    /**
     * Set the type.
     *
     * @param type The type.
     */
    public void setType(String type) {

        mType = type;
    }

    /**
     * Get the bitrate.
     *
     * @return The bitrate.
     */
    public BigInteger getBitrate() {

        return mBitrate;
    }

    /**
     * Set the bitrate.
     *
     * @param bitrate The bitrate.
     */
    public void setBitrate(BigInteger bitrate) {

        mBitrate = bitrate;
    }

    /**
     * Get the width.
     *
     * @return The width.
     */
    public BigInteger getWidth() {

        return mWidth;
    }

    /**
     * Set the width.
     *
     * @param width The width.
     */
    public void setWidth(BigInteger width) {

        mWidth = width;
    }

    /**
     * Get the height.
     *
     * @return The height.
     */
    public BigInteger getHeight() {

        return mHeight;
    }

    /**
     * Set the height.
     *
     * @param height The height.
     */
    public void setHeight(BigInteger height) {

        mHeight = height;
    }

    /**
     * Whether the media file is meant to scale to larger dimensions or not.
     *
     * @return True if it can be scaled, false otherwise.
     */
    public boolean isScalable() {

        return mScalable;
    }

    /**
     * Set the scalable boolean.
     *
     * @param scalable True if its meant to be scaled, false otherwise.
     */
    public void setScalable(boolean scalable) {

        mScalable = scalable;
    }

    /**
     * Whether the aspect ratio for media file dimensions should be maintained when scaled to new
     * dimensions or not.
     *
     * @return True if the aspect ratio should be maintained, false otherwise.
     */
    public boolean isMaintainAspectRatio() {

        return mMaintainAspectRatio;
    }

    /**
     * Set the maintain aspect ratio boolean.
     *
     * @param maintainAspectRatio True if the aspect ratio should be maintained, false otherwise.
     */
    public void setMaintainAspectRatio(boolean maintainAspectRatio) {

        mMaintainAspectRatio = maintainAspectRatio;
    }

    /**
     * Get the API framework needed to execute an interactive media file.
     *
     * @return The API framework.
     */
    public String getApiFramework() {

        return mApiFramework;
    }

    /**
     * Set the API framework needed to execute an interactive media file.
     *
     * @param apiFramework The API framework
     */
    public void setApiFramework(String apiFramework) {

        mApiFramework = apiFramework;
    }

    @Override
    public String toString() {

        return "MediaFile{" +
                "mValue='" + mValue + '\'' +
                ", mId='" + mId + '\'' +
                ", mDelivery='" + mDelivery + '\'' +
                ", mType='" + mType + '\'' +
                ", mBitrate=" + mBitrate +
                ", mWidth=" + mWidth +
                ", mHeight=" + mHeight +
                ", mScalable=" + mScalable +
                ", mMaintainAspectRatio=" + mMaintainAspectRatio +
                ", mApiFramework='" + mApiFramework + '\'' +
                '}';
    }
}
