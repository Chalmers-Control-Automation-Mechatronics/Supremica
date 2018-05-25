//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.analysis.hisc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.analysis.AbstractConflictCheckerTest;
import net.sourceforge.waters.model.analysis.AbstractModelVerifierTest;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public abstract class AbstractSICProperty5VerifierTest
  extends AbstractConflictCheckerTest
{

  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractAnalysisTest
  @Override
  protected void configure(final ModuleCompiler compiler)
  {
    super.configure(compiler);
    compiler.setHISCCompileMode(HISCCompileMode.HISC_HIGH);
  }


  //#########################################################################
  //# Test Cases
  // testHISC
  public void testSICProperty5Verifier_hisc0_low1() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc0_low1.wmod", true);
  }

  public void testSICProperty5Verifier_hisc0_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc0_low2.wmod", true);
  }

  // testHISC1
  public void testSICProperty5Verifier_hisc1_low1() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc1_low1.wmod", true);
  }

  public void testSICProperty5Verifier_hisc1_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc1_low2.wmod", true);
  }

  // testHISC10
  public void testSICProperty5Verifier_hisc10_low1() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc10_low1.wmod", true);
  }

  public void testSICProperty5Verifier_hisc12_low1() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc12_low1.wmod", true);
  }

  public void testSICProperty5Verifier_hisc12_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc12_low2.wmod", true);
  }

  /*
   * public void testSICProperty5Verifier_hisc13_low1() throws Exception {
   * runModelVerifier("despot", "testHISC", "hisc13_low1.wmod", false); }
   *
   * public void testSICProperty5Verifier_hisc13_low2() throws Exception {
   * runModelVerifier("despot", "testHISC", "hisc13_low2.wmod", true); }
   */

  public void testSICProperty5Verifier_hisc14_low1() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc14_low1.wmod", true);
  }

  public void testSICProperty5Verifier_hisc14_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc14_low2.wmod", true);
  }

  public void testSICProperty5Verifier_hisc2_low1() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc2_low1.wmod", true);
  }

  public void testSICProperty5Verifier_hisc2_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc2_low2.wmod", true);
  }

  public void testSICProperty5Verifier_hisc3_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc3_low2.wmod", true);
  }

  public void testSICProperty5Verifier_hisc7_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc7_low2.wmod", true);
  }

  public void testSICProperty5Verifier_hisc8_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc8_low2.wmod", false);
  }

  public void testSICProperty5Verifier_hisc9_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc9_low2.wmod", false);
  }

  // ParallelManufacturingExample
  public void testSICProperty5Verifier_parManEg_I_mfb_lowlevel()
  throws Exception
  {
    runModelVerifier("tests", "hisc", "parManEg_I_mfb_lowlevel.wmod", true);
  }

  public void testSICProperty5Verifier_parManEg_I_mfb_lowlevel_multiAnswers()
  throws Exception
  {
    runModelVerifier("tests", "hisc",
                     "parManEg_I_mfb_lowlevel_multiAnswers.wmod", true);
  }

  public void testSICProperty5Verifier_parManEg_I_mfb_lowlevel_multiAnswers_noInterface()
  throws Exception
  {
    runModelVerifier("tests", "hisc",
                     "parManEg_I_mfb_lowlevel_multiAnswers_noInterface.wmod",
                     false);
  }

  public void testSICProperty5Verifier_parManEg_I_mfb_middlelevel()
  throws Exception
  {
    runModelVerifier("tests", "hisc", "parManEg_I_mfb_middlelevel.wmod", true);
  }

  public void testSICProperty5Verifier_parManEg_node1()
  throws Exception
  {
    runModelVerifier("despot", "parallelManufacturingExample", "Node1.wmod", true);
  }

  public void testSICProperty5Verifier_parManEg_node4()
  throws Exception
  {
    runModelVerifier("despot", "parallelManufacturingExample", "Node4.wmod", true);
  }

  // SimpleManufacturingExample
  public void testSICProperty5Verifier_Manuf_Cells()
  throws Exception
  {
    runModelVerifier("despot", "simpleManufacturingExample",
                     "Manuf-Cells.wmod", true);
  }

  // song_aip
  public void testSICProperty5Verifier_aip3_syn_as1() throws Exception
  {
    runModelVerifier("despot", "song_aip", "aip3_syn", "as1.wmod", true);
  }

  public void testSICProperty5Verifier_aip3_syn_as2() throws Exception
  {
    runModelVerifier("despot", "song_aip", "aip3_syn", "as2.wmod", true);
  }

  public void testSICProperty5Verifier_aip3_syn_as3() throws Exception
  {
    runModelVerifier("despot", "song_aip", "aip3_syn", "as3.wmod", true);
  }

  public void testSICProperty5Verifier_aip3_syn_io() throws Exception
  {
    runModelVerifier("despot", "song_aip", "aip3_syn", "io.wmod", true);
  }

  public void testSICProperty5Verifier_aip3_syn_tu1() throws Exception
  {
    runModelVerifier("despot", "song_aip", "aip3_syn", "tu1.wmod", true);
  }

  public void testSICProperty5Verifier_aip3_syn_tu2() throws Exception
  {
    runModelVerifier("despot", "song_aip", "aip3_syn", "tu2.wmod", true);
  }

  public void testSICProperty5Verifier_aip3_syn_tu3() throws Exception
  {
    runModelVerifier("despot", "song_aip", "aip3_syn", "tu3.wmod", true);
  }

  public void testSICProperty5Verifier_aip3_syn_tu4() throws Exception
  {
    runModelVerifier("despot", "song_aip", "aip3_syn", "tu4.wmod", true);
  }

  // tbed_hisc
  public void testSICProperty5Verifier_tbed_hisc_crane1()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "Low Level Crane1.wmod", true);
  }

  public void testSICProperty5Verifier_tbed_hisc_crane2()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "Low Level Crane2.wmod", true);
  }

  public void testSICProperty5Verifier_tbed_hisc_crane3()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "Low Level Crane3.wmod", true);
  }

  public void testSICProperty5Verifier_tbed_hisc_ll2()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "Low Level II.wmod", true);
  }

  public void testSICProperty5Verifier_tbed_hisc_ll46()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "sec46sup.wmod", true);
  }

  public void testSICProperty5Verifier_tbed_hisc_ll57()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "sec57sup.wmod", true);
  }

  public void testSICProperty5Verifier_tbed_hisc_switch3()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "switch3sup.wmod", true);
  }

  public void testSICProperty5Verifier_tbed_hisc_switch8()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "switch8sup.wmod", true);
  }

  // AIP
  public void testSICProperty5Verifier_aip1sub1ld()
  throws Exception
  {
    runModelVerifier("tests", "hisc", "aip1sub1ld.wmod", true);
  }

  public void testSICProperty5Verifier_aip1sub1ld_failsic5()
  throws Exception
  {
    runModelVerifier("tests", "hisc", "aip1sub1ld_failsic5.wmod", false);
  }

  /**
   * Checks the correctness of a conflict counterexample which is converted back
   * to the original model by SICPropertyVBuilder. A conflict counterexample has
   * to be a {@link ConflictTraceProxy}, its event sequence has to be accepted
   * by all automata in the original model. Also the trace must put all
   * interfaces in a state where the answer in question is enabled. Furthermore,
   * when a state has a nondeterministic choice it is verified whether the
   * counter example includes correct state information.
   *
   * @see AbstractModelVerifierTest#checkCounterExample(ProductDESProxy,TraceProxy)
   * @see #createLanguageInclusionChecker(ProductDESProxy,ProductDESProxyFactory)
   */
  @Override
  protected void checkCounterExample(final ProductDESProxy des,
                                     final TraceProxy trace) throws Exception
  {
    final ConflictTraceProxy counterexample = (ConflictTraceProxy) trace;
    final Collection<AutomatonProxy> automata = des.getAutomata();
    final int size = automata.size();
    final Map<AutomatonProxy,StateProxy> tuple =
        new HashMap<AutomatonProxy,StateProxy>(size);
    final EventProxy failedAnswer = getModelVerifier().getFailedAnswer();
    for (final AutomatonProxy aut : automata) {
      final StateProxy state = checkCounterExample(aut, counterexample);
      assertNotNull("Counterexample not accepted by automaton " + aut.getName()
          + "!", state);
      tuple.put(aut, state);

      // tests that in the end state of the trace all interfaces have the answer
      // in question enabled
      if (HISCAttributeFactory.isInterface(aut.getAttributes())) {
        boolean answerEnabled = false;
        final Collection<TransitionProxy> transitions = aut.getTransitions();
        for (final TransitionProxy transition : transitions) {
          if (transition.getSource() == state) {
            if (transition.getEvent() == failedAnswer) {
              answerEnabled = true;
              break;
            }
          }
        }
        if (!answerEnabled) {
          final File filename = saveCounterExample(trace);
          fail("Counterexample leads to a state where the interface "
              + aut.getName() + " does not have the answer event "
              + failedAnswer.getName() + " enabled (trace written to "
              + filename + ")!");
        }
      }
    }

    final ProductDESProxy ldes =
        createLanguageInclusionModel(des, tuple, failedAnswer);
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final LanguageInclusionChecker lchecker =
        createLanguageInclusionChecker(ldes, factory);
    final boolean blocking = lchecker.run();
    if (!blocking) {
      final TraceProxy ltrace = lchecker.getCounterExample();
      final File filename = saveCounterExample(ltrace);
      fail("Counterexample does not lead to a state where the answer event "
          + failedAnswer.getName()
          + " can never be executed (trace written to " + filename + ")!");
    }
  }

  @Override
  protected SICProperty5Verifier getModelVerifier()
  {
    return (SICProperty5Verifier) super.getModelVerifier();
  }

  // #########################################################################
  // # Coreachability Model
  private ProductDESProxy createLanguageInclusionModel(
                                                       final ProductDESProxy des,
                                                       final Map<AutomatonProxy,StateProxy> inittuple,
                                                       final EventProxy answer)
  {
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final Collection<EventProxy> events = des.getEvents();
    final int numevents = events.size();
    final Collection<EventProxy> newevents =
        new ArrayList<EventProxy>(numevents);
    for (final EventProxy oldevent : events) {
      if (oldevent.getKind() != EventKind.PROPOSITION) {
        newevents.add(oldevent);
      }
    }

    final Collection<AutomatonProxy> oldautomata = des.getAutomata();
    final int numaut = oldautomata.size();
    final Collection<AutomatonProxy> newautomata =
        new ArrayList<AutomatonProxy>(numaut + 1);
    for (final AutomatonProxy oldaut : oldautomata) {
      final StateProxy init = inittuple.get(oldaut);
      final AutomatonProxy newaut =
          createLanguageInclusionAutomaton(oldaut, init);
      newautomata.add(newaut);
    }
    final AutomatonProxy prop = createPropertyAutomaton(answer);
    newautomata.add(prop);
    final AutomatonProxy nonLocalEventsDisabled =
        createDisabledNonLocalEventsAutomaton(newevents, answer);
    newautomata.add(nonLocalEventsDisabled);
    final String name = des.getName() + '-' + answer.getName() + "-sic5";
    return factory.createProductDESProxy(name, newevents, newautomata);
  }

  private AutomatonProxy createDisabledNonLocalEventsAutomaton
    (final Collection<EventProxy> events, final EventProxy answer)
  {
    final String name = ":disableNonLocalEvents";
    final Collection<EventProxy> disabledevents =
      new ArrayList<EventProxy>(events.size());
    for (final EventProxy event : events) {
      if (event != answer) {
        final Map<String,String> attribs = event.getAttributes();
        if (HISCAttributeFactory.getEventType(attribs) !=
            HISCAttributeFactory.EventType.DEFAULT) {
          disabledevents.add(event);
        }
      }
    }
    return createDisablingAutomaton(name, ComponentKind.PLANT, disabledevents);
  }

  private AutomatonProxy createLanguageInclusionAutomaton(
                                                          final AutomatonProxy aut,
                                                          final StateProxy newinit)
  {
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final Collection<EventProxy> oldevents = aut.getEvents();
    final int numevents = oldevents.size();
    final Collection<EventProxy> newevents =
        new ArrayList<EventProxy>(numevents);
    for (final EventProxy oldevent : oldevents) {
      if (oldevent.getKind() != EventKind.PROPOSITION) {
        newevents.add(oldevent);
      }
    }

    final Collection<StateProxy> oldstates = aut.getStates();
    final int numstates = oldstates.size();
    final Collection<StateProxy> newstates =
        new ArrayList<StateProxy>(numstates);
    final Map<StateProxy,StateProxy> statemap =
        new HashMap<StateProxy,StateProxy>(numstates);
    final Collection<TransitionProxy> oldtransitions = aut.getTransitions();
    final int numtrans = oldtransitions.size();
    final Collection<TransitionProxy> newtransitions =
        new ArrayList<TransitionProxy>(numtrans);
    for (final StateProxy oldstate : oldstates) {
      final String statename = oldstate.getName();
      final StateProxy newstate =
          factory.createStateProxy(statename, oldstate == newinit, null);
      newstates.add(newstate);

      statemap.put(oldstate, newstate);
    }
    for (final TransitionProxy oldtrans : oldtransitions) {
      final StateProxy oldsource = oldtrans.getSource();
      final StateProxy newsource = statemap.get(oldsource);
      final StateProxy oldtarget = oldtrans.getTarget();
      final StateProxy newtarget = statemap.get(oldtarget);
      final EventProxy event = oldtrans.getEvent();
      final TransitionProxy newtrans =
          factory.createTransitionProxy(newsource, event, newtarget);
      newtransitions.add(newtrans);
    }
    final String autname = aut.getName();
    final ComponentKind kind = aut.getKind();
    return factory.createAutomatonProxy(autname, kind, newevents, newstates,
                                        newtransitions);
  }

}
