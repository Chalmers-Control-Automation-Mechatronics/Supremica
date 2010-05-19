package net.sourceforge.waters.analysis.gnonblocking;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.TraceProxy;


/**
 * A result record returned by a {@link CompositionalGeneralisedConflictChecker}
 * A verification result contains the information on whether a property checked
 * is true or false, and in the latter case, it also contains a counterexample.
 * In addition, it contains statistics about the analysis run.
 *
 * @author Rachel Francis
 */
public class CompositionalGeneralisedConflictCheckerVerificationResult
    extends VerificationResult
{
  // #########################################################################
  // # Constructors
  /**
   * Creates a <I>true</I> verification result. This constructor creates a
   * verification result which indicates that the property checked is true.
   */
  public CompositionalGeneralisedConflictCheckerVerificationResult()
  {
    this(true, null);
  }

  /**
   * Creates a <I>false</I> verification result. This constructor creates a
   * verification result which indicates that the property checked is false,
   * because of the given counterexample.
   */
  public CompositionalGeneralisedConflictCheckerVerificationResult
    (final TraceProxy counterexample)
  {
    this(false, counterexample);
  }

  /**
   * Creates a verification result with parameters as given.
   */
  public CompositionalGeneralisedConflictCheckerVerificationResult
    (final boolean satisfied, final TraceProxy counterexample)
  {
    super(satisfied, counterexample);
    mUnsuccessfulCompositionCount = 0;
    mSuccessfulCompositionCount = 0;
    mAbstractionRuleStats = null;
  }

  // #########################################################################
  // # Simple Access Methods
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

  // #########################################################################
  // # Printing
  @Override
  public void print(final PrintStream stream)
  {
    super.print(stream);

    stream.println("Number of times a model is successfully composed: "
        + mSuccessfulCompositionCount);
    stream.println("Number of times a model is unsuccessfully composed: "
        + mUnsuccessfulCompositionCount);

    final double probability =
        (double) mUnsuccessfulCompositionCount
            / (double) getTotalCompositionCount();

    stream.println("Probability of a candidate selection being unsuccessful: "
        + probability);
    /*
     * stream
     * .println("-----------------------Rule Results ----------------------");
     * for (final AbstractionRuleStatistics ruleStats : mAbstractionRuleStats) {
     * ruleStats.print(stream); stream.println(); }
     */

  }

  private List<AbstractionRuleStatistics> mAbstractionRuleStats;
  private int mSuccessfulCompositionCount;
  private int mUnsuccessfulCompositionCount;

}
