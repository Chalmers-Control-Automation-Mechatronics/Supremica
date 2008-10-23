//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   CompiledGuard
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.Collection;
import java.util.LinkedList;

import net.sourceforge.waters.model.compiler.dnf.CompiledNormalForm;
import net.sourceforge.waters.model.module.EdgeProxy;


/**
 * A compiler-internal representation of a single guard/action relation
 * attached to one ore more edges in an automaton. More than one edges
 * are supported in order to detect multiple equivalent guards attached
 * to the same event in the same automaton.
 *
 * @author Robi Malik
 */

class CompiledGuard
{

  //#########################################################################
  //# Constructors
  CompiledGuard(final CompiledNormalForm cnf)
  {
    this(cnf, null);
  }

  CompiledGuard(final CompiledNormalForm cnf, final EdgeProxy edge)
  {
    mCNF = cnf;
    mEdges = new LinkedList<EdgeProxy>();
    addEdge(edge);
  }


  //#########################################################################
  //# Simple Access
  CompiledNormalForm getCNF()
  {
    return mCNF;
  }

  Collection<EdgeProxy> getEdges()
  {
    return mEdges;
  }

  void addEdge(final EdgeProxy edge)
  {
    if (edge != null) {
      mEdges.add(edge);
    }
  }


  //#########################################################################
  //# Data Members
  private final CompiledNormalForm mCNF;
  private final Collection<EdgeProxy> mEdges;

}
