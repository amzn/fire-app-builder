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
package com.amazon.android.model.content;

import com.amazon.utils.StringManipulation;

import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

/**
 * ContentContainer class is a container for {@link Content} objects.
 */
public class ContentContainer implements Iterable<Content> {

    /**
     * Debug TAG.
     */
    private static final String TAG = ContentContainer.class.getSimpleName();

    /**
     * Debugging enable flag.
     */
    private static final boolean DEBUG = false;

    /**
     * Name of the container.
     */
    private String mName;

    /**
     * List of ContentContainers in this ContentContainer.
     */
    private LinkedList<ContentContainer> mContentContainers = new LinkedList<>();

    /**
     * List of {@link Content} in this ContentContainer.
     */
    private LinkedList<Content> mContents = new LinkedList<>();

    /**
     * Helper extra data storage.
     */
    private HashMap<String, Object> mExtras;

    /**
     * Constant for title field name.
     */
    public static final String NAME_FIELD_NAME = "mName";

    /**
     * Constructs an empty ContentContainer.
     */
    public ContentContainer() {

    }

    /**
     * Constructs a ContentContainer with a name.
     *
     * @param name Name of the container.
     */
    public ContentContainer(String name) {

        mName = name;
    }

    /**
     * Creates a new instance of ContentContainer.
     *
     * @param name Name of the container.
     * @return New instance reference.
     */
    public static ContentContainer newInstance(String name) {

        return new ContentContainer(name);
    }

    /**
     * Add {@link Content} object into this container.
     *
     * @param content Content object.
     * @return The reference to this container.
     */
    public ContentContainer addContent(Content content) {
        mContents.add(content);
        return this;
    }

    /**
     * Add a ContentContainer object into this container.
     *
     * @param contentContainer The ContentContainer object to add.
     * @return The reference to this container.
     */
    public ContentContainer addContentContainer(ContentContainer contentContainer) {

        mContentContainers.add(contentContainer);
        return this;
    }

    /**
     * Get the count of {@link Content} objects that are directly attached to this container.
     *
     * @return Count of the {@link Content} objects.
     */
    public int getContentCount() {

        return mContents.size();
    }

    /**
     * Get list of {@link Content} objects that are directly attached to this container.
     *
     * @return List of {@link Content}.
     */
    public List<Content> getContents() {

        return mContents;
    }

    /**
     * Get list of ContentContainer objects under this container.
     *
     * @return List of ContentContainers.
     */
    public List<ContentContainer> getContentContainers() {

        return mContentContainers;
    }

    /**
     * Get the count of ContentContainer objects which are directly attached to this container.
     *
     * @return Count of the ContentContainer objects.
     */
    public int getContentContainerCount() {

        return mContentContainers.size();
    }

    /**
     * Get the ContentContainer that is located at the argument index.
     *
     * @param index Location index.
     * @return ContentContainer object at location.
     */
    public ContentContainer getChildContentContainerAtIndex(int index) {

        return mContentContainers.get(index);
    }

    /**
     * Determine if this ContentContainer has sub containers.
     *
     * @return True if there are sub-containers; false otherwise.
     */
    public boolean hasSubContainers() {

        return (mContentContainers.size() != 0);
    }

    /**
     * Find content container by name.
     *
     * @param name Container name to be searched.
     * @return Found content container reference.
     */
    public ContentContainer findContentContainerByName(String name) {
        ContentContainer result = null;
        for (ContentContainer contentContainer:getContentContainers()) {
            if (contentContainer.getName().equals(name)) {
                return contentContainer;
            }
        }

        return result;
    }

    /**
     * Remove empty sub containers from this container.
     */
    public void removeEmptySubContainers() {

        ListIterator<ContentContainer> contentContainerListIterator = getContentContainers()
                .listIterator();

        while (contentContainerListIterator.hasNext()) {
            ContentContainer contentContainer = contentContainerListIterator.next();
            if (contentContainer.getContentCount() == 0 &&
                    contentContainer.getContentContainerCount() == 0) {
                contentContainerListIterator.remove();
            }
        }
    }

    /**
     * Get the name of this ContentContainer.
     *
     * @return Name of this ContentContainer as a String.
     */
    public String getName() {

        return mName;
    }

    /**
     * Set name of this ContentContainer.
     *
     * @param name Name of this ContentContainer as a String.
     */
    public void setName(String name) {

        mName = name;
    }

    /**
     * Get extra data as a string from the internal map.
     *
     * @param key Key value as a string.
     * @return Value as a string.
     */
    public String getExtraStringValue(String key) {

        if (mExtras == null || mExtras.get(key) == null) {
            return null;
        }
        return mExtras.get(key).toString();
    }

    /**
     * Set extra data into a map. The map will be created the first time a value is set. If the key
     * already exists, its value will be overwritten with the newly supplied value.
     *
     * @param key   Key value as string.
     * @param value Value.
     */
    public void setExtraValue(String key, Object value) {

        if (mExtras == null) {
            mExtras = new HashMap<>();
        }

        mExtras.put(key, value);
    }

    /**
     * String representation of this ContentContainer object.
     *
     * @return String representation of this ContentContainer object.
     */
    @Override
    public String toString() {

        return "ContentContainer-> name: " + mName + " mExtras: " + mExtras;
    }

    /**
     * Tests that two {@link ContentContainer} objects are equal.
     *
     * @param o The reference object with which to compare.
     * @return True if this object is the same as the o argument; false otherwise.
     */
    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContentContainer container = (ContentContainer) o;

        if (getName() != null ? !getName().equals(container.getName()) : container.getName() !=
                null)
            return false;
        if (getContentContainers() != null ? !getContentContainers().equals(
                container.getContentContainers()) : container.getContentContainers() != null)
            return false;
        if (mContents != null ? !mContents.equals(container.mContents) : container.mContents !=
                null)
            return false;
        return !(mExtras != null ? !mExtras.equals(container.mExtras) : container.mExtras != null);

    }

    /**
     * Iterable implementation.
     *
     * @return {@link FlatContentIterator} that lets you traverse all {@link Content} of a
     * ContentContainer including the content of its sub-containers.
     */
    @Override
    public Iterator<Content> iterator() {

        return new FlatContentIterator();
    }

    /**
     * This class lets you iterate though all content objects within a container.
     */
    private class FlatContentIterator implements Iterator<Content> {

        /**
         * Current content index under current container.
         */
        private int icCurrentContentsIndex;

        /**
         * Current content container.
         */
        private ContentContainer icCurrentContentContainer;

        /**
         * Current content list reference.
         */
        private LinkedList<Content> icCurrentContents;

        /**
         * Stack of all content containers under a container recursively.
         */
        private Stack<ContentContainer> icContentContainerStack = new Stack<>();

        /**
         * Recursively add all sub containers to a stack
         *
         * @param stack            Stack in use for recursion.
         * @param contentContainer ContentContainer to be traversed.
         */
        private void addToStack(Stack<ContentContainer> stack, ContentContainer contentContainer) {
            // Push ContentContainer to stack.
            stack.push(contentContainer);
            // Skip Containers with no sub containers.
            if (contentContainer.getContentContainerCount() == 0) {
                return;
            }
            // Add all the sub containers recursively.
            for (ContentContainer cci : contentContainer.mContentContainers) {
                addToStack(icContentContainerStack, cci);
            }
        }

        /**
         * Constructors a FlatContentIterator.
         */
        public FlatContentIterator() {
            // Zero current content index.
            icCurrentContentsIndex = 0;
            // Set the current contents to root container's contents.
            icCurrentContents = mContents;

            if (DEBUG) {
                Log.d(TAG, "FlatContentIterator -> mContentContainers size:" + mContentContainers
                        .size());
            }

            // Add all sub containers of the root.
            for (ContentContainer cc : mContentContainers) {
                if (!(cc.getContentCount() == 0 && cc.getContentContainerCount() == 0)) {
                    addToStack(icContentContainerStack, cc);
                }
            }

            // Print Stack for debugging only.
            if (DEBUG) {
                Log.d(TAG, "FlatContentIterator -> contentContainerStack size:" +
                        icContentContainerStack.size());
                for (ContentContainer cc : icContentContainerStack) {
                    Log.d(TAG, "contentContainerStack item:" + cc.getName());
                }
            }
        }

        /**
         * {@inheritDoc}
         *
         * @return False if there are no {@link Content} objects left to be traversed.
         */
        @Override
        public boolean hasNext() {
            // hasNext if stack still has containers and/or contents under container.
            return !((icCurrentContentsIndex == icCurrentContents.size() ||
                    icCurrentContents.size() == 0) && icContentContainerStack.size() == 0);
        }

        /**
         * {@inheritDoc}
         *
         * @return The next {@link Content} object from the flattened list.
         */
        @Override
        public Content next() {

            Content content = null;

            // Still tackling icCurrentContents.
            if (icCurrentContentsIndex < icCurrentContents.size()) {
                content = icCurrentContents.get(icCurrentContentsIndex++);
            }

            // Prepare next step.
            if (icCurrentContentsIndex == icCurrentContents.size() && icContentContainerStack
                    .size() != 0) {
                // Get a sub container.
                icCurrentContentContainer = icContentContainerStack.pop();
                // Set current contents.
                icCurrentContents = icCurrentContentContainer.mContents;
                // Zero current content index.
                icCurrentContentsIndex = 0;
                // Prev container has no content.
                if (content == null && icCurrentContentsIndex < icCurrentContents.size()) {
                    content = icCurrentContents.get(icCurrentContentsIndex++);
                }
            }

            return content;
        }

        /**
         * Remove is unsupported!
         */
        @Override
        public void remove() {

            throw new UnsupportedOperationException();
        }
    }

    /**
     * Searches the container and all sub-containers for specific content Id.
     *
     * @param contentId Id of content to search for
     * @return returns content object if one exists, else null
     */
    public Content findContentById(String contentId) {

        if (contentId == null) {
            return null;
        }
        Iterator<Content> contentIterator = iterator();
        while (contentIterator.hasNext()) {
            Content content = contentIterator.next();
            if (StringManipulation.areStringsEqual(contentId, content.getId())) {
                return content;
            }
        }
        return null;
    }
}
