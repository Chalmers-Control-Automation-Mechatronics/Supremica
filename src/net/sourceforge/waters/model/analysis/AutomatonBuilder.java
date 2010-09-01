//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AutomatonBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * <P>An automaton builder takes a finite-state machine model as input,
 * performs some kind of analysis, and computes a new automaton as a
 * result. The input model may contain several automata, in which case the
 * system to be analysed is their synchronous product.</P>
 *
 * <P>To use an automaton builder, the user first creates an instance of a
 * subclass of this class, and sets up the model to be checked as well as
 * any other parameters that may be needed. Then the algorithm is started
 * using the {@link #run() run()} method. Afterwards results can be queried
 * using the {@link #getComputedAutomaton()} method.</P>
 *
 * <P>This interface is extended for different types of algorithms.</P>
 *
 * @author Robi Malik
 */

public interface AutomatonBuilder extends ModelAnalyser
{

  //#########################################################################
  //# Configuration
  /**
   * Sets the name to be given to the output automaton.
   * @param  name   Name for output automaton, or <CODE>null</CODE> to
   *                indicate that the name of the input automaton is to
   *                be used.
   */
  public void setOutputName(String name);

  /**
   * Gets the configured name of the output automaton.
   * @see {@link #setOutputName(String) setOutputName()}
   */
  public String getOutputName();

  /**
   * Gets the component kind to be given to the output automaton.
   * @param  name   Kind of output automaton, or <CODE>null</CODE> to
   *                indicate that the kind of the input automaton is to
   *                be used.
   */
  public void setOutputKind(ComponentKind kind);

  /**
   * Gets the configured component kind of the output automaton.
   * @see {@link #setOutputKind(ComponentKind) setOutputKind()}
   */
  public ComponentKind getOutputKind();


  //#########################################################################
  //# More Specific Access to the Results
  /**
   * Gets the automaton computed by this algorithm.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before {@link
   *         ModelAnalyser#run() run()} has been called, or model checking
   *         has found that no proper automaton can be computed for the
   *         input model.
   */
  public AutomatonProxy getComputedAutomaton();

  public AutomatonResult getAnalysisResult();

}
