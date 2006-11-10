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

public class MinStatesHeuristic
  extends AbstractModularHeuristic
{
  private final boolean mPrefferSystem;
  
  public MinStatesHeuristic(boolean prefferSystem)
  {
    mPrefferSystem = prefferSystem;
  }
  
  public Collection<AutomatonProxy> heur(ProductDESProxy composition,
                                         Set<AutomatonProxy> nonComposedPlants,
                                         Set<AutomatonProxy> nonComposedSpecs,
                                         TraceProxy counterExample,
                                         KindTranslator translator)
  {
    AutomatonProxy automaton = checkAutomata(false, nonComposedPlants,
                                             new MinStatesComparator(),
                                             counterExample, translator);
    if (automaton == null || !mPrefferSystem) {
      automaton = checkAutomata(automaton, true, nonComposedSpecs, 
                                new MinStatesComparator(),
                                counterExample, translator);
    }
    return automaton == null ? null : Collections.singleton(automaton);
  }
  
  private static class MinStatesComparator
    implements Comparator<AutomatonProxy>
  {
    public int compare(AutomatonProxy a1, AutomatonProxy a2)
    {
      return a2.getStates().size() - a1.getStates().size();
    }
  }
}
