//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   UnifiedEFAEvent
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import net.sourceforge.waters.xsd.base.EventKind;


/**
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class SilentEFAEvent extends AbstractEFAEvent
{
  //#########################################################################
  //# Constructors
  public SilentEFAEvent(final String suffix)
  {
    mSuffix = suffix;
  }


  //#########################################################################
  //# Simple Access
  @Override
  public String getName()
  {
    if (mSuffix != null) {
      return "tau:" + mSuffix;
    }
    return "tau";
  }


  @Override
  public EventKind getKind()
  {
    return EventKind.CONTROLLABLE;
  }


  @Override
  public boolean isObservable()
  {
    return false;
  }


  //#########################################################################
  //# Data Members
  private final String mSuffix;
}
