//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.SchemaBase;
import net.sourceforge.waters.xsd.SchemaDES;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class SAXProductDESImporter
  extends SAXDocumentImporter<ProductDESProxy>
{
  //#########################################################################
  //# Constructors
  public SAXProductDESImporter(final ProductDESProxyFactory factory)
    throws SAXException, ParserConfigurationException
  {
    super("waters-des.xsd", SchemaDES.NUMBER_OF_ELEMENTS);
    mFactory = factory;

    registerHandler(SchemaDES.ELEMENT_Automaton,
      new SAXHandlerCreator<AutomatonProxy>() {
        @Override
        AbstractContentHandler<AutomatonProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new AutomatonProxyHandler(parent);
        }
      });
    registerHandler(SchemaDES.ELEMENT_Event,
      new SAXHandlerCreator<EventProxy>() {
        @Override
        AbstractContentHandler<EventProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new EventProxyHandler(parent);
        }
      });
    registerHandler(SchemaDES.ELEMENT_ProductDES,
      new SAXHandlerCreator<ProductDESProxy>() {
        @Override
        AbstractContentHandler<ProductDESProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new ProductDESProxyHandler(parent);
        }
      });
    registerHandler(SchemaDES.ELEMENT_State,
      new SAXHandlerCreator<StateProxy>() {
        @Override
        AbstractContentHandler<StateProxy> createHandler
          (final AbstractContentHandler<?> parent)
        {
          return new StateProxyHandler(parent);
        }
      });

    mEventRefHandlerCreator = new SAXHandlerCreator<EventProxy>() {
      @Override
      AbstractContentHandler<EventProxy> createHandler
        (final AbstractContentHandler<?> parent)
      {
        return new EventRefHandler(parent);
      }
    };
    mPropositionRefHandlerCreator = new SAXHandlerCreator<EventProxy>() {
      @Override
      AbstractContentHandler<EventProxy> createHandler
        (final AbstractContentHandler<?> parent)
      {
        return new PropositionRefHandler(parent);
      }
    };
    mTransitionHandlerCreator = new SAXHandlerCreator<TransitionProxy>() {
      @Override
      AbstractContentHandler<TransitionProxy> createHandler
        (final AbstractContentHandler<?> parent)
      {
        return new TransitionProxyHandler(parent);
      }
    };
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.marshaller.SAXDocumentImporter<ProductDESProxy>
  @Override
  Class<ProductDESProxy> getDocumentClass()
  {
    return ProductDESProxy.class;
  }


  //#########################################################################
  //# Inner Class AutomatonProxyHandler
  private class AutomatonProxyHandler
    extends NamedProxyHandler<AutomatonProxy>
  {
    //#######################################################################
    //# Constructor
    private AutomatonProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<AutomatonProxyHandler>
    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaDES.ATTRIB_Kind)) {
        mKind = Enum.valueOf(ComponentKind.class, value);
      } else {
        super.setAttribute(localName, value);
      }
    }

    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaDES.ELEMENT_EventRefList)) {
        mEventListHandler = new StaticUniqueListHandler<>(this,
          SchemaDES.ELEMENT_EventRef, mEventRefHandlerCreator);
        pushHandler(mEventListHandler);
      } else if (localName.equals(SchemaDES.ELEMENT_StateList)) {
        mStateListHandler =
          new GenericUniqueListHandler<>(this, StateProxy.class);
        pushHandler(mStateListHandler);
      } else if (localName.equals(SchemaDES.ELEMENT_TransitionList)) {
        mTransitionListHandler = new StaticListHandler<>(this,
          SchemaDES.ELEMENT_Transition, mTransitionHandlerCreator);
        pushHandler(mTransitionListHandler);
      } else if (localName.equals(SchemaBase.ELEMENT_AttributeMap)) {
        mAttributeMapHandler = new AttributeMapHandler(this);
        pushHandler(mAttributeMapHandler);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    AutomatonProxy getResult() throws SAXParseException
    {
      final String name = getName();
      final List<EventProxy> events =
        mEventListHandler == null ? null : mEventListHandler.getResult();
      final List<StateProxy> states =
        mStateListHandler == null ? null : mStateListHandler.getResult();
      final List<TransitionProxy> transitions =
        mTransitionListHandler == null ? null : mTransitionListHandler.getResult();
      final Map<String,String> attribs =
        mAttributeMapHandler == null ? null : mAttributeMapHandler.getResult();
      return mFactory.createAutomatonProxy(name, mKind, events, states,
                                           transitions, attribs);
    }

    //#######################################################################
    //# Parsing Support
    EventProxy getEvent(final String name)
    {
      return mEventListHandler == null ? null : mEventListHandler.get(name);
    }

    StateProxy getState(final String name)
    {
      return mStateListHandler == null ? null : mStateListHandler.get(name);
    }

   //#######################################################################
    //# Data Members
    private ComponentKind mKind;
    private UniqueListHandler<EventProxy> mEventListHandler;
    private UniqueListHandler<StateProxy> mStateListHandler;
    private ListHandler<TransitionProxy> mTransitionListHandler;
    private AttributeMapHandler mAttributeMapHandler;
  }


  //#########################################################################
  //# Inner Class EventProxyHandler
  private class EventProxyHandler
    extends NamedProxyHandler<EventProxy>
  {
    //#######################################################################
    //# Constructor
    private EventProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<EventProxy>
    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaDES.ATTRIB_Kind)) {
        mKind = Enum.valueOf(EventKind.class, value);
      } else if (localName.equals(SchemaDES.ATTRIB_Observable)) {
        mObservable = Boolean.parseBoolean(value);
      } else {
        super.setAttribute(localName, value);
      }
    }

    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaBase.ELEMENT_AttributeMap)) {
        mAttributeMapHandler = new AttributeMapHandler(this);
        pushHandler(mAttributeMapHandler);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    EventProxy getResult() throws SAXParseException
    {
      final String name = getName();
      final Map<String,String> attribs =
        mAttributeMapHandler == null ? null : mAttributeMapHandler.getResult();
      return mFactory.createEventProxy(name, mKind, mObservable, attribs);
    }

    //#######################################################################
    //# Data Members
    private EventKind mKind;
    private boolean mObservable;
    private AttributeMapHandler mAttributeMapHandler;
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
      mAutomatonHandler = parent.getAncestor(AutomatonProxyHandler.class);
      mDESHandler = mAutomatonHandler.getAncestor(ProductDESProxyHandler.class);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<EventProxy>
    @Override
    EventProxy getResult()
      throws SAXParseException
    {
      final String name = getName();
      final EventProxy event = mDESHandler.getEvent(name);
      if (event == null) {
        final String autName = mAutomatonHandler.getName();
        final StringBuilder builder = new StringBuilder();
        if (autName == null) {
          builder.append("An automaton ");
        } else {
          builder.append("The automaton '");
          builder.append(autName);
          builder.append("' ");
        }
        builder.append(" references an undefined event named '");
        builder.append(name);
        builder.append("'.");
        throw createSAXParseException(builder.toString());
      }
      return event;
    }

    //#######################################################################
    //# Data Members
    private final AutomatonProxyHandler mAutomatonHandler;
    private final ProductDESProxyHandler mDESHandler;
  }


  //#########################################################################
  //# Inner Class ProductDESProxyHandler
  private class ProductDESProxyHandler
    extends DocumentProxyHandler<ProductDESProxy>
  {
    //#######################################################################
    //# Constructor
    private ProductDESProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<ProductDESProxy>
    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaDES.ELEMENT_EventList)) {
        mEventListHandler = new GenericUniqueListHandler<>(this, EventProxy.class);
        pushHandler(mEventListHandler);
      } else if (localName.equals(SchemaDES.ELEMENT_AutomataList)) {
        mAutomataListHandler =
          new GenericUniqueListHandler<>(this, AutomatonProxy.class);
        pushHandler(mAutomataListHandler);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    ProductDESProxy getResult() throws SAXParseException
    {
      final String name = getName();
      final String comment = getComment();
      final URI uri = getURI();
      final List<EventProxy> events =
        mEventListHandler == null ? null : mEventListHandler.getResult();
      final List<AutomatonProxy> automata =
        mAutomataListHandler == null ? null : mAutomataListHandler.getResult();
      return mFactory.createProductDESProxy(name, comment, uri,
                                            events, automata);
    }

    //#######################################################################
    //# Parsing Support
    EventProxy getEvent(final String name)
    {
      return mEventListHandler == null ? null : mEventListHandler.get(name);
    }

    //#######################################################################
    //# Data Members
    private UniqueListHandler<EventProxy> mEventListHandler;
    private ListHandler<AutomatonProxy> mAutomataListHandler;
  }


  //#########################################################################
  //# Inner Class PropositionRefHandler
  private class PropositionRefHandler
    extends NamedProxyHandler<EventProxy>
  {
    //#######################################################################
    //# Constructor
    private PropositionRefHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
      mStateHandler = parent.getAncestor(StateProxyHandler.class);
      mAutomatonHandler = mStateHandler.getAncestor(AutomatonProxyHandler.class);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<EventProxy>
    @Override
    EventProxy getResult()
      throws SAXParseException
    {
      final String name = getName();
      final EventProxy event = mAutomatonHandler.getEvent(name);
      if (event == null) {
        final String stateName = mStateHandler.getName();
        final String autName = mAutomatonHandler.getName();
        final StringBuilder builder = new StringBuilder();
        if (autName == null || stateName == null) {
          builder.append("A state ");
        } else {
          builder.append("The state '");
          builder.append(stateName);
          builder.append("' of automaton '");
          builder.append(autName);
          builder.append("' ");
        }
        builder.append(" references an undefined proposition named '");
        builder.append(name);
        builder.append("'.");
        throw createSAXParseException(builder.toString());
      }
      return event;
    }

    //#######################################################################
    //# Data Members
    private final StateProxyHandler mStateHandler;
    private final AutomatonProxyHandler mAutomatonHandler;
  }


  //#########################################################################
  //# Inner Class StateProxyHandler
  private class StateProxyHandler
    extends NamedProxyHandler<StateProxy>
  {
    //#######################################################################
    //# Constructor
    private StateProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<StateProxy>
    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaDES.ATTRIB_Initial)) {
        mInitial = Boolean.parseBoolean(value);
      } else {
        super.setAttribute(localName, value);
      }
    }

    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaDES.ELEMENT_EventRefList)) {
        mPropositionListHandler = new StaticUniqueListHandler<>(this,
          SchemaDES.ELEMENT_EventRef, mPropositionRefHandlerCreator);
        pushHandler(mPropositionListHandler);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    StateProxy getResult() throws SAXParseException
    {
      final String name = getName();
      final List<EventProxy> props = mPropositionListHandler == null ?
        null : mPropositionListHandler.getResult();
      return mFactory.createStateProxy(name, mInitial, props);
    }

    //#######################################################################
    //# Data Members
    private boolean mInitial;
    private ListHandler<EventProxy> mPropositionListHandler;
  }


  //#########################################################################
  //# Inner Class TransitionProxyHandler
  private class TransitionProxyHandler
    extends AbstractContentHandler<TransitionProxy>
  {
    //#######################################################################
    //# Constructor
    private TransitionProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
      mAutomatonHandler = parent.getAncestor(AutomatonProxyHandler.class);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<EventProxy>
    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaDES.ATTRIB_Source)) {
        mSource = findState(value);
      } else if (localName.equals(SchemaDES.ATTRIB_Event)) {
        mEvent = findEvent(value);
      } else if (localName.equals(SchemaDES.ATTRIB_Target)) {
        mTarget = findState(value);
      } else {
        super.setAttribute(localName, value);
      }
    }

    @Override
    TransitionProxy getResult()
    {
      return mFactory.createTransitionProxy(mSource, mEvent, mTarget);
    }

    //#######################################################################
    //# Parsing Support
    private StateProxy findState(final String name)
      throws SAXParseException
    {
      final StateProxy state = mAutomatonHandler.getState(name);
      if (state == null) {
        final StringBuilder builder = new StringBuilder();
        builder.append("A transition");
        final String autName = mAutomatonHandler.getName();
        if (autName != null) {
          builder.append(" in automaton '");
          builder.append(autName);
          builder.append('\'');
        }
        builder.append(" references an undefined state named '");
        builder.append(name);
        builder.append("'.");
        throw createSAXParseException(builder.toString());
      }
      return state;
    }

    private EventProxy findEvent(final String name)
      throws SAXParseException
    {
      final EventProxy event = mAutomatonHandler.getEvent(name);
      if (event == null) {
        final StringBuilder builder = new StringBuilder();
        builder.append("A transition");
        final String autName = mAutomatonHandler.getName();
        if (autName != null) {
          builder.append(" in automaton '");
          builder.append(autName);
          builder.append('\'');
        }
        builder.append(" references an event named '");
        builder.append(name);
        builder.append("' that is not in the automaton alphabet.");
        throw createSAXParseException(builder.toString());
      }
      return event;
    }

    //#######################################################################
    //# Data Members
    private final AutomatonProxyHandler mAutomatonHandler;
    private StateProxy mSource;
    private EventProxy mEvent;
    private StateProxy mTarget;
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;
  private final SAXHandlerCreator<EventProxy> mEventRefHandlerCreator;
  private final SAXHandlerCreator<EventProxy> mPropositionRefHandlerCreator;
  private final SAXHandlerCreator<TransitionProxy> mTransitionHandlerCreator;

}
