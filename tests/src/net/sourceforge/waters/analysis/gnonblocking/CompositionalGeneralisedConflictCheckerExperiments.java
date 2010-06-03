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
    final String outputprop = System.getProperty("waters.test.outputdir");

    final CompositionalGeneralisedConflictCheckerExperiments experiment =
        new CompositionalGeneralisedConflictCheckerExperiments(outputprop
            + filename);
    experiment.setUp();
    experiment.runAllTests();
    experiment.tearDown();
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    mPrintStream = new PrintStream(mOut, true);
    mStats = new CompositionalGeneralisedConflictCheckerVerificationResult();
    // TODO: add rule count
    mStats.printCSVHorizontalHeadings(mPrintStream, 7);
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mVerifier = new CompositionalGeneralisedConflictChecker(factory);
  }

  @Override
  protected void tearDown() throws Exception
  {
    mVerifier = null;
    mPrintStream.close();
    mOut.close();
    System.out.println("All experiments complete");
    super.tearDown();
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
    verify_profisafe_i4_host();
    verify_profisafe_i5_host();

  }

  protected void configureModelVerifier(final ProductDESProxy des)
  {
    mVerifier.setModel(des);
    mVerifier.setInternalStepNodeLimit(1000);
    mVerifier.setInternalStepTransitionLimit(100000);
    mVerifier.setFinalStepNodeLimit(100000);
    mVerifier.setFinalStepTransitionLimit(0);
    // TODO: configure other settings here
  }

  private void runModel(final String group, final String subdir,
                        final String name) throws Exception
  {
    System.out.println("Running " + name + "....");
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
      mStats =
          (CompositionalGeneralisedConflictCheckerVerificationResult) mVerifier
              .getAnalysisResult();
      mPrintStream.print(name + ",");
      mStats.printCSVHorizontal(mPrintStream);
    } catch (final Exception e) {
      System.out.println(e);
    }
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

  private void verify_tbed_noderail() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_noderail.wmod";
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

  // #######################################################################
  // # Data Members
  CompositionalGeneralisedConflictCheckerVerificationResult mStats;
  private CompositionalGeneralisedConflictChecker mVerifier;
  final FileOutputStream mOut;
  PrintStream mPrintStream;
}
