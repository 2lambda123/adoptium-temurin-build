##############################################################################
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
##############################################################################
OS:=$(shell uname -s)
SBOM_FILE := $(shell find $(TEST_JDK_HOME)/../ -type f -name '*-sbom_*.json')
JDK_FILE := $(shell find $(TEST_JDK_HOME)/../ -type f -name '*-jdk_*')

ifeq ($(OS),Linux)
	SBOM_FILE := $(subst $(TEST_JDK_HOME)/..,/home/jenkins/jdkbinary,$(SBOM_FILE))
	JDK_FILE := $(subst $(TEST_JDK_HOME)/..,/home/jenkins/jdkbinary,$(JDK_FILE))
endif

