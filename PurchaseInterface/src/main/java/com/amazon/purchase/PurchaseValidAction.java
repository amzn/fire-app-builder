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
 * Class to trigger check if a purchase is valid.
 *
 * The {@link #doInBackground} triggers the call and saves the purchase listener in a map. The
 * response of this call searches the map for the purchase listener and communicates the response
 * via this purchase listener. The {@link IPurchase#isPurchaseValid} call can be either synchronous
 * or asynchronous. If its sync, the call response is triggered before the listener map is actually
 * updated. This is because the response call is unable to find the listener in the listener map so
 * its unable to communicate the response. In this case the response call saves the response in a
 * response map and returns. The {@link #onPostExecute(Object)} will then find the response in the
 * response map and communicate via the listener.
 */
class PurchaseValidAction extends AsyncTask<Void, Void, String> {

    private static final String TAG = PurchaseValidAction.class.getName();
    private PurchaseManager mPurchaseManager;
    private Receipt mReceipt;
    private String mSku;
    private PurchaseManagerListener mPurchaseManagerListener;

    /**
     * Constructor.
     *
     * @param purchaseManager The instance of the purchase manager.
     * @param sku             The SKU to purchase.
     * @param listener        The listener for the response.
     * @param receipt         The receipt to validate.
     */
    public PurchaseValidAction(PurchaseManager purchaseManager, String sku,
                               PurchaseManagerListener listener, Receipt receipt) {

        this.mPurchaseManager = purchaseManager;
        this.mSku = sku;
        this.mPurchaseManagerListener = listener;
        this.mReceipt = receipt;
    }

    /**
     * {@inheritDoc}
     * Triggers the is purchase valid call and stores the listener in the map keyed
     * with the request id.
     *
     * @return The request id received from the call.
     */
    @Override
    protected String doInBackground(Void... params) {

        String requestId = mPurchaseManager.mPurchaseSystem.isPurchaseValid(mSku, mPurchaseManager
                .mUserData, mReceipt);
        mPurchaseManager.purchaseValidObjectMap.put(requestId, this);
        return requestId;
    }

    /**
     * {@inheritDoc}
     * Triggers the validating purchase logic if the purchase response is available.
     *
     * @param requestId The request id received from the call.
     */
    @Override
    protected void onPostExecute(String requestId) {

        if (!mPurchaseManager.purchaseResponseMap.containsKey(requestId)) {
            Log.d(TAG, "mPurchaseSystem.isPurchaseValid not complete yet");
            return;
        }
        runPurchaseValidFlow(requestId);

    }

    /**
     * Utility to execute logic required on valid purchase call. Ends with informing the user
     * about the result of the purchase request once the validation flow is complete.
     *
     * @param requestId The request id of this request.
     */
    public void informUser(String requestId) {

        runPurchaseValidFlow(requestId);

    }

    /**
     * Starts the purchase validation flow.
     *
     * @param requestId The request id of this call.
     */
    private void runPurchaseValidFlow(String requestId) {

        Response response = mPurchaseManager.purchaseResponseMap.get(requestId);
        Boolean result = mPurchaseManager.purchaseValidResultMap.get(requestId);
        if (mPurchaseManagerListener != null) {
            mPurchaseManagerListener.onValidPurchaseResponse(response, result, mSku);
        }
        cleanUp(requestId);
    }

    /**
     * Removes all objects that are stored for this call.
     *
     * @param requestId The request id.
     */
    private void cleanUp(String requestId) {

        mPurchaseManager.purchaseValidObjectMap.remove(requestId);
        mPurchaseManager.purchaseResponseMap.remove(requestId);
    }
}
