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

package net.sourceforge.waters.analysis.efa.base;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

/**
 * @author Robi Malik
 */
public abstract class AbstractEFATransitionRelation<L>
 implements Comparable<AbstractEFATransitionRelation<?>>
{

  //#########################################################################
  //# Constructors
  protected AbstractEFATransitionRelation(final ListBufferTransitionRelation rel,
                                          final AbstractEFATransitionLabelEncoding<L> labels,
                                          final List<SimpleNodeProxy> nodes)
  {
    mTransitionRelation = rel;
    mTransitionLabelEncoding = labels;
    mNodeList = nodes;
  }

  protected AbstractEFATransitionRelation(final ListBufferTransitionRelation rel,
                                          final AbstractEFATransitionLabelEncoding<L> labels)
  {
    this(rel, labels, null);
  }


  //#########################################################################
  //# Simple Access
  public String getName()
  {
    return mTransitionRelation.getName();
  }

  public void setName(final String name)
  {
    mTransitionRelation.setName(name);
  }

  public ListBufferTransitionRelation getTransitionRelation()
  {
    return mTransitionRelation;
  }

  public List<SimpleNodeProxy> getNodeList()
  {
    return mNodeList;
  }


  //#########################################################################
  //# Accessing Events
  protected AbstractEFATransitionLabelEncoding<L> getTransitionLabelEncoding()
  {
    return mTransitionLabelEncoding;
  }

  protected Set<L> getUsedTransitionLabels(final int start)
  {
    return new UsedLabelList(start);
  }

  /**
   * Returns whether the given transition label is marked as used in the
   * transition relation.
   * @param  code   Code of transition label to be checked.
   */
  protected boolean isUsedTransitionLabel(final int code)
  {
    final byte status = mTransitionRelation.getProperEventStatus(code);
    return EventStatus.isUsedEvent(status);
  }

  /**
   * Returns whether the given transition label is marked as used in the
   * transition relation.
   */
  protected boolean isUsedTransitionLabel(final L label)
  {
    final int code = mTransitionLabelEncoding.getTransitionLabelId(label);
    return code >= 0 ? isUsedTransitionLabel(code) : false;
  }


  //#########################################################################
  //# Interface java.util.Comparable
  @Override
  public int compareTo(final AbstractEFATransitionRelation<?> efaTR)
  {
    final String name1 = getName();
    final String name2 = efaTR.getName();
    return name1.compareTo(name2);
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    return getName() + "\n" + mTransitionRelation.toString();
  }


  //#########################################################################
  //# Inner Class UsedLabelList
  private class UsedLabelList extends AbstractSet<L>
  {
    //#######################################################################
    //# Constructor
    private UsedLabelList(final int start)
    {
      mStart = start;
    }

    //#######################################################################
    //# Interface java.util.List
    @Override
    public Iterator<L> iterator()
    {
      return new UsedLabelIterator(mStart);
    }

    @Override
    public int size()
    {
      int count = 0;
      for (int e = mStart+1; e < mTransitionLabelEncoding.size(); e++) {
        if (isUsedTransitionLabel(e)) {
          count++;
        }
      }
      return count;
    }

    //#######################################################################
    //# Data Members
    private final int mStart;
  }


  //#########################################################################
  //# Inner Class UsedLabelIterator
  private class UsedLabelIterator implements Iterator<L>
  {
    //#######################################################################
    //# Constructor
    private UsedLabelIterator(final int start)
    {
      mIndex = mNextIndex = start;
    }

    //#######################################################################
    //# Interface java.util.Iterator
    @Override
    public boolean hasNext()
    {
      final int numEvents = mTransitionLabelEncoding.size();
      if (mNextIndex == mIndex) {
        for (mNextIndex = mIndex + 1; mNextIndex < numEvents; mNextIndex++) {
          if (isUsedTransitionLabel(mNextIndex)) {
            break;
          }
        }
      }
      return mNextIndex < numEvents;
    }

    @Override
    public L next()
    {
      if (hasNext()) {
        mIndex = mNextIndex;
        return mTransitionLabelEncoding.getTransitionLabel(mIndex);
      } else {
        throw new NoSuchElementException
          ("Reached end of list in " + ProxyTools.getShortClassName(this) + "!");
      }
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) + " does not support modifications!");
    }

    //#######################################################################
    //# Data Members
    private int mIndex;
    private int mNextIndex;
  }


  //#########################################################################
  //# Data Members
  private final ListBufferTransitionRelation mTransitionRelation;
  private final AbstractEFATransitionLabelEncoding<L> mTransitionLabelEncoding;
  private final List<SimpleNodeProxy> mNodeList;
}
