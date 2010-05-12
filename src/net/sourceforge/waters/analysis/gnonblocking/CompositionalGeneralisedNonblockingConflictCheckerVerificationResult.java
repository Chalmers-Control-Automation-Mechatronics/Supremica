package net.sourceforge.waters.analysis.gnonblocking;

import java.io.PrintStream;
import java.util.Formatter;

import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.TraceProxy;


/**
 * A result record returned by a {@link CompositionalGeneralisedConflictChecker}
 * . A verification result contains the information on whether a property
 * checked is true or false, and in the latter case, it also contains a
 * counterexample. In addition, it contains statistics about the analysis run.
 *
 * @author Rachel Francis
 */
public class CompositionalGeneralisedNonblockingConflictCheckerVerificationResult
    extends VerificationResult
{
  // #########################################################################
  // # Constructors
  /**
   * Creates a <I>true</I> verification result. This constructor creates a
   * verification result which indicates that the property checked is true.
   */
  public CompositionalGeneralisedNonblockingConflictCheckerVerificationResult()
  {
    this(true, null);
  }

  /**
   * Creates a <I>false</I> verification result. This constructor creates a
   * verification result which indicates that the property checked is false,
   * because of the given counterexample.
   */
  public CompositionalGeneralisedNonblockingConflictCheckerVerificationResult(
                                                                              final TraceProxy counterexample)
  {
    this(false, counterexample);
  }

  /**
   * Creates a verification result with parameters as given.
   */
  public CompositionalGeneralisedNonblockingConflictCheckerVerificationResult(
                                                                              final boolean satisfied,
                                                                              final TraceProxy counterexample)
  {
    super(satisfied, counterexample);

  }

  // #########################################################################
  // # Simple Access Methods
  // #########################################################################
  // # Providing Statistics

  // #########################################################################
  // # Printing
  @Override
  public void print(final PrintStream stream)
  {
    super.print(stream);
    @SuppressWarnings("unused")
    final Formatter formatter = new Formatter(stream);
    // TODO:add stats to print
  }

}
