//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   IndexedListModel
//###########################################################################
//# $Id: IndexedListModel.java,v 1.1 2006-08-08 23:59:21 robi Exp $
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
    mSortedMirror = new ArrayList<NamedSubject>(subject);
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

  public void finalize()
  {
    dispose();
  }


  //#########################################################################
  //# Interface javax.swing.ListModel
  public NamedSubject getElementAt(final int index)
  {
    return mSortedMirror.get(index);
  }

  public int getSize()
  {
    return mSortedMirror.size();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.ModelObserver
  public void modelChanged(final ModelChangeEvent event)
  {
    final Subject source = event.getSource();
    switch (event.getKind()) {
    case ModelChangeEvent.ITEM_ADDED:
      if (source == mSubject) {
        final NamedSubject value = (NamedSubject) event.getValue();
        final String name = value.getName();
        final int index = Collections.binarySearch(mSortedMirror, value);
        if (index >= 0) {
          throw new IllegalStateException
            ("Inserted item '" + name + "' already contained in mirror!");
        }
        final int inspoint = -index - 1;
        mSortedMirror.add(inspoint, value);
        fireIntervalAdded(this, inspoint, inspoint);
      }
      break;
    case ModelChangeEvent.ITEM_REMOVED:
      if (source == mSubject) {
        final NamedSubject value = (NamedSubject) event.getValue();
        final String name = value.getName();
        final int index = Collections.binarySearch(mSortedMirror, value);
        if (index < 0) {
          throw new IllegalStateException
            ("Removed item '" + name + "' not found in mirror!");
        }
        mSortedMirror.remove(index);
        fireIntervalAdded(this, index, index);
      }
      break;
    case ModelChangeEvent.NAME_CHANGED:
      if (source.getParent() == mSubject) {
        Collections.sort(mSortedMirror);
        final int last = mSortedMirror.size() - 1;
        fireContentsChanged(this, 0, last);
      }
      break;
    case ModelChangeEvent.STATE_CHANGED:
      {
        final NamedSubject value = getChangeRoot(source);
        final String name = value.getName();
        final int index = Collections.binarySearch(mSortedMirror, value);
        if (index < 0) {
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
  private List<NamedSubject> mSortedMirror;

}