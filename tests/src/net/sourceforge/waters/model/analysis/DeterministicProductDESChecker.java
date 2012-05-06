//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   DeterministicProductDESChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import gnu.trove.THashSet;
import gnu.trove.TObjectHashingStrategy;

import java.util.Set;

import net.sourceforge.waters.model.des.DefaultProductDESProxyVisitor;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.GraphProxy;

/**
 * A debugging tool to check whether a module may contain nondeterminism.
 * This algorithm simply checks the 'deterministic' flag of the model's
 * graphs {@link GraphProxy}. If a graph with the 'deterministic' flag set
 * to <CODE>false</CODE>, the module is assumed to be nondeterministic.
 *
 * @author Robi Malik
 */
public class DeterministicProductDESChecker
  extends DefaultProductDESProxyVisitor
  implements TObjectHashingStrategy<TransitionProxy>
{

  //#######################################################################
  //# Singleton Pattern
  public static DeterministicProductDESChecker getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final DeterministicProductDESChecker INSTANCE =
      new DeterministicProductDESChecker();
  }

  private DeterministicProductDESChecker()
  {
    mTransitions = new THashSet<TransitionProxy>(this);
  }


  //#######################################################################
  //# Invocation
  public boolean isDeterministic(final ProductDESProxy des)
  {
    return visitProductDESProxy(des);
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.des.ProductDESProxyVisitor
  @Override
  public Boolean visitAutomatonProxy(final AutomatonProxy aut)
  {
    try {
      mHasInit = false;
      for (final StateProxy state : aut.getStates()) {
        if (!visitStateProxy(state)) {
          return false;
        }
      }
      mTransitions.clear();
      for (final TransitionProxy trans : aut.getTransitions()) {
        if (!visitTransitionProxy(trans)) {
          return false;
        }
      }
      return true;
    } finally {
      mTransitions.clear();
    }
  }

  @Override
  public Boolean visitProductDESProxy(final ProductDESProxy des)
  {
    for (final AutomatonProxy aut : des.getAutomata()) {
      final boolean det = visitAutomatonProxy(aut);
      if (!det) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Boolean visitStateProxy(final StateProxy state)
  {
    if (!state.isInitial()) {
      return true;
    } else if (mHasInit) {
      return false;
    } else {
      mHasInit = true;
      return true;
    }
  }

  @Override
  public Boolean visitTransitionProxy(final TransitionProxy trans)
  {
    return mTransitions.add(trans);
  }


  //#######################################################################
  //# Interface gnu.trove.TObjectHashingStrategy
  public int computeHashCode(final TransitionProxy trans)
  {
    return trans.getSource().hashCode() + 5 * trans.getEvent().hashCode();
  }


  public boolean equals(final TransitionProxy trans1,
                        final TransitionProxy trans2)
  {
    return
      trans1.getSource() == trans2.getSource() &&
      trans1.getEvent() == trans2.getEvent();
  }


  //#######################################################################
  //# Data Members
  private boolean mHasInit;
  private final Set<TransitionProxy> mTransitions;


  //#######################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
