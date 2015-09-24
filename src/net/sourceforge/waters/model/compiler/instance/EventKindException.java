//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.model.compiler.instance;

import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.EventDeclProxy;

import net.sourceforge.waters.xsd.base.EventKind;


public class EventKindException extends EvalException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs an exception indicating that an event value cannot be
   * used at a particular location. This exception may be thrown if the
   * user tries to use a proposition in and edge's label block. 
   * @param  event     The misplaced event.
   * @param  badmask   The part of the type mask that causes the error.
   */
  public EventKindException(final CompiledEvent event,
                            final int badmask)
  {
    mDecl = null;
    mEvent = event;
    mIndex = badmask;
    mDeclRange = null;
  }

  /**
   * Constructs an exception indicating that an event value cannot be
   * bound to a parameter because of event kind mismatch.
   * @param  decl      The event declaration representing the parameter
   *                   to be bound.
   * @param  event     The event that was attempted to bind to the
   *                   parameter.
   */
  public EventKindException(final EventDeclProxy decl,
                            final CompiledEvent event)
  {
    mDecl = decl;
    mEvent = event;
    mIndex = -1;
    mDeclRange = null;
  }

  /**
   * Constructs an exception indicating that an event value cannot be
   * bound to a parameter because of array range mismatch.
   * @param  decl      The event declaration representing the parameter
   *                   to be bound.
   * @param  event     The event that was attempted to bind to the
   *                   parameter.
   * @param  index     The number of the array range that does not match.
   */
  public EventKindException(final EventDeclProxy decl,
                            final CompiledEvent event,
                            final int index)
  {
    mDecl = decl;
    mEvent = event;
    mIndex = index;
    mDeclRange = null;
  }

  /**
   * Constructs an exception indicating that an event value cannot be
   * bound to a parameter because of array range mismatch.
   * @param  decl      The event declaration representing the parameter
   *                   to be bound.
   * @param  event     The event that was attempted to bind to the
   *                   parameter.
   * @param  index     The number of the array range that does not match.
   * @param  declRange The expected index range at the given position.
   */
  public EventKindException(final EventDeclProxy decl,
                            final CompiledEvent event,
                            final int index,
                            final CompiledRange declRange)
  {
    mDecl = decl;
    mEvent = event;
    mIndex = index;
    mDeclRange = declRange;
  }


  //#########################################################################
  //# Message
  public String getMessage()
  {
    if (mDecl == null) {
      final int badmask = mIndex;
      final Iterable<SingleEventOutput> outputs =
        new EventOutputIterable(mEvent);
      for (final SingleEventOutput output : outputs) {
        final CompiledSingleEvent event = output.getEvent();
        final int mask = event.getKindMask();
        if ((mask & badmask) != 0) {
          final String name = event.toString();
          final EventKind kind = event.getKind();
          return
            "Event '" + name + "' of kind " + kind + " is not allowed here!";
        }
      }
      return
        "<bad data in EventKindException: " +
        mDecl.getName() + " / " + mIndex + ">!";
    } else if (mIndex == -1) {
      final String declName = mDecl.getName();
      final EventKind declKind = mDecl.getKind();
      final boolean declObs = mDecl.isObservable();
      final Iterable<SingleEventOutput> outputs =
        new EventOutputIterable(mEvent);
      for (final SingleEventOutput output : outputs) {
        final CompiledSingleEvent event = output.getEvent();
        if (!EventKindMask.isAssignable(declKind, event.getKindMask())) {
          return
            "Can't assign event '" + event.toString() + "' of kind " +
            event.getKind() + " to parameter '" + declName +
            "' of kind " + declKind + "!";
        } else if (declObs && !event.isObservable()) {
          return
            "Can't assign observable event '" + event.toString() +
            "' to unobservable parameter '" + declName + "'!";
        }
      }
      return
        "<bad data in EventKindException: " +
        mDecl.getName() + " / " + mEvent + ">!";
    } else if (mDeclRange == null) {
      return
        "Can't assign event '" + mEvent + "' to parameter '" +
        mDecl.getName() + "': cannot accept " + mIndex + " array indexes!";
    } else {
      final CompiledRange eventRange = mEvent.getIndexRanges().get(mIndex);
      return
        "Can't assign event '" + mEvent + "' to parameter '" +
        mDecl.getName() + "': type mismatch for array range at position " +
        mIndex + ", expected " + mDeclRange + ", got " + eventRange + "!";
    }
  }


  //#########################################################################
  //# Data Members
  private final EventDeclProxy mDecl;
  private final CompiledEvent mEvent;
  private final int mIndex;
  private final CompiledRange mDeclRange;


  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
