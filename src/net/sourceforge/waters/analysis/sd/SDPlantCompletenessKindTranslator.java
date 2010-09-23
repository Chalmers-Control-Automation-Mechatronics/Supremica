//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ControllabilityKindTranslator
//###########################################################################
//# $Id: ControllabilityKindTranslator.java 5206 2010-02-04 00:45:23Z robi $
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import java.io.Serializable;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.model.analysis.KindTranslator;

/**
 * <P>A kind translator used for controllability checking.
 * This translator relabels supervisors as specifications and otherwise
 * returns all component and event types as they are in the original
 * model.</P>
 *
 * @author Robi Malik
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
    switch (kind) {
    case CONTROLLABLE:
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
