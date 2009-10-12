package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.util.Comparator;

import net.sourceforge.waters.analysis.distributed.schemata.AutomatonSchema;
import net.sourceforge.waters.analysis.distributed.schemata.ProductDESSchema;

/**
 * A tuple containing an automaton and its estimated probability of
 * changing. Actually, it is currently a misnomer. It doesn't
 * calculate a probability. The values across a number of automata
 * could be normalised into a probability distribution.
 * @author Sam Douglas
 */
class AutomatonProbability
{
  public AutomatonProbability(AutomatonSchema automaton, double prob)
  {
    mAutomaton = automaton;
    mProbability = prob;
  }

  /**
   * Gets the automaton associated with this probability
   * estimate.
   */ 
  public AutomatonSchema getAutomaton()
  {
    return mAutomaton;
  }

  /**
   * Gets an estimate of the change probability.
   */
  public double getProbability()
  {
    return mProbability;
  }    

  public String toString()
  {
    return "(" + mAutomaton.getName() + ": " + mProbability + ")";
  }

  private final AutomatonSchema mAutomaton;
  private final double mProbability;
}


/**
 * Gives an ordering of AutomatonProbability tuples with
 * lowest probability first.
 */
class LowestProbabilityFirst 
  implements Comparator<AutomatonProbability>
{
  public int compare(AutomatonProbability a1, AutomatonProbability a2)
  {
    Double m1prob = new Double(a1.getProbability());
    Double m2prob = new Double(a2.getProbability());
    return m1prob.compareTo(m2prob);
  }
}


/**
 * A kludgy comparator that sorts by <code>probability^2 / |Qn|</code>
 */
class ProbabilityOverStatesSquared 
  implements Comparator<AutomatonProbability>
{ 
  public int compare(AutomatonProbability m1, AutomatonProbability m2)
  {
    Double m1prob = new Double(m1.getProbability() * Math.pow(m1.getAutomaton().getStateCount(), 2));
    Double m2prob = new Double(m2.getProbability() * Math.pow(m2.getAutomaton().getStateCount(), 2));
    return m1prob.compareTo(m2prob);
  }
}