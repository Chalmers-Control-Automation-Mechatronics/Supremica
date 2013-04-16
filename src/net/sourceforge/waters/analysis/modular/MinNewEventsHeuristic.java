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


public class MinNewEventsHeuristic
  extends AbstractModularHeuristic
{
  private final ModularHeuristicFactory.Preference mType;

  public MinNewEventsHeuristic(final KindTranslator translator)
  {
    this(translator, ModularHeuristicFactory.Preference.PREFER_REAL_PLANT);
  }

  public MinNewEventsHeuristic(final KindTranslator translator,
                               final ModularHeuristicFactory.Preference type)
  {
    super(translator);
    mType = type;
  }

  public Collection<AutomatonProxy> heur(final ProductDESProxy composition,
                                         final Set<AutomatonProxy> nonComposedPlants,
                                         final Set<AutomatonProxy> nonComposedSpecPlants,
                                         final Set<AutomatonProxy> nonComposedSpecs,
                                         final TraceProxy counterExample)
  {
    AutomatonProxy automaton = checkAutomata(false, nonComposedPlants,
                                             new MinNewEventComparator(composition),
                                             counterExample);
    final boolean runspecs = mType == ModularHeuristicFactory.Preference.PREFER_REAL_PLANT && automaton == null;
    if (automaton == null || mType != ModularHeuristicFactory.Preference.PREFER_REAL_PLANT) {
      automaton = checkAutomata(automaton, false, nonComposedSpecPlants,
                                new MinNewEventComparator(composition),
                                counterExample);
    }
    if (automaton == null || mType == ModularHeuristicFactory.Preference.NOPREF || runspecs) {
      automaton = checkAutomata(automaton, true, nonComposedSpecs,
                                new MinNewEventComparator(composition),
                                counterExample);
    }
    return automaton == null ? null : Collections.singleton(automaton);
  }

  private static class MinNewEventComparator
    implements Comparator<AutomatonProxy>
  {
    private final Set<EventProxy> mEvents;

    public MinNewEventComparator(final ProductDESProxy composition)
    {
      mEvents = composition.getEvents();
    }

    public int compare(final AutomatonProxy a1, final AutomatonProxy a2)
    {
      int count1 = 0;
      int count2 = 0;
      for (final EventProxy e : a1.getEvents()) {
        if (!mEvents.contains(e)) {
          count1++;
        }
      }
      for (final EventProxy e : a2.getEvents()) {
        if (!mEvents.contains(e)) {
          count2++;
        }
      }
      return count2 - count1;
    }
  }
}
