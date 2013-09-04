//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis.efa
//# CLASS:   AbstractEFAVariable
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.base;

import gnu.trove.set.hash.THashSet;

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

/**
 * A representation of an EFA variable for use in compositional analysis.
 * <p/>
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
