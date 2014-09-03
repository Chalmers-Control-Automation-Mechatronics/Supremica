//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   SupervisorSynthesizer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis.des;

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
   * <P>Sets the <I>marking proposition</I> to be used for synthesis.</P>
   * <P>The synthesised supervisor must ensure nonblocking with respect
   * to this proposition.</P>
   * <P>Every state has a list of propositions attached to it; the
   * synthesiser considers only those states as marked that are labelled by
   * <CODE>marking</CODE>, i.e., their list of propositions must contain
   * this event (exactly the same object).</P>
   * <P>A marking proposition of&nbsp;<CODE>null</CODE> may be specified to
   * use the <I>default marking</I>. In this case, the model must contain a
   * proposition event named {@link
   * net.sourceforge.waters.model.module#EventDeclProxy.DEFAULT_MARKING_NAME
   * EventDeclProxy.DEFAULT_MARKING_NAME}, which is used as marking
   * proposition. It is an error to request default marking, if no suitable
   * event is present.</P>
   * @param  marking  The marking proposition to be used,
   *                  or <CODE>null</CODE> to use the default marking
   *                  proposition of the model.
   */
  public void setConfiguredDefaultMarking(EventProxy marking);

  /**
   * Gets the <I>marking proposition</I> used for synthesis.
   * @return The current marking proposition or <CODE>null</CODE> to
   *         indicate default marking.
   * @see #setConfiguredDefaultMarking(EventProxy)
   */
  public EventProxy getConfiguredDefaultMarking();

  /**
   * <P>Sets whether synthesis should use supervisor reduction.</P>
   * <P>If enabled, every synthesised supervisor component may be replaced by
   * a single smaller automaton.</P>
   * <P><STRONG>Reference:</STRONG> Rong Su and W. Murray Wonham. Supervisor
   * Reduction for Discrete-Event Systems. Discrete Event Dynamic Systems
   * <STRONG>14</STRONG>&nbsp;(1), 31-53, 2004.</P>
   */
  public void setSupervisorReductionEnabled(final boolean enable);

  /**
   * Returns whether the synthesiser uses supervisor reduction.
   * @see #setSupervisorReductionEnabled(boolean) setSupervisorReductionEnabled()
   */
  public boolean getSupervisorReductionEnabled();

  /**
   * <P>Sets whether synthesis should use supervisor localisation.</P>
   * <P>If enabled, every synthesised supervisor component may be replaced by
   * several smaller automata, one for each controllable event to be disabled
   * by the supervisor.</P>
   * <P><STRONG>Reference:</STRONG> Kai Cai and W. M. Wonham. Supervisor
   * Localization: A Top-Down Approach to Distributed Control of Discrete
   * Event Systems. IEEE Transactions on Automatic Control,
   * <STRONG>55</STRONG>&nbsp;(3), 605-618, March 2010.</P>
   */
  public void setSupervisorLocalizationEnabled(final boolean enable);

  /**
   * Returns whether the synthesiser uses supervisor localisation.
   * @see #setSupervisorLocalizationEnabled(boolean) setSupervisorLocalizationEnabled()
   */
  public boolean getSupervisorLocalizationEnabled();

}
