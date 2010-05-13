package net.sourceforge.waters.analysis.gnonblocking;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


/**
 * This class runs experiments with CompositionalGeneralisedConflictChecker with
 * a variety of configurations. The heuristics for choosing candidates are
 * varied, as well as the abstraction rules applied and the order of them.
 *
 * @author Rachel Francis
 */
public class CompositionalGeneralisedConflictCheckerExperiments
{

  // #######################################################################
  // # Constructor
  public CompositionalGeneralisedConflictCheckerExperiments(final int repeat)
  {
    mRepeat = repeat;
  }

  // #######################################################################
  // # Invocation
  public static void main(final String[] args) throws AnalysisException,
      IOException
  {
    final String filename = args[0];
    final int repeat = Integer.decode(args[1]);
    final CompositionalGeneralisedConflictCheckerExperiments experiment =
        new CompositionalGeneralisedConflictCheckerExperiments(repeat);
    experiment.runTest(filename);
  }

  private void runTest(final String outputFilename) throws AnalysisException,
      IOException
  {
    final ProductDESProxyFactory factory =
        ProductDESElementFactory.getInstance();

    final CompositionalGeneralisedConflictChecker checker =
        new CompositionalGeneralisedConflictChecker(factory);
    // TODO: need to set model to run with
    checker.run();
    final CompositionalGeneralisedNonblockingConflictCheckerVerificationResult stats =
        (CompositionalGeneralisedNonblockingConflictCheckerVerificationResult) checker
            .getAnalysisResult();
    final FileOutputStream out = new FileOutputStream(outputFilename);
    final PrintStream printStream = new PrintStream(out, true);
    stats.print(printStream);
    printStream.close();
    out.close();
  }

  // #######################################################################
  // # Data Members
  @SuppressWarnings("unused")
  private final int mRepeat;
}
