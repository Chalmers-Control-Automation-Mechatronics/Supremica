//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   SimpleExpressionElementFactory
//###########################################################################
//# $Id: SimpleExpressionElementFactory.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.ObjectFactory;
import net.sourceforge.waters.xsd.module.SimpleExpressionType;


public class SimpleExpressionElementFactory implements ElementFactory {

  //#########################################################################
  //# The Factory
  static ObjectFactory getFactory()
  {
    return sFactory;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.ElementFactory
  public ElementType createElement(final Proxy proxy)
    throws JAXBException
  {
    final SimpleExpressionProxy expr = (SimpleExpressionProxy) proxy;
    return expr.createElement(getFactory());
  }

  public ElementType createContainerElement() throws JAXBException
  {
    throw new UnsupportedOperationException
      ("No default list implemented in " + getClass().getName() + "!");
  }

  public List getElementList(ElementType container)
  {
    throw new UnsupportedOperationException
      ("No default list implemented in " + getClass().getName() + "!");
  }

  public ElementFactory getNextFactory()
  {
    return this;
  }


  //#########################################################################
  //# Class Variables
  static final ObjectFactory sFactory = new ObjectFactory();

}