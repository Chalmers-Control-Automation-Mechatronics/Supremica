package net.sourceforge.waters.despot;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.TraceProxy;


/**
 * A result record returned by a {@link SICPropertyVVerifier} A verification
 * result contains the information on whether the property checked is true or
 * false, and in the latter case, it also contains a counterexample. In
 * addition, it contains individual statistics about the Conflict Checker run
 * for every answer event.
 *
 * @author Rachel Francis
 */
public class SICPropertyVVerifierVerificationResult extends VerificationResult
{
  // #########################################################################
  // # Constructors
  /**
   * Creates a <I>true</I> verification result. This constructor creates a
   * verification result which indicates that the property checked is true.
   */
  public SICPropertyVVerifierVerificationResult()
  {
    this(true, null);
  }

  /**
   * Creates a <I>false</I> verification result. This constructor creates a
   * verification result which indicates that the property checked is false,
   * because of the given counterexample.
   */
  public SICPropertyVVerifierVerificationResult(final TraceProxy counterexample)
  {
    this(false, counterexample);
  }

  /**
   * Creates a verification result with parameters as given.
   */
  public SICPropertyVVerifierVerificationResult(final boolean satisfied,
                                                final TraceProxy counterexample)
  {
    super(satisfied, counterexample);
    mConflictCheckerStats = new ArrayList<VerificationResult>();
  }

  // #########################################################################
  // # Simple Access Methods
  /**
   * Get all results from conflict checker runs (one result for each answer
   * event).
   */
  public List<? extends VerificationResult> getConflictCheckerResults()
  {
    return mConflictCheckerStats;
  }

  // #########################################################################
  // # Providing Statistics

  /**
   * Sets all the conflict checker results for this SIC property V verification
   * (one result for each answer event).
   */
  public void setConflictCheckerResult
    (final List<? extends VerificationResult> results)
  {
    mConflictCheckerStats = results;
  }

  // #########################################################################
  // # Printing
  @Override
  public void print(final PrintStream stream)
  {
    super.print(stream);
    for (final VerificationResult result : mConflictCheckerStats) {
      result.print(stream);
    }
  }


  //#########################################################################
  //# Data Members
  private List<? extends VerificationResult> mConflictCheckerStats;

}
