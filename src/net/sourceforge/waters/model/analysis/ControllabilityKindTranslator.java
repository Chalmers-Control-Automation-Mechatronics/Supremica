//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ControllabilityKindTranslator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.Serializable;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A kind translator used for controllability checking.
 * This translator relabels supervisors as specifications and otherwise
 * returns all component and event types as they are in the original
 * model.</P>
 *
 * @author Robi Malik
 */

public class ControllabilityKindTranslator
  implements KindTranslator, Serializable
{

  //#########################################################################
  //# Singleton Pattern
  public static ControllabilityKindTranslator getInstance()
  {
    return SingletonHolder.theInstance;
  }

  private static class SingletonHolder {
    private static final ControllabilityKindTranslator theInstance =
      new ControllabilityKindTranslator();
  }

  private ControllabilityKindTranslator()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.KindTranslator
  public ComponentKind getComponentKind(final AutomatonProxy aut)
  {
    final ComponentKind kind = aut.getKind();
    switch (kind) {
    case SUPERVISOR:
      return ComponentKind.SPEC;
    default:
      return kind;
    }
  }

  public EventKind getEventKind(final EventProxy event)
  {
    return event.getKind();
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
