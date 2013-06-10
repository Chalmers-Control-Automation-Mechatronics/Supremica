//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   SupervisorSynthesizer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis.des;

import java.util.Collection;

import net.sourceforge.waters.model.des.EventProxy;


/**
 * <P>A general interface for synthesis algorithms.
 * A synthesiser takes a finite-state machine model ({@link
 * net.sourceforge.waters.model.des.ProductDESProxy ProductDESProxy}) as input
 * an automaton that, composed with the system, produce the largest
 * controllable and nonblocking sublanguage.</P>
 *
 * <P>The result is returned in a {@link
 * net.sourceforge.waters.model.des.ProductDESProxy ProductDESProxy}, so
 * implementations may also return a modular supervisor. In that case,
 * the actual supervisor is represented by the synchronous composition
 * of the automata in the returned result.</P>
 *
 * @author Robi Malik
 */

public interface SupervisorSynthesizer
  extends ProductDESBuilder
{

  //#########################################################################
  //# Parameterisation
  /**
   * Defines the set of propositions to be consider by synthesis.
   * product. If specified, the synthesised supervisor must be nonblocking
   * with respect to all propositions contained in this set. If unspecified,
   * the default marking proposition (named {@link
   * net.sourceforge.waters.model.module.EventDeclProxy#DEFAULT_MARKING_NAME
   * DEFAULT_MARKING_NAME}) will be used.
   * @param  props       The set of propositions to be retained,
   *                     or <CODE>null</CODE> to keep all propositions.
   */
  public void setPropositions(final Collection<EventProxy> props);

  /**
   * Gets the set of propositions considered by synthesis.
   * @see #setPropositions(Collection) setPropositions()
   */
  public Collection<EventProxy> getPropositions();

}
