package net.sourceforge.waters.analysis.modular;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.TraceProxy;


public class MinEventsHeuristic
  extends AbstractModularHeuristic
{
  private final HeuristicType mType;
	private final boolean foo = false;
  
  public MinEventsHeuristic(HeuristicType type)
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
                                             new MinEventsComparator(),
                                             counterExample, translator);
    boolean runspecs = mType == HeuristicType.PREFERREALPLANT && automaton == null;
    if (automaton == null || mType != HeuristicType.PREFERREALPLANT) {
      automaton = checkAutomata(automaton, false, nonComposedSpecPlants, 
                                new MinEventsComparator(),
                                counterExample, translator);
    }
    if (automaton == null || mType == HeuristicType.NOPREF || (runspecs && foo)) {
      automaton = checkAutomata(automaton, true, nonComposedSpecs, 
                                new MinEventsComparator(),
                                counterExample, translator);
    }
    return automaton == null ? null : Collections.singleton(automaton);
  }
  
  private static class MinEventsComparator
    implements Comparator<AutomatonProxy>
  {
    public int compare(AutomatonProxy a1, AutomatonProxy a2)
    {
      return a2.getEvents().size() - a1.getEvents().size();
    }
  }
}
