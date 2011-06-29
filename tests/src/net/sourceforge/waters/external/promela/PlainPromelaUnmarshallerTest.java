//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.external.promela
//# CLASS:   PlainPromelaUnmarshallerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.external.promela;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public class PlainPromelaUnmarshallerTest
  extends AbstractPromelaUnmarshallerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(PlainPromelaUnmarshallerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.external.promela.AbstractPromelaUnmarshallerTest
  @Override
  ModuleProxyFactory getModuleProxyFactory()
  {
    return ModuleElementFactory.getInstance();
  }

}
