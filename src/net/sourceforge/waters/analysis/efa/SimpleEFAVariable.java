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
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;

import java.util.ArrayList;
import java.util.Collection;

/**
 * An implementation of {@link AbstractEFAVariable}.
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFAVariable
 extends AbstractEFAVariable<SimpleEFATransitionLabel>
{

  /**
   * Collection of transition relations (EFA) using this variable.
   */
  private final Collection<SimpleEFAComponent> mModifiers;
  private final Collection<SimpleEFAComponent> mVisitors;
  private final Collection<VariableMarkingProxy> mMarkings;

  public SimpleEFAVariable(final VariableComponentProxy var,
   final CompiledRange range,
   final ModuleProxyFactory factory,
   final CompilerOperatorTable op)
  {
    super(var, range, factory, op);
    mModifiers = new THashSet<SimpleEFAComponent>();
    mVisitors = new THashSet<SimpleEFAComponent>();
    mMarkings = new ArrayList<VariableMarkingProxy>(var.getVariableMarkings());
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
  
}
