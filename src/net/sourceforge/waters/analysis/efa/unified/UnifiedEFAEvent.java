//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   UnifiedEFAEvent
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.module.EventDeclProxy;

/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class UnifiedEFAEvent
{
  UnifiedEFAEvent(final EventDeclProxy eventDecl, final ConstraintList update)
  {
    mEventDecl = eventDecl;
    mUpdate = update;
  }

  public EventDeclProxy getEventDecl()
  {
    return mEventDecl;
  }

  public ConstraintList getUpdate()
  {
    return mUpdate;
  }

  //#########################################################################
  //# Data Members
  private final EventDeclProxy mEventDecl;
  private final ConstraintList mUpdate;
}
