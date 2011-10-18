//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ConflictKindTranslator
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.automata.algorithms;

import java.io.Serializable;

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A kind translator used for synthesis.
 * This translator remaps all plants and specifications as plants,
 * and all controllable and uncontrollable events as controllable.
 *
 * @author Robi Malik
 */

public class SynthesisKindTranslator
  implements KindTranslator, Serializable
{

  //#########################################################################
  //# Singleton Pattern
  public static SynthesisKindTranslator getInstance()
  {
    return SingletonHolder.theInstance;
  }

  private static class SingletonHolder {
    private static final SynthesisKindTranslator theInstance =
      new SynthesisKindTranslator();
  }

  private SynthesisKindTranslator()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.KindTranslator
  /**
   * Returns the component kind of the given automaton in a conflict
   * check.
   * @return {@link ComponentKind#PLANT}, if the given automaton is
   *         a plant, spec, or supervisor.
   */
  public ComponentKind getComponentKind(final AutomatonProxy aut)
  {
    final ComponentKind kind = aut.getKind();
    switch (kind) {
    case PLANT:
    case SPEC:
    case SUPERVISOR:
      return ComponentKind.PLANT;
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
    final EventKind kind = event.getKind();
    switch (kind) {
    case CONTROLLABLE:
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
