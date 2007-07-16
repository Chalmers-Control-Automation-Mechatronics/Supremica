//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   ProductDESProxy
//###########################################################################
//# $Id: ProductDESProxy.java,v 1.6 2007-07-16 11:34:32 flordal Exp $
//###########################################################################

package net.sourceforge.waters.model.des;

import java.util.Set;

import net.sourceforge.waters.model.base.DocumentProxy;

/**
 * <P>A collection of finite-state machines.</P>
 *
 * <P>A product DES is a set of finite-state machines that are to be
 * composed by the synchronous product operation, synchronising by shared
 * events. The product DES includes the event alphabet available to all
 * finite-state machines, but each finite-state machine has its own event
 * alphabet identifying the set of events it actually synchronises on.</P>
 *
 * <P>In contrast to the module representation ({@link
 * net.sourceforge.waters.model.module.ModuleProxy}), the product DES
 * representation is very simple and supports no parametric
 * structures. Most product DES are obtained from a module by
 * compilation.</P>
 * 
 * @see net.sourceforge.waters.model.module.ModuleProxy
 * @see net.sourceforge.waters.model.compiler.ModuleCompiler
 *
 * @author Robi Malik
 */
public interface ProductDESProxy
  extends DocumentProxy
{
  //#########################################################################
  //# Cloning
  public ProductDESProxy clone();

  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the set of events for this product DES.
   * @return  An unmodifiable set of objects of type {@link EventProxy}.
   */
  public Set<EventProxy> getEvents();

  /**
   * Gets the list of automata for this product DES.
   * @return  An unmodifiable list of objects of type {@link AutomatonProxy}.
   */
  public Set<AutomatonProxy> getAutomata();
}
