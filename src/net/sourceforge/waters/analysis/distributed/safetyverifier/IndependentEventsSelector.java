package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.waters.analysis.distributed.schemata.AutomatonSchema;
import net.sourceforge.waters.analysis.distributed.schemata.ProductDESSchema;

class IndependentEventsSelector implements AutomataSelector
{
  public IndependentEventsSelector(ProductDESSchema model, ProbabilityEstimator estimator)
  {
    mModel = model;
    mEstimator = estimator;
  }

  public AutomatonSchema[] select(AutomatonSchema[] automata)
  {
    AutomatonProbability[] probs = new AutomatonProbability[automata.length];
    for (int i = 0; i < automata.length; i++)
      {
	probs[i] = mEstimator.estimate(automata[i], automata);
      }

    Arrays.sort(probs, new LowestProbabilityFirst());

    //Events that have already been used. If an automaton shares any
    //events with this set, then the automaton should not be chosen.
    Set<Integer> usedEvents = new TreeSet<Integer>();

    //Take the sorted list of probabilities as a list and start adding
    //events to the used event set. If an event has been used, the 
    //automaton should be discarded from the list.
    List<AutomatonProbability> aplist = 
      new ArrayList<AutomatonProbability>(Arrays.asList(probs));
    
    Iterator<AutomatonProbability> it = aplist.iterator();
  iterating: while (it.hasNext())
      {
	AutomatonProbability ap = it.next();
	AutomatonSchema aut = ap.getAutomaton();
	
	//Are any of the automaton's events in the used 
	//set?
	for (int i = 0; i < aut.getEventIdCount(); i++)
	  {
	    if (usedEvents.contains(aut.getEventId(i)))
	      {
		//Not a good candidate.
		it.remove();
		continue iterating;
	      }
	  }

	//All good, add the events to the used set
	for (int i = 0; i < aut.getEventIdCount(); i++)
	  {
	    usedEvents.add(aut.getEventId(i));
	  }
      }

    //Return whatever is left over.
    AutomatonSchema[] result = new AutomatonSchema[aplist.size()];
    for (int i = 0; i < aplist.size(); i++)
      {
	result[i] = aplist.get(i).getAutomaton();
      }

    return result;
  }
  
  private final ProbabilityEstimator mEstimator;
  @SuppressWarnings("unused")
  private final ProductDESSchema mModel;
}