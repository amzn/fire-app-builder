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
import com.amazon.purchase.model.UserData;
import com.amazon.purchase.testResources.MockIPurchaseImpl;
import com.amazon.purchase.testResources.MockPurchaseManager;
import com.amazon.purchase.testResources.TestUtils;
import com.amazon.purchase.testResources.VerifyUtil;
import com.amazon.utils.DateAndTimeHelper;

import org.junit.Before;
import org.junit.Test;

import android.content.Context;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link PurchaseManager}.
 */
public class PurchaseManagerTest {

    private PurchaseManager mPurchaseManager;
    private IPurchase purchaseSystem;
    private MockIPurchaseImpl mockPurchaseSystem;
    private VerifyUtil verifyUtil;
    private Context mContext = InstrumentationRegistry.getTargetContext();
    private PurchaseUtils purchaseUtils;

    @Before
    public void setUp() throws IOException {

        verifyUtil = mock(VerifyUtil.class);
        MockIPurchaseImpl.sMockIPurchase = spy(new MockIPurchaseImpl());
        purchaseSystem = (MockIPurchaseImpl.sMockIPurchase);
        mockPurchaseSystem = (MockIPurchaseImpl.sMockIPurchase);

        TestUtils.createSkuDataSet();
        purchaseUtils = spy(new PurchaseUtils());

        MockPurchaseManager.sContext = mContext;
        mPurchaseManager = new MockPurchaseManager();
        mPurchaseManager.setPurchaseUtils(purchaseUtils);
        doReturn(TestUtils.skuSet).when(purchaseUtils).readSkusFromConfigFile(any(Context.class));

    }

    /**
     * Test for getInstance, validating that instance is correctly initialized and same instance
     * is returned every time.
     */
    @Test
    public void testGetInstance() {

        assertNotNull(PurchaseManager.getInstance(mContext));
        assertEquals(PurchaseManager.getInstance(mContext), PurchaseManager.getInstance(mContext));
        assertEquals(PurchaseUtils.PurchaseUpdateStatus.NOT_STARTED, PurchaseManager
                .getInstance(mContext).PURCHASE_UPDATE_STATUS);
    }


    /**
     * Test for init happy path, asserting that all class variables are correctly initialized,
     * purchase information is correctly fetched and purchase manager has latest updates
     */
    @Test
    public void testConfigure() throws Exception {

        mPurchaseManager.init(purchaseSystem, new PurchaseManagerListener() {
            @Override
            public void onRegisterSkusResponse(Response response) {

                assertEquals(Response.Status.SUCCESSFUL, response.getStatus());
                assertEquals(mPurchaseManager.mPurchaseSystem, purchaseSystem);
                assertNotNull(MockIPurchaseImpl.sMockIPurchase.context);
                assertNotNull(MockIPurchaseImpl.sMockIPurchase.listener);
                assertEquals(TestUtils.userData, mPurchaseManager.mUserData);
                assertEquals(TestUtils.skuDataMap, mPurchaseManager.mSkuDataMap);
                for (Receipt receipt : mPurchaseManager.mReceiptMap.values()) {
                    assertTrue(TestUtils.receiptMap.containsValue(receipt));
                }
                assertEquals(PurchaseUtils.PurchaseUpdateStatus.COMPLETED, mPurchaseManager
                        .PURCHASE_UPDATE_STATUS);
                verifyUtil.verified();
            }

            @Override
            public void onValidPurchaseResponse(Response response, boolean validity, String sku) {

            }
        });
        Thread.sleep(1000);

        verify(verifyUtil).verified();
    }

    /**
     * Tests for init failure, asserting that the purchase update status is correctly set
     */
    @Test
    public void testConfigureFailure() throws Exception {

        doThrow(new RuntimeException("testException")).when(purchaseSystem).init(any(Context.class),
                                                                                 any(Bundle.class));
        mPurchaseManager.init(purchaseSystem, new PurchaseManagerListener() {
            @Override
            public void onRegisterSkusResponse(Response response) {

                assertEquals(Response.Status.FAILED, response.getStatus());
                verifyUtil.verified();
            }

            @Override
            public void onValidPurchaseResponse(Response response, boolean validity, String sku) {

            }
        });
        Thread.sleep(1000);
        assertEquals(mPurchaseManager.mPurchaseSystem, purchaseSystem);
        assertEquals(PurchaseUtils.PurchaseUpdateStatus.FAILED, mPurchaseManager
                .PURCHASE_UPDATE_STATUS);
        verify(verifyUtil).verified();

    }

    /**
     * Tests for update purchase failure, asserting that the purchase update status is correctly
     * set
     */
    @Test
    public void testUpdatePurchaseFailure() throws Exception {

        Response response = new Response("getUserPurchaseDataSuccess", Response.Status.FAILED,
                                         null);
        doReturn(response).when(mockPurchaseSystem).listenerResponse();

        mPurchaseManager.init(purchaseSystem, new PurchaseManagerListener() {
            @Override
            public void onRegisterSkusResponse(Response response) {

                assertEquals(Response.Status.FAILED, response.getStatus());
                verifyUtil.verified();
            }

            @Override
            public void onValidPurchaseResponse(Response response, boolean validity, String sku) {

            }
        });
        Thread.sleep(1000);
        assertEquals(mPurchaseManager.mPurchaseSystem, purchaseSystem);
        assertEquals(PurchaseUtils.PurchaseUpdateStatus.FAILED, mPurchaseManager
                .PURCHASE_UPDATE_STATUS);
        verify(verifyUtil).verified();

    }

    /**
     * tests isPurchaseValid for non existent purchase
     */
    @Test
    public void testIsPurchaseValidForNonExistentPurchase() throws Exception {

        mPurchaseManager.init(purchaseSystem, null);
        Thread.sleep(1000);
        mPurchaseManager.isPurchaseValid("rentSku", new PurchaseManagerListener() {
            @Override
            public void onRegisterSkusResponse(Response response) {

            }

            @Override
            public void onValidPurchaseResponse(Response response, boolean validity, String sku) {

                assertEquals(Response.Status.SUCCESSFUL, response.getStatus());
                assertFalse(validity);
                verifyUtil.verified();
            }
        });
        Thread.sleep(1000);
        verify(verifyUtil).verified();
    }

    /**
     * tests isPurchaseValid for expired existent purchase
     */
    @Test
    public void testIsPurchaseValidForExpiredExistentPurchase() throws Exception {

        mPurchaseManager.init(purchaseSystem, null);
        Thread.sleep(1000);
        Receipt receipt = TestUtils.createReceipt("subSku", "subSku", new Date(), new Date());
        mPurchaseManager.mReceiptMap.put("subSku", receipt);
        mPurchaseManager.isPurchaseValid("subSku", new PurchaseManagerListener() {
            @Override
            public void onRegisterSkusResponse(Response response) {

            }

            @Override
            public void onValidPurchaseResponse(Response response, boolean validity, String sku) {

                assertEquals(Response.Status.SUCCESSFUL, response.getStatus());
                assertFalse(validity);
                verify(purchaseSystem).notifyFulfillment(any(String.class), any(UserData.class),
                                                         any(Receipt.class),
                                                         any(Receipt.FulfillmentStatus.class));
                assertFalse(mPurchaseManager.mReceiptMap.containsKey("subSku"));
                verifyUtil.verified();

            }
        });
        Thread.sleep(1000);
        verify(verifyUtil).verified();
    }

    /**
     * tests isPurchaseValid for expired rent purchase
     */
    @Test
    public void testIsPurchaseValidForExpiredRentPurchase() throws Exception {

        mPurchaseManager.init(purchaseSystem, null);
        Thread.sleep(1000);
        Receipt receipt = TestUtils.createReceipt("rentSku", "rentSku", new Date(), null);
        mPurchaseManager.mReceiptMap.put("rentSku", receipt);
        mPurchaseManager.isPurchaseValid("rentSku", new PurchaseManagerListener() {
            @Override
            public void onRegisterSkusResponse(Response response) {

            }

            @Override
            public void onValidPurchaseResponse(Response response, boolean validity, String sku) {

                assertEquals(Response.Status.SUCCESSFUL, response.getStatus());
                assertFalse(validity);
                assertFalse(mPurchaseManager.mReceiptMap.containsKey("rentSku"));
                verify(purchaseSystem).notifyFulfillment(any(String.class), any(UserData.class),
                                                         any(Receipt.class),
                                                         any(Receipt.FulfillmentStatus.class));
                verifyUtil.verified();
            }
        });
        Thread.sleep(1000);
        verify(verifyUtil).verified();
    }

    /**
     * tests isPurchaseValid for non valid purchase
     */
    @Test
    public void testIsPurchaseValidForNonValidPurchase() throws Exception {

        Response response = new Response("isPurchaseValidRequest", Response.Status.SUCCESSFUL,
                                         null);
        doReturn(response).when(mockPurchaseSystem).listenerResponse();
        doReturn(false).when(mockPurchaseSystem).receiptValidResponse();

        mPurchaseManager.init(purchaseSystem, null);
        Thread.sleep(1000);
        Receipt receipt = TestUtils.createReceipt("rentSku", "rentSku", DateAndTimeHelper
                .addSeconds(new Date(), 25), null);
        mPurchaseManager.mReceiptMap.put("rentSku", receipt);
        mPurchaseManager.isPurchaseValid("rentSku", new PurchaseManagerListener() {
            @Override
            public void onRegisterSkusResponse(Response response) {

            }

            @Override
            public void onValidPurchaseResponse(Response response, boolean validity, String sku) {

                assertEquals(Response.Status.SUCCESSFUL, response.getStatus());
                assertFalse(validity);
                assertFalse(mPurchaseManager.mReceiptMap.containsKey("rentSku"));
                verifyUtil.verified();
            }
        });
        Thread.sleep(1000);
        verify(verifyUtil).verified();
    }

    /**
     * tests isPurchaseValid for valid purchase
     */
    @Test
    public void testIsPurchaseValidForValidPurchase() throws Exception {

        Response response = new Response("isPurchaseValidRequest", Response.Status.SUCCESSFUL,
                                         null);
        doReturn(response).when(mockPurchaseSystem).listenerResponse();
        doReturn(true).when(mockPurchaseSystem).receiptValidResponse();

        mPurchaseManager.init(purchaseSystem, null);
        Thread.sleep(1000);
        Receipt receipt = TestUtils.createReceipt("rentSku", "rentSku", DateAndTimeHelper
                .addSeconds(new Date(), 25), null);
        mPurchaseManager.mReceiptMap.put("rentSku", receipt);
        mPurchaseManager.isPurchaseValid("rentSku", new PurchaseManagerListener() {
            @Override
            public void onRegisterSkusResponse(Response response) {

            }

            @Override
            public void onValidPurchaseResponse(Response response, boolean validity, String sku) {

                assertEquals(Response.Status.SUCCESSFUL, response.getStatus());
                assertTrue(validity);
                assertTrue(mPurchaseManager.mReceiptMap.containsKey("rentSku"));
                verifyUtil.verified();
            }
        });
        Thread.sleep(1000);
        verify(verifyUtil).verified();
    }

    /**
     * tests isPurchaseValid for receipt validation system failure
     */
    @Test
    public void testIsPurchaseValidForReceiptValidSystemFailure() throws Exception {

        Response response = new Response("isPurchaseValidRequest", Response.Status.FAILED, null);
        doReturn(response).when(mockPurchaseSystem).listenerResponse();
        doReturn(false).when(mockPurchaseSystem).receiptValidResponse();

        mPurchaseManager.init(purchaseSystem, null);
        Thread.sleep(1000);
        Receipt receipt = TestUtils.createReceipt("rentSku", "rentSku", DateAndTimeHelper
                .addSeconds(new Date(), 25), null);
        mPurchaseManager.mReceiptMap.put("rentSku", receipt);
        mPurchaseManager.isPurchaseValid("rentSku", new PurchaseManagerListener() {
            @Override
            public void onRegisterSkusResponse(Response response) {

            }

            @Override
            public void onValidPurchaseResponse(Response response, boolean validity, String sku) {

                assertEquals(Response.Status.FAILED, response.getStatus());
                assertFalse(validity);
                assertTrue(mPurchaseManager.mReceiptMap.containsKey("rentSku"));
                verifyUtil.verified();
            }
        });
        Thread.sleep(1000);
        verify(verifyUtil).verified();
    }

    /**
     * Test purchase happy path
     */
    @Test
    public void testPurchaseHappyPath() throws Exception {

        Response response = new Response("isPurchaseValidRequest", Response.Status.SUCCESSFUL,
                                         null);
        doReturn(response).when(mockPurchaseSystem).listenerResponse();
        doReturn(true).when(mockPurchaseSystem).receiptValidResponse();

        Response purchaseResponse = new Response("purchaseRequest", Response.Status.SUCCESSFUL,
                                                 null);
        doReturn(purchaseResponse).when(mockPurchaseSystem).purchaseListenerResponse();

        mPurchaseManager.init(purchaseSystem, null);
        Thread.sleep(1000);

        mPurchaseManager.purchaseSku("subSku", new PurchaseManagerListener() {
            @Override
            public void onRegisterSkusResponse(Response response) {

            }

            @Override
            public void onValidPurchaseResponse(Response response, boolean validity, String sku) {

                assertEquals(Response.Status.SUCCESSFUL, response.getStatus());
                assertTrue(validity);
                assertTrue(mPurchaseManager.mReceiptMap.containsKey("subSku"));
                verifyUtil.verified();
            }
        });
        Thread.sleep(1000);
        verify(verifyUtil).verified();
    }

    /**
     * Tests purchase for already purchased product
     */
    @Test
    public void testPurchaseForAlreadyPurchased() throws Exception {

        Receipt receipt = TestUtils.createReceipt("subSku", "subSku", new Date(), null);
        mPurchaseManager.mReceiptMap.put("subSku", receipt);
        mPurchaseManager.init(purchaseSystem, null);
        Thread.sleep(1000);

        mPurchaseManager.purchaseSku("subSku", new PurchaseManagerListener() {
            @Override
            public void onRegisterSkusResponse(Response response) {

            }

            @Override
            public void onValidPurchaseResponse(Response response, boolean validity, String sku) {

                assertEquals(Response.Status.SUCCESSFUL, response.getStatus());
                assertTrue(validity);
                assertTrue(mPurchaseManager.mReceiptMap.containsKey("subSku"));
                verifyUtil.verified();
            }
        });
        Thread.sleep(1000);
        verify(verifyUtil).verified();
    }

    /**
     * Tests purchase failure
     */
    @Test
    public void testPurchaseFailure() throws Exception {

        Response purchaseResponse = new Response("purchaseRequest", Response.Status.FAILED,
                                                 null);
        doReturn(purchaseResponse).when(mockPurchaseSystem).purchaseListenerResponse();

        mPurchaseManager.init(purchaseSystem, null);
        Thread.sleep(1000);

        mPurchaseManager.purchaseSku("subSku", new PurchaseManagerListener() {
            @Override
            public void onRegisterSkusResponse(Response response) {

            }

            @Override
            public void onValidPurchaseResponse(Response response, boolean validity, String sku) {

                assertEquals(Response.Status.FAILED, response.getStatus());
                assertFalse(validity);
                assertFalse(mPurchaseManager.mReceiptMap.containsKey("subSku"));
                verifyUtil.verified();
            }
        });
        Thread.sleep(1000);
        verify(verifyUtil).verified();
    }

    /**
     * Tests purchase with non valid receipt
     */
    @Test
    public void testPurchaseWithNonValidReceipt() throws Exception {

        Response response = new Response("isPurchaseValidRequest", Response.Status.FAILED, null);
        doReturn(response).when(mockPurchaseSystem).listenerResponse();
        doReturn(false).when(mockPurchaseSystem).receiptValidResponse();

        Response purchaseResponse = new Response("purchaseRequest", Response.Status.SUCCESSFUL,
                                                 null);
        doReturn(purchaseResponse).when(mockPurchaseSystem).purchaseListenerResponse();

        mPurchaseManager.init(purchaseSystem, null);
        Thread.sleep(1000);

        mPurchaseManager.purchaseSku("subSku", new PurchaseManagerListener() {
            @Override
            public void onRegisterSkusResponse(Response response) {

            }

            @Override
            public void onValidPurchaseResponse(Response response, boolean validity, String sku) {

                assertEquals(Response.Status.FAILED, response.getStatus());
                assertFalse(validity);
                assertFalse(mPurchaseManager.mReceiptMap.containsKey("subSku"));
                verifyUtil.verified();
            }
        });
        Thread.sleep(1000);
        verify(verifyUtil).verified();
    }


}