//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFASystem
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.efa.base.AbstractEFASystem;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.xsd.base.EventKind;

/**
 * A simple implementation of the abstract system {@link AbstractEFASystem}.
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFASystem
 extends AbstractEFASystem<Integer, SimpleEFAVariable, SimpleEFAComponent, SimpleEFAVariableContext>
{

  public SimpleEFASystem(final String name,
                         final SimpleEFAVariableContext context,
                         final int size)
  {
    super(name, context, size);
    mEventEncoding = new SimpleEFAEventEncoding();
  }

  public SimpleEFASystem(final String name,
                         final SimpleEFAEventEncoding eventEncoding,
                         final List<SimpleEFAComponent> components,
                         final SimpleEFAVariableContext context)
  {
    super(name, new ArrayList<>(context.getVariables()), components, context);
    mEventEncoding = eventEncoding;
  }

  public void setEventEncoding(final SimpleEFAEventEncoding eventEncoding)
  {
    mEventEncoding = eventEncoding;
  }

  public int getNbrComponents()
  {
    return super.getTransitionRelations().size();
  }

  public ModuleSubject getModuleProxy(final ModuleProxyFactory mFactory)
  {
    throw new UnsupportedOperationException
      ("getModuleProxy() not implemented for SimpleEFASystem!");
  }

  public List<SimpleEFAComponent> getComponents()
  {
    return super.getTransitionRelations();
  }

  public void addComponent(final SimpleEFAComponent component) throws AnalysisException
  {
    if (!super.getVariableContext().equals(component.getVariableContext())) {
      throw new AnalysisException(
       "Inconsistency in variable context is detected!");
    }

    if (super.addTransitionRelation(component)) {
      component.addSystem(this);
    }
  }

  public SimpleEFAEventEncoding getEventEncoding()
  {
    return mEventEncoding;
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
    for (final int id : component.getEvents().toArray()) {
      final SimpleEFAEventDecl event = mEventEncoding.getEventDecl(id);
      if (event.isLocalIn(component) && !event.isProposition()) {
        mEventEncoding.removeEventDecl(event);
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
        final int label = trEncoding.getTransitionLabel(id);
        byte status = EventEncoding.STATUS_NONE;
        if (trEncoding.getEventDecl(label).getKind() == EventKind.CONTROLLABLE) {
          status |= EventEncoding.STATUS_CONTROLLABLE;
        }
        if (trEncoding.getEventDecl(label).isLocalIn(comp)) {
          status |= EventEncoding.STATUS_LOCAL;
        }
        tr.setProperEventStatus(id, status);
      }
    }
  }

  //#########################################################################
  //# Data Members
  private SimpleEFAEventEncoding mEventEncoding;

}
