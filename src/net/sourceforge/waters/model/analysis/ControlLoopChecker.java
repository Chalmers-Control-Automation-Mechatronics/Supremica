//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ControlLoopChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.LoopTraceProxy;


/**
 * <P>A model verifier that checks for control loops. This model verifier
 * checks whether the synchronous composition of all automata in the input
 * model contains any non-empty loop consisting of controllable events
 * only. The set of loops events can be parameterised using a kind
 * translator.</P>
 *
 * @see KindTranslator
 * @author Robi Malik
 */

public interface ControlLoopChecker extends ModelVerifier
{

  //#########################################################################
  //# Simple Access Methods
  /**
   * Sets a kind translator to be used by this control-loop checker.
   */
  public void setKindTranslator(KindTranslator translator);

  /**
   * Gets the kind translator used by this control-loop checker.
   * Control-loops can only consist of events labelled controllable by
   * the kind translator; the component kind is ignored.
   */
  public KindTranslator getKindTranslator();


  //#########################################################################
  //# More Specific Access to the Results
  public LoopTraceProxy getCounterExample();

}
