//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   PlainModuleTest
//###########################################################################
//# $Id: PlainModuleTest.java,v 1.3 2006-07-20 02:28:38 robi Exp $
//###########################################################################


package net.sourceforge.waters.plain.module;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.module.AbstractModuleTest;
import net.sourceforge.waters.model.module.ModuleProxyFactory;


public class PlainModuleTest extends AbstractModuleTest
{

  //#########################################################################
  //# Overrides for junit.framework.TestCase
  public static Test suite() {
    return new TestSuite(PlainModuleTest.class);
  }

  public static void main(String args[]) {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.module.ModuleTest
  protected ModuleProxyFactory getModuleProxyFactory()
  {
    return ModuleElementFactory.getInstance();
  }

}
