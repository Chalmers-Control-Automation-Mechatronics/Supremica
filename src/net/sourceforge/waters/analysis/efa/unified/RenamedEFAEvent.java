//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   UnifiedEFAEvent
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * @author Robi Malik, Sahar Mohajerani
 */

public class RenamedEFAEvent extends AbstractEFAEvent
{
  //#########################################################################
  //# Constructors
  public RenamedEFAEvent(final AbstractEFAEvent original,
                         final ConstraintList update)
  {
    this(original, update, 0);
  }

  public RenamedEFAEvent(final AbstractEFAEvent original,
                         final ConstraintList update,
                         final int index)
  {
    super(update);
    mOriginalEvent = original;
    mIndex = index;
  }


  //#########################################################################
  //# Simple Access
  public AbstractEFAEvent getOriginalEvent()
  {
    return mOriginalEvent;
  }

  public int getIndex()
  {
    return mIndex;
  }

  public void setIndex(final int index)
  {
    mIndex = index;
  }

  @Override
  public String getName()
  {
    if (mIndex < 0) {
      return mOriginalEvent.getName();
    } else {
      return mOriginalEvent.getName() + ":" + mIndex;
    }
  }

  @Override
  public EventKind getKind()
  {
    return mOriginalEvent.getKind();
  }

  @Override
  public boolean isObservable()
  {
    return mOriginalEvent.isObservable();
  }


  //#########################################################################
  //# Data Members
  private final AbstractEFAEvent mOriginalEvent;
  private int mIndex;

}
