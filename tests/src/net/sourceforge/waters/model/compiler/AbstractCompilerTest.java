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
  //# Handcrafting Test Cases
  public void testCompile_empty_1()
    throws EvalException
  {
    final String name = "empty";
    mModule = mModuleFactory.createModuleProxy
      (name, null, null, null, null, null, null);
    final ProductDESProxy des = compile(mModule);
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
      final InstanceProxy instance = mModuleFactory.createInstanceProxy
        (ident, instname, null);
      mModule = mModuleFactory.createModuleProxy
        (modname, null, null, null, null, null,
         Collections.singletonList(instance));
      compile(mModule);
      fail("Expected InstantiationException not caught!");
    } catch (final WatersException exception) {
      final String[] culprit = {"'" + instname + "'"};
      checkExceptions(exception, InstantiationException.class, culprit);
    }
  }


  //#########################################################################
  //# Successful Test Cases
  public void testCompile_array()
    throws IOException, WatersException
  {
    compile("tests", "nasty", "array");
  }

  public void testCompile_array2d()
    throws IOException, WatersException
  {
    compile("tests", "nasty", "array2d");
  }

  public void testCompile_buffer_sf1()
    throws IOException, WatersException
  {
    compile("handwritten", "buffer_sf1");
  }

  public void testCompile_buffertest()
    throws IOException, WatersException
  {
    compile("handwritten", "buffertest");
  }

  public void testCompile_colours()
    throws IOException, WatersException
  {
    compile("handwritten", "colours");
  }

  public void testCompile_empty_intrange() throws IOException,
    WatersException
  {
    compile("tests", "nasty", "empty_intrange");
  }

  public void testCompile_empty_prop() throws IOException, WatersException
  {
    compile("tests", "nasty", "empty_prop");
  }

  public void testCompile_empty_spec() throws IOException, WatersException
  {
    compile("tests", "nasty", "empty_spec");
  }

  public void testCompile_machine() throws IOException, WatersException
  {
    compile("handwritten", "machine");
  }

  public void testCompile_nested_groups() throws IOException, WatersException
  {
    compile("tests", "nasty", "nested_groups");
  }

  public void testCompile_nodegroup1()
    throws IOException, WatersException
  {
    compile("handwritten", "nodegroup1");
  }

  public void testCompile_nodegroup2()
    throws IOException, WatersException
  {
    compile("handwritten", "nodegroup2");
  }

  public void testCompile_nodegroup4()
    throws IOException, WatersException
  {
    compile("handwritten", "nodegroup4");
  }

  public void testCompile_manwolfgoatcabbage()
    throws IOException, WatersException
  {
    compile("handwritten", "manwolfgoatcabbage");
  }

  public void testCompile_markus2()
    throws IOException, WatersException
  {
    compile("handwritten", "markus2");
  }

  public void testCompile_parManEg_I_mfb_lowlevel()
    throws IOException, WatersException
  {
    compile("tests", "hisc", "parManEg_I_mfb_lowlevel");
  }

  public void testCompile_PLanTS()
    throws IOException, WatersException
  {
    compile("handwritten", "PLanTS");
  }

  public void testCompile_small_factory_2()
    throws IOException, WatersException
  {
    compile("handwritten", "small_factory_2");
  }

  public void testCompile_small_factory_n()
    throws IOException, WatersException
  {
    compile("handwritten", "small_factory_n");
  }

  public void testCompile_spaces()
  throws IOException, WatersException
  {
    compile("tests", "nasty", "spaces");
  }

  public void testCompile_tictactoe()
    throws IOException, WatersException
  {
    compile("handwritten", "tictactoe");
  }

  public void testCompile_transferline()
    throws IOException, WatersException
  {
    compile("handwritten", "transferline");
  }

  public void testCompile_transferline__1()
    throws IOException, WatersException
  {
    final File root = getWatersInputRoot();
    final File dir = new File(root, "handwritten");
    final List<ParameterBindingProxy> bindings =
      new LinkedList<ParameterBindingProxy>();
    final ParameterBindingProxy binding = createBinding("N", 1);
    bindings.add(binding);
    compile(dir, "transferline", bindings, false);
  }

  public void testCompile_transferline__2()
    throws IOException, WatersException
  {
    final File root = getWatersInputRoot();
    final File dir = new File(root, "handwritten");
    final List<ParameterBindingProxy> bindings =
      new LinkedList<ParameterBindingProxy>();
    final ParameterBindingProxy binding = createBinding("N", 2);
    bindings.add(binding);
    compile(dir, "transferline", bindings, true);
  }

  public void testCompile_unused_prop()
    throws IOException, WatersException
  {
    compile("tests", "nasty", "unused_prop");
  }

  public void testCompile_winemerchant()
    throws IOException, WatersException
  {
    compile("handwritten", "winemerchant");
  }


  //#########################################################################
  //# Successful Test Cases using EFA
  public void testCompile_AmbiguousVariableStatus()
    throws IOException, WatersException
  {
    compile("handwritten", "AmbiguousVariableStatus");
  }

  public void testCompile_BallLift()
    throws IOException, WatersException
  {
    compile("handwritten", "BallLift");
  }

  public void testCompile_batch_tank_vout()
    throws IOException, WatersException
  {
    compile("tests", "efa", "batch_tank_vout");
  }

  public void testCompile_blocked_efa()
    throws IOException, WatersException
  {
    compile("tests", "efa", "blocked_efa");
  }

  public void testCompile_ControllableTestModelEFA()
    throws IOException, WatersException
  {
    compile("handwritten", "ControllableTestModelEFA");
  }

  public void testCompile_dosingtankEFA()
    throws IOException, WatersException
  {
    compile("handwritten", "dosingtankEFA");
  }

  public void testCompile_DosingTankControllerEFA()
    throws IOException, WatersException
  {
    compile("handwritten", "DosingTankControllerEFA");
  }

  public void testCompile_DosingTankWithJelly1()
    throws IOException, WatersException
  {
    compile("handwritten", "DosingTankWithJellyEFA1");
  }

  public void testCompile_DosingTankWithJelly2()
    throws IOException, WatersException
  {
    compile("handwritten", "DosingTankWithJellyEFA2");
  }

  public void testCompile_EFA0()
    throws IOException, WatersException
  {
    compile("handwritten", "EFA0");
  }

  public void testCompile_EFAJournalExample()
  throws IOException, WatersException
  {
    compile("handwritten", "EFAJournalExample");
  }

  public void testCompile_EFATransferLine()
  throws IOException, WatersException
  {
    compile("efa", "transferline_efa");
  }

  public void testCompile_enumvar()
    throws IOException, WatersException
  {
    compile("handwritten", "enumvar");
  }

  public void testCompile_forbidden()
    throws IOException, WatersException
  {
    compile("tests", "nasty", "forbidden");
  }

  public void testCompile_funcall_max()
    throws IOException, WatersException
  {
    compile("tests", "efa", "funcall_max");
  }

  public void testCompile_funcall_min()
    throws IOException, WatersException
  {
    compile("tests", "efa", "funcall_min");
  }

  public void testCompile_GlobalAndLocalVariables()
    throws IOException, WatersException
  {
    compile("handwritten", "GlobalAndLocalVariables");
  }

  public void testCompile_guard_conflict_1()
    throws IOException, WatersException
  {
    compile("handwritten", "guard_conflict_1");
  }

  public void testCompile_guard_conflict_2()
    throws IOException, WatersException
  {
    compile("handwritten", "guard_conflict_2");
  }


  public void testCompile_host_sets_fv_after_host_crc_fault_notinit()
    throws IOException, WatersException
  {
    compile("tests", "profisafe", "host_sets_fv_after_host_crc_fault_notinit");
  }

  public void testCompile_increment()
  throws IOException, WatersException
  {
    compile("tests", "efa", "increment");
  }

  public void testCompile_instantiate_efa()
    throws IOException, WatersException
  {
    compile("tests", "efa", "instantiate_efa");
  }

  public void testCompile_io1()
  throws IOException, WatersException
  {
    compile("handwritten", "io1");
  }

  public void testCompile_machines_buffer_efa()
    throws IOException, WatersException
  {
    compile("handwritten", "machines_buffer_efa");
  }

  public void testCompile_markedvar()
    throws IOException, WatersException
  {
    compile("handwritten", "markedvar");
  }

  public void testCompile_nodegroup_efa1()
    throws IOException, WatersException
  {
    compile("handwritten", "nodegroup_efa1");
  }

  public void testCompile_nodegroup_efa2()
    throws IOException, WatersException
  {
    compile("handwritten", "nodegroup_efa2");
  }

  public void testCompile_nondetvar()
    throws IOException, WatersException
  {
    compile("handwritten", "nondetvar");
  }

  public void testCompile_patrik1()
    throws IOException, WatersException
  {
    compile("tests", "efa", "patrik1");
  }

  public void testCompile_patrik2()
    throws IOException, WatersException
  {
    compile("tests", "efa", "patrik2");
  }

  /*
  public void testCompile_profisafe_ihost_efa()
    throws IOException, WatersException
  {
    compile("tests", "profisafe", "profisafe_ihost_efa");
  }

  public void testCompile_profisafe_islave()
    throws IOException, WatersException
  {
    compile("tests", "profisafe", "profisafe_islave_efa");
  }
  */

  public void testCompile_profisafe_ihost_nonsubsumptions()
    throws IOException, WatersException
  {
    compile("tests", "efa", "profisafe_ihost_nonsubsumptions");
  }

  /*
  public void testCompile_profisafe_islave_property()
    throws IOException, WatersException
  {
    compile("tests", "efa", "profisafe_islave_property");
  }
  */

  public void testCompile_profisafe_islave_pfork()
    throws IOException, WatersException
  {
    compile("tests", "efa", "profisafe_islave_pfork");
  }

  public void testCompile_randomEFA()
    throws IOException, WatersException
  {
    compile("tests", "efa", "random_efa");
  }

  public void testCompile_sahar1()
    throws IOException, WatersException
  {
    compile("tests", "efa", "sahar1");
  }

  public void testCompile_sahar2()
    throws IOException, WatersException
  {
    compile("tests", "efa", "sahar2");
  }

  public void testCompile_sensoractuator1()
    throws IOException, WatersException
  {
    compile("handwritten", "sensoractuator1");
  }

  public void testCompile_sensoractuator2()
    throws IOException, WatersException
  {
    compile("handwritten", "sensoractuator2");
  }

  public void testCompile_sensoractuator_nondet()
    throws IOException, WatersException
  {
    compile("handwritten", "sensoractuator_nondet");
  }

  public void testCompile_stick_picking_game()
    throws IOException, WatersException
  {
    compile("handwritten", "stick_picking_game");
  }


  //#########################################################################
  //# Test Cases that Expect Exceptions
  public void testCompile_assignmentInGuard1()
    throws IOException, WatersException
  {
    compileError("tests", "efa", "assignment_in_guard1", null,
                 ActionSyntaxException.class, "Assignment operator =");
  }

  public void testCompile_assignmentInGuard2()
    throws IOException, WatersException
  {
    compileError("tests", "efa", "assignment_in_guard2", null,
                 ActionSyntaxException.class, "Assignment operator =");
  }

  public void testCompile_edge0()
    throws IOException, WatersException
  {
    compileError("handwritten", "edge0", null,
                 EmptyLabelBlockException.class, "q0");
  }

  public void testCompile_error_batch_tank_out()
    throws IOException, WatersException
  {
    compileError("tests", "nasty", "batch_tank_out", null,
                 DuplicateIdentifierException.class, "'out'");
  }

  public void testCompile_error_ims()
    throws IOException, WatersException
  {
    compileError("tests", "ims", "error_ims", null,
                 UndefinedIdentifierException.class, "'finishLathe'");
  }

  public void testCompile_error1_small()
    throws IOException, WatersException
  {
    compileError("handwritten", "error1_small", null,
                 DuplicateIdentifierException.class, "'mach'");
  }

  public void testCompile_error2_small()
    throws IOException, WatersException
  {
    final String[] culprit1 = {"required parameter 'break'"};
    final String[] culprit2 = {"required parameter 'repair'"};
    compileError("handwritten", "error2_small",null,
                 InstantiationException.class,
                 culprit1, culprit2);
  }

  public void testCompile_error3_small()
    throws IOException, WatersException
  {
    compileError("handwritten", "error3_small", null,
                 InstantiationException.class, "'finish_after'");
  }

  public void testCompile_error4_small()
    throws IOException, WatersException
  {
    final String[] culprit1 = {"'start1'"};
    final String[] culprit2 = {"'start2'"};
    compileError("handwritten", "error4_small", null,
                 InstantiationException.class, culprit1, culprit2);
  }

  public void testCompile_error5_small()
    throws IOException, WatersException
  {
    compileError("handwritten", "error5_small", null,
                 InstantiationException.class, "'finish_before'");
  }

  public void testCompile_error6_small()
    throws IOException, WatersException
  {
    compileError("handwritten", "error6_small", null,
                 InstantiationException.class, "'start2'");
  }

  public void testCompile_error7_small()
    throws IOException, WatersException
  {
    compileError("handwritten", "error7_small", null,
                 UndefinedIdentifierException.class, "'buffer.curr'");
  }

  public void testCompile_error8_small()
    throws IOException, WatersException
  {
    compileError("handwritten", "error8_small", null,
                 EventKindException.class, "'repair1'");
  }

  public void testCompile_markus1()
    throws IOException, WatersException
  {
    compileError("handwritten", "markus1", null,
                 NondeterministicModuleException.class, "'s0'", "'a'");
  }

  public void testCompile_nodegroup3()
    throws IOException, WatersException
  {
    compileError("handwritten", "nodegroup3", null,
                 NondeterministicModuleException.class, "'q0'", "'e'");
  }

  public void testCompile_twoinit()
    throws IOException, WatersException
  {
    compileError("handwritten", "twoinit", null,
                 NondeterministicModuleException.class, "'comp'");
  }

  public void testCompile_undefvar_01()
    throws IOException, WatersException
  {
    compileError("handwritten", "undefvar_01", null,
                 UndefinedIdentifierException.class, "'undefvar'");
  }

  public void testCompile_undefvar_02()
    throws IOException, WatersException
  {
    compileError("handwritten", "undefvar_02", null,
                 UndefinedIdentifierException.class, "'undefvar'");
  }


  //#########################################################################
  //# Customisation
  void configure(final ModuleCompiler compiler)
  {
  }

  abstract String getTestSuffix();


  //#########################################################################
  //# Utilities
  private void compileError(final String dirname,
                            final String name,
                            final List<ParameterBindingProxy> bindings,
                            final Class<? extends WatersException> exclass,
                            final String... culprit)
    throws IOException, WatersException
  {
    final String[][] culprits = {culprit};
    compileError(dirname, name, bindings, exclass, culprits);
  }

  private void compileError(final String dirname,
                            final String name,
                            final List<ParameterBindingProxy> bindings,
                            final Class<? extends WatersException> exclass,
                            final String[]... culprits)
    throws IOException, WatersException
  {
    final File root = getWatersInputRoot();
    final File dir = new File(root, dirname);
    compileError(dir, name, bindings, exclass, culprits);
  }

  private void compileError(final String dirname,
                            final String subdirname,
                            final String name,
                            final List<ParameterBindingProxy> bindings,
                            final Class<? extends WatersException> exclass,
                            final String... culprit)
    throws IOException, WatersException
  {
    final File root = getWatersInputRoot();
    final File dir = new File(root, dirname);
    final File subdir = new File(dir, subdirname);
    final String[][] culprits = {culprit};
    compileError(subdir, name, bindings, exclass, culprits);
  }

  private void compileError(final File dir,
                            final String name,
                            final List<ParameterBindingProxy> bindings,
                            final Class<? extends WatersException> exclass,
                            final String[][] culprits)
    throws IOException, WatersException
  {
    try {
      final String inextname = name + mModuleMarshaller.getDefaultExtension();
      final File infilename = new File(dir, inextname);
      final String outextname =
        name + mProductDESMarshaller.getDefaultExtension();
      final File outfilename = new File(mOutputDirectory, outextname);
      compile(infilename, outfilename, bindings);
      fail("Expected " + exclass.getSimpleName() + " not caught!");
    } catch (final WatersException exception) {
      checkExceptions(exception, exclass, culprits);
    }
  }

  private void compile(final String dirname, final String name)
    throws IOException, WatersException
  {
    final File root = getWatersInputRoot();
    final File dir = new File(root, dirname);
    compile(dir, name, null, false);
  }

  private void compile(final String dirname,
                       final String subdirname,
                       final String name)
    throws IOException, WatersException
  {
    final File root = getWatersInputRoot();
    final File dir = new File(root, dirname);
    final File subdir = new File(dir, subdirname);
    compile(subdir, name, null, false);
  }

  private void compile(final File dir,
                       final String name,
                       final List<ParameterBindingProxy> bindings,
                       final boolean appendToName)
    throws IOException, WatersException
  {
    final String inextname = name + mModuleMarshaller.getDefaultExtension();
    final File infilename = new File(dir, inextname);
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
    compile(infilename, outfilename, bindings);
    final String suffix = getTestSuffix();
    buffer.setLength(pos);
    buffer.append('-');
    buffer.append(suffix);
    buffer.append(ext);
    final String suffixedname = buffer.toString();
    final File suffixedfilename = new File(dir, suffixedname);
    if (suffixedfilename.exists()) {
      compare(outfilename, suffixedfilename);
    } else {
      final File compfilename = new File(dir, outextname);
      compare(outfilename, compfilename);
    }
  }

  private void compile(final File infilename,
                       final File outfilename,
                       final List<ParameterBindingProxy> bindings)
    throws IOException, WatersException
  {
    final URI uri = infilename.toURI();
    mModule = mModuleMarshaller.unmarshal(uri);
    final ProductDESProxy des = compile(mModule, bindings);
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
    mCompiler =
      new ModuleCompiler(mDocumentManager, mProductDESFactory, module);
    configure(mCompiler);
    return mCompiler.compile(bindings);
  }

  private void compare(final File filename1, final File filename2)
    throws IOException, WatersUnmarshalException
  {
    final URI uri1 = filename1.toURI();
    final URI uri2 = filename2.toURI();
    final DocumentProxy proxy1 = mProductDESMarshaller.unmarshal(uri1);
    final DocumentProxy proxy2 = mProductDESMarshaller.unmarshal(uri2);
    assertProductDESProxyEquals(proxy1, proxy2);
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
  private void checkException(final WatersException exception,
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
                   mDescendantCheckVisitor.isDescendant(location, mModule));
      }
    }
  }

  /**
   * Asserts that the exception fulfills all the requirements.
   * <CODE>MultiEvalExceptions</CODE> are handled appropriately.
   */
  private void checkExceptions(final WatersException exception,
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
        checkException(ex, exclass, culprits);
      }
      for (final String[] culprit : culprits) {
        assertMentioned(culprit, exceptions);
      }
    } else {
      checkException(exception, exclass, culprits[0]);
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
  private ModuleProxy mModule;
  private DescendantCheckVisitor mDescendantCheckVisitor;

}
