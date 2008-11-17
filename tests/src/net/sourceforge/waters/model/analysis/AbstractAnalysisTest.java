//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractAnalysisTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.File;
import java.util.List;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public abstract class AbstractAnalysisTest extends AbstractWatersTest
{

  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  public AbstractAnalysisTest()
  {
  }

  public AbstractAnalysisTest(final String name)
  {
    super(name);
  }

  protected void setUp() throws Exception
  {
    super.setUp();
    mProductDESProxyFactory = ProductDESElementFactory.getInstance();
    mProductDESMarshaller =
      new JAXBProductDESMarshaller(mProductDESProxyFactory);
    mModuleProxyFactory = ModuleElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    final JAXBModuleMarshaller modmarshaller =
      new JAXBModuleMarshaller(mModuleProxyFactory, optable);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(modmarshaller);
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
      configure(compiler);
      return compiler.compile(bindings);
    } else {
      fail("Unknown document type " + doc.getClass().getName() + "!");
      return null;
    }
  }

  protected void configure(final ModuleCompiler compiler)
  {
  }

  protected DocumentManager getDocumentManager()
  {
    return mDocumentManager;
  }


  //#########################################################################
  //# Utilities
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

  protected EventProxy findEvent(final ProductDESProxy des,
                                 final String name)
    throws NameNotFoundException
  {
    for (final EventProxy event : des.getEvents()) {
      if (event.getName().equals(name)) {
        return event;
      }
    }
    throw new NameNotFoundException
      ("DES '" + des.getName() + "' does not have any event named '" +
       name + "'!");
  }

  protected AutomatonProxy findAutomaton(final ProductDESProxy des,
                                         final String name)
    throws NameNotFoundException
  {
    for (final AutomatonProxy automaton : des.getAutomata()) {
      if (automaton.getName().equals(name)) {
        return automaton;
      }
    }
    throw new NameNotFoundException
      ("DES '" + des.getName() + "' does not have any automaton named '" +
       name + "'!");
  }


  //#########################################################################
  //# Data Members
  private ProductDESProxyFactory mProductDESProxyFactory;
  private ModuleProxyFactory mModuleProxyFactory;
  private JAXBProductDESMarshaller mProductDESMarshaller;
  private DocumentManager mDocumentManager;	

}
