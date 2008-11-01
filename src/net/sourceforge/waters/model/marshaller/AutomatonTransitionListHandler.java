//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   AutomatonTransitionListHandler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;

import net.sourceforge.waters.model.des.TransitionProxy;

import net.sourceforge.waters.xsd.des.Automaton;
import net.sourceforge.waters.xsd.des.ObjectFactory;
import net.sourceforge.waters.xsd.des.TransitionList;


class AutomatonTransitionListHandler
  extends JAXBListHandler<Automaton,TransitionList,TransitionProxy>
{
  
  //#########################################################################
  //# Constructors
  AutomatonTransitionListHandler()
  {
    this(null);
  }

  AutomatonTransitionListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }

  
  //#########################################################################
  //# Overrides for Abstract Base Class JAXBListHandler
  TransitionList createListElement(Automaton container)
  {
    final TransitionList listelem = mFactory.createTransitionList();
    container.setTransitionList(listelem);
    return listelem;
  }

  TransitionList getListElement(Automaton container)
  {
    return container.getTransitionList();
  }

  List getList(TransitionList listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
