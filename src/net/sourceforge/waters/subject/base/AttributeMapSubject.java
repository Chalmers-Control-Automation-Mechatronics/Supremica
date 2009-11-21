//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   AttributeMapSubject
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.subject.base;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


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
  public AttributeMapSubject clone()
  {
    return new AttributeMapSubject(this);
  }

  /**
   * Assigns the contents of another attribute map to this attribute map. This
   * method ensures that the contents of this attribute map are equal to the
   * contents of the given attribute map according to the
   * {@link java.lang.Object#equals(Object) equals()} method. Mappings already
   * contained in this attribute map are reused, the method produces as few
   * model change notifications as possible.
   * @param map
   *          The attribute map to be copied from.
   */
  public void assignFrom(final Map<String,String> map)
  {
    for (final String key : keySet()) {
      if (!map.containsKey(key)) {
        remove(key);
      }
    }
    for (final Map.Entry<String,String> entry : map.entrySet()) {
      final String key = entry.getKey();
      final String value = entry.getValue();
      put(key, value);
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
      fireModelChanged(event);
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
      final StringBuffer buffer = new StringBuffer();
      buffer.append("Trying to redefine parent of ");
      final String clsname = AbstractSubject.getShortClassName(this);
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

  public void fireModelChanged(final ModelChangeEvent event)
  {
    if (mObservers != null) {
      // Just in case they try to register or unregister observers
      // in response to the update ...
      final List<ModelObserver> copy = new ArrayList<ModelObserver>(mObservers);
      for (final ModelObserver observer : copy) {
        observer.modelChanged(event);
      }
    }
    if (mParent != null) {
      mParent.fireModelChanged(event);
    }
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
      fireModelChanged(event);
      return old;
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
      fireModelChanged(event);
    }

    //#######################################################################
    //# Data Members
    private final Iterator<Map.Entry<String,String>> mIterator;
    private Map.Entry<String,String> mVictim;

  }


  //#########################################################################
  //# Interface java.util.Map
  private final Map<String,String> mMap;
  private final Set<Entry<String,String>> mAttributeEntrySet;
  private Subject mParent;
  private List<ModelObserver> mObservers;

}
