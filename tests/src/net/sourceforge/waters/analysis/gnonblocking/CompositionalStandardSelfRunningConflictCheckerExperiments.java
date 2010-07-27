package net.sourceforge.waters.analysis.gnonblocking;

import java.io.FileNotFoundException;


/**
 * This class runs experiments using the CompositionalGeneralisedConflictChecker
 * with all possible combinations of heuristics for every model.
 *
 * @author Rachel Francis
 */
public class CompositionalStandardSelfRunningConflictCheckerExperiments extends
    CompositionalStandardConflictCheckerExperiments
{

  // #######################################################################
  // # Constructor
  public CompositionalStandardSelfRunningConflictCheckerExperiments(
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
    if (args.length == 2) {

      final int rules = Integer.decode(args[1]);

      final String filename = args[0];
      final String property = "standardnonblocking";
      final SelfRunningExperiment experiments =
          new SelfRunningExperiment(property, filename, rules);
    } else {
      System.err
          .println("Usage: CompositionalStandardSelfRunningConflictCheckerExperiments outputFilename listOfRulesSelection");
    }
  }

}
