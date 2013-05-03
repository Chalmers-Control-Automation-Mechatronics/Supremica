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

  public MaxCommonUncontrollableEventsHeuristic
    (final KindTranslator translator)
  {
    this(translator, ModularHeuristicFactory.Preference.PREFER_REAL_PLANT);
  }

  public MaxCommonUncontrollableEventsHeuristic
    (final KindTranslator translator,
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
    final KindTranslator translator = getKindTranslator();
    AutomatonProxy automaton = checkAutomata(false, nonComposedPlants,
                                             new MaxEventComparator(composition, translator),
                                             counterExample);
    final boolean runspecs = mType == ModularHeuristicFactory.Preference.PREFER_REAL_PLANT && automaton == null;
    if (automaton == null || mType != ModularHeuristicFactory.Preference.PREFER_REAL_PLANT) {
      automaton = checkAutomata(automaton, false, nonComposedSpecPlants,
                                new MaxEventComparator(composition, translator),
                                counterExample);
    }
    if (automaton == null ||
        mType == ModularHeuristicFactory.Preference.NOPREF ||
        runspecs) {
      automaton = checkAutomata(automaton, true, nonComposedSpecs,
                                new MaxEventComparator(composition, translator),
                                counterExample);
    }
    return automaton == null ? null : Collections.singleton(automaton);
  }

  private static class MaxEventComparator
    implements Comparator<AutomatonProxy>
  {
    private final Set<EventProxy> mEvents;
    private final KindTranslator mTranslator;

    public MaxEventComparator(final ProductDESProxy composition,
                              final KindTranslator translator)
    {
      mEvents = composition.getEvents();
      mTranslator = translator;
    }

    public int compare(final AutomatonProxy a1, final AutomatonProxy a2)
    {
      int count1 = 0;
      int count2 = 0;
      for (final EventProxy e : a1.getEvents()) {
        if (mTranslator.getEventKind(e).equals(EventKind.UNCONTROLLABLE)) {
          if (mEvents.contains(e)) {
            count1++;
          }
        }
      }
      for (final EventProxy e : a2.getEvents()) {
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
