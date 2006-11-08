//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractModelVerifierTest
//###########################################################################
//# $Id: AbstractModelVerifierTest.java,v 1.5 2006-11-08 22:55:25 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.File;
import java.util.List;

import net.sourceforge.waters.junit.AbstractWatersTest;
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
    
  protected void runModelVerifier(final File filename,
                        final List<ParameterBindingProxy> bindings,
                        final boolean expect)
    throws Exception
  {
    final ModuleProxy module = (ModuleProxy) mDocumentManager.load(filename);
    final ModuleCompiler compiler =
      new ModuleCompiler(mDocumentManager, mProductDESProxyFactory, module);
    final ProductDESProxy des = compiler.compile(bindings);
    runModelVerifier(des, bindings, expect);
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
    final ProductDESProxy des =
      (ProductDESProxy) mDocumentManager.load(filename);
    runModelVerifier(des, expect);
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
