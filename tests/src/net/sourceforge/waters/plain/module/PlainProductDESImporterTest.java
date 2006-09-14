//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   PlainModuleTest
//###########################################################################
//# $Id: PlainProductDESImporterTest.java,v 1.1 2006-09-14 11:31:12 robi Exp $
//###########################################################################


package net.sourceforge.waters.plain.module;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.marshaller.AbstractProductDESImporterTest;
import net.sourceforge.waters.model.module.ModuleProxyFactory;


public class PlainProductDESImporterTest extends AbstractProductDESImporterTest
{

  //#########################################################################
  //# Overrides for junit.framework.TestCase
  public static Test suite() {
    return new TestSuite(PlainProductDESImporterTest.class);
  }

  public static void main(String args[]) {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.module.AbstractProductDESImporterTest
  protected ModuleProxyFactory getModuleProxyFactory()
  {
    return ModuleElementFactory.getInstance();
  }

}
