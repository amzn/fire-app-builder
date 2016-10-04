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

import com.amazon.purchase.model.Product;
import com.amazon.purchase.model.Receipt;
import com.amazon.purchase.model.SkuData;
import com.amazon.purchase.model.UserData;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utils required by purchase testing.
 */
public class TestUtils {

    public static Set<String> validSkuSet = new HashSet<>();
    public static List<Map<String, String>> skuSet = new ArrayList<>();
    public static Map<String, SkuData> skuDataMap = new HashMap<>();
    public static Map<String, Receipt> receiptMap = new HashMap<>();
    public static UserData userData;

    public static void createSkuDataSet() {

        Map<String, String> rentSku = new HashMap<>();
        rentSku.put("sku", "rentSku");
        rentSku.put("productType", "RENT");
        rentSku.put("purchaseSku", "rentSku");
        skuSet.add(rentSku);
        skuDataMap.put("rentSku", new SkuData("rentSku", Product.ProductType.RENT, "rentSku"));
        validSkuSet.add("rentSku");
        Map<String, String> subSku = new HashMap<>();
        subSku.put("sku", "subSku");
        subSku.put("productType", "SUBSCRIBE");
        subSku.put("purchaseSku", "subSku");
        skuSet.add(subSku);
        skuDataMap.put("subSku", new SkuData("subSku", Product.ProductType.SUBSCRIBE, "subSku"));
        validSkuSet.add("subSku");
    }

    public static Receipt createReceipt(String sku, String receiptTitle, Date purchaseDate,
                                        Date expiryDate) {

        Receipt receipt = new Receipt();
        receipt.setPurchasedDate(purchaseDate);
        receipt.setSku(sku);
        receipt.setReceiptId(receiptTitle);
        receipt.setExpiryDate(expiryDate);
        return receipt;
    }
}
