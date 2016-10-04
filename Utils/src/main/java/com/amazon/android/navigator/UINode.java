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
package com.amazon.android.navigator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * Class which defines how a UI node is represented in Navigator json file.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UINode {

    /**
     * Verify screen access flag.
     */
    private boolean verifyScreenAccess;

    /**
     * Verify network connection flag.
     */
    private boolean verifyNetworkConnection;

    /**
     * OnAction string.
     */
    private String onAction;

    /**
     * Load type as string.
     */
    private String loadType;

    /**
     * Node extras.
     */
    private Map<String, String> extras;

    /**
     * Nodes list.
     */
    private List<String> nodes;

    /**
     * Recipe list.
     */
    private List<Map> recipes;

    /**
     * Load Type enum.
     */
    public enum LoadType {
        PRE,
        POST
    }

    /**
     * Constructor.
     */
    public UINode() {

    }

    /**
     * Get verify screen access flag.
     *
     * @return Verify screen access flag.
     */
    public boolean isVerifyScreenAccess() {

        return verifyScreenAccess;
    }

    /**
     * Set verify screen access flag.
     *
     * @param verifyScreenAccess Screen access flag.
     */
    public void setVerifyScreenAccess(boolean verifyScreenAccess) {

        this.verifyScreenAccess = verifyScreenAccess;
    }

    /**
     * Get verify network connection flag.
     *
     * @return Verify network connection flag.
     */
    public boolean isVerifyNetworkConnection() {

        return verifyNetworkConnection;
    }

    /**
     * Set verify network connection flag.
     *
     * @param verifyNetworkConnection Verify network connection flag.
     */
    public void setVerifyNetworkConnection(boolean verifyNetworkConnection) {

        this.verifyNetworkConnection = verifyNetworkConnection;
    }

    /**
     * Get load type.
     *
     * @return Load type as string.
     */
    public String getLoadType() {

        return loadType;
    }

    /**
     * Get load type.
     *
     * @param loadType Load type as string.
     */
    public void setLoadType(String loadType) {

        this.loadType = loadType;
    }

    /**
     * Get on action string.
     *
     * @return OnAction string.
     */
    public String getOnAction() {

        return onAction;
    }

    /**
     * Set OnAction string.
     *
     * @param onAction OnAction string.
     */
    public void setOnAction(String onAction) {

        this.onAction = onAction;
    }

    /**
     * Get extras map.
     *
     * @return Extras map.
     */
    public Map<String, String> getExtras() {

        return extras;
    }

    /**
     * Set extras map.
     *
     * @param extras Extras map.
     */
    public void setExtras(Map<String, String> extras) {

        this.extras = extras;
    }

    /**
     * Get nodes list.
     *
     * @return Nodes list.
     */
    public List<String> getNodes() {

        return nodes;
    }

    /**
     * Set nodes list.
     *
     * @param nodes Nodes list.
     */
    public void setNodes(List<String> nodes) {

        this.nodes = nodes;
    }

    /**
     * Get recipes list.
     *
     * @return Recipes list.
     */
    public List<Map> getRecipes() {

        return recipes;
    }

    /**
     * Set recipes list.
     *
     * @param recipes Recipes list.
     */
    public void setRecipes(List<Map> recipes) {

        this.recipes = recipes;
    }
}
