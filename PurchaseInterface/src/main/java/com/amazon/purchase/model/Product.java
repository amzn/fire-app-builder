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

import android.os.Bundle;

/**
 * A product is a single entity identified by a SKU (unique string) that can be bought via the app
 * from the purchase platform.
 *
 * There are 3 types of products:
 * {@link com.amazon.purchase.model.Product.ProductType#BUY} : This product is supposed to be 'buy
 * once, consume everywhere'. It can be bought from any device owned by the user and consumed on
 * all devices owned by the user like tablet, TV, etc. Once bought, this product is owned by user
 * forever.
 *
 * {@link com.amazon.purchase.model.Product.ProductType#RENT} : This product is rented for a
 * specific period of time and is available only on the device from which the rent request was
 * sent.
 *
 * {@link  com.amazon.purchase.model.Product.ProductType#SUBSCRIBE} : This product is consumed
 * based on a recurring subscription
 */
public class Product {

    /**
     * Enum for possible product types.
     */
    public enum ProductType {
        RENT("RENT"),
        BUY("BUY"),
        SUBSCRIBE("SUBSCRIBE");

        String productType;

        ProductType(String productType) {

            this.productType = productType;
        }
    }

    private String mSku;
    private String mPrice;
    private String mTitle;
    private String mDescription;
    private String mIconUrl;
    private Bundle mExtras;
    private ProductType mProductType;

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
     * Gets the price.
     *
     * @return The price.
     */
    public String getPrice() {

        return mPrice;
    }

    /**
     * Sets the price.
     *
     * @param price The price.
     */
    public void setPrice(String price) {

        mPrice = price;
    }

    /**
     * Gets the title.
     *
     * @return The title.
     */
    public String getTitle() {

        return mTitle;
    }

    /**
     * Sets the title.
     *
     * @param title The title.
     */
    public void setTitle(String title) {

        mTitle = title;
    }

    /**
     * Gets the description.
     *
     * @return The description.
     */
    public String getDescription() {

        return mDescription;
    }

    /**
     * Sets the description.
     *
     * @param description The description.
     */
    public void setDescription(String description) {

        mDescription = description;
    }

    /**
     * Gets the icon URL.
     *
     * @return The icon URL.
     */
    public String getIconUrl() {

        return mIconUrl;
    }

    /**
     * Sets the icon URL.
     *
     * @param iconUrl The icon URL.
     */
    public void setIconUrl(String iconUrl) {

        mIconUrl = iconUrl;
    }

    /**
     * Gets the extras bundle.
     *
     * @return The extras bundle.
     */
    public Bundle getExtras() {

        return mExtras;
    }

    /**
     * Sets the extras bundle.
     *
     * @param extras The bundle.
     */
    public void setExtras(Bundle extras) {

        mExtras = extras;
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
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof Product)) return false;

        Product product = (Product) o;

        if (getSku() != null ? !getSku().equals(product.getSku()) : product.getSku() != null)
            return false;
        if (getPrice() != null ? !getPrice().equals(product.getPrice()) : product.getPrice() !=
                null)
            return false;
        if (getTitle() != null ? !getTitle().equals(product.getTitle()) : product.getTitle() !=
                null)
            return false;
        if (getDescription() != null ? !getDescription().equals(product.getDescription()) :
                product.getDescription() != null)
            return false;
        if (getIconUrl() != null ? !getIconUrl().equals(product.getIconUrl()) : product
                .getIconUrl() != null)
            return false;
        if (getExtras() != null ? !getExtras().equals(product.getExtras()) : product.getExtras()
                != null)
            return false;
        return getProductType() == product.getProductType();

    }

    /**
     * Custom hashcode to support equate at parameter level instead of instance level.
     *
     * @return The hashcode of the instance.
     */
    @Override
    public int hashCode() {

        int result = getSku() != null ? getSku().hashCode() : 0;
        result = 31 * result + (getPrice() != null ? getPrice().hashCode() : 0);
        result = 31 * result + (getTitle() != null ? getTitle().hashCode() : 0);
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + (getIconUrl() != null ? getIconUrl().hashCode() : 0);
        result = 31 * result + (getExtras() != null ? getExtras().hashCode() : 0);
        result = 31 * result + (getProductType() != null ? getProductType().hashCode() : 0);
        return result;
    }
}
