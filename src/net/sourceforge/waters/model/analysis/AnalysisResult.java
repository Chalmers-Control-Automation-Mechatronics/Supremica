//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AnalysisResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.PrintStream;
import java.io.PrintWriter;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.module.ModuleProxy;


/**
 * A record containing the result of a {@link ModelAnalyzer}.
 *
 * The analysis result contains all data returned from running an analysis
 * algorithm. This includes computed data such as counterexamples or generated
 * automata as well as runtime statistics.
 *
 * This base class only provides basic information, so it can be determined
 * whether the analysis operation has terminated and if an exception has
 * occurred. It also can store a Boolean result and the time taken, and it
 * records basic statistics about automata sizes. More specific result data
 * can be added by subclasses.
 *
 * @author Robi Malik
 */

public interface AnalysisResult
{

  //#########################################################################
  //# Read Access
  /**
   * Gets the termination status of this analysis result. An analysis result may
   * be created while an analysis operation is in progress.
   * @return <CODE>true</CODE> to confirm that the result represents a completed
   *         analysis run, <CODE>false</CODE> otherwise.
   */
  public boolean isFinished();

  /**
   * Gets the Boolean analysis result. The Boolean result typically indicates
   * whether model checking has found a property of interest to be true, or
   * whether synthesis or a similar has been successful.
   */
  public boolean isSatisfied();

  /**
   * Gets the exception produced by the analysis run. If analysis is aborted by
   * an exception, the exception should be stored on the analysis result. Note,
   * if an exception is set, the Boolean result and other information may not be
   * accurate.
   * @see #isSatisfied()
   */
  public WatersException getException();

  /**
   * Gets the runtime of the operation that produced this result.
   * @return Time taken, in milliseconds. A value of <CODE>-1</CODE> indicates
   *         that timing information is not available.
   */
  public long getRunTime();

  /**
   * Gets the compile time recorded for this analysis result.
   * The compile time measures the time spent by the {@link ModuleCompiler}
   * to compile the input {@link ModuleProxy} to a {@link ProductDESProxy},
   * in milliseconds.
   */
  public long getCompileTime();

  /**
   * Gets the peak memory usage. Memory usage is determined by the maximum
   * amount of memory allocated by the Java virtual machine and any dependent
   * libraries at any one time during the complete analysis.
   * @return Memory used in bytes.
   */
  public long getPeakMemoryUsage();

  /**
   * Gets the total number of automata used by the analysis.
   * @return The number of automata, or <CODE>-1</CODE> if unknown.
   */
  public int getTotalNumberOfAutomata();


  /**
   * Gets the total number of states constructed by the analysis.
   * @return The total number of states, or <CODE>-1</CODE> if unknown.
   */
  public double getTotalNumberOfStates();


  /**
   * Gets the maximum number of states constructed by the analysis. The peak
   * number of states should identify the size of the largest automaton
   * constructed. For monolithic algorithms, it will be equal to the total
   * number of states, but for compositional algorithms it may be different.
   * @return The peak number of states, or <CODE>-1</CODE> if unknown.
   */
  public double getPeakNumberOfStates();

  /**
   * <P>
   * Gets the maximum number of nodes used during analysis.
   * </P>
   * <P>
   * A 'node' here represents a basic unit of memory such as a state in a
   * synchronous product or a BDD node.
   * </P>
   * <P>
   * <I>Note.</I> It does not make much sense to speak of the total number of
   * nodes in BDD-based algorithms, as the final number of nodes often is much
   * smaller than the size of interim BDDs. Therefore, no total number of nodes
   * will be computed.
   * </P>
   *
   * @return The peak number of nodes, or <CODE>-1</CODE> if unknown.
   */
  public int getPeakNumberOfNodes();


  /**
   * Gets the total number of transitions constructed by the analysis.
   * @return The total number of transitions, or <CODE>-1</CODE> if unknown.
   */
  public double getTotalNumberOfTransitions();

  /**
   * Gets the maximum number of transitions constructed by the analysis. The
   * peak number of transitions should identify the size of the largest
   * automaton constructed. For monolithic algorithms, it will be equal to the
   * total number of transitions, but for compositional algorithms it may be
   * different.
   * @return The peak number of transitions, or <CODE>-1</CODE> if unknown.
   */
  public double getPeakNumberOfTransitions();


  //#########################################################################
  //# Write Access
  /**
   * Sets the Boolean analysis result. Setting the result also marks the result
   * run as 'finished'.
   * @see #isSatisfied()
   * @see #isFinished()
   */
  public void setSatisfied(final boolean sat);

  /**
   * Stores an exception on this analysis result. Setting the result also marks
   * the result run as 'finished'.
   * @see #isFinished()
   */
  public void setException(final WatersException exception);

  /**
   * Sets a runtime for this result.
   * @param time
   *          Time to be stored, in milliseconds.
   */
  public void setRuntime(final long time);

  /**
   * Sets a compile for this result.
   * @param time
   *          Time to be stored, in milliseconds.
   */
  public void setCompileTime(final long time);

  /**
   * Updates the recorded memory usage.
   * This method checks whether the given usage exceeds the currently
   * recorded memory usage, and if so, updates the recorded usage.
   * @param usage
   *          Amount of memory currently used, in bytes.
   */
  public void updatePeakMemoryUsage(final long usage);

  /**
   * Specifies a value for both the peak and total number of states constructed
   * by the analysis.
   */
  public void setNumberOfStates(final double numstates);

  /**
   * Specifies a value for the total number of automata used by the analysis.
   */
  public void setNumberOfAutomata(final int numaut);

  /**
   * Specifies a value for the total number of states constructed by the
   * analysis.
   */
  public void setTotalNumberOfStates(final double numstates);

  /**
   * Specifies a value for the peak number of states constructed by the
   * analysis.
   */
  public void setPeakNumberOfStates(final double numstates);

  /**
   * Specifies a value for both the peak and total number of transitions
   * constructed by the analysis.
   */
  public void setNumberOfTransitions(final double numtrans);

  /**
   * Specifies a value for the total number of transitions constructed by the
   * analysis.
   */
  public void setTotalNumberOfTransitions(final double numtrans);

  /**
   * Specifies a value for the peak number of transitions constructed by the
   * analysis.
   */
  public void setPeakNumberOfTransitions(final double numtrans);

  /**
   * Specifies the maximum number of nodes used during analysis. A 'node' here
   * represents a basic unit of memory such as a state in a synchronous product
   * or a BDD node.
   */
  public void setPeakNumberOfNodes(final int numnodes);


  //#########################################################################
  //# Merging
  /**
   * <P>Merges this result with another.</P>
   *
   * <P>This method destructively modifies the contents of this result record
   * by merging in the contents of the given other record.</P>
   *
   * <P>Merging is done for the purpose of accumulating statistics over
   * multiple runs of the same algorithm. The way how information is merged
   * depends on the type of data stored, e.g., run times and totals of state
   * numbers will be added, while for other values such as peak state number
   * the maximum value will be chosen. Not all result data makes sense for
   * multiple runs, so items that are only relevant for a single run (such
   * as counterexamples) may be removed by this method.</P>
   * @param other
   *          The record to be merged into this record.
   * @throws ClassCaseException
   *          to indicate that the two records merged are not of exactly the
   *          same type.
   */
  public void merge(final AnalysisResult other);


  //#########################################################################
  //# Printing
  public void print(final PrintStream stream);

  public void print(final PrintWriter writer);

  public void printCSVHorizontal(final PrintWriter writer);

  public void printCSVHorizontalHeadings(final PrintWriter writer);

}
