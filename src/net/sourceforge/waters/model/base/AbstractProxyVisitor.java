//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   AbstractProxyVisitor
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Collection;


/**
 * <P>An empty implementation of the {@link ProxyVisitor} interface.</P>
 *
 * <P>This is an adapter class to make it more convenient to implement
 * visitors that do not explicitly implement all the visit methods.
 * All the visit methods in this adapter class do nothing or call the visit
 * method for the immediate superclass of their argument.</P>
 *
 * @author Robi Malik
 */

public class AbstractProxyVisitor implements ProxyVisitor {

  //#########################################################################
  //# Visitor Methods
  public Object visitProxy(final Proxy proxy)
    throws VisitorException
  {
    final StringBuffer buffer = new StringBuffer();
    buffer.append("Visitor class ");
    buffer.append(getClass().getName());
    buffer.append(" cannot handle objects of type ");
    buffer.append(proxy.getClass().getName());
    buffer.append('!');
    throw new UnsupportedOperationException(buffer.toString());
  }

  public Object visitGeometryProxy(final GeometryProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

  public Object visitNamedProxy(final NamedProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

  public Object visitDocumentProxy(final DocumentProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  public Object visitCollection(final Collection<? extends Proxy> collection)
    throws VisitorException
  {
    for (final Proxy proxy : collection) {
      proxy.acceptVisitor(this);
    }
    return null;
  }


  //#########################################################################
  //# Exception Handling
  protected VisitorException wrap(final Throwable exception)
  {
    return new VisitorException(exception);
  }

}
