//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFATransitionLabelEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.simple;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import net.sourceforge.waters.analysis.efa.base.AbstractEFAVariable;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;

/**
 * An implementation of {@link AbstractEFAVariable}.
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFAVariable
 extends AbstractEFAVariable<SimpleEFATransitionLabel>
{

  public SimpleEFAVariable(final VariableComponentProxy var,
                           final CompiledRange range,
                           final ModuleProxyFactory factory,
                           final CompilerOperatorTable op)
  {
    super(var, range, factory, op);
    mModifiers = new THashSet<>();
    mVisitors = new THashSet<>();
    mInStates = new THashSet<>();
    mMarkings = new ArrayList<>(var.getVariableMarkings());
    mVar = var;
    mOperatorTable = op;
    mTransitionRelations = new THashSet<>();
  }

  /**
   * Returns a collection containing all transition relations (EFAs) updating
   * this variable.
   */
  public Collection<IdentifierProxy> getModifiers()
  {
    return Collections.unmodifiableCollection(mModifiers);
  }

  /**
   * Add this component to the list of this variable modifiers.
   * <p/>
   * @param component An EFA component.
   */
  public void addModifier(final SimpleEFAComponent component)
  {
    mModifiers.add(component.getIdentifier());
  }

  public void removeModifier(final SimpleEFAComponent component)
  {
    mModifiers.remove(component.getIdentifier());
  }

  /**
   * Returns a collection containing all transition relations (EFAs) checking
   * this variable.
   */
  public Collection<IdentifierProxy> getVisitors()
  {
    return Collections.unmodifiableCollection(mVisitors);
  }

  /**
   * Add this component to the list of this variable visitors.
   * <p/>
   * @param comopnent
   */
  public void addVisitor(final SimpleEFAComponent comopnent)
  {
    mVisitors.add(comopnent.getIdentifier());
  }

  public void removeVisitor(final SimpleEFAComponent component)
  {
    mVisitors.remove(component.getIdentifier());
  }

  public boolean isModifiedBy(final SimpleEFAComponent component)
  {
    return mModifiers.contains(component.getIdentifier());
  }

  public boolean isVisitedBy(final SimpleEFAComponent component)
  {
    return mVisitors.contains(component.getIdentifier());
  }

  public boolean hasVisitor()
  {
    return mVisitors.isEmpty();
  }

  public boolean hasModifier()
  {
    return !mModifiers.isEmpty();
  }

  public boolean hasStateUser()
  {
    return !mInStates.isEmpty();
  }

  public void addUseInState(final SimpleEFAComponent comopnent)
  {
    mInStates.add(comopnent.getIdentifier());
  }

  public void removeUseInState(final SimpleEFAComponent comopnent)
  {
    mInStates.remove(comopnent.getIdentifier());
  }

  public boolean isUsedInStateBy(final SimpleEFAComponent component)
  {
    return mInStates.contains(component.getIdentifier());
  }

  public boolean isOnlyUsedInStateBy(final SimpleEFAComponent component)
  {
    return (mInStates.size() == 1
     && mInStates.contains(component.getIdentifier()));
  }

  /**
   *
   * @return Marking propositions of this variable.
   */
  public Collection<VariableMarkingProxy> getVariableMarkings()
  {
    return mMarkings;
  }

  public void clearVariableMarkings()
  {
    mMarkings.clear();
  }
  /**
   * Returns a collection containing all transition relations (EFAs) using this
   * variable.
   */
  protected Collection<SimpleEFAComponent> getTransitionRelations()
  {
    return mTransitionRelations;
  }

  protected void addTransitionRelation(final SimpleEFAComponent trans)
  {
    mTransitionRelations.add(trans);
  }

  protected void removeTransitionRelation(final SimpleEFAComponent trans)
  {
    mTransitionRelations.remove(trans);
  }

  /**
   * Return whether this variable is local.
   * <p/>
   * @return <CODE>true</CODE> if the variable is modifies by at most one
   *         component but may visit (appears in guards) by others.
   */
  public boolean isLocalIn(final SimpleEFAComponent component)
  {
    final boolean OM = isOnlyModifiedBy(component);
    final boolean HSU = hasStateUser();
    final boolean OUS = isOnlyUsedInStateBy(component);
    final boolean HM = hasModifier();
    final boolean VB = isVisitedBy(component);
    return (OM) || (!HM && (OUS || !HSU) && VB);
//    return ((isOnlyModifiedBy(component) && (!hasStateUser() || isOnlyUsedInStateBy(component)))
//     || (!hasModifier() && (!hasStateUser() || isOnlyUsedInStateBy(component))
//     && isVisitedBy(component)));
  }


  /**
   * Return whether this variable is local in given component.
   * <p/>
   * @param component
   * <p/>
   * @return <CODE>true</CODE> if the variable is only modified by this
   *         component but may visit (appears in guards) by others or it does
   *         not have any modifier and only checked here.
   */
  public boolean isOnlyModifiedBy(final SimpleEFAComponent component)
  {
    return mModifiers.size() < 2 && isModifiedBy(component);
  }

  public boolean isOnlyVisitedBy(final SimpleEFAComponent component)
  {
    return mVisitors.size() < 2 && isVisitedBy(component);
  }

  public VariableComponentProxy getVariableComponent(
   final ModuleProxyFactory factory)
  {
    final ModuleProxyCloner cloner = factory.getCloner();
    final IdentifierProxy iden =
     (IdentifierProxy) cloner.getClone(getVariableName());
    final CompiledRange range = getRange();
    final SimpleExpressionProxy type =
     range.createExpression(factory, mOperatorTable);

    final SimpleExpressionProxy initialStatePredicate =
     (SimpleExpressionProxy) cloner.getClone(getInitialStatePredicate());
    final Collection<VariableMarkingProxy> variableMarkings =
     cloner.getClonedList(getVariableMarkings());

    return factory.createVariableComponentProxy(iden,
                                                type,
                                                initialStatePredicate,
                                                variableMarkings);
  }

  @Override
  public boolean equals(final Object obj)
  {
    if (obj instanceof SimpleEFAVariable) {
      final VariableComponentProxy var = ((SimpleEFAVariable) obj).getComponent();
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      return eq.equals(mVar, var);
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 17 * hash + Objects.hashCode(mVar);
    return hash;
  }

  private final Collection<IdentifierProxy> mModifiers;
  private final Collection<IdentifierProxy> mVisitors;
  private final THashSet<IdentifierProxy> mInStates;
  private final Collection<VariableMarkingProxy> mMarkings;
  private final CompilerOperatorTable mOperatorTable;
  private final VariableComponentProxy mVar;
  private final Collection<SimpleEFAComponent> mTransitionRelations;
}
