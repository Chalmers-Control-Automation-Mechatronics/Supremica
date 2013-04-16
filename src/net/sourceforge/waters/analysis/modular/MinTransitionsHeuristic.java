package net.sourceforge.waters.analysis.modular;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.TraceProxy;


public class MinTransitionsHeuristic
  extends AbstractModularHeuristic
{
  private final ModularHeuristicFactory.Preference mType;

  public MinTransitionsHeuristic(final KindTranslator translator)
  {
    this(translator, ModularHeuristicFactory.Preference.PREFER_REAL_PLANT);
  }

  public MinTransitionsHeuristic(final KindTranslator translator,
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
    AutomatonProxy automaton = checkAutomata(false, nonComposedPlants,
                                             new MinTransitionsComparator(),
                                             counterExample);
    final boolean runspecs = mType == ModularHeuristicFactory.Preference.PREFER_REAL_PLANT && automaton == null;
    if (automaton == null || mType != ModularHeuristicFactory.Preference.PREFER_REAL_PLANT) {
      automaton = checkAutomata(automaton, false, nonComposedSpecPlants,
                                new MinTransitionsComparator(),
                                counterExample);
    }
    if (automaton == null || mType == ModularHeuristicFactory.Preference.NOPREF || runspecs) {
      automaton = checkAutomata(automaton, true, nonComposedSpecs,
                                new MinTransitionsComparator(),
                                counterExample);
    }
    return automaton == null ? null : Collections.singleton(automaton);
  }

  private static class MinTransitionsComparator
    implements Comparator<AutomatonProxy>
  {
    public int compare(final AutomatonProxy a1, final AutomatonProxy a2)
    {
      return a2.getTransitions().size() - a1.getTransitions().size();
    }
  }
}
