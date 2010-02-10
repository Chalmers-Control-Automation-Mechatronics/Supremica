//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractLanguageInclusionKindTranslator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A kind translator used for language inclusion checking.
 * This translator remaps all events to be uncontrollable,
 * all specs and supervisors are considered as plants, and all properties are
 * considered as specs. Such a remapping makes it possible to
 * implement language inclusion checking using a controllability
 * checker.</P>
 * <P>This is a non-serialisable, abstract default implementation of a
 * language inclusion kind translator, which is subclassed for more specific
 * implementations.</P>
 *
 * @author Robi Malik
 */

public abstract class AbstractLanguageInclusionKindTranslator
  implements KindTranslator
{

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.KindTranslator
  /**
   * Returns the component kind of the given automaton in a language
   * inclusion check.
   * @return {@link ComponentKind#PLANT}, if the given automaton is
   *         a plant, spec, or supervisor, or {@link ComponentKind#SPEC} if
   *         the given automaton is a property.
   */
  public ComponentKind getComponentKind(final AutomatonProxy aut)
  {
    final ComponentKind kind = aut.getKind();
    switch (kind) {
    case PLANT:
    case SPEC:
    case SUPERVISOR:
      return ComponentKind.PLANT;
    case PROPERTY:
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
    final EventKind kind = event.getKind();
    switch (kind) {
    case CONTROLLABLE:
    case UNCONTROLLABLE:
      return EventKind.UNCONTROLLABLE;
    default:
      return kind;
    }
  }

}
