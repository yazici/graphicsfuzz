package com.graphicsfuzz.reducer.glslreducers;

import com.graphicsfuzz.reducer.reductionopportunities.IReductionOpportunity;
import com.graphicsfuzz.reducer.reductionopportunities.IReductionOpportunityFinder;
import com.graphicsfuzz.reducer.reductionopportunities.ReducerContext;

public abstract class AbstractReductionPass implements IReductionPass {

  private final ReducerContext reducerContext;
  private final IReductionOpportunityFinder<? extends IReductionOpportunity> finder;

  AbstractReductionPass(ReducerContext reducerContext,
                        IReductionOpportunityFinder<? extends IReductionOpportunity> finder) {
    this.reducerContext = reducerContext;
    this.finder = finder;
  }

  @Override
  public final String getName() {
    return finder.getName();
  }

  protected final IReductionOpportunityFinder<? extends IReductionOpportunity> getFinder() {
    return finder;
  }

  protected final ReducerContext getReducerContext() {
    return reducerContext;
  }

}
