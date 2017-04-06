Component Test Framework
========================
Framework for testing library components in an Android project.


## Requirements

- python3


## Installation

Simply clone the repo to your machine.

```bash
git clone git@...
```


## Setup

### Android SDK location

Set the location of the Android SDK. Do one of the following:

- Copy the following into your ~/.bash_profile (or other shell config, such as ~/.zshrc, etc.).
Make sure to reload your shell to load the changes from the config file.
```bash
export ANDROID_HOME=~/Library/Android/sdk
```

- Open EmptyAndroidApp in Android Studio and Android Studio will automatically
set sdk.dir in EmptyAndroidApp/local.properties.

- Specify the Android SDK location at runtime using the -a,--android-home
command line option.

### settings.gradle

Edit the settings.gradle file of the EmptyAndroidApp to list all the component
projects and their project dependencies. Example settings.gradle:

```groovy
include 'app',
        ':ComponentA',
        ':ComponentB'

project(':ComponentA').projectDir = new File(rootProject.projectDir, '../../ComponentA')
project(':ComponentB').projectDir = new File(rootProject.projectDir, '../../ComponentB')
```

### config.json

Create a config.json file to define how to configure the empty app for each
component. The config file is structured as an array of component
configurations. Either use a single single config file that contains all the
components, or use separate config files for each component. Each component
configuration has the following fields:

- componentName - name of the component
- dir - directory of the component relative to the test runner script
- files - (optional) Files to copy from/to the given source/destination. Structured as an array of the following fields:
	- src - (required) source file relative to the test runner script (where it will be copied from)
	- dest - (required) destination file relative to the test runner script (where it will be copied to)
- commands - (optional) Custom commands for running the component's tests. Should only be used if the component requires a command different from the default. Structured as an array of the following fields:
	- command - (required) Shell command to run for the component's tests.
	- success_string - (required) String that is expected in the output of a successful test.

Example config file:

```json
[
  {
    "componentName": "ComponentA",
    "dir": "../ComponentA",
    "files": [
      {
        "src": "./ApiKeys/ComponentA_api_key.txt",
        "dest": "../ComponentA/src/androidTest/assets/api_key.txt"
      }
    ]
  },
  {
    "componentName": "ComponentB",
    "dir": "../ComponentB",
    "commands": [
      {
        "command": "echo Custom command for ComponentB",
        "success_string": "Custom command"
      },
      {
        "command": "echo Another custom command for ComponentB",
        "success_string": "Tests fail because this string isn't in the test output"
      }
    ]
  }
]
```


## Usage

```bash
# Test all the components in the default config file
python3 test_runner.py

# Test all the components in the given config file
python3 test_runner.py -f non_default_config.json

# Test ComponentA and ComponentB and use the default config file
python3 test_runner.py -c ComponentA ComponentB

# Test ComponentA and use the given config file
python3 test_runner.py -c ComponentA -f another_config.json

# Test all the components in the given config files
python3 test_runner.py -f ComponentA_config.json ComponentB_config.json

# Set the Android SDK location and test all the components
python3 test_runner.py -a ~/Library/Andriod/sdk

# Print the help message for more details
python3 test_runner.py -h
```