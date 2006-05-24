//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   GuardActionBlockProxy
//###########################################################################
//# $Id: GuardActionBlockProxy.java,v 1.3 2006-05-24 09:13:02 markus Exp $
//###########################################################################

package net.sourceforge.waters.model.module;
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
  
  // @optional 
  public String getAction();
  
  public LabelGeometryProxy getGeometry();
}