//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ControllabilityKindTranslator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A kind translator used for controllable inclusion checking.
 * This translator remaps supervisor automata as specifications,
 * and makes no other changes.</P>
 *
 * @author Robi Malik
 */

public class ControllabilityKindTranslator implements KindTranslator
{

  //#########################################################################
  //# Singleton Implementation
  public static ControllabilityKindTranslator getInstance()
  {
    if (theInstance == null) {
      theInstance = new ControllabilityKindTranslator();
    }
    return theInstance;
  }

  private ControllabilityKindTranslator()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.KindTranslator
  /**
   * Returns the component kind of the given automaton in a controllability
   * check.
   * @return {@link ComponentKind#PLANT}, if the given automaton is
   *         a plant, or {@link ComponentKind#SPEC} if the
   *         given automaton is a spec or supervisor.
   */
  public ComponentKind getComponentKind(final AutomatonProxy aut)
  {
    final ComponentKind kind = aut.getKind();
    switch (kind) {
    case PLANT:
      return ComponentKind.PLANT;
    case SPEC:
    case SUPERVISOR:
      return ComponentKind.SPEC;
    default:
      return kind;
    }
  }

  /**
   * Returns the event kind of the given event in a language
   * inclusion check.
   * @return {@link EventKind#UNCONTROLLABLE}.
   */
  public EventKind getEventKind(final EventProxy event)
  {
    return event.getKind();
  }


  //#########################################################################
  //# Static Class Variables
  private static ControllabilityKindTranslator theInstance;

}
