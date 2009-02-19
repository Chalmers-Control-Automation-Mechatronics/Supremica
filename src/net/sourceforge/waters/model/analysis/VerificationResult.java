//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   VerificationResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.TraceProxy;


/**
 * A result record returned by a {@link ModelAnalyser}.
 * A verification result contains the information on whether a property
 * checked is true or false, and in the latter case, it also contains
 * a counterexample. In addition, it may contain some statistics about
 * the analysis run.
 *
 * @author Robi Malik
 */

public class VerificationResult extends AnalysisResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a <I>true</I> verification result.
   * This constructor creates a verification result which indicates
   * that the property checked is true.
   */
  public VerificationResult()
  {
    this(true, null);
  }

  /**
   * Creates a <I>false</I> verification result.
   * This constructor creates a verification result which indicates
   * that the property checked is false, because of the given
   * counterexample.
   */
  public VerificationResult(final TraceProxy counterexample)
  {
    this(false, counterexample);
  }
  
  /**
   * Creates a verification result with parameters as given.
   */
  public VerificationResult(final boolean satisfied,
                            final TraceProxy counterexample)
  {
    super(satisfied);
    mCounterExample = counterexample;
    mTotalNumberOfAutomata = -1;
    mTotalNumberOfStates = -1.0;
    mPeakNumberOfStates = -1.0;
    mPeakNumberOfNodes = -1;
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the counter example computed by the model checker,
   * or <CODE>null</CODE> if the property checked was true.
   */
  public TraceProxy getCounterExample()
  {
    return mCounterExample;
  }
  
  /**
   * Gets the total number of automata used by the analysis.
   * @return The number of automata, or <CODE>-1</CODE> if unknown.
   */
  public int getTotalNumberOfAutomata()
  {
    return mTotalNumberOfAutomata;
  }

  /**
   * Gets the total number of states constructed by the analysis.
   * @return The total number of states, or <CODE>-1</CODE> if unknown.
   */
  public double getTotalNumberOfStates()
  {
    return mTotalNumberOfStates;
  }

  /**
   * Gets the maximum number of states constructed by the analysis.
   * The peak number of states should identify the size of the largest
   * automaton constructed. For monolithic algorithms, it will be
   * equal to the total number of states, but for compositional algorithms
   * it may be different.
   * @return The peak number of states, or <CODE>-1</CODE> if unknown.
   */
  public double getPeakNumberOfStates()
  {
    return mPeakNumberOfStates;
  }

  /**
   * <P>Gets the maximum number of nodes used during analysis.</P>
   * <P>A 'node' here represents a basic unit of memory such as a state
   * in a synchronous product or a BDD node.</P>
   * <P><I>Note.</I> It does not make much sense to speak of the total number
   * of nodes in BDD-based algorithms, as the final number of nodes
   * often is much smaller than the size of interim BDDs. Therefore,
   * no total number of nodes will be computed.</P>
   * @return The peak number of nodes, or <CODE>-1</CODE> if unknown.
   */
  public int getPeakNumberOfNodes()
  {
    return mPeakNumberOfNodes;
  }
  

  //#########################################################################
  //# Providing Statistics
  /**
   * Specifies a value for the total number of automata used by the
   * analysis.
   * @throws IllegalStateException if the total number of automata has been
   *         set by a previous call to this method.
   */
  public void setNumberOfAutomata(final int numaut)
  {
    if (mTotalNumberOfAutomata < 0) {
      mTotalNumberOfAutomata = numaut;
    } else {
      throw new IllegalStateException
	("Trying to overwrite previously set total number of automata " +
	 "in verification result!");
    }
  }

  /**
   * Specifies a value for both the peak and total number of states constructed
   * by the analysis.
   * @throws IllegalStateException if the total number of states has been
   *         set by a previous call to this method.
   */
  public void setNumberOfStates(final double numstates)
  {
    setTotalNumberOfStates(numstates);
    setPeakNumberOfStates(numstates);
  }

  /**
   * Specifies a value for the total number of states constructed by the
   * analysis.
   * @throws IllegalStateException if the total number of states has been
   *         set by a previous call to this method.
   */
  public void setTotalNumberOfStates(final double numstates)
  {
    if (mTotalNumberOfStates < 0) {
      mTotalNumberOfStates = numstates;
    } else {
      throw new IllegalStateException
	("Trying to overwrite previously set total number of states " +
	 "in verification result!");
    }
  }

  /**
   * Specifies a value for the peak number of states constructed by the
   * analysis.
   * @throws IllegalStateException if the total number of states has been
   *         set by a previous call to this method.
   */
  public void setPeakNumberOfStates(final double numstates)
  {
    if (mPeakNumberOfStates < 0) {
      mPeakNumberOfStates = numstates;
    } else {
      throw new IllegalStateException
	("Trying to overwrite previously set peak number of states " +
	 "in verification result!");
    }
  }

  /**
   * Specifies the maximum number of nodes used during analysis.
   * A 'node' here represents a basic unit of memory such as a state
   * in a synchronous product or a BDD node.
   * @throws IllegalStateException if the peak number of nodes has been
   *         set by a previous call to this method.
   */
  public void setPeakNumberOfNodes(final int numnodes)
  {
    if (mPeakNumberOfNodes < 0) {
      mPeakNumberOfNodes = numnodes;
    } else {
      throw new IllegalStateException
	("Trying to overwrite previously set peak number of nodes " +
	 "in verification result!");
    }
  }


  //#########################################################################
  //# Data Members
  private final TraceProxy mCounterExample;

  private int mTotalNumberOfAutomata;
  private double mTotalNumberOfStates;
  private double mPeakNumberOfStates;
  private int mPeakNumberOfNodes;

}
