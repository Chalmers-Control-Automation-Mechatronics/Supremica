//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyVisitor
//###########################################################################
//# $Id: ProxyVisitor.java,v 1.2 2005-11-03 01:24:15 robi Exp $
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
