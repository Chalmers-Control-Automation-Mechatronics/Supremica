//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.sd
//# CLASS:   SDPlantCompletenessKindTranslator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import java.io.Serializable;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.model.analysis.KindTranslator;

/**
 * <P>A kind translator used for plant completeness checking.
 * This translator relabels supervisors and specifications as plants
 * and plants as specifications. Furthermore, controllable and
 * uncontrollable events are swapped.</P>
 *
 * @author Mahvash Baloch, Robi Malik
 */

public class SDPlantCompletenessKindTranslator
  implements KindTranslator, Serializable
{

  //#########################################################################
  //# Singleton Pattern
  public static SDPlantCompletenessKindTranslator getInstance()
  {
    return SingletonHolder.theInstance;
  }

  private static class SingletonHolder {
    private static final SDPlantCompletenessKindTranslator theInstance =
      new SDPlantCompletenessKindTranslator();
  }

  private SDPlantCompletenessKindTranslator()
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
      return ComponentKind.PLANT;
    case PLANT:
      return ComponentKind.SPEC;
    default:
      return kind;
    }
  }

  public EventKind getEventKind(final EventProxy event)
  {
    final EventKind kind = event.getKind();
    final String eName = event.getName();
    switch (kind) {
    case CONTROLLABLE:
      if(eName.equals("tick"))
        return kind;
      else
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
