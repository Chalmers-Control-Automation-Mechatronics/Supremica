//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   AbstractAbstractionRuleTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.IsomorphismChecker;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


/**
 * @author Robi Malik
 */

public abstract class AbstractAbstractionRuleTest extends AbstractAnalysisTest
{

  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
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
    mIsomorphismChecker = new IsomorphismChecker(factory, false);
  }

  protected void tearDown()
    throws Exception
  {
    mAbstractionRule = null;
    mIsomorphismChecker = null;
    mBindings = null;
    super.tearDown();
  }


  //#########################################################################
  //# Instantiating and Checking Modules
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


  //#########################################################################
  //# Checking Instantiated Product DES problems
  protected void runAbstractionRule(final String group, final String name)
    throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runAbstractionRule(groupdir, name);
  }

  protected void runAbstractionRule(final String group,
                                    final String subdir,
                                    final String name)
    throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runAbstractionRule(groupdir, subdir, name);
  }

  protected void runAbstractionRule(final File groupdir,
                                    final String subdir,
                                    final String name)
    throws Exception
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

  protected void runAbstractionRule(final File filename)
    throws Exception
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


  //#########################################################################
  //# Auxiliary Methods
  private void runAbstractionRule(final ProductDESProxy des)
      throws Exception
  {
    getLogger().info("Checking " + des.getName() + " ...");
    configureAbstractionRule(des);
    final EventProxy tau = getEvent(des, TAU);
    final AutomatonProxy before = findAutomaton(des, BEFORE);
    final AutomatonProxy result = mAbstractionRule.applyRule(before, tau);
    checkResult(des, before, result);
    getLogger().info("Done " + des.getName());
  }

  private void checkResult(final ProductDESProxy des,
                           final AutomatonProxy before,
                           final AutomatonProxy result)
    throws WatersMarshalException, IOException, AnalysisException
  {
    final String name = des.getName();
    final String basename = appendSuffixes(name, mBindings);
    final String comment = "Test output from " +
      ProxyTools.getShortClassName(mAbstractionRule) + '.';
    saveAutomaton(result, basename, comment);
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
  //# To be Provided by Subclasses
  /**
   * Creates an instance of the abstraction rule test. This method
   * instantiates the class of the abstraction rule tested by the particular
   * subclass of this test, and configures it as needed.
   * @param factory
   *          The factory used by the abstraction rule to create its output.
   * @return An instance of the abstraction rule.
   */
  protected abstract AbstractionRule createAbstractionRule
    (ProductDESProxyFactory factory);

  /**
   * Configures the automaton builder under test for a given product DES. This
   * method is called just before the automaton builder is started for each
   * model to be tested. Subclasses that override this method should call the
   * superclass method first.
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


  //#########################################################################
  //# Data Members
  private AbstractionRule mAbstractionRule;
  private IsomorphismChecker mIsomorphismChecker;
  private List<ParameterBindingProxy> mBindings;


  //#########################################################################
  //# Class Constants
  final String ALPHA = ":alpha";
  final String OMEGA = EventDeclProxy.DEFAULT_MARKING_NAME;
  final String TAU = "tau";

  private final String BEFORE = "before";
  private final String AFTER = "after";

}
