package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import net.sourceforge.waters.analysis.distributed.schemata.*;

public class PrototypeStateDistribution extends HashStateDistribution
{
  public PrototypeStateDistribution(String[] handlers, ProductDESSchema model, StateEncoding encoding, int desiredHashBits)
  {
    super(handlers);
    MachineProbability[] probs = getMachineChangeProbabilities(model);
    Arrays.sort(probs, new ProbabilityOverStatesSquared(model));
    
    double sum = 0;

    //Override the desired hash bits parameter to calculate
    //from the number of handlers
    desiredHashBits = clog2(2 * handlers.length);

    for (int i = 0; i < probs.length; i++)
      {
	int a = probs[i].automaton;
	System.out.format("%s (%d states):\t%s\n", 
			  model.getAutomaton(a).getName(), 
			  model.getAutomaton(a).getStateCount(),
			  probs[i].probability);

	sum += probs[i].probability;
      }

    System.out.format("Probability sum: %f\n", sum);

    //Select a subset of the states to use to build the hash
    int bitcount = 0;
    List<MachineProbability> hashProbs = new ArrayList<MachineProbability>();
    for (int i = 0; i < probs.length && bitcount < desiredHashBits; i++)
      {
	if (probs[i].probability <= 0.0)
	  {
	    continue;
	  }
	else
	  {
	    bitcount += clog2(model.getAutomaton(probs[i].automaton).getStateCount());
	    hashProbs.add(probs[i]);
	  }
      }

    //Now prepare the data necessary to extract the information
    //from an unpacked state tuple and build a hash code. This
    //needs the automaton index and the amount to shift (i.e. bits
    //needed for the automaton).
    mHashAutomata = new int[hashProbs.size()];
    mAutomataShift = new int[hashProbs.size()];
    int cshift = 0;
    for (int i = 0; i < hashProbs.size(); i++)
      {
	MachineProbability mp = hashProbs.get(i);
	mHashAutomata[i] = mp.automaton;
	mAutomataShift[i] = cshift;
	cshift += clog2(model.getAutomaton(mp.automaton).getStateCount());
      }

    mEncoding = encoding;
  }

  /**
   * Counts how many times an event occurs in a machine
   */
  private int machineEventCount(ProductDESSchema model, int eventid, int aut)
  {
    AutomatonSchema as = model.getAutomaton(aut);
    int evx = 0;
    
    //Count the number of transitions with the specified event
    for (int i = 0; i < as.getTransitionCount(); i++)
      {
	TransitionSchema t = as.getTransition(i);
	if (t.getEventId() == eventid)
	  {
	    evx++;
	  }
      }
    
    return evx;
  }
  
  /**
   * Gets the number of meaningful transitions in a machine
   */
  private int machineTransitionCount(ProductDESSchema model, int aut)
  {
    return model.getAutomaton(aut).getTransitionCount();
  }

  /**
   * Gets the `probability' of an event occurring in a machine. 
   */
  private double machineEventProb(ProductDESSchema model, int eventid, int aut)
  {
    int transcount = machineTransitionCount(model, aut);
    if (transcount > 0)
      return machineEventCount(model, eventid, aut) / (double)
	machineTransitionCount(model, aut);
    else
      return 0.0;
  }


  /**
   * Gets the approximate probability of an event being taken over all
   * machines.
   *
   * If the transition is never taken, the value will be zero.
   */
  private double eventProbability(ProductDESSchema model, int event)
  {
    /* Value initially set negative, will be assigned to first value */
    double evProb = -1.0;
    for (int aut = 0; aut < model.getAutomataCount(); aut++)
      {
	AutomatonSchema as = model.getAutomaton(aut);
	for (int ev = 0; ev < as.getEventIdCount(); ev++)
	  {
	    int eve = as.getEventId(ev);
	    if (eve == event)
	      {
		double mevProb = machineEventProb(model, event, aut);
		if (evProb < 0.0)
		  evProb = mevProb;
		else
		  evProb *= mevProb;
	      }
	  }
      }

    return (evProb < 0.0) ? 0.0 : evProb;
  }
  
  private double machineChangeProb(ProductDESSchema model, int aut)
  {
    double p = -1.0;
    AutomatonSchema as = model.getAutomaton(aut);
        
    for (int e = 0; e < as.getEventIdCount(); e++)
      {
	int event = as.getEventId(e);
	double ep = eventProbability(model, event);

	if (p < 0.0)
	  p = ep;
	else
	  p += ep;
      }

    if (p < 0.0)
      return 0;
    else
      return p / (double) as.getStateCount();
  }

  private MachineProbability[] getMachineChangeProbabilities(ProductDESSchema model)
  {
    MachineProbability[] probs = new MachineProbability[model.getAutomataCount()];
    
    for (int i = 0; i < model.getAutomataCount(); i++)
      {
	probs[i] = new MachineProbability(i, machineChangeProb(model, i));
      }

    makeProbabilityDistribution(probs);
    return probs;
  }

  private void makeProbabilityDistribution(MachineProbability[] probs)
  {
    double sum = 0;

    for (int i = 0; i < probs.length; i++)
      {
	sum += probs[i].probability;
      }

    for (int i = 0; i < probs.length; i++)
      {
	probs[i].probability = probs[i].probability / sum;
      }
  }


  /**
   * A simple, `plain old data' tuple for machine probabilities.
   * This is intended to make sorting easier.
   */
  private static class MachineProbability
  {
    public MachineProbability(int aut, double prob)
    {
      this.automaton = aut;
      this.probability = prob;
    }

    public String toString()
    {
      return "(" + automaton + ": " + probability + ")";
    }

    public int automaton;
    public double probability;
  }

  @SuppressWarnings("unused")
  private static class LowestProbabilityFirst 
    implements Comparator<MachineProbability>
  {
    public int compare(MachineProbability m1, MachineProbability m2)
    {
      Double m1prob = new Double(m1.probability);
      Double m2prob = new Double(m2.probability);
      return m1prob.compareTo(m2prob);
    }
  }

  private static class ProbabilityOverStatesSquared 
    implements Comparator<MachineProbability>
  {
    private final ProductDESSchema mModel;

    public ProbabilityOverStatesSquared(ProductDESSchema model)
      {
	mModel = model;
      }

    public int compare(MachineProbability m1, MachineProbability m2)
    {
      Double m1prob = new Double(m1.probability * Math.pow(mModel.getAutomaton(m1.automaton).getStateCount(), 2));
      Double m2prob = new Double(m2.probability * Math.pow(mModel.getAutomaton(m2.automaton).getStateCount(), 2));
      return m1prob.compareTo(m2prob);
    }
  }

  public StateHandler lookupStateHandler(StateTuple state)
  {
    int hash = 0;
    for (int i = 0; i < mHashAutomata.length; i++)
      {
	int aut = mHashAutomata[i];
	int shift = mAutomataShift[i];
	hash |= mEncoding.decodeAutomatonState(state, aut) << shift;
      }

    //hash = rehash(hash);
    //return getStateHandler(Math.abs(hash % getStateHandlerCount()));

    return hashLookupStateHandler(hash);
  }

  private static int clog2(int x)
  {
    x--;
    int y = 0;
    while (x > 0) 
      {
	x >>= 1;
	y++;
      }
    return y;
  }

  private final int[] mHashAutomata;
  private final int[] mAutomataShift;
  private final StateEncoding mEncoding;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}