//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.gnonblocking;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * This class runs experiments using the CompositionalGeneralisedConflictChecker
 * with a variety of configurations. The heuristics for choosing candidates are
 * varied, as well as the abstraction rules applied and the order of them.
 *
 * @author Rachel Francis
 */
public abstract class CompositionalConflictCheckerExperiments extends
    AbstractAnalysisTest
{

  //#########################################################################
  //# Constructor
  public CompositionalConflictCheckerExperiments
    (final String statsFilename,
     final String preselectingHeuristic,
     final String selectingHeuristic,
     final int rules)
  throws FileNotFoundException
  {
    final String outputprop = System.getProperty("waters.test.outputdir");
    final File dir = new File(outputprop);
    ensureDirectoryExists(dir);
    final File statsFile = new File(dir, statsFilename);
    mOut = new FileOutputStream(statsFile);
    mPrintWriter = null;
    mPreselecting = preselectingHeuristic.toLowerCase();
    mSelecting = selectingHeuristic.toLowerCase();
    mRules = rules;
    mRuleCount = 0;
    if (mRules == 1 || mRules == 2) {
      mRuleCount = 8;
    } else if (mRules == 3) {
      mRuleCount = 10;
    } else if (mRules == 5) {
      mRuleCount = 7;
    }
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mVerifier = new CompositionalGeneralisedConflictChecker(factory);
    mPrintWriter = new PrintWriter(mOut, true);
    final int internalStateLimit = 5000;
    mVerifier.setInternalStepNodeLimit(internalStateLimit);
    final int internalTransitionLimit = 1000000;
    mVerifier.setInternalStepTransitionLimit(internalTransitionLimit);
    final int finalStateLimit = 40000000;
    mVerifier.setFinalStepNodeLimit(finalStateLimit);
    final int finalTransitionLimit = 0;
    mVerifier.setFinalStepTransitionLimit(finalTransitionLimit);
    mPrintWriter.println("InternalStateLimit," + internalStateLimit
        + ",InternalTransitionLimit," + internalTransitionLimit
        + ",FinalStateLimit," + finalStateLimit + ",FinalTransitionLimit,"
        + finalTransitionLimit);

    setPreselectingHeuristic();
    setSelectingHeuristic();

    mPrintWriter.println("PreselHeuristic," + mPreselecting
        + ",SelecHeuristic," + mSelecting);

    mStats = new CompositionalGeneralisedConflictCheckerVerificationResult();
    mPrintWriter.print("Model,");
    mStats.printCSVHorizontalHeadings(mPrintWriter, mRuleCount);
  }

  protected void setPreselectingHeuristic()
  {
    // sets correct preselecting heuristic
    if (mPreselecting.equals("mint")) {
      mVerifier.setPreselectingHeuristic(mVerifier.createHeuristicMinT());
    } else if (mPreselecting.equals("maxs")) {
      mVerifier.setPreselectingHeuristic(mVerifier.createHeuristicMaxS());
    } else if (mPreselecting.equals("mustl")) {
      mVerifier.setPreselectingHeuristic(mVerifier.createHeuristicMustL());
    } else {
      System.err
          .println("Error: Preselecting Heuristic not specified correctly, it must be one of: mint, maxs, mustl");
    }
  }

  protected void setSelectingHeuristic()
  {
    // sets correct selecting heuristic
    if (mSelecting.equals("maxl")) {
      mVerifier.setSelectingHeuristic(mVerifier.createHeuristicMaxL());
    } else if (mSelecting.equals("maxlt")) {
      mVerifier.setSelectingHeuristic(mVerifier.createHeuristicMaxLt());
    } else if (mSelecting.equals("maxc")) {
      mVerifier.setSelectingHeuristic(mVerifier.createHeuristicMaxC());
    } else if (mSelecting.equals("maxct")) {
      mVerifier.setSelectingHeuristic(mVerifier.createHeuristicMaxCt());
    } else if (mSelecting.equals("mins")) {
      mVerifier.setSelectingHeuristic(mVerifier.createHeuristicMinS());
    } else if (mSelecting.equals("minsc")) {
      mVerifier.setSelectingHeuristic(mVerifier.createHeuristicMinSCommon());
    } else if (mSelecting.equals("maxlt")) {
      mVerifier.setSelectingHeuristic(mVerifier
          .createHeuristicMaxLOnTransitions());
    } else if (mSelecting.equals("maxlc")) {
      mVerifier.setSelectingHeuristic(mVerifier
          .createHeuristicMaxCOnTransitions());
    } else {
      System.err
          .println("Error: Selecting Heuristic not specified correctly, it must be one of: maxl, maxlt, maxc, maxct, mins");
    }
  }

  @Override
  protected void tearDown() throws Exception
  {
    mVerifier = null;
    mPrintWriter.close();
    mOut.close();
    System.out.println("All experiments complete");
    super.tearDown();
  }

  protected void configureModelVerifier(final ProductDESProxy des)
      throws EventNotFoundException
  {
    mVerifier.setModel(des);
    // sets order of abstraction rules
    final List<AbstractionRule> ruleList = new LinkedList<AbstractionRule>();
    final ProductDESProxyFactory factory = mVerifier.getFactory();
    final EventProxy alpha = mVerifier.getUsedPreconditionMarkingProposition();
    mVerifier.setConfiguredPreconditionMarking(alpha);
    final EventProxy omega = mVerifier.getUsedDefaultMarking();
    final List<EventProxy> propositions = new ArrayList<EventProxy>(2);
    propositions.add(alpha);
    propositions.add(omega);
    final KindTranslator translator = mVerifier.getKindTranslator();
    final TauLoopRemovalRule tlrRule =
        new TauLoopRemovalRule(factory, translator, propositions);

    final ObservationEquivalenceRule oeRule =
        new ObservationEquivalenceRule(factory, translator, propositions);
    oeRule.setTransitionLimit(mVerifier.getInternalStepTransitionLimit());

    final RemovalOfAlphaMarkingsRule ramRule =
        new RemovalOfAlphaMarkingsRule(factory, translator, propositions);
    ramRule.setAlphaMarking(alpha);

    final RemovalOfDefaultMarkingsRule rdmRule =
        new RemovalOfDefaultMarkingsRule(factory, translator, propositions);
    rdmRule.setAlphaMarking(alpha);
    rdmRule.setDefaultMarking(omega);

    final RemovalOfNoncoreachableStatesRule rnsRule =
        new RemovalOfNoncoreachableStatesRule(factory, translator,
                                              propositions);
    rnsRule.setAlphaMarking(alpha);
    rnsRule.setDefaultMarking(omega);

    final DeterminisationOfNonAlphaStatesRule dnasRule =
        new DeterminisationOfNonAlphaStatesRule(factory, translator,
                                                propositions);
    dnasRule.setAlphaMarking(alpha);
    dnasRule.setTransitionLimit(mVerifier.getInternalStepTransitionLimit());

    final RemovalOfTauTransitionsLeadingToNonAlphaStatesRule rttlnsRule =
        new RemovalOfTauTransitionsLeadingToNonAlphaStatesRule(factory,
                                                               translator,
                                                               propositions);
    rttlnsRule.setAlphaMarking(alpha);

    final RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule rttonsRule =
        new RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule
          (factory, translator, propositions);
    rttonsRule.setAlphaMarking(alpha);
    rttonsRule.setDefaultMarking(omega);
    if (mRules == 1) {
      // order the rules are presented in the paper
      ruleList.add(tlrRule);
      ruleList.add(oeRule);
      ruleList.add(ramRule);
      ruleList.add(rdmRule);
      ruleList.add(rnsRule);
      ruleList.add(dnasRule);
      ruleList.add(rttlnsRule);
      ruleList.add(rttonsRule);
    } else if (mRules == 2) {
      // observation equivalence last
      ruleList.add(ramRule);
      ruleList.add(rdmRule);
      ruleList.add(rnsRule);
      ruleList.add(dnasRule);
      ruleList.add(rttlnsRule);
      ruleList.add(rttonsRule);
      ruleList.add(tlrRule);
      ruleList.add(oeRule);
    } else if (mRules == 3) {
      // observation equivalence first and last
      ruleList.add(tlrRule);
      ruleList.add(oeRule);
      ruleList.add(ramRule);
      ruleList.add(rdmRule);
      ruleList.add(rnsRule);
      ruleList.add(dnasRule);
      ruleList.add(rttlnsRule);
      ruleList.add(rttonsRule);
      ruleList.add(tlrRule);
      ruleList.add(oeRule);
    } else if (mRules == 4) {
      // ordering i came up with while writing report
      ruleList.add(tlrRule);
      ruleList.add(oeRule);
      ruleList.add(ramRule);
      ruleList.add(rdmRule);
      ruleList.add(dnasRule);
      ruleList.add(rttonsRule);
      ruleList.add(rttlnsRule);
      ruleList.add(rnsRule);
    } else if (mRules == 5) {
      // dont apply removal of tau leading to, since it increases number of
      // transitions
      ruleList.add(tlrRule);
      ruleList.add(oeRule);
      ruleList.add(ramRule);
      ruleList.add(rdmRule);
      ruleList.add(rnsRule);
      ruleList.add(dnasRule);
      ruleList.add(rttonsRule);
    } else {
      System.err
          .println("Error: Rules must be specified by specifying the integer code of the ordered list wanted.");
    }
    mVerifier.setAbstractionRules(ruleList);
  }

  protected void runModel(final String group, final String subdir,
                          final String name) throws Exception
  {
    System.out.println("Running " + name + " ...");
    final String inputprop = System.getProperty("waters.test.inputdir");
    final File inputRoot = new File(inputprop);
    final File rootdir = new File(inputRoot, "waters");
    final File groupdir = new File(rootdir, group);
    final File dir = new File(groupdir, subdir);
    final File filename = new File(dir, name);
    final ProductDESProxy des = getCompiledDES(filename, null);
    configureModelVerifier(des);
    try {
      mVerifier.run();
    } catch (final Exception e) {
      System.out.print(e.getMessage());
      mPrintWriter.println(name + "," + e.getMessage());
    } finally {
      mStats =
          (CompositionalGeneralisedConflictCheckerVerificationResult) mVerifier
              .getAnalysisResult();
      mPrintWriter.print(name);
      mPrintWriter.print(',');
      mStats.printCSVHorizontal(mPrintWriter);
      mPrintWriter.println();
    }
  }

  // #######################################################################
  // # Data Members
  CompositionalGeneralisedConflictCheckerVerificationResult mStats;
  protected CompositionalGeneralisedConflictChecker mVerifier;
  final FileOutputStream mOut;
  PrintWriter mPrintWriter;
  final String mPreselecting;
  final String mSelecting;
  final int mRules;
  int mRuleCount;
}
