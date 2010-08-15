//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   SafetyProjectionBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import java.util.Set;

import net.sourceforge.waters.model.analysis.AutomatonBuilder;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * The interface for projection algorithms used in safety verification.
 *
 * A projection builder takes one or more automata as input, builds their
 * synchronous product, then hides events to be projected out, and afterwards
 * uses subset construction to build an automaton accepting the same language.
 * The final result may be minimised before it is returned.
 *
 * @author Robi Malik
 */
public interface SafetyProjectionBuilder extends AutomatonBuilder
{

  //#########################################################################
  //# Configuration
  /**
   * Gets the set of events projected out by this projection.
   * Hidden events are replaced by silent events (epsilon-moves)
   * after synchronous composition and before subset construction.
   */
  public Set<EventProxy> getHidden();

  /**
   * Sets the set of events projected out by this projection.
   * @see #getHidden()
   */
  public void setHidden(final Set<EventProxy> hidden);

  /**
   * Gets the set of forbidden events of this safety projection.
   * In safety verification, it may be known for certain events that the
   * property to be verified fails if one of these events is possible.
   * With this knowledge, state exploration can be stopped as soon as a
   * forbidden event is found to be possible, leading to a simpler
   * projection result.
   */
  public Set<EventProxy> getForbidden();

  /**
   * Sets the set of forbidden events of this safety projection.
   * @see #getForbidden()
   */
  public void setForbidden(final Set<EventProxy> forbidden);

}
