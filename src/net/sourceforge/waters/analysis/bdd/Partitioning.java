//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters BDD
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   Partitioning
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import java.util.ArrayList;
import java.util.List;
import net.sf.javabdd.BDDFactory;


/**
 * @author Robi Malik
 */

abstract class Partitioning<P extends PartitionBDD>
{

  //#########################################################################
  //# Constructor
  Partitioning(final BDDFactory factory,
               final Class<P> clazz,
               final int partitioningSizeLimit)
  {
    mFullPartition = new ArrayList<P>();
    mBDDFactory = factory;
    mClass = clazz;
    mPartitioningSizeLimit = partitioningSizeLimit;
  }


  //#########################################################################
  //# Simple Access
  void add(final P part)
  {
    mFullPartition.add(part);
  }

  List<P> getFullPartition()
  {
    return mFullPartition;
  }

  BDDFactory getBDDFactory()
  {
    return mBDDFactory;
  }

  P castBDD(final PartitionBDD bdd)
  {
    return mClass.cast(bdd);
  }

  int getPartitioningSizeLimit()
  {
    return mPartitioningSizeLimit;
  }

  //#########################################################################
  //# Algorithm
  abstract void setUpAndMerge(final AutomatonBDD[] automatonBDDs);

  abstract List<P> nextGroup(boolean stable);


  //#########################################################################
  //# Data Members
  private final List<P> mFullPartition;
  private final BDDFactory mBDDFactory;
  private final Class<P> mClass;
  private final int mPartitioningSizeLimit;

}
