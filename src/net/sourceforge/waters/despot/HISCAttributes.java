//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.despot
//# CLASS:   HISCAttributes
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.despot;

import java.util.Collections;
import java.util.Map;


/**
 * <P>A collection of static methods and constants to facilitate the storage
 * and retrieval of HISC component and event types in attribute maps.</P>
 *
 * <P>This class helps to store and retrieve specific automaton and event
 * types used for Hierarchical Interface-Based Supervisory Control (HISC).
 * Since these types are not directly supported by Waters, they are stored
 * as application specific data in attribute maps. The following two
 * attributes are supported.</P>
 *
 * <UL>
 * <LI>Simple components
 *     ({@link net.sourceforge.waters.model.module.SimpleComponentProxy
 *     SimpleComponentProxy} and automata
 *     ({@link net.sourceforge.waters.model.des.AutomatonProxy AutomatonProxy}
 *     can be designated as an <I>HISC interface</I> using the key
 *     <CODE>&quot;HISC:Interface&quot;</CODE> ({@link #INTERFACE_KEY}).
 *     If the key is present in an attribute map, the corresponding
 *     automaton is designated as an HISC interface automaton, otherwise
 *     it is a normal automaton. The value associated with the key is
 *     irrelevant and should be kept to <CODE>&quot;&quot;</CODE>.
 *     For convenience, Boolean methods are provided to read or write
 *     the interface designation in attribute maps.</LI>
 * <LI>Event declarations
 *     ({@link net.sourceforge.waters.model.module.EventDeclProxy
 *     EventDeclProxy} and events
 *     ({@link net.sourceforge.waters.model.des.EventProxy EventProxy}
 *     can be assigned an HISC event type using the key
 *     <CODE>&quot;HISC:EventType&quot;</CODE> ({@link #EVENTTYPE_KEY}).
 *     Its associated values can be <CODE>&quot;REQUEST&quot;</CODE>,
 *     <CODE>&quot;ANSWER&quot;</CODE>, or <CODE>&quot;LOWDATA&quot;</CODE>;
 *     if the key is missing for an event, this defines the event to be
 *     of <I>default</I> or <I>local</I> type.
 *     For convenience, an enumeration {@link EventType} containing these
 *     values is provided, along with methods to read and write its values
 *     in attribute maps.</LI>
 * </UL>
 *
 * <P>This class contains only static methods and constants.</P>
 *
 * @author Robi Malik
 */

public class HISCAttributes
{

  //#########################################################################
  //# Constructor
  /**
   * Dummy constructor to prevent instantiation of class.
   */
  private HISCAttributes()
  {
  }


  //#########################################################################
  //# Static Methods
  /**
   * Checks whether the given attribute map designates an automaton as
   * an HISC interface.
   */
  public static boolean isInterface(final Map<String,String> attribs)
  {
    return attribs.containsKey(INTERFACE_KEY);
  }

  /**
   * Changes an attribute map to designate an automaton as an HISC
   * interface.
   * @param attribs  The attribute map to be modified.
   * @param iface    <CODE>true</CODE> if the attribute map should designate
   *                 an automaton as an HISC interface, <CODE>false</CODE>
   *                 if the automaton should not be an interface after the
   *                 call to this method.
   */
  public static void setInterface(final Map<String,String> attribs,
                                  final boolean iface)
  {
    if (iface) {
      attribs.put(INTERFACE_KEY, "");
    } else {
      attribs.remove(INTERFACE_KEY);
    }
  }

  /**
   * Returns the HISC event type specified by the given attribute map.
   */
  public static EventType getEventType(final Map<String,String> attribs)
  {
    final String value = attribs.get(EVENTTYPE_KEY);
    if (value == null) {
      return EventType.DEFAULT;
    } else {
      return EventType.valueOf(value);
    }
  }

  /**
   * Modifies the HISC event type specified by the given attribute map.
   */
  public static void setEventType(final Map<String,String> attribs,
                                  final EventType type)
  {
    if (type == EventType.DEFAULT) {
      attribs.remove(EVENTTYPE_KEY);
    } else {
      attribs.put(EVENTTYPE_KEY, type.toString());
    }
  }


  //#########################################################################
  //# Inner Enumeration Class EventKind
  public static enum EventType {
    /**
     * The default or local HISC event type.
     */
    DEFAULT,
    /**
     * The HISC request event type.
     */
    REQUEST,
    /**
     * The HISC answer event type.
     */
    ANSWER,
    /**
     * The HISC low data event type.
     */
    LOWDATA
  }


  //#########################################################################
  //# String Constants
  /**
   * The attribute key used to define the HISC event type.
   */
  public static final String EVENTTYPE_KEY = "HISC:EventType";
  /**
   * The attribute key used to designate components as an HISC interface.
   */
  public static final String INTERFACE_KEY = "HISC:Interface";


  //#########################################################################
  //# Attribute Map Constants
  /**
   * An unmodifiable attribute map that identifies an event as an HISC request
   * event. This map contains a single mapping for the {@link #EVENTTYPE_KEY}
   * key and nothing else.
   */
  public static final Map<String,String> ATTRIBUTES_REQUEST =
    Collections.singletonMap(EVENTTYPE_KEY, EventType.REQUEST.toString());
  /**
   * An unmodifiable attribute map that identifies an event as an HISC answer
   * event. This map contains a single mapping for the {@link #EVENTTYPE_KEY}
   * key and nothing else.
   */
  public static final Map<String,String> ATTRIBUTES_ANSWER =
    Collections.singletonMap(EVENTTYPE_KEY, EventType.ANSWER.toString());
  /**
   * An unmodifiable attribute map that identifies an event as an HISC low data
   * event. This map contains a single mapping for the {@link #EVENTTYPE_KEY}
   * key and nothing else.
   */
  public static final Map<String,String> ATTRIBUTES_LOWDATA =
    Collections.singletonMap(EVENTTYPE_KEY, EventType.LOWDATA.toString());
  /**
   * An unmodifiable attribute map that designates an automaton as an HISC
   * interface. This map contains a single mapping for the
   * {@link #INTERFACE_KEY} key and nothing else.
   */
  public static final Map<String,String> ATTRIBUTES_INTERFACE =
    Collections.singletonMap(INTERFACE_KEY, "");

}
