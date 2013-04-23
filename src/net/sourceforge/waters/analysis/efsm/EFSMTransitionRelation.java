//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   EFSMTransitionRelation
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import java.util.List;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class EFSMTransitionRelation
{

  //#########################################################################
  //# Constructors
  public EFSMTransitionRelation(final ListBufferTransitionRelation rel,
                                final EFSMEventEncoding events,
                                final List<SimpleNodeProxy> nodes)
  {
    mTransitionRelation = rel;
    mEventEncoding = events;
    mNodeList = nodes;
  }

  public EFSMTransitionRelation(final ListBufferTransitionRelation rel,
                                final EFSMEventEncoding events)
  {
    mTransitionRelation = rel;
    mEventEncoding = events;
    mNodeList = null;
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


  //#########################################################################
  //# Data Members
  private final ListBufferTransitionRelation mTransitionRelation;
  private final EFSMEventEncoding mEventEncoding;
  private final List<SimpleNodeProxy> mNodeList;
}
