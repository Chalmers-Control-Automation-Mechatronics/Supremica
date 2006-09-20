//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   SubjectModuleTest
//###########################################################################
//# $Id: SubjectModuleTest.java,v 1.4 2006-09-20 16:24:13 robi Exp $
//###########################################################################


package net.sourceforge.waters.subject.module;

import junit.framework.Test;
import junit.framework.TestSuite;
import java.util.List;

import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.SetSubject;
import net.sourceforge.waters.subject.base.Subject;
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
