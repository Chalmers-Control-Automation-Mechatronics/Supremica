//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   AbstractEFAEventEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;


/**
 * @author Robi Malik
 */

public class AbstractEFAEventEncoding<L>
{

  //#########################################################################
  //# Constructors
  public AbstractEFAEventEncoding()
  {
    this(DEFAULT_SIZE);
  }

  public AbstractEFAEventEncoding(final int size)
  {
    mEventMap = new TObjectIntHashMap<L>(size, 0.5f, -1);
    mUpdateList = new ArrayList<L>(size);
  }

  public AbstractEFAEventEncoding(final AbstractEFAEventEncoding<L> encoding)
  {
    this(encoding.size());
    for (int e = EventEncoding.TAU; e < encoding.size(); e++) {
      final L update = encoding.getUpdate(e);
      createEventId(update);
    }
  }


  //#########################################################################
  //# Simple Access
  /**
   * Returns the number of updates in this event encoding.
   * Note that this method always returns at least&nbsp;1 because the
   * true update is contained in every encoding.
   */
  public int size()
  {
    return mEventMap.size();
  }

  public int getEventId(final ConstraintList update)
  {
    return mEventMap.get(update);
  }

  public L getUpdate(final int event)
  {
    return mUpdateList.get(event);
  }

  public int createEventId(final L update)
  {
    final int id = mEventMap.get(update);
    if (id >= 0) {
      return id;
    } else {
      final int event = mEventMap.size();
      mEventMap.put(update, event);
      mUpdateList.add(update);
      return event;
    }
  }

  /**
   * Adds all updates found in the given event encoding to this event
   * encoding.
   */
  public void merge(final AbstractEFAEventEncoding<L> enc)
  {
    for (final L update : enc.mUpdateList) {
      createEventId(update);
    }
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    final StringBuffer buffer = new StringBuffer();
    int e = 0;
    for (final L update : mUpdateList) {
      buffer.append(e++);
      buffer.append(" : ");
      buffer.append(update);
      buffer.append("\n");
    }
    return buffer.toString();
  }


  //#########################################################################
  //# Data Members
  private final TObjectIntHashMap<L> mEventMap;
  private final List<L> mUpdateList;


  //#########################################################################
  //# Class Constants
  protected static final int DEFAULT_SIZE = 16;
}

