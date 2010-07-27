package net.sourceforge.waters.analysis.gnonblocking;

import java.io.FileNotFoundException;


/**
 * This class runs SIC property 5 experiments using the
 * CompositionalGeneralisedConflictChecker with a variety of configurations. The
 * heuristics for choosing candidates are varied, as well as the abstraction
 * rules applied and the order of them.
 *
 * @author Rachel Francis
 */
public class CompositionalSICProperty5Experiments extends
    CompositionalConflictCheckerExperiments
{

  // #######################################################################
  // # Constructor
  public CompositionalSICProperty5Experiments(
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
      final int rules = Integer.decode(args[1]);
      final CompositionalSICProperty5Experiments experiment =
          new CompositionalSICProperty5Experiments(outputprop + filename,
              preselectingHeuristic, selectingHeuristic, rules);
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

  }

  protected void runModel(final String group, final String subdir,
                          final String name) throws Exception
  {

  }

  // #######################################################################
  // # Models
  private void verify_aip3_syn_as1() throws Exception
  {
    runModel("despot", "song_aip/aip3_syn", "as1.wmod");
  }

}
