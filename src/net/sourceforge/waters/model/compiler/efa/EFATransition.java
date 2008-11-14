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
import net.sourceforge.waters.model.compiler.dnf.CompiledClause;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;


/**
 * A compiler-internal representation of a partial transition of an EFA.
 * Each guard/action block is converted into disjunctive normal form (DNF),
 * and each minterm of the DNF (conjunction of literals) gives rise to
 * a partial transition.
 *
 * @author Robi Malik
 */

class EFATransition
{

  //#########################################################################
  //# Constructors
  EFATransition(final SimpleComponentProxy comp,
                final CompiledClause conditions)
  {
    final int numnodes = comp.getGraph().getNodes().size();
    mComponent = comp;
    mConditions = conditions;
    mSourceNodes = new HashSet<NodeProxy>(numnodes);
    mSourceLocations = new LinkedList<Proxy>();
  }


  //#########################################################################
  //# Simple Access
  SimpleComponentProxy getComponent()
  {
    return mComponent;
  }

  CompiledClause getConditions()
  {
    return mConditions;
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
  private final CompiledClause mConditions;
  private final Set<NodeProxy> mSourceNodes;
  private final Collection<Proxy> mSourceLocations;

}
