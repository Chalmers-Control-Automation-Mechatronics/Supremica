package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.model.des.TraceProxy;
import java.util.Set;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.model.analysis.KindTranslator;
import java.util.Collections;
import net.sourceforge.waters.model.des.AutomatonProxy;
import java.util.Collection;

public class LateNotAcceptHeuristic
  extends AbstractModularHeuristic
{
  private final boolean mPreffersystem;
  
  public LateNotAcceptHeuristic(boolean preffersystem)
  {
    mPreffersystem = preffersystem;
  }
  
  public Collection<AutomatonProxy> heur(ProductDESProxy composition,
                                         Set<AutomatonProxy> nonComposedPlants,
                                         Set<AutomatonProxy> nonComposedSpecs,
                                         TraceProxy counterExample,
                                         KindTranslator translator)
  {
    AutomatonProxy bestautomaton = null;
    int greatest = Integer.MIN_VALUE;
    for (AutomatonProxy automaton : nonComposedPlants) {
      int i = accepts(automaton, counterExample);
      if (i != counterExample.getEvents().size()) {
        if (i > greatest) {
          bestautomaton = automaton;
          greatest = i;
        }
      }
    }
    if (bestautomaton == null || mPreffersystem) {
      for (AutomatonProxy automaton : nonComposedSpecs) {
        int i = accepts(automaton, counterExample);
        if (i != counterExample.getEvents().size()
            && translator.getEventKind(counterExample.getEvents().get(i)) 
            == EventKind.CONTROLLABLE) {
          if (i > greatest) {
            bestautomaton = automaton;
            greatest = i;
          }
        }
      }
    }
    return bestautomaton == null ? null : Collections.singleton(bestautomaton);
  }
}
