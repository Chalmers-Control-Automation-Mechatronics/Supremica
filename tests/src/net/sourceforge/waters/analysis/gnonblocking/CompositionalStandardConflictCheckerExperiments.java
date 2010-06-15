package net.sourceforge.waters.analysis.gnonblocking;

import java.io.FileNotFoundException;


/**
 * This class runs experiments using the CompositionalGeneralisedConflictChecker
 * with a variety of configurations for models with only one marking
 * proposition. The heuristics for choosing candidates are varied, as well as
 * the abstraction rules applied and the order of them.
 *
 * @author Rachel Francis
 */
public class CompositionalStandardConflictCheckerExperiments extends
    CompositionalConflictCheckerExperiments
{

  // #######################################################################
  // # Constructor
  public CompositionalStandardConflictCheckerExperiments(
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
      final CompositionalStandardConflictCheckerExperiments experiment =
          new CompositionalStandardConflictCheckerExperiments(outputprop
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
    verifyBig_BMW();
    verify_ftechnik();
    verify_verriegel4();
    verify_verriegel4b();
    verify_rhone_alps();
    verify_tbed_ctct();
    verify_fzelle();
    verify_tbed_uncont();
    verify_tbed_noderail();
    verify_tbed_noderail_block();
    verify_tbed_valid();
    verify_profisafe_i4_host();
    verify_profisafe_i5_host();

  }

  // #######################################################################
  // # Models
  private void verifyBig_BMW() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "big_bmw.wmod";
    runModel(group, dir, name);
  }

  private void verify_ftechnik() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "ftechnik.wmod";
    runModel(group, dir, name);
  }

  private void verify_fzelle() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "fzelle.wmod";
    runModel(group, dir, name);
  }

  private void verify_rhone_alps() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "rhone_alps.wmod";
    runModel(group, dir, name);
  }

  private void verify_tbed_ctct() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_ctct.wmod";
    runModel(group, dir, name);
  }

  private void verify_tbed_uncont() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_uncont.wmod";
    runModel(group, dir, name);
  }

  private void verify_tbed_noderail_block() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_noderail_block.wmod";
    runModel(group, dir, name);
  }

  private void verify_tbed_noderail() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_noderail.wmod";
    runModel(group, dir, name);
  }

  private void verify_tbed_valid() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_valid.wmod";
    runModel(group, dir, name);
  }

  private void verify_verriegel4() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "verriegel4.wmod";
    runModel(group, dir, name);
  }

  private void verify_verriegel4b() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "verriegel4b.wmod";
    runModel(group, dir, name);
  }

  private void verify_profisafe_i4_host() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4_host.wmod";
    runModel(group, dir, name);
  }

  private void verify_profisafe_i5_host() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i5_host.wmod";
    runModel(group, dir, name);
  }
}
