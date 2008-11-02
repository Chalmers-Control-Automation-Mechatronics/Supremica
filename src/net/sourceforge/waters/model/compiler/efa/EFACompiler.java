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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
import net.sourceforge.waters.model.compiler.dnf.CompiledClause;
import net.sourceforge.waters.model.expr.BinaryOperator;
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
    mVariableMap = new EFAVariableMap(mFactory, mOperatorTable);
    final Comparator<SimpleExpressionProxy> comparator =
      mVariableMap.getExpressionComparator();
    mSimpleExpressionCompiler =
      new SimpleExpressionCompiler(mFactory, mOperatorTable, comparator);
    mGuardCompiler = new GuardCompiler(mFactory, mOperatorTable, comparator);
    mConstraintPropagator =
      new ConstraintPropagator(mFactory, mOperatorTable,
                               mSimpleExpressionCompiler, mVariableMap);
    mSplitComputer = new SplitComputer(mVariableMap);
    mInputModule = module;
  }


  //#########################################################################
  //# Invocation
  public ModuleProxy compile()
    throws EvalException
  {
    try {
      mRootContext = new ModuleBindingContext(mInputModule);
      // Pass 1 ...
      final ModuleProxyVisitor pass1 = new Pass1Visitor();
      mInputModule.acceptVisitor(pass1);
      // Pass 2 ...
      final ModuleProxyVisitor pass2 = new Pass2Visitor();
      mInputModule.acceptVisitor(pass2);
      // Pass 3 ...
      computeEventPartitions();
      // Pass 4 ...
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
      mVariableMap.clear();
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
  {
    final BinaryOperator andop = mOperatorTable.getAndOperator();
    final CompiledClause startcond = new CompiledClause(andop);
    mEventMap = new HashMap<Proxy,Collection<EFAEvent>>();
    for (final EFAEventDecl edecl : mEventDeclMap.values()) {
      if (!edecl.isBlocked()) {
        final Collection<EFATransitionGroup> allgroups =
          edecl.getTransitionGroups();
        final int allsize = allgroups.size();
        final List<EFATransitionGroup> groups =
          new ArrayList<EFATransitionGroup>(allsize);
        for (final EFATransitionGroup group : allgroups) {
          if (!group.isTrivial()) {
            groups.add(group);
          }
        }
        if (!groups.isEmpty()) {
          final int size = groups.size();
          final List<EFATransition> parts = new ArrayList<EFATransition>(size);
          Collections.sort(groups);
          collectEventPartition(edecl, groups, parts, 0, startcond);
        }
      }
    }
  }

  private void collectEventPartition(final EFAEventDecl edecl,
                                     final List<EFATransitionGroup> groups,
                                     final List<EFATransition> parts,
                                     final int index,
                                     final CompiledClause prevcond)
  {
    if (index < groups.size()) {
      final EFATransitionGroup group = groups.get(index);
      for (final EFATransition part : group.getPartialTransitions()) {
        final CompiledClause cond = part.getConditions();
        final CompiledClause nextcond =
          mConstraintPropagator.propagate(prevcond, cond);
        if (nextcond != null) {
          parts.add(part);
          collectEventPartition(edecl, groups, parts, index + 1, nextcond);
          parts.remove(index);
        }
      }
    } else {
      splitEventPartition(edecl, parts, prevcond);
    }
  }

  private void splitEventPartition(final EFAEventDecl edecl,
                                   final List<EFATransition> parts,
                                   final CompiledClause cond)
  {
    final List<EFAVariable> splitlist = mSplitComputer.computeSplitList(cond);
    if (splitlist.isEmpty()) {
      createEvent(edecl, parts, cond);
    } else {
      splitEventPartition(edecl, parts, cond, splitlist, 0);
    }
  }

  private void splitEventPartition(final EFAEventDecl edecl,
                                   final List<EFATransition> parts,
                                   final CompiledClause cond,
                                   final List<EFAVariable> splitlist,
                                   final int index)
  {
    if (index < splitlist.size()) {
      final EFAVariable var = splitlist.get(index);
      final SimpleExpressionProxy varname = var.getVariableName();
      final CompiledRange range = var.getRange();
      for (final SimpleExpressionProxy value : range.getValues()) {
        final CompiledClause nextcond =
          mConstraintPropagator.propagate(cond, varname, value);
        if (nextcond != null) {
          splitEventPartition(edecl, parts, nextcond, splitlist, index + 1);
        }
      }
    } else {
      splitEventPartition(edecl, parts, cond);
    }
  }

  private void createEvent(final EFAEventDecl edecl,
                           final List<EFATransition> parts,
                           final CompiledClause cond)
  {
    final EFAEvent event = edecl.createEvent(cond);
    for (final EFATransition part : parts) {
      for (final Proxy location : part.getSourceLocations()) {
        Collection<EFAEvent> collection = mEventMap.get(location);
        if (collection == null) {
          collection = new LinkedList<EFAEvent>();
          mEventMap.put(location, collection);
        }
        collection.add(event);
      }
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void insertEvent(final IdentifierProxy ident,
                           final EFAEventDecl edecl)
    throws DuplicateIdentifierException
  {
    final ProxyAccessor<IdentifierProxy> accessor =
      new ProxyAccessorByContents<IdentifierProxy>(ident);
    if (mEventDeclMap.containsKey(accessor)) {
      throw new DuplicateIdentifierException(ident, "event");
    } else {
      mEventDeclMap.put(accessor, edecl);
    }
  }

  private EFAEventDecl findEvent(final IdentifierProxy ident)
    throws UndefinedIdentifierException
  {
    final ProxyAccessor<IdentifierProxy> accessor =
      new ProxyAccessorByContents<IdentifierProxy>(ident);
    final EFAEventDecl edecl = mEventDeclMap.get(accessor);
    if (edecl == null) {
      throw new UndefinedIdentifierException(ident, "event");
    } else {
      return edecl;
    }
  }


  //#########################################################################
  //# Inner Class Pass1Visitor
  /**
   * The visitor implementing the first pass of EFA compilation. It
   * initialises the variables map {@link #mVariableMap} and associates
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
      mVariableMap.reset(size);
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
      final GraphProxy graph = comp.getGraph();
      final List<SimpleIdentifierProxy> list = visitGraphProxy(graph);
      final CompiledRange range = new CompiledEnumRange(list);
      mVariableMap.createVariables(comp, range);
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
        final SimpleExpressionProxy expr = var.getType();
        final CompiledRange range =
          mSimpleExpressionCompiler.getRangeValue(expr);
        mVariableMap.createVariables(var, range);
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
          mVariableMap.checkIdentifier(ident);
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
        final GuardActionBlockProxy ga = edge.getGuardActionBlock();
        if (ga == null) {
          mCurrentGuard = mTrueGuard;
        } else {
          if (mIsUsingEventAlphabet) {
            mCollectedVariables =
              new ProxyAccessorHashMapByContents<IdentifierProxy>();
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

    public EFAEventDecl visitEventDeclProxy(final EventDeclProxy decl)
      throws VisitorException
    {
      try {
        final IdentifierProxy ident = decl.getIdentifier();
        final EFAEventDecl edecl = new EFAEventDecl(decl);
        insertEvent(ident, edecl);
        return edecl;
      } catch (final DuplicateIdentifierException exception) {
        throw wrap(exception);
      }
    }

    public Object visitGraphProxy(final GraphProxy graph)
      throws VisitorException
    {
      final LabelBlockProxy blocked = graph.getBlockedEvents();
      visitLabelBlockProxy(blocked);
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

    public EFAEventDecl visitIdentifierProxy(final IdentifierProxy ident)
      throws VisitorException
    {
      try {
        final EFAEventDecl edecl = findEvent(ident);
        if (!edecl.isBlocked()) {
          mCollectedEvents.add(edecl);
          if (mIsUsingEventAlphabet && mCollectedVariables != null) {
            edecl.addVariables(mCollectedVariables.values());
          }
          if (mCurrentGuard != null) {
            edecl.addTransitions(mCurrentComponent, mCurrentGuard, ident);
          }
        }
        return edecl;
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
      mEventDeclMap =
        new HashMap<ProxyAccessor<IdentifierProxy>,EFAEventDecl>(size);
      visitCollection(events);
      final List<Proxy> components = module.getComponentList();
      visitCollection(components);
      return null;
    }

    public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
      throws VisitorException
    {
      try {
        final int size = mEventDeclMap.size();
        mCurrentComponent = comp;
        mCollectedEvents = new HashSet<EFAEventDecl>(size);
        if (!mIsUsingEventAlphabet) {
          mCollectedVariables =
            new ProxyAccessorHashMapByContents<IdentifierProxy>();
        }
        final ComponentKind ckind = comp.getKind();
        final GraphProxy graph = comp.getGraph();
        visitGraphProxy(graph);
        for (final EFAEventDecl edecl : mCollectedEvents) {
          if (!mIsUsingEventAlphabet) {
            edecl.addVariables(mCollectedVariables.values());
          }
          final EventKind ekind = edecl.getKind();
          final EFATransitionGroup trans = edecl.getTransitionGroup(comp);
          if (ekind == EventKind.CONTROLLABLE &&
              ckind == ComponentKind.PROPERTY ||
              ekind == EventKind.UNCONTROLLABLE &&
              ckind != ComponentKind.PLANT) {
            // Include a catch-all event to be blocked ...
            if (!trans.hasTrueGuard()) {
              final Collection<SimpleExpressionProxy> guards =
                trans.getGuards();
              final CompiledGuard complement =
                mGuardCompiler.getComplementaryGuard(guards);
              if (complement != null) {
                trans.addTransitions(complement, null);
              }
            }
          } else {
            if (trans.isEmpty()) {
              edecl.setBlocked();
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
    private Set<EFAEventDecl> mCollectedEvents;
    private CompiledGuard mCurrentGuard;
  }


  //#########################################################################
  //# Inner Class Pass1Visitor
  /**
   * The visitor implementing the fourth pass of EFA compilation.
   */
  private class Pass4Visitor extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Object visitEventDeclProxy(final EventDeclProxy decl)
      throws VisitorException
    {
      try {
        final IdentifierProxy ident = decl.getIdentifier();
        final EFAEventDecl edecl = findEvent(ident);
        final EventKind kind = edecl.getKind();
        final boolean observable = edecl.isObservable();
        for (final EFAEvent event : edecl.getEvents()) {
          final IdentifierProxy subident = event.createIdentifier();
          final EventDeclProxy subdecl = mFactory.createEventDeclProxy
            (subident, kind, observable, ScopeKind.LOCAL, null, null);
          mEventDeclarations.add(subdecl);
        }
        return null;
      } catch (final UndefinedIdentifierException exception) {
        throw wrap(exception);
      }
    }

    public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
    {
      return null;
    }

    public Object visitVariableComponentProxy(final VariableComponentProxy var)
    {
      return null;
    }

    //#######################################################################
    //# Data Members
    private List<EventDeclProxy> mEventDeclarations;
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final SourceInfoBuilder mSourceInfoBuilder;
  private final CompilerOperatorTable mOperatorTable;
  private final CompiledGuard mTrueGuard;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final GuardCompiler mGuardCompiler;
  private final ConstraintPropagator mConstraintPropagator;
  private final SplitComputer mSplitComputer;
  private final ModuleProxy mInputModule;

  private boolean mIsUsingEventAlphabet = true;

  private ModuleBindingContext mRootContext;
  // Pass 1
  /**
   * A map that assigns to each expression that refers to a variable
   * component {@link VariableComponentProxy} or simple component {@link
   * SimpleComponentProxy} an EFA variable object that contains the
   * computed range of its state space.
   */
  private final EFAVariableMap mVariableMap;
  // Pass 2
  /**
   * A map that assigns to each identifier of an event declaration {@link
   * EventDeclProxy} the information about its event variable set and
   * associated guards.
   */
  private Map<ProxyAccessor<IdentifierProxy>,EFAEventDecl> mEventDeclMap;
  // Pass 3
  /**
   * A map that assigns to each identifier of an event label on an edge the
   * list of EFA events to be associated with it. Likewise, it assigns to
   * each automaton ({@link SimpleComponentProxy}) a list of events to be
   * blocked in globally in the automaton. Identifiers associated with true
   * guards to not have entries in this table, as they will simply receive
   * all events associated with the event declaration; all other
   * identifiers receive mappings that reflect the results of simplifying
   * their guards.
   */
  private Map<Proxy,Collection<EFAEvent>> mEventMap;

}
