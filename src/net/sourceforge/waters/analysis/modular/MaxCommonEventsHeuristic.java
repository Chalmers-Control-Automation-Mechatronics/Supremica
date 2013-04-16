package net.sourceforge.waters.analysis.modular;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import net.sourceforge.waters.model.analysis.des.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.TraceProxy;


public class MaxCommonEventsHeuristic
  extends AbstractModularHeuristic
{
  private final ModularHeuristicFactory.Preference mType;
	private final boolean foo = true;

  public MaxCommonEventsHeuristic(final KindTranslator translator)
  {
    this(translator, ModularHeuristicFactory.Preference.PREFER_REAL_PLANT);
  }

  public MaxCommonEventsHeuristic(final KindTranslator translator,
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
                                             new MaxEventComparator(composition),
                                             counterExample);
    final boolean runspecs = mType == ModularHeuristicFactory.Preference.PREFER_REAL_PLANT && automaton == null;
    if (automaton == null || mType != ModularHeuristicFactory.Preference.PREFER_REAL_PLANT) {
      automaton = checkAutomata(automaton, false, nonComposedSpecPlants,
                                new MaxEventComparator(composition),
                                counterExample);
    }
    if (automaton == null || mType == ModularHeuristicFactory.Preference.NOPREF || (runspecs && foo)) {
      automaton = checkAutomata(automaton, true, nonComposedSpecs,
                                new MaxEventComparator(composition),
                                counterExample);
    }
    return automaton == null ? null : Collections.singleton(automaton);
  }

  private static class MaxEventComparator
    implements Comparator<AutomatonProxy>
  {
    private final Set<EventProxy> mEvents;

    public MaxEventComparator(final ProductDESProxy composition)
    {
      mEvents = composition.getEvents();
    }

    public int compare(final AutomatonProxy a1, final AutomatonProxy a2)
    {
      int count1 = 0;
      int count2 = 0;
      for (final EventProxy e : a1.getEvents()) {
        if (mEvents.contains(e)) {
          count1++;
        }
      }
      for (final EventProxy e : a2.getEvents()) {
        if (mEvents.contains(e)) {
          count2++;
        }
      }
      return count1 - count2;
    }
  }
}
