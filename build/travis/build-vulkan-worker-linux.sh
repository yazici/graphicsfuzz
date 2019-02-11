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

test -d temp
test -d build/travis

mkdir -p out/

mkdir -p vulkan-worker/build

pushd vulkan-worker/build
rm -rf ./*
cmake ../ -G "Unix Makefiles" -DCMAKE_BUILD_TYPE=Debug -DCMAKE_PREFIX_PATH="${HOME}/local/glfw"
cmake --build . --config Debug -- -j$(nproc)
popd

cp vulkan-worker/build/vkworker out/vkworker

# Check headers.
build/travis/python-launch build/travis/check_headers.py
