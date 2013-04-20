//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   EFSMTransitionRelation
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;


/**
 * @author Robi Malik, Sahar Mohajerani
 */
class EFSMTransitionRelation
{

  //#########################################################################
  //# Constructors
  public EFSMTransitionRelation(final ListBufferTransitionRelation rel,
                                final EFSMEventEncoding events)
  {
    mTransitionRelation = rel;
    mEventEncoding = events;
  }


  //#########################################################################
  //# Simple Access
  public ListBufferTransitionRelation getTransitionRelation()
  {
    return mTransitionRelation;
  }

  public EFSMEventEncoding getEventEncoding()
  {
    return mEventEncoding;
  }


  //#########################################################################
  //# Data Members
  private final ListBufferTransitionRelation mTransitionRelation;
  private final EFSMEventEncoding mEventEncoding;
}
