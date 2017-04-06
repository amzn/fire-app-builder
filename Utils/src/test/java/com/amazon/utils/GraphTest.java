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
package com.amazon.utils;

import com.amazon.utils.ds.Graph;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Test class for Graph class testing.
 */
public class GraphTest extends TestCase {

    /**
     * Test get name method.
     */
    @Test
    public void testGetName() throws Exception {

        Graph graph = new Graph("test");

        // Check if we could get name correctly.
        assertEquals("test", graph.getName());
    }

    /**
     * Test node count method.
     */
    @Test
    public void testNodeCount() throws Exception {

        Graph graph = new Graph("test");

        // Check zero count case.
        assertEquals(0, graph.nodeCount());

        // Add a node to graph.
        graph.addNode(new Graph.Node<String>("test"));
        // Check node added case.
        assertEquals(1, graph.nodeCount());
    }

    /**
     * Test edge count method.
     */
    @Test
    public void testEdgeCount() throws Exception {

        Graph graph = new Graph("test");

        // Check zero count case.
        assertEquals(0, graph.edgeCount());

        // Add a node connection to graph.
        graph.addConnection(new Graph.Node<String>("test1"),
                new Graph.Node<String>("test2"));
        // Check node connection added case.
        assertEquals(1, graph.edgeCount());
    }

    /**
     * Test has node method.
     */
    @Test
    public void testHasNode() throws Exception {

        Graph graph = new Graph("test");

        // Check negative case.
        assertFalse(graph.hasNode("test"));

        // Create a node.
        Graph.Node<String> testNode = new Graph.Node<String>("test");
        // Add a node to graph.
        graph.addNode(testNode);

        // Check if node is there?.
        assertEquals(testNode, graph.getNodeByName("test"));
        // Check positive case.
        assertTrue(graph.hasNode("test"));
    }

    /**
     * Test add node method.
     */
    @Test
    public void testAddNode() throws Exception {

        Graph graph = new Graph("test");

        // Add a node to graph.
        graph.addNode(new Graph.Node<String>("test"));
        // Check positive case.
        assertTrue(graph.hasNode("test"));
    }

    /**
     * Test get node by name method.
     */
    @Test
    public void testGetNodeByName() throws Exception {

        Graph graph = new Graph("test");

        // Add a node to graph.
        graph.addNode(new Graph.Node<String>("test"));
        // Check negative case.
        assertNull(graph.getNodeByName("randomNodeName"));
        // Check positive case.
        assertNotNull(graph.getNodeByName("test"));
    }

    /**
     * Test has connection method.
     */
    @Test
    public void testHasConnection() throws Exception {

        Graph graph = new Graph("test");

        // Add a node connection to graph.
        graph.addConnection(new Graph.Node<String>("test1"),
                new Graph.Node<String>("test2"));

        // Check negative case.
        assertFalse(graph.hasConnection("test1", "test2random"));
        // Check node connection added case.
        assertTrue(graph.hasConnection("test1", "test2"));
    }

    /**
     * Test add connection method.
     */
    @Test
    public void testAddConnection() throws Exception {

        Graph graph = new Graph("test");

        // Add a node connection to graph.
        graph.addConnection(new Graph.Node<String>("test1"),
                new Graph.Node<String>("test2"));

        // Check negative case.
        assertFalse(graph.hasConnection("test1", "test2random"));
        // Check positive case.
        assertTrue(graph.hasConnection("test1", "test2"));
    }

    /**
     * Test get nodes as iterable method.
     */
    @Test
    public void testGetNodesIterable() throws Exception {

        Graph graph = new Graph("test");

        // Add a node connection to graph.
        graph.addConnection(new Graph.Node<String>("test1"),
                new Graph.Node<String>("test2"));

        // Positive test case.
        assertNotNull(graph.getNodesIterable());
    }

    /**
     * Test get connections of a node as iterable method.
     */
    @Test
    public void testGetConnectionsOfANode() throws Exception {

        Graph graph = new Graph("test");

        Graph.Node testNode = new Graph.Node<String>("test1");

        // Add a node connection to graph.
        graph.addConnection(testNode,
                new Graph.Node<String>("test2"));

        // Add a node connection to graph.
        graph.addConnection(testNode,
                new Graph.Node<String>("test3"));

        // Positive test case.
        assertNotNull(graph.getConnectionsOfANode("test2"));
    }

    /**
     * Test to string method.
     */
    @Test
    public void testToString() throws Exception {

        Graph graph = new Graph("test");

        String oracleString1 = "{\"test2\":[\"test1\"],\"test1\":[\"test2\"]}";
        String oracleString2 = "{\"test1\":[\"test2\"],\"test2\":[\"test1\"]}";

        // Add a node connection to graph.
        graph.addConnection(new Graph.Node<String>("test1"),
                new Graph.Node<String>("test2"));

        // Check if toString is generating output.
        assertNotNull(graph.toString());

        // Check if toString is generating the right output.
        assertTrue((graph.toString().equals(oracleString1) ||
                graph.toString().equals(oracleString2)));
    }
}