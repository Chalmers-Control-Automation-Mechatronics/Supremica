package net.sourceforge.waters.analysis.distributed.safetyverifier;

import net.sourceforge.waters.analysis.distributed.schemata.*;

/**
 * Estimates probability of an automaton changing based
 * on event probabilities.
 */
class ChangeProbabilityEstimator implements ProbabilityEstimator
{
  public ChangeProbabilityEstimator(ProductDESSchema model)
  {
    mModel = model;
  }

  private double eventProb(int event, AutomatonSchema[] automata)
  {
    double sum = 0;

    for (AutomatonSchema a : automata)
      {
	if (!a.hasEvent(event))
	  continue;

	sum += countWithEnabled(event, a) / 
	  ((double)a.getStateCount());
      }

    return sum;
  }

  private double change(AutomatonSchema a, AutomatonSchema[] automata)
  {
    double val = 0;

    for (int i = 0; i < mModel.getEventCount(); i++)
      {
	val += eventProb(i, automata) * (nonSelfloopEventTrans(i, a) /
					 ((double)a.getStateCount()));
      }

    return val;
  }

  private int nonSelfloopEventTrans(int event, AutomatonSchema aut)
  {
    int count = 0;
    for (int i = 0; i < aut.getTransitionCount(); i++)
      {
	TransitionSchema t = aut.getTransition(i);

	if (t.getEventId() == event && t.getSource() != t.getTarget())
	  count++;
      }
    
    return count;
  }

  private int countWithEnabled(int event, AutomatonSchema aut)
  {
    boolean[] enabledInState = new boolean[aut.getStateCount()];
    int enabledCount = 0;

    for (int i = 0; i < aut.getTransitionCount(); i++)
      {
	TransitionSchema t = aut.getTransition(i);
	
	if (t.getEventId() == event && !enabledInState[t.getSource()])
	  {
	    enabledCount++;
	    enabledInState[t.getSource()] = true;
	  }
      }

    return enabledCount;
  }

  public AutomatonProbability estimate(AutomatonSchema aut, AutomatonSchema[] automata)
  { 
    return new  AutomatonProbability(aut, change(aut, automata));
  }

  private final ProductDESSchema mModel;
}