//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   AbstractCompilerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.compiler.context.DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.UndefinedIdentifierException;
import net.sourceforge.waters.model.compiler.efa.ActionSyntaxException;
import net.sourceforge.waters.model.compiler.graph.NondeterministicModuleException;
import net.sourceforge.waters.model.compiler.instance.EmptyLabelBlockException;
import net.sourceforge.waters.model.compiler.instance.EventKindException;
import net.sourceforge.waters.model.compiler.instance.InstantiationException;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.MultiEvalException;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.DescendingModuleProxyVisitor;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public abstract class AbstractCompilerTest
  extends AbstractWatersTest
{

  //#########################################################################
  //# Hand-Crafting Test Cases
  public void testCompile_empty_1()
    throws EvalException
  {
    final String name = "empty";
    final ModuleProxy module = mModuleFactory.createModuleProxy
      (name, null, null, null, null, null, null);
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
    final SimpleIdentifierProxy ident =
      mModuleFactory.createSimpleIdentifierProxy(instname);
    final InstanceProxy instance = mModuleFactory.createInstanceProxy
      (ident, instname, null);
    final ModuleProxy module = mModuleFactory.createModuleProxy
      (modname, null, null, null, null, null,
       Collections.singletonList(instance));
    try {
      compile(module);
      fail("Expected InstantiationException not caught!");
    } catch (final WatersException exception) {
      final String[] culprit = {"'" + instname + "'"};
      checkExceptions(module, exception, InstantiationException.class, culprit);
    }
  }


  //#########################################################################
  //# Successful Test Cases
  public void testCompile_array()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/instance", "array");
    testCompile(module);
  }

  public void testCompile_array2d()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/instance", "array2d");
    testCompile(module);
  }

  public void testCompile_buffer_sf1()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "buffer_sf1");
    testCompile(module);
  }

  public void testCompile_buffertest()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "buffertest");
    testCompile(module);
  }

  public void testCompile_colours()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/graph", "colours");
    testCompile(module);
  }

  public void testCompile_empty_intrange() throws IOException,
    WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/instance", "empty_intrange");
    testCompile(module);
  }

  public void testCompile_empty_prop() throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "nasty", "empty_prop");
    testCompile(module);
  }

  public void testCompile_empty_spec() throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "nasty", "empty_spec");
    testCompile(module);
  }

  public void testCompile_machine() throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "machine");
    testCompile(module);
  }

  public void testCompile_nested_groups() throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/groupnode", "nested_groups");
    testCompile(module);
  }

  public void testCompile_nodegroup1()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/groupnode", "nodegroup1");
    testCompile(module);
  }

  public void testCompile_nodegroup2()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/groupnode", "nodegroup2");
    testCompile(module);
  }

  public void testCompile_nodegroup4()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/groupnode", "nodegroup4");
    testCompile(module);
  }

  public void testCompile_manwolfgoatcabbage()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "manwolfgoatcabbage");
    testCompile(module);
  }

  public void testCompile_markus2()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/graph", "markus2");
    testCompile(module);
  }

  public void testCompile_parManEg_I_mfb_lowlevel()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "hisc", "parManEg_I_mfb_lowlevel");
    testCompile(module);
  }

  public void testCompile_PLanTS()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "PLanTS");
    testCompile(module);
  }

  public void testCompile_small_factory_2()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "small_factory_2");
    testCompile(module);
  }

  public void testCompile_small_factory_n()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "small_factory_n");
    testCompile(module);
  }

  public void testCompile_spaces()
  throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests","compiler/instance", "spaces");
    testCompile(module);
  }

  public void testCompile_tictactoe()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "tictactoe");
    testCompile(module);
  }

  public void testCompile_transferline()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "transferline");
    testCompile(module);
  }

  public void testCompile_transferline__1()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "transferline");
    final List<ParameterBindingProxy> bindings =
      new LinkedList<ParameterBindingProxy>();
    final ParameterBindingProxy binding = createBinding("N", 1);
    bindings.add(binding);
    testCompile(module, bindings, false);
  }

  public void testCompile_transferline__2()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "transferline");
    final List<ParameterBindingProxy> bindings =
      new LinkedList<ParameterBindingProxy>();
    final ParameterBindingProxy binding = createBinding("N", 2);
    bindings.add(binding);
    testCompile(module, bindings, true);
  }

  public void testCompile_unused_prop()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "nasty", "unused_prop");
    testCompile(module);
  }

  public void testCompile_winemerchant()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "winemerchant");
    testCompile(module);
  }


  //#########################################################################
  //# Successful Test Cases using EFA
  public void testCompile_AmbiguousVariableStatus()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "AmbiguousVariableStatus");
    testCompile(module);
  }

  public void testCompile_BallLift()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "BallLift");
    testCompile(module);
  }

  public void testCompile_batch_tank_vout()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "batch_tank_vout");
    testCompile(module);
  }

  public void testCompile_blocked_efa()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "blocked_efa");
    testCompile(module);
  }

  public void testCompile_ControllableTestModelEFA()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "ControllableTestModelEFA");
    testCompile(module);
  }

  public void testCompile_dosingtankEFA()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "dosingtankEFA");
    testCompile(module);
  }

  public void testCompile_DosingTankControllerEFA()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "DosingTankControllerEFA");
    testCompile(module);
  }

  public void testCompile_DosingTankWithJelly1()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "DosingTankWithJellyEFA1");
    testCompile(module);
  }

  public void testCompile_DosingTankWithJelly2()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "DosingTankWithJellyEFA2");
    testCompile(module);
  }

  public void testCompile_EFA0()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "EFA0");
    testCompile(module);
  }

  public void testCompile_EFAJournalExample()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "EFAJournalExample");
    testCompile(module);
  }

  public void testCompile_EFATransferLine()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("efa", "transferline_efa");
    testCompile(module);
  }

  public void testCompile_enumvar()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "enumvar");
    testCompile(module);
  }

  public void testCompile_forbidden()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/graph", "forbidden");
    testCompile(module);
  }

  public void testCompile_funcall_max()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "funcall_max");
    testCompile(module);
  }

  public void testCompile_funcall_min()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "funcall_min");
    testCompile(module);
  }

  public void testCompile_GlobalAndLocalVariables()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "GlobalAndLocalVariables");
    testCompile(module);
  }

  public void testCompile_guard_conflict_1()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/groupnode", "guard_conflict_1");
    testCompile(module);
  }

  public void testCompile_guard_conflict_2()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/groupnode", "guard_conflict_2");
    testCompile(module);
  }


  public void testCompile_host_sets_fv_after_host_crc_fault_notinit()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "profisafe", "host_sets_fv_after_host_crc_fault_notinit");
    testCompile(module);
  }

  public void testCompile_increment()
  throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "increment");
    testCompile(module);
  }

  public void testCompile_instantiate_efa()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/instance", "instantiate_efa");
    testCompile(module);
  }

  public void testCompile_io1()
  throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "io1");
    testCompile(module);
  }

  public void testCompile_machines_buffer_efa()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "machines_buffer_efa");
    testCompile(module);
  }

  public void testCompile_markedvar()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "markedvar");
    testCompile(module);
  }

  public void testCompile_nodegroup_efa1()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/groupnode", "nodegroup_efa1");
    testCompile(module);
  }

  public void testCompile_nodegroup_efa2()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/groupnode", "nodegroup_efa2");
    testCompile(module);
  }

  public void testCompile_nondetvar()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "nondetvar");
    testCompile(module);
  }

  public void testCompile_patrik1()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "patrik1");
    testCompile(module);
  }

  public void testCompile_patrik2()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "patrik2");
    testCompile(module);
  }

  public void testCompile_profisafe_ihost_nonsubsumptions()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "profisafe_ihost_nonsubsumptions");
    testCompile(module);
  }

  public void testCompile_profisafe_islave_pfork()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "profisafe_islave_pfork");
    testCompile(module);
  }

  public void testCompile_randomEFA()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "random_efa");
    testCompile(module);
  }

  public void testCompile_sahar1()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "sahar1");
    testCompile(module);
  }

  public void testCompile_sahar2()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "sahar2");
    testCompile(module);
  }

  public void testCompile_sensoractuator1()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "sensoractuator1");
    testCompile(module);
  }

  public void testCompile_sensoractuator2()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "sensoractuator2");
    testCompile(module);
  }

  public void testCompile_sensoractuator_nondet()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "sensoractuator_nondet");
    testCompile(module);
  }

  public void testCompile_stick_picking_game()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "stick_picking_game");
    testCompile(module);
  }

  public void testCompile_unsat_guard()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "unsat_guard");
    testCompile(module);
  }


  //#########################################################################
  //# Test Cases Expecting Exceptions
  public void testCompile_assignmentInGuard1()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "assignment_in_guard1");
    compileError(module, ActionSyntaxException.class, "Assignment operator =");
  }

  public void testCompile_assignmentInGuard2()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "assignment_in_guard2");
    compileError(module, ActionSyntaxException.class, "Assignment operator =");
  }

  public void testCompile_edge0()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/graph", "edge0");
    compileError(module, EmptyLabelBlockException.class, "q0");
  }

  public void testCompile_error_batch_tank_out()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "batch_tank_out");
    compileError(module, DuplicateIdentifierException.class, "'out'");
  }

  public void testCompile_error_ims()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/instance", "error_ims");
    compileError(module, UndefinedIdentifierException.class, "'finishLathe'");
  }

  public void testCompile_error1_small()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/instance", "error1_small");
    compileError(module, DuplicateIdentifierException.class, "'mach'");
  }

  public void testCompile_error2_small()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/instance", "error2_small");
    final String[] culprit1 = {"required parameter 'break'"};
    final String[] culprit2 = {"required parameter 'repair'"};
    compileError(module, null, InstantiationException.class,
                 culprit1, culprit2);
  }

  public void testCompile_error3_small()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/instance", "error3_small");
    compileError(module, InstantiationException.class, "'finish_after'");
  }

  public void testCompile_error4_small()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/instance", "error4_small");
    final String[] culprit1 = {"'start1'"};
    final String[] culprit2 = {"'start2'"};
    compileError(module, null, InstantiationException.class,
                 culprit1, culprit2);
  }

  public void testCompile_error5_small()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/instance", "error5_small");
    compileError(module, InstantiationException.class, "'finish_before'");
  }

  public void testCompile_error6_small()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/instance", "error6_small");
    compileError(module, InstantiationException.class, "'start2'");
  }

  public void testCompile_error7_small()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "error7_small");
    compileError(module, UndefinedIdentifierException.class, "'buffer.curr'");
  }

  public void testCompile_error8_small()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/graph", "error8_small");
    compileError(module, EventKindException.class, "'repair1'");
  }

  public void testCompile_markus1()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/graph", "markus1");
    compileError(module, NondeterministicModuleException.class, "'s0'", "'a'");
  }

  public void testCompile_nodegroup3()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/groupnode", "nodegroup3");
    compileError(module, NondeterministicModuleException.class, "'q0'", "'e'");
  }

  public void testCompile_twoinit()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/graph", "twoinit");
    compileError(module, NondeterministicModuleException.class, "'comp'");
  }

  public void testCompile_undefvar_01()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "undefvar_01");
    compileError(module, UndefinedIdentifierException.class, "'undefvar'");
  }

  public void testCompile_undefvar_02()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler/efsm", "undefvar_02");
    compileError(module, UndefinedIdentifierException.class, "'undefvar'");
  }


  //#########################################################################
  //# Customisation
  void configure(final ModuleCompiler compiler)
  {
  }

  abstract String[] getTestSuffices();


  //#########################################################################
  //# Auxiliary Methods
  protected ModuleProxy loadModule(final String... path)
    throws IOException, WatersException
  {
    File dir = getWatersInputRoot();
    final int numDirs = path.length - 1;
    for (int i = 0; i < numDirs; i++) {
      final String name = path[i];
      dir = new File(dir, name);
    }
    String extname = path[numDirs];
    if (extname.indexOf('.') < 0) {
      extname += mModuleMarshaller.getDefaultExtension();
    }
    final File filename = new File(dir, extname);
    final URI uri = filename.toURI();
    return mModuleMarshaller.unmarshal(uri);
  }

  private void compileError(final ModuleProxy module,
                              final Class<? extends WatersException> exclass,
                              final String... culprit)
    throws IOException, WatersException
  {
    final String[][] culprits = {culprit};
    compileError(module, null, exclass, culprits);
  }

  @SuppressWarnings("unused")
  private void compileError(final ModuleProxy module,
                            final List<ParameterBindingProxy> bindings,
                            final Class<? extends WatersException> exclass,
                            final String... culprit)
    throws IOException, WatersException
  {
    final String[][] culprits = {culprit};
    compileError(module, bindings, exclass, culprits);
  }

  protected void compileError(final ModuleProxy module,
                            final List<ParameterBindingProxy> bindings,
                            final Class<? extends WatersException> exclass,
                            final String[]... culprits)
    throws IOException, WatersException
  {
    try {
      compile(module, bindings);
      fail("Expected " + exclass.getSimpleName() + " not caught!");
    } catch (final WatersException exception) {
      checkExceptions(module, exception, exclass, culprits);
    }
  }

  private void testCompile(final ModuleProxy module)
    throws IOException, WatersException
  {
    testCompile(module, null, false);
  }

  private void testCompile(final ModuleProxy module,
                           final List<ParameterBindingProxy> bindings,
                           final boolean appendToName)
    throws IOException, WatersException
  {
    final ProductDESProxy des = compile(module, bindings);
    final String name = module.getName();
    final StringBuilder buffer = new StringBuilder(name);
    if (bindings != null && appendToName) {
      for (final ParameterBindingProxy binding : bindings) {
        buffer.append('-');
        buffer.append(binding.getExpression().toString());
      }
    }
    final String ext = mProductDESMarshaller.getDefaultExtension();
    final int pos = buffer.length();
    buffer.append(ext);
    final String outextname = buffer.toString();
    final File outfilename = new File(mOutputDirectory, outextname);
    mProductDESMarshaller.marshal(des, outfilename);
    final String[] suffices = getTestSuffices();
    buffer.setLength(pos);
    String temp = "";
    if (suffices.length == 2)
      temp = buffer.toString().concat("-" + suffices[1] + ext);
    for (final String suffix : suffices) {
      buffer.append('-');
      buffer.append(suffix);
    }
    buffer.append(ext);
    final String suffixedname = buffer.toString();
    final File location = module.getFileLocation();
    final File dir = location.getParentFile();
    final File suffixedfilename = new File(dir, suffixedname);
    final File suffixedfilename2 = new File(dir, temp);
    if (suffixedfilename.exists()) {
      compare(des, suffixedfilename);
    } else if (suffices.length == 2 && suffixedfilename2.exists()) {
      compare(des, suffixedfilename2);
    } else {
      final File compfilename = new File(dir, outextname);
      compare(des, compfilename);
    }
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
    mCompiler =
      new ModuleCompiler(mDocumentManager, mProductDESFactory, module);
    configure(mCompiler);
    return mCompiler.compile(bindings);
  }

  private void compare(final DocumentProxy doc1, final File filename2)
    throws IOException, WatersUnmarshalException
  {
    final URI uri2 = filename2.toURI();
    final DocumentProxy doc2 = mProductDESMarshaller.unmarshal(uri2);
    assertProductDESProxyEquals(doc1, doc2);
  }

  private ParameterBindingProxy createBinding(final String name,
                                              final int value)
  {
    final IntConstantProxy expr = mModuleFactory.createIntConstantProxy(value);
    return mModuleFactory.createParameterBindingProxy(name, expr);
  }

  /**
   * Asserts that the exception mentions all the phrases in the culprit.
   */
  private void assertMentions(final WatersException exception,
                              final String[] culprit)
  {
    final String msg = exception.getMessage();
    for (final String phrase : culprit) {
      assertTrue("Caught " + exception.getClass().getSimpleName() +
                 " as expected, but message '" + msg +
                 "' does not mention culprit: " + phrase + "!",
                 msg.contains(phrase));
    }
  }

  /**
   * Checks if the exception mentions all of the phrases in the culprit.
   */
  private boolean mentions(final WatersException exception,
                           final String[] culprit)
  {
    final String msg = exception.getMessage();
    for (final String phrase : culprit) {
      if (!msg.contains(phrase)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Asserts that the culprit is mentioned by at least one exception.
   */
  private void assertMentioned(final String[] culprit,
                               final WatersException[] exceptions)
  {
    for (final WatersException exception : exceptions) {
      if (mentions(exception, culprit)) {
        return;
      }
    }
    fail("The culprit " + Arrays.toString(culprit) +
         " is not mentioned in any of the exception messages!");
  }

  /**
   * Asserts that the exception mentions at least one culprit.
   */
  private void assertMentionsAny(final WatersException exception,
                                 final String[][] culprits)
  {
    if (culprits.length == 1) {
      assertMentions(exception, culprits[0]);
    } else {
      for (final String[] culprit : culprits) {
        if (mentions(exception, culprit)) {
          return;
        }
      }
      fail("Caught " + exception.getClass().getSimpleName() +
           " as expected, but message '" + exception.getMessage() +
           "' does not mention any of the culprits: " +
           Arrays.deepToString(culprits) + "!");
    }
  }

  /**
   * Asserts that the exception has the right type, mentions one of the
   * culprits, and has a valid location.
   */
  private void checkException(final ModuleProxy module,
                              final WatersException exception,
                              final Class<? extends WatersException> exclass,
                              final String[]... culprits)
  {
    assertEquals("Wrong exception type!", exclass, exception.getClass());
    final String msg = exception.getMessage();
    assertNotNull("Caught " + exclass.getSimpleName() +
                  " as expected, but no error message found!", msg);
    assertMentionsAny(exception, culprits);
    if (exception instanceof EvalException) {
      final EvalException evalException = (EvalException) exception;
      final Proxy location = evalException.getLocation();
      assertNotNull("Caught " + exclass.getSimpleName() + " <" + msg +
                    "> provides no location!",
                    location);
      if (mCompiler.isSourceInfoEnabled()) {
        assertTrue("Caught " + exception.getClass().getSimpleName() + " <" +
                   msg + "> in " + location.getClass().getSimpleName() +
                   " which is not in the module!",
                   mDescendantCheckVisitor.isDescendant(location, module));
      }
    }
  }

  /**
   * Asserts that the exception fulfills all the requirements.
   * <CODE>MultiEvalExceptions</CODE> are handled appropriately.
   */
  private void checkExceptions(final ModuleProxy module,
                               final WatersException exception,
                               final Class<? extends WatersException> exclass,
                               final String[]... culprits)
  {
    assertTrue("Invalid test, does not specify any culprits!",
               culprits.length > 0);
    final boolean multi = mCompiler.isMultiExceptionsEnabled();
    if (multi && EvalException.class.isAssignableFrom(exclass)) {
      assertEquals("Not a MultiEvalException!",
                   MultiEvalException.class, exception.getClass());
      final EvalException[] exceptions = ((EvalException) exception).getAll();
      assertTrue("Caught MultiEvalException as expected, " +
                 "but it does not contain any exceptions!",
                 exceptions.length > 0);
      for (final EvalException ex : exceptions) {
        checkException(module, ex, exclass, culprits);
      }
      for (final String[] culprit : culprits) {
        assertMentioned(culprit, exceptions);
      }
    } else {
      checkException(module, exception, exclass, culprits[0]);
    }
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp()
    throws Exception
  {
    super.setUp();
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
    mDescendantCheckVisitor = new DescendantCheckVisitor();
  }

  @Override
  protected void tearDown()
    throws Exception
  {
    mOutputDirectory = null;
    mModuleFactory = null;
    mProductDESFactory = null;
    mModuleMarshaller = null;
    mProductDESMarshaller = null;
    mDocumentManager = null;
    mDescendantCheckVisitor = null;
    super.tearDown();
  }


  //#########################################################################
  //# Inner Class DescendantCheckVisitor
  private static class DescendantCheckVisitor
    extends DescendingModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private boolean isDescendant(final Proxy child, final Proxy parent)
    {
      mChild = child;
      try {
        parent.acceptVisitor(this);
      } catch (final VisitorException exception) {
        if (exception == SUCCESS) {
          return true;
        } else {
          throw exception.getRuntimeException();
        }
      }
      return false;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitProxy(final Proxy proxy)
      throws VisitorException
    {
      if (proxy == mChild) {
        throw SUCCESS;
      }
      return null;
    }

    //#######################################################################
    //# Data Members
    private Proxy mChild;

    //#######################################################################
    //# Class Constants
    private static final VisitorException SUCCESS = new VisitorException();

  }


  //#########################################################################
  //# Data Members
  private File mOutputDirectory;
  private ModuleProxyFactory mModuleFactory;
  private ProductDESProxyFactory mProductDESFactory;
  private JAXBModuleMarshaller mModuleMarshaller;
  private JAXBProductDESMarshaller mProductDESMarshaller;
  private DocumentManager mDocumentManager;
  private ModuleCompiler mCompiler;
  private DescendantCheckVisitor mDescendantCheckVisitor;

}
