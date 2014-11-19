//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   TRSynchronousProductResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TRSynchronousProductStateMap;
import net.sourceforge.waters.model.analysis.des.DefaultSynchronousProductResult;


/**
 * @author Robi Malik
 */

public class TRSynchronousProductResult
  extends DefaultSynchronousProductResult
{

  //#########################################################################
  //# Simple Access Methods
  @Override
  public TRAutomatonProxy getComputedAutomaton()
  {
    return (TRAutomatonProxy) super.getComputedAutomaton();
  }

  @Override
  public TRSynchronousProductStateMap getStateMap()
  {
    return (TRSynchronousProductStateMap) super.getStateMap();
  }

}
