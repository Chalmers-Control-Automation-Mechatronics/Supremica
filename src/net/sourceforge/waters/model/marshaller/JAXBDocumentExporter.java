//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBDocumentExporter
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.xsd.base.Attribute;
import net.sourceforge.waters.xsd.base.AttributeMap;
import net.sourceforge.waters.xsd.base.NamedType;
import net.sourceforge.waters.xsd.base.ObjectFactory;


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


  //#########################################################################
  //# Exporting Element
  AttributeMap createAttributeMap(final Map<String,String> attribs)
  {
    final AttributeMap mapElement = mFactory.createAttributeMap();
    final List<Attribute> list = mapElement.getList();
    for (final Map.Entry<String,String> entry : attribs.entrySet()) {
      final String name = entry.getKey();
      final String value = entry.getValue();
      final Attribute element = mFactory.createAttribute();
      element.setName(name);
      if (!value.equals("")) {
        element.setValue(value);
      }
      list.add(element);
    }
    return mapElement;
  }


  //#########################################################################
  //# Data Members
  private static final ObjectFactory mFactory = new ObjectFactory();

}
