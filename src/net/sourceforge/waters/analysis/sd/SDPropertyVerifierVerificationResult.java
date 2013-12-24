package net.sourceforge.waters.analysis.sd;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.analysis.DefaultVerificationResult;
import net.sourceforge.waters.model.analysis.VerificationResult;


/**
 * A result record returned by a {@link SDSingularProhibitableBehaviorVerifier}.
 * A verification result contains the information on whether the property
 * checked is true or false, and in the latter case, it also contains a
 * counterexample. In addition, it contains individual statistics about the
 * Language Inclusion Checker run for every prohibitable event.
 *
 * @author Rachel Francis, Mahvash Baloch
 */
public class SDPropertyVerifierVerificationResult
  extends DefaultVerificationResult
{
  // #########################################################################
  // # Constructors
  /**
   * Creates a verification result representing an incomplete run.
   */
  public SDPropertyVerifierVerificationResult()
  {
    mCheckerStats = new ArrayList<VerificationResult>();
  }

  // #########################################################################
  // # Simple Access Methods
  /**
   * Get all results from Modular Language Inclusion checker runs (one result for each
   * prohibitable event).
   */
  public List<? extends VerificationResult> getCheckerResults()
  {
    return mCheckerStats;
  }

  //#########################################################################
  //# Providing Statistics
  /**
   * Sets all the language Inclusion checker results for this SD property verification
   * (one result for each prohibitable event).
   */
  public void setCheckerResult
    (final List<? extends VerificationResult> results)
  {
    mCheckerStats = results;
  }


  //#########################################################################
  //# Printing
  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    for (final VerificationResult result : mCheckerStats) {
      result.print(writer);
    }
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    for (final VerificationResult result : mCheckerStats) {
      writer.print(',');
      result.printCSVHorizontal(writer);
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    for (final VerificationResult result : mCheckerStats) {
      writer.print(',');
      result.printCSVHorizontalHeadings(writer);
    }
  }


  //#########################################################################
  //# Data Members
  private List<? extends VerificationResult> mCheckerStats;

}
