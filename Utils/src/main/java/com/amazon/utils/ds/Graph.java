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
package com.amazon.utils.ds;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Undirected graph implementation which can hold any object with a name.
 */
public class Graph {

    /**
     * Node hash map.
     */
    private final HashMap<Node, HashSet<Node>> mNodes = new HashMap<>();

    /**
     * Number of edges with in the graph.
     */
    private int mEdgeCount;

    /**
     * Name of the graph.
     */
    private final String mName;

    /**
     * Helper node class.
     *
     * @param <T> Type of the node object.
     */
    public static class Node<T> {

        /**
         * Node object reference.
         */
        private T mObject;

        /**
         * Name of the node.
         */
        public final String mName;

        /**
         * Create a node object by setting the name of it.
         *
         * @param name Name of the node.
         */
        public Node(String name) {

            mName = name;
        }

        /**
         * Set node object reference.
         *
         * @param object Object which will be hold by the node.
         */
        public void setObject(T object) {

            this.mObject = object;
        }

        /**
         * Get node object reference.
         *
         * @return Node object reference.
         */
        public T getObject() {

            return this.mObject;
        }

        /**
         * Get name of the node.
         *
         * @return Name of the node.
         */
        public String getName() {

            return this.mName;
        }
    }

    /**
     * Constructor with name.
     *
     * @param name Name of the graph.
     */
    public Graph(String name) {

        mName = name;
    }

    /**
     * Get name of the graph.
     *
     * @return Name of the graph.
     */
    public String getName() {

        return mName;
    }

    /**
     * Get node count.
     *
     * @return Node count.
     */
    public int nodeCount() {

        return mNodes.size();
    }

    /**
     * Get edge count.
     *
     * @return Edge count.
     */
    public int edgeCount() {

        return mEdgeCount;
    }

    /**
     * Check if node exists in graph.
     *
     * @param name Node name.
     * @return True if node exists.
     */
    public boolean hasNode(String name) {

        if (name == null) {
            return false;
        }

        // Traverse nodes.
        for (Node node : mNodes.keySet()) {
            // Check for a match.
            if (node.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add node to graph.
     *
     * @param node Node object.
     */
    public void addNode(Node node) {

        if (node == null) {
            return;
        }

        // Check if node already exists.
        if (!hasNode(node.getName())) {
            // Add node.
            mNodes.put(node, new HashSet<>());
        }
    }

    /**
     * Get node by name.
     *
     * @param name Node name.
     * @return Node reference if it exists.
     */
    public Node getNodeByName(String name) {

        if (name == null) {
            return null;
        }

        // Traverse nodes.
        for (Node node : mNodes.keySet()) {
            // Check if there is a match.
            if (node.getName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Find out if two given nodes are connected.
     *
     * @param nodeName1 First node name.
     * @param nodeName2 Second node name.
     * @return True if connection exists between given nodes.
     */
    public boolean hasConnection(String nodeName1, String nodeName2) {
        // Check if nodes are in graph or not first.
        if (!hasNode(nodeName1) || !hasNode(nodeName2)) {
            return false;
        }

        // Traverse nodes.
        for (Map.Entry<Node, HashSet<Node>> entry : mNodes.entrySet()) {
            Node node = entry.getKey();
            // Find first node first.
            if (node.getName().equals(nodeName1)) {
                // Traverse connections of first node.
                for (Node subNode : entry.getValue()) {
                    // Check if there is a match.
                    if (subNode.getName().equals(nodeName2)) {
                        return true;
                    }
                }
                // Skip other nodes if we couldn't find connection.
                return false;
            }
        }

        return false;
    }

    /**
     * Add connection between two nodes, first add nodes if they are not already in the graph.
     *
     * @param node1 First node reference.
     * @param node2 Second node reference.
     */
    public void addConnection(Node node1, Node node2) {

        if (node1 == null || node2 == null) {
            return;
        }

        // Make sure we are not adding a different node object with same node name.
        if ((hasNode(node1.getName()) && node1 != getNodeByName(node1.getName())) ||
                (hasNode(node2.getName()) && node2 != getNodeByName(node2.getName()))) {
            throw new RuntimeException("Node(s) already exists!");
        }

        // Add first node.
        addNode(node1);
        // Add second node.
        addNode(node2);

        // Check if they are already connected or not.
        if (!hasConnection(node1.getName(), node2.getName())) {
            // Connect first node to second.
            mNodes.get(node1).add(node2);
            // Connect second node to first.
            mNodes.get(node2).add(node1);
            // Increment edge count.
            mEdgeCount++;
        }
    }

    /**
     * Get nodes as iterable.
     *
     * @return Nodes iterable.
     */
    public Iterable<Node> getNodesIterable() {

        return mNodes.keySet();
    }

    /**
     * Get connections of a node.
     *
     * @param name Name of the node.
     * @return HashSet for node's connections.
     */
    public HashSet<Node> getConnectionsOfANode(String name) {

        if (name == null || getNodeByName(name) == null) {
            return null;
        }

        return mNodes.get(getNodeByName(name));
    }

    /**
     * Get string representation of the graph.
     *
     * @return String representation of the graph as Json.
     */
    @Override
    public String toString() {
        // Create a new string builder.
        StringBuilder sb = new StringBuilder();
        // Add Json object start point.
        sb.append("{");
        // Traverse all the nodes.
        for (Map.Entry<Node, HashSet<Node>> entry : mNodes.entrySet()) {
            Node node = entry.getKey();
            // Append node name.
            sb.append("\"").append(node.getName()).append("\":[");
            // Append connections as an array.
            for (Node subNode : entry.getValue()) {
                sb.append("\"").append(subNode.getName()).append("\",");
            }
            // Remove last comma.
            if (entry.getValue().size() != 0) {
                sb.setLength(sb.length() - 1);
            }
            sb.append("],");
        }
        // Remove last comma.
        sb.setLength(sb.length() - 1);
        // Append Json object end point.
        sb.append("}");
        return sb.toString();
    }
}

