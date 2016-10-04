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
package com.amazon.inapppurchase;

import com.amazon.purchase.IPurchase;
import com.amazon.purchase.model.Receipt;
import com.amazon.purchase.model.Response;
import com.amazon.purchase.model.UserData;

import android.content.Context;

/**
 * Default implementation of {@link AReceiptVerifier} interface that directly calls the
 * {@link DefaultReceiptVerificationService}. The developer should have their own service that
 * calls the ReceiptVerificationService from the app.
 */
public class DefaultReceiptVerificationService extends AReceiptVerifier {

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateReceipt(Context context, String requestId, String sku, UserData
            userData, Receipt receipt, IPurchase.PurchaseListener listener) {

        Response purchaseResponse = new Response(requestId, Response.Status.SUCCESSFUL, null);
        listener.isPurchaseValidResponse(purchaseResponse, sku, receipt, true, userData);
        return requestId;
    }

    /**
     * Creates an instance of {@link AReceiptVerifier}.
     *
     * @return An instance of {@link AReceiptVerifier}.
     */
    public static AReceiptVerifier createInstance(Context context) {

        return new DefaultReceiptVerificationService();
    }
}
