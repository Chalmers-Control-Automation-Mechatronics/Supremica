//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   SynchronousProductBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Collection;

import net.sourceforge.waters.model.des.EventProxy;


/**
 * <P>The synchronous product algorithm. A synchronous product builder
 * takes a finite-state machine model ({@link
 * net.sourceforge.waters.model.des.ProductDESProxy ProductDESProxy}) as input
 * and computes a single automaton representing the synchronous product
 * of all automata contained in the input model.</P>
 *
 * @author Robi Malik
 */

public interface SynchronousProductBuilder extends AutomatonBuilder
{

  //#########################################################################
  //# Parameterisation
  /**
   * Defines the set of propositions to be retained in the synchronous
   * product. If specified, only the events from the given proposition set
   * will be copied to the output automaton, all others will be ignored.
   * @param  props       The set of propositions to be retained,
   *                     or <CODE>null</CODE> to keep all propositions.
   */
  public void setPropositions(final Collection<EventProxy> props);

  /**
   * Gets the set of propositions retained in the synchronous product.
   * @see {@link #setProposotions(Collection<EventProxy) setPropositions()}
   */
  public Collection<EventProxy> getPropositions();

  /**
   * Specifies an event mask for hiding. Events can be masked or hidden
   * by specifying a set of events to be masked and a replacement event.
   * When creating transitions of the output automaton, all events in the
   * mask will be replaced by the specified event. This method can be called
   * multiply; in this case, the result is undefined if the specified event
   * sets are not disjoint.
   * @param  hidden      A set of events to be replaced.
   * @param  replacement An event to be used instead of any of the hidden
   *                     events.
   */
  public void addMask(final Collection<EventProxy> hidden,
                      final EventProxy replacement);

  /**
   * Resets all event masks. This method clears any masks set by the
   * {@link addMask(Collection<EventProxy>,EventProxy) addMask()} method,
   * so any further computation is done without hiding.
   */
  public void clearMask();


  //#########################################################################
  //# More Specific Access to the Results
  /**
   * Gets a state map that can be used to decompose the states of the
   * computed synchronous product automaton and map them to the states
   * of the automata in the original model.
   */
  public SynchronousProductStateMap getStateMap();

}
