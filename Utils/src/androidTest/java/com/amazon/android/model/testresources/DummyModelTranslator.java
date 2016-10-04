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
package com.amazon.android.model.testresources;

import com.amazon.android.model.AModelTranslator;


/**
 * This is a dummy implementation of the {@link AModelTranslator} interface used for testing the
 * interface. This class will translate maps into {@link DummyModelTranslator.TestObject}s.
 */
public class DummyModelTranslator extends AModelTranslator<DummyModelTranslator.TestObject> {

    private static final String TAG = DummyModelTranslator.class.getSimpleName();

    /**
     * The object this interface is implemented for.
     */
    public class TestObject {

        /**
         * A test field.
         */
        public String field1;
        /**
         * Another test field.
         */
        public String field2;

        /**
         * Empty constructor.
         */
        public TestObject() {

        }

        /**
         * Constructor that allows you to set the member variables.
         *
         * @param f1 field1
         * @param f2 field2
         */
        public TestObject(String f1, String f2) {

            this.field1 = f1;
            this.field2 = f2;
        }

        /**
         * Compares two {@link DummyModelTranslator.TestObject} for
         * equality. If the member variables have the same value, they are equal objects.
         *
         * @param o The object to compare against.
         * @return True if they equal; false otherwise
         */
        @Override
        public boolean equals(Object o) {

            if (o instanceof TestObject) {
                TestObject obj = (TestObject) o;
                return field1.equals(obj.field1) && field2.equals(obj.field2);
            }
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestObject instantiateModel() {

        return new TestObject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setMemberVariable(TestObject model, String field, Object value) {

        boolean variableSet = true;
        switch (field) {
            case "field1":
                model.field1 = (String) value;
                break;
            case "field2":
                model.field2 = (String) value;
                break;
            default:
                variableSet = false;
                break;
        }
        return variableSet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateModel(TestObject model) {

        return !(model.field1 == null || model.field2 == null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {

        return DummyModelTranslator.class.getSimpleName();
    }
}
