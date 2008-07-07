//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   CompiledGuardCollection
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import net.sourceforge.waters.model.compiler.dnf.CompiledClause;
import net.sourceforge.waters.model.compiler.dnf.CompiledNormalForm;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;


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
  CompiledGuardCollection(final SimpleComponentProxy comp)
  {
    mComponent = comp;
    mMap = new HashMap<CompiledNormalForm,CompiledGuard>();
  }

  CompiledGuardCollection(final SimpleComponentProxy comp,
                          final CompiledNormalForm cnf)
  {
    this(comp);
    addGuard(cnf);
  }

  CompiledGuardCollection(final SimpleComponentProxy comp,
                          final CompiledNormalForm cnf,
                          final EdgeProxy edge)
  {
    this(comp);
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

  Collection<CompiledGuard> getGuards()
  {
    return mMap.values();
  }


  //#########################################################################
  //# Compilation
  Collection<CompiledClause> removeSharedConjuncts()
  {
    if (mMap.isEmpty()) {
      return Collections.emptyList();
    }
    final Collection<CompiledGuard> guards = mMap.values();
    Iterator<CompiledGuard> iter = guards.iterator();
    final CompiledGuard guard1 = iter.next();
    final CompiledNormalForm cnf1 = guard1.getCNF();
    if (cnf1.isEmpty()) {
      return Collections.emptyList();
    }
    final Collection<CompiledClause> result = new LinkedList<CompiledClause>();
    for (final CompiledClause clause : cnf1.getClauses()) {
      boolean shared = true;
      while (iter.hasNext()) {
        final CompiledGuard guard = iter.next();
        final CompiledNormalForm cnf = guard.getCNF();
        if (!cnf.contains(clause)) {
          shared = false;
          break;
        }
      }
      if (shared) {
        result.add(clause);
      }
      iter = guards.iterator();
      iter.next();
    }
    if (result.isEmpty()) {
      return Collections.emptyList();
    }
    final Collection<CompiledGuard> oldguards =
      new ArrayList<CompiledGuard>(guards);
    mMap.clear();
    for (final CompiledGuard guard : oldguards) {
      final CompiledNormalForm cnf = guard.getCNF();
      cnf.removeAll(result);
      mMap.put(cnf, guard);
    }
    return result;
  }


  //#########################################################################
  //# Data Members
  private final SimpleComponentProxy mComponent;
  private final Map<CompiledNormalForm,CompiledGuard> mMap;

}
