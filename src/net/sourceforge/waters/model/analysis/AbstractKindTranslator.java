//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   IdenticalKindTranslator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A default implementation of kind translator that does not change
 * component and event attributes. It can be subclassed to provide more
 * advanced implementations.</P>
 *
 * @author Robi Malik
 */

public abstract class AbstractKindTranslator
  implements KindTranslator
{

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.KindTranslator
  /**
   * Returns the component kind of the given automaton.
   */
  public ComponentKind getComponentKind(final AutomatonProxy aut)
  {
    return aut.getKind();
  }

  /**
   * Returns the event kind of the given event.
   */
  public EventKind getEventKind(final EventProxy event)
  {
    return event.getKind();
  }

}
