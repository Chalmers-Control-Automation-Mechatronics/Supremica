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

package net.sourceforge.waters.model.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.hisc.HISCAttributeFactory;
import net.sourceforge.waters.analysis.hisc.HISCCompileMode;
import net.sourceforge.waters.analysis.hisc.SICPropertyBuilder;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.EventKind;


public abstract class AbstractGeneralisedConflictCheckerTest
  extends AbstractConflictCheckerTest
{

  //#########################################################################
  //# Constructors
  public AbstractGeneralisedConflictCheckerTest()
  {
  }

  public AbstractGeneralisedConflictCheckerTest(final String name)
  {
    super(name);
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mBuilder = new SICPropertyBuilder(factory);
  }

  @Override
  protected void tearDown() throws Exception
  {
    mBuilder = null;
    super.tearDown();
  }


  //#########################################################################
  //# Test Cases --- paper (multi-coloured automata)
  public void testG1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "generalisedNonblocking", "g1.wmod");
    runModelVerifier(des, true);
  }

  public void testG2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "generalisedNonblocking", "g2.wmod");
    runModelVerifier(des, true);
  }

  public void testG3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "generalisedNonblocking", "g3.wmod");
    runModelVerifier(des, false);
  }

  public void testG4() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "generalisedNonblocking", "g4.wmod");
    runModelVerifier(des, false);
  }


  //#########################################################################
  //# Test Cases --- Nasty
  public void testCertainConf7() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "certainconf7.wmod");
    runModelVerifier(des, false);
  }

  public void testCertainConf8() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "certainconf8.wmod");
    runModelVerifier(des, false);
  }

  public void testDeterminisation26Counter1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "determinisation_26_counter1.wmod");
    runModelVerifier(des, false);
  }

  public void testDeterminisation26Counter2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "determinisation_26_counter2.wmod");
    runModelVerifier(des, false);
  }

  public void testDisjoint1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "disjoint1.wmod");
    runModelVerifier(des, true);
  }

  public void testDisjoint2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "disjoint2.wmod");
    runModelVerifier(des, true);
  }

  public void testDisjoint3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "disjoint3.wmod");
    runModelVerifier(des, false);
  }

  public void testDisjoint4() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "disjoint4.wmod");
    runModelVerifier(des, true);
  }

  public void testOmegaSat01() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "omega_sat_01.wmod");
    runModelVerifier(des, false);
  }

  public void testOmegaSat02() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "omega_sat_02.wmod");
    runModelVerifier(des, false);
  }

  public void testOmegaSat03() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "omega_sat_03.wmod");
    runModelVerifier(des, false);
  }

  public void testTwoInit3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "twoinit3.wmod");
    runModelVerifier(des, true);
  }


  //#########################################################################
  //# Test Cases --- SIC Property V
  public void testSIC5__hisc8_low2__a1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledHighLevel("despot", "testHISC", "hisc8_low2.wmod");
    testSICPropertyV(des, "a1", true);
  }

  public void testSIC5__hisc8_low2__a2_2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledHighLevel("despot", "testHISC", "hisc8_low2.wmod");
    testSICPropertyV(des, "a2:2", false);
  }

  public void testSIC5__parManEg_I_mfb_lowlevel() throws Exception
  {
    final ProductDESProxy des =
      getCompiledHighLevel("tests", "hisc", "parManEg_I_mfb_lowlevel.wmod");
    testSICPropertyV(des, "fin_exit", true);
  }

  public void testSIC5__parManEg_I_mfb_lowlevel_multiAnswers__compl_pol()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledHighLevel("tests", "hisc",
                           "parManEg_I_mfb_lowlevel_multiAnswers.wmod");
    testSICPropertyV(des, "compl_pol", true);
  }

  public void testSIC5__parManEg_I_mfb_lowlevel_multiAnswers__finA_attch()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledHighLevel("tests", "hisc",
                           "parManEg_I_mfb_lowlevel_multiAnswers.wmod");
    testSICPropertyV(des, "finA_attch", true);
  }

  public void testSIC5__parManEg_I_mfb_lowlevel_multiAnswers__finB_attch()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledHighLevel("tests", "hisc",
                           "parManEg_I_mfb_lowlevel_multiAnswers.wmod");
    testSICPropertyV(des, "finB_attch", true);
  }

  public void testSIC5__parManEg_I_mfb_lowlevel_multiAnswers__compl_case()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledHighLevel("tests", "hisc",
                           "parManEg_I_mfb_lowlevel_multiAnswers.wmod");
    testSICPropertyV(des, "compl_case", true);
  }

  public void testSIC5__parManEg_I_mfb_lowlevel_multiAnswers_noInterface__compl_pol()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledHighLevel("tests", "hisc",
                           "parManEg_I_mfb_lowlevel_multiAnswers_noInterface.wmod");
    testSICPropertyV(des, "compl_pol", false);
  }

  public void testSIC5__parManEg_I_mfb_lowlevel_multiAnswers_noInterface__finA_attch()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledHighLevel("tests", "hisc",
                           "parManEg_I_mfb_lowlevel_multiAnswers_noInterface.wmod");
    testSICPropertyV(des, "finA_attch", false);
  }

  public void testSIC5__parManEg_I_mfb_lowlevel_multiAnswers_noInterface__finB_attch()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledHighLevel("tests", "hisc",
                           "parManEg_I_mfb_lowlevel_multiAnswers_noInterface.wmod");
    testSICPropertyV(des, "finB_attch", false);
  }

  public void testSIC5__parManEg_I_mfb_lowlevel_multiAnswers_noInterface__compl_case()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledHighLevel("tests", "hisc",
                           "parManEg_I_mfb_lowlevel_multiAnswers_noInterface.wmod");
    testSICPropertyV(des, "compl_case", false);
  }

  public void testSIC5__parManEg_I_mfb_middlelevel() throws Exception
  {
    final ProductDESProxy des =
      getCompiledHighLevel("tests", "hisc",
                           "parManEg_I_mfb_middlelevel.wmod");
    testSICPropertyV(des, "fin_exit", true);
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  @Override
  protected void configureModelVerifier(final ProductDESProxy des)
  {
    super.configureModelVerifier(des);
    final Set<EventProxy> events = des.getEvents();
    // checks that this des does include the precondition marking :alpha
    for (final EventProxy event : events) {
      if (event.getName().equals(":alpha")
          && event.getKind() == EventKind.PROPOSITION) {
        mAlpha = event;
        final ConflictChecker modelVer = getModelVerifier();
        modelVer.setConfiguredPreconditionMarking(event);
        return;
      }
    }
    fail("Model '" + des.getName() +
         "' does not contain a proposition named :alpha.");
  }

  @Override
  protected void configure(final ModuleCompiler compiler)
  {
    super.configure(compiler);
    final ArrayList<String> propositions = new ArrayList<String>(2);
    propositions.add(":alpha");
    propositions.add(EventDeclProxy.DEFAULT_MARKING_NAME);
    compiler.setEnabledPropositionNames(propositions);
    compiler.setHISCCompileMode(mHISCCompileMode);
  }

  @Override
  protected StateProxy checkTrace(final AutomatonProxy aut,
                                  final TraceProxy trace)
  {
    final StateProxy endstate = super.checkTrace(aut, trace);
    if (endstate != null &&
        !endstate.getPropositions().contains(mAlpha) &&
        aut.getEvents().contains(mAlpha)) {
      fail("Counterexample takes automaton '" + aut.getName() +
           "' to state '" + endstate.getName() +
           "', which is not marked by the precondition '" +
           mAlpha.getName() + "'!");
    }
    return endstate;
  }


  //#########################################################################
  //# Testing SIC Property V
  private ProductDESProxy getCompiledHighLevel(final String... path)
    throws Exception
  {
    try {
      mHISCCompileMode = HISCCompileMode.HISC_HIGH;
      return getCompiledDES(path);
    } finally {
      mHISCCompileMode = HISCCompileMode.NOT_HISC;
    }
  }

  private void testSICPropertyV(final ProductDESProxy des,
                                final String eventName,
                                final boolean expect)
    throws Exception
  {
    mBuilder.setInputModel(des);
    final EventProxy answer = findAnswerEvent(des, eventName);
    final ProductDESProxy answerDES = mBuilder.createSIC5Model(answer);
    final DocumentManager docman = getDocumentManager();
    final ProxyMarshaller<ProductDESProxy> marshaller =
      docman.findProxyMarshaller(ProductDESProxy.class);
    final String ext = marshaller.getDefaultExtension();
    final File outdir = getOutputDirectory();
    final String outname = answerDES.getName();
    final File outfile = new File(outdir, outname + ext);
    docman.saveAs(answerDES, outfile);
    runModelVerifier(answerDES, expect);
  }


  //#########################################################################
  //# Auxiliary Methods
  private EventProxy findAnswerEvent(final ProductDESProxy des,
                                     final String eventName)
  {
    final EventProxy event = findEvent(des, eventName);
    final Map<String,String> attribs = event.getAttributes();
    if (HISCAttributeFactory.getEventType(attribs) !=
        HISCAttributeFactory.EventType.ANSWER) {
      fail("The event '" + eventName + "' in model '" + des.getName() +
           "'is not an answer event!");
    }
    return event;
  }


  //#########################################################################
  //# Data Members
  private HISCCompileMode mHISCCompileMode = HISCCompileMode.NOT_HISC;
  private EventProxy mAlpha = null;
  private SICPropertyBuilder mBuilder;

}
