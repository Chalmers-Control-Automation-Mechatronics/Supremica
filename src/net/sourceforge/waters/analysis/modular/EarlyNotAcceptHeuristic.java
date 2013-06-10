package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.model.des.TraceProxy;
import java.util.Set;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.model.analysis.KindTranslator;

import java.util.Collections;
import net.sourceforge.waters.model.des.AutomatonProxy;
import java.util.Collection;

public class EarlyNotAcceptHeuristic
  extends AbstractModularHeuristic
{
  private final ModularHeuristicFactory.Preference mType;
	private final boolean foo = true;

  public EarlyNotAcceptHeuristic(final KindTranslator translator)
  {
    this(translator, ModularHeuristicFactory.Preference.PREFER_REAL_PLANT);
  }

  public EarlyNotAcceptHeuristic(final KindTranslator translator,
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
    AutomatonProxy bestautomaton = null;
    int least = Integer.MAX_VALUE;
    for (final AutomatonProxy automaton : nonComposedPlants) {
      final int i = getNumberOfAcceptedEvents(automaton, counterExample);
      if (i != counterExample.getEvents().size()) {
        if (i <= least) {
          bestautomaton = automaton;
          least = i;
        }
      }
    }
    final boolean runspecs = mType == ModularHeuristicFactory.Preference.PREFER_REAL_PLANT && bestautomaton == null;
    if (bestautomaton == null || mType != ModularHeuristicFactory.Preference.PREFER_REAL_PLANT) {
      for (final AutomatonProxy automaton : nonComposedSpecPlants) {
        final int i = getNumberOfAcceptedEvents(automaton, counterExample);
        if (i != counterExample.getEvents().size()) {
          if (i <= least) {
            bestautomaton = automaton;
            least = i;
          }
        }
      }
    }
    if (bestautomaton == null || mType == ModularHeuristicFactory.Preference.NOPREF || (runspecs && foo)) {
      for (final AutomatonProxy automaton : nonComposedSpecs) {
        final KindTranslator translator = getKindTranslator();
        final int i = getNumberOfAcceptedEvents(automaton, counterExample);
        if (i != counterExample.getEvents().size()
            && translator.getEventKind(counterExample.getEvents().get(i))
            == EventKind.CONTROLLABLE) {
          if (i < least) {
            bestautomaton = automaton;
            least = i;
          }
        }
      }
    }
    return bestautomaton == null ? null : Collections.singleton(bestautomaton);
  }
}
