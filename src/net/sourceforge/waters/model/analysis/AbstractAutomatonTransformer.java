//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractAutomatonTransformer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Collection;
import java.util.Collections;

import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * An automaton builder implementation that supports rewriting of automata.
 * This class provides more convenient access to a single automaton contained
 * in the input model.
 *
 * @author Robi Malik
 */

public abstract class AbstractAutomatonTransformer
  extends AbstractAutomatonBuilder
{

  //#########################################################################
  //# Constructors
  public AbstractAutomatonTransformer(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public AbstractAutomatonTransformer(final ProductDESProxy model,
                                      final ProductDESProxyFactory factory)
  {
    super(model, factory);
  }

  public AbstractAutomatonTransformer(final AutomatonProxy aut,
                                      final ProductDESProxyFactory factory)
  {
    super(createAutomatonModel(aut, factory), factory);
  }


  //#########################################################################
  //# Configuration
  public void setModel(final AutomatonProxy aut)
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = createAutomatonModel(aut, factory);
    setModel(des);
  }


  //#########################################################################
  //# Auxiliary Access
  protected AutomatonProxy getInputAutomaton()
    throws InvalidModelException
  {
    final ProductDESProxy des = getModel();
    final Collection<AutomatonProxy> automata = des.getAutomata();
    if (automata.size() == 1) {
      return automata.iterator().next();
    } else {
      throw new InvalidModelException
        ("The input product DES '" + des.getName() +
         "' does not contain exactly one automaton, which is required for " +
         ProxyTools.getShortClassName(this) + "!");
    }
  }


  //#########################################################################
  //# Static Methods
  public static ProductDESProxy createAutomatonModel
    (final AutomatonProxy aut, final ProductDESProxyFactory factory)
  {
    final String name = aut.getName();
    final Collection<EventProxy> events = aut.getEvents();
    final Collection<AutomatonProxy> automata = Collections.singletonList(aut);
    return factory.createProductDESProxy(name, events, automata);
  }

}
