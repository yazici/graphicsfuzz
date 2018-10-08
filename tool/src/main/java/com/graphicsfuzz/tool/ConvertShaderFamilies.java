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

import com.graphicsfuzz.common.glslversion.ShadingLanguageVersion;
import com.graphicsfuzz.common.transformreduce.ShaderJob;
import com.graphicsfuzz.common.util.Helper;
import com.graphicsfuzz.common.util.LocalShaderSet;
import com.graphicsfuzz.common.util.ParseTimeoutException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConvertShaderFamilies {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConvertShaderFamilies.class);

  public static void convertShaderFamiliesToGLVulkan(
      File shaderFamiliesDir,
      File outputDir,
      String prefix,
      boolean stripHeaders
  ) throws IOException, ParseTimeoutException {

    if (prefix == null) {
      throw new NullPointerException("prefix is null");
    }

    File[] shaderFamilies = shaderFamiliesDir.listFiles(File::isDirectory);
    if (shaderFamilies == null) {
      throw new IllegalArgumentException("No directories found in " + shaderFamiliesDir);
    }

    FileUtils.forceMkdir(outputDir);

    for (File shaderFamilyFile : shaderFamilies) {

      LocalShaderSet shaderFamily = new LocalShaderSet(shaderFamilyFile);

      LOGGER.info("Processing family {}.", shaderFamilyFile);

      try {
        // Collect all shader files.
        List<File> shaderFiles = new ArrayList<>();
        shaderFiles.add(shaderFamily.getReference().getAbsoluteFile());
        for (File f : shaderFamily.getVariants()) {
          shaderFiles.add(f.getAbsoluteFile());
        }

        // Convert each file.
        for (File shaderFile : shaderFiles) {
          final File sourceDir = shaderFamilyFile;
          String sourcePrefix = FilenameUtils.removeExtension(shaderFile.getName());
          File destDir =
              Paths.get(outputDir.toString(), prefix + shaderFamily.getName()).toFile();

          FileUtils.forceMkdir(destDir);

          ShaderJob sourceJob = Helper.parseShaderJob(sourceDir, sourcePrefix, stripHeaders);
          sourceJob.makeUniformBindings();
          Helper.emitShaderJob(
              sourceJob,
              ShadingLanguageVersion.ESSL_310,
              sourcePrefix,
              destDir,
              null);
        }
      } catch (Exception exception) {
        LOGGER.error("", exception);
        // Fallthrough.
      }
    }
  }

  private static ArgumentParser getParser() {

    ArgumentParser parser = ArgumentParsers.newArgumentParser("convert_shader_families")
        .defaultHelp(true)
        .description("Convert a directory of shader families to GLSL 310 Vulkan.");

    // Required arguments
    parser.addArgument("shader_families")
        .help("Directory to be converted")
        .type(File.class);

    parser.addArgument("output")
        .help("Output directory")
        .type(File.class);

    parser.addArgument("prefix")
        .help("String prefix to add to the name of each output shader family. E.g. Vulkan_")
        .type(String.class);

    parser.addArgument("headers")
        .help("Whether the shaders have headers. I.e. // END OF GENERATED HEADER")
        .type(Boolean.class);

    return parser;

  }

  public static void main(String[] args) {
    try {
      mainHelper(args);
    } catch (ArgumentParserException exception) {
      exception.getParser().handleError(exception);
      System.exit(1);
    } catch (Throwable ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  public static void mainHelper(String[] args) throws ArgumentParserException, IOException,
      ParseTimeoutException {

    ArgumentParser parser = getParser();

    Namespace ns = parser.parseArgs(args);

    File shaderFamilies = ns.get("shader_families");
    File outputDir = ns.get("output");
    String prefix = ns.get("prefix");
    boolean headers = ns.get("headers");

    convertShaderFamiliesToGLVulkan(
        shaderFamilies,
        outputDir,
        prefix,
        headers
    );

  }


}
