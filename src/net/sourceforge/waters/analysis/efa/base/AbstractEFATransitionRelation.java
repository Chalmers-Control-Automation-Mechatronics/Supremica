//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   AbstractEFATransitionRelation
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.base;

import java.util.AbstractSequentialList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import net.sourceforge.waters.analysis.tr.EventEncoding;
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

  public AbstractEFATransitionLabelEncoding<L> getTransitionLabelEncoding()
  {
    return mTransitionLabelEncoding;
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
  public List<L> getUsedTransitionLabels()
  {
    return new UsedLabelList();
  }

  public boolean isUsedLabel(final int e)
  {
    final byte status = mTransitionRelation.getProperEventStatus(e);
    return EventEncoding.isUsedEvent(status);
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
  private class UsedLabelList extends AbstractSequentialList<L>
  {
    //#######################################################################
    //# Interface java.util.List
    @Override
    public ListIterator<L> listIterator(final int index)
    {
      return new UsedLabelIterator(index);
    }

    @Override
    public int size()
    {
      int count = 0;
      for (int e = EventEncoding.TAU; e < mTransitionLabelEncoding.size(); e++) {
        if (isUsedLabel(e)) {
          count++;
        }
      }
      return count;
    }
  }


  //#########################################################################
  //# Inner Class UsedLabelIterator
  private class UsedLabelIterator implements ListIterator<L>
  {
    //#######################################################################
    //# Constructor
    private UsedLabelIterator(final int startIndex)
    {
      if (startIndex < 0) {
        throw new NoSuchElementException
          ("Attempt to create " + ProxyTools.getShortClassName(this) +
           " with negative start index!");
      }
      mEventIndex = mPreviousEventIndex = mNextEventIndex = mIteratorIndex = -1;
      int index = -1;
      while (index < startIndex && hasNext()) {
        mEventIndex = mNextEventIndex;
        index++;
      }
      if (index == startIndex) {
        mEventIndex--;
        mPreviousEventIndex = mNextEventIndex = mEventIndex;
        mIteratorIndex = startIndex - 1;
      } else {
        mPreviousEventIndex = mNextEventIndex = mEventIndex;
        mIteratorIndex = index;
      }
    }

    //#######################################################################
    //# Interface java.util.Iterator
    @Override
    public boolean hasNext()
    {
      findNextIndex();
      return mNextEventIndex < mTransitionLabelEncoding.size();
    }

    @Override
    public L next()
    {
      if (hasNext()) {
        mEventIndex = mPreviousEventIndex = mNextEventIndex;
        mIteratorIndex++;
        return mTransitionLabelEncoding.getTransitionLabel(mEventIndex);
      } else {
        throw new NoSuchElementException
          ("Reached end of list in " + ProxyTools.getShortClassName(this) + "!");
      }
    }

    @Override
    public boolean hasPrevious()
    {
      findPreviousIndex();
      return mPreviousEventIndex >= 0;
    }

    @Override
    public L previous()
    {
      if (hasNext()) {
        mEventIndex = mNextEventIndex = mPreviousEventIndex;
        mIteratorIndex--;
        return mTransitionLabelEncoding.getTransitionLabel(mEventIndex);
      } else {
        throw new NoSuchElementException
          ("Reached start of list in " + ProxyTools.getShortClassName(this) + "!");
      }
    }

    @Override
    public int nextIndex()
    {
      return mIteratorIndex + 1;
    }

    @Override
    public int previousIndex()
    {
      return mIteratorIndex - 1;
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) + " does not support modifications!");
    }

    @Override
    public void set(final L e)
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) + " does not support modifications!");
    }

    @Override
    public void add(final L e)
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) + " does not support modifications!");
    }

    //#######################################################################
    //# Auxiliary Methods
    private void findNextIndex()
    {
      if (mNextEventIndex == mEventIndex) {
        final int numEvents = mTransitionLabelEncoding.size();
        for (mNextEventIndex++; mNextEventIndex < numEvents; mNextEventIndex++) {
          if (isUsedLabel(mNextEventIndex)) {
            break;
          }
        }
      }
    }

    private void findPreviousIndex()
    {
      if (mPreviousEventIndex == mEventIndex) {
        for (mPreviousEventIndex--; mPreviousEventIndex >=0; mPreviousEventIndex--) {
          if (isUsedLabel(mPreviousEventIndex)) {
            break;
          }
        }
      }
    }

    //#######################################################################
    //# Data Members
    private int mEventIndex;
    private int mNextEventIndex;
    private int mPreviousEventIndex;
    private int mIteratorIndex;
  }


  //#########################################################################
  //# Data Members
  private final ListBufferTransitionRelation mTransitionRelation;
  private final AbstractEFATransitionLabelEncoding<L> mTransitionLabelEncoding;
  private final List<SimpleNodeProxy> mNodeList;
}
