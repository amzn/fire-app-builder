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

/**
 * Interface for Tasks to be executed by {@link ScheduledBackgroundTask}.
 * The tasks are executed in a background thread.
 */
public interface ITask {

    /**
     * The method to be executed by scheduled job.
     * This method will be executed in a background thread.
     */
    void executeTask();
}
