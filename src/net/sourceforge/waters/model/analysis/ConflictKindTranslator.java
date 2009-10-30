//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ConflictKindTranslator
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
 * <P>A kind translator used for conflict checking.
 * This translator remaps all plants and specifications as plants,
 * and all controllable and uncontrollable events as uncontrollable.
 * The use of kind translators is optional in conflict checking,
 * but the unification of types may make things easier for some
 * implementations.</P>
 *
 * @author Robi Malik
 */

public class ConflictKindTranslator
  implements KindTranslator, Serializable
{

  //#########################################################################
  //# Singleton Pattern
  public static ConflictKindTranslator getInstance()
  {
    return SingletonHolder.theInstance;
  }

  private static class SingletonHolder {
    private static final ConflictKindTranslator theInstance =
      new ConflictKindTranslator();
  }

  private ConflictKindTranslator()
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
    case SPEC:
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
      return EventKind.UNCONTROLLABLE;
    default:
      return kind;
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
