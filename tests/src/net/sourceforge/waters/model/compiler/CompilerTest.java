//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   JAXBTestCase
//###########################################################################
//# $Id: CompilerTest.java,v 1.9 2006-09-11 15:37:39 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.EmptyLabelBlockException;
import net.sourceforge.waters.model.compiler.InstantiationException;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.compiler.NondeterminismException;
import net.sourceforge.waters.model.compiler.UndefinedIdentifierException;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.AliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.ParameterProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

import net.sourceforge.waters.junit.AbstractWatersTest;

import org.xml.sax.SAXException;


public class CompilerTest
  extends AbstractWatersTest
{

  //#########################################################################
  //# Handcrafting Test Cases
  public void testCompile_empty_1()
    throws EvalException
  {
    final String name = "empty";
    final List<ParameterProxy> paramList = Collections.emptyList();
    final List<AliasProxy> aliasList = Collections.emptyList();
    final List<EventDeclProxy> eventList = Collections.emptyList();
    final List<Proxy> proxyList = Collections.emptyList();
    final ModuleProxy module = mModuleFactory.createModuleProxy
      (name, null, paramList, aliasList, eventList, proxyList, proxyList);
    final ProductDESProxy des = compile(module);
    assertTrue("Unexpected name!", des.getName().equals(name));
    assertTrue("Unexpected location!", des.getLocation() == null);
    assertTrue("Unexpected event!", des.getEvents().isEmpty());
    assertTrue("Unexpected automata!", des.getAutomata().isEmpty());
  }

  public void testCompile_empty_2()
    throws EvalException
  {
    final String modname = "almost_empty";
    final String instname = "instance";
    try {
      final SimpleIdentifierProxy ident =
        mModuleFactory.createSimpleIdentifierProxy(instname);
      final List<ParameterBindingProxy> bindingList = Collections.emptyList();
      final InstanceProxy instance = mModuleFactory.createInstanceProxy
        (ident, instname, bindingList);
      final List<ParameterProxy> paramList = Collections.emptyList();
      final List<AliasProxy> aliasList = Collections.emptyList();
      final List<EventDeclProxy> eventList = Collections.emptyList();
      final List<Proxy> proxyList = Collections.emptyList();
      final ModuleProxy module = mModuleFactory.createModuleProxy
        (modname, null,
         paramList, aliasList, eventList, proxyList,
         Collections.singletonList(instance));
      final ProductDESProxy des = compile(module);
      fail("Expected InstantiationException not caught!");
    } catch (final InstantiationException exception) {
      final String culprit = "'" + instname + "'";
      final String msg = exception.getMessage();
      assertTrue("InstantiationException <" + msg +
                 "> does not mention culprit " + culprit + "!",
                 msg.indexOf(culprit) >= 0);
    }
  }


  //#########################################################################
  //# Successful Test Cases
  public void testCompile_buffer_sf1()
    throws IOException, WatersException
  {
    compile("buffer_sf1");
  }

  public void testCompile_buffertest()
    throws IOException, WatersException
  {
    compile("buffertest");
  }

  public void testCompile_colours()
    throws IOException, WatersException
  {
    compile("colours");
  }

  public void testCompile_machine()
    throws IOException, WatersException
  {
    compile("machine");
  }

  public void testCompile_nodegroup1()
    throws IOException, WatersException
  {
    compile("nodegroup1");
  }

  public void testCompile_nodegroup2()
    throws IOException, WatersException
  {
    compile("nodegroup2");
  }

  public void testCompile_nodegroup4()
    throws IOException, WatersException
  {
    compile("nodegroup4");
  }

  public void testCompile_manwolfgoatcabbage()
    throws IOException, WatersException
  {
    compile("manwolfgoatcabbage");
  }

  public void testCompile_markus2()
    throws IOException, WatersException
  {
    compile("markus2");
  }

  public void testCompile_PLanTS()
    throws IOException, WatersException
  {
    compile("PLanTS");
  }

  public void testCompile_small_factory_2()
    throws IOException, WatersException
  {
    compile("small_factory_2");
  }

  public void testCompile_small_factory_n()
    throws IOException, WatersException
  {
    compile("small_factory_n");
  }

  public void testCompile_tictactoe()
    throws IOException, WatersException
  {
    compile("tictactoe");
  }

  public void testCompile_transferline()
    throws IOException, WatersException
  {
    compile("transferline");
  }

  public void testCompile_transferline__1()
    throws IOException, WatersException
  {
    final List<ParameterBindingProxy> bindings =
      new LinkedList<ParameterBindingProxy>();
    final ParameterBindingProxy binding = createBinding("N", 1);
    bindings.add(binding);
    compile("transferline", bindings, false);
  }

  public void testCompile_transferline__2()
    throws IOException, WatersException
  {
    final List<ParameterBindingProxy> bindings =
      new LinkedList<ParameterBindingProxy>();
    final ParameterBindingProxy binding = createBinding("N", 2);
    bindings.add(binding);
    compile("transferline", bindings, true);
  }

  public void testCompile_winemerchant()
    throws IOException, WatersException
  {
    compile("winemerchant");
  }


  //#########################################################################
  //# Successful Test Cases using EFA
  public void testCompile_EFA0()
    throws IOException, WatersException
  {
    compile("EFA0");
  }

  public void testCompile_GlobalAndLocalVariables()
    throws IOException, WatersException
  {
    compile("GlobalAndLocalVariables");
  }

  public void testCompile_sensoractuator()
    throws IOException, WatersException
  {
    compile("sensoractuator");
  }

  public void testCompile_stick_picking_game()
    throws IOException, WatersException
  {
    compile("stick_picking_game");
  }


  //#########################################################################
  //# Test Cases that Expect Exceptions
  public void testCompile_edge0()
    throws IOException, WatersException
  {
    compileError("edge0", null, EmptyLabelBlockException.class);
  }

  public void testCompile_error1_small()
    throws IOException, WatersException
  {
    compileError("error1_small", null,
                 DuplicateIdentifierException.class, "'mach'");
  }

  public void testCompile_error2_small()
    throws IOException, WatersException
  {
    compileError("error2_small",null, 
                 UndefinedIdentifierException.class,
                 "required parameter 'break'");
  }

  public void testCompile_error3_small()
    throws IOException, WatersException
  {
    compileError("error3_small", null,
                 UndefinedIdentifierException.class, "'finish_after'");
  }

  public void testCompile_error4_small()
    throws IOException, WatersException
  {
    compileError("error4_small", null, EventKindException.class, "'start1'");
  }

  public void testCompile_error5_small()
    throws IOException, WatersException
  {
    compileError("error5_small", null, 
                 UndefinedIdentifierException.class, "'finish_before'");
  }

  public void testCompile_error6_small()
    throws IOException, WatersException
  {
    compileError("error6_small", null, EventKindException.class, "'start2'");
  }

  public void testCompile_markus1()
    throws IOException, WatersException
  {
    compileError("markus1", null,
                 NondeterminismException.class, "'s0'", "'a'");
  }

  public void testCompile_nodegroup3()
    throws IOException, WatersException
  {
    compileError("nodegroup3", null,
                 NondeterminismException.class, "'q0'", "'e'");
  }

  public void testCompile_twoinit()
    throws IOException, WatersException
  {
    compileError("twoinit", null,
                 NondeterminismException.class, "'comp'");
  }


  //#########################################################################
  //# Utilities
  private void compileError(final String name,
                            final List<ParameterBindingProxy> bindings,
                            final Class<? extends WatersException> exclass)
    throws IOException, WatersException
  {
    final String[] culprits = {};
    compileError(name, bindings, exclass, culprits);
  }

  private void compileError(final String name,
                            final List<ParameterBindingProxy> bindings,
                            final Class<? extends WatersException> exclass,
                            final String culprit)
    throws IOException, WatersException
  {
    final String[] culprits = {culprit};
    compileError(name, bindings, exclass, culprits);
  }

  private void compileError(final String name,
                            final List<ParameterBindingProxy> bindings,
                            final Class<? extends WatersException> exclass,
                            final String culprit1,
                            final String culprit2)
    throws IOException, WatersException
  {
    final String[] culprits = {culprit1, culprit2};
    compileError(name, bindings, exclass, culprits);
  }

  private void compileError(final String name,
                            final List<ParameterBindingProxy> bindings,
                            final Class<? extends WatersException> exclass,
                            final String[] culprits)
    throws IOException, WatersException
  {
    try {
      final String inextname = name + mModuleMarshaller.getDefaultExtension();
      final File infilename = new File(mInputDirectory, inextname);
      final String outextname =
        name + mProductDESMarshaller.getDefaultExtension();
      final File outfilename = new File(mOutputDirectory, outextname);
      compile(infilename, outfilename, bindings);
      fail("Expected " + exclass.getName() + " not caught!");
    } catch (final WatersException exception) {
      if (exception.getClass() == exclass) {
        for (int i = 0; i < culprits.length; i++) {
          final String culprit = culprits[i];
          final String msg = exception.getMessage();
          assertTrue("Caught " + exclass.getName() +
                     " as expected, but message '" + msg +
                     "' does not mention culprit: " + culprit + "!",
                     msg != null && msg.indexOf(culprit) >= 0);
        }
        if (exception instanceof EvalException) {
          final EvalException evalException = (EvalException) exception;
          final Proxy location = evalException.getLocation();
          assertNotNull("Caught " + exception.getClass().getName() + " <" +
                        exception.getMessage() + "> provides no location!",
                        location);
        }
      } else {
        throw exception;
      }
    }
  }

  private void compile(final String name)
    throws IOException, WatersException
  {
    compile(name, null, false);
  }

  private void compile(final String name,
                       final List<ParameterBindingProxy> bindings,
                       final boolean appendToName)
    throws IOException, WatersException
  {
    final String inextname = name + mModuleMarshaller.getDefaultExtension();
    final File infilename = new File(mInputDirectory, inextname);
    final StringBuffer buffer = new StringBuffer(name);
    if (bindings != null && appendToName) {
      for (final ParameterBindingProxy binding : bindings) {
        buffer.append('-');
        buffer.append(binding.getExpression().toString());
      }
    }
    buffer.append(mProductDESMarshaller.getDefaultExtension());
    final String outextname = buffer.toString();
    final File outfilename = new File(mOutputDirectory, outextname);
    compile(infilename, outfilename, bindings);
    final File compfilename = new File(mInputDirectory, outextname);
    compare(outfilename, compfilename);
  }

  private void compile(final File infilename,
                       final File outfilename,
                       final List<ParameterBindingProxy> bindings)
    throws IOException, WatersException
  {
    final URI uri = infilename.toURI();
    final ModuleProxy module = mModuleMarshaller.unmarshal(uri);
    final ProductDESProxy des = compile(module, bindings);
    ensureParentDirectoryExists(outfilename);
    mProductDESMarshaller.marshal(des, outfilename);
  }

  private ProductDESProxy compile(final ModuleProxy module)
    throws EvalException
  {
    return compile(module, null);
  }

  private ProductDESProxy compile(final ModuleProxy module,
                                  final List<ParameterBindingProxy> bindings)
    throws EvalException
  {
    final ModuleCompiler compiler =
      new ModuleCompiler(mDocumentManager, mProductDESFactory, module);
    return compiler.compile(bindings);
  }

  private void compare(final File filename1, final File filename2)
    throws IOException, WatersUnmarshalException
  {
    final URI uri1 = filename1.toURI();
    final URI uri2 = filename2.toURI();
    final DocumentProxy proxy1 = mProductDESMarshaller.unmarshal(uri1);
    final DocumentProxy proxy2 = mProductDESMarshaller.unmarshal(uri2);
    assertTrue("Unexpected result!", proxy2.equalsByContents(proxy1));
  }

  private ParameterBindingProxy createBinding(final String name,
                                              final int value)
  {
    final IntConstantProxy expr = mModuleFactory.createIntConstantProxy(value);
    return mModuleFactory.createParameterBindingProxy(name, expr);
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
    throws JAXBException, SAXException
  { 
    mInputDirectory = new File(getInputRoot(), "handwritten");
    mOutputDirectory = getOutputDirectory();
    mModuleFactory = ModuleElementFactory.getInstance();
    mProductDESFactory = ProductDESElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mModuleMarshaller = new JAXBModuleMarshaller(mModuleFactory, optable);
    mProductDESMarshaller = new JAXBProductDESMarshaller(mProductDESFactory);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerMarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
  }

  protected void tearDown()
  {
    mInputDirectory = null;
    mOutputDirectory = null;
    mModuleFactory = null;
    mProductDESFactory = null;
    mModuleMarshaller = null;
    mProductDESMarshaller = null;
    mDocumentManager = null;
  }


  //#########################################################################
  //# Data Members
  private File mInputDirectory;
  private File mOutputDirectory;
  private ModuleProxyFactory mModuleFactory;
  private ProductDESProxyFactory mProductDESFactory;
  private JAXBModuleMarshaller mModuleMarshaller;
  private JAXBProductDESMarshaller mProductDESMarshaller;
  private DocumentManager mDocumentManager;

}
