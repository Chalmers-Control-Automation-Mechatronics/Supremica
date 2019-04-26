//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.analysis.gnonblocking;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.des.IsomorphismChecker;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESIntegrityChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


/**
 * @author Robi Malik
 */

public abstract class AbstractAbstractionRuleTest extends AbstractAnalysisTest
{

  // #########################################################################
  // # Overrides for base class junit.framework.TestCase
  public AbstractAbstractionRuleTest()
  {
  }

  public AbstractAbstractionRuleTest(final String name)
  {
    super(name);
  }

  protected void setUp() throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mAbstractionRule = createAbstractionRule(factory);
    mIntegrityChecker = ProductDESIntegrityChecker.getInstance();
    mIsomorphismChecker = new IsomorphismChecker(factory, false, true);
  }

  protected void tearDown() throws Exception
  {
    mAbstractionRule = null;
    mIntegrityChecker = null;
    mIsomorphismChecker = null;
    mBindings = null;
    super.tearDown();
  }

  // #########################################################################
  // # Instantiating and Checking Modules
  protected void runAbstractionRule(final String group, final String name,
                                    final List<ParameterBindingProxy> bindings)
      throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runAbstractionRule(groupdir, name, bindings);
  }

  protected void runAbstractionRule(final String group, final String subdir,
                                    final String name,
                                    final List<ParameterBindingProxy> bindings)
      throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runAbstractionRule(groupdir, subdir, name, bindings);
  }

  protected void runAbstractionRule(final File groupdir, final String subdir,
                                    final String name,
                                    final List<ParameterBindingProxy> bindings)
      throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runAbstractionRule(dir, name, bindings);
  }

  protected void runAbstractionRule(final File dir, final String name,
                                    final List<ParameterBindingProxy> bindings)
      throws Exception
  {
    final File filename = new File(dir, name);
    runAbstractionRule(filename, bindings);
  }

  // #########################################################################
  // # Checking Instantiated Product DES problems
  protected void runAbstractionRule(final String group, final String name)
      throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runAbstractionRule(groupdir, name);
  }

  protected void runAbstractionRule(final String group, final String subdir,
                                    final String name) throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runAbstractionRule(groupdir, subdir, name);
  }

  protected void runAbstractionRule(final File groupdir, final String subdir,
                                    final String name) throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runAbstractionRule(dir, name);
  }

  protected void runAbstractionRule(final File dir, final String name)
      throws Exception
  {
    final File filename = new File(dir, name);
    runAbstractionRule(filename);
  }

  protected void runAbstractionRule(final File filename) throws Exception
  {
    runAbstractionRule(filename, (List<ParameterBindingProxy>) null);
  }

  protected void runAbstractionRule(final File filename,
                                    final List<ParameterBindingProxy> bindings)
      throws Exception
  {
    mBindings = bindings;
    final ProductDESProxy des = getCompiledDES(filename, bindings);
    runAbstractionRule(des);
  }

  // #########################################################################
  // # Auxiliary Methods
  private void runAbstractionRule(final ProductDESProxy des) throws Exception
  {
    getLogger().info("Checking " + des.getName() + " ...");
    configureAbstractionRule(des);
    final EventProxy tau = getEvent(des, TAU);
    final AutomatonProxy before = findAutomaton(des, BEFORE);
    final AutomatonProxy result = mAbstractionRule.applyRuleToAutomaton(before, tau);
    checkResult(des, before, result);
    getLogger().info("Done " + des.getName());
  }

  private void checkResult(final ProductDESProxy des,
                           final AutomatonProxy before,
                           final AutomatonProxy result)
      throws Exception
  {
    final String name = des.getName();
    final String basename = appendSuffixes(name, mBindings);
    final String comment =
        "Test output from " + ProxyTools.getShortClassName(mAbstractionRule)
            + '.';
    saveAutomaton(result, basename, comment);
    mIntegrityChecker.check(result, des);
    final AutomatonProxy expected = getAutomaton(des, AFTER);
    if (expected == null) {
      assertSame("Test expects no change, "
          + "but the object returned is not the same as the input!", before,
                 result);
    } else {
      mIsomorphismChecker.checkIsomorphism(result, expected);
    }
  }

  // #########################################################################
  // # To be Provided by Subclasses
  /**
   * Creates an instance of the abstraction rule test. This method instantiates
   * the class of the abstraction rule tested by the particular subclass of this
   * test, and configures it as needed.
   *
   * @param factory
   *          The factory used by the abstraction rule to create its output.
   * @return An instance of the abstraction rule.
   */
  protected abstract AbstractionRule createAbstractionRule(
                                                           ProductDESProxyFactory factory);

  /**
   * Configures the automaton builder under test for a given product DES. This
   * method is called just before the automaton builder is started for each
   * model to be tested. Subclasses that override this method should call the
   * superclass method first.
   *
   * @param des
   *          The model to be analysed for the current test case.
   */
  protected void configureAbstractionRule(final ProductDESProxy des)
  {
    final List<EventProxy> props = new ArrayList<EventProxy>(2);
    final EventProxy alpha = getEvent(des, ALPHA);
    if (alpha != null) {
      props.add(alpha);
    }
    final EventProxy omega = getEvent(des, OMEGA);
    if (omega != null) {
      props.add(omega);
    }
    mAbstractionRule.setPropositions(props);
  }

  /**
   * Retrieves the abstraction rule used by this test.
   */
  protected AbstractionRule getAbstractionRule()
  {
    return mAbstractionRule;
  }

  // #########################################################################
  // # Overrides for Abstract Base Class
  // # net.sourceforge.waters.model.analysis.AbstractAnalysisTest
  protected void configure(final ModuleCompiler compiler)
  {
    compiler.setOptimizationEnabled(false);
  }

  // #########################################################################
  // # Data Members
  private AbstractionRule mAbstractionRule;
  private ProductDESIntegrityChecker mIntegrityChecker;
  private IsomorphismChecker mIsomorphismChecker;
  private List<ParameterBindingProxy> mBindings;

  // #########################################################################
  // # Class Constants
  final String ALPHA = ":alpha";
  final String OMEGA = EventDeclProxy.DEFAULT_MARKING_NAME;
  final String TAU = "tau";

  private final String BEFORE = "before";
  private final String AFTER = "after";

}
