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

import com.amazon.purchase.model.Product.ProductType;


/**
 * This class provides the structure for basic {@link Product} data that is required to register a
 * SKU.
 */
public class SkuData {

    private String mSku;
    private ProductType mProductType;
    private String mPurchaseSku;

    /**
     * Constructor.
     *
     * @param sku         SKU to which this data object belongs.
     * @param productType Product type of this product.
     * @param purchaseSku Purchase SKU.
     */
    public SkuData(String sku, ProductType productType, String purchaseSku) {

        mSku = sku;
        mProductType = productType;
        mPurchaseSku = purchaseSku;
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
     * Sets the SKU.
     *
     * @param sku The SKU.
     */
    public void setSku(String sku) {

        mSku = sku;
    }

    /**
     * Gets the product type.
     *
     * @return The product type.
     */
    public ProductType getProductType() {

        return mProductType;
    }

    /**
     * Sets the product type.
     *
     * @param productType The product type.
     */
    public void setProductType(ProductType productType) {

        mProductType = productType;
    }

    /**
     * Sets the purchase SKU.
     *
     * @param purchaseSku The purchase SKU.
     */
    public void setPurchaseSku(String purchaseSku) {

        mPurchaseSku = purchaseSku;
    }

    /**
     * Gets the purchase SKU
     *
     * @return The purchase SKU.
     */
    public String getPurchaseSku() {

        return mPurchaseSku;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof SkuData)) return false;

        SkuData skuData = (SkuData) o;

        if (getSku() != null ? !getSku().equals(skuData.getSku()) : skuData.getSku() != null)
            return false;
        if (getProductType() != skuData.getProductType()) return false;
        return !(getPurchaseSku() != null ? !getPurchaseSku().equals(skuData.getPurchaseSku()) :
                skuData.getPurchaseSku() != null);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        int result = getSku() != null ? getSku().hashCode() : 0;
        result = 31 * result + (getProductType() != null ? getProductType().hashCode() : 0);
        result = 31 * result + (getPurchaseSku() != null ? getPurchaseSku().hashCode() : 0);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return "SkuData{" +
                "mSku='" + mSku + '\'' +
                ", mProductType=" + mProductType +
                ", mPurchaseSku='" + mPurchaseSku + '\'' +
                '}';
    }
}
