//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ProductDESImporter
//###########################################################################
//# $Id: ProductDESImporter.java,v 1.4 2006-09-19 15:53:20 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.waters.model.base.ItemNotFoundException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A converter to translate the Product DES structure ({@link
 * ProductDESProxy}) back into the Module ({@link ModuleProxy}) structure.</P>
 *
 * <P>This converter produces a very simple module without any structure or
 * geometry information. It is intended for graphical display and editing
 * of automata that have been automatically computed by analysis
 * algorithms, or that have been imported from file formats that do not
 * support structure or geometry information.</P>
 *
 * <P>It is possible to convert a full product DES object ({@link
 * ProductDESProxy}) or individual automata ({@link AutomatonProxy}).</P>
 *
 * @author Robi Malik
 * @see net.sourceforge.waters.model.compiler.ModuleCompiler
 */

public class ProductDESImporter
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new product DES importer.
   * This default constructor yiels a converter that does not provide
   * location (file name) information in the modules it produces.
   * @param  factory    The factory used to create the module.
   */
  public ProductDESImporter(final ModuleProxyFactory factory)
  {
    this(factory, null);
  }

  /**
   * Creates a new product DES importer.
   * @param  factory    The factory used to create the module.
   * @param  manager    The document manager environment providing marshallers.
   *                    Used to provide a default extension.
   */
  public ProductDESImporter(final ModuleProxyFactory factory,
                            final DocumentManager manager)
  {
    mFactory = factory;
    mDocumentManager = manager;
  }


  //#########################################################################
  //# Entity Resolving
  /**
   * Gets the document manager used by this importer to resolve
   * references to other files. Presently, this is only used to
   * obtain a default extension.
   */
  public DocumentManager getDocumentManager()
  {
    return mDocumentManager;
  }

  /**
   * Sets a document manager to used by this unmarshaller to resolve
   * references to other files. Presently, this is only used to
   * obtain a default extension.
   */
  public void setDocumentManager(DocumentManager manager)
  {
    mDocumentManager = manager;
  }


  //#########################################################################
  //# Invocation
  /**
   * Converts a product DES ({@link ProductDESProxy}) object to a module
   * ({@link ModuleProxy}) object. If a default extension has been provided
   * to the constructor, the file name of the created module will be
   * constructed from its name and the default extension, and it will be
   * located in the same directory as the original product DES.
   * @param  des        The product DES to be imported.
   * @return A module representing the same content as the given product DES.
   *         Compiling it should produce a result equal to <CODE>des</CODE>.
   */
  public ModuleProxy importModule(final ProductDESProxy des)
  {
    final String name = des.getName();
    final String comment = des.getComment();
    final URI location = getOutputLocation(des);
    final Set<EventProxy> events = des.getEvents();
    final int numevents = events.size();
    final Collection<EventDeclProxy> decls =
      new ArrayList<EventDeclProxy>(numevents);
    for (final EventProxy event : events) {
      final EventDeclProxy decl = importEventDecl(event);
      decls.add(decl);
    }
    final Set<AutomatonProxy> automata = des.getAutomata();
    final int numautomata = automata.size();
    final Collection<SimpleComponentProxy> comps =
      new ArrayList<SimpleComponentProxy>(numautomata);
    for (final AutomatonProxy aut : automata) {
      final SimpleComponentProxy comp = importComponent(aut);
      comps.add(comp);
    }
    return mFactory.createModuleProxy
      (name, comment, location, null, null, decls, null, comps);
  }

  /**
   * Converts an automaton ({@link AutomatonProxy}) object to a component
   * ({@link SimpleComponentProxy}) object.
   * @param  aut          The automaton to be imported.
   * @return A simple component that compiles to the given automaton.
   */
  public SimpleComponentProxy importComponent(final AutomatonProxy aut)
  {
    try {
      mCurrentAutomaton = aut;
      final String name = aut.getName();
      final ComponentKind kind = aut.getKind();
      mCurrentEvents = aut.getEvents();
      mCurrentBlockedEvents = new HashSet<EventProxy>(mCurrentEvents);
      final Set<StateProxy> states = aut.getStates();
      final int numstates = states.size();
      mCurrentNodeMap = new HashMap<StateProxy,SimpleNodeProxy>(numstates);
      for (final StateProxy state : states) {
        final SimpleNodeProxy node = importNode(state);
        mCurrentNodeMap.put(state, node);
      }
      final Collection<TransitionProxy> transitions = aut.getTransitions();
      final int numtrans = transitions.size();
      final Map<NodePair,Set<EventProxy>> transmap =
        new HashMap<NodePair,Set<EventProxy>>(numtrans);
      final Set<StateEventPair> dettest = new HashSet<StateEventPair>();
      boolean deterministic = true;
      for (final TransitionProxy trans : transitions) {
        final StateProxy source = trans.getSource();
        final SimpleNodeProxy sourcenode = getCurrentNode(source);
        final StateProxy target = trans.getTarget();
        final SimpleNodeProxy targetnode = getCurrentNode(target);
        final EventProxy event = trans.getEvent();
        checkEvent(event);
        if (deterministic) {
          final StateEventPair detpair = new StateEventPair(source, event);
          deterministic = dettest.add(detpair);
        }
        final NodePair pair = new NodePair(sourcenode, targetnode);
        final Set<EventProxy> labels = transmap.get(pair);
        if (labels != null) {
          labels.add(event);
        } else {
          final Set<EventProxy> newlabel = new TreeSet<EventProxy>();
          newlabel.add(event);
          transmap.put(pair, newlabel);
        }
      }
      final int numedges = transmap.size();
      final Collection<EdgeProxy> edges = new ArrayList<EdgeProxy>(numedges);
      final Set<Map.Entry<NodePair,Set<EventProxy>>> entries =
        transmap.entrySet();
      for (final Map.Entry<NodePair,Set<EventProxy>> entry : entries) {
        final NodePair pair = entry.getKey();
        final SimpleNodeProxy source = pair.getSource();
        final SimpleNodeProxy target = pair.getTarget();
        final Set<EventProxy> events = entry.getValue();
        final int numevents = events.size();
        final Collection<SimpleIdentifierProxy> labels =
          new ArrayList<SimpleIdentifierProxy>(numevents);
        for (final EventProxy event : events) {
          final SimpleIdentifierProxy label = importEvent(event);
          labels.add(label);
        }
        final LabelBlockProxy labelblock =
          mFactory.createLabelBlockProxy(labels, null);
        final EdgeProxy edge =
          mFactory.createEdgeProxy(source, target, labelblock);
        edges.add(edge);
      }
      final int numblocked = mCurrentBlockedEvents.size();
      final Collection<SimpleIdentifierProxy> blockedlabels =
        new ArrayList<SimpleIdentifierProxy>(numblocked);
      for (final EventProxy event : mCurrentBlockedEvents) {
        final SimpleIdentifierProxy label = importEvent(event);
        blockedlabels.add(label);
      }
      final LabelBlockProxy blockedblock =
        mFactory.createLabelBlockProxy(blockedlabels, null);
      final Collection<SimpleNodeProxy> nodes = mCurrentNodeMap.values();
      final GraphProxy graph =
        mFactory.createGraphProxy(deterministic, blockedblock, nodes, edges);
      final SimpleIdentifierProxy ident =
        mFactory.createSimpleIdentifierProxy(name);
      return mFactory.createSimpleComponentProxy(ident, kind, graph);
    } finally {
      mCurrentAutomaton = null;
      mCurrentEvents = null;
      mCurrentBlockedEvents = null;
      mCurrentNodeMap = null;
    }
  }


  //#########################################################################
  //# Visitor Methods
  private EventDeclProxy importEventDecl(final EventProxy event)
  {
    final String name = event.getName();
    final EventKind kind = event.getKind();
    final boolean observable = event.isObservable();
    return mFactory.createEventDeclProxy(name, kind, observable, null, null);
  }

  private SimpleIdentifierProxy importEvent(final EventProxy event)
  {
    final String name = event.getName();
    return mFactory.createSimpleIdentifierProxy(name);
  }

  private SimpleNodeProxy importNode(final StateProxy state)
  {
    final String name = state.getName();
    final boolean initial = state.isInitial();
    final Collection<EventProxy> props = state.getPropositions();
    final Collection<SimpleIdentifierProxy> idents =
      new TreeSet<SimpleIdentifierProxy>();
    for (final EventProxy prop : props) {
      checkEvent(prop);
      final SimpleIdentifierProxy ident = importEvent(prop);
      idents.add(ident);
    }
    final PlainEventListProxy list =
      mFactory.createPlainEventListProxy(idents);
    return mFactory.createSimpleNodeProxy
      (name, list, initial, null, null, null);
  }


  //#########################################################################
  //# Auxiliary Methods
  private SimpleNodeProxy getCurrentNode(final StateProxy state)
  {
    final SimpleNodeProxy node = mCurrentNodeMap.get(state);
    if (node != null) {
      return node;
    } else {
      throw new ItemNotFoundException
        ("Automaton '" + mCurrentAutomaton.getName() +
         "' does not contain the state named '" + state.getName() + "'!");
    }
  }

  private void checkEvent(final EventProxy event)
  {
    if (mCurrentEvents.contains(event)) {
      mCurrentBlockedEvents.remove(event);
    } else {
      throw new ItemNotFoundException
        ("Automaton '" + mCurrentAutomaton.getName() +
         "' does not contain the event named '" + event.getName() + "'!");
    }
  }

  private URI getOutputLocation(final ProductDESProxy des)
  {
    final URI deslocation = des.getLocation();
    if (deslocation == null) {
      return null;
    }
    if (mDocumentManager == null) {
      return null;
    }
    final ProxyMarshaller<ModuleProxy> marshaller =
      mDocumentManager.findProxyMarshaller(ModuleProxy.class);
    if (marshaller == null) {
      return null;
    }
    final String ext = marshaller.getDefaultExtension();
    final String name = des.getName();
    try {
      return deslocation.resolve(name + ext);
    } catch (final IllegalArgumentException exception) {
      // Bad name, use null, i.e., no file location ...
      return null;
    }
  }


  //#########################################################################
  //# Inner Class NodePair
  private static class NodePair {

    //#######################################################################
    //# Constructors
    private NodePair(final SimpleNodeProxy source,
                     final SimpleNodeProxy target)
    {
      mSource = source;
      mTarget = target;
    }

    //#######################################################################
    //# Simple Access
    private SimpleNodeProxy getSource()
    {
      return mSource;
    }

    private SimpleNodeProxy getTarget()
    {
      return mTarget;
    }

    //#######################################################################
    //# Equals and Hashcode
    public boolean equals(final Object other)
    {
      if (other != null && other.getClass() == getClass()) {
        final NodePair pair = (NodePair) other;
        return mSource.equals(pair.mSource) && mTarget.equals(pair.mTarget);
      } else {
        return false;
      }
    }

    public int hashCode()
    {
      return mSource.hashCode() + 5 * mTarget.hashCode();
    }

    //#######################################################################
    //# Data Members
    private final SimpleNodeProxy mSource;
    private final SimpleNodeProxy mTarget;

  }


  //#########################################################################
  //# Inner Class StateEventPair
  private static class StateEventPair {

    //#######################################################################
    //# Constructors
    private StateEventPair(final StateProxy state, final EventProxy event)
    {
      mState = state;
      mEvent = event;
    }

    //#######################################################################
    //# Simple Access
    private StateProxy getState()
    {
      return mState;
    }

    private EventProxy getEvent()
    {
      return mEvent;
    }

    //#######################################################################
    //# Equals and Hashcode
    public boolean equals(final Object other)
    {
      if (other != null && other.getClass() == getClass()) {
        final StateEventPair pair = (StateEventPair) other;
        return mState.equals(pair.mState) && mEvent.equals(pair.mEvent);
      } else {
        return false;
      }
    }

    public int hashCode()
    {
      return mState.hashCode() + 5 * mEvent.hashCode();
    }

    //#######################################################################
    //# Data Members
    private final StateProxy mState;
    private final EventProxy mEvent;

  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private DocumentManager mDocumentManager;

  private AutomatonProxy mCurrentAutomaton;
  private Set<EventProxy> mCurrentEvents;
  private Set<EventProxy> mCurrentBlockedEvents;
  private Map<StateProxy,SimpleNodeProxy> mCurrentNodeMap;

}
