//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Compiler
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAUnifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import gnu.trove.set.hash.THashSet;

import java.io.IOException;
import java.io.StringWriter;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.AbortableCompiler;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.constraint.ConstraintPropagator;
import net.sourceforge.waters.model.compiler.context.CompiledEnumRange;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfoBuilder;
import net.sourceforge.waters.model.compiler.context.UndefinedIdentifierException;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;


/**
 * <P>A preprocessor for EFA modules.</P>
 *
 * <P>The EFA unifier identifies the overall updates associated with
 * each event and produces a module where each event is associated
 * with a unique update formula shared over all automata. Events are
 * renamed as necessary to achieve this condition.</P>
 *
 * <P><STRONG>Algorithm</STRONG></P>
 *
 * <P>The EFA unifier proceeds in four passes.</P>
 *
 * <OL>
 * <LI>Compute the range of a variables and initialise the constraint
 *     propagator context.</LI>
 * <LI>Collect information about events and associated updates in each
 *     automaton. This information is stored in the map
 *     {@link #mEventMap}.</LI>
 * <LI>Combine updates and generate unique event identifiers.</LI>
 * <LI>Build output module.</LI>
 * </OL>
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public class EFAUnifier extends AbortableCompiler
{

  //#########################################################################
  //# Constructors
  public EFAUnifier(final ModuleProxyFactory factory,
                    final SourceInfoBuilder builder,
                    final ModuleProxy module)
  {
    mFactory = factory;
    mOperatorTable = CompilerOperatorTable.getInstance();
    mSourceInfoBuilder = builder;
    mTrueGuard = new ConstraintList();
    mSimpleExpressionCompiler =
      new SimpleExpressionCompiler(mFactory, mOperatorTable);
    mInputModule = module;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
  }


  //#########################################################################
  //# Invocation
  public ModuleProxy compile()
    throws EvalException
  {
    try {
      mRootContext = new EFAModuleContext(mInputModule);
      // Pass 1 ...
      final Pass1Visitor pass1 = new Pass1Visitor();
      mInputModule.acceptVisitor(pass1);
      // Pass 2 ...
      mPropagator =
        new ConstraintPropagator(mFactory, mOperatorTable, mRootContext);
      mGuardCompiler = new EFAGuardCompiler(mFactory, mOperatorTable);
      final ModuleEqualityVisitor eq =
        ModuleEqualityVisitor.getInstance(false);
      final int size = mInputModule.getEventDeclList().size();
      mEventMap =
        new ProxyAccessorHashMap<IdentifierProxy,EFAEventInfo>(eq,size);
      final Pass2Visitor pass2 = new Pass2Visitor();
      mInputModule.acceptVisitor(pass2);
      // Pass 3 ...
      if (mUsesEventNameBuilder) {
        mEventNameBuilder =
          new EFAEventNameBuilder(mFactory, mOperatorTable, mRootContext);
      }
      mEventUpdateMap = new ProxyAccessorHashMap<>(eq, size);
      for (final EFAEventInfo info : mEventMap.values()) {
        info.combineUpdates();
        info.generateEventNames();
      }
      mEventNameBuilder = null;
      // Pass 4 ...
      final Pass4Visitor pass4 = new Pass4Visitor();
      return pass4.visitModuleProxy(mInputModule);
    } catch (final VisitorException exception) {
      final Throwable cause = exception.getCause();
      if (cause instanceof EvalException) {
        throw (EvalException) cause;
      } else {
        throw exception.getRuntimeException();
      }
    } finally {
      mRootContext = null;
    }
  }


  //#########################################################################
  //# Configuration
  public void setCreatesGuardAutomaton(final boolean create)
  {
    mCreatesGuardAutomaton = create;
  }

  public boolean getCreatesGuardAutomaton()
  {
    return mCreatesGuardAutomaton;
  }

  public void setUsesEventNameBuilder(final boolean use)
  {
    mUsesEventNameBuilder = use;
  }

  public boolean getUsesEventNameBuilder()
  {
    return mUsesEventNameBuilder;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void insertEventInfo(final IdentifierProxy ident,
                               final EFAEventInfo edecl)
    throws DuplicateIdentifierException
  {
    final ProxyAccessor<IdentifierProxy> accessor =
      mEventMap.createAccessor(ident);
    if (mEventMap.containsKey(accessor)) {
      throw new DuplicateIdentifierException(ident, "event");
    } else {
      mEventMap.put(accessor, edecl);
    }
  }

  private EFAEventInfo getEventInfo(final IdentifierProxy ident)
  {
    return mEventMap.getByProxy(ident);
  }

  private EFAEventInfo findEventInfo(final IdentifierProxy ident)
    throws UndefinedIdentifierException
  {
    final EFAEventInfo edecl = getEventInfo(ident);
    if (edecl == null) {
      throw new UndefinedIdentifierException(ident, "event");
    } else {
      return edecl;
    }
  }

  private void addSourceInfo(final Proxy target, final Proxy source)
  {
    if (mSourceInfoBuilder != null) {
      mSourceInfoBuilder.add(target, source);
    }
  }

  private SimpleComponentProxy createGuardAutomaton
    (final List<EventDeclProxy> events)
  {
    if (!mCreatesGuardAutomaton) {
      return null;
    }
    final SimpleNodeProxy node =
      mFactory.createSimpleNodeProxy("init", null, null, true, null, null, null);
    final List<EdgeProxy> edges = new ArrayList<>(mEventUpdateMap.size());
    for (final EventDeclProxy event : events) {
      final IdentifierProxy ident = event.getIdentifier();
      final ConstraintList update = mEventUpdateMap.getByProxy(ident);
      if (update != null && !update.isTrue()) {
        final SimpleExpressionProxy guard =
          update.createExpression(mFactory, mOperatorTable.getAndOperator());
        final List<SimpleExpressionProxy> guards =
          Collections.singletonList(guard);
        final GuardActionBlockProxy ga =
          mFactory.createGuardActionBlockProxy(guards, null, null);
        final List<IdentifierProxy> labels = Collections.singletonList(ident);
        final LabelBlockProxy block =
          mFactory.createLabelBlockProxy(labels, null);
        final EdgeProxy edge =
          mFactory.createEdgeProxy(node, node, block, ga, null, null, null);
        edges.add(edge);
      }
    }
    if (edges.isEmpty()) {
      return null;
    }
    final List<SimpleNodeProxy> nodes = Collections.singletonList(node);
    final GraphProxy graph =
      mFactory.createGraphProxy(true, null, nodes, edges);
    final IdentifierProxy name =
      mFactory.createSimpleIdentifierProxy(":updates");
    return
      mFactory.createSimpleComponentProxy(name, ComponentKind.PLANT, graph);
  }

  public ProxyAccessorMap<IdentifierProxy, ConstraintList> getEventUpdateMap()
  {
    return mEventUpdateMap;
  }

  //#########################################################################
  //# Inner Class Pass1Visitor
  /**
   * The visitor implementing the first pass of EFA compilation. It
   * initialises the variables map {@link #mRootContext} and associates
   * the identifier of each simple or variable component with a {@link
   * EFAVariable} object that contains the range of possible state values
   * of that component.
   */
  private class Pass1Visitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public List<SimpleIdentifierProxy> visitGraphProxy(final GraphProxy graph)
      throws VisitorException
    {
      try {
        final Collection<NodeProxy> nodes = graph.getNodes();
        final int size = nodes.size();
        mCurrentRange = new ArrayList<>(size);
        visitCollection(nodes);
        return mCurrentRange;
      } finally {
        mCurrentRange = null;
      }
    }

    @Override
    public Object visitModuleProxy(final ModuleProxy module)
      throws VisitorException
    {
      final List<Proxy> components = module.getComponentList();
      visitCollection(components);
      return null;
    }

    @Override
    public Object visitNodeProxy(final NodeProxy node)
    {
      return null;
    }

    @Override
    public CompiledRange visitSimpleComponentProxy
      (final SimpleComponentProxy comp)
      throws VisitorException
    {
      try {
        final GraphProxy graph = comp.getGraph();
        final List<SimpleIdentifierProxy> list = visitGraphProxy(graph);
        final CompiledRange range = new CompiledEnumRange(list);
        mRootContext.createVariables(comp, range, mFactory, mOperatorTable);
        return range;
      } catch (final DuplicateIdentifierException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public IdentifierProxy visitSimpleNodeProxy(final SimpleNodeProxy node)
      throws VisitorException
    {
      try {
        checkAbortInVisitor();
        final String name = node.getName();
        final SimpleIdentifierProxy ident =
          mFactory.createSimpleIdentifierProxy(name);
        mRootContext.insertEnumAtom(ident);
        mCurrentRange.add(ident);
        return ident;
      } catch (final DuplicateIdentifierException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public CompiledRange visitVariableComponentProxy
      (final VariableComponentProxy var)
      throws VisitorException
    {
      try {
        final SimpleExpressionProxy expr = var.getType();
        final SimpleExpressionProxy value =
          mSimpleExpressionCompiler.eval(expr, mRootContext);
        final CompiledRange range =
          mSimpleExpressionCompiler.getRangeValue(value);
        mRootContext.createVariables(var, range, mFactory, mOperatorTable);
        return range;
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    //#######################################################################
    //# Data Members
    private List<SimpleIdentifierProxy> mCurrentRange;
  }


  //#########################################################################
  //# Inner Class Pass2Visitor
  /**
   * The visitor implementing the second pass of EFA unification. For
   * each automaton, it collects all the information about events and their
   * relevant updates and stores it in the map {@link EFAUnifier#mEventMap}.
   */
  private class Pass2Visitor extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      try {
        final GuardActionBlockProxy ga = edge.getGuardActionBlock();
        if (ga == null) {
          mCurrentUpdate = mTrueGuard;
        } else {
          visitGuardActionBlockProxy(ga);
        }
        final LabelBlockProxy block = edge.getLabelBlock();
        visitLabelBlockProxy(block);
        return null;
      } finally {
        mCurrentUpdate = null;
      }
    }

    @Override
    public EFAEventInfo visitEventDeclProxy(final EventDeclProxy decl)
      throws VisitorException
    {
      try {
        checkAbortInVisitor();
        final IdentifierProxy ident = decl.getIdentifier();
        final EFAEventInfo edecl = new EFAEventInfo(decl);
        insertEventInfo(ident, edecl);
        return edecl;
      } catch (final DuplicateIdentifierException e) {
        throw wrap(e);
      }
    }

    @Override
    public Object visitGraphProxy(final GraphProxy graph)
      throws VisitorException
    {
      final Collection<EdgeProxy> edges = graph.getEdges();
      visitCollection(edges);
      return null;
    }

    @Override
    public ConstraintList visitGuardActionBlockProxy
      (final GuardActionBlockProxy ga)
      throws VisitorException
    {
      try {
        checkAbortInVisitor();
        mCurrentUpdate = mGuardCompiler.getCompiledGuard(ga);
        return mCurrentUpdate;
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public EFAEventInfo visitIdentifierProxy(final IdentifierProxy ident)
      throws VisitorException
    {
      try {
        checkAbortInVisitor();
        final EFAEventInfo edecl = findEventInfo(ident);
        edecl.addUpdate(mCurrentComponent, mCurrentUpdate);
        return edecl;
      } catch (final UndefinedIdentifierException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public Object visitLabelBlockProxy(final LabelBlockProxy block)
      throws VisitorException
    {
      final List<Proxy> list = block.getEventIdentifierList();
      visitCollection(list);
      return null;
    }

    @Override
    public Object visitModuleProxy(final ModuleProxy module)
      throws VisitorException
    {
      final List<EventDeclProxy> events = module.getEventDeclList();
      visitCollection(events);
      final List<Proxy> components = module.getComponentList();
      visitCollection(components);
      return null;
    }

    @Override
    public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
      throws VisitorException
    {
      try {
        mCurrentComponent = comp;
        final GraphProxy graph = comp.getGraph();
        visitGraphProxy(graph);
        return null;
      } finally {
        mCurrentComponent = null;
      }
    }

    @Override
    public Object visitVariableComponentProxy(final VariableComponentProxy var)
    {
      return null;
    }

    //#######################################################################
    //# Data Members
    private SimpleComponentProxy mCurrentComponent;
    private ConstraintList mCurrentUpdate;
  }


  //#########################################################################
  //# Inner Class Pass4Visitor
  /**
   * The visitor implementing the fourth pass of EFA compilation.
   */
  private class Pass4Visitor extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Constructor
    private Pass4Visitor()
    {
      mCloner = mFactory.getCloner();
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitEventDeclProxy(final EventDeclProxy decl)
      throws VisitorException
    {
      try {
        checkAbortInVisitor();
        final IdentifierProxy ident = decl.getIdentifier();
        final EFAEventInfo info = findEventInfo(ident);
        final EventKind kind = decl.getKind();
        final boolean observable = decl.isObservable();
        final Map<String,String> attribs = decl.getAttributes();
        for (final EFAIdentifier event : info.getEvents()) {
          final IdentifierProxy subClone =
            (IdentifierProxy) mCloner.getClone(event.getIdentifier());
          final EventDeclProxy subDecl = mFactory.createEventDeclProxy
            (subClone, kind, observable, ScopeKind.LOCAL, null, null, attribs);
          mEventDeclarations.add(subDecl);
          addSourceInfo(subDecl, decl);
        }
        return null;
      } catch (final UndefinedIdentifierException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public EdgeProxy visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      try{
        final NodeProxy source0 = edge.getSource();
        final NodeProxy source1 = mNodeMap.get(source0);
        final NodeProxy target0 = edge.getTarget();
        final NodeProxy target1 = mNodeMap.get(target0);
        final GuardActionBlockProxy ga = edge.getGuardActionBlock();
        if (ga == null) {
          mCurrentUpdate = mTrueGuard;
        } else {
          visitGuardActionBlockProxy(ga);
        }
        final LabelBlockProxy block0 = edge.getLabelBlock();
        final LabelBlockProxy block1 = visitLabelBlockProxy(block0);
        final EdgeProxy result = mFactory.createEdgeProxy
          (source1, target1, block1, null, null, null, null);
        mEdgeList.add(result);
        return result;
      } finally {
        mCurrentUpdate = null;
      }
    }

    @Override
    public GraphProxy visitGraphProxy(final GraphProxy graph)
      throws VisitorException
    {
      try {
        final Collection<NodeProxy> nodes = graph.getNodes();
        final int numnodes = nodes.size();
        mNodeList = new ArrayList<>(numnodes);
        mNodeMap = new HashMap<>(numnodes);
        visitCollection(nodes);
        final Collection<EdgeProxy> edges = graph.getEdges();
        final int numedges = edges.size();
        mEdgeList = new ArrayList<>(numedges);
        mUnblockedEvents = new THashSet<>();
        visitCollection(edges);
        final LabelBlockProxy blocked0 = graph.getBlockedEvents();
        LabelBlockProxy blocked1 = null;
        if (blocked0 != null) {
          blocked1 = visitLabelBlockProxy(blocked0);
          if (blocked1.getEventIdentifierList().isEmpty()) {
            blocked1 = null;
          }
        }
        final boolean deterministic = graph.isDeterministic();
        return mFactory.createGraphProxy
          (deterministic, blocked1, mNodeList, mEdgeList);
      } finally {
        mNodeList = null;
        mNodeMap = null;
        mEdgeList = null;
        mUnblockedEvents = null;
      }
    }

    @Override
    public GroupNodeProxy visitGroupNodeProxy(final GroupNodeProxy group)
      throws VisitorException
    {
      final String name = group.getName();
      final PlainEventListProxy props0 = group.getPropositions();
      final PlainEventListProxy props1 =
        (PlainEventListProxy) mCloner.getClone(props0);
      final Map<String,String> attribs0 = group.getAttributes();
      final Map<String,String> attribs1 = new HashMap<>(attribs0);
      final Collection<NodeProxy> children0 = group.getImmediateChildNodes();
      final int numchildren = children0.size();
      final Collection<NodeProxy> children1 =
        new ArrayList<NodeProxy>(numchildren);
      for (final NodeProxy child0 : children0) {
        final NodeProxy child1 = mNodeMap.get(child0);
        children1.add(child1);
      }
      final GroupNodeProxy result =
        mFactory.createGroupNodeProxy(name, props1, attribs1, children1, null);
      mNodeList.add(result);
      mNodeMap.put(group, result);
      addSourceInfo(result, group);
      return result;
    }

    @Override
    public ConstraintList visitGuardActionBlockProxy
      (final GuardActionBlockProxy ga)
      throws VisitorException
    {
      try {
        checkAbortInVisitor();
        mCurrentUpdate = mGuardCompiler.getCompiledGuard(ga);
        return mCurrentUpdate;
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public Object visitIdentifierProxy(final IdentifierProxy ident)
      throws VisitorException
    {
      try {
        checkAbortInVisitor();
        final EFAEventInfo eventInfo = findEventInfo(ident);
        List<EFAIdentifier> events;
        if (mCurrentUpdate == null) {
          events = eventInfo.getEvents();
          if (!mUnblockedEvents.isEmpty()) {
            final List<EFAIdentifier> retained = new ArrayList<>();
            for (final EFAIdentifier event : events) {
              if (!mUnblockedEvents.contains(event)) {
                retained.add(event);
              }
            }
            events = retained;
          }
        } else {
          events = eventInfo.getEvents(mCurrentComponent, mCurrentUpdate);
          mUnblockedEvents.addAll(events);
        }
        for (final EFAIdentifier event : events) {
          final IdentifierProxy subident = event.getIdentifier();
          mLabelList.add(subident);
          addSourceInfo(subident, ident);
        }
        return null;
      } catch(final UndefinedIdentifierException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public LabelBlockProxy visitLabelBlockProxy(final LabelBlockProxy block)
      throws VisitorException
    {
      try {
        mLabelList = new LinkedList<IdentifierProxy>();
        final List<Proxy> list = block.getEventIdentifierList();
        visitCollection(list);
        return mFactory.createLabelBlockProxy(mLabelList, null);
      } finally {
        mLabelList = null;
      }
    }

    @Override
    public ModuleProxy visitModuleProxy(final ModuleProxy module)
      throws VisitorException
    {
      try {
        final String name = module.getName();
        final String comment = module.getComment();
        final List<EventDeclProxy> decls = module.getEventDeclList();
        final int numdecls = decls.size();
        mEventDeclarations = new ArrayList<>(numdecls);
        visitCollection(decls);
        final List<Proxy> components = module.getComponentList();
        final int numcomps = components.size()+(mCreatesGuardAutomaton? 1:0);
        mComponents = new ArrayList<>(numcomps);
        visitCollection(components);
        final SimpleComponentProxy aut = createGuardAutomaton(mEventDeclarations);
        if (aut != null) {
          mComponents.add(aut);
        }
        return mFactory.createModuleProxy
          (name, comment, null, null, mEventDeclarations, null, mComponents);
      } finally {
        mEventDeclarations = null;
        mComponents = null;
      }
    }

    @Override
    public SimpleComponentProxy visitSimpleComponentProxy
      (final SimpleComponentProxy comp)
      throws VisitorException
    {
      try {
        mCurrentComponent = comp;
        final IdentifierProxy ident0 = comp.getIdentifier();
        final IdentifierProxy ident1 =
          (IdentifierProxy) mCloner.getClone(ident0);
        final ComponentKind kind = comp.getKind();
        final GraphProxy graph0 = comp.getGraph();
        final GraphProxy graph1 = visitGraphProxy(graph0);
        final Map<String,String> attribs = comp.getAttributes();
        final SimpleComponentProxy result =
          mFactory.createSimpleComponentProxy(ident1, kind, graph1, attribs);
        addSourceInfo(result, comp);
        mComponents.add(result);
        return result;
      } finally {
        mCurrentComponent = null;
      }
    }

    @Override
    public SimpleNodeProxy visitSimpleNodeProxy(final SimpleNodeProxy node)
      throws VisitorException
    {
      checkAbortInVisitor();
      final String name = node.getName();
      final PlainEventListProxy props0 = node.getPropositions();
      final PlainEventListProxy props1 =
        (PlainEventListProxy) mCloner.getClone(props0);
      final Map<String,String> attribs0 = node.getAttributes();
      final Map<String,String> attribs1 = new HashMap<>(attribs0);
      final boolean initial = node.isInitial();
      final SimpleNodeProxy result = mFactory.createSimpleNodeProxy
        (name, props1, attribs1, initial, null, null, null);
      mNodeList.add(result);
      mNodeMap.put(node, result);
      addSourceInfo(result, node);
      return result;
    }

    @Override
    public VariableComponentProxy visitVariableComponentProxy
      (final VariableComponentProxy comp)
      throws VisitorException
    {
      checkAbortInVisitor();
      final VariableComponentProxy variable =
        (VariableComponentProxy) mCloner.getClone(comp);
      addSourceInfo(variable, comp);
      mComponents.add(variable);
      return variable;
    }

    //#######################################################################
    //# Data Members
    private final ModuleProxyCloner mCloner;

    private List<EventDeclProxy> mEventDeclarations;
    private List<ComponentProxy> mComponents;
    private SimpleComponentProxy mCurrentComponent;
    private ConstraintList mCurrentUpdate;
    private List<NodeProxy> mNodeList;
    private Map<NodeProxy,NodeProxy> mNodeMap;
    private List<EdgeProxy> mEdgeList;
    private List<IdentifierProxy> mLabelList;
    private Set<EFAIdentifier> mUnblockedEvents;
  }


  //#########################################################################
  //# Inner Class EFAEventInfo
  private class EFAEventInfo
  {
    //#######################################################################
    //# Constructor
    private EFAEventInfo(final EventDeclProxy eventDecl)
    {
      mEventDecl = eventDecl;
      mMap = new HashMap<>();
      mList = new ArrayList<>();
      mConstraintMap = new HashMap<>();
      mEventList = new ArrayList<>();
    }

    //#######################################################################
    //# Simple Access
    private void addUpdate(final SimpleComponentProxy automaton,
                           final ConstraintList update)
    {
      EFAUpdateInfo info = mMap.get(automaton);
      if (info == null) {
        info = new EFAUpdateInfo();
        mMap.put(automaton, info);
        mList.add(info);
      }
      info.addUpdate(update);
    }

    private void combineUpdates() throws EvalException
    {
      Collections.sort(mList);
      combineUpdates(mTrueGuard, 0);
    }

    private List<EFAIdentifier> getEvents()
    {
      if (mEventList.size() == 1) {
        final EFAIdentifier event = new EFAIdentifier(mEventDecl, mTrueGuard);
        return Collections.singletonList(event);
      } else {
        return mEventList;
      }
    }

    private List<EFAIdentifier> getEvents(final SimpleComponentProxy comp,
                                         final ConstraintList update)
    {
      final EFAUpdateInfo info = mMap.get(comp);
      final List<EFAIdentifier> identifiers = info.getEvents(update);
      if (mEventList.size() == 1 && !identifiers.isEmpty()) {
        final EFAIdentifier event = new EFAIdentifier(mEventDecl, mTrueGuard);
        return Collections.singletonList(event);
      } else {
        return identifiers;
      }
    }

    //#######################################################################
    //# Data Members
    private List<EFAIdentifier> combineUpdates(final ConstraintList oldUpdate,
                                               final int index)
      throws EvalException
    {
      if (index < mList.size()) {
        final EFAUpdateInfo info = mList.get(index);
        final List<EFAIdentifier> events = new ArrayList<>();
        for (final ConstraintList update : info.getUpdates()) {
          final List<SimpleExpressionProxy> constraints =
            new ArrayList<>(oldUpdate.size() + update.size());
          constraints.addAll(oldUpdate.getConstraints());
          constraints.addAll(update.getConstraints());
          final ConstraintList newUpdate = new ConstraintList(constraints);
          final List<EFAIdentifier> updateIndentifiers =
            combineUpdates(newUpdate, index + 1);
          info.addEvents(update, updateIndentifiers);
          events.addAll(updateIndentifiers);
        }
        return events;
      } else {
        mPropagator.init(oldUpdate);
        mPropagator.propagate();
        if (mPropagator.isUnsatisfiable()) {
          return Collections.emptyList();
        } else {
          final ConstraintList result = mPropagator.getAllConstraints();
          EFAIdentifier event = mConstraintMap.get(result);
          if (event == null) {
            event = new EFAIdentifier(mEventDecl, result);
            mEventList.add(event);
            mConstraintMap.put(result, event);
          }
          return Collections.singletonList(event);
        }
      }
    }

    private void generateEventNames()
    {
      final IdentifierProxy base = mEventDecl.getIdentifier();
      switch (mEventList.size()) {
      case 0:
        break;
      case 1:
        final EFAIdentifier event1 = mEventList.get(0);
        final ConstraintList update1 = event1.getUpdate();
        mEventUpdateMap.putByProxy(base, update1);
        break;
      default:
        if (mEventNameBuilder == null) {
          int index = 0;
          for (final EFAIdentifier event : mEventList) {
            final String name = generateEventName(index);
            final IdentifierProxy ident =
              mFactory.createSimpleIdentifierProxy(name);
            event.setIdentifier(ident);
            final ConstraintList update = event.getUpdate();
            mEventUpdateMap.putByProxy(ident, update);
            index++;
          }
        } else {
          mEventNameBuilder.restart();
          for (final EFAIdentifier event : mEventList) {
            final ConstraintList update = event.getUpdate();
            mEventNameBuilder.addGuard(update);
          }
          for (final EFAIdentifier event : mEventList) {
            final ConstraintList update = event.getUpdate();
            final String suffix = mEventNameBuilder.getNameSuffix(update);
            final IdentifierProxy qualified;
            if (suffix.length() == 0) {
              qualified = base;
            } else {
              final IdentifierProxy comp =
                mFactory.createSimpleIdentifierProxy(suffix);
              qualified = mFactory.createQualifiedIdentifierProxy(base, comp);
            }
            event.setIdentifier(qualified);
            mEventUpdateMap.putByProxy(qualified, update);
          }
          mEventNameBuilder.clear();
        }
        break;
      }
    }

    private String generateEventName(final int index)
    {
      try {
        final StringWriter writer = new StringWriter();
        final IdentifierProxy ident = mEventDecl.getIdentifier();
        if (ident instanceof SimpleIdentifierProxy) {
          final SimpleIdentifierProxy simple = (SimpleIdentifierProxy) ident;
          writer.write(simple.getName());
        } else {
          writer.write('{');
          ProxyPrinter.printProxy(writer, ident);
          writer.write('}');
        }
        writer.write(':');
        writer.write(Integer.toString(index));
        return writer.toString();
      } catch (final IOException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    //#######################################################################
    //# Data Members
    private final EventDeclProxy mEventDecl;
    private final Map<SimpleComponentProxy, EFAUpdateInfo> mMap;
    private final List<EFAUpdateInfo> mList;
    private final Map<ConstraintList,EFAIdentifier> mConstraintMap;
    private final List<EFAIdentifier> mEventList;
  }


  //#########################################################################
  //# Inner Class EFAUpdateInfo
  private static class EFAUpdateInfo
    implements Comparable<EFAUpdateInfo>
  {
    //#######################################################################
    //# Constructor
    private EFAUpdateInfo()
    {
      mUpdates = new ArrayList<>();
      mMap = new HashMap<>();
    }

    //#######################################################################
    //# Simple Access
    private void addUpdate(final ConstraintList update)
    {
      mUpdates.add(update);
    }

    private List<ConstraintList> getUpdates()
    {
      return mUpdates;
    }

    private void addEvents(final ConstraintList update,
                           final Collection<EFAIdentifier> events)
    {
      EFAEventList list = mMap.get(update);
      if (list == null) {
        list = new EFAEventList();
        mMap.put(update, list);
      }
      list.addAll(events);
    }

    private List<EFAIdentifier> getEvents(final ConstraintList update)
    {
      return mMap.get(update);
    }

    //#######################################################################
    //# Interface java.lang.Comparable<EFAUpdateInfo>
    @Override
    public int compareTo(final EFAUpdateInfo info)
    {
      return mUpdates.size() - info.mUpdates.size();
    }

    //#######################################################################
    //# Data Members
    private final List<ConstraintList> mUpdates;
    private final Map<ConstraintList, EFAEventList> mMap;
  }


  //#########################################################################
  //# Inner Class EFAEventList
  /**
   * A list of identifiers that avoids duplicate entries.
   */
  private static class EFAEventList extends AbstractList<EFAIdentifier>
  {
    //#######################################################################
    //# Constructor
    private EFAEventList()
    {
      mList = new ArrayList<>();
      mSet = new THashSet<>();
    }

    private EFAEventList(final int size)
    {
      mList = new ArrayList<>(size);
      mSet = new THashSet<>(size);
    }

    //#######################################################################
    //# Interface java.util.List<IdentifierProxy>
    @Override
    public void add(final int index, final EFAIdentifier event)
    {
      if (mSet.add(event)) {
        mList.add(index, event);
      }
    }

    @Override
    public EFAIdentifier get(final int index)
    {
      return mList.get(index);
    }

    @Override
    public int size()
    {
      return mList.size();
    }

    //#######################################################################
    //# Data Members
    private final List<EFAIdentifier> mList;
    private final Set<EFAIdentifier> mSet;
  }


  //#########################################################################
  //# Inner Class EFAIdentifier
  /**
   * A placeholder for an event identifier to be inserted in label blocks.
   */
  private static class EFAIdentifier {

    //#######################################################################
    //# Constructor
    private EFAIdentifier(final EventDeclProxy decl,
                          final ConstraintList update)
    {
      mIdentifier = decl.getIdentifier();
      mUpdate = update;
    }

    //#######################################################################
    //# Simple Access
    private ConstraintList getUpdate()
    {
      return mUpdate;
    }

    private IdentifierProxy getIdentifier()
    {
      return mIdentifier;
    }

    private void setIdentifier(final IdentifierProxy ident)
    {
      mIdentifier = ident;
    }

    //#######################################################################
    //# Data Members
    private IdentifierProxy mIdentifier;
    private final ConstraintList mUpdate;
  }


  //#########################################################################
  //# Data Members
  private boolean mCreatesGuardAutomaton = false;
  private boolean mUsesEventNameBuilder = false;

  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final SourceInfoBuilder mSourceInfoBuilder;
  private final ConstraintList mTrueGuard;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private EFAEventNameBuilder mEventNameBuilder;

  private final ModuleProxy mInputModule;

  private EFAModuleContext mRootContext;
  private ConstraintPropagator mPropagator;
  private EFAGuardCompiler mGuardCompiler;
  private ProxyAccessorMap<IdentifierProxy,EFAEventInfo> mEventMap;
  private ProxyAccessorMap<IdentifierProxy,ConstraintList> mEventUpdateMap;

}
