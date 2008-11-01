//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   GuardActionBlockProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.module;

import java.util.List;

import net.sourceforge.waters.model.base.Proxy;

/**
 * Guard and Actions with associated geometry information. A GuardActionBlock 
 * is found on the edges of a graph. 
 *
 * @author Markus Sk&ouml;ldstam
 */

public interface GuardActionBlockProxy extends Proxy {

  //#########################################################################
  //# Getters and Setters  
  public List<SimpleExpressionProxy> getGuards();
  
  public List<BinaryExpressionProxy> getActions();
  
  public LabelGeometryProxy getGeometry();

}
