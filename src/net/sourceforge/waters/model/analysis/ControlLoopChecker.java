//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ControlLoopChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Collection;

import net.sourceforge.waters.model.des.EventProxy;
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
  //# More Specific Access to the Results
  public LoopTraceProxy getCounterExample();

  /**
   * Gets a collection of events that are guaranteed not to be
   * contained in any control-loop after a failed call to
   * {@link #run()}. This is an optional method.
   * @throws IllegalStateException if this method is called before
   *         {@link #run()}, or if the last call to to {@link #run()}
   *         returned true.
   */
  public Collection<EventProxy> getNonLoopEvents();
}
