//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFACompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import gnu.trove.TObjectIntHashMap;
import gnu.trove.TObjectIntIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.constraint.ConstraintPropagator;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.ModuleBindingContext;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfoBuilder;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.compiler.efa.EFAGuardCompiler;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
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
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


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

public class EFSMCompiler
{

  //#########################################################################
  //# Constructors
  public EFSMCompiler(final ModuleProxyFactory factory,
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
    final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
    mModuleContext = new ModuleBindingContext(module);
    mVariableContext = new EFSMVariableContext();
    mGlobalVariableMap =
      new ProxyAccessorHashMap<IdentifierProxy,EFSMVariable>(eq);
    final int size = module.getComponentList().size();
    mTransitionRelations = new ArrayList<EFSMTransitionRelation>(size);
    mVariables = new ArrayList<EFSMVariable>(size);
  }


  //#########################################################################
  //# Invocation
  public EFSMSystem compile()
    throws EvalException
  {
    try {
      // Pass 1 ...
      final Pass1Visitor pass1 = new Pass1Visitor();
      mInputModule.acceptVisitor(pass1);
      // Pass 2 ...
      final Pass2Visitor pass2 = new Pass2Visitor();
      mInputModule.acceptVisitor(pass2);
      // Pass 3 ...
      final Pass3Visitor pass3 = new Pass3Visitor();
      mInputModule.acceptVisitor(pass3);
      return new EFSMSystem(mVariables, mTransitionRelations);
    } catch (final VisitorException exception) {
      final Throwable cause = exception.getCause();
      if (cause instanceof EvalException) {
        throw (EvalException) cause;
      } else {
        throw exception.getRuntimeException();
      }
    }
  }


  //#########################################################################
  //# Configuration


  //#########################################################################
  //# Auxiliary Methods


  //#########################################################################
  //# Inner Class Pass1Visitor
  private class Pass1Visitor extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Constructor
    private Pass1Visitor()
    {
      final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
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
      visitLabelBlockProxy(bEvents);
      visitCollection(edges);
      return null;
    }

    @Override
    public Object visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      final LabelBlockProxy label = edge.getLabelBlock();
      visitLabelBlockProxy(label);
      return null;
    }

    @Override
    public Object visitLabelBlockProxy(final LabelBlockProxy label)
      throws VisitorException
    {
      final List<Proxy> list = label.getEventIdentifierList();
      visitCollection(list);
      return null;
    }

    @Override
    public Object visitIdentifierProxy(final IdentifierProxy ident)
      throws VisitorException
    {
      final SimpleComponentProxy comp = mGlobalEventsMap.get(ident);
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
      visitCollection(components);
      return null;
    }

    @Override
    public CompiledRange visitSimpleComponentProxy
      (final SimpleComponentProxy comp)
      throws VisitorException
    {
      mCurrentComponent = comp;
      final GraphProxy graph = comp.getGraph();
      visitGraphProxy(graph);
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
    public Object visitModuleProxy(final ModuleProxy module)
      throws VisitorException
    {
      final List<Proxy> components = module.getComponentList();
      visitCollection(components);
      return null;
    }

    @Override
    public EFSMVariable visitVariableComponentProxy
      (final VariableComponentProxy var)
    throws VisitorException
    {
      try {
        final SimpleExpressionProxy type = var.getType();
        visitSimpleExpressionProxy(type);
        final SimpleExpressionProxy value =
          mSimpleExpressionCompiler.eval(type, null);
        final CompiledRange range =
          mSimpleExpressionCompiler.getRangeValue(value);
        final EFSMVariable EFSMvar= new EFSMVariable(var, range);
        final IdentifierProxy ident = var.getIdentifier();
        mGlobalVariableMap.putByProxy(ident, EFSMvar);
        mVariables.add(EFSMvar);
        return EFSMvar;
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public Object visitEnumSetExpressionProxy(final EnumSetExpressionProxy expr)
      throws VisitorException
    {
      try {
        for (final SimpleIdentifierProxy ident: expr.getItems()) {
          mModuleContext.insertEnumAtom(ident);
        }
        return null;
      } catch (final DuplicateIdentifierException exception) {
        throw wrap(exception);
      }
    }
  }


  //#########################################################################
  //# Inner Class Pass3Visitor
  /**
   * The visitor implementing the second pass of EFA compilation.
   */
  private class Pass3Visitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Constructor
    private Pass3Visitor()
    {
      mGuardCompiler = new EFAGuardCompiler(mFactory, mOperatorTable);
      mConstraintPropagator =new ConstraintPropagator
        (mFactory, mOperatorTable, mVariableContext);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitModuleProxy(final ModuleProxy module)
      throws VisitorException
    {
      final List<Proxy> components = module.getComponentList();
      visitCollection(components);
      return null;
    }

    @Override
    public Object visitSimpleComponentProxy
      (final SimpleComponentProxy comp)
    throws VisitorException
    {
      try {
        final GraphProxy graph = comp.getGraph();
        visitGraphProxy(graph);
        final String name = comp.getName();
        final int eventSize = mEventEncoding.size();
        final ComponentKind kind = comp.getKind();
        final int numProps = mUsesMarking ? 1 : 0;
        final ListBufferTransitionRelation rel =
          new ListBufferTransitionRelation(name, kind, eventSize,
                                           numProps, mStateMap.size(),
                                           ListBufferTransitionRelation.CONFIG_SUCCESSORS);
        final TObjectIntIterator<SimpleNodeProxy> iter = mStateMap.iterator();
        while(iter.hasNext()) {
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
        final Collection <EdgeProxy> edges = graph.getEdges();
        for(final EdgeProxy edge : edges) {
          final GuardActionBlockProxy gABlock = edge.getGuardActionBlock();
          final ConstraintList simplifiedList = mSimplifiedGuardActionBlockMap.get(gABlock);
          final int event = mEventEncoding.getEventId(simplifiedList);
          final SimpleNodeProxy source = (SimpleNodeProxy) edge.getSource();
          final int sourceState = mStateMap.get(source);
          final SimpleNodeProxy target = (SimpleNodeProxy) edge.getTarget();
          final int targetState = mStateMap.get(target);
          rel.addTransition(sourceState, event, targetState);
        }
        final EFSMTransitionRelation efsmTransitionRelation =
          new EFSMTransitionRelation (rel, mEventEncoding);
        mTransitionRelations.add(efsmTransitionRelation);
      } catch (final OverflowException exception) {
        throw wrap(exception);
      }
      return null;
    }


    @Override
    public Object visitGraphProxy(final GraphProxy graph)
      throws VisitorException
    {
      mUsesMarking = false;
      final LabelBlockProxy block = graph.getBlockedEvents();
      if (block!=null) {
        mUsesMarking = containsMarkingProposition(block);
      }
      final Collection<NodeProxy> nodes = graph.getNodes();
      mStateMap = new TObjectIntHashMap<SimpleNodeProxy>(nodes.size());
      visitCollection(nodes);
      final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
      final Collection<EdgeProxy> edges = graph.getEdges();
      mSimplifiedGuardActionBlockMap =
        new ProxyAccessorHashMap<GuardActionBlockProxy,ConstraintList>(eq, edges.size());
      mEventEncoding = new EFSMEventEncoding(edges.size());
      visitCollection(edges);
      return null;
    }

    @Override
    public Object visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      final GuardActionBlockProxy update = edge.getGuardActionBlock();
      if (update != null) {
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
    public Object visitSimpleNodeProxy(final SimpleNodeProxy node)
    {
      final int code = mStateMap.size();
      mStateMap.put(node, code);
      final EventListExpressionProxy props = node.getPropositions();
      if (containsMarkingProposition(props)) {
        mUsesMarking = true;
      }
      return null;
    }

    //#######################################################################
    //# Auxiliary Methods
    private boolean containsMarkingProposition(final EventListExpressionProxy list)
    {
      final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
      return eq.contains(list.getEventIdentifierList(), mDefaultMarking);
    }

    //#######################################################################
    //# Data Members
    private final EFAGuardCompiler mGuardCompiler;
    private final ConstraintPropagator mConstraintPropagator;
    private EFSMEventEncoding mEventEncoding;
    private ProxyAccessorMap<GuardActionBlockProxy,ConstraintList> mSimplifiedGuardActionBlockMap;
    private TObjectIntHashMap<SimpleNodeProxy> mStateMap;
    private boolean mUsesMarking;
  }



//#########################################################################
  //# Inner Class EFSMVariableContext
  /**
   * The visitor implementing the second pass of EFA compilation.
   */
  private class EFSMVariableContext implements VariableContext
  {
    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public CompiledRange getVariableRange(final SimpleExpressionProxy varname)
    {
      if (varname instanceof IdentifierProxy) {
        final EFSMVariable variable = mGlobalVariableMap.get(varname);
        if (variable != null) {
          return variable.getRange();
        }
      }
      if (varname instanceof UnaryExpressionProxy) {
        final UnaryExpressionProxy unary = (UnaryExpressionProxy) varname;
        final UnaryOperator op = unary.getOperator();
        if (op == mOperatorTable.getNextOperator()) {
          return getVariableRange(unary.getSubTerm());
        }
      }
      return null;
    }

    @Override
    public SimpleExpressionProxy getBoundExpression(final SimpleExpressionProxy ident)
    {
      return mModuleContext.getBoundExpression(ident);
    }

    @Override
    public boolean isEnumAtom(final IdentifierProxy ident)
    {
      return mModuleContext.isEnumAtom(ident);
    }

    @Override
    public ModuleBindingContext getModuleBindingContext()
    {
      return mModuleContext;
    }

    @Override
    public int getNumberOfVariables()
    {
      return mGlobalVariableMap.size();
    }
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  @SuppressWarnings("unused")
  private final SourceInfoBuilder mSourceInfoBuilder;
  private final CompilerOperatorTable mOperatorTable;
  @SuppressWarnings("unused")
  private final ConstraintList mTrueGuard;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final ModuleProxy mInputModule;
  private IdentifierProxy mDefaultMarking;

  private final ModuleBindingContext mModuleContext;
  private final EFSMVariableContext mVariableContext;
  private final ProxyAccessorMap<IdentifierProxy,EFSMVariable> mGlobalVariableMap;
  private final List<EFSMVariable> mVariables;
  private final List<EFSMTransitionRelation> mTransitionRelations;
}
