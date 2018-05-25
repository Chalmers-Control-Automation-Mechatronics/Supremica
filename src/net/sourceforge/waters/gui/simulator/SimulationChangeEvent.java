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

package net.sourceforge.waters.gui.simulator;

import java.util.EventObject;

import net.sourceforge.waters.gui.observer.Subject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;


/**
 * <P>
 * A notification sent by a subject to inform its observers that its state has
 * been changed.
 * </P>
 *
 * <P>
 * All objects in the subject implementation support the <EMPH>observer design
 * pattern</EMPH>. Whenever some aspect of a subject is changed, a
 * <CODE>ModelChangeEvent</CODE> is created and passed to the
 * {@link ModelObserver#modelChanged(ModelChangeEvent) modelChanged()} method of
 * all registered observers, as well as all observers registered on some of its
 * parents.
 * </P>
 *
 * <P>
 * The model change event class provides detailed information about the change
 * that is signalled. The specific type of change signalled is identified by the
 * event <EMPH>kind</EMPH>, which can take the following values.
 * </P>
 * <UL>
 * <LI>{@link #MODEL_CHANGED}</LI>
 * <LI>{@link #STATE_CHANGED}</LI>
 * </UL>
 * <P>
 * In addition, each event has its <EMPH>source</EMPH> that identifies the item
 * that was affected by the change, and may have an additional
 * <EMPH>value</EMPH> and an index representing the position of a change in a
 * list. The precise meaning of these fields depends on the event kind.
 * </P>
 *
 * @see Subject
 * @see ModelObserver
 *
 * @author Andrew Holland
 */

public class SimulationChangeEvent extends EventObject
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new simulation change event with <CODE>null</CODE> value.
   *
   * @param source
   *          The source of the event.
   * @param kind
   *          The kind of notification.
   */
  public SimulationChangeEvent(final Simulation source, final int kind)
  {
    super(source);
    mKind = kind;
  }

  //#########################################################################
  //# Simple Access
  /**
   * Gets the source of this event.
   */
  @Override
  public Simulation getSource()
  {
    return (Simulation) super.getSource();
  }

  /**
   * Gets the specific kind of notification. There are two possible values
   * <UL>
   * <LI>{@link #MODEL_CHANGED}</LI>
   * <LI>{@link #STATE_CHANGED}</LI>
   * </UL>
   */
  public int getKind()
  {
    return mKind;
  }


  //#########################################################################
  //# Data Members
  private final int mKind;


  //#########################################################################
  //# Class Constants
  /**
   * The constant identifying a model change notification. This notification is
   * sent after a new simulation has been loaded, or a new ProductDES has been
   * compiled and loaded into the simulation.
   */
  public static final int MODEL_CHANGED = 1;

  /**
   * The constant identifying a state change notification.
   * This notification is sent when the current state of the simulation has
   * changed, for example after an event has been executed.
   */
  public static final int STATE_CHANGED = 2;


  private static final long serialVersionUID = 1;

}
