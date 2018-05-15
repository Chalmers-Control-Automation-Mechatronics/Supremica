//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.model.analysis.des;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;


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
   * proposition event named {@link EventDeclProxy#DEFAULT_MARKING_NAME},
   * which is used as marking proposition. It is an error to request default
   * marking, if no suitable event is present.</P>
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
   * <P>Sets whether the synthesiser allows nondeterministic input.</P>
   * <P>Although most synthesisers implicitly support nondeterminism, it is
   * not always clear how to interpret the result of synthesis when the
   * input is nondeterministic. If nondeterminism is disabled (the default),
   * the synthesiser must throw {@link NondeterministicDESException} when
   * encountering a nondeterministic automaton in the input. Otherwise it
   * may synthesise, although it may depend on the algorithm how to interpret
   * the result.</P>
   * <P>The method {@link #supportsNondeterminism()} should return whether
   * nondeterminism is enabled is disabled by this method.</P>
   */
  public void setNondeterminismEnabled(boolean enable);

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
