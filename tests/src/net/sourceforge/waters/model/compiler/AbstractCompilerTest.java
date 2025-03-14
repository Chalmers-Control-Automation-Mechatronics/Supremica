//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.model.compiler;

import gnu.trove.set.hash.THashSet;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.compiler.context.UndefinedIdentifierException;
import net.sourceforge.waters.model.compiler.efa.ActionSyntaxException;
import net.sourceforge.waters.model.compiler.efsm.EFSMControllabilityException;
import net.sourceforge.waters.model.compiler.graph.NondeterministicModuleException;
import net.sourceforge.waters.model.compiler.instance.EmptyLabelBlockException;
import net.sourceforge.waters.model.compiler.instance.EventKindException;
import net.sourceforge.waters.model.compiler.instance.InstantiationException;
import net.sourceforge.waters.model.compiler.instance.NestedNextException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.MultiEvalException;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.marshaller.SAXProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.DescendingModuleProxyVisitor;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public abstract class AbstractCompilerTest extends AbstractWatersTest
{
  //#########################################################################
  //# Hand-Crafting Test Cases
  public void testCompile_empty_1()
    throws AnalysisException
  {
    final String name = "empty";
    final ModuleProxy module = mModuleFactory.createModuleProxy
      (name, null, null, null, null, null, null);
    final ProductDESProxy des = compile(module);
    assertTrue("Unexpected name!", des.getName().equals(name));
    assertNull("Unexpected location!", des.getLocation());
    assertTrue("Unexpected event!", des.getEvents().isEmpty());
    assertTrue("Unexpected automata!", des.getAutomata().isEmpty());
  }

  public void testCompile_empty_2()
    throws AnalysisException
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
      final String culprit = "'" + instname + "'";
      final ExpectedException expected =
        new ExpectedException(InstantiationException.class, culprit);
      checkExceptions(module, exception, expected);
    }
  }


  //#########################################################################
  //# Successful Test Cases
  public void testCompile_array()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "instance", "array");
    testCompile(module);
  }

  public void testCompile_array2d()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "instance", "array2d");
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
    final ModuleProxy module =
      loadModule("tests", "compiler", "graph", "colours");
    testCompile(module);
  }

  public void testCompile_empty_intrange() throws IOException,
    WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "instance", "empty_intrange");
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

  public void testCompile_foreach_cond() throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "instance", "foreach_cond");
    testCompile(module);
  }

  public void testCompile_instantiate_duplicate_identifiers()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "instance",
                                          "instantiate_duplicate_identifiers");
    testCompile(module);
  }

  public void testCompile_instantiate_order()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "instance", "instantiate_order");
    testCompile(module);
  }

  public void testCompile_machine() throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "machine");
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
    final ModuleProxy module =
      loadModule("tests", "compiler", "graph", "markus2");
    testCompile(module);
  }

  public void testCompile_notDiag2()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "diagnosability", "notDiag_2");
    testCompile(module);
  }

  public void testCompile_order()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "instance", "order");
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

  public void testCompile_selfloop1()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "graph", "selfloop1");
    testCompile(module);
  }

  public void testCompile_selfloop2()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "graph", "selfloop2");
    testCompile(module);
  }

  public void testCompile_spaces()
  throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "instance", "spaces");
    testCompile(module);
  }

  public void testCompile_tictactoe()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "tictactoe");
    testCompile(module);
  }

  public void testCompile_tictactoe_incomplete()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("handwritten", "tictactoe_incomplete");
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

  public void testCompile_undef_event_guarded_1()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "instance", "undef_event_guarded_1");
    testCompile(module);
  }

  public void testCompile_undef_event_guarded_2()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "instance", "undef_event_guarded_2");
    testCompile(module);
  }

  public void testCompile_unused_event()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "nasty", "unused_event");
    testCompile(module);
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
  //# Successful Test Cases using Group Nodes
  public void testCompile_action_conflict_1()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "groupnode", "action_conflict_1");
    compileError(module, ActionSyntaxException.class, "group nodes");
  }

  public void testCompile_action_conflict_2()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "groupnode", "action_conflict_2");
    compileError(module, ActionSyntaxException.class, "group nodes");
  }

  public void testCompile_action_conflict_3()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "groupnode", "action_conflict_3");
    compileError(module, ActionSyntaxException.class, "group nodes");
  }

  public void testCompile_guard_conflict_1()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "groupnode", "guard_conflict_1");
    testCompile(module);
  }

  public void testCompile_guard_conflict_2()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "groupnode", "guard_conflict_2");
    testCompile(module);
  }

  public void testCompile_nested_groups_1()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "groupnode", "nested_groups_1");
    testCompile(module);
  }

  public void testCompile_nested_groups_2()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "groupnode", "nested_groups_2");
    testCompile(module);
  }

  public void testCompile_nodegroup1()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "groupnode", "nodegroup1");
    testCompile(module);
  }

  public void testCompile_nodegroup2()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "groupnode", "nodegroup2");
    testCompile(module);
  }

  public void testCompile_nodegroup3()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "groupnode", "nodegroup3");
    compileError(module, NondeterministicModuleException.class, "'q0'", "'e'");
  }

  public void testCompile_nodegroup4()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "groupnode", "nodegroup4");
    testCompile(module);
  }

  public void testCompile_nodegroup_efa1()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "groupnode", "nodegroup_efa1");
    testCompile(module);
  }

  public void testCompile_nodegroup_efa2()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "groupnode", "nodegroup_efa2");
    testCompile(module);
  }


  //#########################################################################
  //# Successful Test Cases using EFSMs
  public void testCompile_AliceRoom()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "alice_room");
    testCompile(module);
  }

  public void testCompile_AmbiguousVariableStatus()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "AmbiguousVariableStatus");
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
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "batch_tank_vout");
    testCompile(module);
  }

  public void testCompile_blocked_action()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "blocked_action");
    testCompile(module);
  }

  public void testCompile_blocked_efa()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "blocked_efa");
    testCompile(module);
  }

  public void testCompile_blocked_event()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "blocked_event");
    testCompile(module);
  }

  public void testCompile_complex_prime_01()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "complex_prime_01");
    if (isNormalisationEnabled()) {
      testCompile(module);
    } else {
      compileError(module, null, TypeMismatchException.class);
    }
  }

  public void testCompile_conditional01()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "conditional01");
    testCompile(module);
  }

  public void testCompile_controllability1() throws IOException, WatersException
  {
    if (isNormalisationEnabled()) {
      final ModuleProxy module =
        loadModule("tests", "compiler", "efsm", "controllability1");
      final String[] culprit = {"'x'", "'event'"};
      compileError(module, null, EFSMControllabilityException.class, culprit);
    }
  }

  public void testCompile_controllability2() throws IOException, WatersException
  {
    if (isNormalisationEnabled()) {
      final ModuleProxy module =
        loadModule("tests", "compiler", "efsm", "controllability2");
      final String[] culprit = {"'c'", "'cat_move'"};
      compileError(module, null, EFSMControllabilityException.class, culprit);
    }
  }

  public void testCompile_ControllableTestModelEFA()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "ControllableTestModelEFA");
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

  public void testCompile_DosingTankWithJellyEFA1()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("handwritten", "DosingTankWithJellyEFA1");
    testCompile(module);
  }

  public void testCompile_DosingTankWithJellyEFA2()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("handwritten", "DosingTankWithJellyEFA2");
    testCompile(module);
  }

  public void testCompile_EFA0()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "EFA0");
    testCompile(module);
  }

  public void testCompile_EFAJournalExample()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "EFAJournalExample");
    testCompile(module);
  }

  public void testCompile_EFATransferLine()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("efa", "transferline_efa");
    if (isNormalisationEnabled()) {
      final ExpectedException expected1 =
        new ExpectedException(EFSMControllabilityException.class,
                              "'bufferA[1].c'", "'acceptT[0]'");
      final ExpectedException expected2 =
        new ExpectedException(EFSMControllabilityException.class,
                              "'bufferA[1].c'", "'rejectT[1]'");
      final ExpectedException expected3 =
        new ExpectedException(EFSMControllabilityException.class,
                              "'bufferB[1].c'", "'finishM[1]'");
      compileError(module, null, expected1, expected2, expected3);
    } else {
      testCompile(module);
    }
  }

  public void testCompile_EFATransferLineNorm()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("efa", "transferline_norm");
    testCompile(module);
  }

  public void testCompile_enumvar()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "enumvar");
    testCompile(module);
  }

  public void testCompile_forbidden()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "graph", "forbidden");
    testCompile(module);
  }

  public void testCompile_foreach_enum1()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "foreach_enum1");
    testCompile(module);
  }

  public void testCompile_foreach_enum2()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "foreach_enum2");
    testCompile(module);
  }

  public void testCompile_funcall_max()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "funcall_max");
    testCompile(module);
  }

  public void testCompile_funcall_min()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "funcall_min");
    testCompile(module);
  }

  public void testCompile_GlobalAndLocalVariables()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "GlobalAndLocalVariables");
    testCompile(module);
  }

  public void testCompile_increment()
  throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "increment");
    testCompile(module);
  }

  public void testCompile_instantiate_efa()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "instance", "instantiate_efa");
    testCompile(module);
  }

  public void testCompile_io1()
  throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "io1");
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
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "markedvar");
    testCompile(module);
  }

  public void testCompile_martijn1()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "martijn1");
    testCompile(module);
  }

  public void testCompile_nondetvar1()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "nondetvar1");
    testCompile(module);
  }

  public void testCompile_nondetvar2()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "nondetvar2");
    testCompile(module);
  }

  public void testCompile_normalise1() throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "normalise1");
    testCompile(module);
  }

  public void testCompile_normalise2() throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler" ,"efsm", "normalise2");
    testCompile(module);
  }

  public void testCompile_patrik1()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "patrik1");
    testCompile(module);
  }

  public void testCompile_patrik2()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "patrik2");
    testCompile(module);
  }

  public void testCompile_patrik3()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "patrik3");
    testCompile(module);
  }

  public void testCompile_primeAndConst()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "instance", "prime_and_const");
    testCompile(module);
  }

  public void testCompile_profisafe_ihost_nonsubsumptions()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "profisafe_ihost_nonsubsumptions");
    testCompile(module);
  }

  public void testCompile_profisafe_islave_pfork()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "profisafe_islave_pfork");
    testCompile(module);
  }

  public void testCompile_randomEFA()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "random_efa");
    testCompile(module);
  }

  public void testCompile_sahar1()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "sahar1");
    testCompile(module);
  }

  public void testCompile_sahar2()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "sahar2");
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
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "unsat_guard");
    testCompile(module);
  }


  //#########################################################################
  //# Test Cases Using Automaton Variables
  public void testCompile_autvars01()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "autvars01");
    if (isAutomatonVariablesEnabled()) {
      testCompile(module);
    } else {
      compileError(module, UndefinedIdentifierException.class, "'buffer'");
    }
  }

  public void testCompile_autvars02()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "autvars02");
    if (isAutomatonVariablesEnabled()) {
      testCompile(module);
    } else {
      compileError(module, UndefinedIdentifierException.class, "'buffer'");
    }
  }

  public void testCompile_autvars03()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "autvars03");
    if (isAutomatonVariablesEnabled()) {
      testCompile(module);
    } else {
      compileError(module, null, UndefinedIdentifierException.class, "'clock'");
    }
  }

  public void testCompile_autvars04()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "autvars04");
    if (isAutomatonVariablesEnabled()) {
      testCompile(module);
    } else {
      compileError(module, null, UndefinedIdentifierException.class, "'clock'");
    }
  }

  public void testCompile_autvars05()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "autvars05");
    if (isAutomatonVariablesEnabled()) {
      testCompile(module);
    } else {
      compileError(module, UndefinedIdentifierException.class, "'mach1'");
    }
  }

  public void testCompile_autvars06()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "autvars06");
    if (isAutomatonVariablesEnabled()) {
      testCompile(module);
    } else {
      compileError(module, UndefinedIdentifierException.class, "'mach1'");
    }
  }

  public void testCompile_autvars07()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "autvars07");
    if (isNormalisationEnabled() &&
        !isAutomatonVariablesEnabled() &&
        !isOptimizationEnabled()) {
      compileError(module, UndefinedIdentifierException.class, "'clock'");
    } else {
      testCompile(module);
    }
  }

  public void testCompile_autvars08()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "autvars08");
    if (isAutomatonVariablesEnabled()) {
      testCompile(module);
    } else {
      compileError(module, UndefinedIdentifierException.class, "'clock'");
    }
  }

  public void testCompile_autvars09()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "autvars09");
    if (isAutomatonVariablesEnabled()) {
      testCompile(module);
    } else {
      compileError(module, UndefinedIdentifierException.class, "'clock'");
    }
  }

  public void testCompile_autvars10()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "autvars10");
    if (isAutomatonVariablesEnabled()) {
      testCompile(module);
    } else {
      compileError(module, UndefinedIdentifierException.class, "'clock'");
    }
  }

  public void testCompile_autvars11()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "autvars11");
    if (isAutomatonVariablesEnabled()) {
      testCompile(module);
    } else {
      compileError(module, UndefinedIdentifierException.class, "'clock'");
    }
  }

  public void testCompile_autvars12()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "autvars12");
    if (isAutomatonVariablesEnabled()) {
      testCompile(module);
    } else {
      compileError(module, UndefinedIdentifierException.class, "'buffer'");
    }
  }

  public void testCompile_autvars13()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "autvars13");
    if (isAutomatonVariablesEnabled()) {
      testCompile(module);
    } else {
      compileError(module, UndefinedIdentifierException.class, "'buffer'");
    }
  }

  public void testCompile_autvars14()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "autvars14");
    if (isAutomatonVariablesEnabled()) {
      testCompile(module);
    } else {
      compileError(module, UndefinedIdentifierException.class, "'clock'");
    }
  }

  public void testCompile_autvars15()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "autvars15");
    if (isAutomatonVariablesEnabled()) {
      testCompile(module);
    } else {
      compileError(module, UndefinedIdentifierException.class, "'buffer'");
    }
  }

  public void testCompile_autvars16()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "autvars16");
    if (isAutomatonVariablesEnabled()) {
      testCompile(module);
    } else {
      compileError(module, UndefinedIdentifierException.class, "'clock'");
    }
  }

  public void testCompile_autvars17()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "autvars17");
    if (isAutomatonVariablesEnabled()) {
      testCompile(module);
    } else {
      compileError(module, UndefinedIdentifierException.class, "'clock'");
    }
  }

  public void testCompile_duplicate_identifier()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "duplicate_identifier");
    if (isAutomatonVariablesEnabled()) {
      compileError(module, DuplicateIdentifierException.class, "'x'");
    } else {
      testCompile(module);
    }
  }

  public void testCompile_error_batch_tank_out()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "batch_tank_out");
    if (isAutomatonVariablesEnabled()) {
      compileError(module, DuplicateIdentifierException.class, "'out'");
    } else {
      testCompile(module);
    }
  }


  //#########################################################################
  //# Test Cases Expecting Exceptions
  public void testCompile_assignmentInGuard1()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "assignment_in_guard1");
    compileError(module, ActionSyntaxException.class, "Assignment operator =");
  }

  public void testCompile_assignmentInGuard2()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "assignment_in_guard2");
    final ExpectedException expected1 =
      new ExpectedException(ActionSyntaxException.class,
                            "Assignment operator =");
    if (isNormalisationEnabled()) {
      final ExpectedException expected2 =
        new ExpectedException(EFSMControllabilityException.class,
                              "'spec'", "'c'", "'put'");
      compileError(module, null, expected1, expected2);
    } else {
      compileError(module, null, expected1);
    }
  }

  public void testCompile_bad_enum()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "bad_enum");
    compileError(module, TypeMismatchException.class, "'2'");
  }

  public void testCompile_error_double_prime()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "error_double_prime");
    compileError(module, NestedNextException.class, "nested");
  }

  public void testCompile_edge0()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "graph", "edge0");
    compileError(module, EmptyLabelBlockException.class, "q0");
  }

  public void testCompile_error_ims()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "instance", "error_ims");
    compileError(module, UndefinedIdentifierException.class, "'finishLathe'");
  }

  public void testCompile_error_min_array()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "error_min_array");
    compileError(module, UndefinedIdentifierException.class, "'id'");
  }

  public void testCompile_error_prime_in_assignment()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "error_prime_in_assignment");
    compileError(module, NestedNextException.class, "assignment");
  }

  public void testCompile_error_prime_in_index()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "error_prime_in_index");
    compileError(module, NestedNextException.class, "array index");
  }

  public void testCompile_error1_small()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "instance", "error1_small");
    compileError(module, DuplicateIdentifierException.class, "'mach'");
  }

  public void testCompile_error2_small()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "instance", "error2_small");
    final ExpectedException expected1 =
      new ExpectedException(InstantiationException.class,
                            "required parameter 'break'");
    final ExpectedException expected2 =
      new ExpectedException(InstantiationException.class,
                            "required parameter 'repair'");
    compileError(module, null, expected1, expected2);
  }

  public void testCompile_error3_small()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "instance", "error3_small");
    compileError(module, InstantiationException.class, "'finish_after'");
  }

  public void testCompile_error4_small()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "instance", "error4_small");
    final ExpectedException expected1 =
      new ExpectedException(InstantiationException.class, "'start1'");
    final ExpectedException expected2 =
      new ExpectedException(InstantiationException.class, "'start2'");
    compileError(module, null, expected1, expected2);
  }

  public void testCompile_error5_small()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "instance", "error5_small");
    compileError(module, InstantiationException.class, "'finish_before'");
  }

  public void testCompile_error6_small()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "instance", "error6_small");
    compileError(module, InstantiationException.class, "'start2'");
  }

  public void testCompile_error7_small()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "error7_small");
    compileError(module, UndefinedIdentifierException.class, "'buffer.curr'");
  }

  public void testCompile_error8_small()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "graph", "error8_small");
    compileError(module, EventKindException.class, "'repair1'");
  }

  public void testCompile_instantiate_edge()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "instance",
                                          "instantiate_edge");
    compileError(module, InstantiationException.class, "q0");
  }

  public void testCompile_instantiate_error7()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "instance",
                                          "instantiate_error7");
    compileError(module, UndefinedIdentifierException.class, "'buffer.curr");
  }

  public void testCompile_instantiate_error8()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "instance",
                                          "instantiate_error8");
    compileError(module, InstantiationException.class, "'repair1'");
  }

  public void testCompile_instantiate_graph()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "instance", "instantiate_graph");
    compileError(module, NondeterministicModuleException.class,
                 "'s0'", "'nondet_error.a'");
  }

  public void testCompile_instantiate_group()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "instance", "instantiate_group");
    compileError(module, NondeterministicModuleException.class,
                 "'q0'", "'nodegroup3.nodegroup3'");
  }

  public void testCompile_instantiate_guard1()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "instance", "instantiate_guard1");
    compileError(module, InstantiationException.class, "Assignment operator =");
  }

  public void testCompile_instantiate_guard2()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "instance", "instantiate_guard2");
    compileError(module, InstantiationException.class, "Assignment operator =");
  }

  public void testCompile_instantiate_twoinit()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "instance", "instantiate_twoinit");
    compileError(module, NondeterministicModuleException.class, "'Two_Initials.comp'");
  }

  public void testCompile_instantiate_undefvar1()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "instance", "instantiate_undefvar1");
    compileError(module, UndefinedIdentifierException.class, "'undefvar'");
  }

  public void testCompile_instantiate_undefvar2()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "instance", "instantiate_undefvar2");
    compileError(module, UndefinedIdentifierException.class, "'undefvar'");
  }

  public void testCompile_markus1()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "graph", "markus1");
    compileError(module, NondeterministicModuleException.class, "'s0'", "'a'");
  }

  public void testCompile_twoinit()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "graph", "twoinit");
    compileError(module, NondeterministicModuleException.class, "'comp'");
  }

  public void testCompile_undefvar_01()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "undefvar_01");
    compileError(module, UndefinedIdentifierException.class, "'undefvar'");
  }

  public void testCompile_undefvar_02()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "compiler", "efsm", "undefvar_02");
    compileError(module, UndefinedIdentifierException.class, "'undefvar'");
  }

  public void testCompile_verriegel3_error()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "verriegel3_error");
    compileError(module, UndefinedIdentifierException.class, "'x'");
  }


  //#########################################################################
  //# Customisation
  void configure(final ModuleCompiler compiler)
  {
    compiler.setOptimizationEnabled(isOptimizationEnabled());
    compiler.setNormalizationEnabled(isNormalisationEnabled());
    compiler.setAutomatonVariablesEnabled(isAutomatonVariablesEnabled());
  }

  abstract String[] getTestSuffices();

  abstract boolean isOptimizationEnabled();

  abstract boolean isNormalisationEnabled();

  abstract boolean isAutomatonVariablesEnabled();


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
                            final Class<? extends EvalException> exclass,
                            final String... culprits)
    throws IOException, WatersException
  {
    compileError(module, null, exclass, culprits);
  }

  private void compileError(final ModuleProxy module,
                            final List<ParameterBindingProxy> bindings,
                            final Class<? extends EvalException> exclass,
                            final String... culprits)
    throws IOException, WatersException
  {
    final ExpectedException expected =
      new ExpectedException(exclass, culprits);
    compileError(module, bindings, expected);
  }

  protected void compileError(final ModuleProxy module,
                              final List<ParameterBindingProxy> bindings,
                              final ExpectedException... expected)
    throws IOException, WatersException
  {
    assertTrue("Invalid test, does not specify any expected exceptions!",
               expected.length > 0);
    try {
      final ProductDESProxy des = compile(module, bindings);
      save(module, bindings, des);
      fail("Expected " + expected[0].getSimpleName() + " not caught!");
    } catch (final WatersException exception) {
      checkExceptions(module, exception, expected);
    }
  }

  protected void testCompile(final ModuleProxy module)
    throws IOException, WatersException
  {
    testCompile(module, null, false);
  }

  private void testCompile(final ModuleProxy module,
                           final List<ParameterBindingProxy> bindings,
                           final boolean appendToName)
    throws IOException, WatersException
  {
    final ProductDESProxy des;
    try {
      des = compile(module, bindings);
    } catch (final MultiEvalException exception) {
      final List<EvalException> all = exception.getLeafExceptions();
      if (all.size() >= 1) {
        throw all.get(0);
      } else {
        throw exception;
      }
    }

    // Save output
    final List<ParameterBindingProxy> nameBindings =
      appendToName ? bindings : null;
    final String stem = save(module, nameBindings, des);

    // Generate file names for expected result
    final String[] suffices = getTestSuffices();
    final String[] fileNames = {stem + suffices[0] + suffices[1],
                                stem + suffices[1],
                                stem + suffices[0],
                                stem};
    final String ext = mProductDESMarshaller.getDefaultExtension();
    for (int i = 0; i < fileNames.length; i++) {
      fileNames[i] = fileNames[i] + ext;
    }

    // Find the expected file, and compare.
    final File location = module.getFileLocation().getParentFile();
    File expectedFileName = null;
    for (final String fileName : fileNames) {
      expectedFileName = new File(location, fileName);
      if (expectedFileName.exists()) {
        break;
      }
    }
    compare(des, expectedFileName);

    // Test source information.
    if (mCompiler.isSourceInfoEnabled()) {
      mSourceInfoChecker = new SourceInfoCheckVisitor(mCompiler, module, des);
      mSourceInfoChecker.checkSourceInfo();
    }

    // Test back import.
    final ModuleProxy backImport = mBackImporter.importModule(des);
    final ModuleCompiler backCompiler =
      new ModuleCompiler(mDocumentManager, mProductDESFactory, backImport);
    backCompiler.setOptimizationEnabled(false);
    final ProductDESProxy backCompiledDES = backCompiler.compile();
    compare(backCompiledDES, expectedFileName);
  }

  private ProductDESProxy compile(final ModuleProxy module)
    throws AnalysisException
  {
    return compile(module, null);
  }

  private ProductDESProxy compile(final ModuleProxy module,
                                  final List<ParameterBindingProxy> bindings)
    throws AnalysisException
  {
    mCompiler =
      new ModuleCompiler(mDocumentManager, mProductDESFactory, module);
    configure(mCompiler);
    mCompiler.setParameterBindings(bindings);
    return mCompiler.compile();
  }

  private String save(final ModuleProxy module,
                      final List<ParameterBindingProxy> bindings,
                      final ProductDESProxy des)
    throws WatersMarshalException, IOException, MalformedURLException
  {
    // Obtain components of the final file name.
    final String name = module.getName();
    final String ext = mProductDESMarshaller.getDefaultExtension();

    // Manipulate bindings.
    final StringBuilder buffer = new StringBuilder(name);
    if (bindings != null) {
      for (final ParameterBindingProxy binding : bindings) {
        buffer.append('-');
        buffer.append(binding.getExpression().toString());
      }
    }
    final String stem = buffer.toString();

    // Write the output file.
    final File producedFileName = new File(mOutputDirectory, stem + ext);
    mProductDESMarshaller.marshal(des, producedFileName);
    return stem;
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

  private void checkExceptions(final ModuleProxy module,
                               final WatersException caught,
                               final ExpectedException... expected)
  {
    final EvalException caughtEval = (EvalException) caught;
    final List<? extends EvalException> allCaught =
      caughtEval.getLeafExceptions();
    for (final EvalException caught1 : allCaught) {
      final String msg = caught1.getMessage();
      assertNotNull("Caught " + ProxyTools.getShortClassName(caught1) +
                    " without any error message!", msg);
      final Proxy location = caught1.getLocation();
      assertNotNull("Caught " + ProxyTools.getShortClassName(caught1) +
                    " <" + msg + "> provides no location!",
                    location);
      if (mCompiler.isSourceInfoEnabled()) {
        assertTrue("Caught " + ProxyTools.getShortClassName(caught1) + " <" +
                   msg + "> in " + ProxyTools.getShortClassName(location) +
                   " which is not in the module!",
                   mDescendantCheckVisitor.isDescendant(location, module));
      }
    }
    if (mCompiler.isMultiExceptionsEnabled()) {
      assertTrue("Multiple exceptions are enabled, " +
                 "but did not catch a MultiEvalException!",
                 caught instanceof MultiEvalException);
      final Set<AnalysisException> covered = new THashSet<>(allCaught.size());
      for (final ExpectedException expected1 : expected) {
        checkException(caughtEval, expected1, covered);
      }
      if (covered.size() < allCaught.size()) {
        for (final EvalException caught1 : allCaught) {
          if (!covered.contains(caught1)) {
            fail("The caught MultiEvalException contains an unexcepted " +
                 ProxyTools.getShortClassName(caught1) + " <" +
                 caught1.getMessage() + ">!");
          }
        }
      }
    } else {
      assertEquals("Multiple exceptions are diabled, " +
                   "but did not produce exactly one exception!",
                   1, allCaught.size());
      final EvalException firstECaught = allCaught.get(0);
      final ExpectedException firstExpected = expected[0];
      checkException(firstECaught, firstExpected, null);
    }
  }

  private void checkException(final AnalysisException caught,
                              final ExpectedException expected,
                              final Set<AnalysisException> covered)
  {
    boolean found = false;
    for (final AnalysisException caught1 : caught.getLeafExceptions()) {
      if (expected.matches(caught1)) {
        found = true;
        if (covered != null) {
          covered.add(caught1);
        }
      }
    }
    if (!found) {
      fail(expected.getMessage());
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
    mModuleMarshaller = new SAXModuleMarshaller(mModuleFactory, optable);
    mProductDESMarshaller = new SAXProductDESMarshaller(mProductDESFactory);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerMarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mBackImporter = new ProductDESImporter(mModuleFactory, mDocumentManager);
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
    mBackImporter = null;
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
      mForeachBlocks.clear();
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

    private List<ForeachProxy> getForeachBlocks()
    {
      return mForeachBlocks;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
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
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitForeachProxy(final ForeachProxy foreach)
      throws VisitorException
    {
      mForeachBlocks.add(foreach);
      final Object result = super.visitForeachProxy(foreach);
      // The following code will not be executed in case of exception,
      // particularly when the child is found and SUCCESS is thrown.
      final int pos = mForeachBlocks.size() - 1;
      mForeachBlocks.remove(pos);
      return result;
    }

    //#######################################################################
    //# Data Members
    private Proxy mChild;
    private final List<ForeachProxy> mForeachBlocks = new ArrayList<>();

    //#######################################################################
    //# Class Constants
    private static final VisitorException SUCCESS = new VisitorException();
  }


  //#########################################################################
  //# Inner Class: SourceInfoCheckVisitor
  private class SourceInfoCheckVisitor
  {
    //#######################################################################
    //# Constructor
    private SourceInfoCheckVisitor(final ModuleCompiler compiler,
                                   final ModuleProxy input,
                                   final ProductDESProxy output)
    {
      mCompiler = compiler;
      mInputModule = input;
      mOutputDES = output;
      mDescendantChecker = new DescendantCheckVisitor();
    }

    //#######################################################################
    //# Invocation
    private void checkSourceInfo()
    {
      for (final EventProxy event : mOutputDES.getEvents()) {
        visitEventProxy(event);
      }
      for (final AutomatonProxy automaton : mOutputDES.getAutomata()) {
        visitAutomatonProxy(automaton);
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Object visitEventProxy(final EventProxy event)
    {
      final SourceInfo info = mCompiler.getSourceInfoMap().get(event);
      checkInModule(event, info);
      checkExpectedType(event, info, EventDeclProxy.class);
      return null;
    }

    public Object visitAutomatonProxy(final AutomatonProxy aut)
    {
      final SourceInfo info = mCompiler.getSourceInfoMap().get(aut);
      checkInModule(aut, info);
      // Check foreach blocks first, they will get overwritten when
      // descendant checker is called again.
      final List<ForeachProxy> foreachBlocks =
        mDescendantChecker.getForeachBlocks();
      for (final ForeachProxy foreach : foreachBlocks) {
        final SourceInfo root = info.getRoot();
        final BindingContext context = root.getBindingContext();
        final String name = foreach.getName();
        final SimpleIdentifierProxy ident =
          mModuleFactory.createSimpleIdentifierProxy(name);
        if (context == null || context.getBoundExpression(ident) == null) {
          final Proxy rootSource = root.getSourceObject();
          fail("The root source info for the " +
               ProxyTools.getContainerName(aut) + " (" +
               ProxyTools.getContainerName(rootSource) +
               ") is lacking a binding for the foreach-block variable '" +
               name + "'!");
        }
      }
      final Proxy source = info.getSourceObject();
      if (source instanceof SimpleComponentProxy) {
        mCurrentComponent = (SimpleComponentProxy) source;
        for (final StateProxy state : aut.getStates()) {
          visitStateProxy(state);
        }
        for (final TransitionProxy transition : aut.getTransitions()) {
          visitTransitionProxy(transition);
        }
      } else if (source instanceof VariableComponentProxy) {
        // ok
      } else {
        fail("The source object of the AutomatonProxy '" + aut.getName() +
             "' is of type " + ProxyTools.getShortClassName(source) +
             ", but should be SimpleComponentProxy or VariableComponentProxy!");
      }
      return null;
    }

    public Object visitStateProxy(final StateProxy state)
    {
      final SourceInfo info = mCompiler.getSourceInfoMap().get(state);
      checkInModule(state, info);
      checkExpectedType(state, info, SimpleNodeProxy.class);
      final Proxy source = info.getSourceObject();
      if (!mDescendantChecker.isDescendant(source, mCurrentComponent)) {
        fail("The source object of the " +
             ProxyTools.getContainerName(state) + " (" +
             ProxyTools.getContainerName(source) +
             ") is not in the current component " +
             ProxyTools.getContainerName(mCurrentComponent) + "!");
      }
      return null;
    }

    public Object visitTransitionProxy(final TransitionProxy trans)
    {
      final SourceInfo info = mCompiler.getSourceInfoMap().get(trans);
      if (info == null) {
        // Not all selfloops have source info when automaton variables are used
        final StateProxy source = trans.getSource();
        final StateProxy target = trans.getTarget();
        if (!isAutomatonVariablesEnabled() || source != target) {
          final EventProxy event = trans.getEvent();
          fail("Missing source information for transition " +
               source.getName() + "-" + event.getName() + "-> " +
               target.getName() + " in automaton '"+
               mCurrentComponent.getName() + "'!");
        }
      } else {
        checkInModule(trans, info);
        checkExpectedType(trans, info, IdentifierProxy.class);
      }
      return null;
    }

    //#######################################################################
    //# Auxiliary Methods
    private void checkInModule(final Proxy proxy, final SourceInfo info)
    {
      final SourceInfo root = info.getRoot();
      final Proxy rootSource = root.getSourceObject();
      if (!mDescendantChecker.isDescendant(rootSource, mInputModule)) {
        fail("The root source object of the " +
             ProxyTools.getContainerName(proxy) + " (" +
             ProxyTools.getContainerName(rootSource) +
             ") is not in the input module!");
      }
      for (SourceInfo parent = info.getParent();
           parent != null; parent = parent.getParent()) {
        final Proxy parentSource = parent.getSourceObject();
        if (!(parentSource instanceof InstanceProxy)) {
          fail("The source object of the " +
               ProxyTools.getContainerName(proxy) +
               "' refers to the parent " +
               ProxyTools.getContainerName(parentSource) +
               ", which is not of type InstanceProxy as expected!");
        }
      }
    }

    private void checkExpectedType(final Proxy proxy,
                                   final SourceInfo info,
                                   final Class<? extends Proxy> type)
    {
      final Proxy source = info.getSourceObject();
      if (!type.isAssignableFrom(source.getClass())) {
        fail("The source object of the " +
             ProxyTools.getContainerName(proxy) +
             " is not of type " + ProxyTools.getShortClassName(type) +
             " as expected!");
      }
    }

    //#######################################################################
    //# Data Members
    private final ModuleCompiler mCompiler;
    private final ModuleProxy mInputModule;
    private final ProductDESProxy mOutputDES;
    private final DescendantCheckVisitor mDescendantChecker;
    private SimpleComponentProxy mCurrentComponent;
  }


  //#########################################################################
  //# Inner Class ExpectedException
  /**
   * A collection of information about an expected {@link EvalException}.
   * Consists of the class of exception, plus zero or more culprit strings
   * that are all expected to be mentioned in the exception message.
   */
  private static class ExpectedException
  {
    //#######################################################################
    //# Constructor
    private ExpectedException(final Class<? extends EvalException> clazz,
                              final String... culprits)
    {
      mExceptionClass = clazz;
      mCulprits = culprits;
    }

    //#######################################################################
    //# Simple Access
    public String getSimpleName()
    {
      return mExceptionClass.getSimpleName();
    }

    private boolean matches(final AnalysisException exception)
    {
      if (!mExceptionClass.isAssignableFrom(exception.getClass())) {
        return false;
      } else {
        final String msg = exception.getMessage();
        for (final String culprit : mCulprits) {
          if (!msg.contains(culprit)) {
            return false;
          }
        }
        return true;
      }
    }

    private String getMessage()
    {
      final StringBuilder builder =
        new StringBuilder("Did not catch expected ");
      builder.append(mExceptionClass.getSimpleName());
      switch (mCulprits.length) {
      case 0:
        break;
      case 1:
        builder.append(" that mentions the culprit '");
        builder.append(mCulprits[0]);
        builder.append('\'');
        break;
      default:
        builder.append(" that mentions the culprits");
        for (int i = 0; i < mCulprits.length; i++) {
          if (i > 0) {
            if (mCulprits.length > 2) {
              builder.append(',');
            }
            if (i == mCulprits.length - 2) {
              builder.append(" and");
            }
          }
          builder.append(" '");
          builder.append(mCulprits[0]);
          builder.append('\'');
        }
      }
      builder.append('!');
      return builder.toString();
    }

    //#######################################################################
    //# Data Members
    private final Class<? extends EvalException> mExceptionClass;
    private final String[] mCulprits;
  }


  //#########################################################################
  //# Data Members
  private File mOutputDirectory;
  private ModuleProxyFactory mModuleFactory;
  private ProductDESProxyFactory mProductDESFactory;
  private SAXModuleMarshaller mModuleMarshaller;
  private SAXProductDESMarshaller mProductDESMarshaller;
  private DocumentManager mDocumentManager;
  private ProductDESImporter mBackImporter;
  private ModuleCompiler mCompiler;
  private DescendantCheckVisitor mDescendantCheckVisitor;
  private SourceInfoCheckVisitor mSourceInfoChecker;

}
