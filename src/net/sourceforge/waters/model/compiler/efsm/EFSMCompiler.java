//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.AbortableCompiler;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.constraint.ConstraintPropagator;
import net.sourceforge.waters.model.compiler.constraint.SplitCandidate;
import net.sourceforge.waters.model.compiler.constraint.SplitComputer;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.compiler.context.CompiledEnumRange;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.ModuleBindingContext;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SingleBindingContext;
import net.sourceforge.waters.model.compiler.context.SourceInfoCloner;
import net.sourceforge.waters.model.compiler.context.UndefinedIdentifierException;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.compiler.efa.EFAEventNameBuilder;
import net.sourceforge.waters.model.compiler.efa.EFAGuardCompiler;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.FunctionCallExpressionProxy;
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
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * <P>A subtask of the {@link ModuleCompiler} to compile EFSM guards.</P>
 *
 * <P>This compiler accepts a normalised module ({@link ModuleProxy}) as input
 * and produces another module as output. It expands all guard/action blocks
 * by partitioning the events, and replaces all variables by simple
 * components. Event arrays, event aliases, foreach constructs, and
 * instantiations are not allowed in the input. Constant alias declarations
 * are only parsed to collect enumeration atoms. All these constructs should
 * be expanded by a previous call the the module instance compiler ({@link
 * net.sourceforge.waters.model.compiler.instance.ModuleInstanceCompiler
 * ModuleInstanceCompiler}).</P>
 *
 * <P>The EFA compiler ensures that the resultant module only contains
 * nodes of the following types:</P>
 * <UL>
 * <LI>{@link ConstantAliasProxy}, where the defined type is an enumeration
 *     type.
 * <LI>{@link EventDeclProxy}, where only simple events are defined,
 *     i.e., the list of ranges is guaranteed to be empty.
 *     Also the guards must be normalised, which means that each occurrence
 *     of a given event in an automaton has the same guard/action block,
 *     and occurrences of the same event in different automata either have
 *     the same or no guard/action block.</LI>
 * <LI>{@link SimpleComponentProxy}.</LI>
 * </UL>
 *
 * <P><STRONG>Algorithm</STRONG></P>
 *
 * <P>The EFSM compiler proceeds in three passes:</P>
 * <OL>
 * <LI>Identify all components (simple or variable) and their state
 *     space.</LI>
 * <LI>Collect and split guards.</LI>
 * <LI>Build output automata.</LI>
 * </OL>
 *
 * @author Robi Malik, Roger Su
 */
public class EFSMCompiler extends AbortableCompiler
{
  //#########################################################################
  //# Constructor
  public EFSMCompiler(final ModuleProxyFactory factory,
                      final CompilationInfo compilationInfo,
                      final ModuleProxy module)
  {
    mFactory = factory;
    mEquality = new ModuleEqualityVisitor(false);
    mCompilationInfo = compilationInfo;
    mOperatorTable = CompilerOperatorTable.getInstance();
    mSimpleExpressionCompiler =
      new SimpleExpressionCompiler(mFactory, mCompilationInfo, mOperatorTable);
    mInputModule = module;
  }


  //#########################################################################
  //# Invocation
  public ModuleProxy compile() throws EvalException
  {
    try {
      mRootContext = new EFSMModuleContext(mInputModule, mOperatorTable);

      final Pass1Processor pass1 = new Pass1Processor();
      pass1.process();

      final Pass2Processor pass2 = new Pass2Processor();
      pass2.process();

      final Pass3Processor pass3 = new Pass3Processor();
      final ModuleProxy result = pass3.process();

      return result;
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
      mEFSMEventDeclarationMap = null;
      mEFSMEventDeclarations = null;
      mEFSMComponents = null;
    }
  }


  //#########################################################################
  //# Configuration
  public void setAutomatonVariablesEnabled(final boolean enabled)
  {
    mAutomatonVariablesEnabled = enabled;
  }


  //#########################################################################
  //# Inner Class Pass1Processor
  /**
   * The visitor implementing the first pass of EFSM compilation.
   * It initialises the lists of events and components.
   * The root context ({@link #mRootContext}) is initialised to associate
   * the identifier of each simple or variable component with a {@link
   * EFSMComponent} object that contains the range of possible state values
   * of that component.
   */
  private class Pass1Processor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Constructor
    private Pass1Processor()
    {
      mGuardCompiler = new EFAGuardCompiler(mFactory, mOperatorTable);
      mAutVarTransitionMap = mAutomatonVariablesEnabled ? new HashMap<>() : null;
    }

    //#######################################################################
    //# Invocation
    private void process()
      throws VisitorException
    {
      mInputModule.acceptVisitor(this);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitConstantAliasProxy(final ConstantAliasProxy alias)
      throws VisitorException
    {
      final ExpressionProxy expr = alias.getExpression();
      return expr.acceptVisitor(this);
    }

    @Override
    public Object visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      try {
        final GuardActionBlockProxy ga = edge.getGuardActionBlock();
        if (ga == null) {
          mCurrentGuard = ConstraintList.TRUE;
        } else {
          mCurrentGuard = visitGuardActionBlockProxy(ga);
        }
        if (mCurrentComponent != null) {
          final NodeProxy source = edge.getSource();
          final int s = mCurrentRangeMap.get(source);
          final NodeProxy target = edge.getTarget();
          final int t = mCurrentRangeMap.get(target);
          mCurrentTransition = EFSMComponent.getTransitionCode(s, t);
        }
        final LabelBlockProxy block = edge.getLabelBlock();
        visitLabelBlockProxy(block);
        return null;
      } finally {
        mCurrentGuard = null;
      }
    }

    @Override
    public Object visitEnumSetExpressionProxy(final EnumSetExpressionProxy expr)
      throws VisitorException
    {
      for (final SimpleIdentifierProxy ident : expr.getItems()) {
        try {
          mRootContext.insertEnumAtom(ident);
        } catch (final EvalException exception) {
          mCompilationInfo.raiseInVisitor(exception);
        }
      }
      return null;
    }

    @Override
    public EFSMEventDeclaration visitEventDeclProxy(final EventDeclProxy decl)
      throws VisitorException
    {
      final IdentifierProxy ident = decl.getIdentifier();
      final ProxyAccessor<IdentifierProxy> accessor =
        mEFSMEventDeclarationMap.createAccessor(ident);
      EFSMEventDeclaration info = mEFSMEventDeclarationMap.get(accessor);
      if (info == null) {
        info = new EFSMEventDeclaration(decl);
        mEFSMEventDeclarationMap.put(accessor, info);
        mEFSMEventDeclarations.add(info);
      } else {
        final DuplicateIdentifierException exception =
          new DuplicateIdentifierException(ident, "event");
        mCompilationInfo.raiseInVisitor(exception);
      }
      return info;
    }

    @Override
    public ConstraintList visitGuardActionBlockProxy
      (final GuardActionBlockProxy ga)
      throws VisitorException
    {
      try {
        checkAbortInVisitor();
        return mGuardCompiler.getCompiledGuard(ga);
      } catch (final EvalException exception) {
        mCompilationInfo.raiseInVisitor(exception);
        return ConstraintList.TRUE;
      }
    }

    @Override
    public EFSMEventDeclaration visitIdentifierProxy(final IdentifierProxy ident)
      throws VisitorException
    {
      checkAbortInVisitor();
      final ProxyAccessor<IdentifierProxy> accessor =
        mEFSMEventDeclarationMap.createAccessor(ident);
      final EFSMEventDeclaration decl = mEFSMEventDeclarationMap.get(accessor);
      if (decl == null) {
        final UndefinedIdentifierException exception =
          new UndefinedIdentifierException(ident, "event");
        mCompilationInfo.raiseInVisitor(exception);
        return null;
      }
      if (mCurrentGuard == null) {
        // in blocked events list
        if (mAutomatonVariablesEnabled &&
            !mAutVarTransitionMap.containsKey(decl)) {
          mAutVarTransitionMap.put(decl, null);
        }
      } else {
        // in edge label block
        if (!decl.provideGuardedActions(mCurrentGuard)) {
          final EFSMNormalisationException exception =
            new EFSMNormalisationException(ident);
          mCompilationInfo.raiseInVisitor(exception);
          return null;
        }
        if (mAutomatonVariablesEnabled && mCurrentComponent != null) {
          // Don't do this for the ":updates" automaton
          TLongArrayList transitions = mAutVarTransitionMap.get(decl);
          if (transitions == null) {
            transitions = new TLongArrayList();
            mAutVarTransitionMap.put(decl, transitions);
          }
          transitions.add(mCurrentTransition);
        }
      }
      return decl;
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
      final List<EventDeclProxy> decls = module.getEventDeclList();
      final int numEvents = decls.size();
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      mEFSMEventDeclarationMap = new ProxyAccessorHashMap<>(eq, numEvents);
      mEFSMEventDeclarations = new ArrayList<>(numEvents);
      visitCollection(decls);
      final List<ConstantAliasProxy> aliases = module.getConstantAliasList();
      visitCollection(aliases);
      final List<Proxy> components = module.getComponentList();
      mEFSMComponents = new ArrayList<>(components.size());
      visitCollection(components);
      return null;
    }

    @Override
    public EFSMSimpleComponent visitSimpleComponentProxy
      (final SimpleComponentProxy comp)
      throws VisitorException
    {
      try {
        final GraphProxy graph = comp.getGraph();
        final Map<String,String> attribs = comp.getAttributes();
        if (attribs.containsKey(ATTRIB_UPDATES)) {
          visitCollection(graph.getEdges());
        } else {
          final Collection<NodeProxy> nodes = graph.getNodes();
          final int size = nodes.size();
          mCurrentRangeList = new ArrayList<>(size);
          mCurrentRangeMap = new TObjectIntHashMap<>(size, 0.5f, -1);
          visitCollection(nodes);
          final CompiledRange range = new CompiledEnumRange(mCurrentRangeList);
          mCurrentComponent = new EFSMSimpleComponent(comp, range);
          mEFSMComponents.add(mCurrentComponent);
          if (mAutomatonVariablesEnabled) {
            try {
              mRootContext.insertEFSMComponent(mCurrentComponent);
            } catch (final DuplicateIdentifierException exception) {
              mCompilationInfo.raiseInVisitor(exception);
            }
          }
          visitCollection(graph.getEdges());
          final LabelBlockProxy blocked = graph.getBlockedEvents();
          if (blocked != null) {
            visitLabelBlockProxy(blocked);
          }
          if (mAutomatonVariablesEnabled) {
            mCurrentComponent.initialiseTransitions(mAutVarTransitionMap);
          }
        }
        return mCurrentComponent;
      } finally {
        mCurrentComponent = null;
        mCurrentRangeList = null;
        mCurrentRangeMap = null;
        if (mAutomatonVariablesEnabled) {
          mAutVarTransitionMap.clear();
        }
      }
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
      if (mAutomatonVariablesEnabled) {
        try {
          mRootContext.insertEnumAtom(ident);
        } catch (final EvalException exception) {
          mCompilationInfo.raiseInVisitor(exception);
        }
      }
      mCurrentRangeMap.put(node, mCurrentRangeList.size());
      mCurrentRangeList.add(ident);
      return ident;
    }

    @Override
    public EFSMComponent visitVariableComponentProxy
      (final VariableComponentProxy var)
      throws VisitorException
    {
      try {
        final SimpleExpressionProxy expr = var.getType();
        final SimpleExpressionProxy value =
          mSimpleExpressionCompiler.eval(expr, mRootContext);
        final CompiledRange range =
          mSimpleExpressionCompiler.getRangeValue(value);
        final EFSMVariableComponent comp =
          new EFSMVariableComponent(var, range);
        mEFSMComponents.add(comp);
        mRootContext.insertEFSMComponent(comp);
        return comp;
      } catch (final EvalException exception) {
        mCompilationInfo.raiseInVisitor(exception);
        return null;
      }
    }

    //#######################################################################
    //# Data Members
    private final EFAGuardCompiler mGuardCompiler;
    private final Map<EFSMEventDeclaration,TLongArrayList> mAutVarTransitionMap;
    private List<SimpleIdentifierProxy> mCurrentRangeList;
    private TObjectIntMap<SimpleNodeProxy> mCurrentRangeMap;
    private EFSMSimpleComponent mCurrentComponent;
    private ConstraintList mCurrentGuard;
    private long mCurrentTransition;
  }


  //#########################################################################
  //# Inner Class Pass2Processor
  /**
   * The visitor implementing the second pass of EFSM compilation.
   */
  private class Pass2Processor implements EFSMTransitionIteratorFactory
  {
    //#######################################################################
    //# Constructor
    private Pass2Processor()
    {
      mSplitComputer =
        new SplitComputer(mFactory, mOperatorTable, mRootContext);
      mEventNameBuilder =
        new EFAEventNameBuilder(mFactory, mOperatorTable, mRootContext);
    }

    //#######################################################################
    //# Invocation
    private void process()
      throws EvalException
    {
      for (final EFSMEventDeclaration decl : mEFSMEventDeclarations) {
        splitEvent(decl);
      }
    }

    //#######################################################################
    //# Event Splitting
    private void splitEvent(final EFSMEventDeclaration decl)
      throws EvalException
    {
      final ConstraintList ga = decl.getGuardedActions();
      if (ga.isTrue()) {
        final EFSMEventInstance inst = new EFSMEventInstance(decl);
        decl.addInstance(inst);
      } else {
        mCollectedConstraintLists = new THashSet<>();
        mCollectedEFSMComponents = new THashSet<>();
        final ConstraintPropagator propagator =
          new ConstraintPropagator(mFactory, mCompilationInfo,
                                   mOperatorTable, mRootContext);
        propagator.addConstraints(ga);
        try {
          propagator.propagate();
          mSubsumptionEnabled = false;
          splitEvent(propagator);
        } catch (final EvalException exception) {
          mCompilationInfo.raise(exception);
          mCollectedConstraintLists = null;
          mCollectedEFSMComponents = null;
          return;
        }
        final EFSMComponent[] components =
          new EFSMComponent[mCollectedEFSMComponents.size()];
        mCollectedEFSMComponents.toArray(components);
        try {
          for (final ConstraintList inst : mCollectedConstraintLists) {
            createInstance(decl, inst, components);
          }
        } catch (final EvalException exception) {
          mCompilationInfo.raise(exception);
          cancel(decl, components);
          return;
        } finally {
          mCollectedConstraintLists = null;
          mCollectedEFSMComponents = null;
        }
        decl.provideSuffixes(mEventNameBuilder);
      }
    }

    private void splitEvent(final ConstraintPropagator parent)
      throws EvalException
    {
      checkAbort();
      if (!parent.isUnsatisfiable()) {
        final ConstraintList ga = parent.getAllConstraints();
        final VariableContext context = parent.getContext();
        final SplitCandidate split = mSplitComputer.proposeSplit(ga, context);
        if (split == null) {
          recordInstance(ga);
        } else {
          mSubsumptionEnabled |= !split.isDisjoint();
          for (final SimpleExpressionProxy expr :
            split.getSplitExpressions(mFactory, mOperatorTable)) {
            final ConstraintPropagator propagator =
              new ConstraintPropagator(parent);
            split.recall(propagator);
            propagator.addConstraint(expr);
            propagator.propagate();
            splitEvent(propagator);
          }
        }
      }
    }

    private void recordInstance(final ConstraintList ga)
    {
      if (mCollectedConstraintLists.add(ga)) {
        for (final SimpleExpressionProxy literal : ga.getConstraints()) {
          final EFSMComponent comp =
            mRootContext.getMentionedEFSMComponent(literal);
          mCollectedEFSMComponents.add(comp);
        }
      }
    }

    private void createInstance(final EFSMEventDeclaration decl,
                                final ConstraintList ga,
                                final EFSMComponent[] components)
      throws EvalException
    {
      final Map<EFSMComponent,List<SimpleExpressionProxy>> map1 =
        new HashMap<>(components.length);
      for (final SimpleExpressionProxy literal : ga.getConstraints()) {
        final EFSMComponent comp =
          mRootContext.getMentionedEFSMComponent(literal);
        List<SimpleExpressionProxy> list = map1.get(comp);
        if (list == null) {
          list = new ArrayList<>(4);
          map1.put(comp, list);
        }
        list.add(literal);
      }
      final EFSMEventInstance inst = new EFSMEventInstance(decl, ga);
      final Map<EFSMComponent,EFSMComponent.TransitionGroup> map2 =
        new HashMap<>(components.length);
      for (final Map.Entry<EFSMComponent,List<SimpleExpressionProxy>>
           entry : map1.entrySet()) {
        final EFSMComponent comp = entry.getKey();
        final List<SimpleExpressionProxy> list = entry.getValue();
        final ConstraintList constraints = new ConstraintList(list);
        final EFSMComponent.TransitionGroup group =
          comp.createTransitionGroup(inst, constraints, this);
        if (group == null) {
          return;
        }
        map2.put(comp, group);
      }
      if (survivesSubsumptionCheck(components, map2, decl)) {
        decl.addInstance(inst);
        for (final Map.Entry<EFSMComponent,EFSMComponent.TransitionGroup>
             entry : map2.entrySet()) {
          final EFSMComponent comp = entry.getKey();
          final EFSMComponent.TransitionGroup group = entry.getValue();
          comp.associateEventInstance(inst, group);
        }
      }
    }

    private void cancel(final EFSMEventDeclaration decl,
                        final EFSMComponent[] components)
    {
      for (final EFSMComponent comp : components) {
        for (final EFSMEventInstance inst : decl.getInstances()) {
          comp.removeEventInstance(inst);
        }
      }
      decl.removeAllInstances();
    }


    //#######################################################################
    //# Subsumption Check
    private boolean survivesSubsumptionCheck
      (final EFSMComponent[] components,
       final Map<EFSMComponent,EFSMComponent.TransitionGroup> newGroups,
       final EFSMEventDeclaration decl)
    {
      if (mSubsumptionEnabled) {
        final Iterable<EFSMEventInstance> instances = decl.getInstances();
        for (final EFSMEventInstance inst : instances) {
          if (isSubsumed(components, newGroups, inst)) {
            return false;
          }
        }
        final Iterator<EFSMEventInstance> iter = instances.iterator();
        while (iter.hasNext()) {
          final EFSMEventInstance inst = iter.next();
          if (subsumes(components, newGroups, inst)) {
            iter.remove();
            for (final EFSMComponent comp : components) {
              comp.removeEventInstance(inst);
            }
          }
        }
      }
      return true;
    }

    private boolean isSubsumed
      (final EFSMComponent[] components,
       final Map<EFSMComponent,EFSMComponent.TransitionGroup> newGroups,
       final EFSMEventInstance existingInst)
    {
      for (final EFSMComponent comp : components) {
        final EFSMComponent.TransitionGroup group = newGroups.get(comp);
        if (!comp.isSubsumed(group, existingInst)) {
          return false;
        }
      }
      return true;
    }

    private boolean subsumes
      (final EFSMComponent[] components,
       final Map<EFSMComponent,EFSMComponent.TransitionGroup> newGroups,
       final EFSMEventInstance existingInst)
    {
      for (final EFSMComponent comp : components) {
        final EFSMComponent.TransitionGroup group = newGroups.get(comp);
        if (!comp.subsumes(group, existingInst)) {
          return false;
        }
      }
      return true;
    }


    //#######################################################################
    //# Interface
    //# net.sourceforge.waters.model.compiler.efsm.EFSMTransitionIteratorFactory
    @Override
    public EFSMTransitionIterator createTransitionIterator
      (final EFSMComponent comp,
       final ConstraintList constraints)
    {
      final IdentifierProxy compIdent = comp.getIdentifier();
      final CompiledRange range = comp.getRange();
      final SimpleExpressionProxy def =
        mUnprimedSearchVisitor.getDefiningExpression(constraints);
      if (!mPrimedSearchVisitor.occurs(compIdent, constraints)) {
        if (def == null) {
          // all values for current state, next state equal to current
          return new FunctionallyDependentIterator
            (comp, constraints, compIdent, true);
        } else if (range.contains(def)) {
          // fixed value for current state, next state same
          return new FullIterator(comp, constraints, def, def);
        } else {
          // fixed value not in range
          return null;
        }
      } else {
        final SimpleExpressionProxy nextDef =
          mPrimedSearchVisitor.getDefiningExpression(constraints);
        if (def == null && nextDef == null) {
          // no functional dependency, try all combinations
          return new FullIterator(comp, constraints, range, range);
        } else if (def != null && nextDef == null) {
          if (mPrimedSearchVisitor.occurs(compIdent, def)) {
            // current state functionally dependent on next state
            return new FunctionallyDependentIterator
              (comp, constraints, def, false);
          } else if (range.contains(def)) {
            // current state fixed, next state undefined
            return new FullIterator(comp, constraints, def, range);
          } else {
            // fixed value not in range
            return null;
          }
        } else if (def == null && nextDef != null) {
          if (mUnprimedSearchVisitor.occurs(compIdent, nextDef)) {
            // next state functionally dependent on current state
            return new FunctionallyDependentIterator
              (comp, constraints, nextDef, true);
          } else if (range.contains(nextDef)) {
            // next state fixed, current state undefined
            return new FullIterator(comp, constraints, range, nextDef);
          } else {
            // fixed value not in range
            return null;
          }
        } else {
          if (mUnprimedSearchVisitor.occurs(compIdent, nextDef)) {
            // next state functionally dependent on current state
            return new FullIterator(comp, constraints, range, nextDef);
          } else if (mPrimedSearchVisitor.occurs(compIdent, def)) {
            // current state functionally dependent on next state
            return new FunctionallyDependentIterator
              (comp, constraints, def, false);
          } else if (range.contains(def) && range.contains(nextDef)) {
            // current and next state fixed separately
            return new FullIterator(comp, constraints, def, nextDef);
          } else {
            // fixed value not in range
            return null;
          }
        }
      }
    }

    @Override
    public boolean isValidTransition(final EFSMComponent comp,
                                     final ConstraintList constraints,
                                     final long transition)
      throws EvalException
    {
      final SimpleExpressionProxy source =
        comp.getTransitionSourceExpression(transition, mFactory);
      final SimpleExpressionProxy target =
        comp.getTransitionTargetExpression(transition, mFactory);
      final ComponentBindingContext context =
        new ComponentBindingContext(comp, source, target);
      return context.eval(constraints);
    }

    //#######################################################################
    //# Data Members
    private final SplitComputer mSplitComputer;
    private final EFAEventNameBuilder mEventNameBuilder;
    private Collection<ConstraintList> mCollectedConstraintLists;
    private Collection<EFSMComponent> mCollectedEFSMComponents;
    private boolean mSubsumptionEnabled;
  }


  //#########################################################################
  //# Inner Class ComponentBindingContext
  private class ComponentBindingContext
    implements BindingContext
  {
    //#######################################################################
    //# Constructor
    private ComponentBindingContext(final EFSMComponent comp)
    {
      mEFSMComponent = comp;
    }

    private ComponentBindingContext(final EFSMComponent comp,
                                    final SimpleExpressionProxy source,
                                    final SimpleExpressionProxy target)
    {
      this(comp);
      mCurrentSourceState = source;
      mCurrentTargetState = target;
    }

    //#######################################################################
    //# Simple Access
    CompiledRange getRange()
    {
      return mEFSMComponent.getRange();
    }

    public SimpleExpressionProxy getCurrentSourceState()
    {
      return mCurrentSourceState;
    }

    public SimpleExpressionProxy getCurrentTargetState()
    {
      return mCurrentTargetState;
    }

    void setCurrentSourceState(final SimpleExpressionProxy expr)
    {
      mCurrentSourceState = expr;
    }

    void setCurrentTargetState(final SimpleExpressionProxy expr)
    {
      mCurrentTargetState = expr;
    }

    boolean eval(final ConstraintList constraints)
      throws EvalException
    {
      for (final SimpleExpressionProxy constraint : constraints.getConstraints()) {
        final SimpleExpressionProxy value =
          mSimpleExpressionCompiler.eval(constraint, this);
        if (!mSimpleExpressionCompiler.getBooleanValue(value)) {
          return false;
        }
      }
      return true;
    }

    //#######################################################################
    //# Interface
    //# net.sourceforge.waters.model.compiler.context.BindingContext
    @Override
    public SimpleExpressionProxy getBoundExpression
      (final SimpleExpressionProxy ident)
    {
      final IdentifierProxy sought = mEFSMComponent.getIdentifier();
      if (mUnprimedSearchVisitor.matches(ident, sought)) {
        return mCurrentSourceState;
      } else if (mPrimedSearchVisitor.matches(ident, sought)) {
        return mCurrentTargetState;
      } else {
        return mRootContext.getBoundExpression(ident);
      }
    }

    @Override
    public boolean isEnumAtom(final IdentifierProxy ident)
    {
      return mRootContext.isEnumAtom(ident);
    }

    @Override
    public ModuleBindingContext getModuleBindingContext()
    {
      return mRootContext.getModuleBindingContext();
    }

    //#######################################################################
    //# Data Members
    private final EFSMComponent mEFSMComponent;
    private SimpleExpressionProxy mCurrentSourceState;
    private SimpleExpressionProxy mCurrentTargetState;
  }


  //#########################################################################
  //# Inner Class AbstractTransitionIterator
  abstract class AbstractTransitionIterator
    extends ComponentBindingContext
    implements EFSMTransitionIterator
  {
    //#######################################################################
    //# Constructor
    private AbstractTransitionIterator
      (final EFSMComponent comp,
       final ConstraintList constraints)
    {
      super(comp);
      mConstraints = constraints;
    }

    //#######################################################################
    //# Interface
    //# net.sourceforge.waters.model.compiler.efsm.TransitionIterator
    @Override
    public boolean advance()
      throws EvalException
    {
      while (advanceOnce()) {
        if (eval(mConstraints)) {
          return true;
        }
      }
      return false;
    }

    //#######################################################################
    //# Auxiliary Methods
    abstract boolean advanceOnce() throws EvalException;

    //#######################################################################
    //# Data Members
    private final ConstraintList mConstraints;
  }


  //#########################################################################
  //# Inner Class FullIterator
  private class FullIterator extends AbstractTransitionIterator
  {
    //#######################################################################
    //# Constructor
    private FullIterator(final EFSMComponent comp,
                         final ConstraintList constraints,
                         final CompiledRange current,
                         final CompiledRange next)
    {
      this(comp,
           constraints,
           current.getValues(),
           next.getValues());
    }

    private FullIterator(final EFSMComponent comp,
                         final ConstraintList constraints,
                         final SimpleExpressionProxy current,
                         final CompiledRange next)
    {
      this(comp,
           constraints,
           Collections.singletonList(current),
           next.getValues());
    }

    private FullIterator(final EFSMComponent comp,
                         final ConstraintList constraints,
                         final CompiledRange current,
                         final SimpleExpressionProxy next)
    {
      this(comp,
           constraints,
           current.getValues(),
           Collections.singletonList(next));
    }

    private FullIterator(final EFSMComponent comp,
                         final ConstraintList constraints,
                         final SimpleExpressionProxy current,
                         final SimpleExpressionProxy next)
    {
      this(comp,
           constraints,
           Collections.singletonList(current),
           Collections.singletonList(next));
    }

    private FullIterator(final EFSMComponent comp,
                         final ConstraintList constraints,
                         final List<? extends SimpleExpressionProxy> current,
                         final List<? extends SimpleExpressionProxy> next)
    {
      super(comp, constraints);
      mCurrentStates = current;
      mNextStates = next;
      mCurrentIter = null;
      mNextIter = null;
    }

    //#######################################################################
    //# Interface
    //# net.sourceforge.waters.model.compiler.efsm.TransitionIterator
    @Override
    public int getEstimatedSize()
    {
      return Math.max(mCurrentStates.size(), mNextStates.size());
    }

    //#######################################################################
    //# Overrides for AbstractTransitionIterator
    @Override
    boolean advanceOnce()
    {
      if (mCurrentIter == null) {
        mCurrentIter = mCurrentStates.iterator();
        mNextIter = mNextStates.iterator();
        if (mCurrentIter.hasNext() && mNextIter.hasNext()) {
          setCurrentSourceState(mCurrentIter.next());
          setCurrentTargetState(mNextIter.next());
          return true;
        } else {
          return false;
        }
      } else if (mNextIter.hasNext()) {
        setCurrentTargetState(mNextIter.next());
        return true;
      } else if (mCurrentIter.hasNext()) {
        setCurrentSourceState(mCurrentIter.next());
        mNextIter = mNextStates.iterator();
        setCurrentTargetState(mNextIter.next());
        return true;
      } else {
        return false;
      }
    }

    //#######################################################################
    //# Data Members
    private final List<? extends SimpleExpressionProxy> mCurrentStates;
    private final List<? extends SimpleExpressionProxy> mNextStates;
    private Iterator<? extends SimpleExpressionProxy> mCurrentIter;
    private Iterator<? extends SimpleExpressionProxy> mNextIter;
  }


  //#########################################################################
  //# Inner Class FunctionallyDependentIterator
  private class FunctionallyDependentIterator extends AbstractTransitionIterator
  {
    //#######################################################################
    //# Constructor
    private FunctionallyDependentIterator(final EFSMComponent comp,
                                          final ConstraintList constraints,
                                          final SimpleExpressionProxy function,
                                          final boolean onCurrent)
    {
      super(comp, constraints);
      mFunctionalDependency = function;
      mOnCurrent = onCurrent;
      final CompiledRange range = comp.getRange();
      final List<? extends SimpleExpressionProxy> list = range.getValues();
      mIterator = list.iterator();
    }

    //#######################################################################
    //# Interface
    //# net.sourceforge.waters.model.compiler.efsm.TransitionIterator
    @Override
    public int getEstimatedSize()
    {
      return getRange().size();
    }

    //#######################################################################
    //# Overrides for AbstractTransitionIterator
    @Override
    boolean advanceOnce()
      throws EvalException
    {
      if (!mIterator.hasNext()) {
        return false;
      } else if (mOnCurrent) {
        final SimpleExpressionProxy source = mIterator.next();
        setCurrentSourceState(source);
        final SimpleExpressionProxy target =
          mSimpleExpressionCompiler.eval(mFunctionalDependency, this);
        setCurrentTargetState(target);
        return true;
      } else {
        final SimpleExpressionProxy target = mIterator.next();
        setCurrentTargetState(target);
        final SimpleExpressionProxy source =
          mSimpleExpressionCompiler.eval(mFunctionalDependency, this);
        setCurrentSourceState(source);
        return true;
      }
    }

    @Override
    boolean eval(final ConstraintList constraints)
      throws EvalException
    {
      final CompiledRange range = getRange();
      if (mOnCurrent) {
        if (!range.contains(getCurrentTargetState())) {
          return false;
        }
      } else {
        if (!range.contains(getCurrentSourceState())) {
          return false;
        }
      }
      return super.eval(constraints);
    }

    //#######################################################################
    //# Data Members
    private final SimpleExpressionProxy mFunctionalDependency;
    private final boolean mOnCurrent;
    private final Iterator<? extends SimpleExpressionProxy> mIterator;
  }


  //#########################################################################
  //# Inner Class Pass3Visitor
  /**
   * The visitor implementing the third pass of EFSM compilation.
   */
  private class Pass3Processor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Constructor
    private Pass3Processor()
    {
      mCloner = new SourceInfoCloner(mFactory, mCompilationInfo);
      mLabelList = new ArrayList<>();
    }

    //#######################################################################
    //# Invocation
    private ModuleProxy process()
      throws VisitorException
    {
      int numEvents = 0;
      for (final EFSMEventDeclaration decl : mEFSMEventDeclarations) {
        numEvents += decl.getNumberOfInstances();
      }
      final List<EventDeclProxy> eventDeclarations = new ArrayList<>(numEvents);
      for (final EFSMEventDeclaration decl : mEFSMEventDeclarations) {
        decl.compile(mFactory, mCompilationInfo, eventDeclarations);
      }

      final int numComponents = 2 * mEFSMComponents.size();
      mComponents = new ArrayList<>(numComponents);
      for (final EFSMComponent comp : mEFSMComponents) {
        mCurrentEFSMComponent = comp;
        final ComponentProxy source = comp.getComponentProxy();
        source.acceptVisitor(this);
      }

      final String name = mInputModule.getName();
      final String comment = mInputModule.getComment();
      return mFactory.createModuleProxy
        (name, comment, null, null, eventDeclarations, null, mComponents);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public EdgeProxy visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      try {
        final NodeProxy source0 = edge.getSource();
        final StateInfo sourceInfo = mNodeMap.get(source0);
        final NodeProxy source1 = sourceInfo.getNode();
        final NodeProxy target0 = edge.getTarget();
        final StateInfo targetInfo = mNodeMap.get(target0);
        final NodeProxy target1 = targetInfo.getNode();
        final int s = sourceInfo.getCode();
        if (mAutomatonVariablesEnabled) {
          final int t = targetInfo.getCode();
          mCurrentTransition = EFSMComponent.getTransitionCode(s, t);
        }
        final LabelBlockProxy block0 = edge.getLabelBlock();
        visitLabelBlockProxy(block0);
        if (mAdditionalEventInstances != null &&
            EFSMComponent.isSelfloop(mCurrentTransition) &&
            !mSelfloops.get(s)) {
          addSelfloopLabels(s);
        }
        final LabelBlockProxy block1 =
          mFactory.createLabelBlockProxy(mLabelList, null);
        final EdgeProxy result = mFactory.createEdgeProxy
          (source1, target1, block1, null, null, null, null);
        mEdgeList.add(result);
        return result;
      } finally {
        mLabelList.clear();
      }
    }

    @Override
    public Object visitIdentifierProxy(final IdentifierProxy ident)
      throws VisitorException
    {
      checkAbortInVisitor();
      final EFSMEventDeclaration decl =
        mEFSMEventDeclarationMap.getByProxy(ident);
      for (final EFSMEventInstance inst : decl.getInstances()) {
        addEventLabel(inst, ident);
      }
      return null;
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
    public SimpleComponentProxy visitSimpleComponentProxy
      (final SimpleComponentProxy comp)
      throws VisitorException
    {
      try {
        final EFSMSimpleComponent simple =
          (EFSMSimpleComponent) mCurrentEFSMComponent;
        mAdditionalEventInstances = simple.getAdditionalEventInstances();
        mCurrentTransition = -1;
        final GraphProxy graph = comp.getGraph();
        final Collection<NodeProxy> nodes = graph.getNodes();
        final int numNodes = nodes.size();
        mNodeList = new ArrayList<>(numNodes);
        mNodeMap = new HashMap<>(numNodes);
        visitCollection(nodes);
        final Collection<EdgeProxy> edges = graph.getEdges();
        final int numEdges = edges.size();
        mEdgeList = new ArrayList<>(numEdges);
        mSelfloops = new BitSet(numNodes);
        visitCollection(edges);
        if (mAdditionalEventInstances != null) {
          for (int s = mSelfloops.nextClearBit(0);
               s < numNodes;
               s = mSelfloops.nextClearBit(s + 1)) {
            try {
              addSelfloopLabels(s);
              if (!mLabelList.isEmpty()) {
                final NodeProxy node = mNodeList.get(s);
                final LabelBlockProxy block =
                  mFactory.createLabelBlockProxy(mLabelList, null);
                final EdgeProxy edge =  mFactory.createEdgeProxy
                  (node, node, block, null, null, null, null);
                mEdgeList.add(edge);
              }
            } finally {
              mLabelList.clear();
            }
          }
        }
        final LabelBlockProxy blocked0 = graph.getBlockedEvents();
        if (blocked0 != null) {
          try {
            mCurrentTransition = -1;
            visitLabelBlockProxy(blocked0);
            mBlockedEvents = mFactory.createLabelBlockProxy(mLabelList, null);
          } finally {
            mLabelList.clear();
          }
        }
        final ComponentKind kind = comp.getKind();
        final boolean deterministic = graph.isDeterministic();
        final Map<String,String> attribs = comp.getAttributes();
        return createSimpleComponent(comp, kind, deterministic, attribs);
      } finally {
        mAdditionalEventInstances = null;
        mNodeList = null;
        mNodeMap = null;
        mBlockedEvents = null;
        mEdgeList = null;
        mSelfloops = null;
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
      final Map<String,String> attribs = node.getAttributes();
      final boolean initial = node.isInitial();
      final SimpleNodeProxy result = mFactory.createSimpleNodeProxy
        (name, props1, attribs, initial, null, null, null);
      final int code = mNodeList.size();
      final StateInfo info = new StateInfo(result, code);
      mNodeList.add(result);
      mNodeMap.put(node, info);
      mCompilationInfo.add(result, node);
      return result;
    }

    @Override
    public SimpleComponentProxy visitVariableComponentProxy
      (final VariableComponentProxy var)
      throws VisitorException
    {
      try {
        createStates(var);
        createEdges(var);
        return createSimpleComponent(var, ComponentKind.PLANT, false, null);
      } finally {
        mNodeList = null;
        mBlockedEvents = null;
        mEdgeList = null;
      }
    }

    //#######################################################################
    //# Simple Component Compilation
    private void addSelfloopLabels(final int s)
    {
      mCurrentTransition = EFSMComponent.getTransitionCode(s, s);
      mSelfloops.set(s);
      for (final EFSMEventInstance inst : mAdditionalEventInstances) {
        addEventLabel(inst, null);
      }
    }

    private void addEventLabel(final EFSMEventInstance inst,
                               final IdentifierProxy location)
    {
      if (mCurrentTransition < 0 ||
          mCurrentEFSMComponent.isValidTransition(inst, mCurrentTransition)) {
        final IdentifierProxy compiled = inst.createIdentifier(mFactory);
        mLabelList.add(compiled);
        if (location != null) {
          mCompilationInfo.add(compiled, location);
        }
      }
    }


    //#######################################################################
    //# Variable Compilation
    private void createStates(final VariableComponentProxy var)
      throws VisitorException
    {
      final IdentifierProxy varName = var.getIdentifier();
      final SimpleExpressionProxy initPred = var.getInitialStatePredicate();
      final List<VariableMarkingProxy> markings = var.getVariableMarkings();
      final int numMarkings = markings.size();
      final Set<IdentifierProxy> blocked = new THashSet<>(numMarkings);
      for (final VariableMarkingProxy marking : markings) {
        final IdentifierProxy prop = marking.getProposition();
        blocked.add(prop);
      }
      final List<IdentifierProxy> props = new ArrayList<>(numMarkings);
      final CompiledRange range = mCurrentEFSMComponent.getRange();
      final int rangeSize = range.size();
      mNodeList = new ArrayList<>(rangeSize);
      for (final SimpleExpressionProxy value : range.getValues()) {
        checkAbortInVisitor();
        final String name = value.toString();
        final BindingContext context =
          new SingleBindingContext(varName, value, mRootContext);
        boolean initial = false;
        try {
          final SimpleExpressionProxy initVal =
            mSimpleExpressionCompiler.eval(initPred, context);
          initial = mSimpleExpressionCompiler.getBooleanValue(initVal);
        } catch (final EvalException exception) {
          mCompilationInfo.raiseInVisitor(exception);
        }
        for (final VariableMarkingProxy marking : markings) {
          try {
            final SimpleExpressionProxy pred = marking.getPredicate();
            final SimpleExpressionProxy predVal =
              mSimpleExpressionCompiler.eval(pred, context);
            if (mSimpleExpressionCompiler.getBooleanValue(predVal)) {
              final IdentifierProxy prop = marking.getProposition();
              props.add(prop);
              blocked.remove(prop);
            }
          } catch (final EvalException exception) {
            mCompilationInfo.raiseInVisitor(exception);
          }
        }
        final PlainEventListProxy elist =
          props.isEmpty() ? null : mFactory.createPlainEventListProxy(props);
        final SimpleNodeProxy node = mFactory.createSimpleNodeProxy
          (name, elist, null, initial, null, null, null);
        mNodeList.add(node);
        props.clear();
      }
      if (!blocked.isEmpty()) {
        mBlockedEvents = mFactory.createLabelBlockProxy(blocked, null);
      }
    }

    private void createEdges(final VariableComponentProxy var)
    {
      final TLongObjectMap<List<EFSMEventInstance>> map =
        mCurrentEFSMComponent.getAllTransitions();
      final long[] transitions = map.keys();
      Arrays.sort(transitions);
      mEdgeList = new ArrayList<>(transitions.length);
      for (final long transition : transitions) {
        final int s = EFSMComponent.getTransitionSource(transition);
        final NodeProxy source = mNodeList.get(s);
        final int t = EFSMComponent.getTransitionTarget(transition);
        final NodeProxy target = mNodeList.get(t);
        final List<EFSMEventInstance> events = map.get(transition);
        final List<IdentifierProxy> idents = new ArrayList<>(events.size());
        for (final EFSMEventInstance event : events) {
          final IdentifierProxy ident = event.createIdentifier(mFactory);
          idents.add(ident);
        }
        final LabelBlockProxy block =
          mFactory.createLabelBlockProxy(idents, null);
        final EdgeProxy edge =
          mFactory.createEdgeProxy(source, target, block, null, null, null, null);
        mEdgeList.add(edge);
      }
    }

    private SimpleComponentProxy createSimpleComponent
      (final ComponentProxy source,
       final ComponentKind kind,
       final boolean deterministic,
       final Map<String,String> attribs)
    {
      final GraphProxy graph = mFactory.createGraphProxy
        (deterministic, mBlockedEvents, mNodeList, mEdgeList);
      final IdentifierProxy ident = source.getIdentifier();
      final IdentifierProxy iclone = (IdentifierProxy) mCloner.getClone(ident);
      final SimpleComponentProxy compiled =
        mFactory.createSimpleComponentProxy(iclone, kind, graph, attribs);
      mComponents.add(compiled);
      mCompilationInfo.add(compiled, source);
      return compiled;
    }

    //#######################################################################
    //# Data Members
    private final ModuleProxyCloner mCloner;
    private final List<IdentifierProxy> mLabelList;

    private List<SimpleComponentProxy> mComponents;
    private EFSMComponent mCurrentEFSMComponent;
    private List<EFSMEventInstance> mAdditionalEventInstances;
    private List<SimpleNodeProxy> mNodeList;
    private Map<SimpleNodeProxy,StateInfo> mNodeMap;
    private LabelBlockProxy mBlockedEvents;
    private List<EdgeProxy> mEdgeList;
    private BitSet mSelfloops;
    private long mCurrentTransition;
  }


  //#########################################################################
  //# Inner Class StateInfo
  private static class StateInfo
  {
    //#######################################################################
    //# Constructor
    private StateInfo(final SimpleNodeProxy node, final int code)
    {
      mNode = node;
      mCode = code;
    }

    //#######################################################################
    //# Simple Access
    private SimpleNodeProxy getNode()
    {
      return mNode;
    }

    private int getCode()
    {
      return mCode;
    }

    //#######################################################################
    //# Data Members
    private final SimpleNodeProxy mNode;
    private final int mCode;
  }


  //#########################################################################
  //# Inner Class SearchVisitor
  private abstract class AbstractSearchVisitor
    extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Simple Access
    IdentifierProxy getSoughtIdentifier()
    {
      return mSoughtIdentifier;
    }

    //#######################################################################
    //# Invocation
    boolean occurs(final IdentifierProxy sought,
                   final ConstraintList constraints)
    {
      return occurs(sought, constraints.getConstraints());
    }

    boolean occurs(final IdentifierProxy sought,
                   final Collection<? extends SimpleExpressionProxy> constraints)
    {
      mSoughtIdentifier = sought;
      for (final SimpleExpressionProxy expr : constraints) {
        if (occursIn(expr)) {
          return true;
        }
      }
      return false;
    }

    boolean occurs(final IdentifierProxy sought,
                   final SimpleExpressionProxy expr)
    {
      mSoughtIdentifier = sought;
      return occursIn(expr);
    }

    SimpleExpressionProxy getDefiningExpression(final ConstraintList constraints)
    {
      for (final SimpleExpressionProxy expr : constraints.getConstraints()) {
        if (expr instanceof BinaryExpressionProxy) {
          final BinaryExpressionProxy binary = (BinaryExpressionProxy) expr;
          if (binary.getOperator() == mOperatorTable.getEqualsOperator()) {
            final SimpleExpressionProxy left = binary.getLeft();
            final SimpleExpressionProxy right = binary.getLeft();
            if (matches(left)) {
              if (!occursIn(right)) {
                return right;
              }
            } else if (matches(right)) {
              if (!occursIn(left)) {
                return left;
              }
            }
          }
        }
      }
      return null;
    }

    boolean matches(final SimpleExpressionProxy expr,
                    final IdentifierProxy sought)
    {
      mSoughtIdentifier = sought;
      return matches(expr);
    }

    //#######################################################################
    //# Auxiliary Methods
    boolean occursIn(final SimpleExpressionProxy expr)
    {
      try {
        return (boolean) expr.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    abstract boolean matches(SimpleExpressionProxy exor);

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy left = expr.getLeft();
      final Boolean result = (Boolean) left.acceptVisitor(this);
      if (result) {
        return result;
      }
      final SimpleExpressionProxy right = expr.getRight();
      return right.acceptVisitor(this);
    }

    @Override
    public Boolean visitFunctionCallExpressionProxy
      (final FunctionCallExpressionProxy expr)
      throws VisitorException
    {
      for (final SimpleExpressionProxy arg : expr.getArguments()) {
        final Boolean result = (Boolean) arg.acceptVisitor(this);
        if (result) {
          return result;
        }
      }
      return false;
    }

    @Override
    public Boolean visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
    {
      return false;
    }

    @Override
    public Object visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy subTerm = expr.getSubTerm();
      return subTerm.acceptVisitor(this);
    }

    //#######################################################################
    //# Data Members
    private IdentifierProxy mSoughtIdentifier;
  }


  //#########################################################################
  //# Inner Class PrimedSearchVisitor
  private class PrimedSearchVisitor extends AbstractSearchVisitor
  {
    //#######################################################################
    //# Auxiliary Methods
    @Override
    boolean matches(final SimpleExpressionProxy expr)
    {
      if (expr instanceof UnaryExpressionProxy) {
        final UnaryExpressionProxy unary = (UnaryExpressionProxy) expr;
        if (unary.getOperator() == mOperatorTable.getNextOperator()) {
          final IdentifierProxy sought = getSoughtIdentifier();
          final SimpleExpressionProxy subTerm = unary.getSubTerm();
          return mEquality.equals(sought, subTerm);
        }
      }
      return false;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy subTerm = expr.getSubTerm();
      if (expr.getOperator() == mOperatorTable.getNextOperator()) {
        final IdentifierProxy sought = getSoughtIdentifier();
        return mEquality.equals(sought, subTerm);
      } else {
        return subTerm.acceptVisitor(this);
      }
    }
  }


  //#########################################################################
  //# Inner Class UnprimedSearchVisitor
  private class UnprimedSearchVisitor extends AbstractSearchVisitor
  {
    //#######################################################################
    //# Auxiliary Methods
    @Override
    boolean matches(final SimpleExpressionProxy expr)
    {
      final IdentifierProxy sought = getSoughtIdentifier();
      return mEquality.equals(sought, expr);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Boolean visitIdentifierProxy(final IdentifierProxy ident)
    {
      return matches(ident);
    }

    @Override
    public Object visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
      throws VisitorException
    {
      if (expr.getOperator() == mOperatorTable.getNextOperator()) {
        return false;
      } else {
        return super.visitUnaryExpressionProxy(expr);
      }
    }
  }


  //#########################################################################
  //# Data Members
  // Configuration
  private final ModuleProxy mInputModule;
  private boolean mAutomatonVariablesEnabled = false;

  // Data structures
  private EFSMModuleContext mRootContext;
  /**
   * A map that assigns to each identifier of an event declaration to an
   * information record that contains the guard/action block and other
   * relevant information about the compilation of the event.
   */
  private ProxyAccessorMap<IdentifierProxy,EFSMEventDeclaration>
    mEFSMEventDeclarationMap;
  /**
   * The list of all EFSM declarations. Contains the values in the
   * {@link #mEFSMEventDeclarationMap} in the order in which their event declarations
   * appear in the input module.
   */
  private List<EFSMEventDeclaration> mEFSMEventDeclarations;
  /**
   * The list of all EFSM components to be generated. Contains an entry
   * for each {@link SimpleComponentProxy} and each {@link
   * VariableComponentProxy} in the order in which they appear in the input
   * module.
   */
  private List<EFSMComponent> mEFSMComponents;

  // Tools
  private final ModuleProxyFactory mFactory;
  private final ModuleEqualityVisitor mEquality;
  private final CompilationInfo mCompilationInfo;
  private final CompilerOperatorTable mOperatorTable;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;

  private final PrimedSearchVisitor mPrimedSearchVisitor =
    new PrimedSearchVisitor();
  private final UnprimedSearchVisitor mUnprimedSearchVisitor =
    new UnprimedSearchVisitor();


  //#########################################################################
  //# Class Constants
  /**
   * The name of the ":updates" automaton generated by the
   * {@link EFSMNormaliser}.
   */
  public static final String NAME_UPDATES = ":updates";
  /**
   * The attribute key used to define the ":updates" automaton generated
   * by the {@link EFSMNormaliser}.
   */
  public static final String ATTRIB_UPDATES = "EFSM:Updates";
  /**
   * An unmodifiable attribute map that identifies a simple component as
   * the ":updates" automaton generated by the {@link EFSMNormaliser}.
   * It contains a single entry mapping the key {@link #ATTRIB_UPDATES}
   * to the empty string.
   */
  public static final Map<String,String> ATTRIBUTES_UPDATES =
    Collections.singletonMap(ATTRIB_UPDATES, "");

}
