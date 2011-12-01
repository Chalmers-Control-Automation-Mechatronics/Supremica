//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters BDD
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   TransitionPartitioningStrategy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import net.sf.javabdd.BDDFactory;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * @author Robi Malik
 */

public enum TransitionPartitioningStrategy
{

  //#########################################################################
  //# Enumeration
  GREEDY {
    Partitioning<TransitionPartitionBDD>
      createPartitioning(final BDDFactory factory,
                         final ProductDESProxy model,
                         final int partitioningSizeLimit)
    {
      return new GreedyPartitioning<TransitionPartitionBDD>
        (factory, TransitionPartitionBDD.class, partitioningSizeLimit);
    }
  },

  AUTOMATA {
    Partitioning<TransitionPartitionBDD>
      createPartitioning(final BDDFactory factory,
                         final ProductDESProxy model,
                         final int partitioningSizeLimit)
    {
      return new AutomatonPartitioning(factory, model, partitioningSizeLimit);
    }
  };

  //#########################################################################
  //# Abstract Methods
  abstract Partitioning<TransitionPartitionBDD>
    createPartitioning(final BDDFactory factory,
                       final ProductDESProxy model,
                       final int partitioningSizeLimit);

}
