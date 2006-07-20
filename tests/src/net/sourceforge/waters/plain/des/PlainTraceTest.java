//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.des
//# CLASS:   PlainTraceTest
//###########################################################################
//# $Id: PlainTraceTest.java,v 1.2 2006-07-20 02:28:38 robi Exp $
//###########################################################################


package net.sourceforge.waters.plain.des;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.des.AbstractTraceTest;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class PlainTraceTest extends AbstractTraceTest
{

  //#########################################################################
  //# Overrides for junit.framework.TestCase
  public static Test suite() {
    return new TestSuite(PlainTraceTest.class);
  }

  public static void main(String args[]) {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.des.TraceTest
  protected ProductDESProxyFactory getProductDESProxyFactory()
  {
    return ProductDESElementFactory.getInstance();
  }

}
