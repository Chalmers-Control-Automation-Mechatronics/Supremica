//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   EFSMEventEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;


/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class EFSMEventEncoding
{

  //#########################################################################
  //# Constructors
  public EFSMEventEncoding()
  {
    this(DEFAULT_SIZE);
  }

  public EFSMEventEncoding(final int size)
  {
    mEventMap = new TObjectIntHashMap<ConstraintList>(size);
    mUpdateList = new ArrayList<ConstraintList>(size);
    // empty constraint list represents true
    final ConstraintList empty = new ConstraintList();
    mEventMap.put(empty, EventEncoding.TAU);
    mUpdateList.add(empty);
  }


  //#########################################################################
  //# Simple Access
  public int size ()
  {
    return mEventMap.size();
  }

  public int getEventId(final ConstraintList update)
  {
    return mEventMap.get(update);
  }

  public ConstraintList getUpdate(final int event)
  {
    return mUpdateList.get(event);
  }

  public int createEventId(final ConstraintList update)
  {
    if (mEventMap.contains(update)) {
      return mEventMap.get(update);
    } else {
      final int event = mEventMap.size();
      mEventMap.put(update, event);
      mUpdateList.add(update);
      return event;
    }
  }


  //#########################################################################
  //# Data Members
  private final TObjectIntHashMap<ConstraintList> mEventMap;
  private final ArrayList<ConstraintList> mUpdateList;

  private static final int DEFAULT_SIZE = 16;
}

