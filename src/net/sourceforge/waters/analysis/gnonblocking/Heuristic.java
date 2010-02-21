//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   Heuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.Collection;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * @author rmf18
 */
public interface Heuristic
{
  public Collection<Candidate> evaluate(final ProductDESProxy model);

}
