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

import com.amazon.purchase.model.Response;

/**
 * Listener for calls made to the {@link PurchaseManager}.
 */
public interface PurchaseManagerListener {

    /**
     * Response call for {@link PurchaseManager#init(IPurchase, PurchaseManagerListener)}.
     *
     * @param response The response of the call.
     */
    void onRegisterSkusResponse(Response response);

    /**
     * Response call for {@link PurchaseManager#purchaseSku(String, PurchaseManagerListener)} and
     * {@link PurchaseManager#isPurchaseValid(String, PurchaseManagerListener)}.
     *
     * @param response The response of the call.
     * @param validity The validity of the purchase.
     * @param sku      The SKU of the item purchased.
     */
    void onValidPurchaseResponse(Response response, boolean validity, String sku);
}
