//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   EFSMPair
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import java.util.Set;

/**
 * A pair of two EFSM transition relation, as a candidate for composition.
 *
 * @author Robi Malik
 */

class EFSMPair implements Comparable<EFSMPair>
{

  //#########################################################################
  //# Constructor
  EFSMPair(final EFSMTransitionRelation efsmTR1,
           final EFSMTransitionRelation efsmTR2)
  {
    if (efsmTR1.compareTo(efsmTR2) <= 0) {
      mEFSMTransitionRelation1 = efsmTR1;
      mEFSMTransitionRelation2 = efsmTR2;
    } else {
      mEFSMTransitionRelation1 = efsmTR2;
      mEFSMTransitionRelation2 = efsmTR1;
    }
  }


  //#########################################################################
  //# Simple Access
  EFSMTransitionRelation getFirst()
  {
    return mEFSMTransitionRelation1;
  }

  EFSMTransitionRelation getSecond()
  {
    return mEFSMTransitionRelation2;
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public boolean equals(final Object object)
  {
    if (getClass() == object.getClass()) {
      final EFSMPair pair = (EFSMPair) object;
      return mEFSMTransitionRelation1 == pair.mEFSMTransitionRelation1 &&
             mEFSMTransitionRelation2 == pair.mEFSMTransitionRelation2;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode()
  {
    return mEFSMTransitionRelation1.hashCode() +
           5 * mEFSMTransitionRelation2.hashCode();
  }


  //#########################################################################
  //# Interface java.util.Comparable
  @Override
  public int compareTo(final EFSMPair pair)
  {
    final int result =
      mEFSMTransitionRelation1.compareTo(pair.mEFSMTransitionRelation1);
    if (result != 0) {
      return result;
    }
    return
      mEFSMTransitionRelation2.compareTo(pair.mEFSMTransitionRelation2);
  }


  //#########################################################################
  //# Collection/Array Access
  EFSMTransitionRelation[] asArray()
  {
    final EFSMTransitionRelation[] array = new EFSMTransitionRelation[2];
    array[0] = mEFSMTransitionRelation1;
    array[1] = mEFSMTransitionRelation2;
    return array;
  }

  boolean containsAll(final Set<EFSMTransitionRelation> set)
  {
    if (set.size() > 2) {
      return false;
    } else {
      for (final EFSMTransitionRelation efsmTR : set) {
        if (mEFSMTransitionRelation1 != efsmTR &&
            mEFSMTransitionRelation2 != efsmTR) {
          return false;
        }
      }
      return true;
    }
  }


  //#########################################################################
  //# Data Members
  private final EFSMTransitionRelation mEFSMTransitionRelation1;
  private final EFSMTransitionRelation mEFSMTransitionRelation2;

}
