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
 * result contains a single automaton (
 * {@link net.sourceforge.waters.model.des.AutomatonProxy AutomatonProxy})
 * representing the result of an analysis algorithm such as projection,
 * minimisation, or synthesis. In addition, it may contain some statistics
 * about the analysis run.
 *
 * @author Robi Malik
 */

public interface AutomatonResult
  extends ProxyResult<AutomatonProxy>
{

  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the automaton computed by the model analyser,
   * or <CODE>null</CODE> if the computation was unsuccessful.
   */
  public AutomatonProxy getComputedAutomaton();

  /**
   * Sets the computed automaton for this result. Setting the computed object
   * also marks the analysis run as completed and sets the Boolean result.
   * @param  aut    The computed automaton, or <CODE>null</CODE> to
   *                indicate an unsuccessful computation. The Boolean analysis
   *                result is set to <CODE>false</CODE> if and only if this
   *                parameter is <CODE>null</CODE>.
   */
  public void setComputedAutomaton(final AutomatonProxy aut);

}
