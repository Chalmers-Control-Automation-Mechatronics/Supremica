//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.base
//# CLASS:   ElementFactory
//###########################################################################
//# $Id: ElementFactory.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.xsd.base.ElementType;


public interface ElementFactory {

  public ElementType createElement(Proxy proxy) throws JAXBException;

  public ElementType createContainerElement() throws JAXBException;

  public List getElementList(ElementType container);

  public ElementFactory getNextFactory();

}
