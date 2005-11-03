//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.des
//# CLASS:   PlainProductDESTest
//###########################################################################
//# $Id: PlainProductDESTest.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################


package net.sourceforge.waters.plain.des;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESTest;


public class PlainProductDESTest extends ProductDESTest
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
