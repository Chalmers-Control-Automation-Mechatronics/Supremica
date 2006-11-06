//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBModuleMarshaller
//###########################################################################
//# $Id: JAXBModuleMarshaller.java,v 1.5 2006-11-06 14:19:19 torda Exp $
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
          "/xsd/waters-module.xsd");
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBMarshaller
  public String getDefaultExtension()
  {
    return ".wmod";
  }
  
  public String getDescription()
  {
      return "Waters Module files [*.wmod]";
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
