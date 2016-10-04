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

import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.FulfillmentResult;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.UserDataResponse;
import com.amazon.purchase.IPurchase;
import com.amazon.purchase.model.Product;
import com.amazon.purchase.model.Receipt;
import com.amazon.purchase.model.Response;
import com.amazon.purchase.model.UserData;
import com.amazon.utils.ObjectVerification;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Implementation of {@link IPurchase} interface backed by Amazon IAP.
 */
public class AmazonInAppPurchase implements IPurchase {

    private static final String TAG = AmazonInAppPurchase.class.getName();

    /**
     * {@link IPurchase} implementation name.
     */
    static final String IMPL_CREATOR_NAME = AmazonInAppPurchase.class.getSimpleName();

    /**
     * The context.
     */
    protected Context mContext;

    /**
     * Bundle for extra data.
     */
    protected Bundle mExtras;

    /**
     * The purchase listener.
     */
    protected PurchaseListener mPurchaseListener;

    /**
     * The receipt verifier.
     */
    protected AReceiptVerifier mReceiptVerifier;

    /**
     * Constructor that initializes the default variable values.
     */
    public AmazonInAppPurchase() {

        super();
    }

    /**
     * Register custom a receipt verifier.
     *
     * @param receiptVerifier The verifier to be used by AmazonInAppPurchase.
     */
    public void registerReceiptVerification(AReceiptVerifier receiptVerifier) {

        this.mReceiptVerifier = receiptVerifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Context context, Bundle extras) {

        this.mContext = ObjectVerification.notNull(context, "Context cannot be null")
                                          .getApplicationContext();
        this.mExtras = extras;

        String receiptVerificationClassPath =
                context.getString(R.string.receipt_verification_service_iap);

        try {
            this.mReceiptVerifier = createReceiptVerifier(mContext, receiptVerificationClassPath);
        }
        catch (Exception e) {
            Log.e(TAG, "Failed to create custom ReceiptVerifier for path " +
                    receiptVerificationClassPath, e);
            // Assigning the default Receipt verifier.
            this.mReceiptVerifier = DefaultReceiptVerificationService.createInstance(context);
        }
    }

    /**
     * Creates an instance of {@link AReceiptVerifier} based on param receiptVerificationClassPath
     *
     * @param context                      The application context.
     * @param receiptVerificationClassPath The fully qualified path of the class.
     * @return The instance of the class.
     * @throws ClassNotFoundException           if the class was not found.
     * @throws java.util.NoSuchElementException if the method was not found on the reflection
     *                                          object.
     * @throws IllegalAccessException           if an illegal access attempt was made.
     * @throws InvocationTargetException        if there was an error invoking the method with
     *                                          reflection.
     * @throws NoSuchMethodException            if the method was not found.
     */
    private AReceiptVerifier createReceiptVerifier(Context context, String
            receiptVerificationClassPath) throws NoSuchMethodException, ClassNotFoundException,
            InvocationTargetException, IllegalAccessException {

        Log.d(TAG, "Creating instance of " + receiptVerificationClassPath);
        long startTime = System.currentTimeMillis();
        Class<?> clazz = Class.forName(receiptVerificationClassPath);
        Method method = clazz.getMethod(AReceiptVerifier.CREATE_INSTANCE_METHOD_NAME, Context
                .class);
        AReceiptVerifier receiptVerifierImpl = (AReceiptVerifier) method.invoke(null, context);
        long endTime = System.currentTimeMillis();
        Log.d(TAG, "Time taken in createReceiptVerifier " + (endTime - startTime));
        return receiptVerifierImpl;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerDefaultPurchaseListener(PurchaseListener purchaseListener) {

        this.mPurchaseListener = purchaseListener;
        PurchasingListener iapListener = createIapPurchasingListener(purchaseListener);
        PurchasingService.registerListener(mContext, iapListener);
        Log.d(TAG, PurchasingService.IS_SANDBOX_MODE + "IS_SANDBOX_MODE");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserData() {

        String requestId = PurchasingService.getUserData().toString();
        Log.d(TAG, "calling PurchaseService getUserData with requestId " + requestId);
        return requestId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProducts(Set<String> skuSet) {

        String requestId = PurchasingService.getProductData(skuSet).toString();
        Log.d(TAG, "calling PurchaseService getProducts with requestId " + requestId);
        return requestId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserPurchaseData(boolean reset) {

        String requestId = PurchasingService.getPurchaseUpdates(reset).toString();
        Log.d(TAG, "calling PurchaseService getUserPurchaseData with requestId " + requestId);
        return requestId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String purchase(String sku) {

        String requestId = PurchasingService.purchase(sku).toString();
        Log.d(TAG, "calling PurchaseService purchase with requestId " + requestId);
        return requestId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String isPurchaseValid(String sku, UserData userData, Receipt receipt) {

        Log.d(TAG, "calling validateReceipt");
        String requestId = createRandomString(receipt.getReceiptId(), 10);
        mReceiptVerifier.validateReceipt(mContext, requestId, sku, userData, receipt,
                                         mPurchaseListener);

        return requestId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyFulfillment(String sku, UserData userData, Receipt receipt, Receipt
            .FulfillmentStatus
            fulfillmentResult) {

        Log.d(TAG, "calling PurchaseService notifyFulfillment " + receipt);
        PurchasingService.notifyFulfillment(receipt.getReceiptId(), FulfillmentResult.FULFILLED);
    }

    /**
     * Creates IAP purchase listener from PurchaseListener.
     */
    /*package private*/
    PurchasingListener createIapPurchasingListener(final PurchaseListener listener) {

        Log.d(TAG, "PurchasingListener registered");
        return new PurchasingListener() {
            @Override
            public void onUserDataResponse(UserDataResponse userDataResponse) {

                Log.d(TAG, "UserDataResponse received " + userDataResponse.toString());
                Response response = createResponse(isSuccessful(userDataResponse),
                                                   userDataResponse.getRequestId().toString());

                UserData userData = createUserDataFromIapUserData(userDataResponse.getUserData());
                listener.onGetUserDataResponse(response, userData);
            }

            @Override
            public void onProductDataResponse(ProductDataResponse productDataResponse) {

                Log.d(TAG, "ProductDataResponse received " + productDataResponse.toString());
                Response response = createResponse(isSuccessful(productDataResponse),
                                                   productDataResponse.getRequestId().toString());

                Map<String, Product> productMap = createProductMapFromProductDataResponse
                        (productDataResponse.getProductData());

                listener.onProductDataResponse(response, productMap,
                                               productDataResponse.getUnavailableSkus());
            }

            @Override
            public void onPurchaseResponse(PurchaseResponse purchaseResponse) {

                Log.d(TAG, "purchaseResponse received " + purchaseResponse.toString());
                Response response = createResponse(isSuccessful(purchaseResponse),
                                                   purchaseResponse.getRequestId().toString());

                com.amazon.device.iap.model.Receipt iapReceipt = purchaseResponse.getReceipt();

                String sku = null;
                if (iapReceipt != null) {
                    sku = iapReceipt.getSku();
                }
                listener.onPurchaseResponse(response, sku, createReceipt(iapReceipt),
                                            createUserDataFromIapUserData(purchaseResponse
                                                                                  .getUserData()));
            }

            @Override
            public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse purchaseUpdatesResponse) {

                Log.d(TAG, "purchaseUpdatesResponse received " + purchaseUpdatesResponse.toString
                        ());
                Response response = createResponse(isSuccessful(purchaseUpdatesResponse),
                                                   purchaseUpdatesResponse.getRequestId()
                                                                          .toString());

                List<Receipt> receipts = createReceiptList(purchaseUpdatesResponse.getReceipts());
                UserData userData = createUserDataFromIapUserData(purchaseUpdatesResponse
                                                                          .getUserData());

                listener.onUserDataResponse(response, receipts, userData, purchaseUpdatesResponse
                        .hasMore());
            }
        };
    }

    /**
     * Converts a list of IAP receipts to purchase receipts.
     *
     * @param iapReceipts The IAP receipts to be converted.
     * @return The purchase receipts.
     */
    private List<Receipt> createReceiptList(List<com.amazon.device.iap.model.Receipt> iapReceipts) {

        List<Receipt> receipts = new ArrayList<>();
        if (iapReceipts == null) {
            return receipts;
        }
        for (com.amazon.device.iap.model.Receipt iapReceipt : iapReceipts) {
            receipts.add(createReceipt(iapReceipt));
        }
        return receipts;
    }

    /**
     * Converts a single IAP receipt to a purchase receipt.
     *
     * @param iapReceipt The IAP receipt to be converted.
     * @return The purchase receipt.
     */
    private Receipt createReceipt(com.amazon.device.iap.model.Receipt iapReceipt) {

        if (iapReceipt == null) {
            return null;
        }
        Receipt receipt = new Receipt();
        receipt.setSku(iapReceipt.getSku());
        receipt.setPurchasedDate(iapReceipt.getPurchaseDate());
        receipt.setExpiryDate(iapReceipt.getCancelDate());
        receipt.setReceiptId(iapReceipt.getReceiptId());
        receipt.setProductType(getProductType(iapReceipt.getProductType()));
        return receipt;
    }

    /**
     * Converts a list of IAP products to purchase products.
     *
     * @param iapProductMap The IAP products to be converted.
     * @return The purchase products.
     */
    private Map<String, Product> createProductMapFromProductDataResponse(Map<String,
            com.amazon.device.iap.model.Product> iapProductMap) {

        Map<String, Product> productMap = new HashMap<>();
        if (iapProductMap == null) {
            return productMap;
        }
        for (String sku : iapProductMap.keySet()) {
            Product product = createProductFromIapProduct(iapProductMap.get(sku));
            if (product != null) {
                productMap.put(sku, product);
            }
        }

        return productMap;
    }

    /**
     * Converts an IAP product to a purchase product.
     *
     * @param iapProduct The IAP product to be converted.
     * @return The purchase product.
     */
    private Product createProductFromIapProduct(com.amazon.device.iap.model.Product iapProduct) {

        if (iapProduct == null) {
            return null;
        }
        Product product = new Product();
        product.setPrice(iapProduct.getPrice());
        product.setSku(iapProduct.getSku());
        product.setTitle(iapProduct.getTitle());
        product.setDescription(iapProduct.getDescription());
        product.setIconUrl(iapProduct.getSmallIconUrl());
        product.setProductType(getProductType(iapProduct.getProductType()));
        return product;
    }

    /**
     * Converts an IAP product type to a purchase product type.
     *
     * @param productType The IAP product to be converted.
     * @return The purchase product.
     */
    private Product.ProductType getProductType(com.amazon.device.iap.model.ProductType
                                                       productType) {

        if (productType == null) {
            return null;
        }
        switch (productType) {
            case CONSUMABLE:
                return Product.ProductType.RENT;
            case ENTITLED:
                return Product.ProductType.BUY;
            case SUBSCRIPTION:
                return Product.ProductType.SUBSCRIBE;
            default:
                throw new RuntimeException("product type " + productType + " not supported");
        }
    }

    /**
     * Converts an IAP user data to a purchase user data object.
     *
     * @param iapUserData The IAP user data to be converted.
     * @return The purchase user data.
     */
    private UserData createUserDataFromIapUserData(com.amazon.device.iap.model.UserData
                                                           iapUserData) {

        if (iapUserData == null) {
            return null;
        }
        return new UserData(iapUserData.getUserId(), iapUserData.getMarketplace());
    }

    /**
     * Creates a {@link Response} object based on parameters.
     *
     * @param successful True if the response was successful; false otherwise.
     * @param requestId  The request id of the response.
     * @return response
     */
    private Response createResponse(boolean successful, String requestId) {

        if (successful) {
            return new Response(requestId, Response.Status.SUCCESSFUL, null);
        }
        else {
            return new Response(requestId, Response.Status.FAILED,
                                new Exception("Could not retrieve data from IAP"));
        }
    }

    /**
     * Decides whether the IAP user data response is a successful one or not.
     *
     * @param response The response to be checked.
     * @return Whether the IAP response is a successful one or not.
     */
    private boolean isSuccessful(UserDataResponse response) {

        return UserDataResponse.RequestStatus.SUCCESSFUL.equals(response.getRequestStatus());
    }


    /**
     * Decides whether the IAP product data response is a successful one or not.
     *
     * @param response The response to be checked.
     * @return Whether the IAP response is of successful one or not.
     */
    private boolean isSuccessful(ProductDataResponse response) {

        return ProductDataResponse.RequestStatus.SUCCESSFUL.equals(response.getRequestStatus());
    }


    /**
     * Decides whether the IAP purchase response is a successful one or not.
     *
     * @param response The response to be checked.
     * @return Whether the IAP response is a successful one or not.
     */
    private boolean isSuccessful(PurchaseResponse response) {

        return PurchaseResponse.RequestStatus.SUCCESSFUL.equals(response.getRequestStatus()) ||
                PurchaseResponse.RequestStatus.ALREADY_PURCHASED
                        .equals(response.getRequestStatus());
    }

    /**
     * Decides whether the IAP purchase updates response is a successful one or not.
     *
     * @param response The response to be checked.
     * @return Whether the IAP response is a successful one or not.
     */
    private boolean isSuccessful(PurchaseUpdatesResponse response) {

        return PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL.equals(response.getRequestStatus());
    }

    /**
     * Creates a random string of the given length and appends the prefix to the front of it.
     *
     * @param prefix The prefix of the string to append.
     * @param length The length of the random part of the string.
     * @return The string.
     */
    public String createRandomString(String prefix, int length) {

        StringBuilder builder;
        if (prefix != null) {
            builder = new StringBuilder(prefix);
        }
        else {
            builder = new StringBuilder();
        }
        Random r = new Random();
        for (int i = 0; i < length; i++) {
            char randomChar = (char) (r.nextInt(26) + 'a');
            builder.append(randomChar);
        }
        return builder.toString();
    }
}
