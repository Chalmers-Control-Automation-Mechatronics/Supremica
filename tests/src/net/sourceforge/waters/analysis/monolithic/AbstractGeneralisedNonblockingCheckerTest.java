//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   AbstractGeneralisedNonblockingCheckerTest
//###########################################################################
//# $Id: AbstractGeneralisedNonblockingCheckerTest.java 4768 2009-10-09 03:16:33Z robi $
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractConflictCheckerTest;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class AbstractGeneralisedNonblockingCheckerTest
  extends AbstractConflictCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    TestSuite testSuite =
      new TestSuite(AbstractGeneralisedNonblockingCheckerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected ConflictChecker
    createModelVerifier(final ProductDESProxyFactory factory)
  {
    return new MonolithicConflictChecker(factory);
  }


  //#########################################################################
  //#Test Cases --- paper (multi-coloured automata)

  public void testG1() throws Exception
  {
    final String group = "tests";
    final String dir = "paper";
    final String name = "g1.wmod";
    runModelVerifier(group, dir, name, true);
  }/*
  public void testG2() throws Exception
  {
    final String group = "tests";
    final String dir = "paper";
    final String name = "g2.wmod";
    runModelVerifier(group, dir, name, true);
  }*/
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
