//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   EFSMTransitionRelation
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class EFSMTransitionRelation implements Comparable<EFSMTransitionRelation>
{

  //#########################################################################
  //# Constructors
  public EFSMTransitionRelation(final ListBufferTransitionRelation rel,
                                final EFSMEventEncoding events,
                                final Collection<EFSMVariable> variables,
                                final List<SimpleNodeProxy> nodes)
  {
    mTransitionRelation = rel;
    mEventEncoding = events;
    mVariables = variables;
    mNodeList = nodes;
  }

  public EFSMTransitionRelation(final ListBufferTransitionRelation rel,
                                final EFSMEventEncoding events,
                                final Collection<EFSMVariable> variables)
  {
    this(rel,events,variables,null);
  }



  //#########################################################################
  //# Simple Access
  public ListBufferTransitionRelation getTransitionRelation()
  {
    return mTransitionRelation;
  }

  public EFSMEventEncoding getEventEncoding()
  {
    return mEventEncoding;
  }

  public List<SimpleNodeProxy> getNodeList()
  {
    return mNodeList;
  }

  public String getName()
  {
    return mTransitionRelation.getName();
  }

  public void setName(final String name)
  {
    mTransitionRelation.setName(name);
  }


  public Collection<EFSMVariable> getVariables()
  {
    return mVariables;
  }

  public void register()
  {
    for (final EFSMVariable var : mVariables) {
      var.addTransitionRelation(this);
    }
  }

  public void dispose() {
    for (final EFSMVariable var : mVariables) {
      var.removeTransitionRelation(this);
    }
  }


  //#########################################################################
  //# Interface java.util.Comparable
  @Override
  public int compareTo(final EFSMTransitionRelation efsmTR)
  {
    final String name1 = getName();
    final String name2 = efsmTR.getName();
    return name1.compareTo(name2);
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    return mTransitionRelation.getName() + "\n" + mTransitionRelation.toString();
  }


  //#########################################################################
  //# Data Members
  private final ListBufferTransitionRelation mTransitionRelation;
  private final EFSMEventEncoding mEventEncoding;
  private final List<SimpleNodeProxy> mNodeList;
  private final Collection<EFSMVariable> mVariables;

}
