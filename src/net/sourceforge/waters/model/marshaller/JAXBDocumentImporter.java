//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBDocumentImporter
//###########################################################################
//# $Id: JAXBDocumentImporter.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.io.File;

import net.sourceforge.waters.model.base.DocumentProxy;

import net.sourceforge.waters.xsd.base.NamedType;


public abstract class JAXBDocumentImporter
  <D extends DocumentProxy, T extends NamedType>
  extends JAXBImporter
{

  //#########################################################################
  //# Invocation
  public abstract D importDocument(final T element, final File location);

}
