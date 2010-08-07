package net.sourceforge.waters.analysis.gnonblocking;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.waters.model.analysis.VerificationResult;


/**
 * A result record returned by a {@link CompositionalGeneralisedConflictChecker}
 * A verification result contains the information on whether a property checked
 * is true or false, and in the latter case, it also contains a counterexample.
 * In addition, it contains statistics about the analysis run.
 *
 * @author Rachel Francis
 */
public class CompositionalGeneralisedConflictCheckerVerificationResult extends
    VerificationResult
{
  // #########################################################################
  // # Constructors
  /**
   * Creates a verification result representing an incomplete verification run.
   */
  public CompositionalGeneralisedConflictCheckerVerificationResult()
  {
    mUnsuccessfulCompositionCount = 0;
    mSuccessfulCompositionCount = 0;
    mComposedModelNumberOfStates = 0;
    mComposedModelNumberOfTransitions = 0;
    mAbstractionRuleStats = null;
  }

  // #########################################################################
  // # Simple Access Methods
  /**
   * Gets the number of states in the final composed model.
   */
  public double getComposedModelStateCount()
  {
    return mComposedModelNumberOfStates;
  }

  /**
   * Gets the number of transitions in the final composed model.
   */
  public double getComposedModelTransitionCount()
  {
    return mComposedModelNumberOfTransitions;
  }

  /**
   * Gets the number of times a successful candidate is chosen, composed and
   * abstracted.
   */
  public int getCompositionCount()
  {
    return mSuccessfulCompositionCount;
  }

  /**
   * Gets the number of times an unsuccessful candidate is chosen and an
   * overflow exception occurs during composition or abstraction.
   */
  public int getUnsuccessfulCompositionCount()
  {
    return mUnsuccessfulCompositionCount;
  }

  /**
   * Gets the total number of times a candidate is chosen and composition and
   * abstraction is attempted. The attempts may or may not have been successful
   * in producing a reduced size model with no overflow exceptions occurring.
   */
  public int getTotalCompositionCount()
  {
    return mSuccessfulCompositionCount + mUnsuccessfulCompositionCount;
  }

  /**
   * Gets the statistics that apply to the abstraction rules used.
   */
  public List<AbstractionRuleStatistics> getAbstractionRuleStatistics()
  {
    return mAbstractionRuleStats;
  }

  // #########################################################################
  // # Providing Statistics

  /**
   * Sets the number of states in the final composed model.
   */
  public void setComposedModelStateCount(final double stateCount)
  {
    mComposedModelNumberOfStates = stateCount;
  }

  /**
   * Sets the number of transitions in the final composed model.
   */
  public void setComposedModelTransitionCount(final double transitionCount)
  {
    mComposedModelNumberOfTransitions = transitionCount;
  }

  /**
   * Maps a list of AbstractionRuleStatistics which represent the statistics for
   * the abstraction rules used during the composition of the model.
   */
  public void setAbstractionRuleStats(final List<AbstractionRule> rules)
  {
    mAbstractionRuleStats =
        new ArrayList<AbstractionRuleStatistics>(rules.size());
    for (final AbstractionRule rule : rules) {
      final AbstractionRuleStatistics stats = rule.getStatistics();
      mAbstractionRuleStats.add(stats);
    }
  }

  /**
   * Sets the number of times a successful candidate is chosen, composed and
   * abstracted.
   */
  public void setSuccessfulCompositionCount(final int count)
  {
    mSuccessfulCompositionCount = count;
  }

  /**
   * Sets the number of times a candidate is chosen and an overflow exception
   * occurs during composition or abstraction.
   */
  public void setUnsuccessfulCompositionCount(final int count)
  {
    mUnsuccessfulCompositionCount = count;
  }

  //#########################################################################
  //# Printing
  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    writer.println("Number of times a model is successfully composed: "
        + mSuccessfulCompositionCount);
    writer.println("Number of times a model is unsuccessfully composed: "
        + mUnsuccessfulCompositionCount);
    final double probability =
        (double) mUnsuccessfulCompositionCount
            / (double) getTotalCompositionCount();
    writer.println("Probability of a candidate selection being unsuccessful: "
        + probability);
    writer.println("Number of states in final composed model: "
        + mComposedModelNumberOfStates);
    writer.println("Number of transitions in final composed model: "
        + mComposedModelNumberOfTransitions);
    writer
        .println("-----------------------Rule Results ----------------------");
    for (final AbstractionRuleStatistics ruleStats : mAbstractionRuleStats) {
      ruleStats.print(writer);
      writer.println();
    }

  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    printCSVHorizontalHeadings(writer, mAbstractionRuleStats.size());
  }

  void printCSVHorizontalHeadings(final PrintStream stream, final int numRules)
  {
    final PrintWriter writer = new PrintWriter(stream);
    printCSVHorizontalHeadings(writer, numRules);
  }

  void printCSVHorizontalHeadings(final PrintWriter writer, final int numRules)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(",Succ composed");
    writer.print(",Unsucc composed");
    writer.print(",Prob candidate unsucc");
    writer.print(",States final");
    writer.print(",Tansitions final");
    final AbstractionRuleStatistics ruleStats =
        new AbstractionRuleStatistics(null);
    for (int i = 0; i < numRules; i++) {
      ruleStats.printCSVHorizontalHeadings(writer);
    }
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print("," + mSuccessfulCompositionCount);
    writer.print("," + mUnsuccessfulCompositionCount);
    final double probability =
        (double) mUnsuccessfulCompositionCount
            / (double) getTotalCompositionCount();
    writer.print("," + probability);
    writer.print("," + mComposedModelNumberOfStates);
    writer.print("," + mComposedModelNumberOfTransitions);
    for (final AbstractionRuleStatistics ruleStats : mAbstractionRuleStats) {
      ruleStats.printCSVHorizontal(writer);
    }
  }


  //#########################################################################
  //# Data Members
  private List<AbstractionRuleStatistics> mAbstractionRuleStats;
  private int mSuccessfulCompositionCount;
  private int mUnsuccessfulCompositionCount;
  private double mComposedModelNumberOfStates;
  private double mComposedModelNumberOfTransitions;
}
