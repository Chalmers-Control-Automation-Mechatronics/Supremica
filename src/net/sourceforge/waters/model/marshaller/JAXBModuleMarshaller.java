//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBModuleMarshaller
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.model.marshaller;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.xsd.module.Module;

import org.xml.sax.SAXException;


/**
 * <P>A helper class to read and write Waters Module (<CODE>.wmod</CODE>)
 * files.</P>
 *
 * <P>The most convenient way to use a JAXBModuleMarshaller is to create an
 * instance and register it with a {@link DocumentManager} as a marshaller
 * and/or unmarshaller. The {@link DocumentManager} can automatically
 * recognise files by their extension and use the appropriate marshaller to
 * load or save their contents.</P>
 *
 * @see DocumentManager
 * @author Robi Malik
 */

public class JAXBModuleMarshaller
  extends JAXBMarshaller<ModuleProxy,Module>
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new module marshaller.
   * @param  factory   The factory to be used to create the objects when
   *                   loading a module from an XML file.
   * @param  optable   The operator table to be used to create the operators
   *                   when loading expressions in a module from an XML file.
   */
  public JAXBModuleMarshaller(final ModuleProxyFactory factory,
                              final OperatorTable optable)
    throws JAXBException, SAXException
  {
    this(factory, optable, true);
  }

  /**
   * Creates a new module marshaller.
   * @param  factory   The factory to be used to create the objects when
   *                   loading a module from an XML file.
   * @param  optable   The operator table to be used to create the operators
   *                   when loading expressions in a module from an XML file.
   * @param  importgeo A flag, indicating whether geometry information should
   *                   be included when reading files. If <CODE>true</CODE>
   *                   (the default), then all geometry information is included
   *                   in the loaded object model, otherwise all geometry is
   *                   set to <CODE>null</CODE> regardless of the file
   *                   contents.
   */
  public JAXBModuleMarshaller(final ModuleProxyFactory factory,
                              final OperatorTable optable,
                              final boolean importgeo)
    throws JAXBException, SAXException
  {
    super(new JAXBModuleExporter(),
          new JAXBModuleImporter(factory, optable),
          "net.sourceforge.waters.xsd.module",
          "waters-module.xsd");
    setImportingGeometry(importgeo);
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
