#! /bin/bash

# Copyright 2015-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# A copy of the License is located at
#
#     http://aws.amazon.com/apache2.0/
#
# or in the "license" file accompanying this file. This file is distributed
# on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
# express or implied. See the License for the specific language governing
# permissions and limitations under the License.

# This script restores the Calypso sample app to its default configuration.
# It must be executed from its current containing folder.

FUNC_TEST=FunctionalTest.java
PERF_TEST=PerformanceTest.java

echo Restoring Calypso sample app to default configuration...

if test -e default/Navigator.json
then
    cp default/Navigator.json ../main/assets/.
else
    echo Navigator.json not found, restoration may have failed
fi

if test -e default/configurations/BasicFileBasedDownloaderConfig.json
then
    cp default/configurations/BasicFileBasedDownloaderConfig.json ../main/assets/configurations/.
else
    echo BasicFileBasedDownloaderConfig.json not found, restoration may have failed
fi

if test -e default/configurations/DataLoadManagerConfig.json
then
    cp default/configurations/DataLoadManagerConfig.json ../main/assets/configurations/.
else
    echo DataLoadManagerConfig.json not found, restoration may have failed
fi

echo Done!
