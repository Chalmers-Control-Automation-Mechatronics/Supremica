package net.sourceforge.waters.analysis.gnonblocking;

import java.io.PrintStream;
import java.util.Formatter;


/**
 * Contains statistics about the application of an abstraction rule.
 *
 * @author Rachel Francis
 */
public class AbstractionRuleStatistics
{
  // #########################################################################
  // # Constructors

  // #########################################################################
  // # Simple Access Methods
  /**
   * Gets the time taken to run the abstraction rule.
   */
  public long getRunTime()
  {
    return mRunTime;
  }

  /**
   * Gets the number of times this abstraction rule reduced the size of the
   * model it was applied to.
   */
  public int getResult()
  {
    return mReductionCount;
  }

  /**
   * Gets the number of times this abstraction rule is applied to the model.
   */
  public int getApplicationCount()
  {
    return mAppliedCount;
  }

  // #########################################################################
  // # Providing Statistics
  /**
   * Specifies the number of times this abstraction rule reduced the size of the
   * model it was applied to.
   */
  public void setResult(final int reduced)
  {
    mReductionCount = reduced;
  }

  /**
   * Specifies the time taken for the abstraction rule to run.
   */
  public void setRunTime(final long runTime)
  {
    mRunTime = runTime;
  }

  /**
   * Specifies the number of times this abstraction rule is applied to the
   * model.
   */
  public void setApplicationCount(final int count)
  {
    mAppliedCount = count;
  }

  // #########################################################################
  // # Printing
  public void print(final PrintStream stream)
  {
    @SuppressWarnings("unused")
    final Formatter formatter = new Formatter(stream);
    // TODO:add stats to print
  }

  // #########################################################################
  // # Data Members
  private long mRunTime;
  private int mAppliedCount;
  private int mReductionCount;

}
