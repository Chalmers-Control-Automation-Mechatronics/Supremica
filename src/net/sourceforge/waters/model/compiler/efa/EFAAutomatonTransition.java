//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAAutomatonTransition
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;


/**
 * A compiler-internal representation of a partial transition of an EFA.
 * Each guard/action block is converted into a constraint list ({@link
 * ConstraintList}) and gives rise to a partial transition.
 *
 * @author Robi Malik
 */

class EFAAutomatonTransition
{

  //#########################################################################
  //# Constructors
  EFAAutomatonTransition(final ConstraintList conditions)
  {
    mGuard = conditions;
    mSourceLocations = new LinkedList<Proxy>();
  }


  //#########################################################################
  //# Simple Access
  ConstraintList getGuard()
  {
    return mGuard;
  }

  Collection<Proxy> getSourceLocations()
  {
    return mSourceLocations;
  }

  void addSource(final Proxy location)
  {
    mSourceLocations.add(location);
  }

  void addSources(final EFAAutomatonTransition trans)
  {
    mSourceLocations.addAll(trans.mSourceLocations);
  }


  //#########################################################################
  //# Data Members
  private final ConstraintList mGuard;
  private final Collection<Proxy> mSourceLocations;

}
