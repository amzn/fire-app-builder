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
package com.amazon.purchase.testResources;

import com.amazon.purchase.IPurchase;
import com.amazon.purchase.model.Receipt;
import com.amazon.purchase.model.Response;
import com.amazon.purchase.model.UserData;

import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Mock implementation of the {@link IPurchase} interface.
 */
public class MockIPurchaseImpl implements IPurchase {

    public static MockIPurchaseImpl sMockIPurchase;
    public PurchaseListener listener;
    public Context context;

    @Override
    public void init(Context context, Bundle extras) {

        this.context = context;
    }

    @Override
    public void registerDefaultPurchaseListener(PurchaseListener listener) {

        this.listener = listener;
    }

    @Override
    public String getUserData() {

        return "getUserData";
    }

    @Override
    public String getProducts(Set<String> skuSet) {

        return "getProducts";
    }


    @Override
    public String getUserPurchaseData(boolean reset) {

        List<Receipt> list = new ArrayList<>();
        list.addAll(TestUtils.receiptMap.values());
        Response response = listenerResponse();
        listener.onUserDataResponse(response, list, TestUtils.userData, false);
        return "getUserPurchaseDataSuccess";
    }

    public Response listenerResponse() {

        return new Response("getUserPurchaseDataSuccess", Response.Status.SUCCESSFUL,
                            null);
    }


    @Override
    public String purchase(String sku) {

        Receipt receipt = TestUtils.createReceipt(sku, "title", new Date(), null);
        listener.onPurchaseResponse(purchaseListenerResponse(), sku, receipt, TestUtils.userData);
        return "purchaseRequest";
    }

    @Override
    public String isPurchaseValid(String sku, UserData userData, Receipt receipt) {

        listener.isPurchaseValidResponse(listenerResponse(), sku, receipt, receiptValidResponse(),
                                         TestUtils.userData);
        return "isPurchaseValidRequest";
    }

    public boolean receiptValidResponse() {

        return true;
    }

    @Override
    public void notifyFulfillment(String sku, UserData userData, Receipt receipt,
                                  Receipt.FulfillmentStatus fulfillmentResult) {

    }

    public Response purchaseListenerResponse() {

        return new Response("getUserPurchaseDataSuccess", Response.Status.SUCCESSFUL, null);
    }
}
