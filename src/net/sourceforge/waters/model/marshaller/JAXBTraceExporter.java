//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBTraceExporter
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.TraceProxy;

import net.sourceforge.waters.xsd.des.TraceType;


class JAXBTraceExporter
  extends JAXBProductDESElementExporter<TraceProxy,TraceType>
{

  //#########################################################################
  //# Overrides for Abstract Base Class JAXBExporter
  TraceType exportDocument(final TraceProxy proxy)
    throws VisitorException
  {
    return (TraceType) exportProxy(proxy);
  }

}
