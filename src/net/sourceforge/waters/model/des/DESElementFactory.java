//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.des
//# CLASS:   DESElementFactory
//###########################################################################
//# $Id: DESElementFactory.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.des;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.xsd.des.ObjectFactory;


abstract class DESElementFactory implements ElementFactory {

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