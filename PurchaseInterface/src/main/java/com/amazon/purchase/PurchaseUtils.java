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
package com.amazon.purchase;

import com.amazon.android.recipe.Recipe;
import com.amazon.android.utils.FileHelper;
import com.amazon.purchase.model.Product;
import com.amazon.purchase.model.Receipt;
import com.amazon.purchase.model.SkuData;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.amazon.utils.DateAndTimeHelper.addSeconds;
import static com.amazon.utils.DateAndTimeHelper.compareDates;
import static com.amazon.utils.DateAndTimeHelper.getCurrentDate;

/**
 * Util methods for the purchasing system.
 */
public class PurchaseUtils {

    /**
     * The string constant for the list of SKUs.
     */
    public static final String SKUS_LIST = "skusList";

    /**
     * The string constant for the product type.
     */
    public static final String PRODUCT_TYPE = "productType";

    /**
     * The string constant for the SKU.
     */
    public static final String SKU = "sku";

    /**
     * The string constant for the purchase SKU.
     */
    public static final String PURCHASE_SKU = "purchaseSku";

    /**
     * Status of purchase update request.
     */
    public enum PurchaseUpdateStatus {
        NOT_STARTED,
        ON_GOING,
        COMPLETED,
        FAILED
    }

    /**
     * Class to present pending actions.
     */
    static class PendingAction {

        /**
         * Constructor.
         *
         * @param sku      SKU belonging to this action.
         * @param listener The listener to be registered for this action call.
         * @param action   The action to perform.
         */
        public PendingAction(String sku, PurchaseManagerListener listener, ACTION action) {

            this.sku = sku;
            this.listener = listener;
            this.action = action;
        }

        /**
         * Possible actions from the purchase manager.
         */
        enum ACTION {
            PURCHASE,
            IS_PURCHASE_VALID
        }

        ACTION action;
        String sku;
        PurchaseManagerListener listener;
    }

    private static final String TAG = PurchaseUtils.class.getName();

    /**
     * Reads a SKU set from SKU configuration file.
     *
     * @param context The application context.
     * @return A SKU set.
     * @throws IOException if there was an error reading the list of SKUs from file.
     */
    public List<Map<String, String>> readSkusFromConfigFile(Context context) throws IOException {

        Recipe recipe =
                Recipe.newInstance(FileHelper.readFile(context,
                                                       context.getString(R.string.skus_file)));
        if (!recipe.containsItem(SKUS_LIST)) {
            throw new IllegalStateException("sku list file does not contain skulist");
        }
        return (List<Map<String, String>>) recipe.getMap().get(SKUS_LIST);
    }

    /**
     * Determines if this SKU is rented.
     *
     * @param skuData The SKU to be checked.
     * @return True if the product is rented; false otherwise.
     */
    public boolean isProductRented(SkuData skuData) {

        return skuData != null && Product.ProductType.RENT.equals(skuData.getProductType());
    }

    /**
     * Evaluates a receipt to determine if the rental period is over for this receipt.
     *
     * @param context The context.
     * @param receipt The receipt to check.
     * @return True if the rental period is over; false otherwise.
     */
    public boolean isRentalExpired(Context context, Receipt receipt) {

        int rentSeconds = context.getResources().getInteger(R.integer.default_rent_seconds);
        Date expiryDate = addSeconds(receipt.getPurchasedDate(), rentSeconds);
        Log.d(TAG, "expiry date " + expiryDate + " purchase date " + receipt.getPurchasedDate());
        return compareDates(expiryDate, getCurrentDate());
    }

    /**
     * Determines if this receipt is passed its expiry date.
     *
     * @param receipt Receipt to check.
     * @return True if this receipt is expired; false otherwise.
     */
    public boolean isReceiptExpired(@NonNull Receipt receipt) {

        return receipt.getExpiryDate() != null && compareDates(receipt.getExpiryDate(),
                                                               getCurrentDate());
    }

    /**
     * Takes a list of SKU data and converts it into map.
     *
     * @param skuSet The SKU list to convert.
     * @return The map of SKU data.
     */
    public Map updateSkuSet(List<Map<String, String>> skuSet) {

        Map skuDataMap = new HashMap<>();
        Log.d(TAG, "skus to be registered " + skuSet);
        for (Map<String, String> skuData : skuSet) {
            Product.ProductType productType = Product.ProductType.valueOf(skuData.get
                    (PRODUCT_TYPE));
            String sku = skuData.get(SKU);
            String purchaseSku = skuData.get(PURCHASE_SKU);
            if (sku != null && productType != null) {
                skuDataMap.put(sku, new SkuData(sku, productType, purchaseSku));
            }
        }
        return skuDataMap;
    }
}
