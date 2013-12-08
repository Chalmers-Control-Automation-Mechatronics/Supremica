//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   NonDeterministicTransitionHashingStrategy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.des;

import gnu.trove.strategy.HashingStrategy;


/**
 * A GNU Trove hashing strategy for transitions in nondeterministic automata.
 * Considers transitions as equal if they have the same source and target
 * states and events, where states and events are compared by object identity.
 *
 * @author Robi Malik
 */

public class NonDeterministicTransitionHashingStrategy
  implements HashingStrategy<TransitionProxy>
{

  //#######################################################################
  //# Singleton Pattern
  public static HashingStrategy<TransitionProxy> getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final NonDeterministicTransitionHashingStrategy INSTANCE =
      new NonDeterministicTransitionHashingStrategy();
  }


  //#######################################################################
  //# Interface gnu.trove.TObjectHashingStrategy
  @Override
  public int computeHashCode(final TransitionProxy trans)
  {
    return trans.getSource().hashCode() +
           5 * trans.getEvent().hashCode() +
           25 * trans.getEvent().hashCode();
  }

  @Override
  public boolean equals(final TransitionProxy trans1,
                        final TransitionProxy trans2)
  {
    return trans1.getSource() == trans2.getSource() &&
           trans1.getEvent() == trans2.getEvent() &&
           trans1.getTarget() == trans2.getTarget();
  }


  //#######################################################################
  //# Class Constants
  private static final long serialVersionUID = 3821697879013630243L;

}