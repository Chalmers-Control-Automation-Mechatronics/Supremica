//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   AutomatonStateListHandler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;

import net.sourceforge.waters.model.des.StateProxy;

import net.sourceforge.waters.xsd.des.Automaton;
import net.sourceforge.waters.xsd.des.ObjectFactory;
import net.sourceforge.waters.xsd.des.State;
import net.sourceforge.waters.xsd.des.StateList;


class AutomatonStateListHandler
  extends JAXBCheckedListHandler<Automaton,StateList,StateProxy>
{
  
  //#########################################################################
  //# Constructors
  AutomatonStateListHandler()
  {
    this(null);
  }

  AutomatonStateListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }

  
  //#########################################################################
  //# Overrides for Abstract Base Class JAXBListHandler
  StateList createListElement(final Automaton container)
  {
    final StateList listelem = mFactory.createStateList();
    container.setStateList(listelem);
    return listelem;
  }

  StateList getListElement(final Automaton container)
  {
    return container.getStateList();
  }

  List<State> getList(final StateList listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
