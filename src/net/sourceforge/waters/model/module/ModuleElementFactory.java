//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   ModuleElementFactory
//###########################################################################
//# $Id: ModuleElementFactory.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.xsd.module.ObjectFactory;


abstract class ModuleElementFactory implements ElementFactory {

  //#########################################################################
  //# The Factory
  static ObjectFactory getFactory()
  {
    return sFactory;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.ElementFactory
  public ElementFactory getNextFactory()
  {
    return this;
  }


  //#########################################################################
  //# Class Variables
  static final ObjectFactory sFactory = new ObjectFactory();

}