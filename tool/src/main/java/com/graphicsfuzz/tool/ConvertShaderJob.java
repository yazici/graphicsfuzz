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

import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.glslversion.ShadingLanguageVersion;
import com.graphicsfuzz.common.transformreduce.Constants;
import com.graphicsfuzz.common.transformreduce.GlslShaderJob;
import com.graphicsfuzz.common.transformreduce.ShaderJob;
import com.graphicsfuzz.common.util.FileHelper;
import com.graphicsfuzz.common.util.Helper;
import com.graphicsfuzz.common.util.IRandom;
import com.graphicsfuzz.common.util.IdGenerator;
import com.graphicsfuzz.common.util.ParseTimeoutException;
import com.graphicsfuzz.common.util.RandomWrapper;
import com.graphicsfuzz.common.util.ReductionProgressHelper;
import com.graphicsfuzz.common.util.UniformsInfo;
import com.graphicsfuzz.reducer.IFileJudge;
import com.graphicsfuzz.reducer.ReductionDriver;
import com.graphicsfuzz.reducer.ReductionKind;
import com.graphicsfuzz.reducer.filejudge.FuzzingFileJudge;
import com.graphicsfuzz.reducer.filejudge.ImageGenErrorShaderFileJudge;
import com.graphicsfuzz.reducer.filejudge.ImageShaderFileJudge;
import com.graphicsfuzz.reducer.filejudge.ValidatorErrorShaderFileJudge;
import com.graphicsfuzz.reducer.reductionopportunities.ReductionOpportunityContext;
import com.graphicsfuzz.server.thrift.FuzzerServiceManager;
import com.graphicsfuzz.server.thrift.ImageComparisonMetric;
import com.graphicsfuzz.shadersets.ExactImageFileComparator;
import com.graphicsfuzz.shadersets.IShaderDispatcher;
import com.graphicsfuzz.shadersets.LocalShaderDispatcher;
import com.graphicsfuzz.shadersets.MetricImageFileComparator;
import com.graphicsfuzz.shadersets.RemoteShaderDispatcher;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConvertShaderJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConvertShaderJob.class);

  public static void convertShaderJobToGL100(
      File sourceDir,
      String sourcePrefix,
      File destDir,
      String destPrefix,
      boolean stripHeader) throws IOException, ParseTimeoutException {

    ShaderJob sourceJob = Helper.parseShaderJob(sourceDir, sourcePrefix, stripHeader);
    sourceJob.removeUniformBindings();
    Helper.emitShaderJob(
        sourceJob,
        ShadingLanguageVersion.ESSL_100,
        destPrefix,
        destDir,
        null);

  }

  private static ArgumentParser getParser() {

    ArgumentParser parser = ArgumentParsers.newArgumentParser("convert_shader")
        .defaultHelp(true)
        .description("Convert a GLSL-Vulkan shader to GLSL 100.");

    // Required arguments
    parser.addArgument("shader_job")
        .help("Path of shader job to be converted.  If path is /path/to/p, shaders and meta data "
            + "to be converted will be /path/to/p.frag, /path/to/p.vert, /path/to/p.json, etc., ")
        .type(String.class);

    parser.addArgument("output_shader_job")
        .help("Path of shader job to be output.  If path is /path/to/p, shaders and meta data "
            + "output will be /path/to/p.frag, /path/to/p.vert, /path/to/p.json, etc., ")
        .type(String.class);

    parser.addArgument("headers")
        .help("Whether the shader_job shaders have headers. I.e. // END OF GENERATED HEADER")
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

    File shaderJobPrefix = new File((String) ns.get("shader_job")).getAbsoluteFile();
    File shaderJobOutputPrefix = new File((String) ns.get("output_shader_job")).getAbsoluteFile();
    boolean stripHeaders = ns.get("headers");

    if (shaderJobPrefix.isFile() || !shaderJobPrefix.getParentFile().isDirectory()) {
      throw new ArgumentParserException(
          "shader_job should be a file prefix, not including the extension",
          parser);
    }

    if (shaderJobOutputPrefix.isFile()) {
      throw new ArgumentParserException(
          "output_shader_job should be a file prefix, not including the extension",
          parser);
    }

    File inputDir = shaderJobPrefix.getParentFile();
    String inputPrefix = shaderJobPrefix.getName();

    File outputDir = shaderJobOutputPrefix.getParentFile();
    String outputPrefix = shaderJobOutputPrefix.getName();

    LOGGER.info("Creating output directory " + outputDir);
    FileUtils.forceMkdir(outputDir);

    convertShaderJobToGL100(inputDir, inputPrefix, outputDir, outputPrefix, stripHeaders);

  }


}
