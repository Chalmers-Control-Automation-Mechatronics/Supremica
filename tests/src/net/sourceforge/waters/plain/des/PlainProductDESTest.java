//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.des
//# CLASS:   PlainProductDESTest
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.plain.des;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.des.AbstractProductDESTest;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class PlainProductDESTest extends AbstractProductDESTest
{

  //#########################################################################
  //# Overrides for junit.framework.TestCase
  public static Test suite() {
    return new TestSuite(PlainProductDESTest.class);
  }

  public static void main(String args[]) {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.des.ProductDESTest
  protected ProductDESProxyFactory getProductDESProxyFactory()
  {
    return ProductDESElementFactory.getInstance();
  }

}
