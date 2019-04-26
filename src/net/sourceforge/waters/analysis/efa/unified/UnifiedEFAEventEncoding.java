//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import net.sourceforge.waters.analysis.efa.base.AbstractEFATransitionLabelEncoding;


/**
 * @author Robi Malik, Sahar Mohajerani
 */

public class UnifiedEFAEventEncoding
  extends AbstractEFATransitionLabelEncoding<AbstractEFAEvent>
{

  //#########################################################################
  //# Constructors
  public UnifiedEFAEventEncoding(final String name)
  {
    this(name, DEFAULT_SIZE);
  }

  public UnifiedEFAEventEncoding(final String name, final int size)
  {
    super(size);
    final SilentEFAEvent tau = new SilentEFAEvent(name);
    createEventId(tau);
  }

  public UnifiedEFAEventEncoding(final UnifiedEFAEventEncoding encoding)
  {
    super(encoding);
  }


  //#########################################################################
  //# Simple Access
  public int getEventId(final AbstractEFAEvent event)
  {
    return super.getTransitionLabelId(event);
  }

  public AbstractEFAEvent getEvent(final int code)
  {
    return getTransitionLabel(code);
  }

  public AbstractEFAEvent getUpdate(final int code)
  {
    return super.getTransitionLabel(code);
  }

  public int createEventId(final AbstractEFAEvent event)
  {
    return super.createTransitionLabelId(event);
  }

  public void replaceEvent(final int code, final AbstractEFAEvent event)
  {
    replaceTransitionLabel(code, event);
  }

  /**
   * Retrieves the list of all events in this encoding, except the
   * silent (tau) event.
   * @return An unmodifiable list backed by the encoding.
   */
  public List<AbstractEFAEvent> getEventsExceptTau()
  {
    return getTransitionLabelsExceptTau();
  }

  /**
   * Retrieves the list of all events in this encoding, including the
   * silent (tau) event.
   * @return An unmodifiable list backed by the encoding.
   */
  public List<AbstractEFAEvent> getEventsIncludingTau()
  {
    return getTransitionLabelsIncludingTau();
  }


  //#########################################################################
  //# Class Constants
  public static final int OMEGA = 0;

}
