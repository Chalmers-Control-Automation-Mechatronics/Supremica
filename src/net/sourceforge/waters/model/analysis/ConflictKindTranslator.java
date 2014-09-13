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
 * and all controllable and uncontrollable events as the same kind.
 * There are two instances of this kind translator that return either
 * controllable or uncontrollable events.
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
  public static ConflictKindTranslator getInstanceControllable()
  {
    return SingletonHolderControllable.theInstance;
  }

  public static ConflictKindTranslator getInstanceUncontrollable()
  {
    return SingletonHolderUncontrollable.theInstance;
  }

  private static class SingletonHolderControllable {
    private static final ConflictKindTranslator theInstance =
      new ConflictKindTranslator(EventKind.CONTROLLABLE);
  }

  private static class SingletonHolderUncontrollable {
    private static final ConflictKindTranslator theInstance =
      new ConflictKindTranslator(EventKind.UNCONTROLLABLE);
  }

  private ConflictKindTranslator(final EventKind kind)
  {
    mEventKind = kind;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.KindTranslator
  /**
   * Returns the component kind of the given automaton in a conflict
   * check.
   * @return {@link ComponentKind#PLANT}, if the given automaton is
   *         a plant, spec, or supervisor.
   */
  @Override
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
   * Returns the event kind of the given event in a conflict check.
   * @return Either {@link EventKind#CONTROLLABLE} or
   *         {@link EventKind#UNCONTROLLABLE}, depending on which
   *         version of the conflict kind translator is used.
   */
  @Override
  public EventKind getEventKind(final EventProxy event)
  {
    final EventKind kind = event.getKind();
    switch (kind) {
    case CONTROLLABLE:
    case UNCONTROLLABLE:
      return mEventKind;
    default:
      return kind;
    }
  }


  //#########################################################################
  //# Data Members
  private final EventKind mEventKind;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
