//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.hisc
//# CLASS:   AbstractSICProperty6VerifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.hisc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import net.sourceforge.waters.analysis.hisc.HISCAttributeFactory;
import net.sourceforge.waters.analysis.hisc.SICPropertyBuilder;
import net.sourceforge.waters.model.analysis.AbstractConflictCheckerTest;
import net.sourceforge.waters.model.analysis.AbstractModelVerifierTest;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;


public abstract class AbstractSICProperty6VerifierTest extends
    AbstractConflictCheckerTest
{

  //#########################################################################
  //# Test Cases
  // testHISC
  public void testSICProperty6Verifier_hisc0_low1() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc0_low1.wmod", true);
  }

  public void testSICProperty6Verifier_hisc0_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc0_low2.wmod", true);
  }

  // testHISC1
  public void testSICProperty6Verifier_hisc1_low1() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc1_low1.wmod", true);
  }

  public void testSICProperty6Verifier_hisc1_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc1_low2.wmod", true);
  }

  // testHISC10
  public void testSICProperty6Verifier_hisc10_low1() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc10_low1.wmod", false);
  }

  public void testSICProperty6Verifier_hisc12_low1() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc12_low1.wmod", true);
  }

  public void testSICProperty6Verifier_hisc12_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc12_low2.wmod", true);
  }

  /*
   * public void testSICProperty6Verifier_hisc13_low1() throws Exception {
   * runModelVerifier("despot", "testHISC", "hisc13_low1.wmod", false); }
   *
   * public void testSICProperty6Verifier_hisc13_low2() throws Exception {
   * runModelVerifier("despot", "testHISC", "hisc13_low2.wmod", true); }
   */

  public void testSICProperty6Verifier_hisc14_low1() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc14_low1.wmod", true);
  }

  public void testSICProperty6Verifier_hisc14_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc14_low2.wmod", true);
  }

  public void testSICProperty6Verifier_hisc2_low1() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc2_low1.wmod", false);
  }

  public void testSICProperty6Verifier_hisc2_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc2_low2.wmod", true);
  }

  public void testSICProperty6Verifier_hisc3_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc3_low2.wmod", true);
  }

  public void testSICProperty6Verifier_hisc7_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc7_low2.wmod", false);
  }

  public void testSICProperty6Verifier_hisc8_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc8_low2.wmod", true);
  }

  public void testSICProperty6Verifier_hisc9_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc9_low2.wmod", true);
  }

  // ParallelManufacturingExample
  public void testSICProperty6Verifier_parManEg_I_mfb_lowlevel()
  throws Exception
  {
    runModelVerifier("tests", "hisc", "parManEg_I_mfb_lowlevel.wmod", true);
  }

  public void testSICProperty6Verifier_parManEg_I_mfb_lowlevel_multiAnswers()
  throws Exception
  {
    runModelVerifier("tests", "hisc",
                     "parManEg_I_mfb_lowlevel_multiAnswers.wmod", true);
  }

  public void testSICProperty6Verifier_parManEg_I_mfb_middlelevel()
  throws Exception
  {
    runModelVerifier("tests", "hisc", "parManEg_I_mfb_middlelevel.wmod", true);
  }

  public void testSICProperty6Verifier_parManEg_I_mfb_parManEg_I_mfb_lowlevel_multiAnswers_noInterface()
  throws Exception
  {
    runModelVerifier("tests", "hisc",
                     "parManEg_I_mfb_lowlevel_multiAnswers_noInterface.wmod",
                     false);
  }

  // SimpleManufacturingExample
  public void testSICProperty6Verifier_Manuf_Cells() throws Exception
  {
    runModelVerifier("despot", "simpleManufacturingExample",
                     "Manuf-Cells.wmod", true);
  }

  // song_aip
  public void testSICProperty6Verifier_aip3_syn_as1() throws Exception
  {
    runModelVerifier("despot", "song_aip/aip3_syn", "as1.wmod", true);
  }

  public void testSICProperty6Verifier_aip3_syn_as2() throws Exception
  {
    runModelVerifier("despot", "song_aip/aip3_syn", "as2.wmod", true);
  }

  public void testSICProperty6Verifier_aip3_syn_as3() throws Exception
  {
    runModelVerifier("despot", "song_aip/aip3_syn", "as3.wmod", true);
  }

  public void testSICProperty6Verifier_aip3_syn_io() throws Exception
  {
    runModelVerifier("despot", "song_aip/aip3_syn", "io.wmod", true);
  }

  public void testSICProperty6Verifier_aip3_syn_tu1() throws Exception
  {
    runModelVerifier("despot", "song_aip/aip3_syn", "tu1.wmod", true);
  }

  public void testSICProperty6Verifier_aip3_syn_tu2() throws Exception
  {
    runModelVerifier("despot", "song_aip/aip3_syn", "tu2.wmod", true);
  }

  public void testSICProperty6Verifier_aip3_syn_tu3() throws Exception
  {
    runModelVerifier("despot", "song_aip/aip3_syn", "tu3.wmod", true);
  }

  public void testSICProperty6Verifier_aip3_syn_tu4() throws Exception
  {
    runModelVerifier("despot", "song_aip/aip3_syn", "tu4.wmod", true);
  }

  // tbed_hisc
  public void testSICProperty6Verifier_tbed_hisc_crane1()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "Low Level Crane1.wmod", true);
  }

  public void testSICProperty6Verifier_tbed_hisc_crane2()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "Low Level Crane2.wmod", true);
  }

  public void testSICProperty6Verifier_tbed_hisc_crane3()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "Low Level Crane3.wmod", true);
  }

  public void testSICProperty6Verifier_tbed_hisc_ll2()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "Low Level II.wmod", true);
  }

  public void testSICProperty6Verifier_tbed_hisc_ll46()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "Low Level Sec4-6.wmod", true);
  }

  public void testSICProperty6Verifier_tbed_hisc_ll57()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "Low Level Sec5-7.wmod", true);
  }

  public void testSICProperty6Verifier_tbed_hisc_switch3()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "Low Level Switch3.wmod", true);
  }

  public void testSICProperty6Verifier_tbed_hisc_switch8()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "Low Level Switch8.wmod", true);
  }

  // rhone_subsystem1_ld
  public void testSICProperty6Verifier_rhone_subsystem1_ld()
  throws Exception
  {
    runModelVerifier("tests", "hisc", "rhone_subsystem1_ld.wmod", true);
  }


  //#########################################################################
  //# Counterexample Verification
  /**
   * Checks the correctness of a conflict counterexample which is converted back
   * to the original model by {@link SICPropertyBuilder}. A SIC Property VI
   * counterexample has to be a {@link ConflictTraceProxy}, and it has to be
   * accepted by all automata in the model, and it has to take them to a state
   * from which no marked state can be reached using only local (non-interface)
   * events. In addition, the trace must put all interfaces in a state marked by
   * the default marking proposition.
   *
   * @see AbstractModelVerifierTest#checkCounterExample(ProductDESProxy,TraceProxy)
   */
  protected void checkCounterExample(final ProductDESProxy des,
                                     final TraceProxy trace) throws Exception
  {
    final ConflictTraceProxy conflictTrace = (ConflictTraceProxy) trace;
    super.checkCounterExample(des, conflictTrace);

    final EventProxy marking =
      findEvent(des, EventDeclProxy.DEFAULT_MARKING_NAME);
    final Collection<AutomatonProxy> automata = des.getAutomata();
    for (final AutomatonProxy aut : automata) {
      final Map<String,String> attribs = aut.getAttributes();
      if (HISCAttributeFactory.isInterface(attribs)) {
        final StateProxy state = checkCounterExample(aut, conflictTrace);
        final Collection<EventProxy> props = state.getPropositions();
        assertTrue("Counterexample takes interface automaton " +
                   aut.getName() + " to state " + state.getName() +
                   ", which is not marked!",
                   props.contains(marking));
      }
    }
  }

  protected ProductDESProxy createLanguageInclusionModel
    (final ProductDESProxy des, final Map<AutomatonProxy,StateProxy> inittuple)
  {
    final Collection<EventProxy> events = des.getEvents();
    final Collection<EventProxy> iface = new ArrayList<EventProxy>();
    for (final EventProxy event : events) {
      final Map<String,String> attribs = event.getAttributes();
      if (HISCAttributeFactory.getEventType(attribs) !=
          HISCAttributeFactory.EventType.DEFAULT) {
        iface.add(event);
      }
    }
    return createLanguageInclusionModel(des, inittuple, iface);
  }

}
