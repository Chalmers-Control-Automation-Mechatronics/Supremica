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
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.xsd.module.ScopeKind;

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

  public Collection<SimpleEFAEventDecl> getSystemEvents()
  {
    return mAlphabet;
  }

  public void AddSystemEvent(final SimpleEFAEventDecl event)
  {
    mAlphabet.add(event);
  }

  public void AddSystemEvents(final Collection<SimpleEFAEventDecl> events)
  {
    mAlphabet.addAll(events);
  }

  public void clearSystemEvents()
  {
    mAlphabet.clear();
  }
  
  public boolean removeSystemEvent(SimpleEFAEventDecl event){
    return mAlphabet.remove(event);
  }
  
  @Override
  public void addVariable(final SimpleEFAVariable variable){
    super.addVariable(variable);
  }

  public void addAllVariable(final Collection<SimpleEFAVariable> variables)
  {
    for (SimpleEFAVariable var : variables) {
      super.addVariable(var);
    }
  }

  @Override
  public void removeVariable(final SimpleEFAVariable var)
  {
    super.removeVariable(var);
  }

  protected void removeComponent(final SimpleEFAComponent component)
  {
    super.removeTransitionRelation(component);
  }

  public ModuleProxy getModuleProxy(final ModuleProxyFactory factory)
  {
    final List<SimpleEFAVariable> variableList = getVariables();
    final List<SimpleEFAComponent> comps = getComponents();
    EFAHelper helper = new EFAHelper(factory);
    Collection<EventDeclProxy> events =
     helper.getEventDeclProxy(getSystemEvents());
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
