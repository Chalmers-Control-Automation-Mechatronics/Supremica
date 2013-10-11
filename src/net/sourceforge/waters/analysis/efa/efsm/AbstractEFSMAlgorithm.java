//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   AbstractEFSMAlgorithm
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import java.util.Collection;
import java.util.Collections;

import net.sourceforge.waters.analysis.efa.base.AbstractEFAAlgorithm;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;


/**
 * @author Sahar Mohajerani, Robi Malik
 */

abstract class AbstractEFSMAlgorithm
  extends AbstractEFAAlgorithm
{

  //#########################################################################
  //# Simple Access
  /**
   * Returns a collection of updates found to be selfloops.
   * This method is called after running an EFSM simplifier.
   * The default implementation returns an empty list, but subclasses
   * may provide more useful information.
   */
  Collection<ConstraintList> getSelfloopedUpdates()
  {
    return Collections.emptyList();
  }

}
