//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AutomatonResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.AutomatonProxy;


/**
 * A result record returned by an {@link AutomatonBuilder}. An automaton
 * result consists of a single automaton representing the result of an
 * analysis algorithm such as projection or automaton minimisation. In
 * addition, it may contain some statistics about the analysis run.
 *
 * @author Robi Malik
 */

public class AutomatonResult extends AnalysisResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an automaton result representing an incomplete run.
   */
  public AutomatonResult()
  {
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the automaton computed by the model checker,
   * or <CODE>null</CODE> if the computation was unsuccessful.
   */
  public AutomatonProxy getAutomaton()
  {
    return mAutomaton;
  }

  /**
   * Sets the computed automaton for this result.
   * Setting the automaton also marks the analysis run as completed and
   * sets the Boolean result.
   * @param  aut    The computed automaton, or <CODE>null</CODE> to
   *                indicate an unsuccessful computation. The Boolean analysis
   *                result is set to <CODE>false</CODE> if and only if this
   *                parameter is <CODE>null</CODE>.
   */
  public void setAutomaton(final AutomatonProxy aut)
  {
    setSatisfied(aut != null);
    mAutomaton = aut;
  }


  //#########################################################################
  //# Data Members
  private AutomatonProxy mAutomaton;

}
