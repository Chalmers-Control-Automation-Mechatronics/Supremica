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
import java.util.LinkedList;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.dnf.CompiledClause;
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
    mComponent = comp;
    mConditions = conditions;
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

  Collection<Proxy> getSourceLocations()
  {
    return mSourceLocations;
  }

  void addSourceLocation(final Proxy location)
  {
    if (!mConditions.isEmpty()) {
      mSourceLocations.add(location);
    }
  }


  //#########################################################################
  //# Data Members
  private final SimpleComponentProxy mComponent;
  private final CompiledClause mConditions;
  private final Collection<Proxy> mSourceLocations;

}
