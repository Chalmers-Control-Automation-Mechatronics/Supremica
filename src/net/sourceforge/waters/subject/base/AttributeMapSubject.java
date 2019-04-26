//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.subject.base;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.sourceforge.waters.model.base.ProxyTools;


/**
 * <P>A subject implementation of an attribute map.</P>
 *
 * <P>An attribute map is a map that maps attribute names to
 * attribute value, both of which are strings. It is used by some
 * objects in Waters modules to store additional information from
 * external provided that is not needed or used by standard DES tools.</P>
 *
 * <P>The subject implementation provides a mutable map from strings to
 * strings with full event notification support. Addition of an attribute
 * leads to an {@link ModelChangeEvent#ITEM_ADDED} notification, removal
 * of an attribute leads to an {@link ModelChangeEvent#ITEM_REMOVED}
 * notification, and changing an attribute leads to a
 * {@link ModelChangeEvent#STATE_CHANGED} notification, each with the
 * name of the attribute provided as additional argument.</P>
 *
 * @author Robi Malik
 */

public final class AttributeMapSubject
    extends AbstractMap<String,String>
    implements Cloneable, Subject
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty attribute map.
   */
  public AttributeMapSubject()
  {
    mMap = new AttributeTreeMap();
    mAttributeEntrySet = new AttributeEntrySet();
    mParent = null;
    mObservers = null;
  }

  /**
   * Creates an attribute map initialised with the contents of a given map.
   */
  public AttributeMapSubject(final Map<String,String> map)
  {
    mMap = new AttributeTreeMap(map);
    mAttributeEntrySet = new AttributeEntrySet();
    mParent = null;
    mObservers = null;
  }

  //#########################################################################
  //# Cloning and Assigning
  @Override
  public AttributeMapSubject clone()
  {
    return new AttributeMapSubject(this);
  }

  public UndoInfo createUndoInfo(final Map<String,String> newMap,
                                 final Set<? extends Subject> boundary)
  {
    if (boundary != null && boundary.contains(this)) {
      return null;
    }
    final RecursiveUndoInfo info = new RecursiveUndoInfo(this);
    for (final Map.Entry<String,String> oldEntry : entrySet()) {
      final String oldKey = oldEntry.getKey();
      final String newValue = newMap.get(oldKey);
      if (newValue == null) {
        final UndoInfo remove = new ReplacementUndoInfo(oldEntry, null);
        info.add(remove);
      } else if (!oldEntry.getValue().equals(newValue)) {
        final Map.Entry<String,String> newEntry =
          new AbstractMap.SimpleEntry<String,String>(oldKey, newValue);
        final UndoInfo replace = new ReplacementUndoInfo(oldEntry, newEntry);
        info.add(replace);
      }
    }
    for (final Map.Entry<String,String> newEntry : newMap.entrySet()) {
      final String newKey = newEntry.getKey();
      if (!containsKey(newKey)) {
        final UndoInfo add = new ReplacementUndoInfo(null, newEntry);
        info.add(add);
      }
    }
    if (info.isEmpty()) {
      return null;
    } else {
      return info;
    }
  }

  public ModelChangeEvent assignMember(final int index,
                                       final Object oldValue,
                                       final Object newValue)
  {
    if (newValue == null) {
      @SuppressWarnings("unchecked")
      final Map.Entry<String,String> entry =
        (Map.Entry<String,String>) oldValue;
      final String attrib = entry.getKey();
      mMap.remove(attrib);
      return ModelChangeEvent.createItemRemoved(this, attrib);
    } else if (oldValue == null) {
      @SuppressWarnings("unchecked")
      final Map.Entry<String,String> entry =
        (Map.Entry<String,String>) newValue;
      final String attrib = entry.getKey();
      final String value = entry.getValue();
      mMap.putRaw(attrib, value);
      return ModelChangeEvent.createItemAdded(this, attrib);
    } else {
      @SuppressWarnings("unchecked")
      final Map.Entry<String,String> entry =
        (Map.Entry<String,String>) newValue;
      final String attrib = entry.getKey();
      final String value = entry.getValue();
      mMap.putRaw(attrib, value);
      return ModelChangeEvent.createStateChanged(this);
    }
  }


  //#########################################################################
  //# Interface java.util.Map
  public boolean containsKey(final Object key)
  {
    return mMap.containsKey(key);
  }

  public Set<Map.Entry<String,String>> entrySet()
  {
    return mAttributeEntrySet;
  }

  public String get(final String key)
  {
    return mMap.get(key);
  }

  public String put(final String attrib, final String value)
  {
    return mMap.put(attrib, value);
  }

  public String remove(final String key)
  {
    final String old = mMap.remove(key);
    if (old != null) {
      final ModelChangeEvent event =
          ModelChangeEvent.createItemRemoved(this, key);
      event.fire();
    }
    return old;
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.Subject
  public Subject getParent()
  {
    return mParent;
  }

  public DocumentSubject getDocument()
  {
    if (mParent != null) {
      return mParent.getDocument();
    } else {
      return null;
    }
  }

  public void setParent(final Subject parent)
  {
    checkSetParent(parent);
    mParent = parent;
  }

  public void checkSetParent(final Subject parent)
  {
    if (parent != null && mParent != null) {
      final StringBuilder buffer = new StringBuilder();
      buffer.append("Trying to redefine parent of ");
      final String clsname = ProxyTools.getShortClassName(this);
      buffer.append(clsname);
      buffer.append('!');
      throw new IllegalStateException(buffer.toString());
    }
  }

  public void addModelObserver(final ModelObserver observer)
  {
    if (mObservers == null) {
      mObservers = new LinkedList<ModelObserver>();
    }
    mObservers.add(observer);
  }

  public void removeModelObserver(final ModelObserver observer)
  {
    if (mObservers != null && mObservers.remove(observer)
        && mObservers.isEmpty()) {
      mObservers = null;
    }
  }

  public Collection<ModelObserver> getModelObservers()
  {
    return mObservers;
  }


  //#########################################################################
  //# Inner Class AttributeTreeMap
  private class AttributeTreeMap
    extends TreeMap<String,String>
  {

    //#########################################################################
    //# Constructors
    private AttributeTreeMap()
    {
    }

    private AttributeTreeMap(final Map<String,String> map)
    {
      for (final Map.Entry<String,String> entry : map.entrySet()) {
        final String key = entry.getKey();
        final String value = entry.getValue();
        super.put(key, value);
      }
    }

    //#########################################################################
    //# Interface java.util.Map
    public String put(final String attrib, final String value)
    {
      final String old = get(attrib);
      final int eventkind;
      if (old == null) {
        eventkind = ModelChangeEvent.ITEM_ADDED;
      } else if (old.equals(value)) {
        return old;
      } else {
        eventkind = ModelChangeEvent.STATE_CHANGED;
      }
      super.put(attrib, value);
      final ModelChangeEvent event =
          new ModelChangeEvent(AttributeMapSubject.this, eventkind, attrib);
      event.fire();
      return old;
    }

    //#########################################################################
    //# Direct Access
    private void putRaw(final String attrib, final String value)
    {
      super.put(attrib, value);
    }

    //#########################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

  }


  //#########################################################################
  //# Inner Class AttributeEntrySet
  private class AttributeEntrySet
    extends AbstractSet<Map.Entry<String,String>>
  {

    //#######################################################################
    //# Interface java.util.Set
    public Iterator<Map.Entry<String,String>> iterator()
    {
      return new AttributeEntryIterator();
    }

    public int size()
    {
      return mMap.size();
    }

  }


  //#########################################################################
  //# Inner Class AttributeEntryIterator
  private class AttributeEntryIterator
    implements Iterator<Map.Entry<String,String>>
  {

    //#######################################################################
    //# Constructor
    private AttributeEntryIterator()
    {
      mIterator = mMap.entrySet().iterator();
    }

    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return mIterator.hasNext();
    }

    public Map.Entry<String,String> next()
    {
      mVictim = mIterator.next();
      return mVictim;
    }

    public void remove()
    {
      mIterator.remove();
      final String attrib = mVictim.getKey();
      mVictim = null;
      final ModelChangeEvent event =
          ModelChangeEvent.createItemRemoved(AttributeMapSubject.this, attrib);
      event.fire();
    }

    //#######################################################################
    //# Data Members
    private final Iterator<Map.Entry<String,String>> mIterator;
    private Map.Entry<String,String> mVictim;

  }


  //#########################################################################
  //# Interface java.util.Map
  private final AttributeTreeMap mMap;
  private final Set<Entry<String,String>> mAttributeEntrySet;
  private Subject mParent;
  private List<ModelObserver> mObservers;

}
