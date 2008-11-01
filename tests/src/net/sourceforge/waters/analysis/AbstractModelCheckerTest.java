//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   AbstractModelCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis;

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


public abstract class AbstractModelCheckerTest extends AbstractWatersTest
{

  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  public void setUp() throws Exception
  {
    super.setUp();   
    mProductDESProxyFactory = ProductDESElementFactory.getInstance();
    mTraceMarshaller = new JAXBTraceMarshaller(mProductDESProxyFactory);
    mProductDESMarshaller = new JAXBProductDESMarshaller(mProductDESProxyFactory);
    mModuleFactory = ModuleElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    final JAXBModuleMarshaller modmarshaller =
      new JAXBModuleMarshaller(mModuleFactory, optable);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(modmarshaller);
  } 


  //#########################################################################
  //# Instantiating and Checking Modules
  void runModelChecker(final String group,
                       final String name,
                       final List<ParameterBindingProxy> bindings,
                       final boolean expect)
    throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runModelChecker(groupdir, name, bindings, expect);
  }

  void runModelChecker(final String group,
                       final String subdir,
                       final String name,
                       final List<ParameterBindingProxy> bindings,
                       final boolean expect)
    throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runModelChecker(groupdir, subdir, name, bindings, expect);
  }

  void runModelChecker(final File groupdir,
                       final String subdir,
                       final String name,
                       final List<ParameterBindingProxy> bindings,
                       final boolean expect)
    throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runModelChecker(dir, name, bindings, expect);
  }

  void runModelChecker(final File dir,
                       final String name,
                       final List<ParameterBindingProxy> bindings,
                       final boolean expect)
    throws Exception
  {
    final File filename = new File(dir, name);
    runModelChecker(filename, bindings, expect);
  }


  //#########################################################################
  //# Checking Instantiated Product DES problems
  void runModelChecker(final String group,
                       final String name,
                       final boolean expect)
    throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runModelChecker(groupdir, name, expect);
  }

  void runModelChecker(final String group,
                       final String subdir,
                       final String name,
                       final boolean expect)
    throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runModelChecker(groupdir, subdir, name, expect);
  }

  void runModelChecker(final File groupdir,
                       final String subdir,
                       final String name,
                       final boolean expect)
    throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runModelChecker(dir, name, expect);
  }

  void runModelChecker(final File dir,
                       final String name,
                       final boolean expect)
    throws Exception
  {
    final File filename = new File(dir, name);
    runModelChecker(filename, expect);
  }

  void runModelChecker(final File filename, final boolean expect)
    throws Exception
  {
    runModelChecker(filename, (List<ParameterBindingProxy>) null, expect);
  }

  void runModelChecker(final File filename,
                       final List<ParameterBindingProxy> bindings,
                       final boolean expect)
    throws Exception
  {
    final ProductDESProxy des = getCompiledDES(filename, bindings);
    runModelChecker(des, bindings, expect);
  }

  void runModelChecker(final ProductDESProxy des, final boolean expect)
    throws Exception
  {
    runModelChecker(des, null, expect);
  }

  void runModelChecker(final ProductDESProxy des,
                       final List<ParameterBindingProxy> bindings,
                       final boolean expect)
    throws Exception
  {
    final ModelChecker checker =
      createModelChecker(des, mProductDESProxyFactory);
    final boolean result = checker.run();
    TraceProxy counterexample = null;
    if (!result) {
      counterexample = checker.getCounterExample();
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
  //# Compiling
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
  abstract ModelChecker createModelChecker(ProductDESProxy des,
                                           ProductDESProxyFactory factory);

  abstract void checkCounterExample(ProductDESProxy des,
                                    TraceProxy counterexample);


  //#########################################################################
  //# Accessing the Factories
  ProductDESProxyFactory getProductDESProxyFactory()
  {
    return mProductDESProxyFactory;
  }

  ParameterBindingProxy createBinding(final String name, final int value)
  {
    final IntConstantProxy expr = mModuleFactory.createIntConstantProxy(value);
    return mModuleFactory.createParameterBindingProxy(name, expr);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void saveCounterExample(final TraceProxy counterexample,
                                  final List<ParameterBindingProxy> bindings)
    throws Exception
  {
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


  //#########################################################################
  //# Data Members
  private ProductDESProxyFactory mProductDESProxyFactory;
  private ModuleProxyFactory mModuleFactory;
  private JAXBProductDESMarshaller mProductDESMarshaller;
  private JAXBTraceMarshaller mTraceMarshaller;
  private DocumentManager mDocumentManager;	

}
