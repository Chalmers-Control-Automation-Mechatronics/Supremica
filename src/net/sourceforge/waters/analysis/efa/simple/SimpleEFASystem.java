//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.analysis.efa.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.efa.base.AbstractEFASystem;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
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


  public ModuleSubject getModuleProxy(final ModuleSubjectFactory mFactory)
  {
    // TODO Auto-generated method stub
    return null;
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
    for (final int id : component.getEvents()) {
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
      final SimpleEFALabelEncoding trEncoding =
       comp.getTransitionLabelEncoding();
      for (int id = 0; id < trEncoding.size(); id++) {
        final int label = trEncoding.getTransitionLabel(id);
        byte status = EventStatus.STATUS_NONE;
        if (trEncoding.getEventDecl(label).getKind() == EventKind.CONTROLLABLE) {
          status |= EventStatus.STATUS_CONTROLLABLE;
        }
        if (trEncoding.getEventDecl(label).isLocalIn(comp)) {
          status |= EventStatus.STATUS_LOCAL;
        }
        tr.setProperEventStatus(id, status);
      }
    }
  }

  //#########################################################################
  //# Data Members
  private SimpleEFAEventEncoding mEventEncoding;

}
