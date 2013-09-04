//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   EFSMTransitionRelation
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.efa.base.AbstractEFATransitionRelation;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class EFSMTransitionRelation
  extends AbstractEFATransitionRelation<ConstraintList>
{

  //#########################################################################
  //# Constructors
  public EFSMTransitionRelation(final ListBufferTransitionRelation rel,
                                final EFSMEventEncoding events,
                                final Collection<EFSMVariable> variables,
                                final List<SimpleNodeProxy> nodes)
  {
    super(rel, events, nodes);
    mVariables = variables;
  }

  public EFSMTransitionRelation(final ListBufferTransitionRelation rel,
                                final EFSMEventEncoding events,
                                final Collection<EFSMVariable> variables)
  {
    this(rel, events, variables, null);
  }


  //#########################################################################
  //# Simple Access
  public EFSMEventEncoding getEventEncoding()
  {
    return (EFSMEventEncoding) super.getTransitionLabelEncoding();
  }

  void addVariable(final EFSMVariable variable)
  {
    mVariables.add(variable);
    variable.addTransitionRelation(this);
  }

  void removeVariable(final EFSMVariable variable)
  {
    mVariables.remove(variable);
    variable.removeTransitionRelation(this);
  }

  Collection<EFSMVariable> getVariables()
  {
    return mVariables;
  }

  /**
   * Registers this transition relation by adding its reference to all its
   * variables.
   */
  void register()
  {
    for (final EFSMVariable var : mVariables) {
      var.addTransitionRelation(this);
    }
  }

  /**
   * Deregisters this transition relation by removing its reference from all its
   * variables.
   */
  void dispose()
  {
    for (final EFSMVariable var : mVariables) {
      var.removeTransitionRelation(this);
    }
  }

  //#########################################################################
  //# Data members
  private final Collection<EFSMVariable> mVariables;
}
