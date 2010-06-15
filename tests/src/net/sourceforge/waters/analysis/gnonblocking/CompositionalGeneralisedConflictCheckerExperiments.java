package net.sourceforge.waters.analysis.gnonblocking;

import java.io.FileNotFoundException;


/**
 * This class runs experiments using the CompositionalGeneralisedConflictChecker
 * with a variety of configurations for models with multiple marking
 * propositions. The heuristics for choosing candidates are varied, as well as
 * the abstraction rules applied and the order of them.
 *
 * @author Rachel Francis
 */
public class CompositionalGeneralisedConflictCheckerExperiments extends
    CompositionalConflictCheckerExperiments
{

  // #######################################################################
  // # Constructor
  public CompositionalGeneralisedConflictCheckerExperiments(
                                                            final String statsFilename,
                                                            final String preselectingHeuristic,
                                                            final String selectingHeuristic,
                                                            final int rules)
      throws FileNotFoundException
  {
    super(statsFilename, preselectingHeuristic, selectingHeuristic, rules);
  }

  // #######################################################################
  // # Invocation
  public static void main(final String[] args) throws Exception
  {
    if (args.length == 4) {
      final String filename = args[0];
      final String outputprop = System.getProperty("waters.test.outputdir");
      final String preselectingHeuristic = args[1];
      final String selectingHeuristic = args[2];
      final int rules = Integer.decode(args[3]);
      final CompositionalGeneralisedConflictCheckerExperiments experiment =
          new CompositionalGeneralisedConflictCheckerExperiments(outputprop
              + filename, preselectingHeuristic, selectingHeuristic, rules);
      experiment.setUp();
      experiment.runAllTests();
      experiment.tearDown();
    } else {
      System.err
          .println("Usage: CompositionalGeneralisedConflictCheckerExperiments outputFilename preselectingHeuristic selectingHeuristic listOfRulesSelection");
    }
  }

  private void runAllTests() throws Exception
  {
    verify_aip3_syn_as1();
    verify_aip3_syn_as2();
    verify_aip3_syn_as3();
    verify_aip3_syn_io();
    verify_aip3_syn_tu1();
    verify_aip3_syn_tu2();
    verify_aip3_syn_tu3();
    verify_aip3_syn_tu4();
    // TODO: look at tests in LargeCompositionalSICPropertyVVerifierTest to see
    // if maip etc can pass and be added here...
  }

  // #######################################################################
  // # Models
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
