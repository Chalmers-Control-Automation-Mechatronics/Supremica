//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   AbstractEFATransitionRelation
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.base;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

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
  public Set<L> getUsedTransitionLabels()
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
  private class UsedLabelList extends AbstractSet<L>
  {
    //#######################################################################
    //# Interface java.util.List
    @Override
    public Iterator<L> iterator()
    {
      return new UsedLabelIterator();
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
  private class UsedLabelIterator implements Iterator<L>
  {
    //#######################################################################
    //# Constructor
    private UsedLabelIterator()
    {
      mIndex = mNextIndex = -1;
    }

    //#######################################################################
    //# Interface java.util.Iterator
    @Override
    public boolean hasNext()
    {
      final int numEvents = mTransitionLabelEncoding.size();
      if (mNextIndex == mIndex) {
        for (mNextIndex = mIndex + 1; mNextIndex < numEvents; mNextIndex++) {
          if (isUsedLabel(mNextIndex)) {
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
