//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AutomatonBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis.des;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * <P>Interface of model analysers that compute a single automaton
 * as a result.</P>
 *
 * @author Robi Malik
 */

public interface AutomatonBuilder extends ModelBuilder<AutomatonProxy>
{

  //#########################################################################
  //# Configuration
  /**
   * Gets the component kind to be given to the output automaton.
   * @param  kind   Kind of output automaton, or <CODE>null</CODE> to
   *                indicate that the kind of the input automaton is to
   *                be used.
   */
  public void setOutputKind(ComponentKind kind);

  /**
   * Gets the configured component kind of the output automaton.
   * @see #setOutputKind(ComponentKind) setOutputKind()
   */
  public ComponentKind getOutputKind();

  //#########################################################################
  //# More Specific Access to the Results
  /**
   * Gets the automaton computed by this algorithm.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before {@link
   *         ModelAnalyzer#run() run()} has been called, or model checking
   *         has found that no proper result can be computed for the
   *         input model.
   */
  public AutomatonProxy getComputedAutomaton();

  public AutomatonResult getAnalysisResult();

}
