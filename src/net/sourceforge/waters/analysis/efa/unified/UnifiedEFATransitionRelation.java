//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   UnifiedEFATransitionRelation
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.efa.base.AbstractEFATransitionRelation;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


/**
 * A transition relation in a unified EFA system.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class UnifiedEFATransitionRelation
  extends AbstractEFATransitionRelation<AbstractEFAEvent>
{

  //#########################################################################
  //# Constructors
  public UnifiedEFATransitionRelation(final ListBufferTransitionRelation rel,
                                      final UnifiedEFAEventEncoding events,
                                      final List<SimpleNodeProxy> nodes)
  {
    super(rel, events, nodes);
  }

  public UnifiedEFATransitionRelation(final ListBufferTransitionRelation rel,
                                      final UnifiedEFAEventEncoding events)
  {
    this(rel, events, null);
  }


  //#########################################################################
  //# Simple Access
  public UnifiedEFAEventEncoding getEventEncoding()
  {
    return (UnifiedEFAEventEncoding) super.getTransitionLabelEncoding();
  }

  /**
   * Returns whether the given event is marked as used in the transition
   * relation.
   * @param  code   Code of event to be checked.
   */
  public boolean isUsedEvent(final int code)
  {
    return isUsedTransitionLabel(code);
  }

  /**
   * Returns whether the given event is marked as used in the transition
   * relation.
   */
  public boolean isUsedEvent(final AbstractEFAEvent event)
  {
    return isUsedTransitionLabel(event);
  }

  /**
   * Returns a set of all events in the encoding, except for the silent
   * event tau.
   * @return An unmodifiable set backed by the event encoding.
   */
  public List<AbstractEFAEvent> getAllEventsExceptTau()
  {
    return getEventEncoding().getEventsExceptTau();
  }

  /**
   * Returns a set of all events in the encoding, including the silent
   * event tau.
   * @return An unmodifiable set backed by the event encoding.
   */
  public List<AbstractEFAEvent> getAllEventsIncludingTau()
  {
    return getEventEncoding().getEventsIncludingTau();
  }

  /**
   * Returns a set of all events in the encoding that are marked as
   * used in the transition relation, except for the silent event tau.
   * @return An unmodifiable set backed by the event encoding.
   */
  public Set<AbstractEFAEvent> getUsedEventsExceptTau()
  {
    return getUsedTransitionLabels(0);
  }

  /**
   * Returns a set of all events in the encoding that are marked as
   * used in the transition relation, including the silent event tau.
   * @return An unmodifiable set backed by the event encoding.
   */
  public Set<AbstractEFAEvent> getUsedEventsIncludingTau()
  {
    return getUsedTransitionLabels(-1);
  }


  //#########################################################################
  //# Data Members

 }
