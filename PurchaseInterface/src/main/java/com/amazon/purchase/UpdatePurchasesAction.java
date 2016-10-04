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

import android.os.AsyncTask;
import android.util.Log;

/**
 * Class to trigger purchase update calls.
 *
 * The {@link #doInBackground} method triggers the call and saves the listener in a map. The
 * response of this call searches the map for the listener and communicates the response via this
 * listener. The {@link IPurchase#getUserPurchaseData} call can be either synchronous or
 * asynchronous. If its sync, the call response is triggered before the listener map is actually
 * updated. This is because the response call is unable to find the listener in the listener map so
 * its unable to communicate the response. In this case the response call saves the response in a
 * response map and returns. The {@link #onPostExecute(Object)} will then find the response in the
 * response map and communicate via the listener.
 */
class UpdatePurchasesAction extends AsyncTask<Void, Void, String> {

    /**
     * Debug tag.
     */
    private static final String TAG = UpdatePurchasesAction.class.getName();

    /**
     * Purchase manager instance.
     */
    private PurchaseManager mPurchaseManager;

    /**
     * Flag for whether or not to start the request all over or to continue the previous call.
     */
    private boolean mReset;

    /**
     * Listener for the calls.
     */
    private static PurchaseManagerListener sListener;

    /**
     * Constructor.
     *
     * @param purchaseManager Instance of purchase manager
     * @param reset           Whether to start the request all over or continue the previous call.
     */
    public UpdatePurchasesAction(PurchaseManager purchaseManager, boolean reset) {

        this.mPurchaseManager = purchaseManager;
        this.mReset = reset;
    }

    /**
     * Set the listener for purchase update requests.
     *
     * @param listener The listener to set.
     */
    public static void setListener(PurchaseManagerListener listener) {

        sListener = listener;
    }

    /**
     * {@inheritDoc}
     * Triggers the get user data call and stores the listener in the map keyed with requestId.
     *
     * @return The request id received from the call.
     */
    @Override
    protected String doInBackground(Void... params) {

        String requestId = mPurchaseManager.mPurchaseSystem.getUserPurchaseData(mReset);
        mPurchaseManager.updatePurchaseObjectMap.put(requestId, this);
        return requestId;
    }

    /**
     * {@inheritDoc}
     * Triggers the the user purchase updated logic if the response is available.
     *
     * @param requestId The request id received from the call.
     */
    @Override
    protected void onPostExecute(String requestId) {

        if (!mPurchaseManager.updatePurchaseResponseMap.containsKey(requestId)) {
            Log.d(TAG, "mPurchaseSystem.getUserPurchaseData not complete yet");
            return;
        }
        userPurchaseUpdated(mPurchaseManager.updatePurchaseResponseMap.get(requestId));

    }

    /**
     * Informs the user of the purchase response.
     *
     * @param requestId The request id of the purchase update request.
     */
    public void informUser(String requestId) {

        userPurchaseUpdated(mPurchaseManager.updatePurchaseResponseMap.get(requestId));
    }

    /**
     * User purchases updated, inform the user.
     *
     * @param response User purchase update response.
     */
    private void userPurchaseUpdated(Response response) {

        String requestId = response.getRequestId();
        if (sListener != null) {
            sListener.onRegisterSkusResponse(response);
        }
        cleanUp(requestId);
    }

    /**
     * Removes all objects stored for this request.
     *
     * @param requestId The request id.
     */
    private void cleanUp(String requestId) {

        mPurchaseManager.updatePurchaseObjectMap.remove(requestId);
        mPurchaseManager.updatePurchaseResponseMap.remove(requestId);
    }
}
