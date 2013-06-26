//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efsm
//# CLASS:   EFSMEventEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;


/**
 * @author Robi Malik, Sahar Mohajerani
 */

class EFSMEventEncoding
{

  //#########################################################################
  //# Constructors
  EFSMEventEncoding()
  {
    this(DEFAULT_SIZE);
  }

  EFSMEventEncoding(final int size)
  {
    mEventMap = new TObjectIntHashMap<ConstraintList>(size, 0.5f, -1);
    mUpdateList = new ArrayList<ConstraintList>(size);
    // empty constraint list represents true
    final ConstraintList empty = new ConstraintList();
    mEventMap.put(empty, EventEncoding.TAU);
    mUpdateList.add(empty);
  }

  EFSMEventEncoding(final EFSMEventEncoding encoding)
  {
    this(encoding.size());
    for (int event=EventEncoding.NONTAU; event < encoding.size(); event++) {
      final ConstraintList update = encoding.getUpdate(event);
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
  int size()
  {
    return mEventMap.size();
  }

  int getEventId(final ConstraintList update)
  {
    return mEventMap.get(update);
  }

  ConstraintList getUpdate(final int event)
  {
    return mUpdateList.get(event);
  }

  int createEventId(final ConstraintList update)
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
  void merge(final EFSMEventEncoding enc)
  {
    for (final ConstraintList update : enc.mUpdateList) {
      createEventId(update);
    }
  }

  void setSelfloops(final ListBufferTransitionRelation rel,
                    final EFSMVariableFinder finder)
  {
    for (int e = EventEncoding.NONTAU; e < size(); e++) {
      final ConstraintList update = getUpdate(e);
      if (!finder.findPrime(update)) {
        final byte status = rel.getProperEventStatus(e);
        rel.setProperEventStatus
          (e, status | EventEncoding.STATUS_OUTSIDE_ONLY_SELFLOOP);
      }
    }
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    final StringBuffer buffer = new StringBuffer();
    int e = 0;
    for (final ConstraintList update : mUpdateList) {
      buffer.append(e++);
      buffer.append(" : ");
      buffer.append(update);
      buffer.append("\n");
    }
    return buffer.toString();
  }


  //#########################################################################
  //# Data Members
  private final TObjectIntHashMap<ConstraintList> mEventMap;
  private final List<ConstraintList> mUpdateList;

  private static final int DEFAULT_SIZE = 16;
}

