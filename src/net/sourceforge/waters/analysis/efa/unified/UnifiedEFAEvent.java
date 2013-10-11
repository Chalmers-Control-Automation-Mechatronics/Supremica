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
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * An event in a unified EFA system.
 * This class is used for original events that are directly related
 * to an event declaration in the input module.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class UnifiedEFAEvent extends AbstractEFAEvent
{
  //#########################################################################
  //# Constructors
  public UnifiedEFAEvent(final EventDeclProxy eventDecl,
                         final ConstraintList update)
  {
    super(update);
    mEventDecl = eventDecl;
  }


  //#########################################################################
  //# Simple Access
  public EventDeclProxy getEventDecl()
  {
    return mEventDecl;
  }

  @Override
  public String getName()
  {
    if (mEventDecl != null) {
      return mEventDecl.getName();
    } else {
      return "(null)";
    }
  }

  @Override
  public EventKind getKind()
  {
    return mEventDecl.getKind();
  }

  @Override
  public boolean isObservable()
  {
    return mEventDecl.isObservable();
  }


  //#########################################################################
  //# Data Members
  private final EventDeclProxy mEventDecl;

}
