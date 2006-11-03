//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   KindTranslator
//###########################################################################
//# $Id: KindTranslator.java,v 1.1 2006-11-03 01:00:07 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>This interface is used by safety verifiers ({@link SafetyVerifier})
 * to remap the types of events and automata.</P>
 *
 * <P>In Waters, each automaton is associated with a component kind ({@link
 * ComponentKind}) to indicate whether it is a plant, spec, or
 * property. Likewise, each event is associated with an event kind ({@link
 * EventKind}) to indicate whether it is a controllable or
 * uncontrollable. However, for several safety verification algorithms, it
 * is convenient to change these types as the algorithm runs. This
 * interface makes it possible to changes these attributes without having
 * to change the actual model that is being analysed.</P>
 *
 * @author Robi Malik
 */

public interface KindTranslator
{
  /**
   * Gets the component kind (plant, spec, etc.) to be associated with
   * the given automaton for the sake of analysis.
   */
  public ComponentKind getComponentKind(AutomatonProxy aut);

  /**
   * Gets the event kind (controllable, uncontrollable, etc.) to be
   * associated with the given event for the sake of analysis.
   */
  public EventKind getEventKind(EventProxy event);

}
