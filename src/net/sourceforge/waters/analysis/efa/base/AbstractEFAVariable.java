//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.efa.base;

import java.util.Collection;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;

import gnu.trove.set.hash.THashSet;

/**
 * A representation of an EFA variable for use in compositional analysis.
 *
 * @author Robi Malik
 */
public abstract class AbstractEFAVariable<L>
 implements Comparable<AbstractEFAVariable<?>>
{

  //#########################################################################
  //# Constructors
  public AbstractEFAVariable(final VariableComponentProxy var,
                             final CompiledRange range,
                             final ModuleProxyFactory factory,
                             final CompilerOperatorTable op)
  {
    mComponent = var;
    mRange = range;
    final ModuleProxyCloner cloner = factory.getCloner();
    final IdentifierProxy ident = var.getIdentifier();
    mVariableName = (IdentifierProxy) cloner.getClone(ident);
    final SimpleExpressionProxy temp = (SimpleExpressionProxy) cloner.getClone(
     ident);
    final UnaryOperator next = op.getNextOperator();
    mPrimedVariableName = factory.createUnaryExpressionProxy(next, temp);
    mInitialStatePredicate =
     (SimpleExpressionProxy) cloner.getClone(var.getInitialStatePredicate());
    mEventDecls = new THashSet<>();
  }

  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public String toString()
  {
    return mVariableName.toString();
  }

  //#########################################################################
  //# Interface java.lang.Comparable<EFAVariable>
  @Override
  public int compareTo(final AbstractEFAVariable<?> var)
  {
    return mComponent.compareTo(var.mComponent);
  }

  //#########################################################################
  //# Simple Access
  public VariableComponentProxy getComponent()
  {
    return mComponent;
  }

  public CompiledRange getRange()
  {
    return mRange;
  }

  public String getName()
  {
    return mVariableName.toString();
  }

  public IdentifierProxy getVariableName()
  {
    return mVariableName;
  }

  public UnaryExpressionProxy getPrimedVariableName()
  {
    return mPrimedVariableName;
  }

  public SimpleExpressionProxy getInitialStatePredicate()
  {
    return mInitialStatePredicate;
  }


  /**
   * Returns a collection containing all events in the system using this
   * variable.
   */
  protected Collection<EventDeclProxy> getRelatedEvent()
  {
    return mEventDecls;
  }

  protected void addTransitionRelation(final EventDeclProxy event)
  {
    mEventDecls.add(event);
  }

  protected void removeTransitionRelation(final EventDeclProxy event)
  {
    mEventDecls.remove(event);
  }

  //#########################################################################
  //# Data Members
  private final VariableComponentProxy mComponent;
  private final CompiledRange mRange;
  private final IdentifierProxy mVariableName;
  private final UnaryExpressionProxy mPrimedVariableName;
  private final SimpleExpressionProxy mInitialStatePredicate;
  private final THashSet<EventDeclProxy> mEventDecls;

}
