//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.des;
//# CLASS:   ProductDESElementFactory
//###########################################################################
//# $Id: ProductDESElementFactory.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.des;

import java.io.File;
import java.util.Collection;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

/**
 * <P>A factory for the <I>plain</I> implementation of the Automaton
 * submodule.</P>
 *
 * @author Robi Malik
 */

public class ProductDESElementFactory
  implements ProductDESProxyFactory
{

  //#########################################################################
  //# Static Class Methods
  public static ProductDESElementFactory getInstance()
  {
    return INSTANCE;
  }

  
  //#########################################################################
  //# Factory Methods
  public ProductDESElement createProductDESProxy
    (final String name,
     final File location,
     final Collection<? extends EventProxy> events,
     final Collection<? extends AutomatonProxy> automata)
  {
    return new ProductDESElement(name, location, events, automata);
  }

  public AutomatonElement createAutomatonProxy
    (final String name,
     final ComponentKind kind,
     final Collection<? extends EventProxy> events,
     final Collection<? extends StateProxy> states,
     final Collection<? extends TransitionProxy> transitions)
  {
    return new AutomatonElement(name, kind, events, states, transitions);
  }

  public EventElement createEventProxy
    (final String name,
     final EventKind kind,
     final boolean observable)
  {
    return new EventElement(name, kind, observable);
  }

  public StateElement createStateProxy
    (final String name,
     final boolean initial,
     final Collection<? extends EventProxy> propositions)
  {
    return new StateElement(name, initial, propositions);
  }

  public TransitionElement createTransitionProxy
    (final StateProxy source,
     final EventProxy event,
     final StateProxy target)
  {
    return new TransitionElement(source, event, target);
  }


  //#########################################################################
  //# Static Class Variables
  private static final ProductDESElementFactory INSTANCE =
    new ProductDESElementFactory();

}
