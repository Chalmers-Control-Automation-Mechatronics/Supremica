//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyVisitor
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;

/**
 * <P>The general visitor interface for all Waters submodules.</P>
 *
 * @author Robi Malik
 */

public interface ProxyVisitor {

  //#########################################################################
  //# Visitor Methods
  public Object visitProxy(Proxy proxy)
    throws VisitorException;

  public Object visitGeometryProxy(GeometryProxy proxy)
    throws VisitorException;

  public Object visitNamedProxy(NamedProxy proxy)
    throws VisitorException;

  public Object visitDocumentProxy(DocumentProxy proxy)
    throws VisitorException;

}
