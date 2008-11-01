//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   ControllableEventBDD
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import net.sf.javabdd.BDDFactory;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * @author Robi Malik
 */

class ControllableEventBDD extends EventBDD
{

  //#########################################################################
  //# Constructor
  ControllableEventBDD(final EventProxy event,
                       final int numautomata,
                       final BDDFactory factory)
  {
    super(event, numautomata, factory);
  }


  //#########################################################################
  //# Overrides for Abstract Base Class EventBDD
  EventKind getEventKind()
  {
    return EventKind.CONTROLLABLE;
  }

}
