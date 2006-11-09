package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import java.util.Set;
import java.util.Collection;
import java.util.List;
import net.sourceforge.waters.model.des.AutomatonProxy;

public interface ModularHeuristic
{
  public Collection<AutomatonProxy> heur(ProductDESProxy composition,
                                         Set<AutomatonProxy> nonComposedPlants,
                                         Set<AutomatonProxy> nonComposedSpecs,
                                         TraceProxy counterExample,
                                         KindTranslator translator);
}
