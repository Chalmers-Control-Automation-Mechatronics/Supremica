//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBModuleMarshaller
//###########################################################################
//# $Id: JAXBModuleMarshaller.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.marshaller;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.xsd.module.ModuleType;


public class JAXBModuleMarshaller
  extends JAXBMarshaller<ModuleProxy,ModuleType>
{

  //#########################################################################
  //# Constructors
  public JAXBModuleMarshaller(final ModuleProxyFactory factory,
                              final OperatorTable optable)
    throws JAXBException
  {
    super(new JAXBModuleExporter(),
          new JAXBModuleImporter(factory, optable),
          "net.sourceforge.waters.xsd.module");
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

  public Class<ModuleType> getElementClass()
  {
    return ModuleType.class;
  }

}
