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

import com.amazon.purchase.model.Receipt;
import com.amazon.purchase.model.Response;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Class to trigger the purchase call.
 * The {@link #doInBackground} triggers the call and saves the mPurchaseManagerListener in a map.
 * The response of this call searches the map for the mPurchaseManagerListener and communicates the
 * response via this mPurchaseManagerListener.
 *
 * The {@link IPurchase#purchase}  call can be either synchronous
 * or asynchronous. If its sync, the call response is triggered before the listener map is actually
 * updated. This is because the response call is unable to find the listener in the listener map so
 * its unable to communicate the response. In this case the response call saves the response in a
 * response map and returns. The {@link #onPostExecute(Object)} will then find the response in the
 * response map and communicate via the listener.
 */
class PurchaseAction extends AsyncTask<Void, Void, String> {

    private static final String TAG = PurchaseAction.class.getName();
    private PurchaseManager mPurchaseManager;
    private String mSku;
    private PurchaseManagerListener mPurchaseManagerListener;

    /**
     * Constructor.
     *
     * @param purchaseManager The instance of the purchase manager.
     * @param sku             The SKU to purchase.
     * @param listener        The listener for the response.
     */
    public PurchaseAction(PurchaseManager purchaseManager, String sku,
                          PurchaseManagerListener listener) {

        this.mPurchaseManager = purchaseManager;
        this.mSku = sku;
        this.mPurchaseManagerListener = listener;
    }

    /**
     * {@inheritDoc}
     * Triggers the purchase call and stores the listener in the map keyed with the request id.
     *
     * @return The request id received from the purchase call.
     */
    @Override
    protected String doInBackground(Void... params) {

        String requestId = mPurchaseManager.mPurchaseSystem.purchase(mSku);
        Log.d(TAG, "purchase called with " + requestId);
        mPurchaseManager.purchaseObjectMap.put(requestId, this);
        return requestId;
    }

    /**
     * {@inheritDoc}
     * Triggers the purchased logic if purchase response is available.
     *
     * @param requestId The request id received from the purchase call.
     */
    @Override
    protected void onPostExecute(String requestId) {

        if (!mPurchaseManager.purchaseResponseMap.containsKey(requestId)) {
            Log.d(TAG, "mPurchaseSystem.purchase not complete yet");
            return;
        }
        purchased(requestId);

    }

    /**
     * Utility to execute logic required on purchase call to inform the user of the result.
     *
     * @param requestId The request id of this request.
     */
    public void informUser(String requestId) {

        purchased(requestId);

    }

    /**
     * Inform the user of the request completion.
     *
     * @param requestId The request id of this call.
     */
    private void purchased(String requestId) {

        Log.d(TAG, "purchased mSku " + mSku + " with request " + requestId);
        Response response = mPurchaseManager.purchaseResponseMap.get(requestId);
        Receipt receipt = mPurchaseManager.purchaseReceiptMap.get(requestId);

        if (Response.Status.SUCCESSFUL.equals(response.getStatus())) {

            if (receipt == null) {
                // Hack for AmazonInAppPurchase, if the product was already purchased they do not
                // return receipt, but the purchase is valid so we need to show the content. We
                // also trigger an update purchases call here to fetch any latest purchases and
                // update them in our system.
                Log.i(TAG, "mSku already purchased, but is not stored in our system, " +
                        "update inventory");
                new UpdatePurchasesAction(mPurchaseManager, false).execute();
                if (mPurchaseManagerListener != null) {
                    mPurchaseManagerListener.onValidPurchaseResponse(
                            new Response(response.getRequestId(), Response.Status.SUCCESSFUL, null),
                            true, mSku);
                }
            }
            else {
                // Validate that the receipt received is not fake.
                new PurchaseValidAction(mPurchaseManager, mSku, mPurchaseManagerListener,
                                        receipt).execute();
            }
        }
        else {
            if (mPurchaseManagerListener != null) {
                mPurchaseManagerListener.onValidPurchaseResponse(
                        new Response(response.getRequestId(), Response.Status.FAILED, null),
                        false, mSku);
            }
        }

        cleanUp(requestId);
    }

    /**
     * Removes all objects stored for this call.
     *
     * @param requestId The request id.
     */
    private void cleanUp(String requestId) {

        mPurchaseManager.purchaseObjectMap.remove(requestId);
        mPurchaseManager.purchaseResponseMap.remove(requestId);
        mPurchaseManager.purchaseReceiptMap.remove(requestId);
    }

}
