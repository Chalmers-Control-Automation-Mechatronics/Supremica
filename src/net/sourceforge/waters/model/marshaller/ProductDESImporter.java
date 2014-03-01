//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ProductDESImporter
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import gnu.trove.set.hash.THashSet;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.waters.model.base.ItemNotFoundException;
import net.sourceforge.waters.model.base.ProxyAccessorHashSet;
import net.sourceforge.waters.model.base.ProxyAccessorSet;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;


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
   * This default constructor yields a converter that does not provide
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
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mFactory = factory;
    mExpressionParser = new ExpressionParser(factory, optable);
    mEnumSymbolCollector = new EnumSymbolCollector();
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
  public void setDocumentManager(final DocumentManager manager)
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
    throws ParseException
  {
    try {
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
      final Collection<ConstantAliasProxy> aliases =
        mEnumSymbolCollector.createAliasList();
      return mFactory.createModuleProxy
        (name, comment, location, aliases, decls, null, comps);
    } finally {
      mEnumSymbolCollector.clear();
    }
  }

  /**
   * Converts an automaton ({@link AutomatonProxy}) object to a component
   * ({@link SimpleComponentProxy}) object.
   * @param  aut          The automaton to be imported.
   * @return A simple component that compiles to the given automaton.
   */
  public SimpleComponentProxy importComponent(final AutomatonProxy aut)
    throws ParseException
  {
    try {
      mCurrentAutomaton = aut;
      final String name = aut.getName();
      final ComponentKind kind = aut.getKind();
      mCurrentEvents = aut.getEvents();
      mCurrentBlockedEvents = new HashSet<EventProxy>(mCurrentEvents);
      final Set<StateProxy> states = aut.getStates();
      final int numstates = states.size();
      final Collection<SimpleNodeProxy> nodes =
        new ArrayList<SimpleNodeProxy>(numstates);
      mCurrentNodeMap = new HashMap<StateProxy,SimpleNodeProxy>(numstates);
      for (final StateProxy state : states) {
        final SimpleNodeProxy node = importNode(state);
        nodes.add(node);
        mCurrentNodeMap.put(state, node);
      }
      final Collection<TransitionProxy> transitions = aut.getTransitions();
      final int numtrans = transitions.size();
      final Map<NodePair,Set<EventProxy>> transmap =
        new HashMap<NodePair,Set<EventProxy>>(numtrans);
      final Set<StateEventPair> dettest = new THashSet<StateEventPair>();
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
      final List<NodePair> pairs = new ArrayList<NodePair>(transmap.keySet());
      Collections.sort(pairs);
      for (final NodePair pair : pairs) {
        final SimpleNodeProxy source = pair.getSource();
        final SimpleNodeProxy target = pair.getTarget();
        final Set<EventProxy> events = transmap.get(pair);
        final int numevents = events.size();
        final Collection<IdentifierProxy> labels =
          new ArrayList<IdentifierProxy>(numevents);
        for (final EventProxy event : events) {
          final IdentifierProxy label = importEvent(event);
          labels.add(label);
        }
        final LabelBlockProxy labelblock =
          mFactory.createLabelBlockProxy(labels, null);
        final EdgeProxy edge = mFactory.createEdgeProxy
          (source, target, labelblock, null, null, null, null);
        edges.add(edge);
      }
      final int numblocked = mCurrentBlockedEvents.size();
      final Collection<IdentifierProxy> blockedlabels =
        new ArrayList<IdentifierProxy>(numblocked);
      for (final EventProxy event : mCurrentEvents) {
        if (mCurrentBlockedEvents.contains(event)) {
          final IdentifierProxy label = importEvent(event);
          blockedlabels.add(label);
        }
      }
      final LabelBlockProxy blockedblock =
        blockedlabels.isEmpty() ? null :
        mFactory.createLabelBlockProxy(blockedlabels, null);
      final GraphProxy graph =
        mFactory.createGraphProxy(deterministic, blockedblock,
                                  nodes, edges);
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

  public EventDeclProxy importEventDecl(final EventProxy event)
  throws ParseException
  {
    final IdentifierProxy ident = importEvent(event);
    final EventKind kind = event.getKind();
    final boolean observable = event.isObservable();
    final Map<String,String> attribs = event.getAttributes();
    return mFactory.createEventDeclProxy
      (ident, kind, observable, ScopeKind.LOCAL, null, null, attribs);
  }


  //#########################################################################
  //# Visitor Methods
  private IdentifierProxy importEvent(final EventProxy event)
    throws ParseException
  {
    final String name = event.getName();
    final IdentifierProxy ident = mExpressionParser.parseIdentifier(name);
    mEnumSymbolCollector.collect(ident);
    return ident;
  }

  private SimpleNodeProxy importNode(final StateProxy state)
    throws ParseException
  {

    final String name = state.getName();
    final boolean initial = state.isInitial();
    final Collection<EventProxy> props = state.getPropositions();
    final Collection<IdentifierProxy> idents = new TreeSet<IdentifierProxy>();
    for (final EventProxy prop : props) {
      checkEvent(prop);
      final IdentifierProxy ident = importEvent(prop);
      idents.add(ident);
    }
    final PlainEventListProxy list =
      mFactory.createPlainEventListProxy(idents);
    return mFactory.createSimpleNodeProxy
      (name, list, null, initial, null, null, null);
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
  //# Inner Class EnumSymbolCollector
  private class EnumSymbolCollector extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Constructors
    private EnumSymbolCollector()
    {
      mCloner = mFactory.getCloner();
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      mEnumSymbols = new ProxyAccessorHashSet<>(eq);
    }

    //#######################################################################
    //# Invocation
    private void collect(final IdentifierProxy ident)
    {
      if (!(ident instanceof SimpleIdentifierProxy)) {
        try {
          ident.acceptVisitor(this);
        } catch (final VisitorException exception) {
          throw new WatersRuntimeException(exception);
        }
      }
    }

    private Collection<ConstantAliasProxy> createAliasList()
    {
      if (mEnumSymbols.isEmpty()) {
        return null;
      } else {
        final Collection<SimpleIdentifierProxy> values = mEnumSymbols.values();
        final List<SimpleIdentifierProxy> list =
          new ArrayList<SimpleIdentifierProxy>(values);
        Collections.sort(list);
        final SimpleIdentifierProxy symbols =
          mFactory.createSimpleIdentifierProxy(":symbols");
        final EnumSetExpressionProxy expr =
          mFactory.createEnumSetExpressionProxy(list);
        final ConstantAliasProxy alias =
          mFactory.createConstantAliasProxy(symbols, expr);
        return Collections.singletonList(alias);
      }
    }

    private void clear()
    {
      mEnumSymbols.clear();
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
      throws VisitorException
    {
      for (final SimpleExpressionProxy index : ident.getIndexes()) {
        index.acceptVisitor(this);
      }
      return null;
    }

    @Override
    public Object visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      final IdentifierProxy base = ident.getBaseIdentifier();
      base.acceptVisitor(this);
      final IdentifierProxy comp = ident.getComponentIdentifier();
      return comp.acceptVisitor(this);
    }

    @Override
    public Object visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
    {
      return null;
    }

    @Override
    public Object visitSimpleIdentifierProxy(final SimpleIdentifierProxy ident)
    {
      final SimpleIdentifierProxy cloned =
        (SimpleIdentifierProxy) mCloner.getClone(ident);
      mEnumSymbols.addProxy(cloned);
      return null;
    }

    //#######################################################################
    //# Data Members
    private final ModuleProxyCloner mCloner;
    private final ProxyAccessorSet<SimpleIdentifierProxy> mEnumSymbols;
  }


  //#########################################################################
  //# Inner Class NodePair
  private static class NodePair implements Comparable<NodePair> {

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
    @Override
    public boolean equals(final Object other)
    {
      if (other != null && other.getClass() == getClass()) {
        final NodePair pair = (NodePair) other;
        return mSource.equals(pair.mSource) && mTarget.equals(pair.mTarget);
      } else {
        return false;
      }
    }

    @Override
    public int hashCode()
    {
      return mSource.hashCode() + 5 * mTarget.hashCode();
    }

    //#######################################################################
    //# Interface java.util.Comparable<NodePair>
    @Override
    public int compareTo(final NodePair pair)
    {
      final int result = mSource.compareTo(pair.mSource);
      if (result != 0) {
        return result;
      } else {
        return mTarget.compareTo(pair.mTarget);
      }
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
    @SuppressWarnings("unused")
    private StateProxy getState()
    {
      return mState;
    }

    @SuppressWarnings("unused")
    private EventProxy getEvent()
    {
      return mEvent;
    }

    //#######################################################################
    //# Equals and Hashcode
    @Override
    public boolean equals(final Object other)
    {
      if (other != null && other.getClass() == getClass()) {
        final StateEventPair pair = (StateEventPair) other;
        return mState.equals(pair.mState) && mEvent.equals(pair.mEvent);
      } else {
        return false;
      }
    }

    @Override
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
  private final ExpressionParser mExpressionParser;
  private final EnumSymbolCollector mEnumSymbolCollector;
  private DocumentManager mDocumentManager;

  private AutomatonProxy mCurrentAutomaton;
  private Set<EventProxy> mCurrentEvents;
  private Set<EventProxy> mCurrentBlockedEvents;
  private Map<StateProxy,SimpleNodeProxy> mCurrentNodeMap;

}

