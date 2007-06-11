//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   IndexedListModel
//###########################################################################
//# $Id: IndexedListModel.java,v 1.4 2007-06-11 05:59:18 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;

import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.base.IndexedListSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.NamedSubject;


public class IndexedListModel<E extends NamedSubject>
  extends AbstractListModel
  implements ModelObserver
{

  //#########################################################################
  //# Constructor
  public IndexedListModel(final IndexedListSubject<E> subject)
  {
    mSubject = subject;
    mSortedMirror = new ArrayList<E>(subject);
    Collections.sort(mSortedMirror);
    subject.addModelObserver(this);
  }


  //#########################################################################
  //# Clean up
  public void dispose()
  {
    if (mSubject != null) {
      mSubject.removeModelObserver(this);
      mSubject = null;
      mSortedMirror = null;
    }
  }

  protected void finalize()
  {
    dispose();
  }


  //#########################################################################
  //# Interface javax.swing.ListModel
  public E getElementAt(final int index)
  {
    return mSortedMirror.get(index);
  }

  public int getSize()
  {
    return mSortedMirror.size();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.ModelObserver
  @SuppressWarnings("unchecked")
  public void modelChanged(final ModelChangeEvent event)
  {
    Subject source = event.getSource();
    int kind = event.getKind();
    if (source == mSubject) {
      switch (kind) {
      case ModelChangeEvent.ITEM_ADDED:
      case ModelChangeEvent.ITEM_REMOVED:
        break;
      default:
        throw new IllegalStateException
          ("Unexpected notification " + kind + " for list subject!");
      }
    } else if (source.getParent() == mSubject) {
      switch (kind) {
      case ModelChangeEvent.NAME_CHANGED:
      case ModelChangeEvent.STATE_CHANGED:
        break;
      case ModelChangeEvent.GEOMETRY_CHANGED:
        return;
      default:
        throw new IllegalStateException
          ("Unexpected notification " + kind + " for list member!");
      }
    } else {
      if (kind == ModelChangeEvent.GEOMETRY_CHANGED) {
        return;
      }
      source = getChangeRoot(source);
      kind = ModelChangeEvent.STATE_CHANGED;
    }
    switch (kind) {
    case ModelChangeEvent.ITEM_ADDED:
      {
        final E value = (E) event.getValue();
        final int index = Collections.binarySearch(mSortedMirror, value);
        if (index >= 0) {
          final String name = value.getName();
          throw new IllegalStateException
            ("Inserted item '" + name + "' already contained in mirror!");
        }
        final int inspoint = -index - 1;
        mSortedMirror.add(inspoint, value);
        fireIntervalAdded(this, inspoint, inspoint);
      }
      break;
    case ModelChangeEvent.ITEM_REMOVED:
      {
        final NamedSubject value = (NamedSubject) event.getValue();
        final int index = Collections.binarySearch(mSortedMirror, value);
        if (index < 0) {
          final String name = value.getName();
          throw new IllegalStateException
            ("Removed item '" + name + "' not found in mirror!");
        }
        mSortedMirror.remove(index);
        fireIntervalAdded(this, index, index);
      }
      break;
    case ModelChangeEvent.NAME_CHANGED:
      {
        Collections.sort(mSortedMirror);
        final int last = mSortedMirror.size() - 1;
        fireContentsChanged(this, 0, last);
      }
      break;
    case ModelChangeEvent.STATE_CHANGED:
      {
        final NamedSubject named = (NamedSubject) source;
        final int index = Collections.binarySearch(mSortedMirror, named);
        if (index < 0) {
          final String name = named.getName();
          throw new IllegalStateException
            ("Modified item '" + name + "' not found in mirror!");
        }
        fireContentsChanged(this, index, index);
      }
      break;
    default:
      break;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private NamedSubject getChangeRoot(final Subject value)
  {
    final Subject parent = value.getParent();
    if (parent == mSubject) {
      return (NamedSubject) value;
    } else {
      return getChangeRoot(parent);
    }
  }


  //#########################################################################
  //# Data Members
  private IndexedListSubject<E> mSubject;
  private List<E> mSortedMirror;

}