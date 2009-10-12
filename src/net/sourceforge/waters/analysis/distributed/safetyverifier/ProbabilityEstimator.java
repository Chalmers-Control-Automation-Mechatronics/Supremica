package net.sourceforge.waters.analysis.distributed.safetyverifier;

import net.sourceforge.waters.analysis.distributed.schemata.AutomatonSchema;

/**
 * Estimates the probability of an automaton.
 */
interface ProbabilityEstimator
{
  /**
   * Estimate the probability for an automaton, considering the 
   * supplied array of automata (whatever that means).
   */
  public AutomatonProbability estimate(AutomatonSchema aut, AutomatonSchema[] automata);
}