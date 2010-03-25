//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractAutomatonBuilderTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
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

  protected void setUp() throws Exception
  {
    super.setUp();
    mFactory = getProductDESProxyFactory();
    mProductDESMarshaller = new JAXBProductDESMarshaller(mFactory);
    mAutomatonBuilder = createAutomatonBuilder(mFactory);
    mIsomorphismChecker = new IsomorphismChecker(mFactory, true);
    setNodeLimit();
  }

  protected void tearDown()
    throws Exception
  {
    mFactory = null;
    mProductDESMarshaller = null;
    mAutomatonBuilder = null;
    mIsomorphismChecker = null;
    mBindings = null;
    super.tearDown();
  }


  //#########################################################################
  //# Instantiating and Checking Modules
  protected void runAutomatonBuilder(final String group, final String name,
                                     final List<ParameterBindingProxy> bindings)
    throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runAutomatonBuilder(groupdir, name, bindings);
  }

  protected void runAutomatonBuilder(final String group, final String subdir,
                                     final String name,
                                     final List<ParameterBindingProxy> bindings)
    throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runAutomatonBuilder(groupdir, subdir, name, bindings);
  }

  protected void runAutomatonBuilder(final File groupdir, final String subdir,
                                     final String name,
                                     final List<ParameterBindingProxy> bindings)
    throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runAutomatonBuilder(dir, name, bindings);
  }

  protected void runAutomatonBuilder(final File dir, final String name,
                                     final List<ParameterBindingProxy> bindings)
    throws Exception
  {
    final String ext = getTestExtension();
    final File filename = new File(dir, name + ext);
    final String expectedName = getExpectedName(name);
    final String expectedSuffixed =
      appendSuffixes(expectedName, bindings, ext);
    final File expectedFile = new File(dir, expectedSuffixed);
    runAutomatonBuilder(filename, bindings, expectedFile);
  }


  //#########################################################################
  //# Checking Instantiated Product DES problems
  protected void runAutomatonBuilder(final String group,
                                     final String name)
    throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runAutomatonBuilder(groupdir, name);
  }

  protected void runAutomatonBuilder(final String group,
                                     final String subdir,
                                     final String name)
      throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runAutomatonBuilder(groupdir, subdir, name);
  }

  protected void runAutomatonBuilder(final File groupdir,
                                     final String subdir,
                                     final String name)
      throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runAutomatonBuilder(dir, name);
  }

  protected void runAutomatonBuilder(final File dir,
                                     final String name)
    throws Exception
  {
    final String ext = getTestExtension();
    final File filename = new File(dir, name + ext);
    final String expectedName = getExpectedName(name);
    final File expectedFile = new File(dir, expectedName + ext);
    runAutomatonBuilder(filename, expectedFile);
  }

  protected void runAutomatonBuilder(final File filename,
                                     final File expectedFile)
    throws Exception
  {
    runAutomatonBuilder(filename,
                        (List<ParameterBindingProxy>) null,
                        expectedFile);
  }

  protected void runAutomatonBuilder(final File filename,
                                     final List<ParameterBindingProxy> bindings,
                                     final File expectedFile)
    throws Exception
  {
    mBindings = bindings;
    final ProductDESProxy des = getCompiledDES(filename, bindings);
    final ProductDESProxy expect = getCompiledDES(expectedFile);
    runAutomatonBuilderWithBindings(des, expect);
  }

  protected void runAutomatonBuilder(final ProductDESProxy des,
                                     final ProductDESProxy expect)
    throws Exception
  {
    runAutomatonBuilder(des, null, expect);
  }

  protected void runAutomatonBuilder(final ProductDESProxy des,
                                     final List<ParameterBindingProxy> bindings,
                                     final ProductDESProxy expect)
    throws Exception
  {
    mBindings = bindings;
    runAutomatonBuilderWithBindings(des, expect);
  }

  protected AutomatonBuilder getAutomatonBuilder()
  {
    return mAutomatonBuilder;
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
  {
    mAutomatonBuilder.setModel(des);
  }

  /**
   * Returns the name of the file that contains the expected result
   * for a given test.
   * @param desname
   *          The base bane of the file containing the test data,
   *          without path and extension.
   * @return  The base name of the file containing the expected result
   *          for the test, without path and extension.
   */
  protected abstract String getExpectedName(final String desname);

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

  private void checkResult(final ProductDESProxy des,
                           final AutomatonProxy result,
                           final ProductDESProxy expectedDES)
    throws WatersMarshalException, IOException, AnalysisException
  {
    final String name = des.getName();
    final String comment = "Test output from " +
      ProxyTools.getShortClassName(mAutomatonBuilder);
    final Collection<EventProxy> events = result.getEvents();
    final Collection<AutomatonProxy> resultAutomata =
      Collections.singletonList(result);
    final ProductDESProxy resultDES = mFactory.createProductDESProxy
      (name, comment, null, events, resultAutomata);
    saveDES(resultDES);
    final Collection<AutomatonProxy> expectedAutomata =
      expectedDES.getAutomata();
    final AutomatonProxy expected = expectedAutomata.iterator().next();
    mIsomorphismChecker.checkIsomorphism(result, expected);
  }

  private File saveDES(final ProductDESProxy des)
    throws WatersMarshalException, IOException
  {
    assertNotNull(des);
    final String name = des.getName();
    final String ext = mProductDESMarshaller.getDefaultExtension();
    final String extname = appendSuffixes(name, mBindings, ext);
    assertTrue("File name '" + extname + "' contains colon, " +
               "which does not work on all platforms!",
               extname.indexOf(':') < 0);
    final File dir = getOutputDirectory();
    final File filename = new File(dir, extname);
    ensureParentDirectoryExists(filename);
    mProductDESMarshaller.marshal(des, filename);
    return filename;
  }

  private String appendSuffixes(final String name,
                                final List<ParameterBindingProxy> bindings,
                                final String ext)
  {
    final StringBuffer buffer = new StringBuffer(name);
    if (bindings != null) {
      for (final ParameterBindingProxy binding : bindings) {
        buffer.append('-');
        buffer.append(binding.getExpression().toString());
      }
    }
    buffer.append(ext);
    return buffer.toString();
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
  private ProductDESProxyFactory mFactory;
  private JAXBProductDESMarshaller mProductDESMarshaller;
  private AutomatonBuilder mAutomatonBuilder;
  private IsomorphismChecker mIsomorphismChecker;
  private List<ParameterBindingProxy> mBindings;

}
