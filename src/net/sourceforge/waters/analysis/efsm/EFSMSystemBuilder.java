//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efsm
//# CLASS:   EFSMSystemBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.constraint.ConstraintPropagator;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfoBuilder;
import net.sourceforge.waters.model.compiler.efa.EFAGuardCompiler;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * <P>
 * A compiler to convert an instantiated module ({@link ModuleProxy}) into and
 * EFSM system ({@link EFSMSystem}).
 * </P>
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class EFSMSystemBuilder extends AbstractEFSMAlgorithm
{

  //#########################################################################
  //# Constructors
  public EFSMSystemBuilder(final ModuleProxyFactory factory,
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
    final String moduleName = module.getName();
    mVariableContext = new EFSMVariableContext(module, mOperatorTable);
    mMarkedVariables = new ArrayList<EFSMVariable>();
    mVariableMarkingPredicates = new ArrayList<SimpleExpressionProxy>();
    final int size = module.getComponentList().size();
    mResultEFSMSystem = new EFSMSystem(moduleName, mVariableContext, size);
  }


  //#########################################################################
  //# Invocation
  public EFSMSystem compile()
    throws EvalException, AnalysisException
  {
    try {
      setUp();
      // Pass 1 ...
      final Pass1Visitor pass1 = new Pass1Visitor();
      mInputModule.acceptVisitor(pass1);
      // Pass 2 ...
      final Pass2Visitor pass2 = new Pass2Visitor();
      mInputModule.acceptVisitor(pass2);
      // Pass 3 ...
      final Pass3Visitor pass3 = new Pass3Visitor();
      mInputModule.acceptVisitor(pass3);
      createMarkingTR();
      return mResultEFSMSystem;
    } catch (final VisitorException exception) {
      final Throwable cause = exception.getCause();
      if (cause instanceof EvalException) {
        throw (EvalException) cause;
      } else if (cause instanceof AnalysisException) {
        throw (AnalysisException) cause;
      } else {
        throw exception.getRuntimeException();
      }
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Configuration
  public boolean isOptimizationEnabled()
  {
    return mIsOptimizationEnabled;
  }

  public void setOptimizationEnabled(final boolean enable)
  {
    mIsOptimizationEnabled = enable;
  }

  public void setConfiguredDefaultMarking(final IdentifierProxy marking)
  {
    mDefaultMarking = marking;
  }

  public IdentifierProxy getConfiguredDefaultMarking()
  {
    return mDefaultMarking;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void createMarkingTR()
    throws AnalysisAbortException
  {
    if (!mMarkedVariables.isEmpty()) {
      try {
        checkAbort();
        final EFSMEventEncoding eventEncoding = new EFSMEventEncoding(2);
        final ConstraintList markingUpdate =
          new ConstraintList(mVariableMarkingPredicates);
        final int event = eventEncoding.createEventId(markingUpdate);
        final String name = ":marking:" + mDefaultMarking.toString();
        final ListBufferTransitionRelation rel =
          new ListBufferTransitionRelation
            (name, ComponentKind.PLANT, 2, 1, 2,
             ListBufferTransitionRelation.CONFIG_SUCCESSORS);
        rel.setInitial(0, true);
        rel.setMarked(1, 0, true);
        rel.addTransition(0, event, 1);
        final EFSMTransitionRelation efsmTR =
          new EFSMTransitionRelation(rel, eventEncoding, mMarkedVariables);
        mResultEFSMSystem.addTransitionRelation(efsmTR);
        efsmTR.register();
      } catch (final OverflowException exception) {
        throw new WatersRuntimeException(exception);
      }
    }
  }


  //#########################################################################
  //# Inner Class Pass1Visitor
  private class Pass1Visitor extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Constructor
    private Pass1Visitor()
    {
      final ModuleEqualityVisitor eq =
        ModuleEqualityVisitor.getInstance(false);
      mGlobalEventsMap =
        new ProxyAccessorHashMap<IdentifierProxy,SimpleComponentProxy>(eq);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public List<SimpleIdentifierProxy> visitGraphProxy(final GraphProxy graph)
      throws VisitorException
    {
      final LabelBlockProxy bEvents = graph.getBlockedEvents();
      final Collection<EdgeProxy> edges = graph.getEdges();
      if (bEvents != null) {
        visitLabelBlockProxy(bEvents);
      }
      visitCollection(edges);
      return null;
    }

    @Override
    public Object visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      final LabelBlockProxy label = edge.getLabelBlock();
      return visitLabelBlockProxy(label);
    }

    @Override
    public Object visitLabelBlockProxy(final LabelBlockProxy label)
      throws VisitorException
    {
      final List<Proxy> list = label.getEventIdentifierList();
      return visitCollection(list);
    }

    @Override
    public Object visitIdentifierProxy(final IdentifierProxy ident)
      throws VisitorException
    {
      checkAbortInVisitor();
      final SimpleComponentProxy comp = mGlobalEventsMap.getByProxy(ident);
      if (comp == null) {
        mGlobalEventsMap.putByProxy(ident, mCurrentComponent);
      } else if (comp != mCurrentComponent) {
        final SharedEventException exception =
          new SharedEventException(ident, comp, mCurrentComponent);
        throw wrap(exception);
      }
      return null;
    }

    @Override
    public Object visitModuleProxy(final ModuleProxy module)
      throws VisitorException
    {
      final List<Proxy> components = module.getComponentList();
      return visitCollection(components);
    }

    @Override
    public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
      throws VisitorException
    {
      mCurrentComponent = comp;
      final GraphProxy graph = comp.getGraph();
      return visitGraphProxy(graph);
    }

    @Override
    public EFSMVariable visitVariableComponentProxy
      (final VariableComponentProxy var)
    {
      return null;
    }

    //#######################################################################
    //# Data Members
    private final ProxyAccessorMap<IdentifierProxy,SimpleComponentProxy> mGlobalEventsMap;
    private SimpleComponentProxy mCurrentComponent;
  }


  //#########################################################################
  //# Inner Class Pass2Visitor
  /**
   * The visitor implementing the second pass of EFA compilation.
   */
  private class Pass2Visitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitEnumSetExpressionProxy(final EnumSetExpressionProxy expr)
      throws VisitorException
    {
      try {
        for (final SimpleIdentifierProxy ident : expr.getItems()) {
          mVariableContext.insertEnumAtom(ident);
        }
        return null;
      } catch (final DuplicateIdentifierException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public Object visitModuleProxy(final ModuleProxy module)
      throws VisitorException
    {
      final List<Proxy> components = module.getComponentList();
      return visitCollection(components);
    }

    @Override
    public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
    {
      return null;
    }

    @Override
    public Object visitSimpleExpressionProxy(final SimpleExpressionProxy simple)
    {
      return null;
    }

    @Override
    public EFSMVariable visitVariableComponentProxy
      (final VariableComponentProxy var)
      throws VisitorException
    {
      try {
        checkAbortInVisitor();
        final SimpleExpressionProxy type = var.getType();
        type.acceptVisitor(this);
        final SimpleExpressionProxy value =
          mSimpleExpressionCompiler.eval(type, null);
        final CompiledRange range =
          mSimpleExpressionCompiler.getRangeValue(value);
        mCurrentVariable =
          new EFSMVariable(var, range, mFactory, mOperatorTable);
        mVariableContext.addVariable(mCurrentVariable);
        mResultEFSMSystem.addVariable(mCurrentVariable);
        visitCollection(var.getVariableMarkings());
        return mCurrentVariable;
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public EFSMVariable visitVariableMarkingProxy
      (final VariableMarkingProxy marking)
    {
      final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
      final IdentifierProxy prop = marking.getProposition();
      if (eq.equals(prop, mDefaultMarking)) {
        final SimpleExpressionProxy pred = marking.getPredicate();
        mVariableMarkingPredicates.add(pred);
        mMarkedVariables.add(mCurrentVariable);
      }
      return null;
    }

    //#######################################################################
    //# Data Members
    private EFSMVariable mCurrentVariable;
  }


  //#########################################################################
  //# Inner Class Pass3Visitor
  /**
   * The visitor implementing the third pass of EFA compilation.
   */
  private class Pass3Visitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Constructor
    private Pass3Visitor()
    {
      mGuardCompiler = new EFAGuardCompiler(mFactory, mOperatorTable);
      mConstraintPropagator =
        new ConstraintPropagator(mFactory, mOperatorTable, mVariableContext);
      mEFSMVariableCollector = new EFSMVariableCollector(mOperatorTable, mVariableContext);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitModuleProxy(final ModuleProxy module)
      throws VisitorException
    {
      final List<Proxy> components = module.getComponentList();
      return visitCollection(components);
    }

    @Override
    public EFSMTransitionRelation visitSimpleComponentProxy
      (final SimpleComponentProxy comp)
      throws VisitorException
    {
      try {
        final GraphProxy graph = comp.getGraph();
        visitGraphProxy(graph);
        ListBufferTransitionRelation rel = createTransitionRelation(comp);
        if (mIsOptimizationEnabled) {
          rel.removeRedundantPropositions();
        }
        if (rel.isUsedProposition(0) && !mMarkedVariables.isEmpty()) {
          // Throw an exception because markings in variables and
          // automata together are not yet supported.
          final EFSMVariable var = mMarkedVariables.get(0);
          final SharedEventException exception =
            new SharedEventException(mDefaultMarking, comp, var.getComponent());
          throw new VisitorException(exception);
        }
        if (mIsOptimizationEnabled) {
          final boolean hasUnreachableStates = rel.checkReachability();
          if (hasUnreachableStates) {
            final int newNumStates = rel.getNumberOfReachableStates();
            final TObjectIntHashMap<SimpleNodeProxy> newStateMap =
              new TObjectIntHashMap<SimpleNodeProxy>(newNumStates,
                                                     0.5f, -1);
            final List<SimpleNodeProxy> newNodeList;
            if (mNodeList == null) {
              newNodeList = null;
            } else {
              newNodeList = new ArrayList<SimpleNodeProxy>(newNumStates);
            }
            int newCode = 0;
            for (final NodeProxy node : graph.getNodes()) {
              if (node instanceof SimpleNodeProxy) {
                final SimpleNodeProxy simple = (SimpleNodeProxy)node;
                final int oldCode = mStateMap.get(simple);
                if (rel.isReachable(oldCode)) {
                  newStateMap.put(simple, newCode);
                  newCode ++;
                  if (newNodeList != null) {
                    newNodeList.add(simple);
                  }
                }
              }
            }
            final int oldNumEvents= mEventEncoding.size();
            int newNumEvents = 1;
            final boolean[] usedEvents = new boolean[oldNumEvents];
            usedEvents[EventEncoding.TAU] = true;
            final TransitionIterator iter =
              rel.createAllTransitionsReadOnlyIterator();
            while (iter.advance()) {
              checkAbortInVisitor();
              final int currentEvent = iter.getCurrentEvent();
              if (!usedEvents[currentEvent]) {
                newNumEvents ++;
                usedEvents[currentEvent] = true;
              }
            }
            final EFSMEventEncoding newEncoding =
              new EFSMEventEncoding(newNumEvents);
            for (int i = 0; i < oldNumEvents; i++) {
              if (usedEvents[i]) {
                final ConstraintList update = mEventEncoding.getUpdate(i);
                newEncoding.createEventId(update);
              }
            }
            mEventEncoding = newEncoding;
            mStateMap = newStateMap;
            mNodeList = newNodeList;
            rel = createTransitionRelation(comp);
            rel.removeRedundantPropositions();
          }
        }
        final Collection<EFSMVariable> variables = new THashSet<EFSMVariable>();
        mEFSMVariableCollector.collectAllVariables(mEventEncoding, variables);
        final EFSMTransitionRelation efsmTransitionRelation =
          new EFSMTransitionRelation(rel, mEventEncoding, variables, mNodeList);
        efsmTransitionRelation.register();
        mResultEFSMSystem.addTransitionRelation(efsmTransitionRelation);
        return efsmTransitionRelation;
      } catch (final AnalysisException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public Object visitGraphProxy(final GraphProxy graph)
      throws VisitorException
    {
      mUsesMarking = false;
      final LabelBlockProxy block = graph.getBlockedEvents();
      if (block != null) {
        mUsesMarking = containsMarkingProposition(block);
      }
      final Collection<NodeProxy> nodes = graph.getNodes();
      mStateMap =
        new TObjectIntHashMap<SimpleNodeProxy>(nodes.size(), 0.5f, -1);
      if (mSourceInfoBuilder != null) {
        mNodeList = new ArrayList<SimpleNodeProxy>(nodes.size());
      } else {
        mNodeList = null;
      }
      visitCollection(nodes);
      final ModuleEqualityVisitor eq =
        ModuleEqualityVisitor.getInstance(false);
      final Collection<EdgeProxy> edges = graph.getEdges();
      mSimplifiedGuardActionBlockMap =
        new ProxyAccessorHashMap<GuardActionBlockProxy,ConstraintList>
          (eq, edges.size());
      mEventEncoding = new EFSMEventEncoding(edges.size());
      return visitCollection(edges);
    }

    @Override
    public Object visitSimpleNodeProxy(final SimpleNodeProxy node)
    {
      final int code = mStateMap.size();
      mStateMap.put(node, code);
      if (mNodeList != null) {
        mNodeList.add(node);
      }
      final EventListExpressionProxy props = node.getPropositions();
      if (containsMarkingProposition(props)) {
        mUsesMarking = true;
      }
      return null;
    }

    @Override
    public Object visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      checkAbortInVisitor();
      final GuardActionBlockProxy update = edge.getGuardActionBlock();
      if (update == null) {
        mSimplifiedGuardActionBlockMap.putByProxy(update, mTrueGuard);
        mEventEncoding.createEventId(mTrueGuard);
      } else {
        visitGuardActionBlockProxy(update);
      }
      return null;
    }

    @Override
    public Object visitGuardActionBlockProxy(final GuardActionBlockProxy update)
      throws VisitorException
    {
      try {
        final ConstraintList list = mGuardCompiler.getCompiledGuard(update);
        mConstraintPropagator.init(list);
        mConstraintPropagator.propagate();
        if (!mConstraintPropagator.isUnsatisfiable()) {
          final ConstraintList allConstraints =
            mConstraintPropagator.getAllConstraints();
          mSimplifiedGuardActionBlockMap.putByProxy(update, allConstraints);
          mEventEncoding.createEventId(allConstraints);
        }
        return null;
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public EFSMVariable visitVariableComponentProxy
      (final VariableComponentProxy var)
    {
      return null;
    }

    //#######################################################################
    //# Auxiliary Methods
    private boolean containsMarkingProposition
      (final EventListExpressionProxy list)
    {
      final ModuleEqualityVisitor eq =
        ModuleEqualityVisitor.getInstance(false);
      return eq.contains(list.getEventIdentifierList(), mDefaultMarking);
    }

    private ListBufferTransitionRelation createTransitionRelation
      (final SimpleComponentProxy comp)
      throws AnalysisException
    {
      final GraphProxy graph = comp.getGraph();
      final String name = comp.getName();
      final int eventSize = mEventEncoding.size();
      final ComponentKind kind = comp.getKind();
      final int numProps = mUsesMarking ? 1 : 0;
      final ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(name,
                                         kind,
                                         eventSize,
                                         numProps,
                                         mStateMap.size(),
                                         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      final TObjectIntIterator<SimpleNodeProxy> iter = mStateMap.iterator();
      while (iter.hasNext()) {
        iter.advance();
        final SimpleNodeProxy node = iter.key();
        final int code = iter.value();
        if (node.isInitial()) {
          rel.setInitial(code, true);
        }
        if (mUsesMarking &&
            containsMarkingProposition(node.getPropositions())) {
          rel.setMarked(code, 0, true);
        }
      }
      final Collection<EdgeProxy> edges = graph.getEdges();
      for (final EdgeProxy edge : edges) {
        checkAbort();
        final GuardActionBlockProxy update = edge.getGuardActionBlock();
        final ConstraintList simplifiedList =
          mSimplifiedGuardActionBlockMap.getByProxy(update);
        if (simplifiedList == null) {
          continue;
        }
        final SimpleNodeProxy source = (SimpleNodeProxy) edge.getSource();
        final int sourceState = mStateMap.get(source);
        if (sourceState < 0) {
          continue;
        }
        final int event = mEventEncoding.getEventId(simplifiedList);
        if (event < 0) {
          continue;
        }
        final SimpleNodeProxy target = (SimpleNodeProxy) edge.getTarget();
        final int targetState = mStateMap.get(target);
        rel.addTransition(sourceState, event, targetState);
      }
      return rel;
    }

    //#######################################################################
    //# Data Members
    private final EFAGuardCompiler mGuardCompiler;
    private final ConstraintPropagator mConstraintPropagator;
    private EFSMEventEncoding mEventEncoding;
    private ProxyAccessorMap<GuardActionBlockProxy,ConstraintList>
      mSimplifiedGuardActionBlockMap;
    private TObjectIntHashMap<SimpleNodeProxy> mStateMap;
    private List<SimpleNodeProxy> mNodeList;
    private boolean mUsesMarking;
    private final EFSMVariableCollector mEFSMVariableCollector;
  }

  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final SourceInfoBuilder mSourceInfoBuilder;
  private final CompilerOperatorTable mOperatorTable;
  private final ConstraintList mTrueGuard;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final ModuleProxy mInputModule;
  private IdentifierProxy mDefaultMarking;
  private boolean mIsOptimizationEnabled;

  private final EFSMVariableContext mVariableContext;
  private final List<EFSMVariable> mMarkedVariables;
  private final List<SimpleExpressionProxy> mVariableMarkingPredicates;
  private final EFSMSystem mResultEFSMSystem;

}
