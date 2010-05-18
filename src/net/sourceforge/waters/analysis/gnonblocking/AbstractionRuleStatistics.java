package net.sourceforge.waters.analysis.gnonblocking;

import java.io.PrintStream;
import java.util.Formatter;


/**
 * Contains statistics about the application of an abstraction rule to one
 * model. Intended use is that a rule will be applied multiple times during the
 * composition of a model, and this will hold accumulative data of each
 * application of the rule.
 *
 * @author Rachel Francis
 */
public class AbstractionRuleStatistics
{
  // #########################################################################
  // # Constructors

  public AbstractionRuleStatistics(final String ruleName)
  {
    mName = ruleName;
    mRunTime = 0;
    mAppliedCount = 0;
    mReductionCount = 0;
    mInputStates = 0;
    mOutputStates = 0;
    mInputTransitions = 0;
    mOutputTransitions = 0;
    mUnchangedStates = 0;
    mUnchangedTransitions = 0;
  }

  // #########################################################################
  // # Simple Access Methods
  /**
   * Gets the name of the abstraction rule these statistics are for.
   */
  public String getRuleName()
  {
    return mName;
  }

  /**
   * Gets the accumulative time taken to run the abstraction rule.
   */
  public long getRunTime()
  {
    return mRunTime;
  }

  /**
   * Gets the number of times this abstraction rule is applied to the model.
   */
  public int getApplicationCount()
  {
    return mAppliedCount;
  }

  /**
   * Gets the number of times this abstraction rule reduced the size of the
   * model it was applied to.
   */
  public int getReductionCount()
  {
    return mReductionCount;
  }

  /**
   * Gets the sum of the number of input states for this rule when a reduction
   * occurred.
   */
  public int getInputStatesSum()
  {
    return mInputStates;
  }

  /**
   * Gets the sum of the number of output states for this rule when a reduction
   * occurred.
   */
  public int getOutputStatesSum()
  {
    return mOutputStates;
  }

  /**
   * Gets the sum of the number of input transitions for this rule when a
   * reduction occurred.
   */
  public int getInputTransitionsSum()
  {
    return mInputTransitions;
  }

  /**
   * Gets the sum of the number of output transitions for this rule when a
   * reduction occurred.
   */
  public int getOutputTransitionsSum()
  {
    return mOutputTransitions;
  }

  /**
   * Gets the sum of the number of input states for this rule when a reduction
   * didn't occur (therefore, output count = this input count).
   */
  public int getUnchangedStatesSum()
  {
    return mUnchangedStates;
  }

  /**
   * Gets the sum of the number of input transitions for this rule when a
   * reduction didn't occur (therefore, output count = this input count).
   */
  public int getUnchangedTransitionsSum()
  {
    return mUnchangedTransitions;
  }

  /**
   * Gets the sum of the total number of input states for this rule. This
   * includes input for when a reduction did occur and when a reduction didn't
   * occur.
   */
  public int getTotalInputStates()
  {
    return mInputStates + mUnchangedStates;
  }

  /**
   * Gets the sum of the total number of input transitions for this rule. This
   * includes input for when a reduction did occur and when a reduction didn't
   * occur.
   */
  public int getTotalInputTransitions()
  {
    return mInputTransitions + mUnchangedTransitions;
  }

  /**
   * Gets the sum of the total number of output states for this rule. This
   * includes output for when a reduction did occur and when a reduction didn't
   * occur.
   */
  public int getTotalOutputStates()
  {
    return mOutputStates + mUnchangedStates;
  }

  /**
   * Gets the sum of the total number of output transitions for this rule. This
   * includes output for when a reduction did occur and when a reduction didn't
   * occur.
   */
  public int getTotalOutputTransitions()
  {
    return mOutputTransitions + mUnchangedTransitions;
  }

  // #########################################################################
  // # Providing Statistics
  /**
   * Sets the number of times this abstraction rule reduced the size of the
   * model it was applied to.
   */
  public void setReductionCount(final int reduced)
  {
    mReductionCount = reduced;
  }

  /**
   * Sets the time taken for the abstraction rule to run.
   */
  public void setRunTime(final long runTime)
  {
    mRunTime = runTime;
  }

  /**
   * Sets the number of times this abstraction rule is applied to the model.
   */
  public void setApplicationCount(final int count)
  {
    mAppliedCount = count;
  }

  /**
   * Sets the sum of the number of input states for this rule when a reduction
   * occurred.
   */
  public void setInputStates(final int sum)
  {
    mInputStates = sum;
  }

  /**
   * Sets the sum of the number of output states for this rule when a reduction
   * occurred.
   */
  public void setOutputStates(final int sum)
  {
    mOutputStates = sum;
  }

  /**
   * Sets the sum of the number of input transitions for this rule when a
   * reduction occurred.
   */
  public void setInputTransitions(final int sum)
  {
    mInputTransitions = sum;
  }

  /**
   * Sets the sum of the number of output transitions for this rule when a
   * reduction occurred.
   */
  public void setOutputTransitions(final int sum)
  {
    mOutputTransitions = sum;
  }

  /**
   * Sets the sum of the number of input states for this rule when a reduction
   * didn't occur.
   */
  public void setUnchangedStates(final int sum)
  {
    mUnchangedStates = sum;
  }

  /**
   * Sets the sum of the number of input transitions for this rule when a
   * reduction didn't occur.
   */
  public void setUnchangedTransitions(final int sum)
  {
    mUnchangedTransitions = sum;
  }

  // #########################################################################
  // # Printing
  public void print(final PrintStream stream)
  {
    @SuppressWarnings("unused")
    final Formatter formatter = new Formatter(stream);
    stream.println("Name of rule: " + mName);
    stream.println("Total number of times applied: " + mAppliedCount);
    if (mAppliedCount > 0 && mReductionCount > 0) {
      stream.println("Total run time: " + mRunTime);
      stream.println("Total number of times a reduction occurred: "
          + mReductionCount);
      final double probability =
          (double) mReductionCount / (double) mAppliedCount;
      stream.println("Probability of a reduction occurring: " + probability);
      stream.println("Total number of input states: " + getTotalInputStates());
      stream.println("Total number of input transitions: "
          + getTotalInputTransitions());
      stream
          .println("Total number of output states: " + getTotalOutputStates());
      stream.println("Total number of output transitions: "
          + getTotalOutputTransitions());
      stream.println("Sum of input states with a reduction: " + mInputStates);
      stream.println("Sum of input transitions with a reduction: "
          + mInputTransitions);
      stream.println("Sum of output states with a reduction: " + mOutputStates);
      stream.println("Sum of output transitions with a reduction: "
          + mOutputTransitions);
      stream.println("Sum of input states with no reduction: "
          + mUnchangedStates);
      stream.println("Sum of input transitions with no reduction: "
          + mUnchangedTransitions);
    }
  }

  // #########################################################################
  // # Data Members
  private final String mName;
  private long mRunTime;
  private int mAppliedCount;
  private int mReductionCount;
  private int mInputStates;
  private int mOutputStates;
  private int mInputTransitions;
  private int mOutputTransitions;
  private int mUnchangedStates;
  private int mUnchangedTransitions;
}
