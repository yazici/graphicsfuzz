/*
 * Copyright 2018 The GraphicsFuzz Project Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.graphicsfuzz.tool;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageCompare {

  private static int[] getColors(BufferedImage image) throws IOException {
    return image.getRGB(
        0,
        0,
        image.getWidth(),
        image.getHeight(),
        null,
        0,
        image.getWidth());
  }

  private static boolean arePixelsSimilar(
      int colorLeft,
      int colorRight,
      int componentThreshold) {
    for (int i = 0; i < 32; i += 8) {
      int mask = 0xff << i;
      int left = (colorLeft & mask) >>> i;
      int right = (colorRight & mask) >>> i;
      assert left >= 0 && left <= 0xff;
      assert right >= 0 && right <= 0xff;
      if (Math.abs(left - right) > componentThreshold) {
        return false;
      }
    }
    return true;
  }

  private static boolean doesSimilarNearPixelExist(
      int[] colorsLeft,
      int[] colorsRight,
      int height,
      int componentThreshold,
      int thresholdDistance,
      int middleX,
      int middleY) {

    int middlePos = middleY * height + middleX;
    for (int y = middleY - thresholdDistance; y < middleY + thresholdDistance; ++y) {
      for (int x = middleX - thresholdDistance; x < middleX + thresholdDistance; ++x) {
        int pos = y * height + x;
        if (arePixelsSimilar(
            colorsLeft[middlePos],
            colorsRight[pos],
            componentThreshold)) {
          return true;
        }
      }
    }
    return false;
  }

  public static int compareImageColors(
      int[] colorsLeft,
      int[] colorsRight,
      int width,
      int height,
      int componentThreshold,
      int thresholdDistance) {

    assert colorsLeft.length == colorsRight.length;

    int badPixelCount = 0;

    // Skip pixels around the edge (thresholdDistance).

    for (int y = thresholdDistance; y < height - thresholdDistance; ++y) {
      for (int x = thresholdDistance; x < width - thresholdDistance; ++x) {

        if (!doesSimilarNearPixelExist(
            colorsLeft,
            colorsRight,
            height,
            componentThreshold,
            thresholdDistance,
            x,
            y
        )) {
          ++badPixelCount;
        }
      }
    }

    return badPixelCount;
  }

  public static int compareImages(
      File left,
      File right,
      int componentThreshold,
      int thresholdDistance) throws IOException {

    BufferedImage leftImage = ImageIO.read(left);
    BufferedImage rightImage = ImageIO.read(right);
    if (leftImage.getWidth() != rightImage.getWidth()
        || leftImage.getHeight() != rightImage.getHeight()) {
      throw new IllegalArgumentException("Images have different sizes! \n" + left + "\n" + right);
    }
    int[] colorsLeft = getColors(leftImage);
    int[] colorsRight = getColors(rightImage);

    // Compare in both directions.

    int res1 = compareImageColors(
        colorsLeft,
        colorsRight,
        leftImage.getWidth(),
        leftImage.getHeight(),
        componentThreshold,
        thresholdDistance);
    int res2 = compareImageColors(
        colorsRight,
        colorsLeft,
        leftImage.getWidth(),
        leftImage.getHeight(),
        componentThreshold,
        thresholdDistance);

    return Math.max(res1, res2);
  }

  public static void main(String[] args) throws IOException {

    int numBadPixels = compareImages(
        new File(args[0]),
        new File(args[1]),
        60,
        4
    );

    System.out.println(numBadPixels);
  }
}
