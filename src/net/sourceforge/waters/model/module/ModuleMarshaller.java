//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   ModuleMarshaller
//###########################################################################
//# $Id: ModuleMarshaller.java,v 1.2 2005-05-08 00:27:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.io.File;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.JAXBMarshaller;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.xsd.base.NamedType;
import net.sourceforge.waters.xsd.module.ModuleType;


/**
 * A {@link JAXBMarshaller} that can read and write modules.  This is a
 * convenient wrapper that provides an easy way to convert between Waters
 * Module (<CODE>.wmod</CODE>) files and their corresponding module
 * structures (class {@link ModuleProxy}).
 *
 * @author Robi Malik
 */

public class ModuleMarshaller extends JAXBMarshaller
{

  //#########################################################################
  //# Constructors
  /**
   * Create a new module marshaller.
   */
  public ModuleMarshaller()
    throws JAXBException
  {
    super("net.sourceforge.waters.xsd.module");
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBMarshaller
  public DocumentProxy createProxy(final NamedType doc, final File location)
    throws ModelException
  {
    final ModuleType module = (ModuleType) doc;
    return new ModuleProxy(module, location);
  }

  public NamedType createElement(final DocumentProxy docproxy)
    throws JAXBException
  {
    final ModuleProxy moduleproxy = (ModuleProxy) docproxy;
    return moduleproxy.toModuleType();
  }

  /**
   * Get a default extension for the XML files handled by this wrapper.
   * @return <CODE>".wmod"</CODE>
   */
  public String getDefaultExtension()
  {
    return ".wmod";
  }

  /**
   * Get the class of the documents handled by this wrapper.
   * @return <CODE>ModuleProxy.class</CODE>
   */
  public Class getOutputClass()
  {
    return ModuleProxy.class;
  }

}
