//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.hisc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.AttributeFactory;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;


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
 *     in attribute maps.<BR>
 *     In addition, the compiler stores the key
 *     <CODE>&quot;HISC:EventParameter&quot;</CODE> ({@link #PARAMETER_KEY})
 *     when compiling a parameter event. This makes it possible to distinguish
 *     whether a request or answer event is linked to the level above or
 *     below the current module in a multi-level hierarchy.</LI>
 * </UL>
 *
 * <P>This class also implements the {@link AttributeFactory} interface for
 * use with GUI classes.</P>
 *
 * @author Robi Malik
 */

public class HISCAttributeFactory implements AttributeFactory
{

  //#########################################################################
  //# Singleton Pattern
  public static HISCAttributeFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private HISCAttributeFactory()
  {
  }

  private static class SingletonHolder {
    private static final HISCAttributeFactory INSTANCE =
      new HISCAttributeFactory();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.AttributeFactory
  /**
   * Returns a list of HISC attribute names that can be used for an item
   * of the given type. This can be used by the GUI to provide suggestions
   * to the user when editing attribute lists.
   * @param  clazz  A proxy class or interface for which attributes are sought.
   * @return The attribute names for objects of this type, in any order.
   *         If no attributes are applicable, an empty collection is returned.
   */
  public Collection<String> getApplicableKeys
    (final Class<? extends Proxy> clazz)
  {
    if (clazz.isAssignableFrom(EventDeclProxy.class) ||
        clazz.isAssignableFrom(EventProxy.class)) {
      return ATTRIBUTES_FOR_EVENT;
    } else if (clazz.isAssignableFrom(SimpleComponentProxy.class) ||
               clazz.isAssignableFrom(AutomatonProxy.class)) {
      return ATTRIBUTES_FOR_AUTOMATON;
    } else {
      return Collections.emptyList();
    }
  }

  /**
   * Returns a list of HISC attribute values that can be used for an attribute
   * with the given name. This can be used by the GUI to provide suggestions
   * to the user when editing attribute lists.
   * @param  attrib  The name of the attribute to be given a value.
   * @return List of attribute value strings in the order suggested to the
   *         user. If the attribute takes no value, an empty list is returned.
   */
  public List<String> getApplicableValues(final String attrib)
  {
    final List<String> values = ATTRIBUTE_VALUES.get(attrib);
    if (values == null) {
      return Collections.emptyList();
    } else {
      return values;
    }
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

  /**
   * Checks whether the given attribute map designates an event as
   * a parameter.
   */
  public static boolean isParameter(final Map<String,String> attribs)
  {
    return attribs.containsKey(PARAMETER_KEY);
  }

  /**
   * Changes an attribute map to designate an event as a parameter.
   * @param attribs  The attribute map to be modified.
   * @param iface    <CODE>true</CODE> if the attribute map should designate
   *                 an event as a parameter, <CODE>false</CODE>
   *                 if the event should not be a parameter after the
   *                 call to this method.
   */
  public static void setParameter(final Map<String,String> attribs,
                                  final boolean iface)
  {
    if (iface) {
      attribs.put(PARAMETER_KEY, "");
    } else {
      attribs.remove(PARAMETER_KEY);
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
   * The attribute key used to identify an event as a parameter.
   */
  public static final String PARAMETER_KEY = "HISC:EventParameter";
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


  //#########################################################################
  //# Attribute List Constants
  private static final Collection<String> ATTRIBUTES_FOR_AUTOMATON =
    Collections.singletonList(INTERFACE_KEY);
  private static final Collection<String> ATTRIBUTES_FOR_EVENT =
    Collections.singletonList(EVENTTYPE_KEY);

  private static final Map<String,List<String>> ATTRIBUTE_VALUES =
    new HashMap<String,List<String>>(2);
  static {
    final List<String> types = new ArrayList<String>(3);
    types.add(EventType.ANSWER.toString());
    types.add(EventType.LOWDATA.toString());
    types.add(EventType.REQUEST.toString());
    ATTRIBUTE_VALUES.put(EVENTTYPE_KEY, types);
    final List<String> empty = Collections.emptyList();
    ATTRIBUTE_VALUES.put(INTERFACE_KEY, empty);
  }
}
