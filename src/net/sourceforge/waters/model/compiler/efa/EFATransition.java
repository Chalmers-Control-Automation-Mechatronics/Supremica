//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFATransition
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;


/**
 * A compiler-internal representation of a partial transition of an EFA.
 * Each guard/action block is converted into a constraint list ({@link
 * ConstraintList}) and gives rise to a partial transition.
 *
 * @author Robi Malik
 */

class EFATransition
{

  //#########################################################################
  //# Constructors
  EFATransition(final SimpleComponentProxy comp,
                final ConstraintList conditions)
  {
    final int numnodes = comp.getGraph().getNodes().size();
    mComponent = comp;
    mGuard = conditions;
    mSourceNodes = new HashSet<NodeProxy>(numnodes);
    mSourceLocations = new LinkedList<Proxy>();
  }


  //#########################################################################
  //# Simple Access
  SimpleComponentProxy getComponent()
  {
    return mComponent;
  }

  ConstraintList getGuard()
  {
    return mGuard;
  }

  Set<NodeProxy> getSourceNodes()
  {
    return mSourceNodes;
  }

  Collection<Proxy> getSourceLocations()
  {
    return mSourceLocations;
  }

  void addSource(final NodeProxy node, final Proxy location)
  {
    mSourceNodes.add(node);
    mSourceLocations.add(location);
  }

  void addSources(final EFATransition trans)
  {
    mSourceNodes.addAll(trans.mSourceNodes);
    mSourceLocations.addAll(trans.mSourceLocations);
  }


  //#########################################################################
  //# Data Members
  private final SimpleComponentProxy mComponent;
  private final ConstraintList mGuard;
  private final Set<NodeProxy> mSourceNodes;
  private final Collection<Proxy> mSourceLocations;

}
