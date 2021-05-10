//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Collection;


/**
 * <P>An empty implementation of the {@link ProxyVisitor} interface.</P>
 *
 * <P>This is an adapter class to make it more convenient to implement
 * visitors that do not explicitly implement all the visit methods.
 * All the visit methods in this adapter class do nothing or call the visit
 * method for the immediate superclass of their argument. All these calls
 * eventually trigger the base method {@link #visitProxy(Proxy) visitProxy()},
 * which throws an {@link UnsupportedOperationException}.</P>
 *
 * <P>A useful visitor is implemented by extending this class and overriding
 * some of the visit methods. As an alternative, consider extending from
 * {@link DescendingProxyVisitor}, whose visit methods includes calls to
 * all referenced proxies of the argument.</P>
 *
 * @author Robi Malik
 */

abstract public class DefaultProxyVisitor implements ProxyVisitor {

  //#########################################################################
  //# Visitor Methods
  @Override
  public Object visitProxy(final Proxy proxy)
    throws VisitorException
  {
    final StringBuilder buffer = new StringBuilder();
    buffer.append("Visitor class ");
    buffer.append(getClass().getName());
    buffer.append(" cannot handle objects of type ");
    buffer.append(proxy.getClass().getName());
    buffer.append('!');
    throw new UnsupportedOperationException(buffer.toString());
  }

  @Override
  public Object visitGeometryProxy(final GeometryProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

  @Override
  public Object visitNamedProxy(final NamedProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

  @Override
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
