//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   SubjectExpressionTest
//###########################################################################
//# $Id: SubjectExpressionTest.java,v 1.1 2007-11-19 02:16:52 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.expr.AbstractExpressionTest;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;


public class SubjectExpressionTest extends AbstractExpressionTest
{

  //#########################################################################
  //# Constructors
  public SubjectExpressionTest()
  {
  }

  public SubjectExpressionTest(final String name)
  {
    super(name);
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.expr.AbstractExpressionTest
  protected ModuleProxyFactory getFactory()
  {
    return ModuleSubjectFactory.getInstance();
  }

}
