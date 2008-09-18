package net.sourceforge.waters.analysis.modular;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.TraceProxy;


public class RelMaxCommonEventsHeuristic
  extends AbstractModularHeuristic
{
  private final ModularHeuristicFactory.Preference mType;
  
  public RelMaxCommonEventsHeuristic()
  {
    this(ModularHeuristicFactory.Preference.PREFER_REAL_PLANT);
  }
  
  public RelMaxCommonEventsHeuristic(ModularHeuristicFactory.Preference type)
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
                                             new RelMaxEventComparator(composition),
                                             counterExample, translator);
    boolean runspecs = mType == ModularHeuristicFactory.Preference.PREFER_REAL_PLANT && automaton == null;
    if (automaton == null || mType != ModularHeuristicFactory.Preference.PREFER_REAL_PLANT) {
      automaton = checkAutomata(automaton, false, nonComposedSpecPlants, 
                                new RelMaxEventComparator(composition),
                                counterExample, translator);
    }
    if (automaton == null || mType == ModularHeuristicFactory.Preference.NOPREF || runspecs) {
      automaton = checkAutomata(automaton, true, nonComposedSpecs, 
                                new RelMaxEventComparator(composition),
                                counterExample, translator);
    }
    return automaton == null ? null : Collections.singleton(automaton);
  }
  
  private static class RelMaxEventComparator
    implements Comparator<AutomatonProxy>
  {
    private final Set<EventProxy> mEvents;
    
    public RelMaxEventComparator(ProductDESProxy composition)
    {
      mEvents = composition.getEvents();
    }
    
    public int compare(AutomatonProxy a1, AutomatonProxy a2)
    {
      int count1 = 0;
      int count2 = 0;
      for (EventProxy e : a1.getEvents()) {
        if (mEvents.contains(e)) {
          count1++;
        }
      }
      for (EventProxy e : a2.getEvents()) {
        if (mEvents.contains(e)) {
          count2++;
        }
      }
      double c1 = (double)count1 / (double)a1.getEvents().size();
      double c2 = (double)count2 / (double)a2.getEvents().size();
      if (c1 < c2) {
        return -1;
      } else if (c1 > c2) {
        return 1;
      } else {
        return 0;
      }
    }
  }
}
