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
import net.sourceforge.waters.model.module.EventDeclProxy;


/**
 * An event in a unified EFA system.
 * Consists of an event name plus associated update.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class UnifiedEFAEvent
{
  //#########################################################################
  //# Data Members
  public UnifiedEFAEvent(final EventDeclProxy eventDecl,
                         final ConstraintList update)
  {
    mEventDecl = eventDecl;
    mUpdate = update;
  }


  //#########################################################################
  //# Data Members
  public EventDeclProxy getEventDecl()
  {
    return mEventDecl;
  }

  public ConstraintList getUpdate()
  {
    return mUpdate;
  }


  //#######################################################################
  //# Debugging
  @Override
  public String toString()
  {
    final String name = mEventDecl == null ? "(null)" : mEventDecl.getName();
    return name + mUpdate.toString();
  }


  //#########################################################################
  //# Data Members
  private final EventDeclProxy mEventDecl;
  private final ConstraintList mUpdate;
}
