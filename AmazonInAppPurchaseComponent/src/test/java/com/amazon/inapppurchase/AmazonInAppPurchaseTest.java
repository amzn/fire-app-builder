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
import com.amazon.device.iap.internal.model.ProductDataResponseBuilder;
import com.amazon.device.iap.internal.model.PurchaseResponseBuilder;
import com.amazon.device.iap.internal.model.PurchaseUpdatesResponseBuilder;
import com.amazon.device.iap.internal.model.ReceiptBuilder;
import com.amazon.device.iap.internal.model.UserDataBuilder;
import com.amazon.device.iap.internal.model.UserDataResponseBuilder;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.ProductType;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.RequestId;
import com.amazon.device.iap.model.UserDataResponse;
import com.amazon.purchase.IPurchase;
import com.amazon.purchase.model.Receipt;
import com.amazon.purchase.model.Response;
import com.amazon.purchase.model.UserData;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.content.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Tests for {@link AmazonInAppPurchase}
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*", "org.json.*"})
@PrepareForTest(PurchasingService.class)
public class AmazonInAppPurchaseTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();
    private Context mContext;

    @Before
    public void setUp() {
        mContext = mock(Context.class);
        when(mContext.getApplicationContext()).thenReturn(mock(Context.class));
        PowerMockito.mockStatic(PurchasingService.class);
    }

    @Test
    public void testRegisterPurchaseListener() {
        AmazonInAppPurchase purchase = new AmazonInAppPurchase();
        IPurchase.PurchaseListener listener = mock(IPurchase.PurchaseListener.class);
        purchase.registerDefaultPurchaseListener(listener);
        assertEquals(listener, purchase.mPurchaseListener);
        verifyStatic(times(1));
    }

    @Test
    public void testRegisterReceiptVerification() {
        AmazonInAppPurchase purchase = new AmazonInAppPurchase();
        AReceiptVerifier verifier = mock(AReceiptVerifier.class);
        purchase.registerReceiptVerification(verifier);
        assertEquals(verifier, purchase.mReceiptVerifier);
    }

    @Test
    public void testConfigure() {
        AmazonInAppPurchase purchase = new AmazonInAppPurchase();
        purchase.init(mContext, null);
        assertNotNull(purchase.mContext);
    }

    @Test
    public void testGetUserData() {
        AmazonInAppPurchase purchase = new AmazonInAppPurchase();
        Mockito.when(PurchasingService.getUserData()).thenReturn(RequestId.fromString("requestId"));
        purchase.getUserData();
        verifyStatic(times(1));
    }

    @Test
    public void testGetProductData() {
        AmazonInAppPurchase purchase = new AmazonInAppPurchase();
        Mockito.when(PurchasingService.getProductData(Matchers.anySetOf(String.class)))
                .thenReturn(RequestId.fromString("requestId"));
        purchase.getProducts(new HashSet<String>());
        verifyStatic(times(1));
    }

    @Test
    public void testisPurchaseValid() {
        AmazonInAppPurchase purchase = new AmazonInAppPurchase();
        AReceiptVerifier verifier = mock(AReceiptVerifier.class);
        purchase.registerReceiptVerification(verifier);
        purchase.isPurchaseValid("", new UserData("", ""), new Receipt());
        verify(verifier).validateReceipt(any(Context.class), any(String.class),
                any(String.class), any (UserData.class), any(Receipt.class),
                any(IPurchase.PurchaseListener.class));
    }

    @Test
    public void testGetUserPurchases() {
        AmazonInAppPurchase purchase = new AmazonInAppPurchase();
        Mockito.when(PurchasingService.getPurchaseUpdates(any(boolean.class))).thenReturn
                (RequestId.fromString("requestId"));
        purchase.getUserPurchaseData(true);
        verifyStatic(times(1));
    }

    @Test
    public void testNotifyFulfillment() {
        AmazonInAppPurchase purchase = new AmazonInAppPurchase();
        purchase.notifyFulfillment("", new UserData("", ""), new Receipt(),
                Receipt.FulfillmentStatus.FULFILLED);
        verifyStatic(times(1));
    }

    @Test
    public void testCreatePurchasingListener() {
        AmazonInAppPurchase purchase = new AmazonInAppPurchase();
        IPurchase.PurchaseListener listener = mock(IPurchase.PurchaseListener.class);
        PurchasingListener iapListener = purchase.createIapPurchasingListener(listener);

        iapListener.onUserDataResponse(createUserDataResponse());
        verify(listener).onGetUserDataResponse(any(Response.class), any(UserData.class));

        iapListener.onProductDataResponse(createProductDataResponse());
        verify(listener).onProductDataResponse(any(Response.class), Matchers.anyMapOf(String.class,
                com.amazon.purchase.model.Product.class), Matchers.anySetOf(String.class));

        iapListener.onPurchaseResponse(createPurchaseResponse());
        verify(listener).onPurchaseResponse(any(Response.class), any(String.class),
                any(Receipt.class), any(UserData.class));

        iapListener.onPurchaseUpdatesResponse(createPurchaseUpdateResponse());
        verify(listener).onUserDataResponse(any(Response.class), Matchers.anyListOf(Receipt.class),
                any(UserData.class), any(Boolean.class));
    }

    private PurchaseUpdatesResponse createPurchaseUpdateResponse() {
        PurchaseUpdatesResponseBuilder builder = new PurchaseUpdatesResponseBuilder()
                .setReceipts(new ArrayList<com.amazon.device.iap.model.Receipt>())
                .setRequestId(RequestId.fromString(""))
                .setUserData(new com.amazon.device.iap.model.UserData(new UserDataBuilder()
                        .setUserId("")
                        .setMarketplace("")))
                .setRequestStatus(PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL);
        return new PurchaseUpdatesResponse(builder);
    }

    private PurchaseResponse createPurchaseResponse() {
        PurchaseResponseBuilder builder = new PurchaseResponseBuilder()
                .setRequestId(RequestId.fromString(""))
                .setRequestStatus(PurchaseResponse.RequestStatus.ALREADY_PURCHASED)
                .setUserData(new com.amazon.device.iap.model.UserData(new UserDataBuilder()
                        .setUserId("")
                        .setMarketplace("")))
                .setReceipt(new com.amazon.device.iap.model.Receipt(new ReceiptBuilder()
                        .setProductType(ProductType.CONSUMABLE)
                        .setSku("")
                        .setPurchaseDate(new Date())));
        return new PurchaseResponse(builder);
    }

    private ProductDataResponse createProductDataResponse() {
        ProductDataResponseBuilder builder = new ProductDataResponseBuilder()
                .setRequestStatus(ProductDataResponse.RequestStatus.SUCCESSFUL)
                .setRequestId(RequestId.fromString(""))
                .setProductData(new HashMap<String, Product>())
                .setUnavailableSkus(new HashSet<String>());
        return new ProductDataResponse(builder);
    }

    private UserDataResponse createUserDataResponse() {
        UserDataResponseBuilder builder = new UserDataResponseBuilder()
                .setRequestId(RequestId.fromString("requestId"))
                .setRequestStatus(UserDataResponse.RequestStatus.SUCCESSFUL)
                .setUserData(new com.amazon.device.iap.model.UserData(new UserDataBuilder()
                        .setUserId("userId")
                        .setMarketplace("mp")));
        return new UserDataResponse(builder);
    }
}
