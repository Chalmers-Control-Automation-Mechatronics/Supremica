//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBDocumentImporter
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sourceforge.waters.model.base.DocumentProxy;

import net.sourceforge.waters.xsd.base.Attribute;
import net.sourceforge.waters.xsd.base.AttributeMap;
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

  public void setDocumentManager(final DocumentManager manager)
  {
    mDocumentManager = manager;
  }


  //#########################################################################
  //# Importing Elements
  Map<String,String> importAttributeMap(final AttributeMap element)
  {
    if (element == null) {
      return null;
    } else {
      final List<Attribute> list = element.getList();
      final TreeMap<String,String> map = new TreeMap<String,String>();
      for (final Attribute attrib : list) {
        final String name = attrib.getName();
        final String value = attrib.getValue();
        map.put(name, value);
      }
      return map;
    }
  }


  //#########################################################################
  //# Data Members
  private DocumentManager mDocumentManager;

}
