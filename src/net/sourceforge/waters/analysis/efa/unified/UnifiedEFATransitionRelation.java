//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.analysis.efa.unified;

import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.efa.base.AbstractEFATransitionRelation;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


/**
 * A transition relation in a unified EFA system.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class UnifiedEFATransitionRelation
  extends AbstractEFATransitionRelation<AbstractEFAEvent>
{

  //#########################################################################
  //# Constructors
  public UnifiedEFATransitionRelation(final ListBufferTransitionRelation rel,
                                      final UnifiedEFAEventEncoding events,
                                      final List<SimpleNodeProxy> nodes)
  {
    super(rel, events, nodes);
  }

  public UnifiedEFATransitionRelation(final ListBufferTransitionRelation rel,
                                      final UnifiedEFAEventEncoding events)
  {
    this(rel, events, null);
  }


  //#########################################################################
  //# Simple Access
  public UnifiedEFAEventEncoding getEventEncoding()
  {
    return (UnifiedEFAEventEncoding) super.getTransitionLabelEncoding();
  }

  /**
   * Returns whether the given event is marked as used in the transition
   * relation.
   * @param  code   Code of event to be checked.
   */
  public boolean isUsedEvent(final int code)
  {
    return isUsedTransitionLabel(code);
  }

  /**
   * Returns whether the given event is marked as used in the transition
   * relation.
   */
  public boolean isUsedEvent(final AbstractEFAEvent event)
  {
    return isUsedTransitionLabel(event);
  }

  /**
   * Returns a set of all events in the encoding, except for the silent
   * event tau.
   * @return An unmodifiable set backed by the event encoding.
   */
  public List<AbstractEFAEvent> getAllEventsExceptTau()
  {
    return getEventEncoding().getEventsExceptTau();
  }

  /**
   * Returns a set of all events in the encoding, including the silent
   * event tau.
   * @return An unmodifiable set backed by the event encoding.
   */
  public List<AbstractEFAEvent> getAllEventsIncludingTau()
  {
    return getEventEncoding().getEventsIncludingTau();
  }

  /**
   * Returns a set of all events in the encoding that are marked as
   * used in the transition relation, except for the silent event tau.
   * @return An unmodifiable set backed by the event encoding.
   */
  public Set<AbstractEFAEvent> getUsedEventsExceptTau()
  {
    return getUsedTransitionLabels(0);
  }

  /**
   * Returns a set of all events in the encoding that are marked as
   * used in the transition relation, including the silent event tau.
   * @return An unmodifiable set backed by the event encoding.
   */
  public Set<AbstractEFAEvent> getUsedEventsIncludingTau()
  {
    return getUsedTransitionLabels(-1);
  }


  //#########################################################################
  //# Data Members

 }
