//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBDocumentImporter
//###########################################################################
//# $Id: JAXBDocumentImporter.java,v 1.3 2006-02-20 22:20:21 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.net.URI;

import net.sourceforge.waters.model.base.DocumentProxy;

import net.sourceforge.waters.xsd.base.NamedType;


public abstract class JAXBDocumentImporter
  <D extends DocumentProxy, T extends NamedType>
  extends JAXBImporter
{

  //#########################################################################
  //# Invocation
  public abstract D importDocument(final T element, final URI uri);

}
