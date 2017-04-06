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
 * Copyright (c) 2014, Nexage, Inc. All rights reserved.
 * Copyright (C) 2016 Amazon Inc.
 *
 * Provided under BSD-3 license as follows:
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *  and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Nexage nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.amazon.android.ads.vast.model;

import java.math.BigInteger;

public class VASTMediaFile {

    private String value;
    private String id;
    private String delivery;
    private String type;
    private BigInteger bitrate;
    private BigInteger width;
    private BigInteger height;
    private Boolean scalable;
    private Boolean maintainAspectRatio;
    private String apiFramework;

    public String getValue() {

        return value;
    }

    public void setValue(String value) {

        this.value = value;
    }

    public String getId() {

        return id;
    }

    public void setId(String value) {

        this.id = value;
    }

    public String getDelivery() {

        return delivery;
    }

    public void setDelivery(String value) {

        this.delivery = value;
    }

    public String getType() {

        return type;
    }

    public void setType(String value) {

        this.type = value;
    }

    public BigInteger getBitrate() {

        return bitrate;
    }

    public void setBitrate(BigInteger value) {

        this.bitrate = value;
    }

    public BigInteger getWidth() {

        return width;
    }

    public void setWidth(BigInteger value) {

        this.width = value;
    }

    public BigInteger getHeight() {

        return height;
    }

    public void setHeight(BigInteger value) {

        this.height = value;
    }

    public Boolean isScalable() {

        return scalable;
    }

    public void setScalable(Boolean value) {

        this.scalable = value;
    }

    public Boolean isMaintainAspectRatio() {

        return maintainAspectRatio;
    }

    public void setMaintainAspectRatio(Boolean value) {

        this.maintainAspectRatio = value;
    }

    public String getApiFramework() {

        return apiFramework;
    }

    public void setApiFramework(String value) {

        this.apiFramework = value;
    }

    @Override
    public String toString() {

        return "MediaFile [value=" + value + ", id=" + id + ", delivery="
                + delivery + ", type=" + type + ", bitrate=" + bitrate
                + ", width=" + width + ", height=" + height + ", scalable="
                + scalable + ", maintainAspectRatio=" + maintainAspectRatio
                + ", apiFramework=" + apiFramework + "]";
    }

}