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
  private final HeuristicType mType;
	private final boolean foo = true;
  
  public AllHeuristic(HeuristicType type)
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
    Collection<AutomatonProxy> automata = new ArrayList<AutomatonProxy>();
    for (AutomatonProxy automaton : nonComposedPlants) {
      if (accepts(automaton, counterExample) != counterExample.getEvents().size()) {
        automata.add(automaton);
      }
    }
    boolean runspecs = mType == HeuristicType.PREFERREALPLANT && automata.isEmpty();
    if (automata.size() == 0 || mType != HeuristicType.PREFERREALPLANT) {
      for (AutomatonProxy automaton : nonComposedSpecPlants) {
        if (accepts(automaton, counterExample) != counterExample.getEvents().size()) {
          automata.add(automaton);
        }
      }
    }
    if (automata.size() == 0 || mType == HeuristicType.NOPREF || (runspecs && foo)) {
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
