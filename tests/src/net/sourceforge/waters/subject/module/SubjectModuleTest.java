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

import net.sourceforge.waters.model.module.AbstractModuleTest;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


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

  protected ModuleProxyFactory getAlternateModuleProxyFactory()
  {
    return ModuleElementFactory.getInstance();
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.marshaller.AbstractJAXBTest
  protected ModuleSubjectIntegrityChecker getIntegrityChecker()
  {
    return ModuleSubjectIntegrityChecker.getInstance();
  }

}
