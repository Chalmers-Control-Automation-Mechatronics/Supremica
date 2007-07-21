//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   VariableComponentProxy
//###########################################################################
//# $Id: VariableComponentProxy.java,v 1.1 2007-07-21 08:46:39 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.util.List;


/**
 * <P>A component representing an EFA variable.</P>
 *
 * @author Robi Malik
 */

public interface VariableComponentProxy extends ComponentProxy {

  //#########################################################################
  //# Simple Access
  public SimpleExpressionProxy getType();

  public boolean isDeterministic();

  public SimpleExpressionProxy getInitialStatePredicate();

  public List<VariableMarkingProxy> getVariableMarkings();

}
