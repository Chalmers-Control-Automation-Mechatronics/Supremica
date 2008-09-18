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
  
  public EarlyNotAcceptHeuristic()
  {
    this(ModularHeuristicFactory.Preference.PREFER_REAL_PLANT);
  }
  
  public EarlyNotAcceptHeuristic(ModularHeuristicFactory.Preference type)
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
    AutomatonProxy bestautomaton = null;
    int least = Integer.MAX_VALUE;
    for (AutomatonProxy automaton : nonComposedPlants) {
      int i = accepts(automaton, counterExample);
      if (i != counterExample.getEvents().size()) {
        if (i <= least) {
          bestautomaton = automaton;
          least = i;
        }
      }
    }
    boolean runspecs = mType == ModularHeuristicFactory.Preference.PREFER_REAL_PLANT && bestautomaton == null;
    if (bestautomaton == null || mType != ModularHeuristicFactory.Preference.PREFER_REAL_PLANT) {
      for (AutomatonProxy automaton : nonComposedSpecPlants) {
        int i = accepts(automaton, counterExample);
        if (i != counterExample.getEvents().size()) {
          if (i <= least) {
            bestautomaton = automaton;
            least = i;
          }
        }
      }
    }
    if (bestautomaton == null || mType == ModularHeuristicFactory.Preference.NOPREF || (runspecs && foo)) {
      for (AutomatonProxy automaton : nonComposedSpecs) {
        int i = accepts(automaton, counterExample);
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
