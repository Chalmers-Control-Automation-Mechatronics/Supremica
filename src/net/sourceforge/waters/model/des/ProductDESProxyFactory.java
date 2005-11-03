//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   ProductDESProxyFactory
//###########################################################################
//# $Id: ProductDESProxyFactory.java,v 1.2 2005-11-03 01:24:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.des;

import java.io.File;
import java.util.Collection;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public interface ProductDESProxyFactory
{
  public AutomatonProxy createAutomatonProxy
      (String name,
       ComponentKind kind,
       Collection<? extends EventProxy> events,
       Collection<? extends StateProxy> states,
       Collection<? extends TransitionProxy> transitions);

  public EventProxy createEventProxy
      (String name,
       EventKind kind,
       boolean observable);

  public ProductDESProxy createProductDESProxy
      (String name,
       File location,
       Collection<? extends EventProxy> events,
       Collection<? extends AutomatonProxy> automata);

  public StateProxy createStateProxy
      (String name,
       boolean initial,
       Collection<? extends EventProxy> propositions);

  public TransitionProxy createTransitionProxy
      (StateProxy source,
       EventProxy event,
       StateProxy target);

}
