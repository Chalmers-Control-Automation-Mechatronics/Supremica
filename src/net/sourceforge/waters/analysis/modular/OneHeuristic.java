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
  public Collection<AutomatonProxy> heur(ProductDESProxy composition,
                                         Set<AutomatonProxy> nonComposedPlants,
                                         Set<AutomatonProxy> nonComposedSpecPlants,
                                         Set<AutomatonProxy> nonComposedSpecs,
                                         TraceProxy counterExample,
                                         KindTranslator translator)
  {
    for (AutomatonProxy automaton : nonComposedPlants) {
      if (accepts(automaton, counterExample) != counterExample.getEvents().size()) {
        return Collections.singleton(automaton);
      }
    }
    for (AutomatonProxy automaton : nonComposedSpecPlants) {
      if (accepts(automaton, counterExample) != counterExample.getEvents().size()) {
        return Collections.singleton(automaton);
      }
    }
    for (AutomatonProxy automaton : nonComposedSpecs) {
      int i = accepts(automaton, counterExample);
      if (i != counterExample.getEvents().size()
          && translator.getEventKind(counterExample.getEvents().get(i)) 
          == EventKind.CONTROLLABLE) {
        return Collections.singleton(automaton);
      }
    }
    return null;
  }
}
