//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.des
//# CLASS:   PlainAutomatonTest
//###########################################################################
//# $Id: PlainAutomatonTest.java,v 1.2 2006-07-20 02:28:38 robi Exp $
//###########################################################################


package net.sourceforge.waters.plain.des;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.des.AbstractAutomatonTest;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class PlainAutomatonTest extends AbstractAutomatonTest
{

  //#########################################################################
  //# Overrides for junit.framework.TestCase
  public static Test suite() {
    return new TestSuite(PlainAutomatonTest.class);
  }

  public static void main(String args[]) {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.des.AutomatonTest
  protected ProductDESProxyFactory getProductDESProxyFactory()
  {
    return ProductDESElementFactory.getInstance();
  }

}
