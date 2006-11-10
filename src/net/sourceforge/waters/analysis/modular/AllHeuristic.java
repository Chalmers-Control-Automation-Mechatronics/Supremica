package net.sourceforge.waters.analysis.modular;

import java.util.ArrayList;
import net.sourceforge.waters.model.des.TraceProxy;
import java.util.Set;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.model.analysis.KindTranslator;
import java.util.Collections;
import net.sourceforge.waters.model.des.AutomatonProxy;
import java.util.Collection;

public class AllHeuristic
  extends AbstractModularHeuristic
{
  private final boolean mPrefferSystem;
  
  public AllHeuristic(boolean prefferSystem)
  {
    mPrefferSystem = prefferSystem;
  }
  
  public Collection<AutomatonProxy> heur(ProductDESProxy composition,
                                         Set<AutomatonProxy> nonComposedPlants,
                                         Set<AutomatonProxy> nonComposedSpecs,
                                         TraceProxy counterExample,
                                         KindTranslator translator)
  {
    Collection<AutomatonProxy> automata = new ArrayList<AutomatonProxy>();
    for (AutomatonProxy automaton : nonComposedPlants) {
      if (accepts(automaton, counterExample) != counterExample.getEvents().size()) {
        automata.add(automaton);
      }
    }
    if (!mPrefferSystem || automata.size() == 0) {
      for (AutomatonProxy automaton : nonComposedSpecs) {
        int i = accepts(automaton, counterExample);
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
