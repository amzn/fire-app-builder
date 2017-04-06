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
import com.amazon.purchase.model.UserData;

import android.content.Context;

/**
 * Interface for verifying receipts.
 */
public abstract class AReceiptVerifier {

    /**
     * Name of the method used to create an instance of AReceiptVerifier.
     */
    public static final String CREATE_INSTANCE_METHOD_NAME = "createInstance";

    /**
     * This method should provide an instance of the class. Extensions classes must override this
     * method to provide and instance of the class.
     *
     * @param context The application context.
     * @return An instance of this class.
     */
    public static AReceiptVerifier createInstance(Context context) {

        throw new IllegalStateException("createInstance not implemented by this implementation " +
                                                "class");
    }

    /**
     * Validates the receipt with an external service.
     *
     * @param context   The application context.
     * @param requestId The request id for this request.
     * @param sku       SKU for the receipt.
     * @param userData  User data of the current user.
     * @param receipt   The receipt to validate.
     * @param listener  The listener of this request.
     * @return The request id.
     */
    public abstract String validateReceipt(Context context, String requestId, String sku, UserData
            userData, Receipt receipt, IPurchase.PurchaseListener listener);
}