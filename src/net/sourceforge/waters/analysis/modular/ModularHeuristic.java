//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ModularHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import java.util.Collection;
import java.util.Set;

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.TraceProxy;


/**
 * A heuristic for automata selection for modular (incremental)
 * safety verification.
 *
 * @author Simon Ware
 */

public interface ModularHeuristic
{
  /**
   * Gets a name to identify this heuristic.
   */
  public String getName();

  /**
   * Evaluates this heuristic.
   * @return Set of candidate automata to be considered.
   */
  public Collection<AutomatonProxy> heur
    (ProductDESProxy composition,
     Set<AutomatonProxy> nonComposedPlants,
     Set<AutomatonProxy> nonComposedSpecPlants,
     Set<AutomatonProxy> nonComposedSpecs,
     TraceProxy counterExample,
     KindTranslator translator);
}
