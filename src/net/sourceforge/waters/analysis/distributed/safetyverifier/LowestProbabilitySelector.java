package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.waters.analysis.distributed.schemata.AutomatonSchema;
import net.sourceforge.waters.analysis.distributed.schemata.ProductDESSchema;


/**
 * Simple selector, pick enough automata to satisfy the number 
 * of bits.
 */
class LowestProbabilitySelector implements AutomataSelector
{
  public LowestProbabilitySelector(ProductDESSchema model, ProbabilityEstimator estimator, int desiredBits)
  {
    mModel = model;
    mEstimator = estimator;
    mDesiredBits = desiredBits;
  }

  public AutomatonSchema[] select(AutomatonSchema[] automata)
  {
    AutomatonProbability[] probs = new AutomatonProbability[automata.length];

    for (int i = 0; i < automata.length; i++)
      {
	probs[i] = mEstimator.estimate(automata[i], automata);
      }

    Arrays.sort(probs, new LowestProbabilityFirst());
    
    System.err.println(Arrays.toString(probs));

    List<AutomatonSchema> result = new ArrayList<AutomatonSchema>();
    int bitcount = 0;
    for (int i = 0; i < probs.length && bitcount < mDesiredBits; i++)
      {
	if (probs[i].getProbability() <= 0.0)
	  {
	    continue;
	  }
	else
	  {
	    bitcount += Util.clog2(probs[i].getAutomaton().getStateCount());
	    result.add(probs[i].getAutomaton());
	  }
      }

    return result.toArray(new AutomatonSchema[0]);
  }

  private final ProbabilityEstimator mEstimator;
  private final ProductDESSchema mModel;
  private final int mDesiredBits;
}