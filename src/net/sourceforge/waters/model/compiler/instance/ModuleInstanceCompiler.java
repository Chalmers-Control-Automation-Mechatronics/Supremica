//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.instance
//# CLASS:   ModuleInstanceCompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.instance;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Set;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.ModuleBindingContext;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.compiler.context.SourceInfoBuilder;
import net.sourceforge.waters.model.compiler.context.
  UndefinedIdentifierException;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifiedProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;


/**
 * <P>The first pass of the compiler.</P>
 *
 * <P>This compiler accepts a module ({@link ModuleProxy}) as input and
 * produces another module as output. It expands all aliases, foreach
 * constructs, and instantiations. Event arrays as well as component and
 * variable arrays are enumerated explicitly. Variable components are
 * preserved in the output, but all guards and actions are simplified by
 * substituting values obtained from aliasing or instantiation.</P>
 *
 * <P>It is ensured that the resultant module only contains
 * nodes of the following types.</P>
 * <UL>
 * <LI>{@link EventDeclProxy}, where only simple events are defined,
 *     i.e., the list of ranges is guaranteed to be empty;</LI>
 * <LI>{@link SimpleComponentProxy};</LI>
 * <LI>{@link VariableComponentProxy}.</LI>
 * </UL>
 *
 * @author Robi Malik
 */

public class ModuleInstanceCompiler extends AbstractModuleProxyVisitor
{

  //#########################################################################
  //# Constructors
  public ModuleInstanceCompiler(final DocumentManager manager,
                                final ModuleProxyFactory factory,
                                final SourceInfoBuilder builder,
                                final ModuleProxy module)
  {
    mDocumentManager = manager;
    mFactory = factory;
    mSourceInfoBuilder = builder;
    mOperatorTable = CompilerOperatorTable.getInstance();
    mSimpleExpressionCompiler =
      new SimpleExpressionCompiler(mFactory, mOperatorTable);
    mNameCompiler = new NameCompiler();
    mIndexAdder = new IndexAdder();
    mNameSpaceVariablesContext = new NameSpaceVariablesContext();
    mInputModule = module;
  }


  //#########################################################################
  //# Invocation
  public ModuleProxy compile(final List<ParameterBindingProxy> bindings)
    throws EvalException
  {
    try {
      mHasGuardActionBlocks = false;
      mContext = new ModuleBindingContext(mInputModule);
      mNameSpace = new CompiledNameSpace();
      mCompiledEvents = new TreeSet<EventDeclProxy>();
      mCompiledComponents = new LinkedList<Proxy>();
      if (bindings != null) {
        mParameterMap = new TreeMap<String,CompiledParameterBinding>();
        visitCollection(bindings);
      }
      visitModuleProxy(mInputModule);
      final String name = mInputModule.getName();
      final String comment = mInputModule.getComment();
      return mFactory.createModuleProxy
        (name, comment, null,
         null, mCompiledEvents, null, mCompiledComponents);
    } catch (final VisitorException exception) {
      final Throwable cause = exception.getCause();
      if (cause instanceof EvalException) {
        throw (EvalException) cause;
      } else {
        throw exception.getRuntimeException();
      }
    } finally {
      mContext = null;
      mNameSpace = null;
      mCompiledEvents = null;
      mCompiledComponents = null;
      mParameterMap = null;
    }
  }

  public boolean getHasGuardActionBlocks()
  {
    return mHasGuardActionBlocks;
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  public SimpleExpressionProxy visitConstantAliasProxy
    (final ConstantAliasProxy alias)
    throws VisitorException
  {
    try {
      final IdentifierProxy ident = alias.getIdentifier();
      final ScopeKind scope = alias.getScope();
      final CompiledParameterBinding binding =
        getParameterBinding(ident, scope);
      final SimpleExpressionProxy value;
      if (binding == null) {
        final SimpleExpressionProxy defaultExpr =
          (SimpleExpressionProxy) alias.getExpression();
        value = mSimpleExpressionCompiler.eval(defaultExpr, mContext);
      } else {
        value = binding.getSimpleValue();
      }
      final ModuleBindingContext context = mContext.getModuleBindingContext();
      context.insertBinding(ident, value);
      return value;
    } catch (final EvalException exception) {
      exception.provideLocation(alias);
      throw wrap(exception);
    }
  }

  public EdgeProxy visitEdgeProxy(final EdgeProxy edge)
    throws VisitorException
  {
    try {
      final LabelBlockProxy labels0 = edge.getLabelBlock();
      final CompiledEventList events = visitLabelBlockProxy
        (labels0, EventKindMask.TYPEMASK_EVENT);
      final GuardActionBlockProxy ga0 = edge.getGuardActionBlock();
      GuardActionBlockProxy ga1 = null;
      if (ga0 != null) {
        ga1 = visitGuardActionBlockProxy(ga0);
        final List<SimpleExpressionProxy> guards = ga1.getGuards();
        final List<BinaryExpressionProxy> actions = ga1.getActions();
        if (guards.isEmpty()) {
          if (actions.isEmpty()) {
            ga1 = null;
          }
        } else {
          final Iterator<SimpleExpressionProxy> iter = guards.iterator();
          final SimpleExpressionProxy guard = iter.next();
          if (!iter.hasNext() &&
              mSimpleExpressionCompiler.isAtomicValue(guard)) {
            final boolean value =
              mSimpleExpressionCompiler.getBooleanValue(guard);
            if (!value) {
              addAdditionalBlockedEvents(events);
              return null;
            } else if (actions.isEmpty()) {
              ga1 = null;
            }
          }
        }
      }
      mHasGuardActionBlocks |= ga1 != null;
      mCurrentEdge = edge;
      final NodeProxy source0 = edge.getSource();
      final NodeProxy source1 = mNodeMap.get(source0);
      final NodeProxy target0 = edge.getTarget();
      final NodeProxy target1 = mNodeMap.get(target0);
      final LabelBlockProxy labels1 = createLabelBlock(events);
      final EdgeProxy compiled = mFactory.createEdgeProxy
        (source1, target1, labels1, ga1, null, null, null);
      mCurrentEdges.add(compiled);
      return compiled;
    } catch (final EvalException exception) {
      throw wrap(exception);
    } finally {
      mCurrentEdge = null;
    }
  }

  public CompiledEvent visitEventAliasProxy(final EventAliasProxy alias)
    throws VisitorException
  {
    try {
      final IdentifierProxy ident = alias.getIdentifier();
      final ExpressionProxy expr = alias.getExpression();
      final Object value = expr.acceptVisitor(this);
      if (!(value instanceof CompiledEvent)) {
        throw new TypeMismatchException(expr, "event");
      }
      final CompiledEvent event = (CompiledEvent) value;
      mNameSpace.addEvent(ident, event);
      return event;
    } catch (final EvalException exception) {
      exception.provideLocation(alias);
      throw wrap(exception);
    }
  }

  public CompiledEvent visitEventDeclProxy(final EventDeclProxy decl)
    throws VisitorException
  {
    try {
      final IdentifierProxy ident = decl.getIdentifier();
      final ScopeKind scope = decl.getScope();
      final CompiledParameterBinding binding =
        getParameterBinding(ident, scope);
      final List<SimpleExpressionProxy> declRanges = decl.getRanges();
      CompiledEvent event = binding == null ? null : binding.getEventValue();
      if (event == null) {
        final List<CompiledRange> ranges =
          new ArrayList<CompiledRange>(declRanges.size());
        for (final SimpleExpressionProxy expr : declRanges) {
          final SimpleExpressionProxy value =
            mSimpleExpressionCompiler.eval(expr, mContext);
          final CompiledRange range =
            mSimpleExpressionCompiler.getRangeValue(value);
          ranges.add(range);
        }
        final CompiledEventDecl entry =
          new CompiledEventDecl(mNameSpace, decl, ranges);
        event = entry.getCompiledEvent();
      } else {
        final EventKind kind = decl.getKind();
        final int mask = event.getKindMask();
        if (!EventKindMask.isAssignable(kind, mask) ||
            decl.isObservable() && !event.isObservable()) {
          throw new EventKindException(decl, event);
        }
        final Iterator<SimpleExpressionProxy> declIter = declRanges.iterator();
        final List<CompiledRange> eventRanges = event.getIndexRanges();
        final Iterator<CompiledRange> eventIter = eventRanges.iterator();
        int index = 0;
        while (declIter.hasNext()) {
          if (!eventIter.hasNext()) {
            throw new EventKindException(decl, event, index);
          }
          final SimpleExpressionProxy expr = declIter.next();
          final SimpleExpressionProxy value =
            mSimpleExpressionCompiler.eval(expr, mContext);
          final CompiledRange declRange =
            mSimpleExpressionCompiler.getRangeValue(value);
          final CompiledRange eventRange = eventIter.next();
          if (!declRange.equals(eventRange)) {
            throw new EventKindException(decl, event, index, declRange);
          }
          index++;
        }
      }
      mNameSpace.addEvent(ident, event);
      return event;
    } catch (final EvalException exception) {
      exception.provideLocation(decl);
      throw wrap(exception);
    }
  }

  public CompiledEventList visitEventListExpressionProxy
    (final EventListExpressionProxy proxy)
    throws VisitorException
  {
    return visitEventListExpressionProxy(proxy, EventKindMask.TYPEMASK_ANY);
  }

  public CompiledEventList visitEventListExpressionProxy
    (final EventListExpressionProxy proxy, final int mask)
    throws VisitorException
  {
    try {
      mCurrentEventList = new CompiledEventList(mask);
      final List<Proxy> list = proxy.getEventList();
      visitCollection(list);
      return mCurrentEventList;
    } finally {
      mCurrentEventList = null;
    }
  }

  public Object visitForeachProxy(final ForeachProxy foreach)
    throws VisitorException
  {
    try {
      final BindingContext root = mContext;
      final String name = foreach.getName();
      final List<Proxy> body = foreach.getBody();
      final SimpleExpressionProxy range = foreach.getRange();
      final SimpleExpressionProxy rvalue =
        mSimpleExpressionCompiler.eval(range, mContext);
      final CompiledRange crange =
        mSimpleExpressionCompiler.getRangeValue(rvalue);
      for (final SimpleExpressionProxy item : crange.getValues()) {
        try {
          mContext = new ForeachBindingContext(name, item, root);
          final SimpleExpressionProxy guard = foreach.getGuard();
          if (guard == null) {
            visitCollection(body);
          } else {
            final SimpleExpressionProxy gvalue =
              mSimpleExpressionCompiler.eval(guard, mContext);
            final boolean gboolean =
              mSimpleExpressionCompiler.getBooleanValue(gvalue);
            if (gboolean) {
              visitCollection(body);
            }
          }
        } finally {
          mContext = root;
        }
      }
      return null;
    } catch (final EvalException exception) {
      throw wrap(exception);
    }
  }

  public GraphProxy visitGraphProxy(final GraphProxy graph)
    throws VisitorException
  {
    try {
      final boolean deterministic = graph.isDeterministic();
      final LabelBlockProxy blocked0 = graph.getBlockedEvents();
      if (blocked0 != null) {
        mCurrentBlockedEvents = visitLabelBlockProxy(blocked0);
      }
      final Collection<NodeProxy> nodes = graph.getNodes();
      final int numnodes = nodes.size();
      mCurrentNodes = new ArrayList<NodeProxy>(numnodes);
      mNodeMap = new HashMap<NodeProxy,NodeProxy>(numnodes);
      visitCollection(nodes);
      mCurrentAlphabet = new ProxyAccessorHashMapByContents<IdentifierProxy>();
      final Collection<EdgeProxy> edges = graph.getEdges();
      final int numedges = edges.size();
      mCurrentEdges = new ArrayList<EdgeProxy>(numedges);
      visitCollection(edges);
      final LabelBlockProxy blocked1 =
        mCurrentBlockedEvents == null ? null :
        createLabelBlock(mCurrentBlockedEvents);
      return mFactory.createGraphProxy
        (deterministic, blocked1, mCurrentNodes, mCurrentEdges);
    } finally {
      mCurrentNodes = null;
      mNodeMap = null;
      mCurrentAlphabet = null;
      mCurrentBlockedEvents = null;
      mCurrentEdges = null;
    }
  }

  public GroupNodeProxy visitGroupNodeProxy(final GroupNodeProxy group)
    throws VisitorException
  {
    final String name = group.getName();
    final PlainEventListProxy props0 = group.getPropositions();
    final CompiledEventList event = visitEventListExpressionProxy
      (props0, EventKindMask.TYPEMASK_PROPOSITION);
    final PlainEventListProxy props1 = createPlainEventList(event);
    final Set<NodeProxy> children0 = group.getImmediateChildNodes();
    final int numchildren = children0.size();
    final List<NodeProxy> children1 = new ArrayList<NodeProxy>(numchildren);
    for (final NodeProxy child0 : children0) {
      final NodeProxy child1 = mNodeMap.get(child0);
      children1.add(child1);
    }
    final GroupNodeProxy compiled =
      mFactory.createGroupNodeProxy(name, props1, children1, null);
    mNodeMap.put(group, compiled);
    mCurrentNodes.add(compiled);
    addSourceInfo(compiled, group);
    return compiled;
  }

  public GuardActionBlockProxy visitGuardActionBlockProxy
    (final GuardActionBlockProxy ga)
    throws VisitorException
  {
    try {
      final List<SimpleExpressionProxy> oldguards = ga.getGuards();
      final int numguards = oldguards.size();
      final List<SimpleExpressionProxy> newguards =
        new ArrayList<SimpleExpressionProxy>(numguards);
      for (final SimpleExpressionProxy oldguard : oldguards) {
        final SimpleExpressionProxy newguard =
          mSimpleExpressionCompiler.simplify
          (oldguard, mNameSpaceVariablesContext);
        if (!mSimpleExpressionCompiler.isAtomicValue(newguard)) {
          newguards.add(newguard);
        } else if (mSimpleExpressionCompiler.getBooleanValue(newguard)) {
          // Don't bother to add true guards ...
        } else {
          // If a guard is false, no need for any other guards ...
          newguards.clear();
          newguards.add(newguard);
          break;
        }
      }
      final List<BinaryExpressionProxy> oldactions = ga.getActions();
      final int numactions = oldactions.size();
      final List<BinaryExpressionProxy> newactions =
        new ArrayList<BinaryExpressionProxy>(numactions);
      for (final BinaryExpressionProxy oldaction : oldactions) {
        final SimpleExpressionProxy newaction =
          mSimpleExpressionCompiler.simplify
          (oldaction, mNameSpaceVariablesContext);
        if (newaction instanceof BinaryExpressionProxy) {
          final BinaryExpressionProxy newbinary =
            (BinaryExpressionProxy) newaction;
          newactions.add(newbinary);
        } else {
          throw new TypeMismatchException(oldaction, "ACTION");
        }
      }
      return mFactory.createGuardActionBlockProxy(newguards, newactions, null);
    } catch (final EvalException exception) {
      throw wrap(exception);
    }
  }

  public Object visitIdentifierProxy(final IdentifierProxy ident)
    throws VisitorException
  {
    try {
      // First evaluate all indexes ...
      final IdentifierProxy newident = mNameCompiler.compileName(ident, false);
      // Second do a lookup ... Where? Depends on context ...
      final CompiledEvent event;
      if (mCurrentEventList == null) {
        final SimpleExpressionProxy value =
          mSimpleExpressionCompiler.simplify(newident, mContext);
        if (mSimpleExpressionCompiler.isAtomicValue(value)) {
          return value;
        }
        final IdentifierProxy ivalue =
          mSimpleExpressionCompiler.getIdentifierValue(value);
        event = mNameSpace.getEvent(ivalue);
        if (event == null) {
          throw new UndefinedIdentifierException(ident);
        }
      } else {
        event = mNameSpace.findEvent(newident);
      }
      final SourceInfo info = new SourceInfo(ident, mContext);
      final CompiledEvent occ = new CompiledEventOccurrence(event, info);
      if (mCurrentEventList != null) {
        mCurrentEventList.addEvent(occ);
      }
      if (mCurrentAlphabet != null) {
        mCurrentAlphabet.addProxy(newident);
      }
      return occ;
    } catch (final EvalException exception) {
      exception.provideLocation(ident);
      throw wrap(exception);
    }
  }

  public Object visitInstanceProxy(final InstanceProxy inst)
    throws VisitorException
  {
    final BindingContext oldContext = mContext;
    final CompiledNameSpace oldNameSpace = mNameSpace;
    try {
      final IdentifierProxy ident = inst.getIdentifier();
      final IdentifierProxy suffix = mNameCompiler.compileName(ident);
      final IdentifierProxy fullname =
        mNameSpace.getPrefixedIdentifier(suffix, mFactory);
      final List<ParameterBindingProxy> bindings = inst.getBindingList();
      mParameterMap = new TreeMap<String,CompiledParameterBinding>();
      visitCollection(bindings);
      final ModuleBindingContext root = mContext.getModuleBindingContext();
      final URI uri = root.getModule().getLocation();
      final String filename = inst.getModuleName();
      final ModuleProxy module =
        mDocumentManager.load(uri, filename, ModuleProxy.class);
      final SourceInfo info = new SourceInfo(inst, mContext);
      mContext = new ModuleBindingContext(module, fullname, info);
      mNameSpace = new CompiledNameSpace(suffix, mNameSpace);
      return visitModuleProxy(module);
    } catch (final IOException exception) {
      final InstantiationException next =
        new InstantiationException(exception, inst);
      throw wrap(next);
    } catch (final WatersUnmarshalException exception) {
      final InstantiationException next =
        new InstantiationException(exception, inst);
      throw wrap(next);
    } finally {
      mContext = oldContext;
      mNameSpace = oldNameSpace;
      mParameterMap = null;
    }
  }

  public CompiledEventList visitLabelBlockProxy(final LabelBlockProxy block)
    throws VisitorException
  {
    return visitLabelBlockProxy(block, EventKindMask.TYPEMASK_ANY);
  }

  public CompiledEventList visitLabelBlockProxy
    (final LabelBlockProxy block, final int mask)
    throws VisitorException
  {
    final List<Proxy> list = block.getEventList();
    if (list.isEmpty()) {
      final EmptyLabelBlockException exception =
        new EmptyLabelBlockException(block, mCurrentEdge, mCurrentComponent);
      throw wrap(exception);
    } else {
      return visitEventListExpressionProxy(block);
    }
  }

  public Object visitModuleProxy(final ModuleProxy module)
    throws VisitorException
  {
    final List<Proxy> parameters = new LinkedList<Proxy>();
    final List<Proxy> nonparameters = new LinkedList<Proxy>();
    for (final ConstantAliasProxy alias : module.getConstantAliasList()) {
      if (alias.getScope() == ScopeKind.LOCAL) {
        nonparameters.add(alias);
      } else {
        parameters.add(alias);
      }
    }
    for (final EventDeclProxy decl : module.getEventDeclList()) {
      if (decl.getScope() == ScopeKind.LOCAL) {
        nonparameters.add(decl);
      } else {
        parameters.add(decl);
      }
    }
    visitCollection(parameters);
    if (mParameterMap != null && !mParameterMap.isEmpty()) {
      // Throw exception when not all paremeters passed into the module
      // have been consumed---unless compiling in top-level context.
      final CompiledParameterBinding entry =
        mParameterMap.values().iterator().next();
      final ParameterBindingProxy binding = entry.getBinding();
      final String name = binding.getName();
      final UndefinedIdentifierException exception =
        new UndefinedIdentifierException(name, "parameter", binding);
      throw wrap(exception);
    }
    mParameterMap = null;
    visitCollection(nonparameters);
    final List<Proxy> aliases = module.getEventAliasList();
    visitCollection(aliases);
    final List<Proxy> components = module.getComponentList();
    visitCollection(components);
    return null;
  }

  public CompiledParameterBinding visitParameterBindingProxy
    (final ParameterBindingProxy binding)
    throws VisitorException
  {
    final String name = binding.getName();
    final ExpressionProxy expr = binding.getExpression();
    final Object value = expr.acceptVisitor(this);
    final CompiledParameterBinding compiled =
      new CompiledParameterBinding(binding, value);
    mParameterMap.put(name, compiled);
    return compiled;
  }

  public SimpleComponentProxy visitSimpleComponentProxy
    (final SimpleComponentProxy comp)
    throws VisitorException
  {
    try {
      mCurrentComponent = comp;
      final IdentifierProxy ident = comp.getIdentifier();
      final IdentifierProxy suffix = mNameCompiler.compileName(ident);
      final IdentifierProxy fullname =
        mNameSpace.getPrefixedIdentifier(suffix, mFactory);
      final ComponentKind kind = comp.getKind();
      final GraphProxy graph = comp.getGraph();
      final GraphProxy newgraph = visitGraphProxy(graph);
      final SimpleComponentProxy newcomp =
        mFactory.createSimpleComponentProxy(fullname, kind, newgraph);
      mNameSpace.addComponent(suffix, newcomp);
      mCompiledComponents.add(newcomp);
      addSourceInfo(newcomp, comp);
      return newcomp;
    } catch (final EvalException exception) {
      exception.provideLocation(comp);
      throw wrap(exception);
    } finally {
      mCurrentComponent = null;
    }
  }

  public SimpleExpressionProxy visitSimpleExpressionProxy
    (final SimpleExpressionProxy expr)
    throws VisitorException
  {
    try {
      return mSimpleExpressionCompiler.eval(expr, mContext);
    } catch (final EvalException exception) {
      throw wrap(exception);
    }
  }

  public SimpleNodeProxy visitSimpleNodeProxy(final SimpleNodeProxy node)
    throws VisitorException
  {
    final String name = node.getName();
    final boolean initial = node.isInitial();
    final PlainEventListProxy props0 = node.getPropositions();
    final CompiledEventList event = visitEventListExpressionProxy
      (props0, EventKindMask.TYPEMASK_PROPOSITION);
    final PlainEventListProxy props1 = createPlainEventList(event);
    final SimpleNodeProxy compiled =
      mFactory.createSimpleNodeProxy(name, props1, initial, null, null, null);
    mNodeMap.put(node, compiled);
    mCurrentNodes.add(compiled);
    addSourceInfo(compiled, node);
    return compiled;
  }

  public VariableComponentProxy visitVariableComponentProxy
    (final VariableComponentProxy var)
    throws VisitorException
  {
    try {
      final IdentifierProxy ident = var.getIdentifier();
      final IdentifierProxy suffix = mNameCompiler.compileName(ident);
      final IdentifierProxy fullname =
        mNameSpace.getPrefixedIdentifier(suffix, mFactory);
      final BindingContext context = new SinglePrefixingContext(suffix);
      final SimpleExpressionProxy expr = var.getType();
      final SimpleExpressionProxy value =
        mSimpleExpressionCompiler.eval(expr, mContext);
      mSimpleExpressionCompiler.getRangeValue(value);
      final boolean deterministic = var.isDeterministic();
      final SimpleExpressionProxy oldinit = var.getInitialStatePredicate();
      final SimpleExpressionProxy newinit =
        mSimpleExpressionCompiler.simplify(oldinit, context);
      final List<VariableMarkingProxy> oldmarkings = var.getVariableMarkings();
      final List<VariableMarkingProxy> newmarkings =
        new LinkedList<VariableMarkingProxy>();
      for (final VariableMarkingProxy oldmarking : oldmarkings) {
        final IdentifierProxy prop = oldmarking.getProposition();
        final CompiledEvent events =
          (CompiledEvent) visitIdentifierProxy(prop);
        final SimpleExpressionProxy oldpred = oldmarking.getPredicate();
        final SimpleExpressionProxy newpred =
          mSimpleExpressionCompiler.simplify(oldpred, context);
        final Iterable<SingleEventOutput> outputs =
          new EventOutputIterable(events);
        for (final SingleEventOutput output : outputs) {
          final CompiledSingleEvent event = output.getEvent();
          if (event.getKind() != EventKind.PROPOSITION) {
            final int mask = event.getKindMask();
            final EventKindException exception =
              new EventKindException(event, mask);
            exception.provideLocation(prop);
            throw exception;
          }
          final IdentifierProxy newident = createSingleEvent(output);
          final VariableMarkingProxy newmarking =
            mFactory.createVariableMarkingProxy(newident, newpred);
          newmarkings.add(newmarking);
        }
      }
      final VariableComponentProxy newvar =
        mFactory.createVariableComponentProxy
        (fullname, value, deterministic, newinit, newmarkings);
      mNameSpace.addComponent(suffix, newvar);
      mCompiledComponents.add(newvar);
      addSourceInfo(newvar, var);
      return newvar;
    } catch (final EvalException exception) {
      exception.provideLocation(var); // ???
      throw wrap(exception);
    }
  }


  //#########################################################################
  //# Specific Evaluation Methods
  private CompiledParameterBinding getParameterBinding
    (final IdentifierProxy ident, final ScopeKind scope)
    throws UndefinedIdentifierException
  {
    if (mParameterMap == null || scope == ScopeKind.LOCAL) {
      return null;
    }
    final CompiledParameterBinding binding;
    if (ident instanceof SimpleIdentifierProxy) {
      final SimpleIdentifierProxy simple = (SimpleIdentifierProxy) ident;
      final String name = simple.getName();
      binding = mParameterMap.remove(name);
    } else {
      binding = null;
    }
    if (binding == null && scope == ScopeKind.REQUIRED_PARAMETER) {
      final String paramname = ident.toString();
      throw new UndefinedIdentifierException
        (paramname, "required parameter", null);
    } else {
      return binding;
    }
  }

  private LabelBlockProxy createLabelBlock(final CompiledEventList events)
  {
    final List<IdentifierProxy> elist = new LinkedList<IdentifierProxy>();
    createEventList(events, elist);
    return mFactory.createLabelBlockProxy(elist, null);
  }

  private PlainEventListProxy createPlainEventList
    (final CompiledEventList events)
  {
    final List<IdentifierProxy> elist = new LinkedList<IdentifierProxy>();
    createEventList(events, elist);
    return mFactory.createPlainEventListProxy(elist);
  }

  private void createEventList(final CompiledEventList events,
                               final List<IdentifierProxy> elist)
  {
    final Iterable<SingleEventOutput> outputs =
      new EventOutputIterable(events);
    for (final SingleEventOutput output : outputs) {
      final IdentifierProxy ident = createSingleEvent(output);
      elist.add(ident);
    }
  }

  private IdentifierProxy createSingleEvent(final SingleEventOutput output)
  {
    final CompiledSingleEvent event = output.getEvent();
    IdentifierProxy ident = event.getIdentifier();
    if (ident == null) {
      final EventDeclProxy decl = createEventDecl(event);
      ident = decl.getIdentifier();
      event.setIdentifier(ident);
    }
    final IdentifierProxy iclone = ident.clone();
    addSourceInfo(iclone, output);
    return iclone;
  }

  private EventDeclProxy createEventDecl(final CompiledSingleEvent event)
  {
    final CompiledEventDecl cdecl = event.getCompiledEventDecl();
    final EventDeclProxy edecl = cdecl.getEventDeclProxy();
    final IdentifierProxy base = edecl.getIdentifier();
    final List<SimpleExpressionProxy> indexes = event.getIndexes();
    final IdentifierProxy suffix = mIndexAdder.addIndexes(base, indexes);
    final CompiledNameSpace namespace = cdecl.getNameSpace();
    final IdentifierProxy ident =
      namespace.getPrefixedIdentifier(suffix, mFactory);
    final EventKind kind = edecl.getKind();
    final boolean observable = edecl.isObservable();
    final EventDeclProxy decl = mFactory.createEventDeclProxy
      (ident, kind, observable, ScopeKind.LOCAL, null, null);
    mCompiledEvents.add(decl);
    addSourceInfo(decl, edecl);
    return decl;
  }

  private void addAdditionalBlockedEvents(final CompiledEvent event)
    throws EventKindException
  {
    if (mCurrentBlockedEvents == null) {
      mCurrentBlockedEvents = new CompiledEventList();
    }
    mCurrentBlockedEvents.addEvent(event);
  }

  private SourceInfo addSourceInfo(final IdentifierProxy target,
                                   final SingleEventOutput output)
  {
    final SourceInfo info = output.getSourceInfo();
    return mSourceInfoBuilder.add(target, info);
  }

  private SourceInfo addSourceInfo(final Proxy target, final Proxy source)
  {
    return mSourceInfoBuilder.add(target, source, mContext);
  }


  //#########################################################################
  //# Inner Class NameCompiler
  private class NameCompiler extends AbstractModuleProxyVisitor {

    //#######################################################################
    //# Invocation
    private IdentifierProxy compileName(final IdentifierProxy ident)
      throws VisitorException
    {
      return compileName(ident, true);
    }

    private IdentifierProxy compileName(final IdentifierProxy ident,
                                        final boolean cloning)
      throws VisitorException
    {
      mIsCloning = cloning;
      return (IdentifierProxy) ident.acceptVisitor(this);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public IndexedIdentifierProxy visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
      throws VisitorException
    {
      try {
        final String name = ident.getName();
        final List<SimpleExpressionProxy> indexes = ident.getIndexes();
        final List<SimpleExpressionProxy> values =
          new ArrayList<SimpleExpressionProxy>(indexes.size());
        boolean cloning = mIsCloning;
        for (final SimpleExpressionProxy index : indexes) {
          final SimpleExpressionProxy value =
            mSimpleExpressionCompiler.eval(index, mContext);
          values.add(value);
          cloning |= !index.equalsByContents(value);
        }
        if (cloning) {
          return mFactory.createIndexedIdentifierProxy(name, values);
        } else {
          return ident;
        }
      } catch (final EvalException exception) {
        exception.provideLocation(ident);
        throw wrap(exception);
      }
    }

    public QualifiedIdentifierProxy visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      final IdentifierProxy base0 = ident.getBaseIdentifier();
      final IdentifierProxy base1 =
        (IdentifierProxy) base0.acceptVisitor(this);
      final IdentifierProxy comp0 = ident.getComponentIdentifier();
      final IdentifierProxy comp1 =
        (IdentifierProxy) comp0.acceptVisitor(this);
      if (mIsCloning ||
          !base0.equalsByContents(base1) ||
          !comp0.equalsByContents(comp1)) {
        return mFactory.createQualifiedIdentifierProxy(base1, comp1);
      } else {
        return ident;
      }
    }

    public SimpleIdentifierProxy visitSimpleIdentifierProxy
      (final SimpleIdentifierProxy ident)
    {
      if (mIsCloning) {
        final ModuleProxyCloner cloner = mFactory.getCloner();
        return (SimpleIdentifierProxy) cloner.getClone(ident);
      } else {
        return ident;
      }
    }

    //#######################################################################
    //# Data Members
    private boolean mIsCloning;

  }


  //#########################################################################
  //# Inner Class NameCompiler
  private class IndexAdder extends AbstractModuleProxyVisitor {

    //#######################################################################
    //# Invocation
    private IdentifierProxy addIndexes
      (final IdentifierProxy ident,
       final List<SimpleExpressionProxy> indexes)
    {
      try {
        if (indexes.isEmpty()) {
          return ident;
        } else {
          mIndexes = indexes;
          return (IdentifierProxy) ident.acceptVisitor(this);
        }
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public IndexedIdentifierProxy visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
    {
      final String name = ident.getName();
      final List<SimpleExpressionProxy> indexes0 = ident.getIndexes();
      final int numindexes = indexes0.size() + mIndexes.size();
      final List<SimpleExpressionProxy> allindexes =
        new ArrayList<SimpleExpressionProxy>(numindexes);
      allindexes.addAll(indexes0);
      allindexes.addAll(mIndexes);
      return mFactory.createIndexedIdentifierProxy(name, mIndexes);
    }

    public QualifiedIdentifierProxy visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      final IdentifierProxy base = ident.getBaseIdentifier();
      final IdentifierProxy comp0 = ident.getComponentIdentifier();
      final IdentifierProxy comp1 =
        (IdentifierProxy) comp0.acceptVisitor(this);
      return mFactory.createQualifiedIdentifierProxy(base, comp1);
    }

    public IndexedIdentifierProxy visitSimpleIdentifierProxy
      (final SimpleIdentifierProxy ident)
    {
      final String name = ident.getName();
      return mFactory.createIndexedIdentifierProxy(name, mIndexes);
    }

    //#######################################################################
    //# Data Members
    private List<SimpleExpressionProxy> mIndexes;
  }


  //#########################################################################
  //# Inner Class NameSpaceVariablesContext
  private class NameSpaceVariablesContext implements BindingContext
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.compiler.context.BindingContext
    public SimpleExpressionProxy getBoundExpression
      (final SimpleExpressionProxy expr)
    {
      final SimpleExpressionProxy bound = mContext.getBoundExpression(expr);
      if (bound != null) {
        return bound;
      }
      if (expr instanceof IdentifierProxy) {
        final IdentifierProxy ident = (IdentifierProxy) expr;
        final IdentifiedProxy comp = mNameSpace.getComponent(ident);
        if (comp != null) {
          return comp.getIdentifier();
        }
      }
      return null;
    }

    public boolean isEnumAtom(final IdentifierProxy ident)
    {
      return mContext.isEnumAtom(ident);
    }

    public ModuleBindingContext getModuleBindingContext()
    {
      return mContext.getModuleBindingContext();
    }
  }


  //#########################################################################
  //# Inner Class SinglePrefixingContext
  private class SinglePrefixingContext implements BindingContext
  {
    //#######################################################################
    //# Constructor
    private SinglePrefixingContext(final IdentifierProxy suffix)
    {
      mSuffix = suffix;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.compiler.context.BindingContext
    public SimpleExpressionProxy getBoundExpression
      (final SimpleExpressionProxy expr)
    {
      if (expr.equalsByContents(mSuffix)) {
        final IdentifierProxy ident = (IdentifierProxy) expr;
        return mNameSpace.getPrefixedIdentifier(mSuffix, mFactory);
      } else {
        return mContext.getBoundExpression(expr);
      }
    }

    public boolean isEnumAtom(final IdentifierProxy ident)
    {
      if (ident.equalsByContents(mSuffix)) {
        return false;
      } else {
        return mContext.isEnumAtom(ident);
      }
    }

    public ModuleBindingContext getModuleBindingContext()
    {
      return mContext.getModuleBindingContext();
    }

    //#######################################################################
    //# Data Members
    private final IdentifierProxy mSuffix;
  }


  //#########################################################################
  //# Data Members
  private final DocumentManager mDocumentManager;
  private final ModuleProxyFactory mFactory;
  private final SourceInfoBuilder mSourceInfoBuilder;
  private final CompilerOperatorTable mOperatorTable;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final NameCompiler mNameCompiler;
  private final IndexAdder mIndexAdder;
  private final NameSpaceVariablesContext mNameSpaceVariablesContext;
  private final ModuleProxy mInputModule;

  private boolean mHasGuardActionBlocks;

  private BindingContext mContext;
  private CompiledNameSpace mNameSpace;
  private Collection<EventDeclProxy> mCompiledEvents;
  private Collection<Proxy> mCompiledComponents;
  private Map<String,CompiledParameterBinding> mParameterMap;

  private SimpleComponentProxy mCurrentComponent;
  private ProxyAccessorMap<IdentifierProxy> mCurrentAlphabet;
  private CompiledEventList mCurrentBlockedEvents;
  private List<NodeProxy> mCurrentNodes;
  private List<EdgeProxy> mCurrentEdges;
  private Map<NodeProxy,NodeProxy> mNodeMap;

  private EdgeProxy mCurrentEdge;
  private CompiledEventList mCurrentEventList;

}
