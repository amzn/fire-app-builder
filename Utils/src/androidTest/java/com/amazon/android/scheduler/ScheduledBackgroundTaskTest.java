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
package com.amazon.android.scheduler;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link ScheduledBackgroundTask}
 */
public class ScheduledBackgroundTaskTest {

    public class MockTask implements ITask {

        @Override
        public void executeTask() {

        }
    }

    /**
     * Tests for happy path case validating invocations of task
     */
    @Test
    public void testScheduledBackgroundTaskPositiveCase() throws InterruptedException {

        MockTask task = mock(MockTask.class);
        ScheduledBackgroundTask scheduledBackgroundTask =
                new ScheduledBackgroundTask(task, 0, 1, TimeUnit.SECONDS);

        scheduledBackgroundTask.start();
        Thread.sleep(4000);
        verify(task, atLeast(2)).executeTask();
    }

    /**
     * Test failure for invalid argument
     */
    @Test
    public void testInvalidArguments() {

        MockTask task = mock(MockTask.class);
        ScheduledBackgroundTask scheduledBackgroundTask = null;
        try {
            scheduledBackgroundTask = new ScheduledBackgroundTask(task, 0, -1, TimeUnit.SECONDS);
            fail("should have failed");
        }
        catch (IllegalArgumentException e) {

        }

        try {
            scheduledBackgroundTask = new ScheduledBackgroundTask(null, 0, 1, TimeUnit.SECONDS);
            fail("should have failed");
        }
        catch (IllegalArgumentException e) {

        }

        try {
            scheduledBackgroundTask = new ScheduledBackgroundTask(task, 0, 1, null);
            fail("should have failed");
        }
        catch (IllegalArgumentException e) {

        }
    }

    /**
     * Any failure of the task execution should not stop further executions, this validates the
     * same.
     */
    @Test
    public void testScheduledBackgroundTaskWithTaskFailure() throws InterruptedException {

        MockTask task = mock(MockTask.class);
        doThrow(new RuntimeException("dummy exception")).when(task).executeTask();

        ScheduledBackgroundTask scheduledBackgroundTask =
                new ScheduledBackgroundTask(task, 0, 1, TimeUnit.SECONDS);

        scheduledBackgroundTask.start();
        Thread.sleep(4000);
        verify(task, atLeast(2)).executeTask();
    }
}
