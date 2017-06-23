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
package com.amazon.ads;

/**
 * Class to hold Ad metadata.
 */
public class AdMetaData {

    /**
     * Ad ID.
     */
    private String adId = "";

    /**
     * Duration of ad played on screen.
     */
    private long durationPlayed;

    /**
     * Duration of ad received in ad meta data.
     */
    private long durationReceived;

    /**
     * Ad Type: Pre roll, Mid roll, Post roll
     */
    private String adType = "";

    /**
     * set id of current ad.
     *
     * @param adId current ad id received in ad metadata.
     */
    public void setAdId(String adId) {

        this.adId = adId;
    }

    /**
     * set duration of current ad played on screen.
     *
     * @param durationPlayed duration of ad played on screen
     */
    public void setDurationPlayed(long durationPlayed) {

        this.durationPlayed = durationPlayed;
    }

    /**
     * set duration of ad received in ad meta data.
     *
     * @param durationReceived duration of ad received in ad meta data
     */
    public void setDurationReceived(long durationReceived) {

        this.durationReceived = durationReceived;
    }

    /**
     * set ad type in ad meta data.
     *
     * @param adType pre, mid or post roll
     */
    public void setAdType(String adType) {

        this.adType = adType;
    }

    /**
     * Return the ad Id.
     *
     * @return id of current ad.
     */
    public String getAdId() {

        return adId;
    }

    /**
     * Return the duration played of current ad.
     *
     * @return duration of ad played on screen.
     */
    public long getDurationPlayed() {

        return durationPlayed;
    }

    /**
     * Return the duration received from ad module.
     *
     * @return duration of ad as received from ad meta data.
     */
    public long getDurationReceived() {

        return durationReceived;
    }

    /**
     * Return the ad type.
     *
     * @return type of current ad.
     */
    public String getAdType() {

        return adType;
    }

    @Override
    public String toString() {

        return "AdMetaData{" +
                "adId='" + adId + '\'' +
                ", durationPlayed=" + durationPlayed +
                ", durationReceived=" + durationReceived +
                ", adType='" + adType + '\'' +
                '}';
    }
}