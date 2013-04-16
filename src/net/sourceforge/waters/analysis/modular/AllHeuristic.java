package net.sourceforge.waters.analysis.modular;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.xsd.base.EventKind;


public class AllHeuristic
  extends AbstractModularHeuristic
{
  private final ModularHeuristicFactory.Preference mType;

  public AllHeuristic(final KindTranslator translator)
  {
    this(translator, ModularHeuristicFactory.Preference.PREFER_REAL_PLANT);
  }

  public AllHeuristic(final KindTranslator translator,
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
    final Collection<AutomatonProxy> automata = new ArrayList<AutomatonProxy>();
    for (final AutomatonProxy automaton : nonComposedPlants) {
      if (getNumberOfAcceptedEvents(automaton, counterExample) != counterExample.getEvents().size()) {
        automata.add(automaton);
      }
    }
    final boolean runspecs = mType == ModularHeuristicFactory.Preference.PREFER_REAL_PLANT && automata.isEmpty();
    if (automata.size() == 0 || mType != ModularHeuristicFactory.Preference.PREFER_REAL_PLANT) {
      for (final AutomatonProxy automaton : nonComposedSpecPlants) {
        if (getNumberOfAcceptedEvents(automaton, counterExample) != counterExample.getEvents().size()) {
          automata.add(automaton);
        }
      }
    }
    if (automata.size() == 0 ||
        mType == ModularHeuristicFactory.Preference.NOPREF ||
        runspecs) {
      final KindTranslator translator = getKindTranslator();
      for (final AutomatonProxy automaton : nonComposedSpecs) {
        final int i = getNumberOfAcceptedEvents(automaton, counterExample);
        if (i != counterExample.getEvents().size()
            && translator.getEventKind(counterExample.getEvents().get(i))
            == EventKind.CONTROLLABLE) {
          automata.add(automaton);
        }
      }
    }
    return automata.size() == 0 ? null : automata;
  }
}
