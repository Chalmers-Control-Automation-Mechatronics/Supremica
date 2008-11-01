//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   ConditionPartitionBDD
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;


/**
 * @author Robi Malik
 */

class ConditionPartitionBDD
  extends PartitionBDD
{

  //#########################################################################
  //# Constructor
  ConditionPartitionBDD(final EventBDD eventBDD)
  {
    super(eventBDD.getEvent(),
          eventBDD.getControllabilityConditionBDD(), 
          eventBDD.getControllabilityTestedAutomata());
  }

  ConditionPartitionBDD(final ConditionPartitionBDD part1,
                        final ConditionPartitionBDD part2,
                        final BDD bdd)
  {
    super(part1, part2, bdd);
  }


  //#########################################################################
  //# Overrides for Abstract Base Class PartitionBDD
  ConditionPartitionBDD compose(final PartitionBDD part,
                                final AutomatonBDD[] automatonBDDs,
                                final BDDFactory factory)
  {
    return compose((ConditionPartitionBDD) part, automatonBDDs, factory);
  }

  ConditionPartitionBDD compose(final ConditionPartitionBDD part,
                                final AutomatonBDD[] automatonBDDs,
                                final BDDFactory factory)
  {
    final BDD bdd1 = getBDD();
    final BDD bdd2 = part.getBDD();
    final BDD bdd = bdd1.and(bdd2);
    return new ConditionPartitionBDD(this, part, bdd);
  }

}
