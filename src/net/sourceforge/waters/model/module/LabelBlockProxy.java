//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   LabelBlockProxy
//###########################################################################
//# $Id: LabelBlockProxy.java,v 1.3 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;


/**
 * A list of events with associated geometry information.  The typical
 * place where a label block is found is on the edges of a graph. The basic
 * functionality is implemented and documented in class {@link
 * EventListExpressionProxy}, this class just adds the geometry
 * information.
 *
 * @author Robi Malik
 */

public interface LabelBlockProxy extends EventListExpressionProxy {

  //#########################################################################
  //# Getters and Setters
  public LabelGeometryProxy getGeometry();

}
