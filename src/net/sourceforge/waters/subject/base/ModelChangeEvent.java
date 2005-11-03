//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   ModelChangeEvent
//###########################################################################
//# $Id: ModelChangeEvent.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.base;

import java.util.EventObject;


/**
 * @author Robi Malik
 */

public class ModelChangeEvent extends EventObject
{

  //#########################################################################
  //# Static Creator Methods
  public static ModelChangeEvent createItemAdded(final Subject container,
                                                 final Object element)
  {
    return new ModelChangeEvent(container, ITEM_ADDED, element);
  }

  public static ModelChangeEvent createItemRemoved(final Subject container,
                                                   final Object element)
  {
    return new ModelChangeEvent(container, ITEM_REMOVED, element);
  }

  public static ModelChangeEvent createNameChanged(final Subject item,
                                                   final String oldname)
  {
    return new ModelChangeEvent(item, NAME_CHANGED, oldname);
  }

  public static ModelChangeEvent createStateChanged(final Subject item)
  {
    return new ModelChangeEvent(item, STATE_CHANGED);
  }

  public static ModelChangeEvent createGeometryChanged(final Subject item)
  {
    return new ModelChangeEvent(item, GEOMETRY_CHANGED);
  }


  //#########################################################################
  //# Constructors
  public ModelChangeEvent(final Subject source, final int kind)
  {
    super(source);
    mKind = kind;
    mValue = null;
  }

  public ModelChangeEvent(final Subject source,
                          final int kind,
                          final Object value)
  {
    super(source);
    mKind = kind;
    mValue = value;
  }


  //#########################################################################
  //# Getters
  public Subject getSource()
  {
    return (Subject) super.getSource();
  }

  public int getKind()
  {
    return mKind;
  }

  public Object getValue()
  {
    return mValue;
  }


  //#########################################################################
  //# Class Constants
  public static final int ITEM_ADDED = 1;
  public static final int ITEM_REMOVED = 2;
  public static final int NAME_CHANGED = 3;
  public static final int STATE_CHANGED = 4;
  public static final int GEOMETRY_CHANGED = 5;


  //#########################################################################
  //# Data Members
  private final int mKind;
  private final Object mValue;

}
