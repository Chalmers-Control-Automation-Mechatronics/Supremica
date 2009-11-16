//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractGeneralisedConflictCheckerTest
//###########################################################################
//# $Id: AbstractGeneralisedConflictCheckerTest.java 4768 2009-10-09 03:16:33Z robi $
//###########################################################################

package net.sourceforge.waters.model.analysis;


public abstract class AbstractGeneralisedConflictCheckerTest
  extends AbstractConflictCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public AbstractGeneralisedConflictCheckerTest()
  {
  }

  public AbstractGeneralisedConflictCheckerTest(final String name)
  {
    super(name);
  }


  //#########################################################################
  //#Test Cases --- paper (multi-coloured automata)
  public void testG1() throws Exception
  {
    final String group = "tests";
    final String dir = "paper";
    final String name = "g1.wmod";
    runModelVerifier(group, dir, name, true);
  }

  /*
  public void testG2() throws Exception
  {
    final String group = "tests";
    final String dir = "paper";
    final String name = "g2.wmod";
    runModelVerifier(group, dir, name, true);
  }
  */

  public void testG3() throws Exception
  {
    final String group = "tests";
    final String dir = "paper";
    final String name = "g3.wmod";
    runModelVerifier(group, dir, name, false);
  }

  public void testG4() throws Exception
  {
    final String group = "tests";
    final String dir = "paper";
    final String name = "g4.wmod";
    runModelVerifier(group, dir, name, false);
  }

}
