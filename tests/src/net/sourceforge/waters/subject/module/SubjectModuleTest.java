//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   SubjectModuleTest
//###########################################################################
//# $Id: SubjectModuleTest.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################


package net.sourceforge.waters.subject.module;

import junit.framework.Test;
import junit.framework.TestSuite;
import java.util.List;

import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.SetSubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ModuleTest;


public class SubjectModuleTest extends ModuleTest
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
  //# Overrides for Abstract Base Class JAXBTestCase
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
