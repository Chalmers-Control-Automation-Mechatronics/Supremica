//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   OPSearchAutomatonSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.IsomorphismChecker;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESIntegrityChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


/**
 * @author Robi Malik
 */

public class OPSearchAutomatonSimplifierTest
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  public OPSearchAutomatonSimplifierTest()
  {
  }

  public OPSearchAutomatonSimplifierTest(final String name)
  {
    super(name);
  }

  protected void setUp() throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mSimplifier = new OPSearchAutomatonSimplifier(factory);
    mIntegrityChecker = ProductDESIntegrityChecker.getInstance();
    mIsomorphismChecker = new IsomorphismChecker(factory, false);
  }

  protected void tearDown() throws Exception
  {
    mSimplifier = null;
    mIntegrityChecker = null;
    mIsomorphismChecker = null;
    mBindings = null;
    super.tearDown();
  }


  //#########################################################################
  //# Test Cases
  public void testOPempty() throws Exception
  {
    runOPVerifier("alpharemoval_8.wmod", true);
  }

  public void testOP1() throws Exception
  {
    runOPVerifier("op_1.wmod", false);
  }

  public void testOP2() throws Exception
  {
    runOPVerifier("op_2.wmod", true);
  }

  public void testOP3() throws Exception
  {
    runOPVerifier("op_3.wmod", false);
  }

  public void testOP4() throws Exception
  {
    runOPVerifier("op_4.wmod", true);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void runOPVerifier(final String name, final boolean expect)
    throws Exception
  {
    final ProductDESProxy des = getCompiledDES("tests", "abstraction", name);
    getLogger().info("Checking " + des.getName() + " ...");
    final AutomatonProxy before = findAutomaton(des, BEFORE);
    final List<EventProxy> hidden = getUnobservableEvents(des);
    mSimplifier.setModel(before);
    mSimplifier.setHiddenEvents(hidden);
    final boolean result = mSimplifier.run();
    assertEquals("Unexpected result from OP-Verifier!", expect, result);
    //checkResult(des, before, result);
    getLogger().info("Done " + des.getName());

  }

  @SuppressWarnings("unused")
  private void checkResult(final ProductDESProxy des,
                           final AutomatonProxy before,
                           final AutomatonProxy result)
      throws Exception
  {
    final String name = des.getName();
    final String basename = appendSuffixes(name, mBindings);
    final String comment =
        "Test output from " + ProxyTools.getShortClassName(mSimplifier)
            + '.';
    saveAutomaton(result, basename, comment);
    mIntegrityChecker.check(result, des);
    final AutomatonProxy expected = getAutomaton(des, AFTER);
    if (expected == null) {
      assertSame("Test expects no change, " +
                 "but the object returned is not the same as the input!",
                 before, result);
    } else {
      mIsomorphismChecker.checkIsomorphism(result, expected);
    }
  }



  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.analysis.AbstractAnalysisTest
  protected void configure(final ModuleCompiler compiler)
  {
    compiler.setOptimizationEnabled(false);
  }


  //#########################################################################
  //# Data Members
  private OPSearchAutomatonSimplifier mSimplifier;
  private ProductDESIntegrityChecker mIntegrityChecker;
  private IsomorphismChecker mIsomorphismChecker;
  private List<ParameterBindingProxy> mBindings;


  //#########################################################################
  //# Class Constants
  private final String BEFORE = "before";
  private final String AFTER = "after";

}
