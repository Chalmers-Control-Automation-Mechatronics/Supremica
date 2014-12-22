//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractAutomatonBuilderTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.analysis.des.AutomatonBuilder;
import net.sourceforge.waters.model.analysis.des.IsomorphismChecker;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


public abstract class AbstractAutomatonBuilderTest
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  public AbstractAutomatonBuilderTest()
  {
  }

  public AbstractAutomatonBuilderTest(final String name)
  {
    super(name);
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mAutomatonBuilder = createAutomatonBuilder(factory);
    mIsomorphismChecker = new IsomorphismChecker(factory, true, true);
    setNodeLimit();
  }

  @Override
  protected void tearDown()
    throws Exception
  {
    mAutomatonBuilder = null;
    mIsomorphismChecker = null;
    mBindings = null;
    super.tearDown();
  }


  //#########################################################################
  //# Invocation
  protected void runAutomatonBuilder(final String... path)
    throws Exception
  {
    runAutomatonBuilder(null, path);
  }

  protected void runAutomatonBuilder(final List<ParameterBindingProxy> bindings,
                                     final String... path)
    throws Exception
  {
    final ProductDESProxy des = getCompiledDES(bindings, path);
    final int last = path.length - 1;
    path[last] = getExpectedName(path[last], bindings);
    final ProductDESProxy expect = getCompiledDES(bindings, path);
    runAutomatonBuilderWithBindings(des, expect);
  }


  //#########################################################################
  //# Hooks
  protected AutomatonBuilder getAutomatonBuilder()
  {
    return mAutomatonBuilder;
  }

  protected IsomorphismChecker getIsomorphismChecker()
  {
    return mIsomorphismChecker;
  }

  protected void configureAutomatonBuilder(final AutomatonProxy aut)
    throws AnalysisException
  {
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final ProductDESProxy des =
      AutomatonTools.createProductDESProxy(aut, factory);
    configureAutomatonBuilder(des);
  }


  //#########################################################################
  //# To be Provided by Subclasses
  /**
   * Creates an instance of the automaton builder under test. This method
   * instantiates the class of the automaton builder tested by the particular
   * subclass of this test, and configures it as needed.
   * @param factory
   *          The factory used by the model analyser to create its output.
   * @return An instance of the model verifier
   */
  protected abstract AutomatonBuilder createAutomatonBuilder
    (ProductDESProxyFactory factory);

  /**
   * Configures the automaton builder under test for a given product DES. This
   * method is called just before the automaton builder is started for each
   * model to be tested. Subclasses that override this method should call the
   * superclass method first.
   * @param des
   *          The model to be analysed for the current test case.
   */
  protected void configureAutomatonBuilder(final ProductDESProxy des)
    throws AnalysisException
  {
    mAutomatonBuilder.setModel(des);
  }

  /**
   * Returns the name of the file that contains the expected result
   * for a given test.
   * @param  desname
   *           The name of the file containing the test data,
   *           with or without path.
   * @param  bindings
   *           Parameter bindings used to instantiate the module.
   * @return The base name of the file containing the expected result
   *         for the test, without path and extension.
   */
  protected abstract String getExpectedName
    (final String desname,
     final List<ParameterBindingProxy> bindings);

  /**
   * Returns the extension used for all test files
   */
  protected String getTestExtension()
  {
    return ".wmod";
  }


  //#########################################################################
  //# Auxiliary Methods
  protected void runAutomatonBuilderWithBindings(final ProductDESProxy des,
                                                 final ProductDESProxy expect)
      throws Exception
  {
    getLogger().info("Checking " + des.getName() + " ...");
    configureAutomatonBuilder(des);
    mAutomatonBuilder.run();
    final AutomatonProxy result = mAutomatonBuilder.getComputedAutomaton();
    checkResult(des, result, expect);
    getLogger().info("Done " + des.getName());
  }

  protected void checkResult(final ProductDESProxy des,
                             final AutomatonProxy result,
                             final ProductDESProxy expectedDES)
    throws Exception
  {
    final String name = des.getName();
    final String basename = appendSuffixes(name, mBindings);
    final String comment = "Test output from " +
      ProxyTools.getShortClassName(mAutomatonBuilder) + '.';
    saveAutomaton(result, basename, comment);
    final Collection<AutomatonProxy> expectedAutomata =
      expectedDES.getAutomata();
    final AutomatonProxy expected = expectedAutomata.iterator().next();
    mIsomorphismChecker.checkIsomorphism(result, expected);
  }

  private void setNodeLimit()
  {
    final String prop = System.getProperty("waters.analysis.statelimit");
    if (prop != null) {
      final int limit = Integer.parseInt(prop);
      mAutomatonBuilder.setNodeLimit(limit);
    }
  }


  //#########################################################################
  //# Data Members
  private AutomatonBuilder mAutomatonBuilder;
  private IsomorphismChecker mIsomorphismChecker;
  private List<ParameterBindingProxy> mBindings;

}
