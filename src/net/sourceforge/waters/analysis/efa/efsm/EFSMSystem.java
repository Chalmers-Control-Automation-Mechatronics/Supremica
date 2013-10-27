//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   EFSMSystem
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.efa.base.AbstractEFASystem;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;


/**
 * @author Robi Malik, Sahar Mohajerani
 */

class EFSMSystem
  extends AbstractEFASystem<ConstraintList,
                            EFSMVariable,
                            EFSMTransitionRelation,
                            EFSMVariableContext>
{

  //#########################################################################
  //# Constructors
  EFSMSystem(final String name, final EFSMVariableContext context)
  {
    super(name, context);
  }

  EFSMSystem(final String name,
             final EFSMVariableContext context,
             final int size)
  {
    super(name, context, size);
  }

  EFSMSystem(final String name,
             final List<EFSMVariable> variables,
             final List<EFSMTransitionRelation> transitionRelations,
             final EFSMVariableContext context)
  {
    super(name, variables, transitionRelations, context);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.efa.base.AbstractEFASystem
  @Override
  public List<EFSMTransitionRelation> getTransitionRelations()
  {
    return super.getTransitionRelations();
  }

  @Override
  public boolean addTransitionRelation(final EFSMTransitionRelation transitionRelation)
  {
    return super.addTransitionRelation(transitionRelation);
  }

  @Override
  public void removeTransitionRelation(final EFSMTransitionRelation transitionRelation)
  {
    super.removeTransitionRelation(transitionRelation);
  }

  @Override
  public void removeVariable(final EFSMVariable var)
  {
    super.removeVariable(var);
    for (final EFSMTransitionRelation tran : getTransitionRelations()) {
      tran.removeVariable(var);
    }
  }


  //#########################################################################
  //# Heuristic Support
  /**
   * Returns a list of all local variables in this system,
   * in the order in which they are listed.
   */
  List<EFSMVariable> getLocalVariables()
  {
    final List<EFSMVariable> all = getVariables();
    final List<EFSMVariable> local = new ArrayList<>(all.size());
    for (final EFSMVariable var : all) {
      if (var.isLocal()) {
        local.add(var);
      }
    }
    return local;
  }

  /**
   * Returns a set of all pairs of transition relations in this system
   * that share at least one variable.
   */
  Set<EFSMPair> getPairs()
  {
    final Set<EFSMPair> pairs = new THashSet<>();
    for (final EFSMVariable var : getVariables()) {
      final Collection<EFSMTransitionRelation> efsmTRSet =
        var.getTransitionRelations();
      final List<EFSMTransitionRelation> efsmTRList =
        new ArrayList<EFSMTransitionRelation>(efsmTRSet);
      for (int i = 0; i < efsmTRList.size(); i++) {
        final EFSMTransitionRelation efsmTR1 = efsmTRList.get(i);
        for (int j = i + 1; j < efsmTRList.size(); j++) {
          final EFSMTransitionRelation efsmTR2 = efsmTRList.get(j);
          final EFSMPair pair = new EFSMPair(efsmTR1, efsmTR2);
          pairs.add(pair);
        }
      }
    }
    return pairs;
  }

}
