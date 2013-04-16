//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.hisc
//# CLASS:   HISCConflictKindTranslator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.hisc;

import java.io.Serializable;
import java.util.Map;

import net.sourceforge.waters.model.analysis.des.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A kind translator used for HISC-CP conflict checking.
 * This translator remaps all plants, specifications, and supervisors as
 * plants, and in addition all interface automata are remapped as plants
 * regardless of their type. All controllable and uncontrollable events are
 * remapped as uncontrollable.</P>
 *
 * @author Robi Malik
 */

public class HISCConflictKindTranslator
  implements KindTranslator, Serializable
{

  //#########################################################################
  //# Singleton Pattern
  public static HISCConflictKindTranslator getInstance()
  {
    return SingletonHolder.theInstance;
  }

  private static class SingletonHolder {
    private static final HISCConflictKindTranslator theInstance =
      new HISCConflictKindTranslator();
  }

  private HISCConflictKindTranslator()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.KindTranslator
  /**
   * Returns the component kind of the given automaton in a conflict
   * check.
   * @return {@link ComponentKind#PLANT}, if the given automaton is
   *         a plant, spec, supervisor, or an interface.
   */
  public ComponentKind getComponentKind(final AutomatonProxy aut)
  {
    final Map<String,String> attribs = aut.getAttributes();
    if (HISCAttributeFactory.isInterface(attribs)) {
      return ComponentKind.PLANT;
    } else {
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
