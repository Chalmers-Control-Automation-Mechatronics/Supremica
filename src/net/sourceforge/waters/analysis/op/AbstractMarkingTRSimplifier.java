//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis Algorithms
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   AbstractMarkingTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;


public abstract class AbstractMarkingTRSimplifier
  extends AbstractTRSimplifier
{

  //#########################################################################
  //# Constructors
  public AbstractMarkingTRSimplifier()
  {
    this(null);
  }

  public AbstractMarkingTRSimplifier
    (final ListBufferTransitionRelation rel)
  {
    super(rel);
    mPreconditionMarkingID = mDefaultMarkingID = -1;
    mPropositions = null;
  }


  //#########################################################################
  //# Configuration
  public void setDefaultMarkingID(final int defaultID)
  {
    setPropositions(-1, defaultID);
  }

  public void setPropositions(final int preconditionID, final int defaultID)
  {
    mPreconditionMarkingID = preconditionID;
    mDefaultMarkingID = defaultID;
    mPropositions = null;
  }

  public int getPreconditionMarkingID()
  {
    return mPreconditionMarkingID;
  }

  public int getDefaultMarkingID()
  {
    return mDefaultMarkingID;
  }

  public int[] getPropositions()
  {
    if (mPropositions == null) {
      if (mPreconditionMarkingID < 0) {
        if (mDefaultMarkingID < 0) {
          mPropositions = new int[0];
        } else {
          mPropositions = new int[1];
          mPropositions[0] = mDefaultMarkingID;
        }
      } else {
        if (mDefaultMarkingID < 0) {
          mPropositions = new int[1];
          mPropositions[0] = mPreconditionMarkingID;
        } else {
          mPropositions = new int[2];
          mPropositions[0] = mDefaultMarkingID;
          mPropositions[1] = mPreconditionMarkingID;
        }
      }
    }
    return mPropositions;
  }


  //#########################################################################
  //# Data Members
  private int mPreconditionMarkingID;
  private int mDefaultMarkingID;
  private int[] mPropositions;

}
