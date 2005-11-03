//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBDocumentExporter
//###########################################################################
//# $Id: JAXBDocumentExporter.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.VisitorException;

import net.sourceforge.waters.xsd.base.NamedType;


abstract class JAXBDocumentExporter
  <D extends DocumentProxy, T extends NamedType>
  extends JAXBExporter
{

  //#########################################################################
  //# Invocation
  T export(final D proxy)
    throws JAXBException
  {
    try {
      return exportDocument(proxy);
    } catch (final VisitorException exception) {
      unwrap(exception);
      return null;
    }
  }


  //#########################################################################
  //# Exporting Documents
  abstract T exportDocument(final D proxy)
    throws VisitorException;

}
