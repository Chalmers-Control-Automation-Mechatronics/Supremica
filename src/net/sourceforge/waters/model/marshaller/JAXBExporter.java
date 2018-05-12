//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.model.marshaller;

import java.util.Collection;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DefaultProxyVisitor;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.GeometryProxy;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.base.VisitorException;

import net.sourceforge.waters.xsd.base.DocumentType;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.base.NamedType;


abstract class JAXBExporter
  extends DefaultProxyVisitor
{

  //#########################################################################
  //# Invocation
  public ElementType export(final Proxy proxy)
    throws JAXBException
  {
    try {
      return exportProxy(proxy);
    } catch (final VisitorException exception) {
      unwrap(exception);
      return null;
    }
  }


  //#########################################################################
  //# Exporting Proxies
  public ElementType exportProxy(final Proxy proxy)
    throws VisitorException
  {
    return (ElementType) proxy.acceptVisitor(this);
  }


  //#########################################################################
  //# Copying Data
  void copyProxy(final Proxy proxy, final ElementType element)
  {
  }

  void copyGeometryProxy(final GeometryProxy proxy, final ElementType element)
  {
    copyProxy(proxy, element);
  }

  void copyNamedProxy(final NamedProxy proxy, final NamedType element)
  {
    copyProxy(proxy, element);
    element.setName(proxy.getName());
  }

  void copyDocumentProxy(final DocumentProxy proxy, final DocumentType element)
  {
    copyNamedProxy(proxy, element);
    element.setComment(proxy.getComment());
  }

  void copyCollection(final Collection<? extends Proxy> proxies,
                      final List<ElementType> elements)
    throws VisitorException
  {
    for (final Proxy proxy : proxies) {
      final ElementType element = exportProxy(proxy);
      elements.add(element);
    }
  }


  //#########################################################################
  //# Exception Handling
  void unwrap(final VisitorException exception)
    throws JAXBException
  {
    final Throwable cause = exception.getCause();
    if (cause instanceof JAXBException) {
      throw (JAXBException) cause;
    } else {
      throw new WatersRuntimeException(cause);
    }
  }

}
