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
public abstract class CompositionalConflictCheckerExperiments extends
    AbstractAnalysisTest
{

  // #######################################################################
  // # Constructor

  public CompositionalConflictCheckerExperiments(
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
    if (mRules == 1 || mRules == 2) {
      mRuleCount = 8;
    } else if (mRules == 3) {
      mRuleCount = 10;
    }
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mVerifier = new CompositionalGeneralisedConflictChecker(factory);
    mPrintStream = new PrintStream(mOut, true);
    final int internalStateLimit = 10000;
    mVerifier.setInternalStepNodeLimit(internalStateLimit);
    final int internalTransitionLimit = 1000000;
    mVerifier.setInternalStepTransitionLimit(internalTransitionLimit);
    final int finalStateLimit = 20000000;
    mVerifier.setFinalStepNodeLimit(finalStateLimit);
    final int finalTransitionLimit = 0;
    mVerifier.setFinalStepTransitionLimit(finalTransitionLimit);
    mPrintStream.println("InternalStateLimit," + internalStateLimit
        + ",InternalTransitionLimit," + internalTransitionLimit
        + ",FinalStateLimit," + finalStateLimit + ",FinalTransitionLimit,"
        + finalTransitionLimit);

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
    mPrintStream.println("PreselHeuristic," + mPreselecting
        + ",SelecHeuristic," + mSelecting);

    mStats = new CompositionalGeneralisedConflictCheckerVerificationResult();
    mStats.printCSVHorizontalHeadings(mPrintStream, mRuleCount);
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

  protected void configureModelVerifier(final ProductDESProxy des)
      throws EventNotFoundException
  {
    mVerifier.setModel(des);
    // sets order of abstraction rules
    final List<AbstractionRule> ruleList = new LinkedList<AbstractionRule>();
    final ProductDESProxyFactory factory = mVerifier.getFactory();
    final EventProxy alpha = mVerifier.getUsedPreconditionMarkingProposition();
    mVerifier.setGeneralisedPrecondition(alpha);
    final EventProxy omega = mVerifier.getUsedMarkingProposition();
    final List<EventProxy> propositions = new ArrayList<EventProxy>(2);
    propositions.add(alpha);
    propositions.add(omega);
    final TauLoopRemovalRule tlrRule =
        new TauLoopRemovalRule(factory, propositions);

    final ObservationEquivalenceRule oeRule =
        new ObservationEquivalenceRule(factory, propositions);
    oeRule.setTransitionLimit(mVerifier.getInternalStepTransitionLimit());

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
    dnasRule.setTransitionLimit(mVerifier.getInternalStepTransitionLimit());

    final RemovalOfTauTransitionsLeadingToNonAlphaStatesRule rttlnsRule =
        new RemovalOfTauTransitionsLeadingToNonAlphaStatesRule(factory,
            propositions);
    rttlnsRule.setAlphaMarking(alpha);

    final RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule rttonsRule =
        new RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule(factory,
            propositions);
    rttonsRule.setAlphaMarking(alpha);
    rttonsRule.setDefaultMarking(omega);
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
      // observation equivalence first and last
      ruleList.add(tlrRule);
      ruleList.add(oeRule);
      ruleList.add(ramRule);
      ruleList.add(rdmRule);
      ruleList.add(rnsRule);
      ruleList.add(dnasRule);
      ruleList.add(rttlnsRule);
      ruleList.add(rttonsRule);
      ruleList.add(tlrRule);
      ruleList.add(oeRule);
    } // TODO: add other orderings to try
    else {
      System.err
          .println("Error: Rules must be specified by specifying the integer code of the ordered list wanted.");
    }
    mVerifier.setAbstractionRules(ruleList);
  }

  protected void runModel(final String group, final String subdir,
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
      mPrintStream.println(des.getName() + "," + e);
    }
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
