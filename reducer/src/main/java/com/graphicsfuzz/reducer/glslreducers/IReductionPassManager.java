package com.graphicsfuzz.reducer.glslreducers;

import com.graphicsfuzz.common.transformreduce.ShaderJob;
import java.util.Optional;

public interface IReductionPassManager {

  /**
   * Uses the managed passes to attempt to produce a simpler shader job from the given shader job.
   * @param shaderJob The shader job to be reduced.
   * @return Empty if the reduction passes have nothing left to try, otherwise a transformed shader
   *     job.
   */
  Optional<ShaderJob> applyReduction(ShaderJob shaderJob);

  /**
   * Notify the pass manager whether the last reduction it applied turned out to be interesting.
   * @param isInteresting True if and only if the last reduction applied by the pass manager
   *                      turned out to be interesting.
   */
  void notifyInteresting(boolean isInteresting);

}
