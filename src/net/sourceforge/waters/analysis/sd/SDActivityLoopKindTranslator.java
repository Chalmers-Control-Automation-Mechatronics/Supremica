//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.sd
//# CLASS:   SDActivityLoopKindTranslator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import java.io.Serializable;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.analysis.des.KindTranslator;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

/**
 * <P>A kind translator used for checking Activity Loops.
 * This translator re-labels uncontrollable events as controllable and labels the tick event as uncontrollable.</P>
 *
 * @author Mahvash Baloch
 */

public class SDActivityLoopKindTranslator
  implements KindTranslator, Serializable
{

  //#########################################################################
  //# Singleton Pattern
  public static SDActivityLoopKindTranslator getInstance()
  {
    return SingletonHolder.theInstance;
  }

  private static class SingletonHolder {
    private static final SDActivityLoopKindTranslator theInstance =
      new SDActivityLoopKindTranslator();
  }

  private SDActivityLoopKindTranslator()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.KindTranslator
  public ComponentKind getComponentKind(final AutomatonProxy aut)
  {
    final ComponentKind kind = aut.getKind();
    switch (kind) {
    case SPEC:
    case SUPERVISOR:
      return ComponentKind.SPEC;
    case PLANT:
      return ComponentKind.PLANT;
    default:
      return kind;
    }
  }

  public EventKind getEventKind(final EventProxy event)
  {
    final EventKind kind = event.getKind();
    final String evename = event.getName();
    switch (kind) {
    case CONTROLLABLE:
    if (evename.equals("tick"))
    	return EventKind.UNCONTROLLABLE;
    case UNCONTROLLABLE:
        return EventKind.CONTROLLABLE;
    default:
      return kind;
   }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
