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

package com.graphicsfuzz.reducer.glslreducers;

import com.graphicsfuzz.common.transformreduce.ShaderJob;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystematicReductionPassManager implements IReductionPassManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      SystematicReductionPassManager.class);

  // The passes to be iterated over.
  private final List<IReductionPass> passes;

  // Determines whether, on completing one round of reduction passes, it is
  // worthwhile trying a further round.
  private boolean anotherRoundWorthwhile;

  // The index of the pass currently being applied.
  private int passIndex;

  public SystematicReductionPassManager(List<IReductionPass> passes) {
    this.passes = new ArrayList<>();
    this.passes.addAll(passes);
    this.anotherRoundWorthwhile = false;
    this.passIndex = 0;
  }

  @Override
  public Optional<ShaderJob> applyReduction(ShaderJob shaderJob) {
    while (true) {
      Optional<ShaderJob> maybeResult =
          getCurrentPass().tryApplyReduction(shaderJob);
      if (maybeResult.isPresent()) {
        LOGGER.info("Pass " + getCurrentPass().getName() + " made a reduction step.");
        return maybeResult;
      }
      // This pass did not have any impact, so move on to the next pass.
      LOGGER.info("Pass " + getCurrentPass().getName() + " did not make a reduction step.");
      passIndex++;
      if (passIndex < passes.size()) {
        anotherRoundWorthwhile |= !getCurrentPass().reachedMinimumGranularity();
      } else if (anotherRoundWorthwhile) {
        startNewRound();
      } else {
        return Optional.empty();
      }
    }
  }

  public void startNewRound() {
    passIndex = 0;
    anotherRoundWorthwhile = false;
  }

  @Override
  public void notifyInteresting(boolean isInteresting) {
    if (isInteresting) {
      anotherRoundWorthwhile = true;
    }
  }

  private IReductionPass getCurrentPass() {
    return passes.get(passIndex);
  }

}
