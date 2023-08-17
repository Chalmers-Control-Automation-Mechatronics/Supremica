//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.AbstractListModel;
import javax.swing.ListSelectionModel;

import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.module.IdentifiedSubject;


public class IndexedListModel<E extends IdentifiedSubject>
  extends AbstractListModel<E>
  implements ModelObserver
{

  //#########################################################################
  //# Constructor
  public IndexedListModel(final ListSubject<E> subject)
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
    }
  }

  @Override
  protected void finalize()
  {
    dispose();
  }


  //#########################################################################
  //# Additional Access
  public boolean contains(final Object item)
  {
    return indexOf(item) >= 0;
  }

  public int indexOf(final Object item)
  {
    if (item instanceof IdentifiedSubject) {
      final IdentifiedSubject named = (IdentifiedSubject) item;
      final int index = Collections.binarySearch(mSortedMirror, named);
      if (index >= 0) {
        final E member = mSortedMirror.get(index);
        return item.equals(member) ? index : -1;
      } else {
        return -1;
      }
    } else {
      return -1;
    }
  }

  public Iterable<E> getSelectedSubjects(final ListSelectionModel selection)
  {
    return new SelectionIterable(selection);
  }


  //#########################################################################
  //# Interface javax.swing.ListModel
  @Override
  public E getElementAt(final int index)
  {
    return mSortedMirror.get(index);
  }

  @Override
  public int getSize()
  {
    return mSortedMirror.size();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.ModelObserver
  @Override
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
      case ModelChangeEvent.GEOMETRY_CHANGED:
      case ModelChangeEvent.GENERAL_NOTIFICATION:
        break;
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
        final IdentifiedSubject value = (IdentifiedSubject) event.getValue();
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
    case ModelChangeEvent.STATE_CHANGED:
      {
        // Identifier and IdentifiedProxy objects fire STATE_CHANGED,
        // not NAME_CHANGED ...
        Collections.sort(mSortedMirror);
        final int last = mSortedMirror.size() - 1;
        fireContentsChanged(this, 0, last);
      }
      break;
    case ModelChangeEvent.GEOMETRY_CHANGED:
    case ModelChangeEvent.GENERAL_NOTIFICATION:
      {
        final IdentifiedSubject isource = (IdentifiedSubject) source;
        final int index = Collections.binarySearch(mSortedMirror, isource);
        if (index < 0) {
          final String name = isource.getName();
          throw new IllegalStateException
            ("Changed item '" + name + "' not found in mirror!");
        }
        fireContentsChanged(this, index, index);
      }
      break;
    default:
      break;
    }
  }

  @Override
  public int getModelObserverPriority()
  {
    return ModelObserver.RENDERING_PRIORITY;
  }


  //#########################################################################
  //# Auxiliary Methods
  private IdentifiedSubject getChangeRoot(final Subject value)
  {
    final Subject parent = value.getParent();
    if (parent == mSubject) {
      return (IdentifiedSubject) value;
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
    @Override
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
      mIndex =
        selection.isSelectionEmpty() ? -1 : selection.getMinSelectionIndex();
    }

    //#######################################################################
    //# Interface java.util.Iterator
    @Override
    public boolean hasNext()
    {
      return mIndex >= 0;
    }

    @Override
    public E next()
    {
      if (mIndex >= 0) {
        final E result = getElementAt(mIndex);
        final int stop = mSelection.getMaxSelectionIndex();
        while (++mIndex <= stop) {
          if (mSelection.isSelectedIndex(mIndex)) {
            break;
          }
        }
        if (mIndex > stop) {
          mIndex = -1;
        }
        return result;
      } else {
        throw new NoSuchElementException
          ("No more elements in list selection iteration!");
      }
    }

    @Override
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
  private ListSubject<E> mSubject;
  private final List<E> mSortedMirror;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
