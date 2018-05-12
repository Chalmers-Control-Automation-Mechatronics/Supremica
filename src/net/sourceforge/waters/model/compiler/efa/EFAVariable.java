//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * A compiler-internal representation of an EFA variable.
 *
 * @author Robi Malik
 */

class EFAVariable implements Comparable<EFAVariable> {

  //#########################################################################
  //# Constructors
  EFAVariable(final boolean isnext,
              final ComponentProxy comp,
              final CompiledRange range,
              final ModuleProxyFactory factory,
              final CompilerOperatorTable optable)
  {
    mIsNext = isnext;
    mComponent = comp;
    mRange = range;
    final IdentifierProxy ident = mComponent.getIdentifier();
    if (mIsNext) {
      final UnaryOperator nextop = optable.getNextOperator();
      mVariableName = factory.createUnaryExpressionProxy(nextop, ident);
    } else {
      mVariableName = ident;
    }
    if (mIsNext) {
      mEventList = null;
      mEventSet = null;
    } else {
      mEventList = new LinkedList<EFAEvent>();
      mEventSet = new HashSet<EFAEvent>();
    }
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  public String toString()
  {
    return mVariableName.toString();
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  public int compareTo(final EFAVariable var)
  {
    if (mIsNext != var.mIsNext) {
      return mIsNext ? 1 : -1;
    } else {
      return mComponent.compareTo(var.mComponent);
    }
  }


  //#########################################################################
  //# Simple Access
  ComponentProxy getComponent()
  {
    return mComponent;
  }

  SimpleExpressionProxy getVariableName()
  {
    return mVariableName;
  }

  boolean isNext()
  {
    return mIsNext;
  }

  boolean isPartnerOf(final EFAVariable var)
  {
    return mIsNext != var.mIsNext && mComponent == var.mComponent;
  }

  CompiledRange getRange()
  {
    return mRange;
  }

  List<EFAEvent> getEFAEvents()
  {
    return mEventList;
  }

  void addEvent(final EFAEvent event)
  {
    if (mEventSet.add(event)) {
      mEventList.add(event);
    }
  }


  //#########################################################################
  //# Data Members
  private final boolean mIsNext;
  private final ComponentProxy mComponent;
  private final CompiledRange mRange;
  private final SimpleExpressionProxy mVariableName;
  private final List<EFAEvent> mEventList;
  private final Set<EFAEvent> mEventSet;

}
