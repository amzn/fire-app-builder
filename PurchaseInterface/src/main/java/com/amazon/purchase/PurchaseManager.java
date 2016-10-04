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
import com.amazon.purchase.model.SkuData;
import com.amazon.purchase.model.UserData;
import com.amazon.utils.ObjectVerification;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.amazon.purchase.PurchaseUtils.*;


/**
 * This class handles the purchase data setup required when the app starts. The user first
 * configures the PurchaseManager via {@link #init(IPurchase, PurchaseManagerListener)} and waits
 * for the product registration to finish via the {@link PurchaseManagerListener}. The product
 * registration involves fetching the existing purchases of the current logged in user and updating
 * the products with their latest receipts. The purchase update state can be checked with the
 * {@link
 * #PURCHASE_UPDATE_STATUS}. Once the system is setup, the user can make calls for buying products
 * or checking the validity of already bought products. These calls cannot be started until product
 * registration is complete. New products bought outside the app will not reflect in the system
 * unless the PurchaseManager is restarted. Though if the user tried to purchase them again he will
 * be notified that the product is already purchased and it will then become available for
 * consumption. NOTE: This manager works on the assumption that PurchaseSystem is supposed to
 * provide the user data. This logic will not work if the app sets the user data via some external
 * login system.
 */
public class PurchaseManager {

    private static final String TAG = PurchaseManager.class.getName();

    /**
     * The application context.
     */
    protected static Context sContext;

    /**
     * The singleton instance.
     */
    private static PurchaseManager sInstance;

    /**
     * The purchase system instance.
     */
    protected IPurchase mPurchaseSystem;

    /**
     * {@link SkuData}s for the SKUs registered in the system. Stored as map with the key as the
     * SKU.
     */
    protected final Map<String, SkuData> mSkuDataMap;

    /**
     * The user data of the logged in user.
     */
    protected UserData mUserData;

    /**
     * Receipts of purchases made by the user. Stored with the key as the SKU.
     */
    protected final Map<String, Receipt> mReceiptMap;

    /**
     * A lock to ensure sequential updates to the receipt map.
     */
    protected Object receiptMapUpdateLock = new Object();

    /**
     * The status of the purchase updates. No other requests will be executed while purchase
     * updates
     * are in progress.
     */
    protected PurchaseUpdateStatus PURCHASE_UPDATE_STATUS;

    /**
     * A lock for the purchase update status.
     */
    protected Object purchaseUpdateLock = new Object();

    /**
     * The purchase utils instance.
     */
    protected PurchaseUtils purchaseUtils;

    /**
     * Map for storing the {@link PurchaseValidAction} objects for each purchase valid request
     * keyed
     * by the request id.
     */
    protected Map<String, PurchaseValidAction> purchaseValidObjectMap = Collections
            .synchronizedMap(new HashMap<String, PurchaseValidAction>());

    /**
     * Map for storing the purchase valid result objects for each purchase valid request keyed by
     * the request id.
     */
    protected Map<String, Boolean> purchaseValidResultMap =
            Collections.synchronizedMap(new HashMap<String, Boolean>());

    /**
     * Map for storing the purchase response for each purchase request keyed by request id.
     */
    protected Map<String, Response> purchaseResponseMap =
            Collections.synchronizedMap(new HashMap<String, Response>());

    /**
     * Map for storing the {@link PurchaseAction} objects for each purchase request keyed by
     * request id.
     */
    protected Map<String, PurchaseAction> purchaseObjectMap =
            Collections.synchronizedMap(new HashMap<String, PurchaseAction>());

    /**
     * Map for storing the purchase receipts for each purchase request keyed by request id.
     */
    protected Map<String, Receipt> purchaseReceiptMap =
            Collections.synchronizedMap(new HashMap<String, Receipt>());

    /**
     * Map for storing the {@link UpdatePurchasesAction} instances for each purchase update request
     * keyed by request id.
     */
    protected Map<String, UpdatePurchasesAction> updatePurchaseObjectMap = Collections
            .synchronizedMap
                    (new HashMap<String, UpdatePurchasesAction>());
    /**
     * Map for storing the purchase update responses for each purchase update request keyed by
     * request id.
     */
    protected Map<String, Response> updatePurchaseResponseMap =
            Collections.synchronizedMap(new HashMap<String, Response>());

    /**
     * Certain actions like purchase and isPurchaseValid cannot be executed while purchases are
     * being updated. This list maintains those pending actions.
     */
    private List<PendingAction> pendingActions = new ArrayList<>();

    /**
     * Method to provide singleton access to the {@link PurchaseManager}.
     *
     * @param context The application context.
     * @return The singleton instance.
     */
    public static PurchaseManager getInstance(Context context) {

        if (sInstance == null) {
            synchronized (PurchaseManager.class) {
                if (sInstance == null) {
                    sContext = context.getApplicationContext();
                    sInstance = new PurchaseManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * Constructor.
     */
    protected PurchaseManager() {

        this.purchaseUtils = new PurchaseUtils();
        this.mReceiptMap = new HashMap<>();
        this.mSkuDataMap = new HashMap<>();
        setPurchaseUpdateStatus(PurchaseUpdateStatus.NOT_STARTED);
    }

    /**
     * Registers an instance of {@link IPurchase} and {@link IPurchase.PurchaseListener} to listen
     * to any {@link Product} related updates. Once registered, it triggers the purchase system
     * configuration and loads the user data and purchases.
     *
     * @param purchaseSystem          The purchase system instance.
     * @param purchaseManagerListener The listener to listen to SKU registration updates.
     * @throws Exception if there were any uncaught errors.
     */
    public void init(IPurchase purchaseSystem, PurchaseManagerListener
            purchaseManagerListener) throws Exception {

        setPurchaseUpdateStatus(PurchaseUpdateStatus.ON_GOING);
        try {
            this.mPurchaseSystem = ObjectVerification.notNull(purchaseSystem, "a null purchase " +
                    "system cannot be registered");
            // Init purchaseSystem and register listener.
            mPurchaseSystem.init(sContext, null);
            mPurchaseSystem.registerDefaultPurchaseListener(createPurchaseListener());
            registerSkusAndPurchases(purchaseManagerListener);
        }
        catch (Exception e) {
            setPurchaseUpdateStatus(PurchaseUpdateStatus.FAILED);
            if (purchaseManagerListener != null) {
                purchaseManagerListener.onRegisterSkusResponse(new Response(null, Response.Status
                        .FAILED, e));
            }
            else {
                Log.e(TAG, "purchaseManagerListener is null");
            }
        }
    }

    /**
     * Reads the SKU list and registers each SKU into the system.
     * It also updates the user data and the purchases made by user.
     *
     * @param listener The listener.
     */
    private void registerSkusAndPurchases(PurchaseManagerListener listener) throws Exception {

        List<Map<String, String>> skuSet = purchaseUtils.readSkusFromConfigFile(sContext);
        mSkuDataMap.putAll(purchaseUtils.updateSkuSet(skuSet));
        UpdatePurchasesAction.setListener(listener);
        new UpdatePurchasesAction(this, true).execute();
    }

    /**
     * Validates that the purchase system is configured. This is required to make sure no call is
     * made before system is configured.
     */
    private void validateSystemConfiguration() {

        ObjectVerification.notNull(mPurchaseSystem, "purchase system is null");
    }

    /**
     * This tests that a purchase is still valid. Receipts can become obsolete at any point of
     * time via external systems, such as cancelling a subscription. Its important to check that
     * they are still valid before using them.
     *
     * @param sku                     The SKU of the receipt that needs to be validated.
     * @param purchaseManagerListener The purchase manager listener.
     */
    public void isPurchaseValid(String sku, PurchaseManagerListener purchaseManagerListener) {

        validateSystemConfiguration();

        if (addPendingAction(new PendingAction(sku, purchaseManagerListener, PendingAction.ACTION
                .IS_PURCHASE_VALID))) {
            Log.i(TAG, "isPurchaseValid call for sku " + sku + " stored for future execution");
            return;
        }
        if (sku == null) {
            purchaseManagerListener.onValidPurchaseResponse(new Response(null, Response.Status
                                                                    .SUCCESSFUL, null),
                                                            false, sku);
        }
        Receipt receipt = mReceiptMap.get(sku);
        // Check if receipt stored with purchaseManager is expired.
        if (!isLocalPurchaseDataValid(receipt)) {
            Log.d(TAG, "local purchase not valid for " + receipt);
            purchaseManagerListener.onValidPurchaseResponse(new Response(null, Response.Status
                                                                    .SUCCESSFUL, null),
                                                            false, sku);
        }
        else {
            // Local receipt is still valid, now check the purchase system for receipt validity
            Log.d(TAG, "local purchase is valid for " + receipt);
            new PurchaseValidAction(this, sku, purchaseManagerListener, receipt)
                    .execute();
        }
    }

    /**
     * Purchases a SKU. This method first validates that the SKU is not already purchased using the
     * {@link #isLocalPurchaseDataValid(Receipt)} method to avoid to duplicate purchases.
     *
     * @param sku                     The SKU to purchase.
     * @param purchaseManagerListener The purchase manager listener.
     */
    public void purchaseSku(String sku, PurchaseManagerListener purchaseManagerListener) {

        validateSystemConfiguration();
        PendingAction pendingAction = new PendingAction(sku, purchaseManagerListener,
                                                        PendingAction.ACTION
                                                                .PURCHASE);
        // Action pending on other ongoing requests.
        if (addPendingAction(pendingAction)) {
            Log.d(TAG, "purchase action for sku " + sku + " added in pending list");
            return;
        }
        if (isLocalPurchaseDataValid(mReceiptMap.get(sku))) {
            purchaseManagerListener.onValidPurchaseResponse(new Response(null, Response.Status
                                                                    .SUCCESSFUL, null),
                                                            true, sku);
            return;
        }
        // Initiate purchase.
        new PurchaseAction(this, sku, purchaseManagerListener).execute();
    }

    /**
     * Returns a purchased SKU if there is any in the system.
     *
     * @return A SKU if there are any purchased SKUs present in system.
     */
    public String getPurchasedSku() {

        Collection<Receipt> receiptList = mReceiptMap.values();
        for (Receipt receipt : receiptList) {
            return receipt.getSku();
        }
        return null;
    }

    /**
     * Checks if the local receipt for this SKU is valid.
     *
     * @param receipt Receipt to validate.
     * @return True or false depending on if the local receipt for this SKU is valid.
     */
    private boolean isLocalPurchaseDataValid(Receipt receipt) {

        // No receipt exist, meaning the product's purchase was never updated locally.
        if (receipt == null) {
            return false;
        }

        // The receipt has expired.
        if (purchaseUtils.isReceiptExpired(receipt)) {
            // Inform the purchaseManager that this purchase is no longer valid.
            unregisterSkuReceipt(receipt);
            return false;
        }

        // The product was rented and the rent period is over.
        if (purchaseUtils.isProductRented(mSkuDataMap.get(receipt.getSku()))
                && purchaseUtils.isRentalExpired(sContext, receipt)) {

            Log.d(TAG, "rent period is over for receipt " + receipt);
            // Inform the purchaseManager that this purchase is no longer valid.
            unregisterSkuReceipt(receipt);
            return false;
        }
        return true;
    }

    /**
     * Create a listener that is required to make calls to purchase system.
     *
     * @return The purchase listener.
     */
    private IPurchase.PurchaseListener createPurchaseListener() {

        return new IPurchase.PurchaseListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onProductDataResponse(Response response, Map<String, Product>
                    productDetails, Set<String> invalidSkus) {
                // Not required for this system.
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onGetUserDataResponse(Response response, UserData userData) {
                // Not required for this system.
            }

            /**
             * {@inheritDoc}
             * This method first validates that the received receipts belong to the same user as
             * the one registered in the system. If its not the case then it resets the current
             * logged in user to the one received via the call and clears the cache receipts from
             * the system.
             * Then the method parses through the receipt list, validates each receipt is still
             * valid and adds it to the receipts stored in the system. If the receipt is not
             * valid it unregisters it from the system.
             */
            @Override
            public void onUserDataResponse(Response response, List<Receipt>
                    receiptList, UserData userData, boolean hasMore) {

                if (Response.Status.SUCCESSFUL == response.getStatus()) {
                    if (userData != null && !userData.equals(mUserData)) {
                        // Current user is not the same as registered user, update the registered
                        // user and reset the receipts
                        mUserData = userData;
                        mReceiptMap.clear();
                    }
                    Log.d(TAG, "user purchases count " + receiptList.size());
                    for (Receipt receipt : receiptList) {
                        Log.d(TAG, "Receipt received " + receipt);

                        // Received receipt for a valid SKU.
                        if (mSkuDataMap.containsKey(receipt.getSku())) {

                            // It is possible that the receipt has expired, so validate before
                            // registering it in the system.
                            if (isLocalPurchaseDataValid(receipt)) {
                                registerReceiptSku(receipt);
                            }
                            else {
                                unregisterSkuReceipt(receipt);
                            }
                        }
                        else {
                            Log.e(TAG, "Received receipt for a non-supported sku " + receipt
                                    .getSku() + " receipt id is " + receipt.getReceiptId());
                        }
                    }
                    // There are more receipts available, poll for them.
                    if (hasMore) {
                        Log.d(TAG, "has more purchases");
                        new UpdatePurchasesAction(sInstance, false).execute();
                    }
                    // User purchase updates complete.
                    else {
                        setPurchaseUpdateStatus(PurchaseUpdateStatus.COMPLETED);
                        completeUpdatePurchaseCall(response);

                    }
                }
                else {
                    Log.e(TAG, "Failed to get user purchases ", response.getThrowable());
                    setPurchaseUpdateStatus(PurchaseUpdateStatus.FAILED);
                    // Inform the user that SKUs purchase update call is complete.
                    completeUpdatePurchaseCall(response);
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onPurchaseResponse(Response response, String sku, Receipt receipt,
                                           UserData userData) {

                try {
                    Log.d(TAG, "onPurchaseResponse " + response.getRequestId());
                    // Saving the response and receipts.
                    purchaseReceiptMap.put(response.getRequestId(), receipt);
                    purchaseResponseMap.put(response.getRequestId(), response);
                    if (purchaseObjectMap.containsKey(response.getRequestId())) {
                        Log.d(TAG, "purchaseObjectMap contains " + response.getRequestId());
                        // Inform user.
                        purchaseObjectMap.get(response.getRequestId()).informUser(response.getRequestId());

                    }
                    else {
                        // Purchase request object not available, moving on.
                        Log.d(TAG, "purchaseObjectMap does not contain " + response.getRequestId());
                    }
                }
                catch (Exception e) {
                    Log.e(TAG, "onPurchaseResponse exception", e);
                }
            }

            /**
             * {@inheritDoc}
             * This method registers or unregisters the receipts from the system based on the {link
             * #purchaseValid} response.
             */
            @Override
            public void isPurchaseValidResponse(Response response, String sku, Receipt receipt,
                                                boolean purchaseValid, UserData userData) {

                Response purchaseManagerResponse;
                boolean result;
                String requestId = response.getRequestId();

                if (Response.Status.SUCCESSFUL.equals(response.getStatus())) {
                    purchaseManagerResponse = new Response(requestId
                            , Response.Status.SUCCESSFUL, null);
                    if (!purchaseValid) {
                        // Purchase is not valid, unregister it from the system.
                        Log.d(TAG, "purchase not valid " + response);
                        unregisterSkuReceipt(receipt);
                        result = false;
                    }
                    else {
                        Log.d(TAG, "purchase valid " + response);
                        // Purchase is not valid, register it into the system.
                        registerReceiptSku(receipt);
                        result = true;
                    }
                }
                else {
                    purchaseManagerResponse = new Response(requestId
                            , Response.Status.FAILED, null);
                    result = false;
                }
                // Save the response.
                purchaseResponseMap.put(requestId, purchaseManagerResponse);
                purchaseValidResultMap.put(requestId, result);
                // Inform user.
                if (purchaseValidObjectMap.containsKey(requestId)) {
                    purchaseValidObjectMap.get(requestId).informUser(requestId);
                }
            }
        };
    }

    /**
     * Inform the user that the SKUs purchase update call is complete.
     *
     * @param response the purchase update response.
     */
    private void completeUpdatePurchaseCall(Response response) {

        updatePurchaseResponseMap.put(response.getRequestId(), response);
        if (updatePurchaseObjectMap.containsKey(response.getRequestId())) {
            updatePurchaseObjectMap.get(response.getRequestId()).informUser(response.getRequestId
                    ());
        }
    }

    /**
     * Registers the new receipt with the purchase manager.
     *
     * @param receipt Receipt to be registered.
     */
    private void registerReceiptSku(Receipt receipt) {

        synchronized (receiptMapUpdateLock) {
            if (receipt == null) {
                return;
            }
            // We already have an existing receipt registered with this SKU.
            if (mReceiptMap.containsKey(receipt.getSku())) {
                Receipt existingReceipt = mReceiptMap.get(receipt.getSku());
                Log.d(TAG, "existing key" + existingReceipt + " new receipt " + receipt);

                // If current receipt is either null or is older than the new receipt received,
                // update it with new receipt
                if (existingReceipt == null || existingReceipt.getPurchasedDate() == null ||
                        existingReceipt.getPurchasedDate().before(receipt.getPurchasedDate())) {
                    mReceiptMap.put(receipt.getSku(), receipt);
                }
            }
            // As of now no receipt exist for this SKU
            else {
                mReceiptMap.put(receipt.getSku(), receipt);
            }
        }
    }

    /**
     * Unregisters the SKU's receipt with the purchase manager
     *
     * @param receipt The receipt to unregister.
     */
    private void unregisterSkuReceipt(Receipt receipt) {

        synchronized (receiptMapUpdateLock) {
            String sku = receipt.getSku();

            // Inform the purchase system that this SKU has been fulfilled.
            mPurchaseSystem.notifyFulfillment(sku, mUserData, receipt,
                                              Receipt.FulfillmentStatus.FULFILLED);
            if (receipt.equals(mReceiptMap.get(sku))) {
                // Remove it from purchaseManager.
                mReceiptMap.remove(sku);
            }
        }
    }

    /**
     * Method to update the purchase status. It locks on {@link #purchaseUpdateLock}
     *
     * @param purchaseUpdateStatus The status to set.
     */
    private void setPurchaseUpdateStatus(PurchaseUpdateStatus purchaseUpdateStatus) {

        synchronized (purchaseUpdateLock) {
            PURCHASE_UPDATE_STATUS = purchaseUpdateStatus;
            if (PurchaseUpdateStatus.COMPLETED.equals(PURCHASE_UPDATE_STATUS) ||
                    PurchaseUpdateStatus.FAILED.equals(PURCHASE_UPDATE_STATUS)) {
                completeActions();
            }
        }
    }

    /**
     * Method to add an action to list of pending actions. It only adds the action if current
     * purchases are being updated; otherwise it returns false.
     *
     * @param action The action to add.
     * @return True or false based on whether the action was added or not.
     */
    private boolean addPendingAction(PendingAction action) {

        synchronized (purchaseUpdateLock) {
            if (PurchaseUpdateStatus.ON_GOING.equals(PURCHASE_UPDATE_STATUS)) {
                pendingActions.add(action);
                return true;
            }
            return false;
        }
    }

    /**
     * Executes the actions stored in the pending actions list.
     */
    private void completeActions() {

        for (PendingAction action : pendingActions) {
            if (PendingAction.ACTION.PURCHASE.equals(action.action)) {
                purchaseSku(action.sku, action.listener);
            }
            else if (PendingAction.ACTION.IS_PURCHASE_VALID.equals(action.action)) {
                isPurchaseValid(action.sku, action.listener);
            }
        }
        pendingActions.clear();

    }

    /**
     * A utility method to set the purchase utils.
     *
     * @param purchaseUtils The purchase utils instance to set.
     */
    @VisibleForTesting
    protected void setPurchaseUtils(PurchaseUtils purchaseUtils) {

        this.purchaseUtils = purchaseUtils;
    }


}
