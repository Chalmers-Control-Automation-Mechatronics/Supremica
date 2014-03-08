//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFASystem
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.simple;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import net.sourceforge.waters.analysis.efa.base.AbstractEFASystem;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.xsd.base.EventKind;

/**
 * A simple implementation of the abstract system {@link AbstractEFASystem}.
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
    mGloballAlphabet = new THashSet<>();
  }

  public SimpleEFASystem(final String name,
                         final List<SimpleEFAVariable> variables,
                         final List<SimpleEFAComponent> components,
                         final SimpleEFAVariableContext context)
  {
    super(name, variables, components, context);
    mGloballAlphabet = new THashSet<>();
  }

  public int getNbrComponents()
  {
    return super.getTransitionRelations().size();
  }

  public List<SimpleEFAComponent> getComponents()
  {
    return super.getTransitionRelations();
  }

  public void addComponent(final SimpleEFAComponent component) throws
   AnalysisException
  {
    for (final SimpleEFAEventDecl event : component.getAlphabet()) {
      if (!mGloballAlphabet.contains(event)) {
        throw new AnalysisException("Inconsistency in event set is detected: <"
         + event.getName() + ">");
      }
    }
    for (final SimpleEFAVariable var : component.getVariables()) {
      if (!super.getVariables().contains(var)) {
        throw new AnalysisException(
         "Inconsistency in variable set is detected: <"
         + var.getName() + ">");
      }
    }
    if (super.addTransitionRelation(component)) {
      component.addSystem(this);
    }
  }

  public Collection<SimpleEFAEventDecl> getEvents()
  {
    return mGloballAlphabet;
  }

  public void addEvent(final SimpleEFAEventDecl event)
  {
    mGloballAlphabet.add(event);
  }

  public void addEvents(final Collection<SimpleEFAEventDecl> events)
  {
    mGloballAlphabet.addAll(events);
  }

  public boolean removeEvent(final SimpleEFAEventDecl event)
  {
    return mGloballAlphabet.remove(event);
  }

  public boolean removeEvents(final Collection<SimpleEFAEventDecl> events)
  {
    return mGloballAlphabet.removeAll(events);
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
    for (final SimpleEFAComponent comp : getComponents()) {
      comp.removeVariable(variable);
    }
  }

  public void removeVariables(final Collection<SimpleEFAVariable> variables)
  {
    for (final SimpleEFAVariable var : variables) {
      removeVariable(var);
    }
  }

  public void removeComponent(final SimpleEFAComponent component)
  {
    for (final SimpleEFAEventDecl event : component.getAlphabet()) {
      if (event.isLocalIn(component) && !event.isProposition()) {
        removeEvent(event);
      }
    }

    component.removeSystem(this);
    super.removeTransitionRelation(component);
  }

  public void disposeComponent(final SimpleEFAComponent component)
  {
    component.dispose();
    removeComponent(component);
  }

  public void disposeComponents(final List<SimpleEFAComponent> components)
  {
    for (final SimpleEFAComponent comp : components) {
      disposeComponent(comp);
    }
  }

  public void updateTransitionRelations()
  {
    for (final SimpleEFAComponent comp : getComponents()) {
      final ListBufferTransitionRelation tr = comp.getTransitionRelation();
      final SimpleEFATransitionLabelEncoding trEncoding =
       comp.getTransitionLabelEncoding();
      for (int id = 0; id < trEncoding.size(); id++) {
        final SimpleEFATransitionLabel label = trEncoding.getTransitionLabel(id);
        byte status = EventEncoding.STATUS_NONE;
        if (label.getKind() == EventKind.CONTROLLABLE) {
          status |= EventEncoding.STATUS_CONTROLLABLE;
        }
        if (label.getEvent().isLocalIn(comp)) {
          status |= EventEncoding.STATUS_LOCAL;
        }
        tr.setProperEventStatus(id, status);
      }
    }
  }

  public ModuleProxy getModuleProxy(final ModuleProxyFactory factory)
  {
    final List<SimpleEFAVariable> variableList = getVariables();
    final List<SimpleEFAComponent> comps = getComponents();
    final SimpleEFAHelper helper = new SimpleEFAHelper(factory);
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
  private final Collection<SimpleEFAEventDecl> mGloballAlphabet;
}
