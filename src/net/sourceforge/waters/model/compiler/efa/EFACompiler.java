//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFACompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorByContents;
import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledEnumRange;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.
  DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.
  UndefinedIdentifierException;
import net.sourceforge.waters.model.compiler.context.ModuleBindingContext;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfoBuilder;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
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
import net.sourceforge.waters.xsd.base.EventKind;


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
 * <P>Th EFA compiler ensures that the resultant module only contains
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
 * <LI>Identify the event variable set and the set of checked components for
 *     each event.
 *     <UL>
 *     <LI>The event variable set consists of the set of all variables whose
 *         value may change if an event occurs. It can be computed in two
 *         different ways, depending on the configuration.<BR>
 *         In <CODE>AUTOMATON_ALPHABET</CODE> mode, the event variable set
 *         of an event is the set of all the variables updated in some
 *         simple component using the event.<BR>
 *         In <CODE>EVENT_ALPHABET</CODE> mode, the event variable set
 *         of an event is the set of all the variables updated in some
 *         guard/action block whose edge includes the event.</LI>
 *     <LI>Independently of the event variable set, the set of checked
 *         components for an event consists of all the components
 *         (variable or simple) whose state value will be considered
 *         when partitioning the event.<BR>
 *         It contains all simple components using a given event, provided
 *         that the event is associated with at least two different
 *         guard/action blocks in that component. If all guard/action
 *         blocks in a component are equal, the associated state-update
 *         relation must be added to the state update relation of the
 *         event, and then the component will simply use all the event
 *         labels generated for the event.<BR>
 *         It also contains all the variables&nbsp;<I>x</I> in the event
 *         variable set, unless the following condition is satisfied. If in
 *         every automaton using the event, all the guards can be written
 *         as <CODE>a&nbsp;&amp;&nbsp;b(</CODE><I>x</I><CODE>)</CODE> where
 *         <CODE>a</CODE> does not depend on&nbsp;<I>x</I>, and
 *         <CODE>b(</CODE><I>x</I><CODE>)</CODE> is an expression that
 *         depends on no variables except&nbsp;<I>x</I>, and is the same for
 *         all guards associated with the given event in the automaton, then
 *         the variable&nbsp;<I>x</I> is not a checked component for that
 *         event. In this case the update operation for the
 *         variable&nbsp;<I>x</I> on the given event is the conjunction of
 *         all the terms <CODE>b(</CODE><I>x</I><CODE>)</CODE> and can be
 *         implemented separately.</LI>
 *     </UL>
 * <LI>Compute normalised state-update relations.<BR>
 *     For each event, iterate over all combinations of values of the
 *     checked components and compute the (nondeterministic set of)
 *     successor states. (Under the current syntax, where only action
 *     functions are permitted, the successor state sets can be represented
 *     as a Cartesian product of state sets of the checked components; this
 *     will change once arbitrary relations are allowed as guards.)<BR>
 *     Each combination of values with a nonempty set of successor states
 *     may warrant an event of its own. But some combinations may be
 *     merged. If, for some component&nbsp;<I>c</I>, there are two or more
 *     state combinations that agree on the current and successor states of
 *     all components except&nbsp;<I>c</I>, then these two or more state
 *     combinations can be represented by the same event label in the
 *     output.</LI>
 * <LI>Create partitioned events and build output automata.</LI>
 * </OL>
 *
 * @author Robi Malik
 */

public class EFACompiler
{

  //#########################################################################
  //# Constructors
  public EFACompiler(final ModuleProxyFactory factory,
                     final SourceInfoBuilder builder,
                     final ModuleProxy module)
  {                     
    mFactory = factory;
    mSourceInfoBuilder = builder;
    mOperatorTable = CompilerOperatorTable.getInstance();
    mTrueGuard = new CompiledGuard();
    mSimpleExpressionCompiler =
      new SimpleExpressionCompiler(mFactory, mOperatorTable);
    mGuardCompiler = new GuardCompiler(mFactory, mOperatorTable);
    mInputModule = module;
  }


  //#########################################################################
  //# Invocation
  public ModuleProxy compile()
    throws EvalException
  {
    try {
      mRootContext = new ModuleBindingContext(mInputModule);
      final ModuleProxyVisitor pass1 = new Pass1Visitor();
      mInputModule.acceptVisitor(pass1);
      return null;
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
  void setUsingEventAlphabet(final boolean using)
  {
    mIsUsingEventAlphabet = using;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void createVariables(final ComponentProxy comp,
                               final IdentifierProxy ident,
                               final CompiledRange range)
  {
    final ProxyAccessor<SimpleExpressionProxy> curaccessor =
      new ProxyAccessorByContents<SimpleExpressionProxy>(ident);
    final EFAVariable curvar = new EFAVariable(comp, ident, range);
    mVariablesMap.put(curaccessor, curvar);
    final UnaryOperator nextop = mOperatorTable.getNextOperator();
    final UnaryExpressionProxy nextident =
      mFactory.createUnaryExpressionProxy(nextop, ident);
    final ProxyAccessor<SimpleExpressionProxy> nextaccessor =
      new ProxyAccessorByContents<SimpleExpressionProxy>(nextident);
    final EFAVariable nextvar = new EFAVariable(comp, nextident, range);
    mVariablesMap.put(nextaccessor, nextvar);
  }

  private void checkVariableIdentifier(final IdentifierProxy ident)
    throws UndefinedIdentifierException
  {
    final ProxyAccessor<SimpleExpressionProxy> accessor =
      new ProxyAccessorByContents<SimpleExpressionProxy>(ident);
    if (!mVariablesMap.containsKey(accessor)) {
      throw new UndefinedIdentifierException(ident, "variable");
    }
  }

  private void insertEvent(final IdentifierProxy ident,
                           final EFAEvent event)
    throws DuplicateIdentifierException
  {
    final ProxyAccessor<IdentifierProxy> accessor =
      new ProxyAccessorByContents<IdentifierProxy>(ident);
    if (mEventMap.containsKey(accessor)) {
      throw new DuplicateIdentifierException(ident, "event");
    } else {
      mEventMap.put(accessor, event);
    }
  }

  private EFAEvent findEvent(final IdentifierProxy ident)
    throws UndefinedIdentifierException
  {
    final ProxyAccessor<IdentifierProxy> accessor =
      new ProxyAccessorByContents<IdentifierProxy>(ident);
    final EFAEvent event = mEventMap.get(accessor);
    if (event == null) {
      throw new UndefinedIdentifierException(ident, "event");
    } else {
      return event;
    }
  }


  //#########################################################################
  //# Inner Class Pass1Visitor
  /**
   * The visitor implementing the first pass of EFA compilation. It
   * initialises the variables map {@link #mVariablesMap} and associates
   * the identifier of each simple or variable component with a {@link
   * EFAVariable} object that contains the range of possible state values
   * of that component.
   */
  private class Pass1Visitor extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
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

    public Object visitModuleProxy(final ModuleProxy module)
      throws VisitorException
    {
      final List<Proxy> components = module.getComponentList();
      final int size = 2 * components.size();
      mVariablesMap =
        new HashMap<ProxyAccessor<SimpleExpressionProxy>,EFAVariable>(size);
      visitCollection(components);
      return null;
    }

    public Object visitNodeProxy(final NodeProxy node)
    {
      return null;
    }

    public CompiledRange visitSimpleComponentProxy
      (final SimpleComponentProxy comp)
      throws VisitorException
    {
      final IdentifierProxy ident = comp.getIdentifier();
      final GraphProxy graph = comp.getGraph();
      final List<SimpleIdentifierProxy> list = visitGraphProxy(graph);
      final CompiledRange range = new CompiledEnumRange(list);
      createVariables(comp, ident, range);
      return range;
    }

    public IdentifierProxy visitSimpleNodeProxy(final SimpleNodeProxy node)
    {
      final String name = node.getName();
      final SimpleIdentifierProxy ident =
        mFactory.createSimpleIdentifierProxy(name);
      mRootContext.addBinding(ident, ident);
      mCurrentRange.add(ident);
      return ident;
    }

    public CompiledRange visitVariableComponentProxy
      (final VariableComponentProxy var)
      throws VisitorException
    {
      try {
        final IdentifierProxy ident = var.getIdentifier();
        final SimpleExpressionProxy expr = var.getType();
        final CompiledRange range =
          mSimpleExpressionCompiler.getRangeValue(expr);
        createVariables(var, ident, range);
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
   * The visitor implementing the second pass of EFA compilation.
   */
  private class Pass2Visitor extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Object visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
      throws VisitorException
    {
      try {
        final SimpleExpressionProxy left = expr.getLeft();
        if (left instanceof IdentifierProxy) {
          final IdentifierProxy ident = (IdentifierProxy) left;
          checkVariableIdentifier(ident);
          mCollectedVariables.addProxy(ident);
          return null;
        } else {
          throw new ActionSyntaxException(expr);
        }
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    public Object visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      try {
        mCurrentEdge = edge;
        mCurrentGuard = mTrueGuard;
        final GuardActionBlockProxy ga = edge.getGuardActionBlock();
        final LabelBlockProxy block = edge.getLabelBlock();
        if (mIsUsingEventAlphabet) {
          if (ga != null) {
            try {
              mCollectedVariables =
                new ProxyAccessorHashMapByContents<IdentifierProxy>();
              visitGuardActionBlockProxy(ga);
              visitLabelBlockProxy(block);
            } finally {
              mCollectedVariables = null;
            }
          }
        } else {
          if (ga != null) {
            visitGuardActionBlockProxy(ga);
          }
          visitLabelBlockProxy(block);
        }
        return null;
      } finally {
        mCurrentEdge = null;
        mCurrentGuard = null;
      }
    }

    public EFAEvent visitEventDeclProxy(final EventDeclProxy decl)
      throws VisitorException
    {
      try {
        final IdentifierProxy ident = decl.getIdentifier();
        final EFAEvent event = new EFAEvent(decl);
        insertEvent(ident, event);
        return event;
      } catch (final DuplicateIdentifierException exception) {
        throw wrap(exception);
      }
    }

    public Object visitGraphProxy(final GraphProxy graph)
      throws VisitorException
    {
      final LabelBlockProxy blocked = graph.getBlockedEvents();
      if (blocked != null) {
        try {
          mCurrentGuard = mTrueGuard;
          visitLabelBlockProxy(blocked);
        } finally {
          mCurrentGuard = null;
        }
      }
      final Collection<EdgeProxy> edges = graph.getEdges();
      visitCollection(edges);
      return null;
    }

    public CompiledGuard visitGuardActionBlockProxy
      (final GuardActionBlockProxy ga)
      throws VisitorException
    {
      try {
        mCurrentGuard = mGuardCompiler.getCompiledGuard(ga);
        final List<BinaryExpressionProxy> actions = ga.getActions();
        visitCollection(actions);
        return mCurrentGuard;
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    public EFAEvent visitIdentifierProxy(final IdentifierProxy ident)
      throws VisitorException
    {
      try {
        final EFAEvent event = findEvent(ident);
        mCollectedEvents.add(event);
        if (mIsUsingEventAlphabet) {
          event.addVariables(mCollectedVariables.values());
        }
        event.addTransitions(mCurrentComponent, mCurrentGuard, ident);
        return event;
      } catch (final UndefinedIdentifierException exception) {
        throw wrap(exception);
      }
    }

    public Object visitLabelBlockProxy(final LabelBlockProxy block)
      throws VisitorException
    {
      final List<Proxy> list = block.getEventList();
      visitCollection(list);
      return null;
    }

    public Object visitModuleProxy(final ModuleProxy module)
      throws VisitorException
    {
      final List<EventDeclProxy> events = module.getEventDeclList();
      final int size = events.size();
      mEventMap =
        new HashMap<ProxyAccessor<IdentifierProxy>,EFAEvent>(size);
      visitCollection(events);
      final List<Proxy> components = module.getComponentList();
      visitCollection(components);
      return null;
    }

    public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
      throws VisitorException
    {
      try {
        final int size = mEventMap.size();
        mCurrentComponent = comp;
        mCollectedEvents = new HashSet<EFAEvent>(size);
        if (!mIsUsingEventAlphabet) {
          mCollectedVariables =
            new ProxyAccessorHashMapByContents<IdentifierProxy>();
        }
        final ComponentKind ckind = comp.getKind();
        final GraphProxy graph = comp.getGraph();
        visitGraphProxy(graph);
        for (final EFAEvent event : mCollectedEvents) {
          if (!mIsUsingEventAlphabet) {
            event.addVariables(mCollectedVariables.values());
          }
          final EventKind ekind = event.getKind();
          if (ekind == EventKind.CONTROLLABLE &&
              ckind == ComponentKind.PROPERTY ||
              ekind == EventKind.UNCONTROLLABLE &&
              ckind != ComponentKind.PLANT) {
            // Include a catch-all event to be blocked ...
            final EFATransitionGroup trans = event.getTransitionGroup(comp);
            final Collection<SimpleExpressionProxy> guards = trans.getGuards();
            if (guards != null) {
              final CompiledGuard complement =
                mGuardCompiler.getComplementaryGuard(guards);
              if (complement != null) {
                trans.addTransitions(complement, null);
              }
            }
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

    public Object visitVariableComponentProxy(final VariableComponentProxy var)
    {
      return null;
    }

    //#######################################################################
    //# Data Members
    private SimpleComponentProxy mCurrentComponent;
    private ProxyAccessorMap<IdentifierProxy> mCollectedVariables;
    private Set<EFAEvent> mCollectedEvents;
    private EdgeProxy mCurrentEdge;
    private CompiledGuard mCurrentGuard;
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final SourceInfoBuilder mSourceInfoBuilder;
  private final CompilerOperatorTable mOperatorTable;
  private final CompiledGuard mTrueGuard;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final GuardCompiler mGuardCompiler;
  private final ModuleProxy mInputModule;

  private boolean mIsUsingEventAlphabet = true;

  private ModuleBindingContext mRootContext;
  // Pass 1
  /**
   * A map that assigns to each expression that refers to a variable
   * component {@link VariableComponentProxy} or simple component {@link
   * SimpleComponentProxy} an EFA variable object that contains the
   * computed range of its state space. The map contains entries for
   * the current state and next state variables.
   */
  private Map<ProxyAccessor<SimpleExpressionProxy>,EFAVariable> mVariablesMap;
  // Pass 2
  /**
   * A map that assigns to each identifier of a event declaration {@link
   * EventDeclProxy} the information about its event variable set and
   * associated guards.
   */
  private Map<ProxyAccessor<IdentifierProxy>,EFAEvent> mEventMap;

}
