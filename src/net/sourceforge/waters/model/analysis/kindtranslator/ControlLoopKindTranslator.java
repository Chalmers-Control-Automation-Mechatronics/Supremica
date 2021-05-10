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

import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.Set;

import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.EventProxy;

/**
 * <P>A kind translator to perform a control-loop check with a custom
 * list of loop events. This translator is parameterised with a list of loop
 * events, which are remapped to be controllable, while other events are
 * remapped to be uncontrollable.</P>
 *
 * @author Robi Malik
 */

public class ControlLoopKindTranslator
  extends DefaultVerificationKindTranslator
{
  //#########################################################################
  //# Constructor
  /**
   * Creates a control loop kind translator
   * @param  loopEvents  Collection of events to be considered as loop
   *                     events. These events will be defined to be
   *                     controllable by the kind translator.
   */
  public ControlLoopKindTranslator
    (final Collection<? extends EventProxy> loopEvents)
  {
    this(loopEvents, EventKind.CONTROLLABLE);
  }

  /**
   * Creates a control loop kind translator
   * @param  loopEvents  Collection of events to be considered as loop
   *                     events.
   * @param  loopKind    The event kind assigned to loop events, either
   *                     {@link EventKind#CONTROLLABLE} or
   *                     {@link EventKind#UNCONTROLLABLE}.
   *                     Non-loop events are assigned the opposite,
   *                     except for propositions which are unchanged.
   */
  public ControlLoopKindTranslator
    (final Collection<? extends EventProxy> loopEvents,
     final EventKind loopKind)
  {
    mLoopEvents = new THashSet<>(loopEvents);
    mLoopKind = loopKind;
    mOtherKind = loopKind == EventKind.CONTROLLABLE ?
      EventKind.UNCONTROLLABLE : EventKind.CONTROLLABLE;
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator
  @Override
  public EventKind getEventKind(final EventProxy event)
  {
    if (event.getKind() == EventKind.PROPOSITION) {
      return EventKind.PROPOSITION;
    } else if (mLoopEvents.contains(event)) {
      return mLoopKind;
    } else {
      return mOtherKind;
    }
  }


  //#########################################################################
  //# Data Members
  private final Set<EventProxy> mLoopEvents;
  private final EventKind mLoopKind;
  private final EventKind mOtherKind;
}
