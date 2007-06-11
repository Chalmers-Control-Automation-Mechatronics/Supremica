//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   IndexedListModel
//###########################################################################
//# $Id: IndexedListModel.java,v 1.5 2007-06-11 15:07:51 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.AbstractListModel;
import javax.swing.ListSelectionModel;

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
  //# Accessing the Selection
  public Iterable<E> getSelectedSubjects(final ListSelectionModel selection)
  {
    return new SelectionIterable(selection);
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
        final E value = uncheckedCast(event.getValue());
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

  @SuppressWarnings("unchecked")
  private E uncheckedCast(final Object value)
  {
    return (E) value;
  }


  //#########################################################################
  //# Inner Class SelectionIterable
  private class SelectionIterable implements Iterable<E>
  {
    //#######################################################################
    //# Constructor
    SelectionIterable(final ListSelectionModel selection)
    {
      mSelection = selection;
    }

    //#######################################################################
    //# Interface java.util.Iterable
    public Iterator<E> iterator()
    {
      return new SelectionIterator(mSelection);
    }

    //#######################################################################
    //# Data Members
    private final ListSelectionModel mSelection;
  }


  //#########################################################################
  //# Inner Class SelectionIterator
  private class SelectionIterator implements Iterator<E>
  {
    //#######################################################################
    //# Constructor
    SelectionIterator(final ListSelectionModel selection)
    {
      mSelection = selection;
      mIndex = selection.getMinSelectionIndex();
    }

    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return mIndex >= 0;
    }

    public E next()
    {
      if (mIndex >= 0) {
        final E result = getElementAt(mIndex);
        final int stop = mSelection.getMaxSelectionIndex();
        while (++mIndex <= stop) {
          if (mSelection.isSelectedIndex(mIndex)) {
            return result;
          }
        }
        mIndex = -1;
        return result;
      } else {
        throw new NoSuchElementException
          ("No more elements in list selection iteration!");
      }
    }

    public void remove()
    {
      throw new UnsupportedOperationException
        ("List selection iteration does not support removal of items!");
    }

    //#######################################################################
    //# Data Members
    private final ListSelectionModel mSelection;
    private int mIndex;
  }


  //#########################################################################
  //# Data Members
  private IndexedListSubject<E> mSubject;
  private List<E> mSortedMirror;

}