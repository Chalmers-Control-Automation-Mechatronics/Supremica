//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBDocumentImporter
//###########################################################################
//# $Id: JAXBDocumentImporter.java,v 1.4 2006-07-20 02:28:37 robi Exp $
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
  public abstract D importDocument(final T element, final URI uri)
    throws WatersUnmarshalException;


  //#########################################################################
  //# Entity Resolving
  public DocumentManager getDocumentManager()
  {
    return mDocumentManager;
  }

  public void setDocumentManager(DocumentManager manager)
  {
    mDocumentManager = manager;
  }


  //#########################################################################
  //# Data Members
  private DocumentManager mDocumentManager;

}
