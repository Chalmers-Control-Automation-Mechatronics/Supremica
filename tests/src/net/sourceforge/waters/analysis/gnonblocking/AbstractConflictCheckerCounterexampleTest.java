//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractStandardConflictCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import net.sourceforge.waters.analysis.modular.ProjectingLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.AbstractConflictCheckerTest;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public abstract class AbstractConflictCheckerCounterexampleTest extends
    AbstractConflictCheckerTest
{

  // #########################################################################
  // # Entry points in junit.framework.TestCase
  public AbstractConflictCheckerCounterexampleTest()
  {
  }

  public AbstractConflictCheckerCounterexampleTest(final String name)
  {
    super(name);
  }

  // #########################################################################
  // # Overrides for
  // # net.sourceforge.waters.model.analysis.AbstractConflictCheckerTest
  protected LanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxy des, final ProductDESProxyFactory factory)
  {
    final LanguageInclusionChecker checker =
      super.createLanguageInclusionChecker(des, factory);
    return new ProjectingLanguageInclusionChecker(des, factory, checker);
  }

  // #########################################################################
  // # Auxiliary Methods
  private void setConfiguration(final int islimit, final int fslimit,
                                final int itlimit, final int ftlimit)
  {
    final CompositionalGeneralisedConflictChecker checker =
        (CompositionalGeneralisedConflictChecker) getModelVerifier();
    checker.setInternalStepNodeLimit(islimit);
    checker.setFinalStepNodeLimit(fslimit);
    checker.setInternalStepTransitionLimit(itlimit);
    checker.setFinalStepTransitionLimit(ftlimit);
  }


  // #########################################################################
  // # Test Cases
  public void testFischertechnik() throws Exception
  {
    setConfiguration(10000, 10000, 1000000, 1000000);
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "ftechnik.wmod";
    runModelVerifier(group, dir, name, false);
  }

  public void testRhoneAlps() throws Exception
  {
    setConfiguration(10000, 10000, 1000000, 1000000);
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "rhone_alps.wmod";
    runModelVerifier(group, dir, name, false);
  }

  /*
   * public void testRhoneTough() throws Exception { final String group =
   * "tests"; final String dir = "incremental_suite"; final String name =
   * "rhone_tough.wmod"; runModelVerifier(group, dir, name, false); // not sure
   * about result ... }
   */// TODO:get configs

  public void testTbedCTCT() throws Exception
  {
    setConfiguration(10000, 10000, 1000000, 1000000);

    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_ctct.wmod";
    runModelVerifier(group, dir, name, false);
  }

  public void testTbedNoderailBlock() throws Exception
  {
    setConfiguration(10000, 100000, 1000000, 1000000);
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_noderail_block.wmod";
    runModelVerifier(group, dir, name, false);
  }

  public void testVerriegel4B() throws Exception
  {
    setConfiguration(10000, 10000, 1000000, 1000000);

    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "verriegel4b.wmod";
    runModelVerifier(group, dir, name, false);
  }

  public void testTip3Bad() throws Exception
  {
    setConfiguration(2000, 1000000, 200000, 0);

    final String group = "tip";
    final String dir = "acsw2006";
    final String name = "tip3_bad.wmod";
    runModelVerifier(group, dir, name, false);
  }
}
