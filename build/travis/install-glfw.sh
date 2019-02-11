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

echo "Get glfw3"

version="3.2.1"

wget --no-verbose https://github.com/glfw/glfw/releases/download/${version}/glfw-${version}.zip
unzip -q glfw-${version}.zip

echo "Compile glfw3"

mkdir -p build

pushd build

cmake ../glfw-${version} -G "Unix Makefiles" -DBUILD_SHARED_LIBS=ON -DGLFW_BUILD_EXAMPLES=OFF -DGLFW_BUILD_TESTS=OFF -DGLFW_BUILD_DOCS=OFF -DGLFW_VULKAN_STATIC=OFF
cmake --build .
sudo cmake -P cmake_install.cmake
popd
