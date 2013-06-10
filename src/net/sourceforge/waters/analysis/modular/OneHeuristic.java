package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.model.des.TraceProxy;
import java.util.Set;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.model.analysis.KindTranslator;

import java.util.Collections;
import net.sourceforge.waters.model.des.AutomatonProxy;
import java.util.Collection;

public class OneHeuristic
  extends AbstractModularHeuristic
{

  //#########################################################################
  //# Constructor
  public OneHeuristic(final KindTranslator translator)
  {
    super(translator);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.modular.ModularHeuristic
  public Collection<AutomatonProxy> heur(final ProductDESProxy composition,
                                         final Set<AutomatonProxy> nonComposedPlants,
                                         final Set<AutomatonProxy> nonComposedSpecPlants,
                                         final Set<AutomatonProxy> nonComposedSpecs,
                                         final TraceProxy counterExample)
  {
    for (final AutomatonProxy automaton : nonComposedPlants) {
      if (getNumberOfAcceptedEvents(automaton, counterExample) != counterExample.getEvents().size()) {
        return Collections.singleton(automaton);
      }
    }
    for (final AutomatonProxy automaton : nonComposedSpecPlants) {
      if (getNumberOfAcceptedEvents(automaton, counterExample) != counterExample.getEvents().size()) {
        return Collections.singleton(automaton);
      }
    }
    for (final AutomatonProxy automaton : nonComposedSpecs) {
      final KindTranslator translator = getKindTranslator();
      final int i = getNumberOfAcceptedEvents(automaton, counterExample);
      if (i != counterExample.getEvents().size()
          && translator.getEventKind(counterExample.getEvents().get(i))
          == EventKind.CONTROLLABLE) {
        return Collections.singleton(automaton);
      }
    }
    return null;
  }
}
