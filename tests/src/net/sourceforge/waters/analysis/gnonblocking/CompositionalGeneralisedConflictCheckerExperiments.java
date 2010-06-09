package net.sourceforge.waters.analysis.gnonblocking;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.EventNotFoundException;
import net.sourceforge.waters.model.des.EventProxy;
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
                                                            final String statsFilename,
                                                            final String preselectingHeuristic,
                                                            final String selectingHeuristic,
                                                            final int rules)
      throws FileNotFoundException
  {
    mOut = new FileOutputStream(statsFilename);
    mPrintStream = null;
    mPreselecting = preselectingHeuristic.toLowerCase();
    mSelecting = selectingHeuristic.toLowerCase();
    mRules = rules;
    mRuleCount = 0;
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

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    mPrintStream = new PrintStream(mOut, true);
    mStats = new CompositionalGeneralisedConflictCheckerVerificationResult();
    mStats.printCSVHorizontalHeadings(mPrintStream, mRuleCount);
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
    verify_tbed_noderail_block();
    verify_tbed_valid();
    verify_profisafe_i4_host();
    verify_profisafe_i5_host();

  }

  protected void configureModelVerifier(final ProductDESProxy des)
      throws EventNotFoundException
  {
    mVerifier.setModel(des);
    mVerifier.setInternalStepNodeLimit(1000);
    final int internalStepTransitionLimit = 100000;
    mVerifier.setInternalStepTransitionLimit(internalStepTransitionLimit);
    mVerifier.setFinalStepNodeLimit(100000);
    mVerifier.setFinalStepTransitionLimit(0);

    // sets correct preselecting heuristic
    if (mPreselecting.equals("mint")) {
      mVerifier.setPreselectingHeuristic(mVerifier.createHeuristicMinT());
    } else if (mPreselecting.equals("maxs")) {
      mVerifier.setPreselectingHeuristic(mVerifier.createHeuristicMaxS());
    } else if (mPreselecting.equals("mustl")) {
      mVerifier.setPreselectingHeuristic(mVerifier.createHeuristicMustL());
    } else {
      System.err
          .println("Error: Preselecting Heuristic not specified correctly, it must be one of: mint, maxs, mustl");
    }

    // sets correct selecting heuristic
    if (mSelecting.equals("maxl")) {
      mVerifier.setSelectingHeuristic(mVerifier.createHeuristicMaxL());
    } else if (mSelecting.equals("maxc")) {
      mVerifier.setSelectingHeuristic(mVerifier.createHeuristicMaxC());
    } else if (mSelecting.equals("mins")) {
      mVerifier.setSelectingHeuristic(mVerifier.createHeuristicMinS());
    } else {
      System.err
          .println("Error: Selecting Heuristic not specified correctly, it must be one of: maxl, maxc, mins");
    }

    // sets order of abstraction rules
    final List<AbstractionRule> ruleList = new LinkedList<AbstractionRule>();
    final ProductDESProxyFactory factory = mVerifier.getFactory();
    final EventProxy alpha = mVerifier.getUsedPreconditionMarkingProposition();
    final EventProxy omega = mVerifier.getUsedMarkingProposition();
    final List<EventProxy> propositions = new ArrayList<EventProxy>(2);
    propositions.add(alpha);
    propositions.add(omega);
    final TauLoopRemovalRule tlrRule =
        new TauLoopRemovalRule(factory, propositions);

    final ObservationEquivalenceRule oeRule =
        new ObservationEquivalenceRule(factory, propositions);
    oeRule.setTransitionLimit(internalStepTransitionLimit);

    final RemovalOfAlphaMarkingsRule ramRule =
        new RemovalOfAlphaMarkingsRule(factory, propositions);
    ramRule.setAlphaMarking(alpha);

    final RemovalOfDefaultMarkingsRule rdmRule =
        new RemovalOfDefaultMarkingsRule(factory, propositions);
    rdmRule.setAlphaMarking(alpha);
    rdmRule.setDefaultMarking(omega);

    final RemovalOfNoncoreachableStatesRule rnsRule =
        new RemovalOfNoncoreachableStatesRule(factory, propositions);
    rnsRule.setAlphaMarking(alpha);
    rnsRule.setDefaultMarking(omega);

    final DeterminisationOfNonAlphaStatesRule dnasRule =
        new DeterminisationOfNonAlphaStatesRule(factory, propositions);
    dnasRule.setAlphaMarking(alpha);
    dnasRule.setTransitionLimit(internalStepTransitionLimit);

    final RemovalOfTauTransitionsLeadingToNonAlphaStatesRule rttlnsRule =
        new RemovalOfTauTransitionsLeadingToNonAlphaStatesRule(factory,
            propositions);
    rttlnsRule.setAlphaMarking(alpha);

    final RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule rttonsRule =
        new RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule(factory,
            propositions);
    rttonsRule.setAlphaMarking(alpha);
    rttonsRule.setDefaultMarking(omega);
    mRuleCount = 8;
    if (mRules == 1) {
      // order the rules are presented in the paper
      ruleList.add(tlrRule);
      ruleList.add(oeRule);
      ruleList.add(ramRule);
      ruleList.add(rdmRule);
      ruleList.add(rnsRule);
      ruleList.add(dnasRule);
      ruleList.add(rttlnsRule);
      ruleList.add(rttonsRule);
    } else if (mRules == 2) {
      // observation equivalence last
      ruleList.add(ramRule);
      ruleList.add(rdmRule);
      ruleList.add(rnsRule);
      ruleList.add(dnasRule);
      ruleList.add(rttlnsRule);
      ruleList.add(rttonsRule);
      ruleList.add(tlrRule);
      ruleList.add(oeRule);
    } else if (mRules == 3) {
      // TODO: add other orderings to try
    } else {
      System.err
          .println("Error: Rules must be specified by specifying the integer code of the ordered list wanted.");
    }
    mVerifier.setAbstractionRules(ruleList);
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

  // #######################################################################
  // # Data Members
  CompositionalGeneralisedConflictCheckerVerificationResult mStats;
  private CompositionalGeneralisedConflictChecker mVerifier;
  final FileOutputStream mOut;
  PrintStream mPrintStream;
  final String mPreselecting;
  final String mSelecting;
  final int mRules;
  int mRuleCount;
}
