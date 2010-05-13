package net.sourceforge.waters.analysis.gnonblocking;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * This class runs experiments with CompositionalGeneralisedConflictChecker with
 * a variety of configurations. The heuristics for choosing candidates are
 * varied, as well as the abstraction rules applied and the order of them.
 *
 * @author Rachel Francis
 */
public class CompositionalGeneralisedConflictCheckerExperiments extends
    AbstractAnalysisTest
{

  // #######################################################################
  // # Constructor
  public CompositionalGeneralisedConflictCheckerExperiments(final int repeat)
  {
    mRepeat = repeat;
  }

  // #######################################################################
  // # Invocation
  public static void main(final String[] args) throws Exception
  {
    final String filename = args[0];
    final int repeat = Integer.decode(args[1]);
    final CompositionalGeneralisedConflictCheckerExperiments experiment =
        new CompositionalGeneralisedConflictCheckerExperiments(repeat);
    experiment.setUp();
    experiment.runAllTests(filename);
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mVerifier = new CompositionalGeneralisedConflictChecker(factory);
  }

  @Override
  protected void tearDown() throws Exception
  {
    mVerifier = null;
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
    // TODO: need to process stats in some way after verifying each model
    final CompositionalGeneralisedNonblockingConflictCheckerVerificationResult stats =
        (CompositionalGeneralisedNonblockingConflictCheckerVerificationResult) mVerifier
            .getAnalysisResult();
    final FileOutputStream out = new FileOutputStream(outputFilename);
    final PrintStream printStream = new PrintStream(out, true);
    stats.print(printStream);
    printStream.close();
    out.close();
  }

  protected void configureModelVerifier(final ProductDESProxy des)
  {
    mVerifier.setModel(des);
    // TODO: configure other settings here
  }

  private void verifyModel(final String group, final String subdir,
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

  }

  // #######################################################################
  // # Models
  private void verifyBig_BMW() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "big_bmw.wmod";
    verifyModel(group, dir, name);
  }

  private void verify_ftechnik() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "ftechnik.wmod";
    verifyModel(group, dir, name);
  }

  private void verify_verriegel4() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "verriegel4.wmod";
    verifyModel(group, dir, name);
  }

  private void verify_verriegel4b() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "verriegel4b.wmod";
    verifyModel(group, dir, name);
  }

  private void verify_rhone_alps() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "rhone_alps.wmod";
    verifyModel(group, dir, name);
  }

  private void verify_rhone_tough() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "rhone_tough.wmod";
    verifyModel(group, dir, name);
  }

  // #######################################################################
  // # Data Members
  @SuppressWarnings("unused")
  private final int mRepeat;
  private CompositionalGeneralisedConflictChecker mVerifier;

}
