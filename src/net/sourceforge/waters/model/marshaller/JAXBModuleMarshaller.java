//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBModuleMarshaller
//###########################################################################
//# $Id: JAXBModuleMarshaller.java,v 1.3 2006-07-20 02:28:37 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.marshaller;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.xsd.module.Module;

import org.xml.sax.SAXException;


public class JAXBModuleMarshaller
  extends JAXBMarshaller<ModuleProxy,Module>
{

  //#########################################################################
  //# Constructors
  public JAXBModuleMarshaller(final ModuleProxyFactory factory,
                              final OperatorTable optable)
    throws JAXBException, SAXException
  {
    super(new JAXBModuleExporter(),
          new JAXBModuleImporter(factory, optable),
          "net.sourceforge.waters.xsd.module",
          "waters-module.xsd");
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBMarshaller
  public String getDefaultExtension()
  {
    return ".wmod";
  }

  public Class<ModuleProxy> getDocumentClass()
  {
    return ModuleProxy.class;
  }

  public Class<Module> getElementClass()
  {
    return Module.class;
  }

}
