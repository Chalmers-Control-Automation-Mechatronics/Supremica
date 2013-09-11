//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   AbstractEFAEvent
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.xsd.base.EventKind;

/**
 * An event in a unified EFA system.
 * Consists of an event name plus associated update.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public abstract class AbstractEFAEvent
{
  //#########################################################################
  //# Constructors
  public AbstractEFAEvent()
  {
    this(ConstraintList.TRUE);
  }

  public AbstractEFAEvent(final ConstraintList update)
  {
    mUpdate = update;
  }


  //#########################################################################
  //# Simple Access
  public ConstraintList getUpdate()
  {
    return mUpdate;
  }

  public abstract String getName();

  public abstract EventKind getKind();

  public abstract boolean isObservable();


  //#######################################################################
  //# Debugging
  @Override
  public String toString()
  {
    return getName() + mUpdate.toString();
  }


  //#########################################################################
  //# Data Members
  private final ConstraintList mUpdate;

}
