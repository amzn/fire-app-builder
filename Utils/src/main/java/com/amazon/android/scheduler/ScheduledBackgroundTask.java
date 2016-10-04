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

import com.amazon.utils.ObjectVerification;

import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Provides a way to schedule execution of an {@link ITask} at a defined interval.
 */
public class ScheduledBackgroundTask {

    private static final String TAG = ScheduledBackgroundTask.class.getName();

    /**
     * Internal class to wrap the ITask in a runnable task that can be executed by
     * ScheduledExecutorService
     */
    class RunnableTask implements Runnable {

        private final ITask mTask;

        /**
         * Constructor
         *
         * @param task to execute
         */
        public RunnableTask(ITask task) {

            mTask = task;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {

            try {
                mTask.executeTask();
            }
            catch (Throwable t) { //Catching generic throwable to make sure that further
                // executions are not suppressed
                Log.e(TAG, "Exception in running task ", t);
            }
        }
    }

    /**
     * task to execute
     */
    private ITask mTask;
    /**
     * Initial delay before starting the scheduler
     */
    private long mInitialDelay;
    /**
     * interval of the job
     */
    private long mInterval;
    /**
     * Unit for the {@link #mInterval} and {@link #mInitialDelay}
     */
    private TimeUnit unit;
    /**
     * Single threaded executor pool to run the job
     */
    private final ScheduledExecutorService scheduledPool =
            Executors.newSingleThreadScheduledExecutor();

    /**
     * Constructor
     *
     * @param task         Task to execute.
     * @param initialDelay Initial interval before starting the execution.
     * @param interval     Interval between each execution. The interval is from end of last
     *                     execution and beginning of next execution.
     * @param unit         Unit of interval values.
     */
    public ScheduledBackgroundTask(ITask task, long
            initialDelay, long interval, TimeUnit unit) {

        mTask = ObjectVerification.notNull(task, "task cannot be null");
        mInitialDelay = initialDelay;
        if (interval <= 0) {
            throw new IllegalArgumentException("interval cannot be 0");
        }
        else {
            mInterval = interval;
        }
        this.unit = ObjectVerification.notNull(unit, "unit cannot be null");
    }

    /**
     * Method to start the job.
     */
    public void start() {

        scheduledPool.scheduleWithFixedDelay(new RunnableTask(mTask),
                                             mInitialDelay,
                                             mInterval,
                                             unit);
    }
}
