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

package net.sourceforge.waters.model.compiler.efsm;

import gnu.trove.set.hash.THashSet;

import java.io.IOException;
import java.io.StringWriter;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
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
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.compiler.context.CompiledEnumRange;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.OccursChecker;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfoCloner;
import net.sourceforge.waters.model.compiler.context.UndefinedIdentifierException;
import net.sourceforge.waters.model.compiler.efa.EFAEventNameBuilder;
import net.sourceforge.waters.model.compiler.efa.EFAGuardCompiler;
import net.sourceforge.waters.model.compiler.efa.EFAModuleContext;
import net.sourceforge.waters.model.compiler.efa.EFAVariable;
import net.sourceforge.waters.model.compiler.efa.EFAVariableCollector;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.ScopeKind;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.printer.ProxyPrinter;


/**
 * <P>The EFSM normalisation algorithm.
 * The EFSM normaliser identifies the overall updates associated with
 * each event and produces a module where each event is associated
 * with a unique update formula shared over all automata.</P>
 *
 * <P>If necessary, events are renamed in order to achieve this condition.</P>
 *
 * <P><STRONG>Algorithm</STRONG></P>
 *
 * <P>The EFSM normaliser proceeds in four passes:</P>
 * <OL>
 * <LI>Compute the range of variables and initialise the constraint
 *     propagator context.</LI>
 * <LI>Collect information about events and associated updates in each
 *     automaton. This information is stored in the map {@link #mEventMap}.
 *     </LI>
 * <LI>Make updates disjoint, combine updates and generate unique event
 *     identifiers.</LI>
 * <LI>Build the output module.</LI>
 * </OL>
 *
 * <P><I>Reference:</I><BR>
 * Sahar Mohajerani, Robi Malik, Martin Fabian. A framework for compositional
 * nonblocking verification of extended finite-state machines. Discrete Event
 * Dynamic Systems, <STRONG>26</STRONG>(1), 33&ndash;84, 2016.</P>
 *
 * @author Sahar Mohajerani, Robi Malik, Roger Su
 */

public class EFSMNormaliser extends AbortableCompiler
{
  //#########################################################################
  //# Constructor
  public EFSMNormaliser(final ModuleProxyFactory factory,
                       final CompilationInfo compilationInfo,
                       final ModuleProxy module)
  {
    mFactory = factory;
    mOperatorTable = CompilerOperatorTable.getInstance();
    final ExpressionComparator comparator =
      new ExpressionComparator(mOperatorTable);
    mComparator = new EFAIdentifierComparator(comparator);
    mCompilationInfo = compilationInfo;
    mSimpleExpressionCompiler =
      new SimpleExpressionCompiler(mFactory, mCompilationInfo, mOperatorTable);
    mInputModule = module;
  }


  //#########################################################################
  //# Invocation
  public ModuleProxy compile() throws EvalException
  {
    try {
      mRootContext = new EFAModuleContext(mInputModule);

      // Pass 1
      final Pass1Visitor pass1 = new Pass1Visitor();
      mInputModule.acceptVisitor(pass1);

      // Pass 2
      mGuardCompiler = new EFAGuardCompiler(mFactory, mOperatorTable);
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      final int size = mInputModule.getEventDeclList().size();
      mEventMap = new ProxyAccessorHashMap<>(eq,size);
      final Pass2Visitor pass2 = new Pass2Visitor();
      mInputModule.acceptVisitor(pass2);

      // Pass 3
      if (mUsesEventNameBuilder) {
        mEventNameBuilder =
          new EFAEventNameBuilder(mFactory, mOperatorTable, mRootContext);
      }
      mEventUpdateMap = new ProxyAccessorHashMap<>(eq, size);
      for (final Map.Entry<ProxyAccessor<IdentifierProxy>,EFAEventInfo> e :
           mEventMap.entrySet()) {
        final IdentifierProxy ident = e.getKey().getProxy();
        final EFAEventInfo info = e.getValue();
        info.makeDisjoint(ident);
        info.combineUpdates();
        info.generateEventNames(mComparator);
      }
      mEventNameBuilder = null;
      // End of Pass 3: Throw any accumulated exceptions
      if (mCompilationInfo.hasExceptions()) {
        throw mCompilationInfo.getExceptions();
      }
      // Pass 4
      final Pass4Visitor pass4 = new Pass4Visitor();
      return pass4.visitModuleProxy(mInputModule);
    }

    catch (final VisitorException exception) {
      final Throwable cause = exception.getCause();
      if (cause instanceof EvalException) {
        throw (EvalException) cause;
      } else {
        throw exception.getRuntimeException();
      }
    }

    finally {
      mRootContext = null;
      mGuardCompiler = null;
      mEventMap = null;
      mEventNameBuilder = null;
    }
  }

  //#########################################################################
  //# Configuration
  public boolean getCreatesGuardAutomaton()
  {
    return mCreatesGuardAutomaton;
  }

  public void setCreatesGuardAutomaton(final boolean create)
  {
    mCreatesGuardAutomaton = create;
  }

  public boolean getUsesEventNameBuilder()
  {
    return mUsesEventNameBuilder;
  }

  public void setUsesEventNameBuilder(final boolean use)
  {
    mUsesEventNameBuilder = use;
  }

  public void setAutomatonVariablesEnabled(final boolean enabled)
  {
    mAutomatonVariablesEnabled = enabled;
  }


  //#########################################################################
  //# Auxiliary Methods
  public ProxyAccessorMap<IdentifierProxy, ConstraintList> getEventUpdateMap()
  {
    return mEventUpdateMap;
  }

  /**
   * Adds to the event map an ordered pair in the form of
   * ({@link IdentifierProxy}, {@link EFAEventInfo}).
   *
   * @param ident   First component of the ordered pair
   * @param edecl   Second component of the ordered pair
   */
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

  /**
   * Obtains the {@link EFAEventInfo} associated with a particular identifier
   * from the event map, assuming that the pair exists.
   *
   * @param ident   The identifier of interest
   * @return        The {@link EFAEventInfo} associated to the identifier
   */
  private EFAEventInfo getEventInfo(final IdentifierProxy ident)
  {
    return mEventMap.getByProxy(ident);
  }

  /**
   * Obtains the {@link EFAEventInfo} associated with a particular identifier
   * from the event map.
   *
   * @param ident   The identifier of interest
   * @return        The {@link EFAEventInfo} associated to the identifier
   */
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

  /**
   * Creates a guard automaton which serves as a table containing all the
   * events and their corresponding unique updates.
   *
   * @param events The list of events to be used
   *
   * @return An automaton which only has one state,
   *         and has all the events with their unique updates as loops.
   */
  private SimpleComponentProxy createGuardAutomaton
                                          (final List<EventDeclProxy> events)
  {
    if (!mCreatesGuardAutomaton) {
      return null;
    }
    final SimpleNodeProxy node =
      mFactory.createSimpleNodeProxy(":init", null, null, true, null, null, null);
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
    final IdentifierProxy ident =
      mFactory.createSimpleIdentifierProxy(EFSMCompiler.NAME_UPDATES);
    return mFactory.createSimpleComponentProxy
      (ident, ComponentKind.PLANT, graph, EFSMCompiler.ATTRIBUTES_UPDATES);
  }


  //#########################################################################
  //# Inner Class: Pass1Visitor
  /**
   * The visitor implementing the first pass of EFA compilation. <p>
   * It initialises the variables map {@link #mRootContext} and associates
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
        if (mAutomatonVariablesEnabled) {
          mCurrentRange = new ArrayList<>(size);
        }
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
    public Object visitSimpleComponentProxy
      (final SimpleComponentProxy comp)
      throws VisitorException
    {
      try {
        final GraphProxy graph = comp.getGraph();
        final List<SimpleIdentifierProxy> list = visitGraphProxy(graph);
        if (mAutomatonVariablesEnabled) {
          final CompiledRange range = new CompiledEnumRange(list);
          mRootContext.createVariables(comp, range, mFactory, mOperatorTable);
        }
        return null;
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
        if (mAutomatonVariablesEnabled) {
          mRootContext.insertEnumAtom(ident);
          mCurrentRange.add(ident);
        }
        return ident;
      } catch (final EvalException exception) {
        exception.replaceLocation(node);
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
  //# Inner Class: Pass2Visitor
  /**
   * The visitor implementing the second pass of EFA unification. <p> For
   * each automaton, it collects all the information about events and their
   * relevant updates and stores it in the map {@link EFSMNormaliser#mEventMap}.
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
        mCurrentGABlock = edge.getGuardActionBlock();
        if (mCurrentGABlock == null) {
          mCurrentUpdate = ConstraintList.TRUE;
        } else {
          visitGuardActionBlockProxy(mCurrentGABlock);
        }
        final LabelBlockProxy block = edge.getLabelBlock();
        visitLabelBlockProxy(block);
        return null;
      } finally {
        mCurrentUpdate = null;
        mCurrentGABlock = null;
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
      final LabelBlockProxy blockedEvents = graph.getBlockedEvents();
      if (blockedEvents != null) {
        visitLabelBlockProxy(blockedEvents);
      }
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
        if (mCurrentUpdate == null) {
          edecl.setBlocked(mCurrentComponent);
        } else {
          edecl.addUpdate(mCurrentComponent, mCurrentUpdate, mCurrentGABlock);
        }
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
    private GuardActionBlockProxy mCurrentGABlock;
    private ConstraintList mCurrentUpdate;
  }


  //#########################################################################
  //# Inner Class: Pass4Visitor
  /**
   * The visitor implementing the fourth pass of EFA compilation.
   * <p>
   * This pass converts the module stored in the local data structures into
   * a {@link ModuleProxy}.
   */
  private class Pass4Visitor extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Constructor
    private Pass4Visitor()
    {
      mCloner = new SourceInfoCloner(mFactory, mCompilationInfo);
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
          mCompilationInfo.add(subDecl, decl);
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
      try {
        final NodeProxy source0 = edge.getSource();
        final NodeProxy source1 = mNodeMap.get(source0);
        final NodeProxy target0 = edge.getTarget();
        final NodeProxy target1 = mNodeMap.get(target0);

        final GuardActionBlockProxy ga = edge.getGuardActionBlock();
        if (ga == null)
          mCurrentUpdate = ConstraintList.TRUE;
        else
          visitGuardActionBlockProxy(ga);

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
        mCurrentEvents = new THashSet<>();
        mCurrentIdentifiers = new THashSet<>();
        visitCollection(edges);

        final LabelBlockProxy blocked = graph.getBlockedEvents();
        if (blocked != null)
          visitLabelBlockProxy(blocked);

        final List<EFAEventInfo> events = new ArrayList<>(mCurrentEvents);
        Collections.sort(events);
        final List<IdentifierProxy> blockedList = new LinkedList<>();
        for (final EFAEventInfo event : events)
          for (final EFAIdentifier ident : event.getEvents())
            if (!mCurrentIdentifiers.contains(ident))
              blockedList.add(ident.getIdentifier());
        final LabelBlockProxy newBlocked =blockedList.isEmpty() ? null :
                           mFactory.createLabelBlockProxy(blockedList, null);

        final boolean deterministic = graph.isDeterministic();
        return mFactory.createGraphProxy
                           (deterministic, newBlocked, mNodeList, mEdgeList);
      } finally {
        mNodeList = null;
        mNodeMap = null;
        mEdgeList = null;
        mCurrentEvents = null;
        mCurrentIdentifiers = null;
      }
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
      try
      {
        checkAbortInVisitor();

        final EFAEventInfo info = findEventInfo(ident);
        mCurrentEvents.add(info);
        if (mCurrentUpdate != null)
        {
          final List<EFAIdentifier> events =
                           info.getEvents(mCurrentComponent, mCurrentUpdate);

          for (final EFAIdentifier event : events)
          {
            IdentifierProxy subIdent = event.getIdentifier();
            subIdent = (IdentifierProxy) mCloner.getClone(subIdent);
            if (!identifierNotDeclared(subIdent))
              mLabelList.add(subIdent);
            mCurrentIdentifiers.add(event);
            mCompilationInfo.add(subIdent, ident);
          }
        }
        return null;
      }

      catch(final UndefinedIdentifierException exception) {
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
        final List<ConstantAliasProxy> aliases =
          module.getConstantAliasList();
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
          (name, comment, null, aliases, mEventDeclarations, null, mComponents);
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
        mCompilationInfo.add(result, comp);
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
      mCompilationInfo.add(result, node);
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
      mCompilationInfo.add(variable, comp);
      mComponents.add(variable);
      return variable;
    }


    //#######################################################################
    // Auxiliary Method
    /**
     * Ensures that an event identifier is declared before adding it to
     * the final output module.
     * <p>
     * New identifiers can be automatically generated during the normalisation
     * process; in such cases, the new identifiers must not be added to the
     * output module, or the next step of the compilation will misbehave.
     *
     * @param ident The event identifier of interest
     *
     * @return <code>true</code> if the identifier given as parameter is not
     *                           declared
     */
    private boolean identifierNotDeclared(final IdentifierProxy ident)
    {
      for (final EventDeclProxy decl : mEventDeclarations)
        if (mEquality.equals(decl.getIdentifier(), ident))
          return false;

      return true;
    }

    //#######################################################################
    //# Data Members

    // Utilities:
    private final ModuleProxyCloner mCloner;
    private final ModuleEqualityVisitor mEquality = new ModuleEqualityVisitor(false);

    // Module Information:
    private List<EventDeclProxy> mEventDeclarations;
    private List<ComponentProxy> mComponents;
    private SimpleComponentProxy mCurrentComponent;
    private Set<EFAEventInfo> mCurrentEvents;
    private Set<EFAIdentifier> mCurrentIdentifiers;
    private ConstraintList mCurrentUpdate;
    private List<NodeProxy> mNodeList;
    private Map<NodeProxy,NodeProxy> mNodeMap;
    private List<EdgeProxy> mEdgeList;
    private List<IdentifierProxy> mLabelList;
  }


  //#########################################################################
  //# Inner Class: EFAEventInfo
  /**
   * Information about an event. <p> It primarily contains a map which
   * projects a {@link SimpleComponentProxy} to an {@link EFAUpdateInfo},
   * i.e. all the events used by a particular automaton.
   */
  private class EFAEventInfo implements Comparable<EFAEventInfo>
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
    //# Interface java.lang.Comparable<EFAEventInfo>
    @Override
    public int compareTo(final EFAEventInfo info)
    {
      return mEventDecl.compareTo(info.mEventDecl);
    }

    //#######################################################################
    //# Simple Access
    private List<EFAIdentifier> getEvents()
    {
      return mEventList;
    }

    private List<EFAIdentifier> getEvents(final SimpleComponentProxy comp,
                                          final ConstraintList update)
    {
      if (isBlocked()) {
        return Collections.emptyList();
      }
      final EFAUpdateInfo info = mMap.get(comp);
      final List<EFAIdentifier> identifiers = info.getEvents(update);
      if (identifiers == null) {
        return Collections.emptyList();
      } else {
        return identifiers;
      }
    }

    /**
     * Returns whether this event is blocked. <p>
     * Blocked events are events known to be always disabled by the plant. <p>
     * These events are removed from the model entirely. <p>
     */
    private boolean isBlocked()
    {
      return mList == null;
    }

    /**
     * Marks this event as blocked.
     */
    private void setBlocked()
    {
      mMap = null;
      mList = null;
      mConstraintMap = null;
      mEventList = Collections.emptyList();
    }

    /**
     * Records a transition.
     *
     * @param  comp       The automaton containing the transition.
     * @param  update     The update associated with the transition.
     */
    private void addUpdate(final SimpleComponentProxy comp,
                           final ConstraintList update,
                           final GuardActionBlockProxy block)
    {
      if (!isBlocked()) {
        EFAUpdateInfo info = mMap.get(comp);
        if (info == null) {
          info = new EFAUpdateInfo(comp);
          mMap.put(comp, info);
          mList.add(info);
        }
        info.addUpdate(update, block);
      }
    }

    /**
     * Records a blocked events list entry.
     * <P>
     * This method is to be called after all regular transitions have been
     * added using {@link #addUpdate(SimpleComponentProxy, ConstraintList,
     * GuardActionBlockProxy) addUpdate()}.
     * <P>
     * If this method is called with another update already recorded for
     * the given automaton, it has no effect. Otherwise, the effect of
     * blocked events depends on the component kind.
     * <p>
     * If an event only occurs in a blocked events list of a plant, then it
     * is marked as blocked&nbsp;- effectively removing it from the model.
     * If an event occurs in a blocked events list of some other type of
     * component, an update associated with a true guard is recorded for
     * the automaton.
     *
     * @param  comp       The automaton containing the blocked events list.
     */
    private void setBlocked(final SimpleComponentProxy comp)
    {
      if (mMap != null && !mMap.containsKey(comp)) {
        if (mEventDecl.getKind() != EventKind.PROPOSITION &&
            comp.getKind() == ComponentKind.PLANT) {
          setBlocked();
        } else {
          addUpdate(comp, ConstraintList.TRUE, null);
        }
      }
    }

    //#######################################################################
    //# Pass 3
    /**
     * Ensures that all guards of one component are disjoint, and this
     * process is performed separately for different automata.
     */
    private void makeDisjoint(final IdentifierProxy ident) throws EvalException
    {
      // If the event is blocked, do nothing.
      if (!isBlocked()) {
        // Perform the disjoint operation for separate automata.
        for (final EFAUpdateInfo update : mList) {
          update.makeDisjoint(mEventDecl, ident);
        }
      }
    }

    /**
     * Computes all combinations of the guards in different automata.
     * This base method uses another recursive method of the same name.
     */
    private void combineUpdates() throws EvalException
    {
      if (!isBlocked()) {
        Collections.sort(mList);
        final ConstraintPropagator propagator =
                      new ConstraintPropagator(mFactory, mCompilationInfo,
                                               mOperatorTable, mRootContext);
        final EFAUpdateInfo[] second = new EFAUpdateInfo[mList.size()];
        for (int i = 0; i < second.length; i++) {
          second[i] = new EFAUpdateInfo(null);
          /* Because the component reference of each temporary
           * EFAUpdateInfo in the array 'second' is not used at
           * all, it is safe to pass 'null' as the parameter.
           */
        }
        combineUpdates(0, propagator, second);
        for (int i = 0; i < second.length; i++)
          mList.get(i).combineMaps(second[i]);
      }
    }

    /**
     * Generates elegant names for each {@link EFAIdentifier} of
     * {@link #mEventList}.
     */
    private void generateEventNames(final Comparator<EFAIdentifier> comparator)
    {
      if (isBlocked()) return;

      final IdentifierProxy base = mEventDecl.getIdentifier();

      switch (mEventList.size())
      {
        case 0: break;

        case 1:
          final EFAIdentifier event1 = mEventList.get(0);
          final ConstraintList update1 = event1.getUpdate();
          mEventUpdateMap.putByProxy(base, update1);
          break;

        default:
          if (mEventNameBuilder == null) {
            Collections.sort(mEventList, comparator);
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


    //#######################################################################
    //# Auxiliary Methods
    /**
     * Computes all combinations of the guards in different automata.
     * This is the main recursive part of the algorithm.
     * @param index      The index of the component
     * @param propagator Constraint propagator used for symbolic computation.
     * @param second     An array that contains the intermediate
     *                   {@link EFAUpdateInfo} which will later be
     *                   merged with the original ones.
     */
    private List<EFAIdentifier> combineUpdates(final int index,
                                               final ConstraintPropagator propagator,
                                               final EFAUpdateInfo[] second)
      throws EvalException
    {
      if (index < mList.size()) {
        final List<EFAIdentifier> events = new ArrayList<>();
        for (final ConstraintList update : mList.get(index).getUpdates()) {
          if (update != null) {
            final ConstraintPropagator subPropagator =
              new ConstraintPropagator(propagator);
            if (!update.isTrue()) {
              subPropagator.addConstraints(update);
              subPropagator.propagate();
            }
            if (!subPropagator.isUnsatisfiable()) {
              final List<EFAIdentifier> identifiers =
                combineUpdates(index + 1, subPropagator, second);
              if (!identifiers.isEmpty()) {
                second[index].addEvents(update, identifiers);
                events.addAll(identifiers);
              }
            }
          }
        }
        return events;
      } else { // Base case of the recursion
        propagator.removeUnchangedVariables();
        final ConstraintList constraints = propagator.getAllConstraints();
        EFAIdentifier event = mConstraintMap.get(constraints);
        if (event == null) {
          event = new EFAIdentifier(mEventDecl, constraints);
          mEventList.add(event);
          mConstraintMap.put(constraints, event);
        }
        return Collections.singletonList(event);
      }
    }

    /**
     * Auxiliary method for generating event names.
     */
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
    //# Debugging
    @Override
    public String toString()
    {
      return mEventDecl.getName();
    }

    //#######################################################################
    //# Data Members
    private final EventDeclProxy mEventDecl;
    private Map<SimpleComponentProxy,EFAUpdateInfo> mMap;
    private List<EFAUpdateInfo> mList;
    private Map<ConstraintList,EFAIdentifier> mConstraintMap;
    private List<EFAIdentifier> mEventList;
  }


  //#########################################################################
  //# Inner Class: EFAUpdateInfo
  /**
   * A record containing information on a given event in a given automaton.
   * <p> It contains the updates that together with the event appear in the
   * automaton, and associates them with the combined updates in the model.
   */
  private class EFAUpdateInfo
    implements Comparable<EFAUpdateInfo>
  {
    //#######################################################################
    //# Constructor
    private EFAUpdateInfo(final SimpleComponentProxy comp)
    {
      mComponent = comp;
      mUpdates = new ArrayList<>();
      mUpdateMap = new HashMap<>();
      mGABlockMap = new HashMap<>();
    }

    //#######################################################################
    //# Simple Access
    private void addUpdate(final ConstraintList update,
                           final GuardActionBlockProxy gaBlock)
    {
      if (!mGABlockMap.containsKey(update)) {
        mGABlockMap.put(update, gaBlock);
        mUpdates.add(update);
      }
    }

    private List<ConstraintList> getUpdates()
    {
      return mUpdates;
    }

    private void addEvents(final ConstraintList update,
                           final Collection<EFAIdentifier> events)
    {
      EFAEventList list = mUpdateMap.get(update);
      if (list == null) {
        list = new EFAEventList();
        mUpdateMap.put(update, list);
      }
      list.addAll(events);
    }

    private List<EFAIdentifier> getEvents(final ConstraintList update)
    {
      return mUpdateMap.get(update);
    }

    private Map<ConstraintList,EFAEventList> getMap()
    {
      return mUpdateMap;
    }


    //#######################################################################
    //# Auxiliary Methods
    /**
     * Initialises the map by putting in pairs of original updates and empty
     * event lists.
     */
    private void initialiseBlankMap()
    {
      for (final ConstraintList update : mUpdates)
        mUpdateMap.put(update, new EFAEventList());
    }

    /**
     * If the disjoint operation is not carried out, then the map should be
     * set to its primitive state, i.e. each original update maps to itself.
     */
    private void initialiseOriginalMap(final EventDeclProxy event)
    {
      for (final ConstraintList update : mUpdateMap.keySet())
        mUpdateMap.get(update).add(new EFAIdentifier(event, update));
    }


    //#######################################################################
    //# Interface java.lang.Comparable<EFAUpdateInfo>
    @Override
    public int compareTo(final EFAUpdateInfo info)
    {
      return mUpdates.size() - info.mUpdates.size();
    }


    //#######################################################################
    //# Pass 3
    /**
     * Merges two {@link EFAUpdateInfo}s. The algorithm is best described
     * with an example.
     *
     * <p><STRONG>Example:</STRONG></p>
     *
     * Original current {@link EFAUpdateInfo}:
     * <UL>
     * <LI>Map: {a={x,y}, b={z}}</LI>
     * <LI>List: {x,y,z}</LI>
     * </UL>
     * Original other {@link EFAUpdateInfo}:
     * <UL>
     * <LI>Map: {x={x1,x2}, y={y1}, z={z1,z2,z3}}</LI>
     * <LI>List: {}</LI>
     * </UL>
     * This algorithm modifies the original current {@link EFAUpdateInfo}
     * and changes its fields into:
     * <UL>
     * <LI>Map: {a={x1,x2,y1}, b={z1,z2,z3}}</LI>
     * <LI>List: {x1,x2,y1,z1,z2,z3}</LI>
     * </UL>
     * <p>
     *
     * @param other The other {@link EFAUpdateInfo}
     */
    private void combineMaps(final EFAUpdateInfo other)
    {
      final EFAEventList newList = new EFAEventList();
      final Map<ConstraintList, EFAEventList> newMap = new HashMap<>();

      // For each entry 'x' of the original EFAUpdateInfo,
      for (final Map.Entry<ConstraintList, EFAEventList> entry :
           mUpdateMap.entrySet()) {
        final ConstraintList key = entry.getKey();
        final EFAEventList value0 = entry.getValue();

        // The new list that will be mapped from 'x'.
        final EFAEventList value1 = new EFAEventList();

        // For each element 'y' of the EFAEventList mapped from 'x',
        for (final EFAIdentifier y : value0) {
          // For each entry 'z' of the other map,
          for (final Map.Entry<ConstraintList, EFAEventList> z :
               other.getMap().entrySet()) {
            if (z.getKey().equals(y.getUpdate())) {
              value1.addAll(z.getValue());  // Add to the new map's list.
              newList.addAll(z.getValue()); // Add to the overall list.
            }
          }
        }
        newMap.put(key, value1); // Update the new map.
      }

      // Reset the list of updates.
      mUpdates = new ArrayList<>();
      for (final EFAIdentifier ident : newList)
        mUpdates.add(ident.getUpdate());
      mUpdates.addAll(mCaughtGuards);

      // Reset the map.
      mUpdateMap = newMap;
    }

    /**
     * Decides whether the disjoint operation should be performed, and
     * manipulates the data structure accordingly.
     * @param event The {@link EventDeclProxy} of interest.
     * @param ident This is required for location identification
     *              if an exception is thrown.
     */
    private void makeDisjoint(final EventDeclProxy event,
                              final IdentifierProxy ident)
      throws EvalException
    {
      initialiseBlankMap();
      // Decide whether the disjoint operation should be performed.
      if (!shouldMakeDisjoint(event, ident)) {
        initialiseOriginalMap(event);
      } else {
        // Carry out the disjoint operation.
        final ListIterator<ConstraintList> iter = mUpdates.listIterator();
        final List<ConstraintList> selected = new ArrayList<>(mUpdates.size());
        final List<ConstraintList> result = new LinkedList<>();
        final ConstraintPropagator propagator = new ConstraintPropagator
          (mFactory, mCompilationInfo, mOperatorTable, mRootContext);
        makeDisjoint(iter, selected, result, propagator, event);
        mUpdates = result;
      }
    }

    /**
     * Makes the updates of one particular event disjoint by computing all
     * of their combinations and assigning each of them to the appropriate
     * original edges.
     *
     * @param iter
     *               The iterator for the list of original updates
     * @param selected
     *               The list of updates selected in one specific combination
     * @param result
     *               The final list containing all the recently created
     *               disjoint updates
     * @param parent
     *               The constraint propagator
     * @param event
     *               The event to which this operation is associated
     */
    private void makeDisjoint(final ListIterator<ConstraintList> iter,
                              final List<ConstraintList> selected,
                              final List<ConstraintList> result,
                              final ConstraintPropagator parent,
                              final EventDeclProxy event)
      throws EvalException
    {
      if (iter.hasNext()) {
        // First handle the positive current literal.
        final ConstraintList guard = iter.next();
        ConstraintPropagator propagator = new ConstraintPropagator(parent);
        propagator.addConstraints(guard);
        propagator.propagate();
        if (!propagator.isUnsatisfiable()) {
          final int end = selected.size();
          selected.add(guard);
          makeDisjoint(iter, selected, result, propagator, event);
          selected.remove(end);
        }
        // Then handle the negated current literal.
        propagator = new ConstraintPropagator(parent);
        propagator.addNegation(guard);
        propagator.propagate();
        if (!propagator.isUnsatisfiable()) {
          makeDisjoint(iter, selected, result, propagator, event);
        }
        iter.previous();
      } else {
        // Base case of the recursion
        final ConstraintList newUpdate = parent.getAllConstraints(false);
        final EFAIdentifier newId = new EFAIdentifier(event, newUpdate);
        for (final ConstraintList literal : selected) {
          final Collection<EFAIdentifier> newIdC = new ArrayList<>(1);
          newIdC.add(newId);
          addEvents(literal, newIdC);
          result.add(newUpdate);
        }
        if (selected.isEmpty()) {
          result.add(newUpdate);
          mCaughtGuards.add(newUpdate);
        }
      }
    }

    /**
     * Determines whether the disjoint operation should be carried for an
     * event in a particular automaton. The disjoint operation should be
     * carried out only in the following cases:</P>
     * <OL>
     * <LI><UL><LI>Automaton Type: SPECIFICATION or SUPERVISOR</LI>
     *         <LI>Event Type: UNCONTROLLABLE</LI>
     *         <LI>Condition: No primed variables</LI>
     *         </UL>
     * <LI><UL><LI>Automaton Type: PROPERTY</LI>
     *         <LI>Event Type: CONTROLLABLE or UNCONTROLLABLE</LI>
     *         <LI>Condition: No primed variables</LI>
     *         </UL>
     * </OL>
     * If the type of automaton and event matches one of the above conditions,
     * but the associated updates contain prime variables, then exceptions
     * would be thrown.
     * @param  event The {@link EventDeclProxy} of interest.
     * @param  eventIdent This is required for location identification
     *               if an exception is thrown.
     * @return <code>true</code> if the disjoint operation should be performed;
     *         <code>false</code> otherwise.
     */
    private boolean shouldMakeDisjoint(final EventDeclProxy event,
                                       final IdentifierProxy eventIdent)
      throws EvalException
    {
      final EventKind eKind = event.getKind();
      final ComponentKind cKind = mComponent.getKind();

      if (cKind == ComponentKind.PLANT || eKind == EventKind.PROPOSITION) {
        return false;
      }
      if (cKind == ComponentKind.PROPERTY || eKind == EventKind.UNCONTROLLABLE) {
        final EFAVariableCollector collector =
          new EFAVariableCollector(mOperatorTable, mRootContext);
        for (final Map.Entry<ConstraintList, GuardActionBlockProxy> entry :
             mGABlockMap.entrySet()) {
          final ConstraintList update = entry.getKey();
          final GuardActionBlockProxy gaBlock = entry.getValue();
          final Set<EFAVariable> primedVariables = new THashSet<>();
          collector.collectPrimedVariables(update, primedVariables);
          if (!primedVariables.isEmpty()) {
            // The variable appears primed in the update: we must raise an
            // EFSMControllabilityException. Let us raise one exception for
            // each primed variable ...
            final OccursChecker occursChecker = new OccursChecker();
            final ModuleEqualityVisitor eq = occursChecker.getEquality();
            final UnaryOperator prime = mOperatorTable.getNextOperator();
            for (final EFAVariable var : primedVariables) {
              final SimpleExpressionProxy varName = var.getVariableName();
              // First check if the variable appears primed in the guard ...
              final UnaryExpressionProxy varNamePrimed =
                mFactory.createUnaryExpressionProxy(prime, varName);
              final List<SimpleExpressionProxy> guards = gaBlock.getGuards();
              if (guards != null && !guards.isEmpty()) {
                final SimpleExpressionProxy guard = guards.get(0);
                if (occursChecker.occurs(varNamePrimed, guard)) {
                  final Proxy location = mCompilationInfo.getErrorLocation(guard);
                  final EFSMControllabilityException exception =
                    new EFSMControllabilityException(mComponent, var, eventIdent, location);
                  mCompilationInfo.raise(exception);
                  continue;
                }
              }
              // Not in the guard - search for an action assigning to it ...
              for (final BinaryExpressionProxy action : gaBlock.getActions()) {
                final SimpleExpressionProxy lhs = action.getLeft();
                if (eq.equals(varName, lhs)) {
                  final Proxy location = mCompilationInfo.getErrorLocation(action);
                  final EFSMControllabilityException exception =
                    new EFSMControllabilityException(mComponent, var, eventIdent, location);
                  mCompilationInfo.raise(exception);
                  break;
                }
              }
            }
            return false;
          }
        }
        return true;
      }
      return false;
    }

    //#######################################################################
    //# Data Members
    /**
     * The reference to the component (automata) associated with this
     * instance of EFAUpdateInfo.
     */
    private final SimpleComponentProxy mComponent;

    /**
     * The list containing the most current updates.
     */
    private List<ConstraintList> mUpdates;

    /**
     * A map from the original updates to the new events.
     */
    private Map<ConstraintList,EFAEventList> mUpdateMap;

    /**
     * A map from each original update to the first guard action block from
     * which the update originates.
     */
    private final Map<ConstraintList,GuardActionBlockProxy> mGABlockMap;

    /**
     * A list that contains the additional complementary guards which are not
     * associated to any transition.
     */
    private final List<ConstraintList> mCaughtGuards = new ArrayList<>();
  }


  //#########################################################################
  //# Inner Class: EFAEventList
  /**
   * A list of identifiers that avoids duplicate entries.
   */
  private class EFAEventList extends AbstractList<EFAIdentifier>
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
  //# Inner Class: EFAIdentifier
  /**
   * A placeholder for an event identifier to be inserted in label blocks.
   */
  private class EFAIdentifier
  {
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
    //# Debugging
    @Override
    public String toString()
    {
      return mIdentifier.toString() + mUpdate.toString();
    }

    //#######################################################################
    //# Data Members
    private IdentifierProxy mIdentifier;
    private final ConstraintList mUpdate;
  }


  //#########################################################################
  //# Inner Class: EFAIdentifierComparator
  /**
   * An implementation of the {@link Comparator} interface, used to compare
   * {@link EFAIdentifier} objects based on an expression ordering of the
   * elements of the update's {@link ConstraintList}.
   *
   * @see ExpressionComparator
   */
  private class EFAIdentifierComparator
    implements Comparator<EFAIdentifier>
  {
    //#######################################################################
    //# Constructor
    private EFAIdentifierComparator(final ExpressionComparator inner)
    {
      mExpressionComparator = inner;
    }

    //#######################################################################
    //# Interface java.util.Comparator
    @Override
    public int compare(final EFAIdentifier ident1,
                       final EFAIdentifier ident2)
    {
      final ConstraintList update1 = ident1.getUpdate();
      final int len1 = update1.size();
      final ConstraintList update2 = ident2.getUpdate();
      final int len2 = update2.size();
      if (len1 != len2) {
        return len1 - len2;
      }
      final List<SimpleExpressionProxy> list1 = update1.getConstraints();
      final Iterator<SimpleExpressionProxy> iter1 = list1.iterator();
      final List<SimpleExpressionProxy> list2 = update2.getConstraints();
      final Iterator<SimpleExpressionProxy> iter2 = list2.iterator();
      while (iter1.hasNext()) {
        final SimpleExpressionProxy expr1 = iter1.next();
        final SimpleExpressionProxy expr2 = iter2.next();
        final int result = mExpressionComparator.compare(expr1, expr2);
        if (result != 0) {
          return result;
        }
      }
      return 0;
    }

    //#######################################################################
    //# Data Members
    private final ExpressionComparator mExpressionComparator;
  }


  //#########################################################################
  //# Data Members
  // Flags
  private boolean mCreatesGuardAutomaton = false;
  private boolean mUsesEventNameBuilder = false;
  private boolean mAutomatonVariablesEnabled = false;

  // Utilities
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final EFAIdentifierComparator mComparator;
  private final CompilationInfo mCompilationInfo;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private EFAModuleContext mRootContext;
  private EFAGuardCompiler mGuardCompiler;
  private EFAEventNameBuilder mEventNameBuilder;

  // Module Information
  private final ModuleProxy mInputModule;

  /**
   * A map from a primitive event identifier ({@link IdentifierProxy}) to
   * an {@link EFAEventInfo}, which is a more sophisticated data structure
   * containing information about an event.
   */
  private ProxyAccessorMap<IdentifierProxy,EFAEventInfo> mEventMap;

  /**
   * A map from a basic event identifier ({@link IdentifierProxy}) to its
   * associated update ({@link ConstraintList}).
   */
  private ProxyAccessorMap<IdentifierProxy,ConstraintList> mEventUpdateMap;
}
