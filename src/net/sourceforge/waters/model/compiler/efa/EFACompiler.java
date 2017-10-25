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

package net.sourceforge.waters.model.compiler.efa;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.AbortableCompiler;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.constraint.ConstraintPropagator;
import net.sourceforge.waters.model.compiler.constraint.SplitCandidate;
import net.sourceforge.waters.model.compiler.constraint.SplitComputer;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfoCloner;
import net.sourceforge.waters.model.compiler.context.UndefinedIdentifierException;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.expr.EvalException;
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
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;


/**
 * <P>The third pass of the compiler.</P>
 *
 * <P>This compiler accepts a module ({@link ModuleProxy}) as input and
 * produces another module as output. It expands all guard/action blocks
 * by partitioning the events, and replaces all variables by simple
 * components. Event arrays, aliases, foreach constructs, and
 * instantiations are not allowed in the input; these should be expanded by
 * a previous call the the module instance compiler ({@link
 * net.sourceforge.waters.model.compiler.instance.ModuleInstanceCompiler
 * ModuleInstanceCompiler}).</P>
 *
 * <P>The EFA compiler ensures that the resultant module only contains
 * nodes of the following types:</P>
 * <UL>
 * <LI>{@link EventDeclProxy}, where only simple events are defined,
 *     i.e., the list of ranges is guaranteed to be empty;</LI>
 * <LI>{@link SimpleComponentProxy};</LI>
 * </UL>
 *
 * <P><STRONG>Algorithm</STRONG></P>
 *
 * <P>The EFA compiler proceeds in four passes:</P>
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
 * @author Robi Malik, Roger Su
 */
public class EFACompiler extends AbortableCompiler
{
  //#########################################################################
  //# Constructor
  public EFACompiler(final ModuleProxyFactory factory,
                     final CompilationInfo compilationInfo,
                     final ModuleProxy module)
  {
    mFactory = factory;
    mCompilationInfo = compilationInfo;
    mOperatorTable = CompilerOperatorTable.getInstance();
    mSimpleExpressionCompiler =
      new SimpleExpressionCompiler(mFactory, mCompilationInfo, mOperatorTable);
    mInputModule = module;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mTransitionRelationBuilder != null) {
      mTransitionRelationBuilder.requestAbort();
    }
    if (mVariableAutomatonBuilder != null) {
      mVariableAutomatonBuilder.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mTransitionRelationBuilder != null) {
      mTransitionRelationBuilder.resetAbort();
    }
    if (mVariableAutomatonBuilder != null) {
      mVariableAutomatonBuilder.resetAbort();
    }
  }


  //#########################################################################
  //# Invocation
  public ModuleProxy compile() throws EvalException
  {
    try {
      mRootContext = new EFAModuleContext(mInputModule);
      mSplitComputer =
        new SplitComputer(mFactory, mOperatorTable, mRootContext);
      mTransitionRelationBuilder =
        new EFATransitionRelationBuilder(mFactory, mOperatorTable,
                                         mRootContext,
                                         mSimpleExpressionCompiler);
      mVariableAutomatonBuilder =
        new EFAVariableAutomatonBuilder(mFactory,
                                        mSimpleExpressionCompiler,
                                        mRootContext);
      // Pass 1
      final Pass1Visitor pass1 = new Pass1Visitor();
      mInputModule.acceptVisitor(pass1);
      // Pass 2
      final Pass2Visitor pass2 = new Pass2Visitor();
      mInputModule.acceptVisitor(pass2);
      // Pass 3
      computeEventPartitions();
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
      mSplitComputer = null;
      mTransitionRelationBuilder = null;
      mVariableAutomatonBuilder = null;
      mEFAEventMap = null;
    }
  }


  //#########################################################################
  //# Configuration
  void setUsingEventAlphabet(final boolean using)
  {
    mIsUsingEventAlphabet = using;
  }


  //#########################################################################
  //# Event Partitioning
  private void computeEventPartitions()
    throws EvalException
  {
    final ConstraintPropagator propagator =
      new ConstraintPropagator(mFactory, mCompilationInfo, mOperatorTable,
                               mRootContext);
    final EFAEventNameBuilder namer =
      new EFAEventNameBuilder(mFactory, mOperatorTable, mRootContext);
    mEFAEventMap = new HashMap<Proxy,Collection<EFAEvent>>();
    for (final EFAEventDecl edecl : mEFAEventDeclMap.values()) {
      if (!edecl.isBlocked() || edecl.getKind() == EventKind.PROPOSITION) {
        mTransitionRelationBuilder.initEventRecords();
        final Collection<EFAAutomatonTransitionGroup> allgroups =
          edecl.getTransitionGroups();
        final int allsize = allgroups.size();
        final List<EFAAutomatonTransitionGroup> groups =
          new ArrayList<EFAAutomatonTransitionGroup>(allsize);
        for (final EFAAutomatonTransitionGroup group : allgroups) {
          if (!group.isTrivial()) {
            groups.add(group);
          }
        }
        Collections.sort(groups);
        collectEventPartition(edecl, groups, propagator);
        namer.restart();
        for (final EFATransitionRelationBuilder.EventRecord record :
               mTransitionRelationBuilder.getSortedEventRecords()) {
          final EFAVariableTransitionRelation rel =
            record.getTransitionRelation();
          //System.err.println(edecl.getEventDecl().getName() + " > " + rel);
          final EFAEvent event = new EFAEvent(edecl, rel);
          for (final Proxy location : record.getSourceLocations()) {
            Collection<EFAEvent> collection = mEFAEventMap.get(location);
            if (collection == null) {
              collection = new LinkedList<EFAEvent>();
              mEFAEventMap.put(location, collection);
            }
            collection.add(event);
          }
          if (!rel.isEmpty()) {
            edecl.addEvent(event);
            for (final EFAVariable var : rel.getVariables()) {
              var.addEvent(event);
            }
            final ConstraintList formula = rel.getFormula();
            namer.addGuard(formula);
          }
        }
        mTransitionRelationBuilder.clearEventRecords();
        for (final EFAEvent event : edecl.getEvents()) {
          final EFAVariableTransitionRelation rel =
            event.getTransitionRelation();
          final ConstraintList formula = rel.getFormula();
          final String suffix = namer.getNameSuffix(formula);
          event.setSuffix(suffix);
        }
        namer.clear();
      }
    }
  }

  private void collectEventPartition
    (final EFAEventDecl edecl,
     final List<EFAAutomatonTransitionGroup> groups,
     final ConstraintPropagator propagator)
    throws EvalException
  {
    final int size = groups.size();
    final List<EFAAutomatonTransition> parts =
      new ArrayList<EFAAutomatonTransition>(size);
    final List<Proxy> locations = new ArrayList<Proxy>(size);
    collectEventPartition(edecl, groups, parts, 0, propagator, locations);
  }

  private void collectEventPartition
    (final EFAEventDecl edecl,
     final List<EFAAutomatonTransitionGroup> groups,
     final List<EFAAutomatonTransition> parts,
     final int index,
     final ConstraintPropagator parent,
     final List<Proxy> locations)
    throws EvalException
  {
    if (index < groups.size()) {
      final int numlocs = locations.size();
      final EFAAutomatonTransitionGroup group = groups.get(index);
      for (final EFAAutomatonTransition part : group.getPartialTransitions()) {
        final ConstraintPropagator propagator =
          new ConstraintPropagator(parent);
        final ConstraintList guard = part.getGuard();
        propagator.addConstraints(guard);
        propagator.propagate();
        parts.add(part);
        if (!guard.isTrue()) {
          locations.addAll(part.getSourceLocations());
        }
        collectEventPartition(edecl, groups, parts, index + 1,
                              propagator, locations);
        parts.remove(index);
        for (int i = locations.size() - 1; i >= numlocs; i--) {
          locations.remove(i);
        }
      }
    } else {
      splitEventPartition(edecl, parent, locations);
    }
  }

  private void splitEventPartition(final EFAEventDecl edecl,
                                   final ConstraintPropagator parent,
                                   final Collection<Proxy> locations)
    throws EvalException
  {
    checkAbort();
    if (parent.isUnsatisfiable()) {
      createEvent(edecl, parent, locations);
      return;
    }
    final ConstraintList guard = parent.getAllConstraints();
    //System.err.println(guard);
    final VariableContext context = parent.getContext();
    final SplitCandidate split = mSplitComputer.proposeSplit(guard, context);
    if (split == null) {
      createEvent(edecl, parent, locations);
    } else {
      for (final SimpleExpressionProxy expr :
             split.getSplitExpressions(mFactory, mOperatorTable)) {
        //System.err.println(" + " + expr);
        final ConstraintPropagator propagator =
          new ConstraintPropagator(parent);
        split.recall(propagator);
        propagator.addConstraint(expr);
        propagator.propagate();
        //System.err.println(" = " + propagator.getAllConstraints());
        if (!propagator.isUnsatisfiable()) {
          splitEventPartition(edecl, propagator, locations);
        }
      }
    }
  }

  private void createEvent(final EFAEventDecl edecl,
                           final ConstraintPropagator propagator,
                           final Collection<Proxy> locations)
    throws EvalException
  {
    final ConstraintList guard = propagator.getAllConstraints();
    //System.err.println(edecl.getEventDecl().getName() + " . " + guard);
    mTransitionRelationBuilder.addEventRecord(edecl, guard, locations);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void insertEventDecl(final IdentifierProxy ident,
                               final EFAEventDecl edecl)
    throws DuplicateIdentifierException
  {
    final ProxyAccessor<IdentifierProxy> accessor =
      mEFAEventDeclMap.createAccessor(ident);
    if (mEFAEventDeclMap.containsKey(accessor)) {
      throw new DuplicateIdentifierException(ident, "event");
    } else {
      mEFAEventDeclMap.put(accessor, edecl);
    }
  }

  private EFAEventDecl getEventDecl(final IdentifierProxy ident)
  {
    return mEFAEventDeclMap.getByProxy(ident);
  }

  private EFAEventDecl findEventDecl(final IdentifierProxy ident)
    throws UndefinedIdentifierException
  {
    final EFAEventDecl edecl = getEventDecl(ident);
    if (edecl == null) {
      throw new UndefinedIdentifierException(ident, "event");
    } else {
      return edecl;
    }
  }


  //#########################################################################
  //# Inner Class: Pass1Visitor
  /**
   * The visitor implementing the first pass of EFA compilation.
   * <p>
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
      final GraphProxy graph = comp.getGraph();
      visitGraphProxy(graph);
      return null;
      // TODO Generation of automaton variable - disabled for now
      // (see also two compiler tests for this)
      // final List<SimpleIdentifierProxy> list = visitGraphProxy(graph);
      // final CompiledRange range = new CompiledEnumRange(list);
      // mRootContext.createVariables(comp, range, mFactory, mOperatorTable);
      //  return range;
    }

    @Override
    public IdentifierProxy visitSimpleNodeProxy(final SimpleNodeProxy node)
      throws VisitorException
    {
      checkAbortInVisitor();
      final String name = node.getName();
      final SimpleIdentifierProxy ident =
        mFactory.createSimpleIdentifierProxy(name);
      mCompilationInfo.add(ident, node);
      // TODO Generation of automaton variable - disabled for now
      // mRootContext.insertEnumAtom(ident);
      mCurrentRange.add(ident);
      return ident;
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
    //# Data Member
    private List<SimpleIdentifierProxy> mCurrentRange;
  }


  //#########################################################################
  //# Inner Class: Pass2Visitor
  /**
   * The visitor implementing the second pass of EFA compilation.
   */
  private class Pass2Visitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Constructor
    private Pass2Visitor()
    {
      mGuardCompiler = new EFAGuardCompiler(mFactory, mOperatorTable);
      mVariableCollector =
        new EFAVariableCollector(mOperatorTable,mRootContext);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      try {
        final GuardActionBlockProxy ga = edge.getGuardActionBlock();
        if (ga == null) {
          mCurrentGuard = ConstraintList.TRUE;
        } else {
          if (mIsUsingEventAlphabet) {
            mCollectedVariables = new THashSet<EFAVariable>();
          }
          visitGuardActionBlockProxy(ga);
        }
        final LabelBlockProxy block = edge.getLabelBlock();
        visitLabelBlockProxy(block);
        return null;
      } finally {
        mCurrentGuard = null;
        if (mIsUsingEventAlphabet) {
          mCollectedVariables = null;
        }
      }
    }

    @Override
    public EFAEventDecl visitEventDeclProxy(final EventDeclProxy decl)
      throws VisitorException
    {
      try {
        checkAbortInVisitor();
        final IdentifierProxy ident = decl.getIdentifier();
        final EFAEventDecl edecl = new EFAEventDecl(decl);
        insertEventDecl(ident, edecl);
        return edecl;
      } catch (final DuplicateIdentifierException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public Object visitGraphProxy(final GraphProxy graph)
      throws VisitorException
    {
      final LabelBlockProxy blocked = graph.getBlockedEvents();
      if (blocked != null) {
        visitLabelBlockProxy(blocked);
      }
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
        mCurrentGuard = mGuardCompiler.getCompiledGuard(ga);
        for (final SimpleExpressionProxy guard :
               mCurrentGuard.getConstraints()) {
          mVariableCollector.collectPrimedVariables
            (guard, mCollectedVariables);
        }
        return mCurrentGuard;
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public EFAEventDecl visitIdentifierProxy(final IdentifierProxy ident)
      throws VisitorException
    {
      try {
        checkAbortInVisitor();
        final EFAEventDecl edecl = findEventDecl(ident);
        if (!edecl.isBlocked()) {
          mCollectedEvents.add(edecl);
          if (mIsUsingEventAlphabet && mCollectedVariables != null) {
            edecl.addVariables(mCollectedVariables);
          }
          if (mCurrentGuard != null) {
            final EFAAutomatonTransitionGroup trans =
              edecl.createTransitionGroup(mCurrentComponent);
            trans.addPartialTransition(mCurrentGuard, ident);
          }
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
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      final List<EventDeclProxy> events = module.getEventDeclList();
      final int size = events.size();
      mEFAEventDeclMap = new ProxyAccessorHashMap<>(eq, size);
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
        final int size = mEFAEventDeclMap.size();
        mCurrentComponent = comp;
        mCollectedEvents = new THashSet<EFAEventDecl>(size);
        if (!mIsUsingEventAlphabet) {
          mCollectedVariables = new THashSet<EFAVariable>();
        }
        final ComponentKind ckind = comp.getKind();
        final GraphProxy graph = comp.getGraph();
        visitGraphProxy(graph);
        for (final EFAEventDecl edecl : mCollectedEvents) {
          if (!mIsUsingEventAlphabet) {
            edecl.addVariables(mCollectedVariables);
          }
          final EventKind ekind = edecl.getKind();
          final EFAAutomatonTransitionGroup group =
            edecl.createTransitionGroup(comp);
          if (ekind == EventKind.CONTROLLABLE &&
              ckind == ComponentKind.PROPERTY ||
              ekind == EventKind.UNCONTROLLABLE &&
              ckind != ComponentKind.PLANT) {
            makeDisjoint(group, true);
          } else if (group.isEmpty()) {
            edecl.setBlocked();
          }
        }
        return null;
      } catch (final EvalException exception) {
        throw wrap(exception);
      } finally {
        mCurrentComponent = null;
        mCollectedEvents = null;
        mCollectedVariables = null;
      }
    }

    @Override
    public Object visitVariableComponentProxy(final VariableComponentProxy var)
    {
      return null;
    }

    //#######################################################################
    //# Auxiliary Methods
    private void makeDisjoint(final EFAAutomatonTransitionGroup group,
                              final boolean catchAll)
      throws EvalException
    {
      final List<EFAAutomatonTransition> parts =
        new ArrayList<>(group.getPartialTransitions());
      final int size = parts.size();
      final ListIterator<EFAAutomatonTransition> iter = parts.listIterator();
      final List<EFAAutomatonTransition> selected = new ArrayList<>(size);
      final Set<EFAAutomatonTransition> everSelected = new THashSet<>(size);
      final Collection<EFAAutomatonTransition> result = new LinkedList<>();
      final ConstraintPropagator propagator =
        new ConstraintPropagator(mFactory, mCompilationInfo,
                                 mOperatorTable, mRootContext);
      final SimpleComponentProxy comp =
        catchAll ? group.getSimpleComponent() : null;
      makeDisjoint(iter, selected, everSelected, result, propagator, comp);
      if (everSelected.size() < size) {
        /* Create an explicit transition with FALSE guard for those
         * transitions that have been dropped.
         */
        final SimpleExpressionProxy falseExpr =
          mFactory.createIntConstantProxy(0);
        final List<SimpleExpressionProxy> falseList =
          Collections.singletonList(falseExpr);
        final ConstraintList falseGuard = new ConstraintList(falseList);
        final EFAAutomatonTransition falseTrans =
          new EFAAutomatonTransition(falseGuard);
        for (final EFAAutomatonTransition trans : parts) {
          if (!everSelected.contains(trans)) {
            falseTrans.addSources(trans);
          }
        }
        result.add(falseTrans);
      }
      group.setPartialTransitions(result);
    }

    private void makeDisjoint(final ListIterator<EFAAutomatonTransition> iter,
                              final List<EFAAutomatonTransition> selected,
                              final Set<EFAAutomatonTransition> everSelected,
                              final Collection<EFAAutomatonTransition> result,
                              final ConstraintPropagator parent,
                              final SimpleComponentProxy catchAll)
      throws EvalException
    {
      checkAbort();
      if (iter.hasNext()) {
        final EFAAutomatonTransition trans = iter.next();
        final ConstraintList guard = trans.getGuard();
        ConstraintPropagator propagator = new ConstraintPropagator(parent);
        propagator.addConstraints(guard);
        propagator.propagate();
        if (!propagator.isUnsatisfiable()) {
          final int end = selected.size();
          selected.add(trans);
          everSelected.add(trans);
          makeDisjoint(iter, selected, everSelected,
                       result, propagator, catchAll);
          selected.remove(end);
        }
        propagator = new ConstraintPropagator(parent);
        propagator.addNegation(guard);
        propagator.propagate();
        if (!propagator.isUnsatisfiable()) {
          makeDisjoint(iter, selected, everSelected,
                       result, propagator, catchAll);
        }
        iter.previous();
      } else if (catchAll != null || !selected.isEmpty()) {
        final ConstraintList guard = parent.getAllConstraints(false);
        final EFAAutomatonTransition trans = new EFAAutomatonTransition(guard);
        if (selected.isEmpty()) {
          trans.addSource(catchAll);
        } else {
          for (final EFAAutomatonTransition old : selected) {
            trans.addSources(old);
          }
        }
        result.add(trans);
      }
    }

    //#######################################################################
    //# Data Members
    private final EFAGuardCompiler mGuardCompiler;
    private final EFAVariableCollector mVariableCollector;

    private SimpleComponentProxy mCurrentComponent;
    private Set<EFAVariable> mCollectedVariables;
    private Set<EFAEventDecl> mCollectedEvents;
    private ConstraintList mCurrentGuard;
  }


  //#########################################################################
  //# Inner Class: Pass4Visitor
  /**
   * The visitor implementing the fourth pass of EFA compilation.
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
        final EFAEventDecl edecl = findEventDecl(ident);
        final EventKind kind = edecl.getKind();
        final boolean observable = edecl.isObservable();
        final Map<String,String> attribs = edecl.getAttributes();
        for (final EFAEvent event : edecl.getEvents()) {
          final IdentifierProxy subident = event.createIdentifier(mFactory);
          final EventDeclProxy subdecl = mFactory.createEventDeclProxy
            (subident, kind, observable, ScopeKind.LOCAL, null, null, attribs);
          mEventDeclarations.add(subdecl);
          mCompilationInfo.add(subdecl, decl);
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
      final NodeProxy source0 = edge.getSource();
      final NodeProxy source1 = mNodeMap.get(source0);
      final NodeProxy target0 = edge.getTarget();
      final NodeProxy target1 = mNodeMap.get(target0);
      final LabelBlockProxy block0 = edge.getLabelBlock();
      final LabelBlockProxy block1 = visitLabelBlockProxy(block0);
      final EdgeProxy result = mFactory.createEdgeProxy
        (source1, target1, block1, null, null, null, null);
      mEdgeList.add(result);
      return result;
    }

    @Override
    public GraphProxy visitGraphProxy(final GraphProxy graph)
      throws VisitorException
    {
      try {
        mEFAAlphabet = new THashSet<EFAEvent>();
        final Collection<NodeProxy> nodes = graph.getNodes();
        final int numnodes = nodes.size();
        mNodeList = new ArrayList<NodeProxy>(numnodes);
        mNodeMap = new HashMap<NodeProxy,NodeProxy>(numnodes);
        visitCollection(nodes);
        mInBlockedEventsList = false;
        final Collection<EdgeProxy> edges = graph.getEdges();
        final int numedges = edges.size();
        mEdgeList = new ArrayList<EdgeProxy>(numedges);
        visitCollection(edges);
        mInBlockedEventsList = true;
        mLabelList = new LinkedList<IdentifierProxy>();
        final LabelBlockProxy blocked0 = graph.getBlockedEvents();
        if (blocked0 != null) {
          final List<Proxy> list = blocked0.getEventIdentifierList();
          visitCollection(list);
        }
        final Collection<EFAEvent> events =
          mEFAEventMap.get(mCurrentComponent);
        if (events != null) {
          for (final EFAEvent event : events) {
            checkAbortInVisitor();
            if (!mEFAAlphabet.contains(event) && !event.isBlocked()) {
              final IdentifierProxy ident = event.createIdentifier(mFactory);
              mLabelList.add(ident);
            }
          }
        }
        final LabelBlockProxy blocked1 =
          mLabelList.isEmpty() ? null :
          mFactory.createLabelBlockProxy(mLabelList, null);
        final boolean deterministic = graph.isDeterministic();
        return mFactory.createGraphProxy
          (deterministic, blocked1, mNodeList, mEdgeList);
      } finally {
        mEFAAlphabet = null;
        mNodeList = null;
        mNodeMap = null;
        mEdgeList = null;
        mLabelList = null;
        mInBlockedEventsList = false;
      }
    }

    @Override
    public Object visitIdentifierProxy(final IdentifierProxy ident)
      throws VisitorException
    {
      checkAbortInVisitor();
      Collection<EFAEvent> events = mEFAEventMap.get(ident);
      if (events == null) {
        final EFAEventDecl edecl = getEventDecl(ident);
        events = edecl.getEvents();
      }
      for (final EFAEvent event : events) {
        if (event.isBlocked()) {
          continue;
        } else if (mInBlockedEventsList) {
          if (mEFAAlphabet.add(event)) {
            final IdentifierProxy subident = event.createIdentifier(mFactory);
            mLabelList.add(subident);
            mCompilationInfo.add(subident, ident);
          }
        } else {
          mEFAAlphabet.add(event);
          final IdentifierProxy subident = event.createIdentifier(mFactory);
          mLabelList.add(subident);
          mCompilationInfo.add(subident, ident);
        }
      }
      return null;
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
        mEventDeclarations = new ArrayList<EventDeclProxy>(numdecls);
        visitCollection(decls);
        final List<Proxy> components = module.getComponentList();
        final int numcomps = components.size();
        mComponents = new ArrayList<SimpleComponentProxy>(numcomps);
        visitCollection(components);
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
        // If the component is the ':updates' automata, then do nothing.
        if (comp.getName().equals(":updates")) return null;

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
      }
      finally {
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
      mCompilationInfo.add(result, node);
      return result;
    }

    @Override
    public SimpleComponentProxy visitVariableComponentProxy
      (final VariableComponentProxy comp)
      throws VisitorException
    {
      try {
        checkAbortInVisitor();
        final IdentifierProxy ident = comp.getIdentifier();
        final EFAVariable var = mRootContext.getVariable(ident);
        final SimpleComponentProxy result =
          mVariableAutomatonBuilder.constructSimpleComponent(var);
        mCompilationInfo.add(result, comp);
        mComponents.add(result);
        return result;
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    //#######################################################################
    //# Data Members
    private final ModuleProxyCloner mCloner;

    private List<EventDeclProxy> mEventDeclarations;
    private List<SimpleComponentProxy> mComponents;
    private SimpleComponentProxy mCurrentComponent;
    private Set<EFAEvent> mEFAAlphabet;
    private List<NodeProxy> mNodeList;
    private Map<NodeProxy,NodeProxy> mNodeMap;
    private List<EdgeProxy> mEdgeList;
    private List<IdentifierProxy> mLabelList;
    private boolean mInBlockedEventsList;
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilationInfo mCompilationInfo;
  private final CompilerOperatorTable mOperatorTable;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final ModuleProxy mInputModule;

  private boolean mIsUsingEventAlphabet = true;

  private EFAModuleContext mRootContext;
  private SplitComputer mSplitComputer;
  private EFATransitionRelationBuilder mTransitionRelationBuilder;
  private EFAVariableAutomatonBuilder mVariableAutomatonBuilder;

  // Pass 2
  /**
   * A map that assigns to each identifier of an event declaration {@link
   * EventDeclProxy} the information about its event variable set and
   * associated guards.
   */
  private ProxyAccessorMap<IdentifierProxy,EFAEventDecl> mEFAEventDeclMap;

  // Pass 3
  /**
   * A map that assigns to each identifier of an event label on an edge the
   * list of EFA events to be associated with it. Likewise, it assigns to
   * each automaton ({@link SimpleComponentProxy}) a list of events to be
   * blocked globally in the automaton. Identifiers associated with true
   * guards do not have entries in this table, as they will simply receive
   * all events associated with the event declaration; all other
   * identifiers receive mappings that reflect the results of simplifying
   * their guards.
   */
  private Map<Proxy,Collection<EFAEvent>> mEFAEventMap;

}
