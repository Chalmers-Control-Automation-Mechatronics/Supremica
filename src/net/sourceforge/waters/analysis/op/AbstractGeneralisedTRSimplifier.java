//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis Algorithms
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   AbstractGeneralisedTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;


public abstract class AbstractGeneralisedTRSimplifier
  extends AbstractTRSimplifier
{

  //#########################################################################
  //# Constructors
  public AbstractGeneralisedTRSimplifier()
  {
    this(null);
  }

  public AbstractGeneralisedTRSimplifier
    (final ListBufferTransitionRelation rel)
  {
    super(rel);
    mPreconditionMarkingID = mDefaultMarkingID = -1;
  }


  //#########################################################################
  //# Configuration
  public void setPropositions(final int preconditionID, final int defaultID)
  {
    mPreconditionMarkingID = preconditionID;
    mDefaultMarkingID = defaultID;
  }

  public int getPreconditionMarkingID()
  {
    return mPreconditionMarkingID;
  }

  public int getDefaultMarkingID()
  {
    return mDefaultMarkingID;
  }


  //#########################################################################
  //# Data Members
  private int mPreconditionMarkingID;
  private int mDefaultMarkingID;

}
