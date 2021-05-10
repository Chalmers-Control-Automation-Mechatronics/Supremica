//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.model.marshaller;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictCounterExampleProxy;
import net.sourceforge.waters.model.des.ConflictKind;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.DualCounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.LoopCounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.SchemaBase;
import net.sourceforge.waters.xsd.SchemaDES;


public class StAXProductDESWriter
  extends StAXDocumentWriter
  implements ProductDESProxyVisitor
{

  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.ProductDESProxyVisitor
  @Override
  public Object visitAutomatonProxy(final AutomatonProxy aut)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaDES.ELEMENT_Automaton);
    writeAttribute(SchemaDES.ATTRIB_Kind, aut.getKind());
    visitNamedProxy(aut);
    writeEventRefList(aut.getEvents());
    writeOptionalList(NAMESPACE, SchemaDES.ELEMENT_StateList,
                      aut.getStates());
    writeOptionalList(NAMESPACE, SchemaDES.ELEMENT_TransitionList,
                      aut.getTransitions());
    writeAttributeMap(aut.getAttributes());
    writeEndElement();
    return null;
  }

  @Override
  public Object visitConflictCounterExampleProxy
    (final ConflictCounterExampleProxy cex)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaDES.ELEMENT_ConflictCounterExample);
    writeAttribute(SchemaDES.ATTRIB_Kind,
                   cex.getKind(), ConflictKind.CONFLICT); // TODO use default
    visitCounterExampleProxy(cex);
    writeEndElement();
    return null;
  }

  @Override
  public Object visitCounterExampleProxy(final CounterExampleProxy cex)
    throws VisitorException
  {
    writeAttribute(SchemaDES.ATTRIB_ProductDES, cex.getProductDES().getName());
    writeDefaultNamespace(SchemaDES.NAMESPACE);
    visitDocumentProxy(cex);
    writeAutomatonRefList(cex.getAutomata());
    writeOptionalList(NAMESPACE, SchemaDES.ELEMENT_TraceList, cex.getTraces());
    return null;
  }

  @Override
  public Object visitDualCounterExampleProxy(final DualCounterExampleProxy cex)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaDES.ELEMENT_DualCounterExample);
    visitCounterExampleProxy(cex);
    writeEndElement();
    return null;
  }

  @Override
  public Object visitEventProxy(final EventProxy event) throws VisitorException
  {
    final Map<String,String> attribs = event.getAttributes();
    final boolean empty = attribs.isEmpty();
    writeStartElement(NAMESPACE, SchemaDES.ELEMENT_Event, empty);
    writeAttribute(SchemaDES.ATTRIB_Kind, event.getKind());
    writeAttribute(SchemaDES.ATTRIB_Observable,
                   event.isObservable(), SchemaDES.DEFAULT_Observable);
    visitNamedProxy(event);
    writeAttributeMap(attribs);
    writeEndElement();
    return null;
  }

  @Override
  public Object visitLoopCounterExampleProxy(final LoopCounterExampleProxy cex)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaDES.ELEMENT_LoopCounterExample);
    visitCounterExampleProxy(cex);
    writeEndElement();
    return null;
  }

  @Override
  public Object visitProductDESProxy(final ProductDESProxy des)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaDES.ELEMENT_ProductDES);
    writeDefaultNamespace(SchemaDES.NAMESPACE);
    visitDocumentProxy(des);
    writeOptionalList(SchemaDES.NAMESPACE, SchemaDES.ELEMENT_EventList,
                      des.getEvents());
    writeOptionalList(SchemaDES.NAMESPACE, SchemaDES.ELEMENT_AutomataList,
                      des.getAutomata());
    writeEndElement();
    return null;
  }

  @Override
  public Object visitSafetyCounterExampleProxy
    (final SafetyCounterExampleProxy cex)
    throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaDES.ELEMENT_SafetyCounterExample);
    visitCounterExampleProxy(cex);
    writeEndElement();
    return null;
  }

  @Override
  public Object visitStateProxy(final StateProxy state)
    throws VisitorException
  {
    final Collection<EventProxy> props = state.getPropositions();
    final boolean empty = props.isEmpty();
    writeStartElement(NAMESPACE, SchemaDES.ELEMENT_State, empty);
    writeAttribute(SchemaDES.ATTRIB_Initial,
                   state.isInitial(), SchemaDES.DEFAULT_Initial);
    visitNamedProxy(state);
    writeEventRefList(props);
    writeEndElement();
    return null;
  }

  @Override
  public Object visitTraceProxy(final TraceProxy trace) throws VisitorException
  {
    writeStartElement(NAMESPACE, SchemaDES.ELEMENT_Trace);
    writeAttribute(SchemaDES.ATTRIB_Name, trace.getName(), "");
    writeAttribute(SchemaDES.ATTRIB_LoopIndex,
                   trace.getLoopIndex(), SchemaDES.DEFAULT_LoopIndex);
    visitProxy(trace);
    if (!isEmptyTrace(trace)) {
      writeOptionalList(NAMESPACE, SchemaDES.ELEMENT_TraceStepList,
                        trace.getTraceSteps());
    }
    writeEndElement();
    return null;
  }

  @Override
  public Object visitTraceStepProxy(final TraceStepProxy step)
    throws VisitorException
  {
    final EventProxy event = step.getEvent();
    if (event != null) {
      writeRef(SchemaDES.ELEMENT_EventRef, event);
    }
    final Map<AutomatonProxy,StateProxy> map = step.getStateMap();
    if (!map.isEmpty()) {
      if (event == null) {
        writeStartElement(NAMESPACE, SchemaDES.ELEMENT_FirstTraceStateTuple);
      } else {
        writeStartElement(NAMESPACE, SchemaDES.ELEMENT_NextTraceStateTuple);
      }
      for (final Map.Entry<AutomatonProxy,StateProxy> entry : map.entrySet()) {
        writeEmptyElement(NAMESPACE, SchemaDES.ELEMENT_TraceState);
        writeAttribute(SchemaDES.ATTRIB_Automaton, entry.getKey().getName());
        final StateProxy state = entry.getValue();
        if (state != null) {
          writeAttribute(SchemaDES.ATTRIB_State, entry.getValue().getName());
        }
        writeEndElement();
      }
      writeEndElement();
    }
    return null;
  }

  @Override
  public Object visitTransitionProxy(final TransitionProxy trans)
    throws VisitorException
  {
    writeEmptyElement(NAMESPACE, SchemaDES.ELEMENT_Transition);
    writeAttribute(SchemaDES.ATTRIB_Source, trans.getSource().getName());
    writeAttribute(SchemaDES.ATTRIB_Event, trans.getEvent().getName());
    writeAttribute(SchemaDES.ATTRIB_Target, trans.getTarget().getName());
    visitProxy(trans);
    writeEndElement();
    return null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void writeAutomatonRefList(final Collection<AutomatonProxy> automata)
    throws VisitorException
  {
    writeRefList(SchemaDES.ELEMENT_AutomatonRefList,
                 SchemaDES.ELEMENT_AutomatonRef,
                 automata);
  }

  private void writeEventRefList(final Collection<EventProxy> events)
    throws VisitorException
  {
    writeRefList(SchemaDES.ELEMENT_EventRefList,
                 SchemaDES.ELEMENT_EventRef,
                 events);
  }

  private void writeRefList(final String listKey,
                            final String itemKey,
                            final Collection<? extends NamedProxy> items)
    throws VisitorException
  {
    if (items != null && !items.isEmpty()) {
      writeStartElement(NAMESPACE, listKey);
      writeNewLine();
      for (final NamedProxy item : items) {
        writeRef(itemKey, item);
      }
      writeEndElement();
    }
  }

  private void writeRef(final String key, final NamedProxy item)
    throws VisitorException
  {
    writeEmptyElement(NAMESPACE, key);
    writeAttribute(SchemaBase.ATTRIB_Name, item.getName());
    writeEndElement();
  }

  private boolean isEmptyTrace(final TraceProxy trace)
  {
    final List<TraceStepProxy> steps = trace.getTraceSteps();
    if (steps.size() > 1) {
      return false;
    } else {
      final TraceStepProxy step = steps.get(0);
      return step.getStateMap().isEmpty();
    }
  }


  //#########################################################################
  //# Class Constants
  private static final String NAMESPACE = SchemaDES.NAMESPACE;

}
