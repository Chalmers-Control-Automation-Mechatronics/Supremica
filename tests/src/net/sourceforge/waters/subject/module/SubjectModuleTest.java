//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   SubjectModuleTest
//###########################################################################
//# $Id: SubjectModuleTest.java,v 1.5 2006-11-03 15:01:57 torda Exp $
//###########################################################################


package net.sourceforge.waters.subject.module;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.module.AbstractModuleTest;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;


public class SubjectModuleTest extends AbstractModuleTest
{

  //#########################################################################
  //# Overrides for junit.framework.TestCase
  public static Test suite() {
    return new TestSuite(SubjectModuleTest.class);
  }

  public static void main(String args[]) {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.module.ModuleTest
  protected ModuleProxyFactory getModuleProxyFactory()
  {
    return ModuleSubjectFactory.getInstance();
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.marshaller.AbstractJAXBTest
  protected void checkIntegrity(final ModuleProxy module)
    throws Exception
  {
    super.checkIntegrity(module);
    mChecker.check(module);
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
    throws Exception
  { 
    super.setUp();
    mChecker = new ModuleHierarchyChecker();
  }

  protected void tearDown()
    throws Exception
  {
    mChecker = null;
    super.tearDown();
  }


  //#########################################################################
  //# Data Members
  private ModuleHierarchyChecker mChecker;

}
