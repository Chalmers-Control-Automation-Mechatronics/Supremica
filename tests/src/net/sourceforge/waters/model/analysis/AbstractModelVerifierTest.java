//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractModelVerifierTest
//###########################################################################
//# $Id: AbstractModelVerifierTest.java,v 1.9 2006-11-15 23:31:59 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.File;
import java.util.List;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBTraceMarshaller;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public abstract class AbstractModelVerifierTest extends AbstractWatersTest
{

  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  public void setUp() throws Exception
  {
    super.setUp();
    mProductDESProxyFactory = ProductDESElementFactory.getInstance();
    mTraceMarshaller = new JAXBTraceMarshaller(mProductDESProxyFactory);
    mProductDESMarshaller =
      new JAXBProductDESMarshaller(mProductDESProxyFactory);
    mModuleProxyFactory = ModuleElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    final JAXBModuleMarshaller modmarshaller =
      new JAXBModuleMarshaller(mModuleProxyFactory, optable);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(modmarshaller);
    mModelVerifier = createModelVerifier(mProductDESProxyFactory);
    setStateLimit();
  }


  //#########################################################################
  //# Instantiating and Checking Modules
  protected void runModelVerifier(final String group,
                                  final String name,
                                  final List<ParameterBindingProxy> bindings,
                                  final boolean expect)
    throws Exception
  {
    final File rootdir = getInputRoot();
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
    final File rootdir = getInputRoot();
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
    final File rootdir = getInputRoot();
    final File groupdir = new File(rootdir, group);
    runModelVerifier(groupdir, name, expect);
  }

  protected void runModelVerifier(final String group,
                                  final String subdir,
                                  final String name,
                                  final boolean expect)
    throws Exception
  {
    final File rootdir = getInputRoot();
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
                 result, expect);
    if (!expect) {
      checkCounterExample(des, counterexample);
    }
  }

  protected ModelVerifier getModelVerifier()
  {
    return mModelVerifier;
  }


  //#########################################################################
  //# Compiling
  protected ProductDESProxy getCompiledDES(final File filename)
    throws Exception
  {
    return getCompiledDES(filename, null);
  }

  protected ProductDESProxy getCompiledDES
    (final File filename,
     final List<ParameterBindingProxy> bindings)
    throws Exception
  {
    final DocumentProxy doc = mDocumentManager.load(filename);
    if (doc instanceof ProductDESProxy) {
      assertTrue("Can't apply bindings to ProductDES!",
                 bindings == null || bindings.isEmpty());
      return (ProductDESProxy) doc;
    } else if (doc instanceof ModuleProxy) {
      final ModuleProxy module = (ModuleProxy) doc;
      final ModuleCompiler compiler =
        new ModuleCompiler(mDocumentManager, mProductDESProxyFactory, module);
      return compiler.compile(bindings);
    } else {
      fail("Unknown document type " + doc.getClass().getName() + "!");
      return null;
    }
  }

                                           
  //#########################################################################
  //# To be Provided by Subclasses
  protected abstract ModelVerifier
    createModelVerifier(ProductDESProxyFactory factory);

  protected abstract void
    checkCounterExample(ProductDESProxy des,
                        TraceProxy counterexample);


  //#########################################################################
  //# Accessing the Factories
  protected ProductDESProxyFactory getProductDESProxyFactory()
  {
    return mProductDESProxyFactory;
  }

  protected ParameterBindingProxy createBinding(final String name,
                                                final int value)
  {
    final IntConstantProxy expr =
      mModuleProxyFactory.createIntConstantProxy(value);
    return mModuleProxyFactory.createParameterBindingProxy(name, expr);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setStateLimit()
  {
    final String prop = System.getProperty("waters.analysis.statelimit");
    if (prop != null) {
      final int limit = Integer.parseInt(prop);
      mModelVerifier.setStateLimit(limit);
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
  private ProductDESProxyFactory mProductDESProxyFactory;
  private ModuleProxyFactory mModuleProxyFactory;
  private JAXBProductDESMarshaller mProductDESMarshaller;
  private JAXBTraceMarshaller mTraceMarshaller;
  private DocumentManager mDocumentManager;	
  private ModelVerifier mModelVerifier;

}
