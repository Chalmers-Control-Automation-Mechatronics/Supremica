//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   PlainExpressionTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.expr.AbstractExpressionTest;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public class PlainExpressionTest extends AbstractExpressionTest
{

  //#########################################################################
  //# Constructors
  public PlainExpressionTest()
  {
  }

  public PlainExpressionTest(final String name)
  {
    super(name);
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.expr.AbstractExpressionTest
  protected ModuleProxyFactory getFactory()
  {
    return ModuleElementFactory.getInstance();
  }

}
