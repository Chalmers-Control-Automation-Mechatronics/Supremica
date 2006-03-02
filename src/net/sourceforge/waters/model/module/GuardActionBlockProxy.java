//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   GuardActionBlockProxy
//###########################################################################
//# $Id: GuardActionBlockProxy.java,v 1.2 2006-03-02 12:12:49 martin Exp $
//###########################################################################

package net.sourceforge.waters.model.module;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;

/**
 * Guard and Actions with associated geometry information. A GuardActionBlock 
 * is found is on the edges of a graph. 
 *
 * @author Markus Sköldstam
 */

public interface GuardActionBlockProxy extends Proxy {

  //#########################################################################
  //# Getters and Setters
  
  // @optional 
  public String getGuard();
   
  public List<BinaryExpressionProxy> getActionList();
  
  public LabelGeometryProxy getGeometry();
}