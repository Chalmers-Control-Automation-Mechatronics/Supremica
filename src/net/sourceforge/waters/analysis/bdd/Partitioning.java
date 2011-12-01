//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters BDD
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   Partitioning
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import java.util.SortedSet;
import java.util.TreeSet;

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
    mPartitions = new TreeSet<P>();
    mBDDFactory = factory;
    mClass = clazz;
    mPartitioningSizeLimit = partitioningSizeLimit;
  }


  //#########################################################################
  //# Simple Access
  SortedSet<P> getPartitions()
  {
    return mPartitions;
  }

  void add(final P part)
  {
    mPartitions.add(part);
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
  abstract SortedSet<P> mergePartitions(final AutomatonBDD[] automatonBDDs);


  //#########################################################################
  //# Data Members
  private final SortedSet<P> mPartitions;
  private final BDDFactory mBDDFactory;
  private final Class<P> mClass;
  private final int mPartitioningSizeLimit;

}
