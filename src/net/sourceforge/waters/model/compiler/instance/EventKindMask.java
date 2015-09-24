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

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.xsd.base.EventKind;


class EventKindMask {

  //#########################################################################
  //# Static Class Methods
  static int getMask(final EventKind kind)
  {
    return TYPEMASKS.get(kind);
  }

  static EventKind getKind(final int mask)
  {
    switch (mask) {
    case TYPEMASK_PROPOSITION:
      return EventKind.PROPOSITION;
    case TYPEMASK_CONTROLLABLE:
      return EventKind.CONTROLLABLE;
    case TYPEMASK_UNCONTROLLABLE:
    case TYPEMASK_EVENT:
      return EventKind.UNCONTROLLABLE;
    default:
      return null;
    }
  }

  static String getMaskName(final int mask)
  {
    switch (mask) {
    case TYPEMASK_PROPOSITION:
      return EventKind.PROPOSITION.toString();
    case TYPEMASK_CONTROLLABLE:
      return EventKind.CONTROLLABLE.toString();
    case TYPEMASK_UNCONTROLLABLE:
      return EventKind.UNCONTROLLABLE.toString();
    case TYPEMASK_EVENT:
      return "EVENT";
    default:
      throw new IllegalArgumentException
	("Illegal event kind mask " + mask + "!");
    }
  }

  static boolean isAssignable(final EventKind kind, final int mask)
  {
    if (kind.equals(EventKind.PROPOSITION)) {
      return (mask & ~TYPEMASK_PROPOSITION) == 0;
    } else if (kind.equals(EventKind.CONTROLLABLE)) {
      return (mask & ~TYPEMASK_CONTROLLABLE) == 0;
    } else if (kind.equals(EventKind.UNCONTROLLABLE)) {
      return (mask & ~TYPEMASK_EVENT) == 0;
    } else {
      throw new IllegalArgumentException("Illegal event kind " + kind + "!");
    }
  }


  //#########################################################################
  //# Class Constants
  static final int TYPEMASK_CONTROLLABLE = 1;
  static final int TYPEMASK_UNCONTROLLABLE = 2;
  static final int TYPEMASK_PROPOSITION = 4;

  static final int TYPEMASK_EVENT =
    TYPEMASK_CONTROLLABLE | TYPEMASK_UNCONTROLLABLE;
  static final int TYPEMASK_ANY =
    TYPEMASK_EVENT | TYPEMASK_PROPOSITION;

  private static final Map<EventKind,Integer> TYPEMASKS =
    new HashMap<EventKind,Integer>(3);
  static {
    TYPEMASKS.put(EventKind.CONTROLLABLE, TYPEMASK_CONTROLLABLE);
    TYPEMASKS.put(EventKind.UNCONTROLLABLE, TYPEMASK_UNCONTROLLABLE);
    TYPEMASKS.put(EventKind.PROPOSITION, TYPEMASK_PROPOSITION);
  }

}
