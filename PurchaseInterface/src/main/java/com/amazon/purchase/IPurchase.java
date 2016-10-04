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

import com.amazon.purchase.model.Product;
import com.amazon.purchase.model.Receipt;
import com.amazon.purchase.model.Response;
import com.amazon.purchase.model.UserData;

import android.content.Context;
import android.os.Bundle;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for purchase systems.
 */
public interface IPurchase {

    /**
     * Method to init this instance.
     *
     * @param context The application context.
     * @param extras  Any extra params.
     */
    void init(Context context, Bundle extras);

    /**
     * Registers the {@link PurchaseListener} for all purchase calls.
     *
     * @param listener The purchase listener to register as the default listener.
     */
    void registerDefaultPurchaseListener(PurchaseListener listener);

    /**
     * Initiates a request to retrieve the user details of the currently logged-in user. The
     * response is delivered via the {@link PurchaseListener#onGetUserDataResponse(Response,
     * UserData)}.
     * The implementation will define if this method call is synchronous or asynchronous.
     *
     * @return The request id for this method call.
     */
    String getUserData();

    /**
     * Initiates a request to fetch the product details for the given set of product SKUs. The
     * response is delivered via the {@link PurchaseListener#onProductDataResponse}.
     * The implementation will define if this method call is synchronous or asynchronous and any
     * limitations on the SKUs.
     *
     * @param skuSet A set of SKUs that require product details.
     * @return The request id for this method call.
     */
    String getProducts(Set<String> skuSet);

    /**
     * Initiates a request to retrieve purchase updates for the user. Set the reset flag parameter
     * to true to start the request from scratch; keep it false to continue from previous request.
     *
     * @param reset Flag to indicate if the request needs ot be started from scratch or continue
     *              from previous request.
     * @return The request id for this method call.
     */
    String getUserPurchaseData(boolean reset);

    /**
     * Initiates a purchase-flow for a product. The response is delivered via the {@link
     * PurchaseListener#onPurchaseResponse}.
     *
     * @param sku SKU to be purchased.
     * @return The request id for this method call.
     */
    String purchase(String sku);

    /**
     * Initiates a flow to validate the receipt. The response is delivered via {@link
     * PurchaseListener#isPurchaseValidResponse(Response, String, Receipt, boolean, UserData)}.
     *
     * @param sku      The SKU to be validated.
     * @param userData The user that the receipt belongs to.
     * @param receipt  The receipt to be validated.
     * @return The request id.
     */
    String isPurchaseValid(String sku, UserData userData, Receipt receipt);

    /**
     * Initiates a request to inform the purchase system about the fulfillment status of the
     * purchased product. The purchase system will mark the purchase completed or canceled based on
     * this call.
     *
     * @param sku               The SKU to be validated.
     * @param userData          The user that the receipt belongs to.
     * @param receipt           The receipt to be validated.
     * @param fulfillmentResult If the product was successfully consumed or not.
     */
    void notifyFulfillment(String sku, UserData userData, Receipt receipt, Receipt
            .FulfillmentStatus fulfillmentResult);

    /**
     * Interface for listening to purchase updates.
     */
    interface PurchaseListener {

        /**
         * Response handler for {@link #getUserData()}.
         *
         * @param response Response of this request.
         * @param userData user data of the user currently logged in.
         */
        void onGetUserDataResponse(Response response, UserData userData);

        /**
         * Response handler for {@link #onProductDataResponse(Response, Map, Set)}.
         *
         * @param response       Response of this request.
         * @param productDetails Map of SKUs vs their corresponding product details.
         * @param invalidSkus    Set of invalid SKUs that do not exist in the system.
         */
        void onProductDataResponse(Response response, Map<String, Product>
                productDetails, Set<String> invalidSkus);

        /**
         * Response handler for {@link #getUserData}.
         *
         * @param response    Response of this request.
         * @param receiptList List of purchase receipts of the user.
         * @param userData    User data of the currently logged in user.
         * @param hasMore     If there is more data or not.
         */
        void onUserDataResponse(Response response, List<Receipt> receiptList, UserData userData,
                                boolean hasMore);

        /**
         * Response handler for {@link #purchase(String)}.
         *
         * @param response Response of this request.
         * @param sku      SKU of the product purchased.
         * @param receipt  Receipt of the purchase.
         * @param userData User data of the user associated with the purchase.
         */
        void onPurchaseResponse(Response response, String sku, Receipt receipt, UserData userData);

        /**
         * Response handler for {@link #isPurchaseValid(String, UserData, Receipt)}.
         *
         * @param response      Response of this request.
         * @param sku           SKU of the product purchased.
         * @param receipt       Receipt of the purchase.
         * @param purchaseValid If the purchase is valid or not.
         * @param userData      User data of the user associated with the purchase.
         */
        void isPurchaseValidResponse(Response response, String sku, Receipt receipt, boolean
                purchaseValid, UserData userData);
    }

}
