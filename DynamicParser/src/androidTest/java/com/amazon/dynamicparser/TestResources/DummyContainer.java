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

package com.amazon.dynamicparser.testResources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a dummy container model used to test the object creation using the
 * "SampleVideoFeed.json" file.
 */
public class DummyContainer {

    /**
     * Constant for id field.
     */
    public static final String ID_FIELD_NAME = "mId";
    /**
     * Constant for name field.
     */
    public static final String NAME_FIELD_NAME = "mName";
    /**
     * Constant for long field.
     */
    public static final String LONG_FIELD_NAME = "mLong";
    /**
     * Constant for short field.
     */
    public static final String SHORT_FIELD_NAME = "mShort";
    /**
     * Constant for char field.
     */
    public static final String CHAR_FIELD_NAME = "mChar";
    /**
     * Constant for float field.
     */
    public static final String FLOAT_FIELD_NAME = "mFloat";
    /**
     * Constant for boolean field.
     */
    public static final String BOOLEAN_FIELD_NAME = "mBoolean";
    /**
     * Constant for double field.
     */
    public static final String DOUBLE_FIELD_NAME = "mDouble";
    /**
     * Constant for byte field.
     */
    public static final String BYTE_FIELD_NAME = "mByte";
    /**
     * Constant for extras field.
     */
    public static final String EXTRAS_FIELD_NAME = "mExtras";

    /**
     * The member variables. Added one of each primitive type for testing purposes.
     */
    private int mId;
    private String mName;
    private long mLong;
    private short mShort;
    private char mChar;
    private float mFloat;
    private boolean mBoolean;
    private double mDouble;
    private byte mByte;
    private List<DummyContent> mContent = new ArrayList<>();
    private Map<String, Object> mExtras;

    /**
     * Get the id.
     *
     * @return The id.
     */
    public int getId() {

        return mId;
    }

    /**
     * Set the id.
     *
     * @param id The id.
     */
    public void setId(int id) {

        mId = id;
    }

    /**
     * Get the name.
     *
     * @return The name.
     */
    public String getName() {

        return mName;
    }

    /**
     * Set the name.
     *
     * @param name The name.
     */
    public void setName(String name) {

        mName = name;
    }

    /**
     * Get the long.
     *
     * @return The long.
     */
    public long getLong() {

        return mLong;
    }

    /**
     * Set the long.
     *
     * @param aLong The long.
     */
    public void setLong(long aLong) {

        mLong = aLong;
    }

    /**
     * Get the short.
     *
     * @return The short.
     */
    public short getShort() {

        return mShort;
    }

    /**
     * Set the short.
     *
     * @param aShort The short.
     */
    public void setShort(short aShort) {

        mShort = aShort;
    }

    /**
     * Get the char.
     *
     * @return The char.
     */
    public char getChar() {

        return mChar;
    }

    /**
     * Set the char.
     *
     * @param aChar The char.
     */
    public void setChar(char aChar) {

        mChar = aChar;
    }

    /**
     * Get the boolean.
     *
     * @return The boolean.
     */
    public boolean getBoolean() {

        return mBoolean;
    }

    /**
     * Set the boolean.
     *
     * @param aBoolean The boolean.
     */
    public void setBoolean(boolean aBoolean) {

        mBoolean = aBoolean;
    }

    /**
     * Get the float.
     *
     * @return The float.
     */
    public float getFloat() {

        return mFloat;
    }

    /**
     * Set the float
     *
     * @param aFloat The float.
     */
    public void setFloat(float aFloat) {

        mFloat = aFloat;
    }

    /**
     * Get the double.
     *
     * @return The double.
     */
    public double getDouble() {

        return mDouble;
    }

    /**
     * Set the double.
     *
     * @param aDouble The double.
     */
    public void setDouble(double aDouble) {

        mDouble = aDouble;
    }

    /**
     * Get the byte.
     *
     * @return The byte.
     */
    public byte getByte() {

        return mByte;
    }

    /**
     * Set the byte.
     *
     * @param aByte The byte.
     */
    public void setByte(byte aByte) {

        mByte = aByte;
    }

    /**
     * Get the content list.
     *
     * @return The content list.
     */
    public List<DummyContent> getContent() {

        return mContent;
    }

    /**
     * Set the content list.
     *
     * @param content The content list.
     */
    public void setContent(List<DummyContent> content) {

        mContent = content;
    }

    /**
     * Get extra data as string from internal map.
     *
     * @param key Key value as string.
     * @return Value as string.
     */
    public Object getExtraStringValue(String key) {

        if (mExtras == null) {
            return null;
        }

        return mExtras.get(key);
    }

    /**
     * Set extra data into a map, Map will be created with first setting.
     *
     * @param key   Key value as string.
     * @param value Value as string.
     */
    public void setExtraValue(String key, Object value) {

        if (mExtras == null) {
            mExtras = new HashMap<>();
        }

        mExtras.put(key, value);
    }
}
