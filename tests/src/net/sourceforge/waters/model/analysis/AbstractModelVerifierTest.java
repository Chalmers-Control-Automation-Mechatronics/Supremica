//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractModelVerifierTest
//###########################################################################
//# $Id: AbstractModelVerifierTest.java,v 1.15 2008-02-14 02:24:09 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.File;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.marshaller.JAXBTraceMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


public abstract class AbstractModelVerifierTest extends AbstractAnalysisTest
{

  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  public AbstractModelVerifierTest()
  {
  }

  public AbstractModelVerifierTest(final String name)
  {
    super(name);
  }

  protected void setUp() throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mTraceMarshaller = new JAXBTraceMarshaller(factory);
    mModelVerifier = createModelVerifier(factory);
    setNodeLimit();
  }


  //#########################################################################
  //# Instantiating and Checking Modules
  protected void runModelVerifier(final String group,
                                  final String name,
                                  final List<ParameterBindingProxy> bindings,
                                  final boolean expect)
    throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runModelVerifier(groupdir, name, bindings, expect);
  }

  protected void runModelVerifier(final String group,
                                  final String subdir,
                                  final String name,
                                  final List<ParameterBindingProxy> bindings,
                                  final boolean expect)
    throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runModelVerifier(groupdir, subdir, name, bindings, expect);
  }

  protected void runModelVerifier(final File groupdir,
                                  final String subdir,
                                  final String name,
                                  final List<ParameterBindingProxy> bindings,
                                  final boolean expect)
    throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runModelVerifier(dir, name, bindings, expect);
  }

  protected void runModelVerifier(final File dir,
                                  final String name,
                                  final List<ParameterBindingProxy> bindings,
                                  final boolean expect)
    throws Exception
  {
    final File filename = new File(dir, name);
    runModelVerifier(filename, bindings, expect);
  }


  //#########################################################################
  //# Checking Instantiated Product DES problems
  protected void runModelVerifier(final String group,
                                  final String name,
                                  final boolean expect)
    throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runModelVerifier(groupdir, name, expect);
  }

  protected void runModelVerifier(final String group,
                                  final String subdir,
                                  final String name,
                                  final boolean expect)
    throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runModelVerifier(groupdir, subdir, name, expect);
  }

  protected void runModelVerifier(final File groupdir,
                                  final String subdir,
                                  final String name,
                                  final boolean expect)
    throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runModelVerifier(dir, name, expect);
  }

  protected void runModelVerifier(final File dir,
                                  final String name,
                                  final boolean expect)
    throws Exception
  {
    final File filename = new File(dir, name);
    runModelVerifier(filename, expect);
  }

  protected void runModelVerifier(final File filename, final boolean expect)
    throws Exception
  {
    runModelVerifier(filename, (List<ParameterBindingProxy>) null, expect);
  }

  protected void runModelVerifier(final File filename,
                                  final List<ParameterBindingProxy> bindings,
                                  final boolean expect)
    throws Exception
  {
    final ProductDESProxy des = getCompiledDES(filename, bindings);
    runModelVerifier(des, bindings, expect);
  }

  protected void runModelVerifier(final ProductDESProxy des,
                                  final boolean expect)
    throws Exception
  {
    runModelVerifier(des, null, expect);
  }

  protected void runModelVerifier(final ProductDESProxy des,
                                  final List<ParameterBindingProxy> bindings,
                                  final boolean expect)
    throws Exception
  {
    mModelVerifier.setModel(des);
    final boolean result = mModelVerifier.run();
    TraceProxy counterexample = null;
    if (!result) {
      counterexample = mModelVerifier.getCounterExample();
      saveCounterExample(counterexample, bindings);
    }
    assertEquals("Wrong result from model checker: got " +
                 result + " but should have been " + expect + "!",
                 expect, result);
    if (!expect) {
      checkCounterExample(des, counterexample);
    }
  }

  protected ModelVerifier getModelVerifier()
  {
    return mModelVerifier;
  }


  //#########################################################################
  //# To be Overridden by Subclasses
  protected void checkCounterExample(final ProductDESProxy des,
                                     final TraceProxy trace)
  {
    assertNotNull("Counterexample is NULL!", trace);
    assertSame("Product DES in trace is not the original model!",
               des, trace.getProductDES());
    final Collection<AutomatonProxy> automata = des.getAutomata();
    for (final AutomatonProxy aut : trace.getAutomata()) {
      if (!automata.contains(aut)) {
        fail("Trace automaton '" + aut.getName() +
             "' does not match any in product DES!");
      }
    }
    final Collection<EventProxy> events = des.getEvents();
    for (final EventProxy event : trace.getEvents()) {
      if (!events.contains(event)) {
        fail("Trace event '" + event.getName() +
             "' does not match any in product DES!");
      }
    }
  }


  //#########################################################################
  //# To be Provided by Subclasses
  protected abstract ModelVerifier
    createModelVerifier(ProductDESProxyFactory factory);


  //#########################################################################
  //# Auxiliary Methods
  private void setNodeLimit()
  {
    final String prop = System.getProperty("waters.analysis.statelimit");
    if (prop != null) {
      final int limit = Integer.parseInt(prop);
      mModelVerifier.setNodeLimit(limit);
    }
  }

  private void saveCounterExample(final TraceProxy counterexample,
                                  final List<ParameterBindingProxy> bindings)
    throws Exception
  {
    if (counterexample == null) {
      System.err.println("WARNING: Got NULL counterexample");
    } else {
      final String name = counterexample.getName();
      final String ext = mTraceMarshaller.getDefaultExtension();
      final StringBuffer buffer = new StringBuffer(name);
      if (bindings != null) {
        for (final ParameterBindingProxy binding : bindings) {
          buffer.append('-');
          buffer.append(binding.getExpression().toString());
        }
      }
      buffer.append(ext);
      final String extname = buffer.toString();
      final File dir = getOutputDirectory();
      final File filename = new File(dir, extname);
      ensureParentDirectoryExists(filename);
      mTraceMarshaller.marshal(counterexample, filename);
    }
  }


  //#########################################################################
  //# Data Members
  private JAXBTraceMarshaller mTraceMarshaller;
  private ModelVerifier mModelVerifier;

}
