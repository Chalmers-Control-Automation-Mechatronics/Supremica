//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictCounterExampleProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.DualCounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.LoopCounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.xsd.SchemaDES;
import net.sourceforge.waters.xsd.des.ConflictKind;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class SAXCounterExampleImporter
  extends SAXDocumentImporter<CounterExampleProxy>
{
  //#########################################################################
  //# Constructors
  public SAXCounterExampleImporter(final ProductDESProxyFactory factory)
    throws SAXException, ParserConfigurationException
  {
    super("waters-des.xsd", SchemaDES.NUMBER_OF_ELEMENTS);
    mFactory = factory;

    registerHandler(SchemaDES.ELEMENT_ConflictCounterExample,
      new SAXHandlerCreator<ConflictCounterExampleProxy>() {
        @Override
        AbstractContentHandler<ConflictCounterExampleProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new ConflictCounterExampleProxyHandler(parent);
        }
      });
    registerHandler(SchemaDES.ELEMENT_DualCounterExample,
      new SAXHandlerCreator<DualCounterExampleProxy>() {
        @Override
        AbstractContentHandler<DualCounterExampleProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new DualCounterExampleProxyHandler(parent);
        }
      });
    registerHandler(SchemaDES.ELEMENT_LoopCounterExample,
      new SAXHandlerCreator<LoopCounterExampleProxy>() {
        @Override
        AbstractContentHandler<LoopCounterExampleProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new LoopCounterExampleProxyHandler(parent);
        }
      });
    registerHandler(SchemaDES.ELEMENT_SafetyCounterExample,
      new SAXHandlerCreator<SafetyCounterExampleProxy>() {
        @Override
        AbstractContentHandler<SafetyCounterExampleProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new SafetyCounterExampleProxyHandler(parent);
        }
      });

    mAutomatonRefHandlerCreator = new SAXHandlerCreator<AutomatonProxy>() {
      @Override
      AbstractContentHandler<AutomatonProxy> createHandler
        (final AbstractContentHandler<?> parent)
      {
        return new AutomatonRefHandler(parent);
      }
    };
    mTraceHandlerCreator = new SAXHandlerCreator<TraceProxy>() {
      @Override
      AbstractContentHandler<TraceProxy> createHandler
        (final AbstractContentHandler<?> parent)
      {
        return new TraceProxyHandler(parent);
      }
    };
  }


  //#########################################################################
  //# Accessing the Product DES
  /**
   * Sets the product DES corresponding to a counterexample to be unmarshalled.
   * The name of the product DES in the <CODE>.wtra</CODE> file must match the
   * name of the given product DES, and the counterexample automata and events
   * are taken from it.
   */
  void setProductDES(final ProductDESProxy des)
  {
    mProductDES = des;
  }



  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.marshaller.SAXDocumentImporter<CounterExampleProxy>
  @Override
  Class<CounterExampleProxy> getDocumentClass()
  {
    return CounterExampleProxy.class;
  }

  @Override
  void reset()
  {
    super.reset();
    mAutomataMap = null;
  }


  //#########################################################################
  //# Finding Automata, Events, and States
  private AutomatonProxy findAutomaton(final String name)
    throws SAXParseException
  {
    final AutomatonInfo info = findAutomatonInfo(name);
    return info.getAutomaton();
  }

  private AutomatonInfo findAutomatonInfo(final String name)
    throws SAXParseException
  {
    if (mAutomataMap == null) {
      createAutomataMap();
    }
    final AutomatonInfo info = mAutomataMap.get(name);
    if (info == null) {
      final StringBuilder builder = new StringBuilder();
      builder.append("The counterexample references an automaton named '");
      builder.append(name);
      builder.append("', which does not appear in the model '");
      builder.append(mProductDES.getName());
      builder.append("'.");
      throw createSAXParseException(builder.toString());
    }
    return info;
  }

  private void createAutomataMap()
  {
    assert mProductDES != null :
      "Attempting to unmarshal counterexample without product DES!";
    final Collection<AutomatonProxy> automata = mProductDES.getAutomata();
    mAutomataMap = new HashMap<>(automata.size());
    for (final AutomatonProxy aut : automata) {
      final String name = aut.getName();
      final AutomatonInfo info = new AutomatonInfo(aut);
      mAutomataMap.put(name, info);
    }
  }


  private EventProxy findEvent(final String name)
    throws SAXParseException
  {
    if (mEventMap == null) {
      createEventMap();
    }
    final EventProxy event = mEventMap.get(name);
    if (event == null) {
      final StringBuilder builder = new StringBuilder();
      builder.append("The counterexample references an event named '");
      builder.append(name);
      builder.append("', which does not appear in the model '");
      builder.append(mProductDES.getName());
      builder.append("'.");
      throw createSAXParseException(builder.toString());
    }
    return event;
  }

  private void createEventMap()
  {
    assert mProductDES != null :
      "Attempting to unmarshal counterexample without product DES!";
    final Collection<EventProxy> events = mProductDES.getEvents();
    mEventMap = new HashMap<>(events.size());
    for (final EventProxy event : events) {
      final String name = event.getName();
      mEventMap.put(name, event);
    }
  }


  //#########################################################################
  //# Inner Class AutomatonInfo
  private class AutomatonInfo
  {
    //#######################################################################
    //# Constructor
    private AutomatonInfo(final AutomatonProxy aut)
    {
      mAutomaton = aut;
    }

    //#######################################################################
    //# Simple Access
    private AutomatonProxy getAutomaton()
    {
      return mAutomaton;
    }

    private StateProxy findState(final String name)
      throws SAXParseException
    {
      if (mStateMap == null) {
        createStateMap();
      }
      final StateProxy state = mStateMap.get(name);
      if (state == null) {
        final StringBuilder builder = new StringBuilder();
        builder.append("The counterexample references a state named '");
        builder.append(name);
        builder.append("' in automaton '");
        builder.append(mAutomaton.getName());
        builder.append("', which does not appear in the model.");
        throw createSAXParseException(builder.toString());
      }
      return state;
    }

    private void createStateMap()
    {
      final Collection<StateProxy> states = mAutomaton.getStates();
      mStateMap = new HashMap<>(states.size());
      for (final StateProxy state : states) {
        final String name = state.getName();
        mStateMap.put(name, state);
      }
    }

    //#######################################################################
    //# Data Members
    private final AutomatonProxy mAutomaton;
    private Map<String,StateProxy> mStateMap;
  }


  //#########################################################################
  //# Inner Class AutomatonRefHandler
  private class AutomatonRefHandler
    extends NamedProxyHandler<AutomatonProxy>
  {
    //#######################################################################
    //# Constructor
    private AutomatonRefHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<AutomatonProxy>
    @Override
    AutomatonProxy getResult()
      throws SAXParseException
    {
      final String name = getName();
      return findAutomaton(name);
    }
  }


  //#########################################################################
  //# Inner Class ConflictCounterExampleProxyHandler
  private class ConflictCounterExampleProxyHandler
    extends CounterExampleProxyHandler<ConflictCounterExampleProxy>
  {
    //#######################################################################
    //# Constructor
    private ConflictCounterExampleProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<ConflictCounterExampleProxy>
    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaDES.ATTRIB_Kind)) {
        mKind = Enum.valueOf(ConflictKind.class, value);
      } else {
        super.setAttribute(localName, value);
      }
    }

    @Override
    ConflictCounterExampleProxy getResult() throws SAXParseException
    {
      final String name = getName();
      final String comment = getComment();
      final URI uri = getURI();
      final List<AutomatonProxy> automata = getAutomata();
      final TraceProxy trace = getTrace();
      return mFactory.createConflictCounterExampleProxy
        (name, comment, uri, mProductDES, automata, trace, mKind);
    }

    //#######################################################################
    //# Data Members
    private ConflictKind mKind = ConflictKind.CONFLICT;  // TODO use default
  }


  //#########################################################################
  //# Inner Class CounterExampleProxyHandler
  private abstract class CounterExampleProxyHandler<C extends CounterExampleProxy>
    extends DocumentProxyHandler<C>
  {
    //#######################################################################
    //# Constructor
    private CounterExampleProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<?>
    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaDES.ATTRIB_ProductDES)) {
        assert mProductDES != null :
          "Attempting to unmarshal counterexample without product DES!";
        if (!mProductDES.getName().equals(value)) {
          final StringBuilder builder = new StringBuilder();
          builder.append("The model name '");
          builder.append(value);
          builder.append("' in the counterexample file does not match " +
                         "the model name '");
          builder.append(mProductDES.getName());
          builder.append("'.");
          throw createSAXParseException(builder.toString());
        }
      } else {
        super.setAttribute(localName, value);
      }
    }

    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaDES.ELEMENT_AutomatonRefList)) {
        mAutomatonRefListHandler =
          new StaticListHandler<>(this, SchemaDES.ELEMENT_AutomatonRef,
                                  mAutomatonRefHandlerCreator);
        pushHandler(mAutomatonRefListHandler);
      } else if (localName.equals(SchemaDES.ELEMENT_TraceList)) {
        mTraceListHandler =
          new StaticListHandler<>(this, SchemaDES.ELEMENT_Trace,
                                  mTraceHandlerCreator);
        pushHandler(mTraceListHandler);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    //#######################################################################
    //# Parsing Support
    List<AutomatonProxy> getAutomata()
      throws SAXParseException
    {
      if (mAutomatonRefListHandler == null) {
        return null;
      } else {
        return mAutomatonRefListHandler.getResult();
      }
    }

    List<TraceProxy> getTraces()
      throws SAXParseException
    {
      if (mTraceListHandler == null) {
        return null;
      } else {
        return mTraceListHandler.getResult();
      }
    }

    TraceProxy getTrace()
      throws SAXParseException
    {
      final List<TraceProxy> traces = getTraces();
      if (traces != null && !traces.isEmpty()) {
        return traces.get(0);
      }
      throw createSAXParseException
        ("The counterexample does not contain any trace.");
    }

    //#######################################################################
    //# Data Members
    ListHandler<AutomatonProxy> mAutomatonRefListHandler;
    ListHandler<TraceProxy> mTraceListHandler;
  }


  //#########################################################################
  //# Inner Class DualCounterExampleProxyHandler
  private class DualCounterExampleProxyHandler
    extends CounterExampleProxyHandler<DualCounterExampleProxy>
  {
    //#######################################################################
    //# Constructor
    private DualCounterExampleProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<ConflictCounterExampleProxy>
    @Override
    DualCounterExampleProxy getResult() throws SAXParseException
    {
      final String name = getName();
      final String comment = getComment();
      final URI uri = getURI();
      final List<AutomatonProxy> automata = getAutomata();
      final List<TraceProxy> traces = getTraces();
      if (traces == null || traces.size() < 2) {
        throw createSAXParseException
          ("The dual counterexample does not contain two traces.");
      }
      final TraceProxy trace1 = traces.get(0);
      final TraceProxy trace2 = traces.get(1);
      return mFactory.createDualCounterExampleProxy
        (name, comment, uri, mProductDES, automata, trace1, trace2);
    }
  }


  //#########################################################################
  //# Inner Class EventRefHandler
  private class EventRefHandler
    extends NamedProxyHandler<EventProxy>
  {
    //#######################################################################
    //# Constructor
    private EventRefHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<EventProxy>
    @Override
    EventProxy getResult()
      throws SAXParseException
    {
      final String name = getName();
      return findEvent(name);
    }
  }


  //#########################################################################
  //# Inner Class LoopCounterExampleProxyHandler
  private class LoopCounterExampleProxyHandler
    extends CounterExampleProxyHandler<LoopCounterExampleProxy>
  {
    //#######################################################################
    //# Constructor
    private LoopCounterExampleProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<SafetyCounterExampleProxy>
    @Override
    LoopCounterExampleProxy getResult() throws SAXParseException
    {
      final String name = getName();
      final String comment = getComment();
      final URI uri = getURI();
      final List<AutomatonProxy> automata = getAutomata();
      final TraceProxy trace = getTrace();
      return mFactory.createLoopCounterExampleProxy
        (name, comment, uri, mProductDES, automata, trace);
    }
  }


  //#########################################################################
  //# Inner Class SafetyCounterExampleProxyHandler
  private class SafetyCounterExampleProxyHandler
    extends CounterExampleProxyHandler<SafetyCounterExampleProxy>
  {
    //#######################################################################
    //# Constructor
    private SafetyCounterExampleProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<SafetyCounterExampleProxy>
    @Override
    SafetyCounterExampleProxy getResult() throws SAXParseException
    {
      final String name = getName();
      final String comment = getComment();
      final URI uri = getURI();
      final List<AutomatonProxy> automata = getAutomata();
      final TraceProxy trace = getTrace();
      return mFactory.createSafetyCounterExampleProxy
        (name, comment, uri, mProductDES, automata, trace);
    }
  }


  //#########################################################################
  //# Inner Class TraceProxyHandler
  private class TraceProxyHandler
    extends AbstractContentHandler<TraceProxy>
  {
    //#######################################################################
    //# Constructor
    private TraceProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<TraceProxy>
    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaDES.ATTRIB_Name)) {
        mName = value;
      } else if (localName.equals(SchemaDES.ATTRIB_LoopIndex)) {
        mLoopIndex = Integer.parseInt(value);
      } else {
        super.setAttribute(localName, value);
      }
    }

    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaDES.ELEMENT_TraceStepList)) {
        mTraceStepListHandler = new TraceStepListHandler(this);
        pushHandler(mTraceStepListHandler);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    TraceProxy getResult() throws SAXParseException
    {
      final List<TraceStepProxy> steps;
      if (mTraceStepListHandler == null) {
        final TraceStepProxy step = mFactory.createTraceStepProxy(null);
        steps = Collections.singletonList(step);
      } else {
        steps = mTraceStepListHandler.getResult();
      }
      return mFactory.createTraceProxy(mName, steps, mLoopIndex);
    }

    @Override
    void reset()
    {
      mName = "";
      mLoopIndex = SchemaDES.DEFAULT_LoopIndex;
    }

    //#######################################################################
    //# Data Members
    private String mName = "";
    private int mLoopIndex = SchemaDES.DEFAULT_LoopIndex;
    private TraceStepListHandler mTraceStepListHandler = null;
  }


  //#########################################################################
  //# Inner Class TraceStateHandler
  private class TraceStateHandler extends AbstractContentHandler<Object>
  {
    //#######################################################################
    //# Constructor
    private TraceStateHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<?>
    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaDES.ATTRIB_Automaton)) {
        mAutomatonInfo = findAutomatonInfo(value);
      } else if (localName.equals(SchemaDES.ATTRIB_State)) {
        mStateName = value;
      }
    }

    @Override
    Object getResult()
    {
      return null;
    }

    @Override
    void reset()
    {
      mAutomatonInfo = null;
      mStateName = null;
    }

    //#######################################################################
    //# Parsing Support
    void put(final Map<AutomatonProxy,StateProxy> map)
      throws SAXParseException
    {
      final AutomatonProxy aut = mAutomatonInfo.getAutomaton();
      boolean exists;
      if (mStateName == null) {
        exists = map.containsKey(aut);
        if (!exists) {
          map.put(aut, null);
        }
      } else {
        final StateProxy state = mAutomatonInfo.findState(mStateName);
        exists = map.put(aut, state) != null;
      }
      if (exists) {
        final StringBuilder builder = new StringBuilder();
        builder.append("Invalid XML content - " +
                       "multiple states recorded for automaton '");
        builder.append(aut.getName());
        builder.append("'.");
        throw createSAXParseException(builder.toString());
      }
    }

    //#######################################################################
    //# Data Members
    private AutomatonInfo mAutomatonInfo;
    private String mStateName;
  }


  //#########################################################################
  //# Inner Class TraceStateTupleHandler
  private class TraceStateTupleHandler
    extends AbstractContentHandler<Map<AutomatonProxy,StateProxy>>
  {
    //#######################################################################
    //# Constructor
    private TraceStateTupleHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
      mTraceStateHandler = new TraceStateHandler(this);
      mMap = new HashMap<>();
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<TraceProxy>
    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaDES.ELEMENT_TraceState)) {
        pushHandler(mTraceStateHandler, atts);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    public void endSubElement(final AbstractContentHandler<?> subHandler)
      throws SAXParseException
    {
      if (subHandler == mTraceStateHandler) {
        mTraceStateHandler.put(mMap);
        mTraceStateHandler.reset();
      } else {
        super.endSubElement(subHandler);
      }
    }

    @Override
    Map<AutomatonProxy,StateProxy> getResult() throws SAXParseException
    {
      return mMap;
    }

    //#######################################################################
    //# Data Members
    private final TraceStateHandler mTraceStateHandler;
    private final Map<AutomatonProxy,StateProxy> mMap;
  }


  //#########################################################################
  //# Inner Class TraceStepListHandler
  private class TraceStepListHandler
    extends AbstractContentHandler<List<TraceStepProxy>>
  {
    //#######################################################################
    //# Constructor
    private TraceStepListHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<List<TraceStepProxy>>
    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaDES.ELEMENT_EventRef)) {
        addStep();
        if (mEventRefHandler == null) {
          mEventRefHandler = new EventRefHandler(this);
        }
        pushHandler(mEventRefHandler, atts);
      } else if (localName.equals(SchemaDES.ELEMENT_FirstTraceStateTuple) ||
                 localName.equals(SchemaDES.ELEMENT_NextTraceStateTuple)) {
        // New handler each time, no reset
        mTraceStateTupleHandler = new TraceStateTupleHandler(this);
        pushHandler(mTraceStateTupleHandler);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    List<TraceStepProxy> getResult() throws SAXParseException
    {
      addStep();
      return mList;
    }

    //#######################################################################
    //# Auxiliary Methods
    private void addStep()
      throws SAXParseException
    {
      final EventProxy event = mEventRefHandler == null ?
        null : mEventRefHandler.getResult();
      final Map<AutomatonProxy,StateProxy> map = mTraceStateTupleHandler == null ?
        null : mTraceStateTupleHandler.getResult();
      final TraceStepProxy step = mFactory.createTraceStepProxy(event, map);
      mList.add(step);
      mTraceStateTupleHandler = null;
    }

    //#######################################################################
    //# Data Members
    private EventRefHandler mEventRefHandler = null;
    private TraceStateTupleHandler mTraceStateTupleHandler = null;
    List<TraceStepProxy> mList = new ArrayList<>();
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;

  private ProductDESProxy mProductDES = null;
  private Map<String,AutomatonInfo> mAutomataMap;
  private Map<String,EventProxy> mEventMap;

  private final SAXHandlerCreator<AutomatonProxy> mAutomatonRefHandlerCreator;
  private final SAXHandlerCreator<TraceProxy> mTraceHandlerCreator;

}
