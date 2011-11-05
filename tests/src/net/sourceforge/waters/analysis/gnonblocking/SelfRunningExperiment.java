package net.sourceforge.waters.analysis.gnonblocking;

/**
 * This class can be used to automatically run experiments for different
 * properties with all possible combinations of heuristics.
 *
 * @author Rachel Francis
 */
public class SelfRunningExperiment
{

  public SelfRunningExperiment(final String property,
                               final String filename,
                               final int rules)
    throws Exception
  {
    for (int preselecting = 0; preselecting < 3; preselecting++) {
      final String preselectingHeuristic =
        getPreselectingHeuristic(preselecting);
      for (int selecting = 0; selecting < 6; selecting++) {
        final String selectingHeuristic = getSelectingHeuristic(selecting);
        final String entirefilename =
          filename + '_' + preselecting + '_' + selecting + ".csv";
        if (property.equals("standardnonblocking")) {
          final CompositionalStandardConflictCheckerExperiments experiment =
            new CompositionalStandardConflictCheckerExperiments
              (entirefilename, preselectingHeuristic,
               selectingHeuristic, rules);
          experiment.setUp();
          experiment.runAllTests();
          experiment.tearDown();
        } else if (property.equals("generalisednonblocking")) {
          final CompositionalGeneralisedConflictCheckerExperiments experiment =
            new CompositionalGeneralisedConflictCheckerExperiments
              (entirefilename, preselectingHeuristic,
               selectingHeuristic, rules);
          experiment.setUp();
          experiment.runAllTests();
          experiment.tearDown();
        }
      }
    }
  }

  private static String getSelectingHeuristic(final int selecting)
  {
    String heuristic = "";
    if (selecting == 0) {
      heuristic = "maxl";
    } else if (selecting == 1) {
      heuristic = "maxlt";
    } else if (selecting == 2) {
      heuristic = "maxc";
    } else if (selecting == 3) {
      heuristic = "maxct";
    } else if (selecting == 4) {
      heuristic = "mins";
    } else if (selecting == 5) {
      heuristic = "minsc";
    }
    return heuristic;
  }

  private static String getPreselectingHeuristic(final int preselecting)
  {
    if (preselecting == 0) {
      return "mustl";
    } else if (preselecting == 1) {
      return "maxs";
    } else if (preselecting == 2) {
      return "mint";
    }
    return "";
  }
}
