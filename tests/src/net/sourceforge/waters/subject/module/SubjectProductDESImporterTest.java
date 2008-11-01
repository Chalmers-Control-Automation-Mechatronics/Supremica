//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   SubjectModuleTest
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.subject.module;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.marshaller.AbstractProductDESImporterTest;
import net.sourceforge.waters.model.module.ModuleProxyFactory;


public class SubjectProductDESImporterTest
  extends AbstractProductDESImporterTest
{

  //#########################################################################
  //# Overrides for junit.framework.TestCase
  public static Test suite() {
    return new TestSuite(SubjectProductDESImporterTest.class);
  }

  public static void main(String args[]) {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.module.AbstractProductDESImporterTest
  protected ModuleProxyFactory getModuleProxyFactory()
  {
    return ModuleSubjectFactory.getInstance();
  }

}
