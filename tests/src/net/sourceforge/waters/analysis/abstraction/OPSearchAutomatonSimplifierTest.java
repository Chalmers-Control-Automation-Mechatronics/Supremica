//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.des.IsomorphismChecker;
import net.sourceforge.waters.model.analysis.kindtranslator.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
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
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    mSimplifier = new OPSearchAutomatonSimplifier(factory, translator);
    mSimplifier.setOutputName("result");
    mIsomorphismChecker = new IsomorphismChecker(factory, true, true);
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
    runOPSearch("alpharemoval_8.wmod");
  }

  public void testOP1() throws Exception
  {
    runOPSearch("op01.wmod");
  }

  public void testOP2() throws Exception
  {
    runOPSearch("op02.wmod");
  }

  public void testOP3() throws Exception
  {
    runOPSearch("op03.wmod");
  }

  public void testOP3a() throws Exception
  {
    runOPSearch("tauTransRemovalFromNonAlpha_3.wmod");
  }

  public void testOP4() throws Exception
  {
    runOPSearch("op04.wmod");
  }

  public void testOP5() throws Exception
  {
    runOPSearch("op05.wmod");
  }

  public void testOP6() throws Exception
  {
    runOPSearch("op06.wmod");
  }

  public void testOP7() throws Exception
  {
    runOPSearch("op07.wmod");
  }

  public void testOP8() throws Exception
  {
    runOPSearch("op08.wmod");
  }

  public void testOP9() throws Exception
  {
    runOPSearch("op09.wmod");
  }

  public void testOP10() throws Exception
  {
    runOPSearch("op10.wmod");
  }

  public void testOP11() throws Exception
  {
    runOPSearch("op11.wmod");
  }

  public void testOP12() throws Exception
  {
    runOPSearch("op12.wmod");
  }

  public void testOP13() throws Exception
  {
    runOPSearch("op13.wmod");
  }

  public void testOP14() throws Exception
  {
    runOPSearch("op14.wmod");
  }

  public void testOP15() throws Exception
  {
    runOPSearch("op15.wmod");
  }

  public void testOP15a() throws Exception
  {
    runOPSearch("op15a.wmod");
  }

  public void testOP16() throws Exception
  {
    runOPSearch("op16.wmod");
  }

  public void testOP17() throws Exception
  {
    runOPSearch("op17.wmod");
  }

  public void testOP18() throws Exception
  {
    runOPSearch("op18.wmod");
  }

  public void testOP19() throws Exception
  {
    runOPSearch("op19.wmod");
  }

  public void testOP20() throws Exception
  {
    runOPSearch("op20.wmod");
  }

  public void testOP21() throws Exception
  {
    runOPSearch("op21.wmod");
  }


  //#########################################################################
  //# Auxiliary Methods
  private void runOPSearch(final String name)
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
