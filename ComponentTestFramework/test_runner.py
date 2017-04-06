#!/usr/bin/env python3
#
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

"""
Test runner for Android library components.
"""

import argparse
import json
import sys
import subprocess
import os
from collections import namedtuple

# String constants
DEFAULT_CONFIG_FILE_NAME = "config.json"
TEST_APP_NAME = "EmptyAndroidApp"
ANDROID_HOME = "ANDROID_HOME"
# Config file key names
COMPONENT_NAME = "componentName"
COMPONENT_DIR = "dir"
FILES_ARRAY = "files"
FILE_SRC = "src"
FILE_DEST = "dest"
CUSTOM_COMMANDS = "commands"
CUSTOM_COMMAND = "command"
CUSTOM_SUCCESS_STRING = "success_string"

# Colors and Formatting
OKAY_GREEN = "\033[92m"
FAIL_RED = "\033[91m"
BOLD = "\033[1m"
END_COLORS = "\033[0m"


def safe_print(str, min_verbosity=0, end="\n"):
	"""Print the string as specified by the quiet and verbose options.

	Do nothing if --quiet was specified on the command line. Print the string if
	the given min verbosity is greater than the verbosity specified on the
	command line.

	:param str: string to print
	:param min_verbosity: minimum verbosity required to print the string
	:param end: line ending to pass to print
	"""
	if command_line_args.quiet:
		return
	if command_line_args.verbose >= min_verbosity:
		print(str, end=end)


def execute_command(command, return_output=False, min_verbosity=0):
	"""Execute the given command in a subprocess.

	:param command: string of the command to execute
	:param return_output: return the full output if true, otherwise return an empty string
	"""
	safe_print(command, 1)
	process = subprocess.Popen(command.split(), stdout=subprocess.PIPE, stderr=subprocess.STDOUT, universal_newlines=True)
	output = ""
	for line in process.stdout:
		if return_output:
			output += line
		safe_print(line, end="", min_verbosity=min_verbosity)
	return output


def make_directory(dirname):
	"""mkdir -p dirname"""
	if len(dirname) <= 0:
		return
	mkdir_command = "mkdir -p " + dirname
	execute_command(mkdir_command)


def copy_file(src, dest):
	"""Copy a file from source to the destination.

	Copy a file from the source file to the destination file. Over write the
	destination file if it already exists. Create missing directories as
	necessary.

	:param src: filename to copy from
	:param dest: filename to copy to
	"""
	dirname = os.path.dirname(dest)
	make_directory(dirname)
	copy_command = "cp " + src + " " + dest
	execute_command(copy_command)


def remove_file(filename):
	"""rm filename"""
	rm_command = "rm " + filename
	execute_command(rm_command)


def setup(config):
	"""Setup the test app and components for testing.

	:param config: configuration options to setup
	"""
	safe_print("Setup for " + config[COMPONENT_NAME], min_verbosity=1)
	if FILES_ARRAY in config:
		for file in config[FILES_ARRAY]:
			copy_file(file[FILE_SRC], file[FILE_DEST])


def print_tests_passed_or_failed(test_output, success_string="BUILD SUCCESSFUL"):
	"""Print a short message describing whether the tests passed or failed.

	:param test_output: string of the output from the test
	:param success_string: string that will be found in the output of successful tests
	"""
	if test_output.find(success_string) >= 0:
		safe_print(OKAY_GREEN + "Tests passed" + END_COLORS)
		return
	safe_print(FAIL_RED + "Tests failed" + END_COLORS)


def run_test(config):
	"""Run the tests for the component defined the given configuration.

	:param config: configuration options for the component
	"""
	safe_print(BOLD + "Run tests for " + config[COMPONENT_NAME] + END_COLORS)
	# Run custom commands
	if CUSTOM_COMMANDS in config:
		for command in config[CUSTOM_COMMANDS]:
			output = execute_command(command[CUSTOM_COMMAND], return_output=True, min_verbosity=1)
			print_tests_passed_or_failed(output, success_string=command[CUSTOM_SUCCESS_STRING])
	else:
		run_test_command = TEST_APP_NAME + "/gradlew connectedAndroidTest test --project-dir " + config[COMPONENT_DIR] + " --settings-file " + TEST_APP_NAME + "/settings.gradle"
		output = execute_command(run_test_command, return_output=True, min_verbosity=1)
		print_tests_passed_or_failed(output)


def tear_down(config):
	"""Undo modifications done by setup.

	If a directory is created in setup, it is not removed here.

	:param config: configuration options to undo
	"""
	safe_print("Tear down for " + config[COMPONENT_NAME], min_verbosity=1)
	if FILES_ARRAY in config:
		for file in config[FILES_ARRAY]:
			remove_file(file[FILE_DEST])


def test_component(config, dry_run=False):
	"""Setup, run, and tear down the tests for the given component.

	:param config: configuration options for the component
	:param dry_run: see which components will be tested without actually running the tests
	"""
	if dry_run:
		safe_print("Dry run for: " + config[COMPONENT_NAME], 0)
		return
	setup(config)
	run_test(config)
	tear_down(config)


def test_components(config_data, dry_run=False):
	"""Run the tests for the given components.

	Run the unit tests for all the given components, or all the components in
	the config file if no components are given.

	:param config_data: list of test configurations for each component
	:param dry_run: see which components will be tested without actually running the tests
	"""
	for element in config_data:
		test_component(element, dry_run)


def verify_files_array(files_array):
	"""Verify the files array of the config file

	:param files_array: files array to verify
	"""
	for element in files_array:
		if FILE_SRC not in element:
			return False
		if FILE_DEST not in element:
			return False
	return True


def verify_custom_commands(custom_commands):
	"""Verify the custom command of the config file

	:param custom_commands: commands array to verify
	"""
	for command in custom_commands:
		if CUSTOM_COMMAND not in command:
			return False
		if CUSTOM_SUCCESS_STRING not in command:
			return False
	return True

def verify_config_file(config_data):
	"""Check that the config file is structured properly.

	:param config_data: list of test configurations for each component
	"""
	for element in config_data:
		# componentName
		if COMPONENT_NAME not in element:
			return False
		# dir
		if COMPONENT_DIR not in element:
			return False
		if FILES_ARRAY in element:
			if not verify_files_array(element[FILES_ARRAY]):
				return False
		if CUSTOM_COMMANDS in element:
			if not verify_custom_commands(element[CUSTOM_COMMANDS]):
				return False
	return True


def get_configs_to_test(config_data, components):
	"""Get a list of configurations for given components.

	Return a list of configurations that is the set intersection of the given
	config list and component names. Return the given configuration list if
	no component names are given.

	:param config_data: list of test configurations for each component
	:param components: list of component names to test
	"""
	if not components:
		return config_data
	return [x for x in config_data if x[COMPONENT_NAME] in components]


def parse_command_line_arguments(argv):
	"""Parse the command line arguments in the given array.

	:param argv: array of command line arguments with the program name removed
	"""
	parser = argparse.ArgumentParser(description=__doc__)
	parser.add_argument("-f", "--config-file", nargs="+", help="file that defines how to configure the test app",
		default=[DEFAULT_CONFIG_FILE_NAME], metavar="file")
	parser.add_argument("-c", "--components", nargs="+", help="list of components to test",
		metavar="component")
	parser.add_argument("-a", "--android-home", help="set the " + ANDROID_HOME + " environment variable",
		metavar="directory")
	parser.add_argument("-d", "--dry-run", help="see which components will be tested without actually running the tests",
		action="store_true", default=False)
	mutex_group = parser.add_mutually_exclusive_group()
	mutex_group.add_argument("-v", "--verbose", action="count", default=0,
		help="increase the verbosity of the program's output")
	mutex_group.add_argument("-q", "--quiet", action="store_true",
		help="don't produce any output")
	return parser.parse_args(argv);


def parse_config_file(filename):
	"""Open the config file and parse as a JSON file.

	:param filename: name of the config file
	"""
	with open(filename) as config_file:
		return json.load(config_file)


def parse_config_files(filenames):
	"""Load the given config files into a list of config data.

	:param filenames: names of config files to load
	"""
	config_data = []
	for filename in filenames:
		config_list = parse_config_file(filename)
		# Add the configs from in config_list to config_data
		for config_item in config_list:
			# Check if the config_item already exists in config_data
			if [x for x in config_data if x[COMPONENT_NAME] in config_item[COMPONENT_NAME]]:
				safe_print("Config for " + config_item[COMPONENT_NAME] + " is duplicated in " + filename + " ... skipping", 0)
				continue
			config_data.append(config_item)
	return config_data


def main(argv):
	# Get the command line arguments
	global command_line_args
	command_line_args = parse_command_line_arguments(argv)
	# Set the ANDROID_HOME environment variable
	if command_line_args.android_home:
		os.environ[ANDROID_HOME] = command_line_args.android_home
	# open the config files
	config_data = parse_config_files(command_line_args.config_file)
	if not verify_config_file(config_data):
		safe_print("Invalid config file")
		return
	config_data_to_test = get_configs_to_test(config_data, command_line_args.components)
	# None of the given components are in the config file
	if not config_data_to_test:
		safe_print("No valid components provided")
		return
	# At least one of the given components is not in the config file
	if command_line_args.components and len(config_data_to_test) != len(command_line_args.components):
		safe_print("Some component names are invalid")
	test_components(config_data_to_test, command_line_args.dry_run)


if __name__ == "__main__":
	main(sys.argv[1:])
