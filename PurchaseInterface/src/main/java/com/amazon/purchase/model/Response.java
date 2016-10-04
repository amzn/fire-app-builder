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
package com.amazon.purchase.model;

/**
 * Response class that represents the generic parameters for a response from a request.
 */
public class Response {

    /**
     * Request completion status.
     */
    public enum Status {
        SUCCESSFUL,
        FAILED,
        NOT_SUPPORTED,
        BLOCKED
    }

    private String mRequestId;
    private Status mStatus;
    private Throwable mThrowable;

    /**
     * Constructor.
     */
    public Response(String requestId, Status status, Throwable throwable) {

        this.mStatus = status;
        this.mRequestId = requestId;
        this.mThrowable = throwable;
    }

    /**
     * Gets the request id.
     *
     * @return The request id.
     */
    public String getRequestId() {

        return mRequestId;
    }

    /**
     * Sets the request id.
     *
     * @param requestId The request id.
     */
    public void setRequestId(String requestId) {

        mRequestId = requestId;
    }

    /**
     * Gets the status.
     *
     * @return The status.
     */
    public Status getStatus() {

        return mStatus;
    }

    /**
     * Sets the status.
     *
     * @param status The status.
     */
    public void setStatus(Status status) {

        mStatus = status;
    }

    /**
     * Gets the throwable.
     *
     * @return The throwable.
     */
    public Throwable getThrowable() {

        return mThrowable;
    }

    /**
     * Sets the throwable.
     *
     * @param throwable The throwable.
     */
    public void setThrowable(Throwable throwable) {

        mThrowable = throwable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof Response)) return false;

        Response response = (Response) o;

        if (getRequestId() != null ? !getRequestId().equals(response.getRequestId()) :
                response.getRequestId() != null)
            return false;
        if (getStatus() != response.getStatus()) return false;
        return getThrowable() != null ? getThrowable().equals(response.getThrowable()) :
                response.getThrowable() == null;

    }

    /**
     * Custom hashcode to support equate at parameter level instead of instance level.
     *
     * @return The hashcode of the instance.
     */
    @Override
    public int hashCode() {

        int result = getRequestId() != null ? getRequestId().hashCode() : 0;
        result = 31 * result + (getStatus() != null ? getStatus().hashCode() : 0);
        result = 31 * result + (getThrowable() != null ? getThrowable().hashCode() : 0);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return "Response{" +
                "mRequestId='" + mRequestId + '\'' +
                ", mStatus=" + mStatus +
                ", mThrowable=" + mThrowable +
                '}';
    }
}
