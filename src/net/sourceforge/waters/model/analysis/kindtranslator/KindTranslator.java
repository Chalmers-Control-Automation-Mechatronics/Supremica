//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.model.analysis.kindtranslator;

import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


/**
 * <P>This interface is used by safety verifiers ({@link SafetyVerifier})
 * to remap the types of events and automata.</P>
 *
 * <P>In Waters, each automaton is associated with a component kind ({@link
 * ComponentKind}) to indicate whether it is a plant, spec, or
 * property. Likewise, each event is associated with an event kind ({@link
 * EventKind}) to indicate whether it is a controllable or
 * uncontrollable. However, for several safety verification algorithms, it
 * is convenient to change these types as the algorithm runs. This
 * interface makes it possible to changes these attributes without having
 * to change the actual model that is being analysed.</P>
 *
 * @author Robi Malik
 */

public interface KindTranslator
{

  //#########################################################################
  //# Methods
  /**
   * Gets the component kind (plant, spec, etc.) to be associated with
   * the given automaton for the sake of analysis.
   */
  public ComponentKind getComponentKind(AutomatonProxy aut);

  /**
   * Gets the event kind (controllable, uncontrollable, etc.) to be
   * associated with the given event for the sake of analysis.
   */
  public EventKind getEventKind(EventProxy event);


  //#########################################################################
  //# Class Constants
  /**
   * Initial transition event marker.
   * This special event denotes a virtual transition into the initial state
   * of an automaton. It is passed to the {@link #getEventKind(EventProxy)
   * getEventKind()} method by {@link SafetyVerifier} implementations
   * to query the controllability status of this initial transition,
   * influencing the verification result in cases where the plant has
   * an initial state but the specification does not. If the initial
   * transition is uncontrollable, safety properties fail in this case.
   * The default is for the initial transition to be controllable, which
   * means that a model containing an automaton without an initial state
   * passes all property checks.
   */
  public static EventProxy INIT = ProductDESElementFactory.getInstance().
    createEventProxy(":init", EventKind.CONTROLLABLE, false);

}
