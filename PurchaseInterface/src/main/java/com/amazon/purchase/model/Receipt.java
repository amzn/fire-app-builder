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

import java.util.Date;

/**
 * Receipt class represents the purchase information of a product.
 */
public class Receipt {

    /**
     * The possible fulfillment statuses.
     */
    public enum FulfillmentStatus {
        /**
         * Fulfilled successfully.
         */
        FULFILLED,
        /**
         * Not supported.
         */
        UNAVAILABLE
    }

    private String mReceiptId;
    private String mSku;
    private Product.ProductType mProductType;
    private Date mPurchasedDate;
    private Date mExpiryDate;

    /**
     * Gets the receipt id.
     *
     * @return The receipt id.
     */
    public String getReceiptId() {

        return mReceiptId;
    }

    /**
     * Sets the receipt id.
     *
     * @param receiptId The receipt id.
     */
    public void setReceiptId(String receiptId) {

        mReceiptId = receiptId;
    }

    /**
     * Gets the SKU.
     *
     * @return The SKU.
     */
    public String getSku() {

        return mSku;
    }

    /**
     * Sets the SKU
     *
     * @param sku The SKU.
     */
    public void setSku(String sku) {

        mSku = sku;
    }

    /**
     * Gets the purchased date.
     *
     * @return The purchased date.
     */
    public Date getPurchasedDate() {

        return mPurchasedDate;
    }

    /**
     * Sets the purchased date.
     *
     * @param purchasedDate The purchased date.
     */
    public void setPurchasedDate(Date purchasedDate) {

        mPurchasedDate = purchasedDate;
    }

    /**
     * Gets the expiry date.
     *
     * @return The expiry date.
     */
    public Date getExpiryDate() {

        return mExpiryDate;
    }

    /**
     * Sets the expiry date.
     *
     * @param expiryDate The expiry date.
     */
    public void setExpiryDate(Date expiryDate) {

        mExpiryDate = expiryDate;
    }

    /**
     * Gets the product type.
     *
     * @return The product type.
     */
    public Product.ProductType getProductType() {

        return mProductType;
    }

    /**
     * Sets the product type.
     *
     * @param productType The product type.
     */
    public void setProductType(Product.ProductType productType) {

        mProductType = productType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof Receipt)) return false;

        Receipt receipt = (Receipt) o;

        if (getReceiptId() != null ? !getReceiptId().equals(receipt.getReceiptId()) : receipt
                .getReceiptId() != null)
            return false;
        if (getSku() != null ? !getSku().equals(receipt.getSku()) : receipt.getSku() != null)
            return false;
        if (getProductType() != receipt.getProductType()) return false;
        if (getPurchasedDate() != null ? !getPurchasedDate().equals(receipt.getPurchasedDate()) :
                receipt.getPurchasedDate() != null)
            return false;
        return !(getExpiryDate() != null ? !getExpiryDate().equals(receipt.getExpiryDate()) :
                receipt
                        .getExpiryDate() != null);

    }

    /**
     * Custom hashcode to support equate at parameter level instead of instance level.
     *
     * @return The hashcode of the instance.
     */
    @Override
    public int hashCode() {

        int result = getReceiptId() != null ? getReceiptId().hashCode() : 0;
        result = 31 * result + (getSku() != null ? getSku().hashCode() : 0);
        result = 31 * result + (getProductType() != null ? getProductType().hashCode() : 0);
        result = 31 * result + (getPurchasedDate() != null ? getPurchasedDate().hashCode() : 0);
        result = 31 * result + (getExpiryDate() != null ? getExpiryDate().hashCode() : 0);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return "Receipt{" +
                "mReceiptId='" + mReceiptId + '\'' +
                ", mSku='" + mSku + '\'' +
                ", mPurchasedDate=" + mPurchasedDate +
                ", mExpiryDate=" + mExpiryDate +
                '}';
    }
}
