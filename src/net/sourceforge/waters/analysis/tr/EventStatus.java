//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   EventStatus
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;


/**
 * Collection of status flags to provide additional information about events
 * in a transition relation. This class contains only static methods and
 * constants.
 *
 * @see EventStatusProvider
 * @see EventEncoding
 * @see ListBufferTransitionRelation
 *
 * @author Robi Malik
 */

public class EventStatus
{

  //#########################################################################
  //# Dummy Constructor
  private EventStatus()
  {
  }


  //#########################################################################
  //# Static Methods
  /**
   * Returns whether the given event status bits identify an event as
   * a controllable in an event encoding.
   */
  public static boolean isControllableEvent(final byte status)
  {
    return (status & STATUS_CONTROLLABLE) != 0;
  }

  /**
   * Returns whether the given event status bits identify an event as
   * a local event in an event encoding.
   */
  public static boolean isLocalEvent(final byte status)
  {
    return (status & STATUS_LOCAL) != 0;
  }

  /**
   * Returns whether the given event status bits identify an event as
   * selfloop-only in an event encoding.
   * @see #STATUS_SELFLOOP_ONLY
   */
  public static boolean isSelfloopOnlyEvent(final byte status)
  {
    return (status & STATUS_SELFLOOP_ONLY) != 0;
  }

  /**
   * Returns whether the given event status bits identify an event as
   * always enabled in an event encoding.
   * @see #STATUS_ALWAYS_ENABLED
   */
  public static boolean isAlwaysEnabledEvent(final byte status)
  {
    return (status & STATUS_ALWAYS_ENABLED) != 0;
  }

  /**
   * Returns whether the given event status bits identify an event as
   * blocked in an event encoding.
   * @see #STATUS_BLOCKED
   */
  public static boolean isBlockedEvent(final byte status)
  {
    return (status & STATUS_BLOCKED) != 0;
  }

  /**
   * Returns whether the given event status bits identify an event as
   * failing in an event encoding.
   * @see #STATUS_FAILING
   */
  public static boolean isFailingEvent(final byte status)
  {
    return (status & STATUS_FAILING) != 0;
  }

  /**
   * Returns whether the given event status bits identify an event as
   * used (i.e., not unused) in an event encoding.
   * @see #STATUS_UNUSED
   */
  public static boolean isUsedEvent(final byte status)
  {
    return (status & STATUS_UNUSED) == 0;
  }

  /**
   * Returns the combined status of an event in the synchronous composition
   * of two automata with the given status.
   */
  public static byte combine(final byte status1, final byte status2)
  {
    final int conj = (status1 & status2) & STATUS_BITS_CONJUNCTIVE;
    final int disj = (status1 | status2) & STATUS_BITS_DISJUNCTIVE;
    return (byte) (conj | disj);
  }

  public static void appendStatusInfo(final StringBuilder buffer,
                                      final byte status)
  {
    if (status != STATUS_NONE) {
      char sep = '<';
      byte bit = 1;
      for (final String name : STATUS_NAMES) {
        if ((status & bit) != 0) {
          buffer.append(sep);
          sep = ',';
          buffer.append(name);
        }
        bit <<= 1;
      }
      buffer.append('>');
    }
  }


  //#########################################################################
  //# Class Constants
  /**
   * An empty status byte to define an event with none of the available
   * status bits sets.
   */
  public static final byte STATUS_NONE = 0x00;
  /**
   * A status flag indicating a controllable event.
   */
  public static final byte STATUS_CONTROLLABLE = 0x01;
  /**
   * A status flags indicating a local event.
   * A local event is assumed not to be used in any other automaton
   * except the current one.
   * Unlike {@link #STATUS_ALWAYS_ENABLED} and
   * {@link #STATUS_SELFLOOP_ONLY}, this flag is purely informational.
   */
  public static final byte STATUS_LOCAL = 0x02;
  /**
   * A status flag indicating an event only appears in selfloop transitions.
   * Selfloops by events with this status flag are automatically suppressed
   * in a {@link ListBufferTransitionRelation} as it is assumed that other
   * automata use the event only as selfloops, so the event is subject to
   * selfloop removal.
   */
  public static final byte STATUS_SELFLOOP_ONLY = 0x04;
  /**
   * A status flag indicating an event is always enabled.
   */
  public static final byte STATUS_ALWAYS_ENABLED = 0x08;
  /**
   * A status flag indicating an event known to be globally disabled.
   */
  public static final byte STATUS_BLOCKED = 0x10;
  /**
   * A status flag indicating a <I>failing</I> event. In a conflict check,
   * failing events are events known to take the system to a blocking state.
   * In safety verification, failing events are events are events known to
   * cause the property checked to fail if they are ever enabled.
   */
  public static final byte STATUS_FAILING = 0x20;
  /**
   * A status flag indicating an event not in the alphabet of the current
   * transition relation. This event is assumed to be implicitly selflooped
   * in all states.
   */
  public static final byte STATUS_UNUSED = 0x40;

  /**
   * Status flags indicating a local event.
   * This is a combination of the bits {@link #STATUS_LOCAL},
   * {@link #STATUS_ALWAYS_ENABLED}, and
   * {@link #STATUS_SELFLOOP_ONLY}.
   * Although {@link #STATUS_LOCAL} usually implies the other two flags,
   * it is separated from the other two for synthesis and other applications,
   * where the automatic suppression of local selfloops is not desired.
   */
  public static final byte STATUS_FULLY_LOCAL =
    STATUS_LOCAL | STATUS_SELFLOOP_ONLY | STATUS_ALWAYS_ENABLED;

  /**
   * All status flags combined.
   */
  public static final byte STATUS_ALL =
    STATUS_CONTROLLABLE | STATUS_LOCAL | STATUS_SELFLOOP_ONLY |
    STATUS_ALWAYS_ENABLED | STATUS_BLOCKED | STATUS_FAILING |
    STATUS_UNUSED;
  /**
   * Status flags that combine conjunctively. The composition of two
   * or more automata only has this status when all automata have it.
   */
  public static final byte STATUS_BITS_CONJUNCTIVE =
    STATUS_CONTROLLABLE | STATUS_LOCAL | STATUS_SELFLOOP_ONLY |
    STATUS_ALWAYS_ENABLED | STATUS_UNUSED;
  /**
   * Status flags that combine disjunctively. The composition of two
   * or more automata has this status when at least one automaton has it.
   */
  public static final byte STATUS_BITS_DISJUNCTIVE =
    STATUS_BLOCKED | STATUS_FAILING;

  private static final String[] STATUS_NAMES = {
    "CONTROLLABLE", "LOCAL", "OUTSIDE_ONLY_SELFLOOP",
    "OUTSIDE_ALWAYS_ENABLED", "BLOCKED", "FAILING", "UNUSED"
  };
  /**
   * Status flags indicating a local event.
   * This is a combination of the bits {@link STATUS_LOCAL},
   * {@link STATUS_OUTSIDE_ALWAYS_ENABLED}, and
   * {@link STATUS_OUTSIDE_ONLY_SELFLOOP}.
   * Although {@link STATUS_LOCAL} usually implies the other two flags,
   * it is separated from the other two for synthesis and other applications,
   * where the automatic suppression of local selfloops is not desired.
   */

}
