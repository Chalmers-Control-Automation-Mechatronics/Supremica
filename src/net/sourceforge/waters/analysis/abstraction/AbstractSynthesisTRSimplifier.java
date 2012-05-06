//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   AbstractSynthesisTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;


/**
 * <P>A supertype for transition relation simplifiers used by compositional
 * synthesis.</P>
 *
 * <P>This class implements the event encoding used by all simplifiers in
 * compositional synthesis to distinguish local from shared events and
 * controllable from uncontrollable events. This is necessary as compositional
 * synthesis does not use hiding, so the event codes of all local events are
 * carried through all abstractions.</P>
 *
 * <P>The encoding is as follows.</P>
 * <OL>
 * <LI>The silent event {@link EventEncoding#TAU} is not used.
 *     Some simplifiers may use this code to represent dummy termination
 *     (omega) events.</LI>
 * <LI>After the unused code of {@link EventEncoding#TAU}, the encoding
 *     starts with uncontrollable local events in the range from
 *     {@link EventEncoding#NONTAU} to
 *     {@link #getLastLocalUncontrollableEvent()} inclusive.</LI>
 * <LI>Next are controllable local events in the range from
 *     {@link #getLastLocalUncontrollableEvent()}+1 to
 *     {@link #getLastLocalControllableEvent()} inclusive.</LI>
 * <LI>Next are uncontrollable shared events in the range from
 *     {@link #getLastLocalControllableEvent()}+1 to
 *     {@link #getLastSharedUncontrollableEvent()} inclusive.</LI>
 * <LI>Last are controllable shared events in the range from
 *     {@link #getLastSharedUncontrollableEvent()}+1 to
 *     {@link #getLastSharedControllableEvent()} inclusive.</LI>
 * </OL>
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public abstract class AbstractSynthesisTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#########################################################################
  //# Constructors
  public AbstractSynthesisTRSimplifier()
  {
  }

  public AbstractSynthesisTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets the code of the last local uncontrollable event. Events are encoded
   * such that all local events appear before all shared events, and all
   * uncontrollable local events appear before controllable local events. The
   * tau event code ({@link EventEncoding#TAU} is not used. Therefore, the
   * range of uncontrollable local events is from {@link EventEncoding#NONTAU}
   * to {@link #getLastLocalUncontrollableEvent()} inclusive.
   */
  public void setLastLocalUncontrollableEvent(final int event)
  {
    mLastLocalUncontrollableEvent = event;
  }

  /**
   * Gets the code of the last local uncontrollable event.
   *
   * @see #setLastLocalUncontrollableEvent(int) setLastLocalUncontrollableEvent()
   */
  public int getLastLocalUncontrollableEvent()
  {
    return mLastLocalUncontrollableEvent;
  }

  /**
   * Sets the code of the last local controllable event. Events are encoded
   * such that all local events appear before all shared events, and all
   * uncontrollable local events appear before controllable local events.
   * Therefore, the range of controllable local events is from
   * {@link #getLastLocalUncontrollableEvent()}+1 to
   * {@link #getLastLocalControllableEvent()} inclusive.
   */
  public void setLastLocalControllableEvent(final int event)
  {
    mLastLocalControllableEvent = event;
  }

  /**
   * Gets the code of the last local controllable event.
   *
   * @see #setLastLocalControllableEvent(int) setLastLocalControllableEvent()
   */
  public int getLastLocalControllableEvent()
  {
    return mLastLocalControllableEvent;
  }

  /**
   * Sets the code of the last shared uncontrollable event. Events are encoded
   * such that all local events appear before all shared events, and all
   * uncontrollable local events appear before controllable events.
   * Therefore, the range of uncontrollable shared events is from
   * {@link #getLastLocalControllableEvent()}+1 to
   * {@link #getLastSharedUncontrollableEvent()} inclusive.
   */
  public void setLastSharedUncontrollableEvent(final int event)
  {
    mLastSharedUncontrollableEvent = event;
  }

  /**
   * Gets the code of the last shared uncontrollable event.
   *
   * @see #setLastSharedUncontrollableEvent(int) setLastSharedUncontrollableEvent()
   */
  public int getLastSharedUncontrollableEvent()
  {
    return mLastSharedUncontrollableEvent;
  }

  /**
   * Gets the code of the last shared controllable event.
   * Shared controllable events are last in the encoding, so this
   * method also returns the code of the last event overall.
   */
  public int getLastSharedControllableEvent()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    return rel.getNumberOfProperEvents() - 1;
  }


  //#########################################################################
  //# Access
  /**
   * Returns whether an event with the given code is controllable in
   * the encoding of this simplifier.
   */
  boolean isControllable(final int event)
  {
    return
      event > mLastLocalUncontrollableEvent &&
      event <= mLastLocalControllableEvent ||
      event > mLastSharedUncontrollableEvent;
  }

  /**
   * Returns whether an event with the given code is local in
   * the encoding of this simplifier.
   */
  boolean isLocal(final int event)
  {
    return event > EventEncoding.TAU && event <= mLastLocalControllableEvent;
  }

  /**
   * Resets the given transition iterator to iterate over all local
   * events in the encoding of this simplifier.
   */
  void iterateLocal(final TransitionIterator iter)
  {
    iter.resetEvents(EventEncoding.NONTAU, mLastLocalControllableEvent);
  }

  /**
   * Resets the given transition iterator to iterate over the local
   * uncontrollable events in the encoding of this simplifier.
   */
  void iterateLocalUncontrollable(final TransitionIterator iter)
  {
    iter.resetEvents(EventEncoding.NONTAU, mLastLocalUncontrollableEvent);
  }

  /**
   * Resets the given transition iterator to iterate over the local
   * controllable events in the encoding of this simplifier.
   */
  void iterateLocalControllable(final TransitionIterator iter)
  {
    iter.resetEvents(mLastLocalUncontrollableEvent + 1,
                     mLastLocalControllableEvent);
  }

  /**
   * Resets the given transition iterator to iterate over all shared
   * events in the encoding of this simplifier.
   */
  void iterateShared(final TransitionIterator iter)
  {
    final int last = getLastSharedControllableEvent();
    iter.resetEvents(mLastLocalControllableEvent + 1, last);
  }

  /**
   * Resets the given transition iterator to iterate over the shared
   * uncontrollable events in the encoding of this simplifier.
   */
  void iterateSharedUncontrollable(final TransitionIterator iter)
  {
    iter.resetEvents(mLastLocalControllableEvent + 1,
                     mLastSharedUncontrollableEvent);
  }

  /**
   * Resets the given transition iterator to iterate over the shared
   * controllable events in the encoding of this simplifier.
   */
  void iterateSharedControllable(final TransitionIterator iter)
  {
    final int last = getLastSharedControllableEvent();
    iter.resetEvents(mLastSharedUncontrollableEvent + 1, last);
  }

  /**
   * Creates a predecessors tau-closure using the local uncontrollable
   * events for the silent transitions.
   * @param limit The maximum number of transitions stored in the tau-closure.
   */
  TauClosure createLocalUncontrollablePredecessorsTauClosure(final int limit)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    return rel.createPredecessorsTauClosure(EventEncoding.NONTAU,
                                            mLastLocalUncontrollableEvent,
                                            limit);
  }

  /**
   * Creates a successors tau-closure using the local uncontrollable
   * events for the silent transitions.
   * @param limit The maximum number of transitions stored in the tau-closure.
   */
  TauClosure createLocalUncontrollableSuccessorsTauClosure(final int limit)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    return rel.createSuccessorsTauClosure(EventEncoding.NONTAU,
                                          mLastLocalUncontrollableEvent,
                                          limit);
  }


  //#########################################################################
  //# Data Members
  private int mLastLocalUncontrollableEvent;
  private int mLastLocalControllableEvent;
  private int mLastSharedUncontrollableEvent;

}
