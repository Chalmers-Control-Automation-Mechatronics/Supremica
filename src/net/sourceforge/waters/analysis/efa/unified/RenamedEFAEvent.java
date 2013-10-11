//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   RenamedEFAEvent
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * An event in a unified EFA system.
 * This class is used for intermediate events that have been generated
 * as a result of renaming or substitution during compositional verification
 * or synthesis.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class RenamedEFAEvent extends AbstractEFAEvent
{
  //#########################################################################
  //# Constructors
  public RenamedEFAEvent(final AbstractEFAEvent original,
                         final ConstraintList update)
  {
    this(original, update, -1);
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
