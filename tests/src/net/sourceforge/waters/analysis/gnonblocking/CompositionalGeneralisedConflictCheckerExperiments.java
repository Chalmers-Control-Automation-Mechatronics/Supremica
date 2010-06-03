package net.sourceforge.waters.analysis.gnonblocking;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * This class runs experiments using the CompositionalGeneralisedConflictChecker
 * with a variety of configurations. The heuristics for choosing candidates are
 * varied, as well as the abstraction rules applied and the order of them.
 *
 * @author Rachel Francis
 */
public class CompositionalGeneralisedConflictCheckerExperiments extends
    AbstractAnalysisTest
{

  // #######################################################################
  // # Constructor

  public CompositionalGeneralisedConflictCheckerExperiments(
                                                            final String statsFilename)
      throws FileNotFoundException
  {
    mOut = new FileOutputStream(statsFilename);
    mPrintStream = null;
  }

  // #######################################################################
  // # Invocation
  public static void main(final String[] args) throws Exception
  {
    final String filename = args[0];
    final CompositionalGeneralisedConflictCheckerExperiments experiment =
        new CompositionalGeneralisedConflictCheckerExperiments(filename);
    experiment.setUp();
    experiment.runAllTests(filename);
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    mPrintStream = new PrintStream(mOut, true);
    mStats = new CompositionalGeneralisedConflictCheckerVerificationResult();
    mStats.printCSVHorizontalHeadings(mPrintStream);
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mVerifier = new CompositionalGeneralisedConflictChecker(factory);
  }

  @Override
  protected void tearDown() throws Exception
  {
    mVerifier = null;
    mPrintStream.close();
    mOut.close();
    super.tearDown();
  }

  private void runAllTests(final String outputFilename) throws Exception
  {
    verifyBig_BMW();
    verify_ftechnik();
    verify_verriegel4();
    verify_verriegel4b();
    verify_rhone_alps();
    verify_rhone_tough();
  }

  protected void configureModelVerifier(final ProductDESProxy des)
  {
    mVerifier.setModel(des);
    // TODO: configure other settings here
  }

  private void runModel(final String group, final String subdir,
                        final String name) throws Exception
  {
    final String inputprop = System.getProperty("waters.test.inputdir");
    // final String outputprop = System.getProperty("waters.test.outputdir");
    final File inputRoot = new File(inputprop);
    // File mOutputRoot = new File(outputprop);
    final File rootdir = new File(inputRoot, "waters");
    final File groupdir = new File(rootdir, group);
    final File dir = new File(groupdir, subdir);
    final File filename = new File(dir, name);
    final ProductDESProxy des = getCompiledDES(filename, null);
    configureModelVerifier(des);
    mVerifier.run();

    mStats =
        (CompositionalGeneralisedConflictCheckerVerificationResult) mVerifier
            .getAnalysisResult();
    mStats.printCSVHorizontal(mPrintStream);
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

  private void verify_rhone_alps() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "rhone_alps.wmod";
    runModel(group, dir, name);
  }

  private void verify_rhone_tough() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "rhone_tough.wmod";
    runModel(group, dir, name);
  }

  // #######################################################################
  // # Data Members
  CompositionalGeneralisedConflictCheckerVerificationResult mStats;
  private CompositionalGeneralisedConflictChecker mVerifier;
  final FileOutputStream mOut;
  PrintStream mPrintStream;
}
