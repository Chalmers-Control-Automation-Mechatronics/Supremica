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
    mSimplifier.setOutputName("result");
    mIsomorphismChecker = new IsomorphismChecker(factory, true);
  }

  protected void tearDown() throws Exception
  {
    mSimplifier = null;
    mIsomorphismChecker = null;
    mBindings = null;
    super.tearDown();
  }


  //#########################################################################
  //# Test Cases
  public void testOPempty() throws Exception
  {
    runOPSearch("alpharemoval_8.wmod", true);
  }

  public void testOP1() throws Exception
  {
    runOPSearch("op_1.wmod", false);
  }

  public void testOP2() throws Exception
  {
    runOPSearch("op_2.wmod", true);
  }

  public void testOP3() throws Exception
  {
    runOPSearch("op_3.wmod", false);
  }

  public void testOP3a() throws Exception
  {
    runOPSearch("tauTransRemovalFromNonAlpha_3.wmod", true);
  }

  public void testOP4() throws Exception
  {
    runOPSearch("op_4.wmod", true);
  }

  public void testOP5() throws Exception
  {
    runOPSearch("op_5.wmod", true);
  }

  public void testOP6() throws Exception
  {
    runOPSearch("op_6.wmod", true);
  }

  public void testOP7() throws Exception
  {
    runOPSearch("op_7.wmod", true);
  }

  public void testOP8() throws Exception
  {
    runOPSearch("op_8.wmod", true);
  }

  public void testOP9() throws Exception
  {
    runOPSearch("op_9.wmod", true);
  }

  public void testOP10() throws Exception
  {
    runOPSearch("op_10.wmod", true);
  }

  public void testOP11() throws Exception
  {
    runOPSearch("op_11.wmod", true);
  }

  public void testOP12() throws Exception
  {
    runOPSearch("op_12.wmod", true);
  }

  public void testOP13() throws Exception
  {
    runOPSearch("op_13.wmod", true);
  }

  public void testOP14() throws Exception
  {
    runOPSearch("op_14.wmod", true);
  }

  public void testOP15() throws Exception
  {
    runOPSearch("op_15.wmod", true);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void runOPSearch(final String name, final boolean expect)
    throws Exception
  {
    final ProductDESProxy des = getCompiledDES("tests", "abstraction", name);
    final String desname = des.getName();
    getLogger().info("Checking " + desname + " ...");
    final AutomatonProxy before = findAutomaton(des, BEFORE);
    final List<EventProxy> hidden = getUnobservableEvents(des);
    mSimplifier.setModel(before);
    mSimplifier.setHiddenEvents(hidden);
    final boolean result = mSimplifier.run();
    final AutomatonProxy aut = mSimplifier.getComputedAutomaton();
    final String basename = appendSuffixes(desname, mBindings);
    final String comment =
      "Test output from " + ProxyTools.getShortClassName(mSimplifier) + '.';
    saveAutomaton(aut, basename, comment);
    assertTrue("Unexpected result (false) from OP-Verifier!", result);
    final AutomatonProxy after = getAutomaton(des, AFTER);
    if (after == null) {
      assertSame("Test expects no change, " +
                 "but the object returned is not the same as the input!",
                 before, aut);
    } else {
      mIsomorphismChecker.checkIsomorphism(aut, after);
    }
    getLogger().info("Done " + desname);
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
  private IsomorphismChecker mIsomorphismChecker;
  private List<ParameterBindingProxy> mBindings;


  //#########################################################################
  //# Class Constants
  private final String BEFORE = "before";
  private final String AFTER = "after";

}
