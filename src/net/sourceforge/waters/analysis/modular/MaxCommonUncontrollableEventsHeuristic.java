package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.model.des.EventProxy;
import java.util.Comparator;
import net.sourceforge.waters.model.des.TraceProxy;
import java.util.Set;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.model.analysis.KindTranslator;
import java.util.Collections;
import net.sourceforge.waters.model.des.AutomatonProxy;
import java.util.Collection;

public class MaxCommonUncontrollableEventsHeuristic
  extends AbstractModularHeuristic
{
  private final ModularHeuristicFactory.Preference mType;
	private final boolean foo = true;
  
  public MaxCommonUncontrollableEventsHeuristic()
  {
    this(ModularHeuristicFactory.Preference.PREFER_REAL_PLANT);
  }
  
  public MaxCommonUncontrollableEventsHeuristic(ModularHeuristicFactory.Preference type)
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
                                             new MaxEventComparator(composition, translator),
                                             counterExample, translator);
    boolean runspecs = mType == ModularHeuristicFactory.Preference.PREFER_REAL_PLANT && automaton == null;
    if (automaton == null || mType != ModularHeuristicFactory.Preference.PREFER_REAL_PLANT) {
      automaton = checkAutomata(automaton, false, nonComposedSpecPlants, 
                                new MaxEventComparator(composition, translator),
                                counterExample, translator);
    }
    if (automaton == null || mType == ModularHeuristicFactory.Preference.NOPREF || (runspecs && foo)) {
      automaton = checkAutomata(automaton, true, nonComposedSpecs, 
                                new MaxEventComparator(composition, translator),
                                counterExample, translator);
    }
    return automaton == null ? null : Collections.singleton(automaton);
  }
  
  private static class MaxEventComparator
    implements Comparator<AutomatonProxy>
  {
    private final Set<EventProxy> mEvents;
    private final KindTranslator mTranslator;
    
    public MaxEventComparator(ProductDESProxy composition,
                              KindTranslator translator)
    {
      mEvents = composition.getEvents();
      mTranslator = translator;
    }
    
    public int compare(AutomatonProxy a1, AutomatonProxy a2)
    {
      int count1 = 0;
      int count2 = 0;
      for (EventProxy e : a1.getEvents()) {
        if (mTranslator.getEventKind(e).equals(EventKind.UNCONTROLLABLE)) {
          if (mEvents.contains(e)) {
            count1++;
          }
        }
      }
      for (EventProxy e : a2.getEvents()) {
        if (mTranslator.getEventKind(e).equals(EventKind.UNCONTROLLABLE)) {
          if (mEvents.contains(e)) {
            count2++;
          }
        }
      }
      return count1 - count2;
    }
  }
}
