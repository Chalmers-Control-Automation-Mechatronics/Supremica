//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   StateProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.des;

import java.util.Collection;

import net.sourceforge.waters.model.base.NamedProxy;


/**
 * <P>A state of an automaton.</P>
 *
 * <P>A state consists of a <I>name</I> (inherited from {@link NamedProxy}),
 * its <I>initial state</I> status, and a collection of <I>propositions</I>
 * or <I>markings</I>.
 *
 * @see AutomatonProxy
 * @author Robi Malik
 */

public interface StateProxy
  extends NamedProxy
{

  //#########################################################################
  //# Getters and Setters
  /**
   * Returns whether this is an initial state of its automaton.
   */
  public boolean isInitial();

  /**
   * Gets the collection of propositions associated with this state.
   * Propositions are generally used to determine whether a state is marked.
   * In a standard model, the list of propositions will either be empty
   * or contain a single proposition event with the {@link
   * net.sourceforge.waters.model.module.EventDeclProxy#DEFAULT_MARKING_NAME
   * DEFAULT_MARKING_NAME}. In multi-coloured models or Kripke-structures,
   * there may be more than one proposition associated with a state.
   * @return  An unmodifiable collection of events, which should all be of
   *          type {@link net.sourceforge.waters.xsd.base.EventKind#PROPOSITION
   *          PROPOSITION}.
   */
  public Collection<EventProxy> getPropositions();

}
