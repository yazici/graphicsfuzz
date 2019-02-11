#!/usr/bin/env bash

# Copyright 2019 The GraphicsFuzz Project Authors
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -x
set -e
set -u

# This script is for Linux only
test "$GITHUB_RELEASE_TOOL_PLATFORM" = "linux_amd64"

echo "Installing Vulkan SDK"

curl -sSo "vulkan-sdk.tar.gz" "https://sdk.lunarg.com/sdk/download/latest/linux/vulkan-sdk.tar.gz"
tar xzf "vulkan-sdk.tar.gz"

relative_path=`find . -name x86_64`
absolute_path=`readlink -f $relative_path`

export VULKAN_SDK="$absolute_path"
