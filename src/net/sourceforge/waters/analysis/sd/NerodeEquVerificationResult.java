package net.sourceforge.waters.analysis.sd;

import net.sourceforge.waters.analysis.monolithic.MonolithicNerodeEChecker;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.SafetyTraceProxy;


/**
 * A result record returned by a {@link MonolithicNerodeEChecker} A verification
 * result contains the information on whether the property checked is true or
 * false, and in the latter case, it also contains two counterexamples.
 *
 * @author Mahvash Baloch
 */
public class NerodeEquVerificationResult extends VerificationResult
{
  // #########################################################################
  // # Constructors
  /**
   * Creates a verification result representing an incomplete run.
   */
  public NerodeEquVerificationResult()
  {
    mCounterExample2 = null;
  }

  // #########################################################################
  // # Simple Access Methods
  /**
   * Get all results from conflict checker runs (one result for each answer
   * event).
   */

   public SafetyTraceProxy getCounterExample2()
  {
    return mCounterExample2;
  }
  //#########################################################################
  //# Providing Statistics
  /**
   * Sets all the conflict checker results for this SIC property V verification
   * (one result for each answer event).
   */

  public void setCounterExample2(final SafetyTraceProxy CounterExample)
  {
    mCounterExample2 = CounterExample;
  }

  //#########################################################################
  //# Data Members
  //private TraceProxy mCounterExample1;
  private SafetyTraceProxy mCounterExample2;

}
