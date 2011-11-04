//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.io.FileNotFoundException;


/**
 * This class runs experiments using the CompositionalGeneralisedConflictChecker
 * with a variety of configurations for models with multiple marking
 * propositions. The heuristics for choosing candidates are varied, as well as
 * the abstraction rules applied and the order of them.
 *
 * TODO Merge this into CompositionalSynthesizerExperiments, then delete.
 *
 * @author Sahar Mohajerani
 */

public class CompositionalGeneralisedSynthesizerCheckerExperiments
  extends CompositionalSynthesizerExperiments
{

  // #######################################################################
  // # Constructor
  public CompositionalGeneralisedSynthesizerCheckerExperiments
    (final String statsFilename)
  throws FileNotFoundException
  {
    super(statsFilename);
  }


  //#########################################################################
  //# Main Method
  public static void main(final String[] args)
  {
    if (args.length == 3) {
      try {
        final String filename = args[0];
        final String outputprop = System.getProperty("waters.test.outputdir");
        final String preselectingHeuristic = args[1];
        final String selectingHeuristic = args[2];
        final CompositionalGeneralisedSynthesizerCheckerExperiments
          experiment =
            new CompositionalGeneralisedSynthesizerCheckerExperiments
              (outputprop + filename);
        experiment.setPreselectingHeuristic(preselectingHeuristic);
        experiment.setSelectingHeuristic(selectingHeuristic);
        experiment.setUp();
        experiment.runAllTests();
        experiment.tearDown();
      } catch (final Throwable exception) {
        System.err.println("FATAL ERROR");
        exception.printStackTrace(System.err);
      }
    } else {
      System.err
      .println("Usage: CompositionalGeneralisedSynthesizerCheckerExperiments "
      + "outputFilename preselectingHeuristic selectingHeuristic " +
      "listOfRulesSelection");
    }
  }


  //#########################################################################
  //# Invocation
  void runAllTests() throws Exception
  {
    verify_aip3_syn_as1();
    verify_aip3_syn_as2();
    verify_aip3_syn_as3();
    verify_aip3_syn_io();
    verify_aip3_syn_tu1();
    verify_aip3_syn_tu2();
    verify_aip3_syn_tu3();
    verify_aip3_syn_tu4();
  }


  //#########################################################################
  //# Models
  private void verify_aip3_syn_as1() throws Exception
  {
    runModel("despot", "song_aip/aip3_syn", "as1.wmod");
  }

  private void verify_aip3_syn_as2() throws Exception
  {
    runModel("despot", "song_aip/aip3_syn", "as2.wmod");
  }

  private void verify_aip3_syn_as3() throws Exception
  {
    runModel("despot", "song_aip/aip3_syn", "as3.wmod");
  }

  private void verify_aip3_syn_io() throws Exception
  {
    runModel("despot", "song_aip/aip3_syn", "io.wmod");
  }

  private void verify_aip3_syn_tu1() throws Exception
  {
    runModel("despot", "song_aip/aip3_syn", "tu1.wmod");
  }

  private void verify_aip3_syn_tu2() throws Exception
  {
    runModel("despot", "song_aip/aip3_syn", "tu2.wmod");
  }

  private void verify_aip3_syn_tu3() throws Exception
  {
    runModel("despot", "song_aip/aip3_syn", "tu3.wmod");
  }

  private void verify_aip3_syn_tu4() throws Exception
  {
    runModel("despot", "song_aip/aip3_syn", "tu4.wmod");
  }
}
