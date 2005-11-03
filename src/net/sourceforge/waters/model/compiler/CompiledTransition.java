//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   CompiledTransition
//###########################################################################
//# $Id: CompiledTransition.java,v 1.2 2005-11-03 01:24:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.NodeProxy;


class CompiledTransition
{

  //#########################################################################
  //# Constructors
  CompiledTransition(final TransitionProxy trans, final NodeProxy group)
  {
    mTransition = trans;
    mGroup = group;
  }


  //#########################################################################
  //# Simple Access
  TransitionProxy getTransition()
  {
    return mTransition;
  }

  StateProxy getSource()
  {
    return mTransition.getSource();
  }

  EventProxy getEvent()
  {
    return mTransition.getEvent();
  }

  StateProxy getTarget()
  {
    return mTransition.getTarget();
  }

  NodeProxy getGroup()
  {
    return mGroup;
  }


  //#########################################################################
  //# Data Members
  private final TransitionProxy mTransition;
  private final NodeProxy mGroup;

}
