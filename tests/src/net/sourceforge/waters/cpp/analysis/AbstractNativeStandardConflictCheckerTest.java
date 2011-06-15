//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   AbstractNativeStandardConflictCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import net.sourceforge.waters.model.analysis.AbstractStandardConflictCheckerTest;


public abstract class AbstractNativeStandardConflictCheckerTest
  extends AbstractStandardConflictCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public AbstractNativeStandardConflictCheckerTest()
  {
  }

  public AbstractNativeStandardConflictCheckerTest(final String name)
  {
    super(name);
  }


  //#########################################################################
  //# Test Case Overrides
  @Override
  public void testWickedCounting() throws Exception
  {
    checkWickedCounting(20);
  }

}
