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
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
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
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
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
    mModuleMarshaller =
      new JAXBModuleMarshaller(mModuleProxyFactory, optable, false);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mDocumentManager.registerMarshaller(mProductDESMarshaller);
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mProductDESImporter = new ProductDESImporter(mModuleProxyFactory);
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
    final EventProxy event = getEvent(des, name);
    if (event != null) {
      return event;
    } else {
      throw new NameNotFoundException
        ("DES '" + des.getName() + "' does not have any event named '" +
         name + "'!");
    }
  }

  protected EventProxy getEvent(final ProductDESProxy des, final String name)
  {
    for (final EventProxy event : des.getEvents()) {
      if (event.getName().equals(name)) {
        return event;
      }
    }
    return null;
  }

  protected AutomatonProxy findAutomaton(final ProductDESProxy des,
                                         final String name)
    throws NameNotFoundException
  {
    final AutomatonProxy aut = getAutomaton(des, name);
    if (aut == null) {
      throw new NameNotFoundException
        ("DES '" + des.getName() + "' does not have any automaton named '" +
         name + "'!");
    }
    return aut;
  }

  protected AutomatonProxy getAutomaton(final ProductDESProxy des,
                                        final String name)
  {
    for (final AutomatonProxy automaton : des.getAutomata()) {
      if (automaton.getName().equals(name)) {
        return automaton;
      }
    }
    return null;
  }

  protected String appendSuffixes(final String name,
                                  final List<ParameterBindingProxy> bindings)
  {
    if (bindings == null) {
      return name;
    } else {
      final StringBuffer buffer = new StringBuffer(name);
      for (final ParameterBindingProxy binding : bindings) {
        buffer.append('-');
        buffer.append(binding.getExpression().toString());
      }
      return buffer.toString();
    }
  }

  protected void saveAutomaton(final AutomatonProxy aut,
                               final String basename, final String comment)
    throws WatersMarshalException, IOException
  {
    assertNotNull(aut);
    final Collection<EventProxy> events = aut.getEvents();
    final Collection<AutomatonProxy> automata =
      Collections.singletonList(aut);
    final ProductDESProxy des = mProductDESProxyFactory.createProductDESProxy
      (basename, comment, null, events, automata);
    saveDES(des, basename);
  }

  protected void saveDES(final ProductDESProxy des, final String basename)
    throws WatersMarshalException, IOException
  {
    assertNotNull(des);
    final String desext = mProductDESMarshaller.getDefaultExtension();
    final String desname = basename + desext;
    assertTrue("File name '" + desname + "' contains colon, " +
               "which does not work on all platforms!",
               desname.indexOf(':') < 0);
    final File dir = getOutputDirectory();
    final File desfilename = new File(dir, desname);
    ensureParentDirectoryExists(desfilename);
    mDocumentManager.saveAs(des, desfilename);
    final ModuleProxy module = mProductDESImporter.importModule(des);
    final String modext = mModuleMarshaller.getDefaultExtension();
    final String modname = basename + modext;
    final File modfilename = new File(dir, modname);
    mDocumentManager.saveAs(module, modfilename);
  }


  //#########################################################################
  //# Data Members
  private ProductDESProxyFactory mProductDESProxyFactory;
  private ModuleProxyFactory mModuleProxyFactory;
  private JAXBProductDESMarshaller mProductDESMarshaller;
  private JAXBModuleMarshaller mModuleMarshaller;
  private DocumentManager mDocumentManager;
  private ProductDESImporter mProductDESImporter;

}
