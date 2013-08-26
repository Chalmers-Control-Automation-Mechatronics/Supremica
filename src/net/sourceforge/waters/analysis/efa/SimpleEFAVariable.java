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

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.module.IdentifierProxy;
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
    mOperatorTable = op;
  }

  /**
   * Returns a collection containing all transition relations (EFAs) updating
   * this variable.
   */
  public Collection<SimpleEFAComponent> getModifiers()
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
    super.addTransitionRelation(component);
    mModifiers.add(component);
  }

  public void removeModifiers(final SimpleEFAComponent trans)
  {
    mModifiers.remove(trans);
  }

  /**
   * Returns a collection containing all transition relations (EFAs) checking
   * this variable.
   */
  public Collection<SimpleEFAComponent> getVisitors()
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
    super.addTransitionRelation(comopnent);
    mVisitors.add(comopnent);
  }

  public void removeVisitor(final SimpleEFAComponent trans)
  {
    mVisitors.remove(trans);
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

  public VariableComponentProxy getVariableComponent(ModuleProxyFactory factory)
  {
    ModuleProxyCloner cloner = factory.getCloner();
    IdentifierProxy iden =
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
  private final Collection<SimpleEFAComponent> mModifiers;
  private final Collection<SimpleEFAComponent> mVisitors;
  private final Collection<VariableMarkingProxy> mMarkings;
  private final CompilerOperatorTable mOperatorTable;
}
