//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efsm
//# CLASS:   EFSMTransitionRelation
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.efa.AbstractEFATransitionRelation;
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
    super(rel, events, variables, nodes);
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

  @Override
  @SuppressWarnings("unchecked")
  public Collection<EFSMVariable> getVariables()
  {
    return (Collection<EFSMVariable>) super.getVariables();
  }

}
