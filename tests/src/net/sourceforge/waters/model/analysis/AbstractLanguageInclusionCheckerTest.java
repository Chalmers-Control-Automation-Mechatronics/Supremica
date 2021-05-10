//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.model.analysis;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


public abstract class AbstractLanguageInclusionCheckerTest
  extends AbstractModelVerifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public AbstractLanguageInclusionCheckerTest()
  {
  }

  public AbstractLanguageInclusionCheckerTest(final String name)
  {
    super(name);
  }

  //#########################################################################
  //# Test Cases --- handcrafted
  public void testEmpty()
    throws Exception
  {
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final ProductDESProxy des = factory.createProductDESProxy("empty");
    runModelVerifier(des, true);
  }

  public void testReentrant()
    throws Exception
  {
    testTrafficLightac61();
    testDisjointProp2();
    testSmallFactory2();
    testTrafficLightac61();
    testHISC8nd();
  }


  //#########################################################################
  //# Test Cases --- no properties
  public void testSmallFactory2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "small_factory_2.wdes");
    runModelVerifier(des, true);
  }

  public void testTictactoe() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "tictactoe.wdes");
    runModelVerifier(des, true);
  }


  //#########################################################################
  //# Test Cases --- nasty
  public void testAgvCounter() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "agv_certainconf.wmod");
    runModelVerifier(des, false);
  }

  public void testDisjointProp1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "disjoint_prop1.wmod");
    runModelVerifier(des, false);
  }

  public void testDisjointProp2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "disjoint_prop2.wmod");
    runModelVerifier(des, true);
  }

  public void testDropSelfloopLang() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "drop_selfloop_lang.wmod");
    runModelVerifier(des, false);
  }

  public void testEmptyPlantAndProp() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "empty_plant_and_prop.wmod");
    runModelVerifier(des, true);
  }

  public void testEmptyProp() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "empty_prop.wmod");
    runModelVerifier(des, false);
  }

  public void testVerriegel4Counter1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "verriegel4counter1.wmod");
    runModelVerifier(des, false);
  }


  //#########################################################################
  //# Test Cases -- traffic light language inclusion
  public void testTrafficLightac61() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "ac61lang.wmod");
    runModelVerifier(des, false);
  }


  //#########################################################################
  //# Test Cases --- nondeterministic
  public void testHISC8nd() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nondeterministic", "hisc8nd.wmod");
    runModelVerifier(des, false);
  }


  //#########################################################################
  //# Test Cases --- ProfiSAFE
  public void testProfisafeI3HostEFA__neversend0() throws Exception
  {
    testProfisafeI3HostEFA("never_send[0]", false);
  }

  public void testProfisafeI3HostEFA__neversend1() throws Exception
  {
    testProfisafeI3HostEFA("never_send[1]", false);
  }

  public void testProfisafeI3HostEFA__neversend2() throws Exception
  {
    testProfisafeI3HostEFA("never_send[2]", false);
  }

  public void testProfisafeI3HostEFA__neversend3() throws Exception
  {
    testProfisafeI3HostEFA("never_send[3]", false);
  }

  public void testProfisafeI3HostEFA__host_sets_fv_after_host_timeout()
    throws Exception
  {
    testProfisafeI3HostEFA("host_sets_fv_after_host_timeout", true);
  }

  public void testProfisafeI3HostEFA__host_sets_fv_after_host_crc_fault()
    throws Exception
  {
    testProfisafeI3HostEFA("host_sets_fv_after_host_crc_fault", false);
  }

  public void
    testProfisafeI3HostEFA__host_sets_fv_after_host_crc_fault_notinit()
    throws Exception
  {
    testProfisafeI3HostEFA("host_sets_fv_after_host_crc_fault_notinit", true);
  }

  public void
    testProfisafeI3HostEFA__host_sets_fv_after_host_seq_fault_0()
    throws Exception
  {
    testProfisafeI3HostEFA("host_sets_fv_after_host_seq_fault[0]", true);
  }

  public void
    testProfisafeI3HostEFA__host_sets_fv_after_host_seq_fault_3()
    throws Exception
  {
    testProfisafeI3HostEFA("host_sets_fv_after_host_seq_fault[3]", false);
  }

  private void testProfisafeI3HostEFA(final String propname,
                                      final boolean expect)
    throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_ihost_efa_1.wmod";
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", 3);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    runModelVerifier(group, dir, name, bindings, expect, propname);
  }

  public void testProfisafeI4SlaveEFA__neversend()
    throws Exception
  {
    checkProfisafe__neversend("profisafe_islave_efa.wmod");
  }

  public void testProfisafeI4SlaveEFSM__neversend()
    throws Exception
  {
    checkProfisafe__neversend("profisafe_islave_efsm.wmod");
  }

  private void checkProfisafe__neversend(final String name)
    throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final int maxseqno = 4;
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", maxseqno);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    for (int isfv = 0; isfv <= 1; isfv++) {
      final String vname = isfv == 0 ? "PV" : "FV";
      for (int seqno = 0; seqno <= maxseqno; seqno++) {
        for (int s4 = 0; s4 <= 1; s4++) {
          for (int s3 = 0; s3 <= 1; s3++) {
            for (int s2 = 0; s2 <= 1; s2++) {
              final String propname =
                String.format("never_send[%s][%d][%d][%d][%d]",
                              vname, s4, s3, s2, seqno);
              final boolean cansend =
                (s4 == isfv) && (s4 == 1 || (s3 == 0 && s2 == 0));
              runModelVerifier(group, dir, name, bindings, !cansend, propname);
            }
          }
        }
      }
    }
  }


  public void testProfisafeI4SlaveEFA__slave_sets_fv_after_slave_crc_fault_2()
    throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_islave_efa.wmod";
    final int maxseqno = 4;
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", maxseqno);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    for (int seqno = 0; seqno <= maxseqno; seqno++) {
      final String propname =
        String.format("slave_sets_fv_after_slave_crc_fault_2[%d]", seqno);
      runModelVerifier(group, dir, name, bindings, true, propname);
    }
  }

  public void testProfisafeI4SlaveEFA__slave_sets_fv_after_slave_crc_fault_3()
    throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_islave_efa.wmod";
    final int maxseqno = 4;
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", maxseqno);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    for (int seqno = 0; seqno <= maxseqno; seqno++) {
      final String propname =
        String.format("slave_sets_fv_after_slave_crc_fault_3[%d]", seqno);
      runModelVerifier(group, dir, name, bindings, seqno == 0, propname);
    }
  }

  public void testProfisafeI4SlaveEFA__slave_sets_fv_after_slave_timeout_2()
    throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_islave_efa.wmod";
    final int maxseqno = 4;
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", maxseqno);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    for (int seqno = 0; seqno <= maxseqno; seqno++) {
      final String propname =
        String.format("slave_sets_fv_after_slave_timeout_2[%d]", seqno);
      runModelVerifier(group, dir, name, bindings, true, propname);
    }
  }

  public void testProfisafeI4SlaveEFA__slave_sets_fv_after_slave_timeout_3()
    throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_islave_efa.wmod";
    final int maxseqno = 4;
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", maxseqno);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    for (int seqno = 1; seqno <= maxseqno; seqno++) {
      final String propname =
        String.format("slave_sets_fv_after_slave_timeout_3[%d]", seqno);
      runModelVerifier(group, dir, name, bindings, seqno == 0, propname);
    }
  }

  public void testProfisafeI4SlaveEFA__slave_sets_fv_after_slave_seq_fault_2()
    throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_islave_efa.wmod";
    final int maxseqno = 4;
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", maxseqno);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    for (int seqno1 = 0; seqno1 <= maxseqno; seqno1++) {
      for (int seqno2 = 0; seqno2 <= maxseqno; seqno2++) {
        if (seqno1 != seqno2 && seqno1 % maxseqno + 1 != seqno2) {
          final String propname =
            String.format("slave_sets_fv_after_slave_seq_fault_2[%d][%d]",
                          seqno1, seqno2);
          runModelVerifier(group, dir, name, bindings, true, propname);
        }
      }
    }
  }

  public void testProfisafeI4SlaveEFA__slave_sets_fv_after_slave_seq_fault_3()
    throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_islave_efa.wmod";
    final int maxseqno = 4;
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", maxseqno);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    for (int seqno1 = 0; seqno1 <= maxseqno; seqno1++) {
      for (int seqno2 = 0; seqno2 <= maxseqno; seqno2++) {
        if (seqno1 != seqno2 && seqno1 % maxseqno + 1 != seqno2) {
          final String propname =
            String.format("slave_sets_fv_after_slave_seq_fault_3[%d][%d]",
                          seqno1, seqno2);
          runModelVerifier(group, dir, name, bindings, seqno2 == 0, propname);
        }
      }
    }
  }

  public void testProfisafeI4Host__fv_crc() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4_host.wmod";
    final String propname = "HOST__fv_crc__property";
    runModelVerifier(group, dir, name, false, propname);
  }

  public void testProfisafeI4Slave() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4_slave.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testProfisafeO4Host__fv_crc() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o4_host.wmod";
    final String propname = "HOST__fv_crc__property";
    runModelVerifier(group, dir, name, false, propname);
  }

  public void testProfisafeO4Slave() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o4_slave.wmod";
    runModelVerifier(group, dir, name, true);
  }


  //#########################################################################
  //# Test Cases -- HISC
  public void testAIP0Sub1Patch0Coreach0() throws Exception
  {
    runModelVerifier("tests", "hisc", "aip0sub1p0_coreach0.wmod", true);
  }


  //#########################################################################
  //# Test Cases -- Parameterised


  //#########################################################################
  //# Specialised Invocation Code to Check Particular Properties
  protected void runModelVerifier(final String group,
                                  final String name,
                                  final boolean expect,
                                  final String propname)
    throws Exception
  {
    runModelVerifier(group, name, null, expect, propname);
  }

  protected void runModelVerifier(final String group,
                                  final String subdir,
                                  final String name,
                                  final boolean expect,
                                  final String propname)
    throws Exception
  {
    runModelVerifier(group, subdir, name, null, expect, propname);
  }

  protected void runModelVerifier(final String group,
                                  final String name,
                                  final boolean expect,
                                  final List<ParameterBindingProxy> bindings,
                                  final String propname)
    throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runModelVerifier(groupdir, name, bindings, expect, propname);
  }

  protected void runModelVerifier(final String group,
                                  final String subdir,
                                  final String name,
                                  final List<ParameterBindingProxy> bindings,
                                  final boolean expect,
                                  final String propname)
    throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runModelVerifier(groupdir, subdir, name, bindings, expect, propname);
  }

  protected void runModelVerifier(final File groupdir,
                                  final String subdir,
                                  final String name,
                                  final List<ParameterBindingProxy> bindings,
                                  final boolean expect,
                                  final String propname)
    throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runModelVerifier(dir, name, bindings, expect, propname);
  }

  protected void runModelVerifier(final File dir,
                                  final String name,
                                  final List<ParameterBindingProxy> bindings,
                                  final boolean expect,
                                  final String propname)
    throws Exception
  {
    final File filename = new File(dir, name);
    runModelVerifier(filename, bindings, expect, propname);
  }

  protected void runModelVerifier(final File filename,
                                  final List<ParameterBindingProxy> bindings,
                                  final boolean expect,
                                  final String propname)
    throws Exception
  {
    final ProductDESProxy des = getCompiledDES(filename, bindings, propname);
    final String name = des.getName() + '-' + propname;
    final Collection<EventProxy> events = des.getEvents();
    final Collection<AutomatonProxy> automata =
      new LinkedList<AutomatonProxy>();
    boolean propfound = false;
    for (final AutomatonProxy aut : des.getAutomata()) {
      final ComponentKind kind = aut.getKind();
      switch (kind) {
      case PLANT:
      case SPEC:
        automata.add(aut);
        break;
      case PROPERTY:
        if (aut.getName().equals(propname)) {
          automata.add(aut);
          propfound = true;
        }
        break;
      default:
        break;
      }
    }
    assertTrue("Property '" + propname + "' not found!", propfound);
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final ProductDESProxy propdes =
      factory.createProductDESProxy(name, events, automata);
    runModelVerifier(propdes, bindings, expect);
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractAnalysisTest
  protected ProductDESProxy getCompiledDES
    (final File filename,
     final List<ParameterBindingProxy> bindings,
     final String propname)
    throws Exception
  {
    try {
      mPropertyName = propname;
      return getCompiledDES(filename, bindings);
    } finally {
      mPropertyName = null;
    }
  }

  @Override
  protected void configure(final ModuleCompiler compiler)
  {
    final Collection<String> empty = Collections.emptyList();
    compiler.setEnabledPropositionNames(empty);
    if (mPropertyName != null) {
      final Collection<String> prop = Collections.singletonList(mPropertyName);
      compiler.setEnabledPropertyNames(prop);
    }
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  @Override
  protected void checkCounterExample(final ProductDESProxy des,
                                     final CounterExampleProxy counter)
    throws Exception
  {
    super.checkCounterExample(des, counter);
    final SafetyCounterExampleProxy castTest =
      (SafetyCounterExampleProxy) counter;
    final TraceProxy trace = castTest.getTrace();
    final List<TraceStepProxy> steps = trace.getTraceSteps();
    final int len = steps.size();
    final Collection<AutomatonProxy> automata = des.getAutomata();
    boolean rejected = false;
    for (final AutomatonProxy aut : automata) {
      final ComponentKind kind = aut.getKind();
      final int accepted = checkCounterExample(aut, steps);
      switch (kind) {
      case PLANT:
      case SPEC:
        assertTrue("Counterexample not accepted by automaton " +
                   aut.getName() + "!", accepted == len);
        break;
      case PROPERTY:
        assertFalse("Counterexample rejected too early (step " + accepted +
                    ") by property " + aut.getName() + "!",
                    accepted < len - 1);
        rejected |= (accepted == len - 1);
        break;
      default:
        break;
      }
    }
    assertTrue("Counterexample not rejected by any property!", rejected);
  }

  private int checkCounterExample(final AutomatonProxy aut,
                                  final List<TraceStepProxy> counterexample)
  {
    final Collection<EventProxy> events = aut.getEvents();
    final Collection<StateProxy> states = aut.getStates();
    final Collection<TransitionProxy> transitions = aut.getTransitions();
    int steps = 0;
    StateProxy current = null;
    for (final TraceStepProxy step : counterexample) {
      final EventProxy event = step.getEvent();
      final StateProxy preset = step.getStateMap().get(aut);
      if (event == null) { // initial step
        if (preset == null) {
          StateProxy found = null;
          for (final StateProxy state : states) {
            if (state.isInitial()) {
              assertNull
                ("Counterexample specifies no initial state for automaton " +
                 aut.getName() + ", which has more than one initial state!",
                 found);
              found = state;
            }
          }
          if (found == null) {
            return steps;
          }
          current = found;
        } else {
          assertTrue
            ("Initial state " + preset.getName() +
             " in counterexample is not an initial state of automaton " +
             aut.getName() + "!", preset.isInitial());
          current = preset;
        }
      } else { // transition step
        steps++;
        if (events.contains(event)) {
          if (preset == null) {
            StateProxy found = null;
            for (final TransitionProxy trans : transitions) {
              if (trans.getSource() == current && trans.getEvent() == event) {
                assertNull
                  ("Counterexample specifies no successor state for the " +
                   "nondeterministic transition from state " +
                   current.getName() + " with event " + event.getName() +
                   " in automaton " + aut.getName() + "!", found);
                found = trans.getTarget();
              }
            }
            if (found == null) {
              return steps;
            }
            current = found;
          } else {
            boolean found = false;
            for (final TransitionProxy trans : transitions) {
              if (trans.getSource() == current &&
                  trans.getEvent() == event &&
                  trans.getTarget() == preset) {
                found = true;
                break;
              }
            }
            assertTrue
              ("There is no transition from state " + current.getName() +
               " to state " + preset.getName() + " with event " +
               event.getName() + " in automaton " + aut.getName() +
               " as specified in the counterexample!", found);
            current = preset;
          }
        }
      }
    }
    return steps + 1;
  }


  //#########################################################################
  //# Data Members
  private String mPropertyName;

}
