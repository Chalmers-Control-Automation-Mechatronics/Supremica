//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   VerificationResult
//###########################################################################
//# $Id: VerificationResult.java,v 1.5 2007-11-02 00:30:37 robi Exp $
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
   * @return The number of states, or <CODE>-1</CODE> if unknown.
   */
  public double getTotalNumberOfStates()
  {
    return mTotalNumberOfStates;
  }

  /**
   * Gets the maximum number of states nodes used during analysis.
   * A 'node' here represents a basic unit of memory such as a state
   * in a synchronous product or a BDD node.
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
   * Specifies a value for the total number of states constructed by the
   * analysis.
   * @throws IllegalStateException if the total number of states has been
   *         set by a previous call to this method.
   */
  public void setNumberOfStates(final double numstates)
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
   * Specifies the maximum number of states nodes used during analysis.
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
  private int mPeakNumberOfNodes;

}
