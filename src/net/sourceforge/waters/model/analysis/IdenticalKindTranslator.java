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
 * <P>A kind translator that does not change component and event
 * attributes. This simple kind translator implementation is used
 * for simple controllability checks.</P>
 *
 * @author Robi Malik
 */

public class IdenticalKindTranslator implements KindTranslator
{

  //#########################################################################
  //# Singleton Implementation
  public static IdenticalKindTranslator getInstance()
  {
    if (theInstance == null) {
      theInstance = new IdenticalKindTranslator();
    }
    return theInstance;
  }

  private IdenticalKindTranslator()
  {
  }


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


  //#########################################################################
  //# Static Class Variables
  private static IdenticalKindTranslator theInstance;

}
