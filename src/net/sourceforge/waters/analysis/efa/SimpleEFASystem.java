//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFASystem
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;

/**
 * An implementation of the {@link AbstractEFASystem}.
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFASystem
 extends AbstractEFASystem<SimpleEFATransitionLabel,
                           SimpleEFAVariable,
                           SimpleEFAComponent,
                           SimpleEFAVariableContext>
{

  public SimpleEFASystem(final String name,
                         final SimpleEFAVariableContext context,
                         final int size)
  {
    super(name, context, size);
    mAlphabet = new THashSet<>();
  }

  public SimpleEFASystem(final String name,
                         final List<SimpleEFAVariable> variables,
                         final List<SimpleEFAComponent> components,
                         final SimpleEFAVariableContext context)
  {
    super(name, variables, components, context);
    mAlphabet = new THashSet<>();
  }

  public int getNbrComponents()
  {
    return super.getTransitionRelations().size();
  }

  public List<SimpleEFAComponent> getComponents()
  {
    return super.getTransitionRelations();
  }

  public void addComponent(final SimpleEFAComponent component)
  {
    if(super.addTransitionRelation(component)){
      component.addSystem(this);
    }
  }

  public Collection<SimpleEFAEventDecl> getEvents()
  {
    return mAlphabet;
  }

  public void AddEvent(final SimpleEFAEventDecl event)
  {
    mAlphabet.add(event);
  }

  public void AddEvents(final Collection<SimpleEFAEventDecl> events)
  {
    mAlphabet.addAll(events);
  }

  public boolean removeEvent(final SimpleEFAEventDecl event)
  {
    return mAlphabet.remove(event);
  }

  public boolean removeEvents(final Collection<SimpleEFAEventDecl> events)
  {
    return mAlphabet.removeAll(events);
  }

  @Override
  public void addVariable(final SimpleEFAVariable variable){
    super.addVariable(variable);
  }

  public void addVariables(final Collection<SimpleEFAVariable> variables)
  {
    for (final SimpleEFAVariable var : variables) {
      super.addVariable(var);
    }
  }

  @Override
  public void removeVariable(final SimpleEFAVariable variable)
  {
    super.removeVariable(variable);
  }

  public void removeVariables(final Collection<SimpleEFAVariable> variables)
  {
    for (final SimpleEFAVariable var : variables) {
      super.removeVariable(var);
    }
  }

  public void removeComponent(final SimpleEFAComponent component)
  {
    final THashSet<SimpleEFAEventDecl> otherEvents =
     new THashSet<>(getEvents().size());
    for (final SimpleEFAComponent other : getComponents()) {
      final String otherName = other.getIdentifier().getName();
      if (!component.getIdentifier().getName().equalsIgnoreCase(otherName)) {
        otherEvents.addAll(other.getAlphabet());
      }
    }
    final Collection<SimpleEFAEventDecl> alphabet = component.getAlphabet();
    alphabet.removeAll(otherEvents);
    for (final SimpleEFAEventDecl event : alphabet) {
      removeEvent(event);
    }
    component.dispose();
    super.removeTransitionRelation(component);
  }

  public ModuleProxy getModuleProxy(final ModuleProxyFactory factory)
  {
    final List<SimpleEFAVariable> variableList = getVariables();
    final List<SimpleEFAComponent> comps = getComponents();
    final EFAHelper helper = new EFAHelper(factory);
    final Collection<EventDeclProxy> events =
     helper.getEventDeclProxy(getEvents());
    final TreeMap<String, SimpleComponentProxy> compList =
     new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    final TreeMap<String, VariableComponentProxy> varList =
     new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    for (final SimpleEFAComponent comp : comps) {
      compList.put(comp.getName(), comp.getSimpleComponent());
    }
    for (final SimpleEFAVariable variable : variableList) {
      varList.put(variable.getName(), variable.getVariableComponent(factory));
    }
    final List<ComponentProxy> list = new ArrayList<>(compList
     .size() + varList.size());
    list.addAll(compList.values());
    list.addAll(varList.values());

    return factory.createModuleProxy(
     getName(), null, null, null, events, null, list);
  }

  //#########################################################################
  //# Data Members
  private final Collection<SimpleEFAEventDecl> mAlphabet;
}
