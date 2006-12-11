package net.sourceforge.waters.analysis.modular;

import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.TraceProxy;


public class MaxCommonEventsHeuristicB
  extends AbstractModularHeuristic
{
  private final HeuristicType mType;
	private final boolean foo = true;
  
  public MaxCommonEventsHeuristicB(HeuristicType type)
  {
    mType = type;
  }
  
  public Collection<AutomatonProxy> heur(ProductDESProxy composition,
                                         Set<AutomatonProxy> nonComposedPlants,
                                         Set<AutomatonProxy> nonComposedSpecPlants,
                                         Set<AutomatonProxy> nonComposedSpecs,
                                         TraceProxy counterExample,
                                         KindTranslator translator)
  {
    AutomatonProxy automaton = checkAutomata(false, nonComposedPlants,
                                             new MaxEventComparator(counterExample),
                                             counterExample, translator);
    boolean runspecs = mType == HeuristicType.PREFERREALPLANT && automaton == null;
    if (automaton == null || mType != HeuristicType.PREFERREALPLANT) {
      automaton = checkAutomata(automaton, false, nonComposedSpecPlants, 
                                new MaxEventComparator(counterExample),
                                counterExample, translator);
    }
    if (automaton == null || mType == HeuristicType.NOPREF || (runspecs && foo)) {
      automaton = checkAutomata(automaton, true, nonComposedSpecs, 
                                new MaxEventComparator(counterExample),
                                counterExample, translator);
    }
    return automaton == null ? null : Collections.singleton(automaton);
  }
  
  private static class MaxEventComparator
    implements Comparator<AutomatonProxy>
  {
    private final Set<EventProxy> mEvents;
    
    public MaxEventComparator(TraceProxy counter)
    {
      mEvents = new HashSet<EventProxy>(counter.getEvents());
    }
    
    public int compare(AutomatonProxy a1, AutomatonProxy a2)
    {
      int count1 = 0;
      int count2 = 0;
      for (EventProxy e : mEvents) {
        if (a1.getEvents().contains(e)) {
          count1++;
        }
        if (a2.getEvents().contains(e)) {
          count2++;
        }
      }
      return count1 - count2;
    }
  }
}
