//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   EventStatusProvider
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;


/**
 * Interface to define a mapping from event codes to status bits
 * for transition relations.
 *
 * @see EventStatus
 * @see DefaultEventStatusProvider
 * @see EventEncoding
 * @see ListBufferTransitionRelation
 *
 * @author Robi Malik
 */

public interface EventStatusProvider
  extends Cloneable
{

  //#########################################################################
  //# Interface java.lang.Cloneable
  public EventStatusProvider clone();


  //#########################################################################
  //# Simple Access
  /**
   * Gets the number of proper events, i.e., non-proposition events,
   * in this encoding. The number of proper events always includes the
   * silent (tau) event, even if none has been specified.
   */
  public int getNumberOfProperEvents();

  /**
   * Retrieves the status flags for the given proper event.
   * @param  event  Code of the proper event to be looked up.
   *                Must be in the range of events in the encoding.
   * @return A combination of the bits
   *         {@link EventStatus#STATUS_CONTROLLABLE},
   *         {@link EventStatus#STATUS_ALWAYS_ENABLED},
   *         {@link EventStatus#STATUS_SELFLOOP_ONLY},
   *         {@link EventStatus#STATUS_BLOCKED},
   *         {@link EventStatus#STATUS_FAILING}, and
   *         {@link EventStatus#STATUS_UNUSED}.
   */
  public byte getProperEventStatus(int event);

  /**
   * Assigns new status flags to the given proper event.
   * @param  event  Code of the proper event to be modified.
   *                Must be in the range of events in the encoding.
   * @param  status A combination of the bits
   *                {@link EventStatus#STATUS_CONTROLLABLE},
   *                {@link EventStatus#STATUS_ALWAYS_ENABLED},
   *                {@link EventStatus#STATUS_SELFLOOP_ONLY},
   *                {@link EventStatus#STATUS_BLOCKED},
   *                {@link EventStatus#STATUS_FAILING},
   *                and {@link EventStatus#STATUS_UNUSED}.
   */
  public void setProperEventStatus(int event, int status);

  /**
   * Gets the number of proposition events in this encoding.
   */
  public int getNumberOfPropositions();

  /**
   * Retrieves whether the given proposition is used. Propositions that
   * are not marked as used are assumed to be not in the alphabet of an
   * automaton and all states are assumed implicitly marked by these
   * propositions.
   * @param  prop   Code of the proposition to be looked up.
   *                Must be in the range of propositions in the encoding.
   */
  public boolean isPropositionUsed(int prop);

  /**
   * Assigns a new value for the used status of the given proposition.
   * @param  prop   Code of the proposition to be modified.
   *                Must be in the range of propositions in the encoding.
   * @param  used   <CODE>true</CODE> to mark the proposition as used,
   *                <CODE>false</CODE> to mark it as unused.
   * @see #isPropositionUsed(int) isPropositionUsed()
   */
  public void setPropositionUsed(int prop, boolean used);

  /**
   * Gets the pattern of propositions marked as used.
   * @return An integer with bits set for each proposition that is marked
   *         as used. If proposition <I>p</I> is marked as used, then the
   *         <I>p</I>-th bit (<CODE>1&nbsp;&lt;&lt;&nbsp;p</CODE>) in the
   *         result is set.
   */
  public int getUsedPropositions();

  /**
   * Gets the current ordering information for this event encoding.
   * If the event encoding has been ordered, the ordering information
   * can be used to iterate over events more efficiently.
   * @return Ordering information if available,
   *         <CODE>null</CODE> otherwise.
   * @see EventEncoding#sortProperEvents(byte...) EventEncoding.sortProperEvents()
   */
  public OrderingInfo getOrderingInfo();


  //#########################################################################
  //# Class Constants
  /**
   * The maximum number of propositions supported in an event encoding.
   * Currently only 30 propositions are supported to allows proposition
   * sets of states to be encoded in an <CODE>int</CODE> with room for
   * two additional status flags.
   * @see IntStateBuffer
   */
  public int MAX_PROPOSITIONS = 30;

}
