//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   IdenticalKindTranslator
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.automata.algorithms;

import java.io.Serializable;

import net.sourceforge.waters.model.analysis.des.AbstractKindTranslator;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * <P>A kind translator for nonblocking-only synthesis.
 * This kind translator redefines all events to be controllable.</P>
 *
 * @author Robi Malik
 */

public class ControllableSynthesisKindTranslator
  extends AbstractKindTranslator
  implements Serializable
{

  //#########################################################################
  //# Singleton Implementation
  public static ControllableSynthesisKindTranslator getInstance()
  {
    return SingletonHolder.theInstance;
  }

  private static class SingletonHolder {
    private static final ControllableSynthesisKindTranslator theInstance =
      new ControllableSynthesisKindTranslator();
  }

  private ControllableSynthesisKindTranslator()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.KindTranslator
  @Override
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
  //# Singleton Implementation
  private static final long serialVersionUID = 1L;

}
