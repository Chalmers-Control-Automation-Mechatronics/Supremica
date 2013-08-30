//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFATransitionLabelEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

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
    mMarkings = new ArrayList<>(var.getVariableMarkings());
    mVar = var;
    mOperatorTable = op;
  }

  /**
   * Returns a collection containing all transition relations (EFAs) updating
   * this variable.
   */
  public Collection<IdentifierProxy> getModifiers()
  {
    return mModifiers;
  }

  /**
   * Add this component to the list of this variable modifiers.
   * <p/>
   * @param component An EFA component.
   */
  public void addModifier(final SimpleEFAComponent component)
  {
//    super.addTransitionRelation(component);
    mModifiers.add(component.getIdentifier());
  }

  public void removeModifiers(final IdentifierProxy component)
  {
    mModifiers.remove(component);
  }

  /**
   * Returns a collection containing all transition relations (EFAs) checking
   * this variable.
   */
  public Collection<IdentifierProxy> getVisitors()
  {
    return mVisitors;
  }

  /**
   * Add this component to the list of this variable visitors.
   * <p/>
   * @param comopnent
   */
  public void addVisitor(final SimpleEFAComponent comopnent)
  {
//    super.addTransitionRelation(comopnent);
    mVisitors.add(comopnent.getIdentifier());
  }

  public void removeVisitor(final IdentifierProxy component)
  {
    mVisitors.remove(component);
  }

  public boolean isModifiedBy(final IdentifierProxy component)
  {
    return mModifiers.contains(component);
  }

  public boolean isVisitedBy(final IdentifierProxy component)
  {
    return mVisitors.contains(component);
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

  public boolean isDeterministic()
  {
    return getComponent().isDeterministic();
  }

  /**
   * Return whether this variable is local.
   * <p/>
   * @return <CODE>true</CODE> if the variable modifies in at most one EFA
   *         component but may visit (appears in guards) by others.
   */
  @Override
  public boolean isLocal()
  {
    return mModifiers.size() <= 1;
  }

  public VariableComponentProxy getVariableComponent(final ModuleProxyFactory factory)
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
                                                isDeterministic(),
                                                initialStatePredicate,
                                                variableMarkings);
  }

  @Override
  public boolean equals(final Object obj)
  {
    if (obj instanceof SimpleEFAVariable) {
      final VariableComponentProxy var = ((SimpleEFAVariable) obj).getComponent();
      final ModuleEqualityVisitor eq =
       ModuleEqualityVisitor.getInstance(false);
      return eq.equals(this.mVar, var);
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 17 * hash + Objects.hashCode(this.mVar);
    return hash;
  }

  private final Collection<IdentifierProxy> mModifiers;
  private final Collection<IdentifierProxy> mVisitors;
  private final Collection<VariableMarkingProxy> mMarkings;
  private final CompilerOperatorTable mOperatorTable;
  private final VariableComponentProxy mVar;
}
