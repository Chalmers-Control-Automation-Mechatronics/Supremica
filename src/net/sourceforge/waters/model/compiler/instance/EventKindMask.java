//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.instance
//# CLASS:   EventKindMask
//###########################################################################
//# $Id: EventKindMask.java,v 1.1 2008-06-16 07:09:51 robi Exp $
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
