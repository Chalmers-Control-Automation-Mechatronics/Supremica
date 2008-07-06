//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   CompiledGuardCollection
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.compiler.dnf.CompiledNormalForm;
import net.sourceforge.waters.model.module.EdgeProxy;


/**
 * A compiler-internal representation of a set of guard/action relations
 * all related to the same event in an automaton.
 *
 * @author Robi Malik
 */

class CompiledGuardCollection
{

  //#########################################################################
  //# Constructors
  CompiledGuardCollection()
  {
    mMap = new HashMap<CompiledNormalForm,CompiledGuard>();
  }

  CompiledGuardCollection(final CompiledNormalForm cnf)
  {
    this();
    addGuard(cnf);
  }

  CompiledGuardCollection(final CompiledNormalForm cnf, final EdgeProxy edge)
  {
    this();
    addGuard(cnf, edge);
  }


  //#########################################################################
  //# Simple Access
  void addGuard(final CompiledNormalForm cnf)
  {
    final CompiledGuard oldguard = mMap.get(cnf);
    if (oldguard == null) {
      final CompiledGuard newguard = new CompiledGuard(cnf);
      mMap.put(cnf, newguard);
    }
  }

  void addGuard(final CompiledNormalForm cnf, final EdgeProxy edge)
  {
    final CompiledGuard oldguard = mMap.get(cnf);
    if (oldguard == null) {
      final CompiledGuard newguard = new CompiledGuard(cnf, edge);
      mMap.put(cnf, newguard);
    } else {
      oldguard.addEdge(edge);
    }
  }


  //#########################################################################
  //# Data Members
  private Map<CompiledNormalForm,CompiledGuard> mMap;

}
