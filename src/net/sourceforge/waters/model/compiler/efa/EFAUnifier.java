//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFACompiler
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
import net.sourceforge.waters.model.base.ProxyAccessorHashSet;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.ProxyAccessorSet;
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
 * <P>The second pass of the compiler.</P>
 *
 * <P>This compiler accepts a module ({@link ModuleProxy}) as input and
 * produces another module as output. It expands all guard/action blocks by
 * partitioning the events, and replaces all variables by simple
 * components. Event arrays, aliases, foreach constructs, and
 * instantiations are not allowed in the input; these should be expanded by
 * a previous call the the module instance compiler ({@link
 * net.sourceforge.waters.model.compiler.instance.ModuleInstanceCompiler
 * ModuleInstanceCompiler}).</P>
 *
 * <P>The EFA compiler ensures that the resultant module only contains
 * nodes of the following types.</P>
 * <UL>
 * <LI>{@link EventDeclProxy}, where only simple events are defined,
 *     i.e., the list of ranges is guaranteed to be empty;</LI>
 * <LI>{@link SimpleComponentProxy};</LI>
 * </UL>
 *
 * <P><STRONG>Algorithm</STRONG></P>
 *
 * <P>The EFA compiler proceeds in four passes.</P>
 *
 * <OL>
 * <LI>Identify all components (simple or variable) and their state
 *     space.</LI>
 * <LI>Collect and normalise guards, and identify the event variable set
 *     for each event.<BR>
 *     The event variable set consists of the set of all variables whose
 *     value may change if an event occurs. It can be computed in two
 *     different ways, depending on the configuration.<BR>
 *     In <CODE>AUTOMATON_ALPHABET</CODE> mode, the event variable set of
 *     an event is the set of all the variables updated in some simple
 *     component using the event.<BR>
 *     In <CODE>EVENT_ALPHABET</CODE> mode, the event variable set of an
 *     event is the set of all the variables updated in some guard/action
 *     block whose edge includes the event.</LI>
 * <LI>Compute event partitionings.</LI>
 * <LI>Build output automata.</LI>
 * </OL>
 *
 * @author Robi Malik
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
    mSourceInfoBuilder = builder;
    mOperatorTable = CompilerOperatorTable.getInstance();
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
      final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
      final int size = mInputModule.getEventDeclList().size();
      mEventMap = new ProxyAccessorHashMap<IdentifierProxy,EFAEventInfo>(eq,size);
      final Pass2Visitor pass2 = new Pass2Visitor();
      mInputModule.acceptVisitor(pass2);
      // Pass 3 ...
      mEventUpdateMap = new ProxyAccessorHashMap<>(eq, size);
      for (final EFAEventInfo info : mEventMap.values()) {
        info.combineUpdates();
      }
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
    final List<EdgeProxy> edges =
      new ArrayList<EdgeProxy>(mEventUpdateMap.size());
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
        mCurrentRange = new ArrayList<SimpleIdentifierProxy>(size);
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
        final EFAEventInfo edecl = findEventInfo(ident);
        final EventKind kind = decl.getKind();
        final boolean observable = decl.isObservable();
        final Map<String,String> attribs = decl.getAttributes();
        for (final IdentifierProxy subIdent : edecl.getIdentifiers()) {
          final IdentifierProxy subClone =
            (IdentifierProxy) mCloner.getClone(subIdent);
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
        mNodeList = new ArrayList<NodeProxy>(numnodes);
        mNodeMap = new HashMap<NodeProxy,NodeProxy>(numnodes);
        visitCollection(nodes);
        final Collection<EdgeProxy> edges = graph.getEdges();
        final int numedges = edges.size();
        mEdgeList = new ArrayList<EdgeProxy>(numedges);
        final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
        mUnblockedIdentifiers = new ProxyAccessorHashSet<>(eq);
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
        mUnblockedIdentifiers = null;
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
      final Map<String,String> attribs1 = new HashMap<String,String>(attribs0);
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
        final List<IdentifierProxy> identifiers;
        if (mCurrentUpdate == null) {
          final List<IdentifierProxy> subIdentifiers = eventInfo.getIdentifiers();
          identifiers = new ArrayList<IdentifierProxy>(subIdentifiers.size());
          for (final IdentifierProxy subIdent : subIdentifiers) {
            if (!mUnblockedIdentifiers.containsProxy(subIdent)) {
              identifiers.add(subIdent);
            }
          }
        } else {
          identifiers = eventInfo.getIdentifiers(mCurrentComponent, mCurrentUpdate);
        }
        for (final IdentifierProxy subident : identifiers) {
          mLabelList.add(subident);
          addSourceInfo(subident, ident);
          mUnblockedIdentifiers.addProxy(subident);
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
      final Map<String,String> attribs1 = new HashMap<String,String>(attribs0);
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
    private ProxyAccessorSet<IdentifierProxy> mUnblockedIdentifiers;
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
      mIdentifierList = new ArrayList<>();
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

    private List<IdentifierProxy> combineUpdates(final ConstraintList oldUpdate,
                                                 final int index)
      throws EvalException
    {
      if (index < mList.size()) {
        final EFAUpdateInfo info = mList.get(index);
        final List<IdentifierProxy> eventIdentifiers = new ArrayList<>();
        for (final ConstraintList update : info.getUpdates()) {
          final List<SimpleExpressionProxy> constraints =
            new ArrayList<>(oldUpdate.size()+update.size());
          constraints.addAll(oldUpdate.getConstraints());
          constraints.addAll(update.getConstraints());
          final ConstraintList newUpdate = new ConstraintList(constraints);
          final List<IdentifierProxy> updateIndentifiers =
            combineUpdates(newUpdate, index + 1);
          info.addEvents(update, updateIndentifiers);
          eventIdentifiers.addAll(updateIndentifiers);
        }
        return eventIdentifiers;
      } else {
        mPropagator.init(oldUpdate);
        mPropagator.propagate();
        if (mPropagator.isUnsatisfiable()) {
          return Collections.emptyList();
        } else {
          final ConstraintList result = mPropagator.getAllConstraints();
          IdentifierProxy ident = mConstraintMap.get(result);
          if (ident == null) {
            final int gen = mConstraintMap.size();
            final String name = generateEventName(gen);
            ident = mFactory.createSimpleIdentifierProxy(name);
            mIdentifierList.add(ident);
            mConstraintMap.put(result, ident);
            mEventUpdateMap.putByProxy(ident, result);
          }
          return Collections.singletonList(ident);
        }
      }
    }

    private List<IdentifierProxy> getIdentifiers()
    {
      if (mIdentifierList.size() == 1) {
        final IdentifierProxy identifier = mEventDecl.getIdentifier();
        return Collections.singletonList(identifier);
      } else {
        return mIdentifierList;
      }
    }

    private List<IdentifierProxy> getIdentifiers(final SimpleComponentProxy comp,
                                                 final ConstraintList update)
    {
      final EFAUpdateInfo info = mMap.get(comp);
      final List<IdentifierProxy> identifiers = info.getIdentifiers(update);
      if (mIdentifierList.size() == 1 && !identifiers.isEmpty()) {
        final IdentifierProxy identifier = mEventDecl.getIdentifier();
        return Collections.singletonList(identifier);
      } else {
        return identifiers;
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
        writer.write(index);
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
    private final Map<ConstraintList, IdentifierProxy> mConstraintMap;
    private final List<IdentifierProxy> mIdentifierList;
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
    @SuppressWarnings("unused")
    private int getNumberOfUpdates()
    {
      return mUpdates.size();
    }

    private void addUpdate(final ConstraintList update)
    {
      mUpdates.add(update);
    }

    private List<ConstraintList> getUpdates()
    {
      return mUpdates;
    }

    private void addEvents(final ConstraintList update,
                           final Collection<IdentifierProxy> events)
    {
      EFAEventList list = mMap.get(update);
      if (list == null) {
        list = new EFAEventList();
        mMap.put(update, list);
      }
      list.addAll(events);
    }

    private List<IdentifierProxy> getIdentifiers(final ConstraintList update)
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
  private static class EFAEventList extends AbstractList<IdentifierProxy>
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
    public void add(final int index, final IdentifierProxy ident)
    {
      if (mSet.add(ident)) {
        mList.add(index, ident);
      }
    }

    @Override
    public IdentifierProxy get(final int index)
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
    private final List<IdentifierProxy> mList;
    private final Set<IdentifierProxy> mSet;
  }


  //#########################################################################
  //# Data Members
  private boolean mCreatesGuardAutomaton = false;

  private final ModuleProxyFactory mFactory;
  private final SourceInfoBuilder mSourceInfoBuilder;
  private final CompilerOperatorTable mOperatorTable;
  private final ConstraintList mTrueGuard;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final ModuleProxy mInputModule;

  private EFAModuleContext mRootContext;
  private ConstraintPropagator mPropagator;
  private EFAGuardCompiler mGuardCompiler;
  private ProxyAccessorMap<IdentifierProxy, EFAEventInfo> mEventMap;
  private ProxyAccessorMap<IdentifierProxy, ConstraintList> mEventUpdateMap;

}
