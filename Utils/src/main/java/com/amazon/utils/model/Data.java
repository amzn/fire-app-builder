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
package com.amazon.utils.model;

import android.support.annotation.NonNull;

/**
 * Data structure used to carry the raw data received from external sources.
 * Besides the data itself, it also provides member variables to represent different
 * characteristics
 * of the data transfer, e.g. if the data is complete and time at which data transfer was complete
 * The class itself does not enforce any variable as mandatory.
 * The equals and hash methods are based on actual values of the variables, hence 2 instances are
 * equal in every manner if all there variables are equals
 */
public class Data {

    /**
     * Data structure to represent the raw data. It also includes member variables to represent
     * different characteristics of the data like payload size, type of data etc.
     * The class itself does not enforce any variable as mandatory.
     * The equals and has methods are based on actual values of the variables, hence 2 instances
     * are equals in every manner if all there variables are equals.
     */
    public static class Record {

        /**
         * Raw data in string format.
         */
        private String mPayload;
        /**
         * Data type of the data.
         */
        private DataType mDataType;
        /**
         * Hash function of content to validate that content received is not corrupt.
         */
        private String mHashValue;
        /**
         * Size of content in bytes.
         */
        private long mPayloadSizeInBytes;

        /**
         * Getter for payload, the raw data in string format.
         *
         * @return The payload, the raw data in string format.
         */
        public String getPayload() {

            return mPayload;
        }

        /**
         * Setter for payload.
         *
         * @param payload Value of payload, the raw data in string format.
         */
        public void setPayload(String payload) {

            this.mPayload = payload;
        }

        /**
         * Getter for Data type of payload.
         *
         * @return {@link DataType} of payload.
         */
        public DataType getDataType() {

            return mDataType;
        }

        /**
         * Setter for Data type of payload.
         *
         * @param dataType {@link DataType} of payload.
         */
        public void setDataType(DataType dataType) {

            this.mDataType = dataType;
        }

        /**
         * Getter for hash value of payload, this is useful in making sure the payload is not
         * corrupt.
         *
         * @return Hash value of payload.
         */
        public String getHashValue() {

            return mHashValue;
        }

        /**
         * Setter for hash value of payload, this is useful in making sure the payload is not
         * corrupt.
         *
         * @param hashValue Hash value of payload.
         */
        public void setHashValue(String hashValue) {

            this.mHashValue = hashValue;
        }

        /**
         * Getter for payload size, the size is in bytes.
         *
         * @return Payload size in bytes.
         */
        public long getPayloadSizeInBytes() {

            return mPayloadSizeInBytes;
        }

        /**
         * Setter for payload size, the size is in bytes.
         *
         * @param payloadSizeInBytes Payload size in bytes.
         */
        public void setPayloadSizeInBytes(long payloadSizeInBytes) {

            this.mPayloadSizeInBytes = payloadSizeInBytes;
        }

        /**
         * {@inheritDoc}
         * Overriding the equals method to equate based on the variables values instead of the hash
         * value of the object. None of the variables are enforced to be mandatory.
         *
         * @param o Object to equate against
         * @return True if equal; false otherwise.
         */
        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Record record = (Record) o;

            if ((getHashValue() != null ? !getHashValue().equals(record.getHashValue()) :
                    record.getHashValue() != null))
                return false;
            if (getPayload() != null ? !getPayload().equals(record.getPayload()) : record
                    .getPayload() != null)
                return false;
            if (getDataType() != record.getDataType()) return false;
            return (getPayloadSizeInBytes() == record.getPayloadSizeInBytes());

        }

        /**
         * {@inheritDoc}
         * Overriding the default has code to return a value based on actual values of the
         * variables. Uses the standard algorithm of Intellij for calculating the hash code.
         *
         * @return The hashcode the instance.
         */
        @Override
        public int hashCode() {

            int result = getPayload() != null ? getPayload().hashCode() : 0;
            result = 31 * result + (getDataType() != null ? getDataType().hashCode() : 0);
            result = 31 * result + (getHashValue() != null ? getHashValue().hashCode() : 0);
            result = 31 * result + (int) (getPayloadSizeInBytes() ^ (getPayloadSizeInBytes() >>>
                    32));
            return result;
        }

        /**
         * Json conversion of Record.
         * {@inheritDoc}
         */
        @Override
        public String toString() {

            return "{\n" +
                    "\"mPayload\" :\"" + mPayload + "\",\n" +
                    "\"mDataType\" :\"" + mDataType + "\",\n" +
                    "\"mHashValue\" :\"" + mHashValue + "\",\n" +
                    "\"mPayloadSizeInBytes\" :\"" + mPayloadSizeInBytes + "\"\n" +
                    "}";
        }
    }

    /**
     * Enum to represent type of Data.
     */
    public enum DataType {
        /**
         * The data is in JSON representation.
         */
        JSON,
        /**
         * The data is in XML representation.
         */
        XML,
        /**
         * Raw type, can be used if the data type is not available.
         */
        RAW_TYPE
    }

    /**
     * Unique id for each data transfer request.
     */
    private String mRequestId;
    /**
     * Raw content that needs to be transferred.
     */
    private Record mContent;
    /**
     * Any metadata received with the data transfer request.
     */
    private Record mMetadata;
    /**
     * Time at which the content download was finished.
     */
    private long mDownloadedTimeInMs;
    /**
     * Is this data complete or part of a batch?
     */
    private boolean mIsComplete;

    /**
     * Getter for content variable, Raw content that needs to be transferred.
     *
     * @return Content corresponding to this object.
     */
    public Record getContent() {

        return mContent;
    }

    /**
     * Setter for content variable, Raw content that needs to be transferred.
     *
     * @param content Content corresponding to this object.
     */
    public void setContent(Record content) {

        this.mContent = content;
    }

    /**
     * Getter for requestId corresponding to this data transfer request.
     *
     * @return The requestId corresponding to this data transfer request.
     */
    public String getRequestId() {

        return mRequestId;
    }

    /**
     * Setter for requestId corresponding to this data transfer request.
     *
     * @param requestId The requestId corresponding to this data transfer request.
     */
    public void setRequestId(String requestId) {

        this.mRequestId = requestId;
    }

    /**
     * Getter for any metadata connected with the data.
     *
     * @return Any metadata connected with the data.
     */
    public Record getMetadata() {

        return mMetadata;
    }

    /**
     * Setter for any metadata connected with the data.
     *
     * @param metadata Any metadata connected with the data
     */
    public void setMetadata(Record metadata) {

        this.mMetadata = metadata;
    }

    /**
     * Getter for download time of the data.
     *
     * @return The download time of the data.
     */
    public long getDownloadedTimeInMs() {

        return mDownloadedTimeInMs;
    }

    /**
     * Setter for download time of the data.
     *
     * @param downloadedTimeInMs The download time of the data.
     */
    public void setDownloadedTimeInMs(long downloadedTimeInMs) {

        this.mDownloadedTimeInMs = downloadedTimeInMs;
    }

    /**
     * Returns true if the data is complete and false if it is part of chain of data transfers.
     *
     * @return If the data is complete or not.
     */
    public boolean isComplete() {

        return mIsComplete;
    }

    /**
     * Returns true if the data is complete and false if it is part of chain of data transfers.
     *
     * @param isComplete If the data is complete or not.
     */
    public void setIsComplete(boolean isComplete) {

        this.mIsComplete = isComplete;
    }

    /**
     * {@inheritDoc}
     * Overriding the equals method to equate based on the variables values instead of the hash
     * value of the object. The downloaded time is ignored for equation. None of the variables are
     * enforced to be mandatory
     *
     * @param o Object to equate against.
     * @return True if equal; false otherwise.
     */
    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof Data)) return false;

        Data data = (Data) o;

        if (getRequestId() != null ? !getRequestId().equals(data.getRequestId()) : data
                .getRequestId() != null)
            return false;
        if (getContent() != null ? !getContent().equals(data.getContent()) : data.getContent() !=
                null)
            return false;
        if (isComplete() != data.isComplete()) return false;
        return !(getMetadata() != null ? !getMetadata().equals(data.getMetadata()) : data
                .getMetadata() != null);

    }

    /**
     * {@inheritDoc}
     * Overriding the default hash code to return a value based on actual values of the variables
     * The downloaded time is ignored for equation. Uses the standard algorithm of Intellij for
     * calculating the hash code
     *
     * @return The hashcode of the instance.
     */
    @Override
    public int hashCode() {

        int result = getRequestId() != null ? getRequestId().hashCode() : 0;
        result = 31 * result + (getContent() != null ? getContent().hashCode() : 0);
        result = 31 * result + (getMetadata() != null ? getMetadata().hashCode() : 0);
        result = 31 * result + (isComplete() ? 1 : 0);
        return result;
    }

    /**
     * Json conversion of Data.
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return "{\n" +
                "\"mRequestId\" : \"" + mRequestId + "\",\n" +
                "\"mContent\" : " + (mContent == null ? "\"null\"" : mContent.toString()) + ",\n" +
                "\"mMetadata\" : " + (mMetadata == null ? "\"null\"" : mMetadata.toString()) + "," +
                "\n" + "\"mDownloadedTimeInMs\" : \"" + mDownloadedTimeInMs + "\",\n" +
                "\"mIsComplete\" : \"" + mIsComplete + "\"\n}";
    }

    /**
     * Creates basic data object with payload received from the URL.
     *
     * @param payload The payload to add to Data.
     * @return Data object created.
     */
    @NonNull
    public static Data createDataForPayload(String payload) {

        Data data = new Data();
        Record record = new Record();
        record.setPayload(payload);
        data.setContent(record);
        data.setIsComplete(true);
        return data;
    }

}


