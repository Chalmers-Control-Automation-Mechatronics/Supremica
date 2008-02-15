//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   ModuleCompiler
//###########################################################################
//# $Id: ModuleCompiler.java,v 1.104 2008-02-15 07:31:49 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Set;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.expr.AtomValue;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.IndexValue;
import net.sourceforge.waters.model.expr.IntValue;
import net.sourceforge.waters.model.expr.RangeValue;
import net.sourceforge.waters.model.expr.SimpleValue;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.expr.Value;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ForeachComponentProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;

/*
 * Depend on org.supremica.automata ? Frown ...
 */
import org.supremica.automata.ExtendedAutomataExpander;


public class ModuleCompiler extends AbstractModuleProxyVisitor
{
 
  //##########################################################################
  //# Constructors
  public ModuleCompiler(final DocumentManager manager,
                        final ProductDESProxyFactory desfactory,
                        final ModuleProxy module)
  {
    mDocumentManager = manager;
    mDESFactory = desfactory;
    mModuleFactory = ModuleElementFactory.getInstance();
    mOperatorTable = CompilerOperatorTable.getInstance();
    mComparator = new ExpressionComparator();
    mDNFConverter =
        new DNFConverter(mModuleFactory, mOperatorTable, mComparator);
    mDNFMinimizer =
        new DNFMinimizer(mDNFConverter, mOperatorTable);
    mModule = module;
    mContext = null;
    mParameterMap = null;
    mCurrentEventID = 1;
  }


  //##########################################################################
  //# Invocation
  public ProductDESProxy compile()
    throws EvalException
  {
    return compile(null);
  }

  public ProductDESProxy compile(final List<ParameterBindingProxy> bindings)
    throws EvalException
  {
    try {
      final String name = mModule.getName();
      final String comment = mModule.getComment();
      final URI moduleLocation = mModule.getLocation();
      URI desLocation = null;
      if (moduleLocation != null) {
        try {
          final ProxyMarshaller<ProductDESProxy> marshaller =
            mDocumentManager.findProxyMarshaller(ProductDESProxy.class);
          final String ext = marshaller.getDefaultExtension();
          desLocation = moduleLocation.resolve(name + ext);
        } catch (final IllegalArgumentException exception) {
          // No marshaller --- O.K.
        }
      }
      mContext = new CompilerContext(mModule);
      mGlobalAlphabet = new TreeSet<EventProxy>();
      mAutomata = new TreeMap<String, AutomatonProxy>();
      if (bindings != null) {
        mParameterMap = new TreeMap<String,CompiledParameterBinding>();
        visitCollection(bindings);
      }
      mIsEFA = false;
      isEFA(mModule.getComponentList());
      if (mIsEFA) {
        // AARGH! We must not clone here!!!
        final ModuleProxyCloner cloner =
          ModuleSubjectFactory.getCloningInstance();
        final ModuleSubject subject = (ModuleSubject) cloner.getClone(mModule);
        ExtendedAutomataExpander.expandTransitions(subject);
        visitModuleProxy(subject);
      } else {
        // Only this case now really works ...
        visitModuleProxy(mModule);
      }
      return mDESFactory.createProductDESProxy
        (name, comment, desLocation, mGlobalAlphabet, mAutomata.values());
    } catch (final VisitorException exception) {
      final Throwable cause = exception.getCause();
      if (cause instanceof EvalException) {
        throw (EvalException) cause;
      } else {
        throw new WatersRuntimeException(cause);
      }
    } finally {
      mContext = null;
      mGlobalAlphabet = null;
      mAutomata = null;
    }
  }


  private void isEFA(List<Proxy> componentList)
  {
    for (final Proxy proxy : componentList) {
      if (proxy instanceof SimpleComponentProxy) {
        final SimpleComponentProxy comp = (SimpleComponentProxy) proxy;
        final Collection<EdgeProxy> edges = comp.getGraph().getEdges();
        mIsEFA = mIsEFA || componentHasNonEmptyGuardActionBlock(edges);
      } else if (proxy instanceof ForeachComponentProxy) {
        final ForeachComponentProxy comp = (ForeachComponentProxy) proxy;
        final List<Proxy> proxyList = comp.getBody();
        isEFA(proxyList);
      }
      if (mIsEFA) {
        return;
      }
    }
  }


  //##########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  public Value visitBinaryExpressionProxy(final BinaryExpressionProxy proxy)
    throws VisitorException
  {
    try {
      final SimpleExpressionProxy lhs = proxy.getLeft();
      final Value lhsValue = (Value) lhs.acceptVisitor(this);
      final SimpleExpressionProxy rhs = proxy.getRight();
      final Value rhsValue = (Value) rhs.acceptVisitor(this);
      final BinaryOperator operator = proxy.getOperator();
      return operator.eval(lhsValue, rhsValue);
    } catch (final EvalException exception) {
      exception.provideLocation(proxy);
      throw wrap(exception);
    }
  }

  public SimpleValue visitConstantAliasProxy(final ConstantAliasProxy proxy)
    throws VisitorException
  {
    try {
      final IdentifierProxy ident = proxy.getIdentifier();
      if (!(ident instanceof SimpleIdentifierProxy)) {
        throw new IndexOutOfRangeException(proxy);
      }
      final SimpleIdentifierProxy simple = (SimpleIdentifierProxy) ident;
      final String name = simple.getName();
      final ExpressionProxy defaultExpr = proxy.getExpression();
      final SimpleValue defaultValue =
        evalTyped(defaultExpr, SimpleValue.class, "LITERAL");
      final ScopeKind scope = proxy.getScope();
      final SimpleValue value =
        getParameterValue(defaultValue, SimpleValue.class, scope,
                          name, "LITERAL");
      mContext.add(name, value);
      return value;
    } catch (final EvalException exception) {
      exception.provideLocation(proxy);
      throw wrap(exception);
    }
  }

  public Object visitEdgeProxy(final EdgeProxy proxy)
    throws VisitorException
  {
    final EventListExpressionProxy labels = proxy.getLabelBlock();
    final List<Proxy> list = labels.getEventList();
    if (list.isEmpty()) {
      final EmptyLabelBlockException exception =
        new EmptyLabelBlockException(proxy, mCurrentComponentName);
      throw wrap(exception);
    }
    final NodeProxy source = proxy.getSource();
    final CompiledNode entry = mPrecompiledNodes.get(source);
    entry.addEdge(proxy);
    return null;
  }

  public Object visitEnumSetExpressionProxy(final EnumSetExpressionProxy proxy)
    throws VisitorException
  {
    final List<SimpleIdentifierProxy> items = proxy.getItems();
    final List<AtomValue> atoms = new ArrayList<AtomValue>(items.size());
    for (final SimpleIdentifierProxy item : items) {
      final String name = item.getName();
      final Value value = mContext.get(name);
      if (value == null) {
        try {
          final AtomValue atom = new CompiledAtomValue(name);
          mContext.add(name, atom);
          atoms.add(atom);
        } catch (final DuplicateIdentifierException exception) {
          throw new WatersRuntimeException(exception);
        }
      } else {
        try {
          final AtomValue atom = checkType(value, AtomValue.class, "ATOM");
          atoms.add(atom);
        } catch (final TypeMismatchException exception) {
          exception.provideLocation(item);
          throw wrap(exception);
        }
      }
    }
    return new CompiledEnumRangeValue(atoms);
  }

  public EventValue visitEventAliasProxy(final EventAliasProxy proxy)
    throws VisitorException
  {
    try {
      final IdentifierProxy ident = proxy.getIdentifier();
      final ExpressionProxy expr = proxy.getExpression();
      final EventValue event = evalTyped(expr, EventValue.class, "EVENT");
      if (ident instanceof SimpleIdentifierProxy) {
        final SimpleIdentifierProxy simple = (SimpleIdentifierProxy) ident;
        final String name = simple.getName();
        mContext.add(name, event);
      } else if (ident instanceof IndexedIdentifierProxy) {
        final IndexedIdentifierProxy indexed = (IndexedIdentifierProxy) ident;
        final String name = indexed.getName();
        final Value found = mContext.get(name);
        CompiledArrayAliasValue entry;
        if (found == null) {
          entry = new CompiledArrayAliasValue(name);
          mContext.add(name, entry);
        } else if (found instanceof CompiledArrayAliasValue) {
          entry = (CompiledArrayAliasValue) found;
        } else {
          throw new DuplicateIdentifierException(name);
        }
        final IndexedIdentifierProxy indexedIdent =
          (IndexedIdentifierProxy) ident;
        final List<SimpleExpressionProxy> indexes = indexedIdent.getIndexes();
        final Iterator<SimpleExpressionProxy> iter = indexes.iterator();
        while (iter.hasNext()) {
          final SimpleExpressionProxy indexExpr = iter.next();
          final IndexValue indexValue = evalIndex(indexExpr);
          final EventValue next = entry.get(indexValue);
          if (iter.hasNext()) {
            if (next == null) {
              final CompiledArrayAliasValue nextEntry =
                new CompiledArrayAliasValue(entry, indexValue);
              entry.set(indexValue, nextEntry);
              entry = nextEntry;
            } else if (next instanceof CompiledArrayAliasValue) {
              entry = (CompiledArrayAliasValue) next;
            } else {
              // throw DuplicateIdentifierException:
              entry.set(indexValue, null);
            }
          } else {
            entry.set(indexValue, event);
          }
        }
      } else {
        throw new UnsupportedOperationException
          ("Support for qualified indentifiers not yet implemented!");
      }
      return event;
    } catch (final EvalException exception) {
      exception.provideLocation(proxy);
      throw wrap(exception);
    }
  }

  public Object visitEventDeclProxy(final EventDeclProxy decl)
    throws VisitorException
  {
    try {
      final String name = decl.getName();
      final ScopeKind scope = decl.getScope();
      final EventValue value =
        getParameterValue(null, EventValue.class, scope, name, "EVENT");
      final List<SimpleExpressionProxy> declRanges = decl.getRanges();
      if (value == null) {
        final List<RangeValue> ranges =
          new ArrayList<RangeValue>(declRanges.size());
        for (final SimpleExpressionProxy expr : declRanges) {
          final RangeValue range = evalRange(expr);
          ranges.add(range);
        }
        final String fullname = mContext.getPrefixedName(name);
        final CompiledEventDecl entry =
          new CompiledEventDecl(fullname, decl, ranges);
        mContext.add(entry);
        return entry;
      } else {
        final EventKind kind = decl.getKind();
        final int mask = value.getKindMask();
        if (!EventKindMask.isAssignable(kind, mask) ||
            decl.isObservable() && !value.isObservable()) {
          throw new EventKindException(decl, value);
        }
        final Iterator<SimpleExpressionProxy> declIter =
          declRanges.iterator();
        final List<RangeValue> valueRanges = value.getIndexRanges();
        final Iterator<RangeValue> valueIter = valueRanges.iterator();
        int index = 0;
        while (declIter.hasNext()) {
          if (!valueIter.hasNext()) {
            throw new EventKindException(decl, value, index);
          }
          final SimpleExpressionProxy expr = declIter.next();
          final RangeValue declRange = evalRange(expr);
          final RangeValue valueRange = valueIter.next();
          if (!declRange.equals(valueRange)) {
            throw new EventKindException(decl, value, index, declRange);
          }
          index++;
        }
        mContext.add(name, value);
        return value;
      }
    } catch (final EvalException exception) {
      exception.provideLocation(decl);
      throw wrap(exception);
    }
  }

  public CompiledEventListValue visitEventListExpressionProxy
    (final EventListExpressionProxy proxy)
    throws VisitorException
  {
    return visitEventListExpressionProxy(proxy, EventKindMask.TYPEMASK_ANY);
  }

  public CompiledEventListValue visitEventListExpressionProxy
    (final EventListExpressionProxy proxy,
     final int mask)
    throws VisitorException
  {
    try {
      mEventList = new CompiledEventListValue(mask);
      final List<Proxy> list = proxy.getEventList();
      visitCollection(list);
      return mEventList;
    } finally {
      mEventList = null;
    }
  }

  public Object visitForeachProxy(final ForeachProxy proxy)
    throws VisitorException
  {
    final CompiledEventListValue savedEventList = mEventList;
    try {
      mEventList = null;
      final String name = proxy.getName();
      final SimpleExpressionProxy rangeExpr = proxy.getRange();
      final RangeValue range = evalRange(rangeExpr);
      for (final IndexValue item : range.getValues()) {
        mContext.add(name, item);
        try {
          final SimpleExpressionProxy guardExpr = proxy.getGuard();
          if (guardExpr == null || evalBoolean(guardExpr)) {
            final List<Proxy> body = proxy.getBody();
            mEventList = savedEventList;
            visitCollection(body);
            mEventList = null;
          }
        } finally {
          mContext.unset(name);
        }
      }
      return null;
    } catch (final DuplicateIdentifierException exception) {
      exception.provideLocation(proxy);
      throw wrap(exception);
    } finally {
      mEventList = savedEventList;
    }
  }

  public CompiledNode visitGroupNodeProxy(final GroupNodeProxy proxy)
    throws VisitorException
  {
    final CompiledNode compiled = new CompiledNode(proxy);
    mPrecompiledNodes.put(proxy, compiled);
    return compiled;
  }

  public Value visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy proxy)
    throws VisitorException
  {
    try {
      Value value;
      final CompiledEventListValue eventList = mEventList;
      mEventList = null;
      try {
        final List<SimpleExpressionProxy> indexes = proxy.getIndexes();
        final List<IndexValue> indexValues =
          new ArrayList<IndexValue>(indexes.size());
        for (final SimpleExpressionProxy indexExpr : indexes) {
          final IndexValue indexValue = evalIndex(indexExpr);
          indexValues.add(indexValue);
        }
        final String name = proxy.getName();
        value = mContext.find(name, indexValues, indexes);
      } finally {
        mEventList = eventList;
      }
      processEvent(value);
      return value;
    } catch (final EvalException exception) {
      exception.provideLocation(proxy);
      throw wrap(exception);
    }
  }

  public Object visitInstanceProxy(final InstanceProxy proxy)
    throws VisitorException
  {
    final CompilerContext oldContext = mContext;
    final Map<String,CompiledParameterBinding> oldParameterMap =
      mParameterMap;
    try {
      final IdentifierProxy ident = proxy.getIdentifier();
      final String name = (String) ident.acceptVisitor(mNameCompiler);
      final String fullName = mContext.getPrefixedName(name);
      final List<ParameterBindingProxy> bindings = proxy.getBindingList();
      mParameterMap = new TreeMap<String,CompiledParameterBinding>();
      visitCollection(bindings);
      final URI uri = mContext.getURI();
      final String filename = proxy.getModuleName();
      final ModuleProxy module =
        mDocumentManager.load(uri, filename, ModuleProxy.class);
      mContext = new CompilerContext(module, fullName);
      visitModuleProxy(module);
      return null;
    } catch (final IOException exception) {
      final InstantiationException next =
        new InstantiationException(exception, proxy);
      throw wrap(next);
    } catch (final WatersUnmarshalException exception) {
      final InstantiationException next =
        new InstantiationException(exception, proxy);
      throw wrap(next);
    } finally {
      mContext = oldContext;
      mParameterMap = oldParameterMap;
    }
  }

  public IntValue visitIntConstantProxy(final IntConstantProxy proxy)
  {
    return new CompiledIntValue(proxy.getValue());
  }

  public ProductDESProxy visitModuleProxy(final ModuleProxy module)
    throws VisitorException
  {
    // EFA
    mEFATransitionAutomatonMap =
      new HashMap<TransitionProxy,AutomatonProxy>();
    mEFATransitionGuardActionBlockMap =
      new HashMap<TransitionProxy, GuardActionBlockProxy>();
    mVariableComponents = new LinkedList<VariableComponentProxy>();
    mEFAEventEventMap = new HashMap<EventProxy, EventProxy>();
    mOriginalAlphabet = new TreeSet<EventProxy>();
    // END EFA

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
      final CompiledParameterBinding entry =
        mParameterMap.values().iterator().next();
      final ParameterBindingProxy binding = entry.getBinding();
      final String name = binding.getName();
      final UndefinedIdentifierException exception =
        new UndefinedIdentifierException(name, "parameter", binding);
      throw wrap(exception);
    }
    visitCollection(nonparameters);
    final List<Proxy> aliases = module.getEventAliasList();
    visitCollection(aliases);
    final List<Proxy> components = module.getComponentList();
    visitCollection(components);
    if (mIsEFA) {
      compileEFA();
    }
    return null;
  }

  public CompiledParameterBinding visitParameterBindingProxy
    (final ParameterBindingProxy proxy)
    throws VisitorException
  {
    final String name = proxy.getName();
    final ExpressionProxy expr = proxy.getExpression();
    final Value value = (Value) expr.acceptVisitor(this);
    final CompiledParameterBinding binding =
      new CompiledParameterBinding(proxy, value);
    mParameterMap.put(name, binding);
    return binding;
  }

  /*
   * Remark: possible to change return, AutomatonProxy to
   * Map<String,AutomatonProxy>?
   */
  public AutomatonProxy visitSimpleComponentProxy
    (final SimpleComponentProxy proxy)
    throws VisitorException
  {
    try {
      final IdentifierProxy ident = proxy.getIdentifier();
      final String name = (String) ident.acceptVisitor(mNameCompiler);
      mCurrentComponentName = mContext.getPrefixedName(name);
      if (mAutomata.containsKey(mCurrentComponentName)) {
        throw new DuplicateIdentifierException(name, "Automaton", proxy);
      }
      final ComponentKind kind = proxy.getKind();
      final GraphProxy graph = proxy.getGraph();
      final boolean deterministic;
      if (!mIsEFA) {
        deterministic = graph.isDeterministic();
        mMaxInitialStates = deterministic ? 1 : -1;
      } else {
        /*
         * TODO: Check when EFA models are deterministic by 
         * evaluating guard expressions.
         */
        deterministic = false;
        mMaxInitialStates = 1;
      }
      mLocalAlphabet = new TreeSet<EventProxy>();
      final EventListExpressionProxy blockedExpr = graph.getBlockedEvents();
      if (blockedExpr != null) {
        final CompiledEventListValue blocked =
          visitEventListExpressionProxy(blockedExpr);
        createAutomatonEvents(blocked);
      }
      final Collection<NodeProxy> nodes = graph.getNodes();
      mStates = new TreeSet<StateProxy>();
      //mMaxInitialStates = deterministic ? 1 : -1;
      mPrecompiledNodes =
        new IdentityHashMap<NodeProxy, CompiledNode>(nodes.size());
      visitCollection(nodes);
      final Collection<EdgeProxy> edges = graph.getEdges();
      visitCollection(edges);
      mTransitions = new TreeSet<TransitionProxy>();
      for (final NodeProxy source : nodes) {
        final CompiledNode sourceEntry = mPrecompiledNodes.get(source);
        for (final EdgeProxy edge : sourceEntry.getEdges()) {
          final NodeProxy target = edge.getTarget();
          final EventListExpressionProxy labelBlock = edge.getLabelBlock();
          final CompiledEventListValue events =
            visitEventListExpressionProxy(labelBlock,
                                          EventKindMask.TYPEMASK_EVENT);
          createAutomatonEvents(events);
          /*
           * In method createTransitions, mEFATransitionGuardActionBlockMap is filled.
           */
          createTransitions(source, events, target, sourceEntry,
                            deterministic, edge);
        }
        sourceEntry.clearProperChildNodes();
      }

      /*
       * States in different Automata are considered equal if the names are
       * the same.  This must be fixed since it leads to problems in the
       * mappings: mEFATransitionGuardActionBlockMap and
       * mEFATransitionAutomatonMap.
       */

      /*
       * The automaton is created. If there exists an EFA-automaton
       * in the Module, all components are collected in a list.
       * Events will be relabeled, transitions divided and
       * variableAutmata will be created, in the method compileEFA().
       * 
       */
      if (mIsEFA) {
    	  Map <StateProxy, StateProxy> stateStateMap =
          new HashMap<StateProxy, StateProxy>();
        /*
         * Rename all states.
         */
        for(StateProxy state: mStates){
          final StateProxy s =
            mDESFactory.createStateProxy(name+"("+state.getName()+")",
                                      state.isInitial(),
                                      state.getPropositions());
          stateStateMap.put(state,s);
        }
        /*
         * Rename all transitions. Update mapping: transition to
         * GuardActionBlock.
         */
        LinkedList<TransitionProxy> Transitions =
          new LinkedList<TransitionProxy>();

        for (TransitionProxy transition : mTransitions) {

          StateProxy source = transition.getSource();
          StateProxy target = transition.getTarget();

          for (StateProxy state : mStates) {
            if (transition.getSource().equals(state)) {
              source = stateStateMap.get(state);
            }
            if (transition.getTarget().equals(state)) {
              target = stateStateMap.get(state);
            }

          }
          TransitionProxy trans =
            mDESFactory.createTransitionProxy(source,
                                           transition.getEvent(),
                                           target);
          if (mEFATransitionGuardActionBlockMap.containsKey(transition)){
            mEFATransitionGuardActionBlockMap.put
              (trans, mEFATransitionGuardActionBlockMap.get(transition));
            mEFATransitionGuardActionBlockMap.remove(transition);
          }
          Transitions.add(trans);
        }
        /*
         * Update transitions and states.
         */
        mTransitions.clear();
        mTransitions.addAll(Transitions);
        mStates.clear();
        mStates.addAll(stateStateMap.values());
        final AutomatonProxy aut =
          mDESFactory.createAutomatonProxy(mCurrentComponentName,
                                        kind, mLocalAlphabet,
                                        mStates, mTransitions);
        for (TransitionProxy trans : mTransitions) {
          mEFATransitionAutomatonMap.put(trans, aut);
        }
        mAutomata.put(mCurrentComponentName, aut);
        return aut;
      } else {
        final AutomatonProxy aut =
          mDESFactory.createAutomatonProxy(mCurrentComponentName,
                                        kind, mLocalAlphabet,
                                        mStates, mTransitions);
        mAutomata.put(mCurrentComponentName, aut);
        return aut;
      }
    } catch (final DuplicateIdentifierException exception) {
      throw wrap(exception);
    } finally {
      mLocalAlphabet = null;
      mStates = null;
      mPrecompiledNodes = null;
      mTransitions = null;
    }
  }

  public Value visitSimpleIdentifierProxy
    (final SimpleIdentifierProxy proxy)
    throws VisitorException
  {
    try {
      final String name = proxy.getName();
      final Value value = mContext.find(name);
      processEvent(value);
      return value;
    } catch (final EvalException exception) {
      exception.provideLocation(proxy);
      throw wrap(exception);
    }
  }

  public CompiledNode visitSimpleNodeProxy(final SimpleNodeProxy proxy)
    throws VisitorException
  {
    final String name = proxy.getName();
    final boolean initial = proxy.isInitial();
    final Collection<EventProxy> stateProps = new TreeSet<EventProxy>();
    final EventListExpressionProxy nodeProps = proxy.getPropositions();
    final EventValue value =
      visitEventListExpressionProxy(nodeProps,
                                    EventKindMask.TYPEMASK_PROPOSITION);
    createAutomatonEvents(value);
    final Iterator<CompiledSingleEventValue> iter =
      value.getEventIterator();
    while (iter.hasNext()) {
      final CompiledSingleEventValue event = iter.next();
      final EventProxy eventProxy = event.getEventProxy();
      stateProps.add(eventProxy);
    }
    final StateProxy state =
      mDESFactory.createStateProxy(name, initial, stateProps);
    if (initial) {
      switch (mMaxInitialStates) {
      case 1:
        mMaxInitialStates = 0;
        break;
      case 0:
        final NondeterministicModuleException exception =
          new NondeterministicModuleException(mCurrentComponentName, state);
        throw wrap(exception);
      default:
        break;
      }
    }
    mStates.add(state);
    final CompiledNode compiled = new CompiledNode(proxy, state);
    mPrecompiledNodes.put(proxy, compiled);
    return compiled;
  }

  public Value visitUnaryExpressionProxy
    (final UnaryExpressionProxy proxy)
    throws VisitorException
  {
    try {
      final SimpleExpressionProxy subTerm = proxy.getSubTerm();
      final Value subValue = (Value) subTerm.acceptVisitor(this);
      final UnaryOperator operator = proxy.getOperator();
      return operator.eval(subValue);
    } catch (final EvalException exception) {
      exception.provideLocation(proxy);
      throw wrap(exception);
    }
  }

  public Object visitVariableComponentProxy
    (final VariableComponentProxy var)
  {
    if (mIsEFA) {
      mVariableComponents.add(var);
    }
    return null;
  }


  //##########################################################################
  //# EFA
  private void compileEFA()
    throws VisitorException
  {
	  /*
	   * Create mutable automata.
	   */
	  mCompiledAutomata = new TreeMap<String, CompiledAutomaton>();
		for (final AutomatonProxy aut : mAutomata.values()) {
			mCompiledAutomata.put(aut.getName(), new CompiledAutomaton(aut
					.getName(), aut.getKind(), aut.getEvents(),
					aut.getStates(), aut.getTransitions()));
		}
		
	/*
	 * Mappings used to create variable automata instantiated.
	 */
    mEFAEventGuardClauseMap = new HashMap<EventProxy,SimpleExpressionProxy>();
    mEFAEventActionListsMap = new HashMap<EventProxy, List<List<BinaryExpressionProxy>>>();
    /*
     * Mappings used to handle controllability instantiated. All variable automata 
     * translated as plants.
     */
//    mEventForbiddenStatesMap= new HashMap<EventProxy,LocationsAndExpression>();
    mForbiddenStates= new HashSet<LocationsAndExpression>();
    /*
     * Because of a stupid parser that does not simplify x>1 & x<1 to
     * false, we use the extra set mForbiddenEvents. Only after we have
     * built all variable automata we know which forbidden events need to
     * be used.
     */
    mForbiddenEvents= new HashSet<EventProxy>();
    /*
     * Copy the GlobalAlphabet.
     */
    final Set<EventProxy> newEvents = new HashSet<EventProxy>();
    newEvents.addAll(mOriginalAlphabet);
    try {
      for (final EventProxy event : mOriginalAlphabet) {
        EventProxy orgEvent=findOriginalEvent(event);
    	  /*
         * Find all possible transitions
         * (synch-combinations between the automata)
         * that the event can trigger(allTrans = allPaths).
         */
            /*
        	 * In order to avoid unneccessary relabelling, 
        	 * it is important that the forbidden states are collected "first".
        	 * 
        	 */
        	if (orgEvent.getKind() == EventKind.UNCONTROLLABLE) {
				findForbiddenStates(orgEvent, newEvents);
			}	
    	  List<List<TransitionProxy>> allTrans = allPossibleTransitions(orgEvent);
    	  
        /*
		 * Collect guard and actions. Split the guards into andClauses. Update
		 * the automata with the final events and transitions. Fill mappings,
		 * mEFAEventGuardClauseMap and mEFAEventActionListMap, needed to build
		 * variable automata.
		 */
        for (List<TransitionProxy> path : allTrans) {
          // The event is renamed in two steps: first for each path and
          // then for each andClause in the guard expression.
          SimpleExpressionProxy guard = collectGuard(path);
          if(orgEvent.getKind()== EventKind.UNCONTROLLABLE){
        	  guard = collectPlantGuard(path);
          }
       
          final CompiledNormalForm dnf = mDNFConverter.convertToDNF(guard);
          List<SimpleExpressionProxy> sortedAndClauses =
              mDNFConverter.createSortedClauseList(dnf);
          if(!dnf.isEmpty()){
          final CompiledNormalForm mdnf = mDNFMinimizer.minimize(dnf);
          sortedAndClauses = mDNFConverter.createSortedClauseList(mdnf);
          }
 //         if (!sortedAndClauses.isEmpty()) {
            newEvents.remove(orgEvent);
            final List<List<BinaryExpressionProxy>> actionLists =
              collectAction(path);
            for (final SimpleExpressionProxy expr : sortedAndClauses) {
              final String eventname =
                orgEvent.getName() + "_" + mCurrentEventID++;
              final EventProxy relabeledEvent =
                mDESFactory.createEventProxy(eventname,
                                             orgEvent.getKind(),
                                             orgEvent.isObservable());
              mEFAEventGuardClauseMap.put(relabeledEvent, expr);
              mEFAEventActionListsMap.put(relabeledEvent, actionLists);
              newEvents.add(relabeledEvent);
              mEFAEventEventMap.put(relabeledEvent, orgEvent);
              // New transitions with the new event names are added to
              // the automata.
              addNewTransitionsToCompiledAutomtata(relabeledEvent, path);
   //          }
          }
        }
      }
      
      
      createForbiddenEvents(newEvents);
      
      /*
       * The variable automata are constructed using mVariableComponents,
       * mEFAEventGuardClauseMap and mEFAEventActionListMap. To handle
       * controllability it is important that the variable automata are built
       * before old transitions are removed.
       */
      /* 
       * Forbidden self loops are first added to the variableAutomata.
       */
      buildCompiledVariableAutomata();
      /*
       * All transitions with old event names are removed. 
       */
      updateTransitionsInCompiledAutomata();
      /* In order to avoid fictional uncontrollable 
       * states caused by the relabelling of events 
       * specifications are transformed into plants. 
       * This is done in such a way that if one or more 
       * relabelled uncontrollable 
       * events are allowed  by the spec i.e. no extra 
       * selfloops triggered by relabellings of that particular event 
       * are added at that state. No optimizations are done yet.
       */
      plantifyCompiledSpec();
      /* 
       * Specifications that forbids events are added.
       */
      addSingleStateCompiledSpec();
      /* Forbidden self loops are added to the rest of the plants
      *  (i.e. not to the variableAutomata. ).
      */
      createForbiddenSelfLoopsInCompiledAutomata();
    
      mGlobalAlphabet.clear();
      mGlobalAlphabet.addAll(newEvents);
      mAutomata.clear();
      mAutomata.putAll(mCompiledAutomata);
    } catch (final EvalException exception) {
      throw wrap(exception);
    }
  }

  private void addSingleStateCompiledSpec()
  {
    // No propositions here. All states are implicitly marked with all
    // propsositions in the model.
    for (final EventProxy event: mForbiddenEvents) {
      final String name = event.getName(); 
      final Collection<EventProxy> alphabet = Collections.singletonList(event);
      final StateProxy state = mDESFactory.createStateProxy(name, true, null);
      Collection<StateProxy> states = Collections.singletonList(state);
      Collection<TransitionProxy> transitions = Collections.emptyList();
      final CompiledAutomaton automaton =
        new CompiledAutomaton(name,
                              ComponentKind.SPEC,
                              alphabet,
                              states,
                              transitions);
      mCompiledAutomata.put(name, automaton);
    }
  }

private void createForbiddenSelfLoopsInCompiledAutomata() {		
	for (EventProxy event: mForbiddenEvents){
	for(String loc: mEventForbiddenLocations.get(event)){
	for(CompiledAutomaton aut: mCompiledAutomata.values()){
	if(aut.getKind()== ComponentKind.PLANT){
		for (StateProxy state: aut.getStates()){
			if (state.getName().equals(loc)){
				final TransitionProxy selfloop =
                    mDESFactory.createTransitionProxy(state, event, state);
				aut.addEvent(event);
                aut.addTransition(selfloop);
            }}}}}}}


/*
private void createForbiddenSelfLoopsInAutomata() {		
		for (EventProxy event: mForbiddenEvents){
		Set<String> namesOfAutomata= new TreeSet<String>();
		namesOfAutomata.addAll(mAutomata.keySet());
		for(String loc: mEventForbiddenStatesMap.get(event).getLocations()){
		for(String name: namesOfAutomata){
		if(mAutomata.get(name).getKind()== ComponentKind.PLANT){
			for (StateProxy state: mAutomata.get(name).getStates()){
				if (state.getName().equals(loc)){
					
					final TransitionProxy selfloop =
                        mDESFactory.createTransitionProxy(state,
                        		event,
                                                          state);
					Collection<EventProxy> alphabet= new TreeSet<EventProxy>();
					alphabet.addAll(mAutomata.get(name).getEvents());
                    alphabet.add(event);
            		Collection <TransitionProxy> transitions =new TreeSet<TransitionProxy>();
            		transitions.addAll(mAutomata.get(name).getTransitions());
            		transitions.add(selfloop);
            		final AutomatonProxy updatedAutomaton =
            	        mDESFactory.createAutomatonProxy(name,
            	                                       ComponentKind.PLANT,
            	                                       alphabet,
            	                                       mAutomata.get(name).getStates(),
            	         transitions);
            		mAutomata.remove(name);
            	        mAutomata.put(name,updatedAutomaton);
            		
                }}}}}}}
*/

private void plantifyCompiledSpec() {
	
	final Map<String,CompiledAutomaton> newPlantAutomata =
	      new TreeMap<String,CompiledAutomaton>();
	    
	for(AutomatonProxy aut: mCompiledAutomata.values()){
		if(aut.getKind()== ComponentKind.SPEC){
		Collection<TransitionProxy> transitions= new TreeSet<TransitionProxy>();
	    transitions.addAll(aut.getTransitions());
		for(EventProxy relabelledEvent: aut.getEvents()){
			if(relabelledEvent.getKind()== EventKind.UNCONTROLLABLE){
				for(StateProxy state: aut.getStates()){
					if(originalEventDisabledAtState(aut, state, relabelledEvent)){
						final TransitionProxy selfloop =
	                        mDESFactory.createTransitionProxy(state,
	                        		relabelledEvent,
	                                                          state);
						transitions.add(selfloop);
					}
									}
			}}
	    String name="Plant("+aut.getName()+")";
	    final CompiledAutomaton plantifiedAut =
	      new CompiledAutomaton(name,
	                                       ComponentKind.PLANT,
	                                       aut.getEvents(),
	                                       aut.getStates(),
	                                       transitions);
	    newPlantAutomata.put(name,plantifiedAut);
	}}
	mCompiledAutomata.putAll(newPlantAutomata);
}

/*
private void plantifySpec() {
	
	final Map<String,AutomatonProxy> newPlantAutomata =
	      new TreeMap<String,AutomatonProxy>();
	    
	for(AutomatonProxy aut: mAutomata.values()){
		if(aut.getKind()== ComponentKind.SPEC){
		Collection<TransitionProxy> transitions= new TreeSet<TransitionProxy>();
	    transitions.addAll(aut.getTransitions());
		for(EventProxy relabelledEvent: aut.getEvents()){
			if(relabelledEvent.getKind()== EventKind.UNCONTROLLABLE){
				for(StateProxy state: aut.getStates()){
					if(originalEventDisabledAtState(aut, state, relabelledEvent)){
						final TransitionProxy selfloop =
	                        mDESFactory.createTransitionProxy(state,
	                        		relabelledEvent,
	                                                          state);
						transitions.add(selfloop);
					}
									}
			}}
	    String name="Plant("+aut.getName()+")";
	    final AutomatonProxy plantifiedAut =
	      mDESFactory.createAutomatonProxy(name,
	                                       ComponentKind.PLANT,
	                                       aut.getEvents(),
	                                       aut.getStates(),
	                                       transitions);
	    newPlantAutomata.put(name,plantifiedAut);
	}}
	mAutomata.putAll(newPlantAutomata);
}
*/

private boolean originalEventDisabledAtState(AutomatonProxy aut, StateProxy state, EventProxy relabelledEvent) {	
	for(TransitionProxy trans: aut.getTransitions()){
		if(trans.getSource()== state){
			EventProxy originalEvent = findOriginalEvent(trans.getEvent());
			final Set<EventProxy> allRelabelled = new HashSet<EventProxy>();
			for(EventProxy event: mEFAEventEventMap.keySet()){
				if(mEFAEventEventMap.get(event)== originalEvent){
					allRelabelled.add(event);
				}
			}
			if(allRelabelled.contains(relabelledEvent)){
				return false;
			}
		}		
	}
	return true;
}


private EventProxy findOriginalEvent(EventProxy event) {
	if(mEFAEventEventMap.isEmpty()){
		return event;
	}
	else if(!mEFAEventEventMap.containsKey(event)){
		return event;
	}
	else{
		return findOriginalEvent(mEFAEventEventMap.get(event));
	}
}

private void findForbiddenStates(final EventProxy event, Set<EventProxy> newEvents) 
throws EvalException {
	/*
	 * If a specification automaton contains the
	 * event in the alphabet but not in transitions forbidden
	 * is set to true, i.e non-empty.  
	 */
	Set<Boolean> forbidden =
        new HashSet<Boolean>();
	/*
	 * At position "0" we have allPlantPaths at position
	 * "1" allSpecPaths.
	 */
	final List<List<List<TransitionProxy>>> plantSpecTrans = plantSpecTransitions(
			event, forbidden);
	/*
	 * For each possible trans, collect the
	 * plant guards and spec guards.
	 */
	for (List<TransitionProxy> plantTrans : plantSpecTrans
			.get(0)) {
		
		
		final SimpleExpressionProxy plantGuard = collectGuard(plantTrans);
		CompiledNormalForm dnfPlant = mDNFConverter
				.convertToDNF(plantGuard);
		/*
		 * dnfPlant.isEmpty() means plantguard always true.
		 */
		if(!dnfPlant.isEmpty()){
		CompiledNormalForm mdnfPlant = mDNFMinimizer.minimize(dnfPlant);
		List<SimpleExpressionProxy> sortedPlantClauses = mDNFConverter
				.createSortedClauseList(mdnfPlant);
		/*
		 * A specification automaton contains the
	     * event in the alphabet but not in transitions.
		 */
		if(!forbidden.isEmpty()&& containsVariableCondition(sortedPlantClauses)){
			Set<String> forbiddenLoc=new TreeSet<String>();
			/*
			 * Collect forbidden locations. Since the event 
			 * is forbidden only plant locations are relevant. 
			 */
			for (TransitionProxy plant : plantTrans) {
				forbiddenLoc.add(plant
						.getSource()
						.getName());
			}			

			boolean createLocation=true;
			for(LocationsAndExpression locExp: mForbiddenStates){
				if(forbiddenLoc.equals(locExp.getLocations())){
					addGuardToExpression(event.getName(), locExp, sortedPlantClauses);
					createLocation=false;
					break;
			}
				}
			if(createLocation){	
				mForbiddenStates.add
		(new LocationsAndExpression(event.getName(), forbiddenLoc, sortedPlantClauses));
			
				}
		/*
		 * If the plant guard is always true we still have a controllability problem 
		 * i.e. either way we must forbid the event. The case when 
		 * the plant guard always is false should correspond to the case 
		 * when sortedPlantClauses.isEmpty().
		 */
	}		
else{
		for (final SimpleExpressionProxy plantExpr : sortedPlantClauses) {
			for (List<TransitionProxy> specTrans : plantSpecTrans
					.get(1)) {
				if (!specTrans.isEmpty()) {
					final SimpleExpressionProxy specGuard = collectGuard(specTrans);
						/*
						 * Collect forbidden guards and
						 * locations.
						 */
						final SimpleExpressionProxy uncGuard = 
							collectUncontrollableGuard(plantExpr, specGuard);
						final CompiledNormalForm dnfUncGuard = mDNFConverter
								.convertToDNF(uncGuard);
						if(!dnfUncGuard.isEmpty()){
						final CompiledNormalForm mdnfUncGuard = mDNFMinimizer.minimize(dnfUncGuard);
						List<SimpleExpressionProxy> sortedUncClauses = mDNFConverter
								.createSortedClauseList(mdnfUncGuard);
						
						if (containsVariableCondition(sortedUncClauses)) {
							Set<String> fLoc=new TreeSet<String>();
							for (TransitionProxy plant : plantTrans) {
								fLoc.add(plant
										.getSource()
										.getName());
								for (TransitionProxy spec : specTrans) {
									String name=spec.getSource().getName(); 
									fLoc.add(name);
								}
							}
						
							boolean createNewLocation=true;
							for(LocationsAndExpression locExp: mForbiddenStates){
								if(fLoc.equals(locExp.getLocations())){
									addGuardToExpression(event.getName(), 
											locExp, sortedUncClauses);
									createNewLocation=false;
									break;
							}
								}
							
							if(createNewLocation){
							mForbiddenStates.add
						(new LocationsAndExpression(event.getName(), fLoc, sortedUncClauses));
							}	
							}
					}
				}
			}
		}}}}
}
	private boolean containsVariableCondition(
			List<SimpleExpressionProxy> clauses) {
		for (VariableComponentProxy variable : mVariableComponents) {
			try {
				VariableSearcher searcher = new VariableSearcher(variable);
				for (SimpleExpressionProxy clause : clauses) {
					if (searcher.search(clause)) {
						return true;
					}
				}
			} catch (final VisitorException exception) {
				exception.getStackTrace();
			}

		}
		return false;
	}
	
	
					
private void addGuardToExpression(String eventName, LocationsAndExpression locExp, List<SimpleExpressionProxy> sortedAndClauses) throws EvalException {
	SimpleExpressionProxy guard;
	SimpleExpressionProxy guardExp1 = mModuleFactory.createIntConstantProxy(0);
	SimpleExpressionProxy guardExp2 = mModuleFactory.createIntConstantProxy(0);
	final BinaryOperator orOp = mOperatorTable.getOrOperator();
	final BinaryOperator andOp = mOperatorTable.getAndOperator();
	for (SimpleExpressionProxy clause: sortedAndClauses){
    	guardExp1= mModuleFactory.createBinaryExpressionProxy(orOp, clause, guardExp1);
    	}
	
   for(SimpleExpressionProxy exp: locExp.getExpression()){
	   guardExp2= mModuleFactory.createBinaryExpressionProxy(orOp, exp, guardExp2); 	
   }
   if(locExp.getEvent()== eventName){
   guard = mModuleFactory.createBinaryExpressionProxy(andOp, guardExp1, guardExp2);
   }
   else{
   guard = mModuleFactory.createBinaryExpressionProxy(orOp, guardExp1, guardExp2);
	}
   final CompiledNormalForm dnf = mDNFConverter.convertToDNF(guard);
	if(!dnf.isEmpty()){
	final CompiledNormalForm mdnf = mDNFMinimizer.minimize(dnf);
	locExp.setExpression(mDNFConverter
			.createSortedClauseList(mdnf));
	}
	else{
		locExp.setExpression(new LinkedList<SimpleExpressionProxy>());
	}
	
}


private void updateTransitionsInCompiledAutomata()
  {
    for (final CompiledAutomaton aut: mCompiledAutomata.values()) {
    	Set<EventProxy> events = new TreeSet<EventProxy>();
        events.addAll(mEFAEventEventMap.values());
    	for (final EventProxy event: events) {
    	  final Collection<TransitionProxy> transitions =
    	        new TreeSet<TransitionProxy>();
    	     transitions.addAll(aut.getTransitions());
    	  for (final TransitionProxy trans: transitions){
          aut.removeTransition(trans.getSource().getName(), trans.getTarget().getName(), event.getName());
          }
        aut.removeEvent(event.getName());
        }
      }
  }

/*
 * All transitions with old event names are removed. 
 */
/*private void updateTransitionsInAutomata()
  {
    final Map<String,AutomatonProxy> newAutomata =
      new TreeMap<String,AutomatonProxy>();
    for (final AutomatonProxy aut: mAutomata.values()) {
      final Set<EventProxy> efaEvents = new TreeSet<EventProxy>();
      final Collection<TransitionProxy> efaTransitions =
        new TreeSet<TransitionProxy>();
      efaEvents.addAll(aut.getEvents());
      efaTransitions.addAll(aut.getTransitions());
      for (final EventProxy event: mEFAEventEventMap.values()) {
        efaEvents.remove(event);
        for (final TransitionProxy trans: aut.getTransitions()){
          if (trans.getEvent() == event) {
            efaTransitions.remove(trans);
          }
        }
      }
      newAutomata.put(aut.getName(),
                      mDESFactory.createAutomatonProxy(aut.getName(),
                                                       aut.getKind(),
                                                       efaEvents,
                                                       aut.getStates(),
                                                       efaTransitions));
    }
    mAutomata.clear();
    mAutomata.putAll(newAutomata);
  }
*/

  private void buildCompiledVariableAutomata()
    throws VisitorException
  {
    for (final VariableComponentProxy variable : mVariableComponents) {
      CompiledAutomaton variableAutomaton =
        createCompiledVariableAutomaton(ComponentKind.PLANT, variable);
      // Add variable automaton to mAutomata.
      mCompiledAutomata.put(variable.getName(), variableAutomaton);
    }
  }

  private CompiledAutomaton createCompiledVariableAutomaton
    (final ComponentKind kind,
     final VariableComponentProxy variable)
    throws VisitorException
  {
    // Evaluate the state range.
    final SimpleExpressionProxy rangeexpr = variable.getType();
    final RangeValue range = evalRange(rangeexpr);
    final int numstates = range.size();
    // Create states corresponding to the different states of the variable.
    final Map<IndexValue,StateProxy> variableStates =
      new HashMap<IndexValue,StateProxy>(numstates);
    final Set<EventProxy> variableAlphabet = new HashSet<EventProxy>();
    createVariableStates(variable, range, variableStates, variableAlphabet);
    // Copy the relabeledAlphabet. Events that translate into
    // selfloops in all variable states will be removed.
    variableAlphabet.addAll(mEFAEventGuardClauseMap.keySet());
    // Create transitions corresponding to all allowed updates of the variable.
    Set<TransitionProxy> variableTransitions =
      createVariableTransitions(variable,
                                range,
                                variableAlphabet,
                                variableStates);
    // Create variable automaton.
    final CompiledAutomaton variableAutomaton =
      new CompiledAutomaton(variable.getName(),kind,
                            variableAlphabet,
                            variableStates.values(),
                            variableTransitions);
    return variableAutomaton;
  }

  private void createVariableStates(final VariableComponentProxy variable,
                                    final RangeValue range,
                                    final Map<IndexValue,StateProxy> states,
                                    final Collection<EventProxy> allprops)
    throws VisitorException
  {
    // First, collect used propositions and add them to the alphabet.
    // Exactly the propositions mentioned in the markings list will be added,
    // so the automaton will be neutral (everywhere marked) with respect
    // to all other propositions.
    final Collection<VariableMarkingProxy> markings =
      variable.getVariableMarkings();
    final int marksize = markings.size();
    final Map<VariableMarkingProxy,EventValue> markmap =
      new HashMap<VariableMarkingProxy,EventValue>(marksize);
    for (final VariableMarkingProxy marking : markings) {
      final IdentifierProxy ident = marking.getProposition();
      final EventValue value =
        evalEvent(ident, EventKindMask.TYPEMASK_PROPOSITION);
      markmap.put(marking, value);
      final Iterator<CompiledSingleEventValue> iter =
        value.getEventIterator();
      while (iter.hasNext()) {
        final CompiledSingleEventValue event = iter.next();
        final EventProxy eventProxy = event.getEventProxy();
        allprops.add(eventProxy);
      }
    }
    // Second create states. For each state, we evaluate the initial state
    // predicate to determine whether the state is initial or not, and all
    // the marking predicates to determine the set of propositions for that
    // state.
    final String varname = variable.getName();
    final SimpleExpressionProxy initpred = variable.getInitialStatePredicate();
    final Collection<EventProxy> stateprops = new LinkedList<EventProxy>();
    for (final IndexValue item : range.getValues()) {
      final StringBuffer buffer = new StringBuffer(varname);
      buffer.append('=');
      buffer.append(item);
      final String statename = buffer.toString();
      try {
        mContext.add(varname, item);
      } catch (final EvalException exception) {
        exception.provideLocation(variable);
        throw wrap(exception);
      }
      try {
        final boolean initial = evalBoolean(initpred);
        for (final VariableMarkingProxy marking : markings) {
          final SimpleExpressionProxy pred = marking.getPredicate();
          final boolean marked = evalBoolean(pred);
          if (marked) {
            final EventValue value = markmap.get(marking);
            final Iterator<CompiledSingleEventValue> iter =
              value.getEventIterator();
            while (iter.hasNext()) {
              final CompiledSingleEventValue event = iter.next();
              final EventProxy eventProxy = event.getEventProxy();
              stateprops.add(eventProxy);
            }
          }
        }
        final StateProxy state =
          mDESFactory.createStateProxy(statename, initial, stateprops);
        states.put(item, state);
        stateprops.clear();
      } finally {
        mContext.unset(varname);
      }
    }
  }

  
  private void createForbiddenEvents(Set<EventProxy> globalAlphabet) {
	 mEventForbiddenExp = new HashMap<EventProxy,SimpleExpressionProxy>();
	  mEventForbiddenLocations = 
		  new HashMap<EventProxy, Set<String>>();
	   for(LocationsAndExpression locExp: mForbiddenStates){
		   for(SimpleExpressionProxy clause: locExp.getExpression()){
		final EventProxy forbiddenEvent = mDESFactory.
				createEventProxy(
						"u"
						+ "*"
						+ mCurrentEventID++, EventKind.UNCONTROLLABLE,
						true);
		mEventForbiddenExp.put(forbiddenEvent,clause);
		  mEventForbiddenLocations.put(forbiddenEvent,
				  locExp.getLocations());
		  mForbiddenEvents.add(forbiddenEvent);
		  globalAlphabet.add(forbiddenEvent);
		   }
	   }
	 }
  private Set<TransitionProxy>
    createVariableTransitions(final VariableComponentProxy variable,
                              final RangeValue range,
                              final Set<EventProxy> variableAlphabet,
                              final Map<IndexValue,StateProxy> variableStates)
    throws VisitorException
  {
    try {
      final VariableSearcher searcher = new VariableSearcher(variable);
      final Set<TransitionProxy> variableTransitions =
        new TreeSet<TransitionProxy>();
      for (EventProxy forbiddenEvent: mEventForbiddenExp.keySet()){
       final SimpleExpressionProxy clause =
          mEventForbiddenExp.get(forbiddenEvent);
        if (searcher.search(clause)){
        	 variableAlphabet.add(forbiddenEvent); 
             mForbiddenEvents.add(forbiddenEvent);
        	final String name = variable.getName();
          for (final IndexValue item : range.getValues()) {
            final StateProxy source = variableStates.get(item);
            mContext.add(name, item);
            try {
              if (evaluatePartialGuard(clause, searcher)) {
                /*
                 * Since we have an uncontrollable state,
                 * we do not consider any actions.
                 */ 
                final TransitionProxy forbiddenTransition =
                  mDESFactory.createTransitionProxy(source,
                                                    forbiddenEvent,
                                                    source);
                variableTransitions.add(forbiddenTransition);
              }
            }
            finally {
              mContext.unset(name);
            }
          }
        }
      }
      for (EventProxy relabeledEvent : mEFAEventGuardClauseMap.keySet()) {
        // Get the right action.
        final List<List<BinaryExpressionProxy>> actionLists =
          mEFAEventActionListsMap.get(relabeledEvent);
        // For each variable, find the action that updates this variable.
        // Only one of the actionLists is allowed to update each variable.
        final BinaryExpressionProxy action =
          findAction(variable.getName(), actionLists);
        // ... and the corresponding guard
        final SimpleExpressionProxy guardClause =
          mEFAEventGuardClauseMap.get(relabeledEvent);
        if (action == null &&
            /*!mEFAEventControllabilityEventsMap.containsKey(relabeledEvent) &&*/ 
        		!searcher.search(guardClause)) {
          // The action does not update this variable and it does
          // not occur in the guard expression => we can remove
          // the corresponding event from the local alphabet.
          variableAlphabet.remove(relabeledEvent);
        } else {
          // Translate the action to a transition in the variable automaton and
          // add this transition in the variable states where the guard is
          // true.
          final String name = variable.getName();
          for (final IndexValue item : range.getValues()) {
            final StateProxy source = variableStates.get(item);
            mContext.add(name, item);
            try {
              if (evaluatePartialGuard(guardClause, searcher)) {
            	  StateProxy target;
            	  if (action == null) {
                  target = source;
                } else {
                  final IndexValue result = evalIndex(action, range);
                  target = variableStates.get(result);
                }
                final TransitionProxy actionTransition =
                  mDESFactory.createTransitionProxy(source,
                                                    relabeledEvent,
                                                    target);
                variableTransitions.add(actionTransition);
            }}
               finally {
              mContext.unset(name);
            }
          }
        }
      }
      return variableTransitions;
    } catch (final DuplicateIdentifierException exception) {
      exception.provideLocation(variable);
      throw wrap(exception);
    }
  }

  private boolean evaluatePartialGuard(final SimpleExpressionProxy clause,
                                       final VariableSearcher searcher)
    throws VisitorException
  {
    if (clause instanceof IntConstantProxy) {
      return evalBoolean(clause);
    } else if (!searcher.search(clause)) {
      return true;
    } else if (!(clause instanceof BinaryExpressionProxy)) {
      return evalBoolean(clause);
    } else {
      final BinaryExpressionProxy binary = (BinaryExpressionProxy) clause;
      final BinaryOperator op = binary.getOperator();
      if (op != mOperatorTable.getAndOperator()) {
        return evalBoolean(clause);
      }
      final SimpleExpressionProxy lhs = binary.getLeft();
      if (!evaluatePartialGuard(lhs, searcher)) {
        return false;
      }
      final SimpleExpressionProxy rhs = binary.getRight();
      return evaluatePartialGuard(rhs, searcher);
    }
  }

  private BinaryExpressionProxy findAction
    (String name, List<List<BinaryExpressionProxy>> actionLists)
  {
      BinaryExpressionProxy action1=null;
	  BinaryExpressionProxy action2=null;
	  for (List<BinaryExpressionProxy> actions : actionLists){
      for (BinaryExpressionProxy action : actions) {
        if (((SimpleIdentifierProxy) action.getLeft()).
            getName().
            equals(name)){
        	if(action1!=null){
        		action2=action1;
        		action1=action;
        		if(!action1.getRight().equalsByContents(action2.getRight())){
        			return null;
        		}
        	}
        	else{
        		action1=action;
        		action2=action;
        	}
        }
      }
    }
    return action1;
  }

  /*
   * This method will be used to separate plant guards and specification guards.
   * Both the plant guard and the spec will be converted into dnf. All 
   * combinations of and-clauses from the plant and spec 
   * will be used to relabel events. Using this "extended" relabelling 
   * it will be possible to handle controllability for EFA.
   */
  private List<List<List<TransitionProxy>>> plantSpecTransitions       
  (final EventProxy event, Set <Boolean> forbidden)
{
	  final List<List<List<TransitionProxy>>> PlantSpecTrans =
	        new LinkedList<List<List<TransitionProxy>>>();
  final List<List<TransitionProxy>> plantTransitions =
    new LinkedList<List<TransitionProxy>>();
  final List<List<TransitionProxy>> specTransitions =
	    new LinkedList<List<TransitionProxy>>();
  
  for (final AutomatonProxy automaton : mAutomata.values()) {
    final List<TransitionProxy> transInAutomaton =
      new LinkedList<TransitionProxy>();
     for (final TransitionProxy transition : automaton.getTransitions()) {
    	 if (event==findOriginalEvent(transition.getEvent())) {
    	 /*
      	 * Transition may already been relabelled.
      	 */
    	  transInAutomaton.add(transition);
        
      } 
    }
    if (transInAutomaton.isEmpty() &&
        automaton.getEvents().contains(event)&& 
		automaton.getKind()== ComponentKind.PLANT) {
   	/*
   	 * This can currently not occur in the editor.
   	 */
     plantTransitions.clear();
      specTransitions.clear();
      break;
    } 
    if (transInAutomaton.isEmpty() &&
            automaton.getEvents().contains(event)&& 
    		automaton.getKind()== ComponentKind.SPEC) {
    		Boolean b=true;
    	 forbidden.add(b); 
        } 
    else if (!transInAutomaton.isEmpty()&& 
    		automaton.getKind()== ComponentKind.PLANT) {
      plantTransitions.add(transInAutomaton);
     }
    else if (!transInAutomaton.isEmpty()&& 
    		automaton.getKind()== ComponentKind.SPEC) {
        specTransitions.add(transInAutomaton);
       }  
  
  }
  
  List<List<TransitionProxy>> allSpecPaths = allPossiblePaths(specTransitions);
  List<List<TransitionProxy>> allPlantPaths = allPossiblePaths(plantTransitions);
  
   PlantSpecTrans.add(0,allPlantPaths);
   PlantSpecTrans.add(1,allSpecPaths);
  
   return PlantSpecTrans;
}

  private List<List<TransitionProxy>> allPossibleTransitions
    (final EventProxy event)
  {
    if (event.getKind() == EventKind.PROPOSITION) {
      return Collections.emptyList();
    }
    final List<List<TransitionProxy>> transitions =
      new LinkedList<List<TransitionProxy>>();
     // It is important that events with different GuardActionBlocks are
    // translated to separate transitions. (mEFATransitions)
    for (final AutomatonProxy automaton : mAutomata.values()) {
      final List<TransitionProxy> transInAutomaton =
        new LinkedList<TransitionProxy>();
       for (final TransitionProxy transition : automaton.getTransitions()) {
        if (event==findOriginalEvent(transition.getEvent())) {
          transInAutomaton.add(transition);
        } 
      }
      if (transInAutomaton.isEmpty() &&
          automaton.getEvents().contains(event)) {
        transitions.clear();
        break;
      } else if (!transInAutomaton.isEmpty()) {
        transitions.add(transInAutomaton);
      }
    }
    List<List<TransitionProxy>> allPaths = allPossiblePaths(transitions);
    return allPaths;
  }

  private List<List<TransitionProxy>> allPossiblePaths
    (final List<List<TransitionProxy>> transitions)
  {
    if (transitions.isEmpty()) {
      final List<List<TransitionProxy>> noPaths =
        new LinkedList<List<TransitionProxy>>();
      noPaths.add(new LinkedList<TransitionProxy>());
      return noPaths;
    } else {
      final List<TransitionProxy> transInAut = transitions.remove(0);
      final List<List<TransitionProxy>> subPaths =
        allPossiblePaths(transitions);
      final List<List<TransitionProxy>> paths =
        new LinkedList<List<TransitionProxy>>();
      for (List<TransitionProxy> subPath : subPaths) {
        for (TransitionProxy trans : transInAut) {
          final List<TransitionProxy> p = new LinkedList<TransitionProxy>();
          p.addAll(subPath);
          p.add(trans);
          paths.add(p);
        }
      }
      return paths;
    }
  }

  private void addNewTransitionsToCompiledAutomtata(EventProxy relabeledEvent, List<TransitionProxy> path) {
	    for(TransitionProxy trans: path){
	      /*
	       * Find the corresponding mutable compiled automaton.
	       */
	      CompiledAutomaton aut= mCompiledAutomata.get(mEFATransitionAutomatonMap.get(trans).getName());
          aut.addEvent(relabeledEvent);
          TransitionProxy relabeledTransition =
  	        mDESFactory.createTransitionProxy(trans.getSource(),
  	                                       relabeledEvent,
  	                                       trans.getTarget());
          aut.addTransition(relabeledTransition);
	    }

	  }

 
 
 /* private void addNewTransitionsToAutomtata(EventProxy relabeledEvent,
			List<TransitionProxy> path) {
		for (TransitionProxy trans : path) {
			Set<EventProxy> relabeledEvents = new TreeSet<EventProxy>();
			Collection<TransitionProxy> relabeledTransitions = new TreeSet<TransitionProxy>();

			/*
			 * Finding the original automata.
			 */
/*
			AutomatonProxy aut = mEFATransitionAutomatonMap.get(trans);

			/*
			 * Add the relabeledEvent to relabeledEvents.
			 * 
			 * It may still exist old transitions with the old event name.
			 * Therefore we cannot remove the old events here.
			 */
/*			relabeledEvents.addAll(mAutomata.get(aut.getName()).getEvents());
			relabeledEvents.add(relabeledEvent);
			/*
			 * Add new transition to relabeledTransitions.
			 */
/*			relabeledTransitions.addAll(mAutomata.get(aut.getName())
					.getTransitions());
			TransitionProxy relabeledTransition = mDESFactory
					.createTransitionProxy(trans.getSource(), relabeledEvent,
							trans.getTarget());
			relabeledTransitions.add(relabeledTransition);

			/*
			 * Create updated automaton.
			 */
/*			AutomatonProxy relabeledAutomaton = mDESFactory
					.createAutomatonProxy(aut.getName(), aut.getKind(),
							relabeledEvents, aut.getStates(),
							relabeledTransitions);
			/*
			 * Update mapping.
			 */
/*			mAutomata.put(aut.getName(), relabeledAutomaton);
		}
	}
*/  
  
  private SimpleExpressionProxy collectUncontrollableGuard
  (SimpleExpressionProxy plantGuard, SimpleExpressionProxy specGuard)
{
  final BinaryOperator andOp = mOperatorTable.getAndOperator();
  final UnaryOperator notOp = mOperatorTable.getNotOperator();
  SimpleExpressionProxy falseSpecGuard = null;
  SimpleExpressionProxy result = null;
  falseSpecGuard=mModuleFactory.createUnaryExpressionProxy(
		  "!"/*notOp.toString()*/+ "(" + specGuard.toString() + ")",
		  notOp, 
		  specGuard);
      result =
 mModuleFactory.createBinaryExpressionProxy(
		 "(" +plantGuard.toString()+ ")"+ "&amp;"+ "(" +falseSpecGuard.toString()+ ")",
		 andOp, 
		 falseSpecGuard, 
		 plantGuard);
  if (result == null) {
    return mModuleFactory.createIntConstantProxy(1);
  } else {
    return result;
  }
}
  
 
  private SimpleExpressionProxy collectGuard
    (final List<TransitionProxy> path)
  {
    final BinaryOperator andop = mOperatorTable.getAndOperator();
    SimpleExpressionProxy result = null;
    for (final TransitionProxy trans : path) {
      final GuardActionBlockProxy block =
        mEFATransitionGuardActionBlockMap.get(trans);
      if (block != null) {
        for (final SimpleExpressionProxy guard : block.getGuards()) {
        	if(guard.toString().equals("false")){
        		return mModuleFactory.createSimpleIdentifierProxy("false");
        	}
        	if (result == null) {
            result = guard;
          } else {
        	  result =
              mModuleFactory.createBinaryExpressionProxy(andop, result, guard);
          }
        }
      }
    }
    if (result == null) {
      return mModuleFactory.createIntConstantProxy(1);
    } else {
      return result;
    }
  }

  private SimpleExpressionProxy collectPlantGuard
  (final List<TransitionProxy> path)
{
  final BinaryOperator andop = mOperatorTable.getAndOperator();
  SimpleExpressionProxy result = null;
  for (final TransitionProxy trans : path) {
	  boolean isPlant= plantTransition(trans);
	  if(isPlant){
	  final GuardActionBlockProxy block =
      mEFATransitionGuardActionBlockMap.get(trans);
    if (block != null) {
      for (final SimpleExpressionProxy guard : block.getGuards()) {
      	/*if(guard.toString().equals("false")){
      		return mModuleFactory.createSimpleIdentifierProxy("false");
      		  }*/
      	if (result == null) {
          result = guard;
        } else {
      	  result =
            mModuleFactory.createBinaryExpressionProxy(andop, result, guard);
        }
      }
    }
  }
}
  
  if (result == null) {
    return mModuleFactory.createIntConstantProxy(1);
  } else {
    return result;
  }
}

  
  private boolean plantTransition(TransitionProxy trans) {
     boolean isPlant=false;	
	  for(AutomatonProxy aut: mAutomata.values()){
		if(aut.getKind()== ComponentKind.PLANT){
		  isPlant=isPlant || aut.getTransitions().contains(trans);
	}
	  }
	return isPlant;
}


private List<List<BinaryExpressionProxy>>
    collectAction(final List<TransitionProxy> path)
  {
    final List<List<BinaryExpressionProxy>> actionLists =
      new LinkedList<List<BinaryExpressionProxy>>();
    for (final TransitionProxy trans : path) {
      final GuardActionBlockProxy block =
        mEFATransitionGuardActionBlockMap.get(trans);
      if (block != null) {
        final List<BinaryExpressionProxy> actions = block.getActions();
        actionLists.add(actions);
      }
    }
    return actionLists;
  }

	private boolean componentHasNonEmptyGuardActionBlock(Collection<EdgeProxy> edges)
	{
		for (final EdgeProxy edge: edges)
		{
			if (edge.getGuardActionBlock() != null)
			{
				GuardActionBlockProxy guardActionProxy = edge.getGuardActionBlock();
				List<SimpleExpressionProxy> guards = guardActionProxy.getGuards();
				if (guards.size() > 0)
					return true;

				List<BinaryExpressionProxy> actions = guardActionProxy.getActions();
				if (actions.size() > 0)
					return true;
			}
		}
		return false;
	}

  //#########################################################################
  //# Specific Evaluation Methods
  private <V extends Value>
    V getParameterValue(final V defaultValue,
                        final Class<? extends V> type,
                        final ScopeKind scope,
                        final String paramname,
                        final String typename)
    throws EvalException
  {
    if (mParameterMap == null || scope == ScopeKind.LOCAL) {
      return defaultValue;
    }
    final CompiledParameterBinding binding = mParameterMap.remove(paramname);
    if (binding != null) {
      final Value actual = binding.getValue();
      return checkType(actual, type, typename);
    } else if (scope == ScopeKind.REQUIRED_PARAMETER) {
      throw new UndefinedIdentifierException
        (paramname, "required parameter", null);
    } else {
      return defaultValue;
    }
  }

  private void processEvent(final Value value)
    throws EventKindException, TypeMismatchException
  {
    if (mEventList != null) {
      final EventValue event = checkType(value, EventValue.class, "EVENT");
      mEventList.addEvent(event);
    }
  }

  private void createAutomatonEvents(final EventValue events)
    throws VisitorException
  {
    final Iterator<CompiledSingleEventValue> iter =
      events.getEventIterator();
    while (iter.hasNext()) {
      final CompiledSingleEventValue event = iter.next();
      EventProxy proxy = event.getEventProxy();
      if (proxy == null) {
        final String name = event.getName();
        final EventKind kind = event.getKind();
        final boolean observable = event.isObservable();
        proxy = mDESFactory.createEventProxy(name, kind, observable);
        event.setEventProxy(proxy);
        mGlobalAlphabet.add(proxy);
      }
      mLocalAlphabet.add(proxy);
      mOriginalAlphabet.add(proxy);
    }
  }

  private void createTransitions(final NodeProxy source,
                                 final CompiledEventListValue events,
                                 final NodeProxy target,
                                 final CompiledNode groupEntry,
                                 final boolean deterministic,
                                 final EdgeProxy edge/* EFA */)
    throws VisitorException
  {
    if (source instanceof SimpleNodeProxy) {
      final SimpleNodeProxy simpleSource = (SimpleNodeProxy) source;
      createTransitions(simpleSource, events, target, groupEntry,
                        deterministic, edge/* EFA */);
    } else {
      for (final NodeProxy child : source.getImmediateChildNodes()) {
        createTransitions(child, events, target, groupEntry,
                          deterministic, edge/* EFA */);
      }
    }
  }

  private void createTransitions(final SimpleNodeProxy source,
                                 final CompiledEventListValue events,
                                 final NodeProxy target,
                                 final CompiledNode groupEntry,
                                 final boolean deterministic,
                                 final EdgeProxy edge/* EFA */)
    throws VisitorException
  {
    if (target instanceof SimpleNodeProxy) {
      final SimpleNodeProxy simpleTarget = (SimpleNodeProxy) target;
      createTransitions(source, events, simpleTarget, groupEntry,
                        deterministic, edge/* EFA */);
    } else {
      for (final NodeProxy child : target.getImmediateChildNodes()) {
        createTransitions(source, events, child, groupEntry,
                          deterministic, edge/* EFA */);
      }
    }
  }

  private void createTransitions(final SimpleNodeProxy source,
                                 final CompiledEventListValue events,
                                 final SimpleNodeProxy target,
                                 final CompiledNode groupEntry,
                                 final boolean deterministic,
                                 final EdgeProxy edge/* EFA */)
    throws VisitorException
  {
    try {
      final CompiledNode sourceEntry = mPrecompiledNodes.get(source);
      final CompiledNode targetEntry = mPrecompiledNodes.get(target);
      final StateProxy sourceState = sourceEntry.getState();
      final StateProxy targetState = targetEntry.getState();
      final Iterator<CompiledSingleEventValue> iter = events
        .getEventIterator();
      while (iter.hasNext()) {
        final CompiledSingleEventValue value = iter.next();
        final EventProxy event = value.getEventProxy();
        CompiledTransition duplicate=null;
        boolean create = true;
        final Collection<CompiledTransition> compiledTransitions =
          sourceEntry.getCompiledTransitions(event);
        for (final CompiledTransition ctrans : compiledTransitions) {
          if (ctrans.getTarget() == targetEntry.getState()) {
            duplicate=ctrans;
            continue;
          }
          final NodeProxy cause = ctrans.getGroup();
          if (groupEntry.hasProperChildNode(cause)) {
            create = false;
            break;
          } else if (deterministic) {
            throw new NondeterministicModuleException
              (mCurrentComponentName, sourceState, event);
          }
        }
        if (mIsEFA && edge.getGuardActionBlock()!= null) {
        for (SimpleExpressionProxy guard : edge.getGuardActionBlock().getGuards()){
    		if(guard.toString().equals("false") || guard.toString().equals("0")){
    			create=false; 
    			/*
    			 * The in transition is removed but the event 
    			 * is in the alphabet.
    			 */
    			mGlobalAlphabet.add(event);
                mLocalAlphabet.add(event);
    		}
    	  }
        }
        if (create) {
          final NodeProxy group = groupEntry.getNode();
          if (duplicate == null) {
        	  final TransitionProxy trans = mDESFactory.createTransitionProxy
              (sourceState, event, targetState);
        	  //EFA with shared variables----------
        	  if (mIsEFA && edge.getGuardActionBlock()!= null){
        	  mEFATransitionGuardActionBlockMap.put
              (trans, edge.getGuardActionBlock());
        	  }
            //---------------------------
            mTransitions.add(trans);
            sourceEntry.addTransition(trans, group);
          } else {
            final TransitionProxy trans = duplicate.getTransition();
            sourceEntry.addTransition(trans, group);
            if (mIsEFA && edge.getGuardActionBlock()!= null) {
              /*
               * EFA: duplicate but with different
               * GuardActionBlock, needs to relabeled.
               */
            	if (mEFATransitionGuardActionBlockMap.containsKey(trans)) {
            	   
            	  if (!mEFATransitionGuardActionBlockMap.get(trans).equals
                      (edge.getGuardActionBlock())) {
                  final EventProxy relabeledEvent =
                    mDESFactory.createEventProxy(trans.getEvent().getName()
                                                 + "_" + mCurrentEventID,
                                                 trans.getEvent().getKind(),
                                                 trans.getEvent()
                                                 .isObservable());
                  mCurrentEventID++;
                  final EventProxy key =
                    mDESFactory.createEventProxy(trans.getEvent().getName()
                                                 + "_" + mCurrentEventID,
                                                 trans.getEvent().getKind(),
                                                 trans.getEvent()
                                                 .isObservable());
                  mCurrentEventID++;
                  /*
                   * mEFAEventEventMap is maps EFA events to EFA events.
                   */
                  mEFAEventEventMap.put(key, relabeledEvent);
                  mGlobalAlphabet.add(relabeledEvent);
                  mLocalAlphabet.add(relabeledEvent);
                  mEFAEventEventMap.put(relabeledEvent, trans.getEvent());
                  final TransitionProxy efaCopy =
                    mDESFactory.createTransitionProxy(trans.getSource(),
                                                      relabeledEvent,
                                                      trans.getTarget());
                  mEFATransitionGuardActionBlockMap.put
                    (efaCopy, edge.getGuardActionBlock());
                  mTransitions.add(efaCopy);
                }
              }
            }
          }
        }
      }
    } catch (final NondeterministicModuleException exception) {
      throw wrap(exception);
    }
  }

  private boolean evalBoolean(final SimpleExpressionProxy expr)
    throws VisitorException
  {
    final IntValue value = evalTyped(expr, IntValue.class, "BOOLEAN");
    final int number = value.getValue();
    if (number < 0 || number > 1) {
      final TypeMismatchException exception =
        new TypeMismatchException(expr, value, "BOOLEAN");
      throw wrap(exception);
    }
    return number != 0;
  }

  private EventValue evalEvent(final SimpleExpressionProxy expr,
                               final int typemask)
    throws VisitorException
  {
    final EventValue value = evalTyped(expr, EventValue.class, "EVENT");
    final int mask = value.getKindMask();
    final int badmask = mask & ~EventKindMask.TYPEMASK_PROPOSITION;
    if (badmask != 0) {
      final EventKindException exception =
        new EventKindException(value, badmask);
      exception.provideLocation(expr);
      throw wrap(exception);
    }
    return value;
  }

  private IndexValue evalIndex(final SimpleExpressionProxy expr)
    throws VisitorException
  {
    return evalTyped(expr, IndexValue.class, "INDEX");
  }

  private IndexValue evalIndex(final SimpleExpressionProxy expr,
                               final RangeValue range)
    throws VisitorException
  {
    IndexValue value = evalIndex(expr);
    if (range.contains(value)) {
      return value;
    } else {
      final TypeMismatchException exception =
        new TypeMismatchException(expr, value, range.toString());
      throw wrap(exception);
    }
  }

  private RangeValue evalRange(final SimpleExpressionProxy expr)
    throws VisitorException
  {
    return evalTyped(expr, RangeValue.class, "RANGE");
  }

  private <V extends Value>
    V evalTyped(final ExpressionProxy expr,
                final Class<V> type,
                final String typename)
    throws VisitorException
  {
    try {
      final Value value = (Value) expr.acceptVisitor(this);
      return checkType(value, type, typename);
    } catch (final TypeMismatchException exception) {
      exception.provideLocation(expr);
      throw wrap(exception);
    }
  }

  private <V extends Value>
    V checkType(final Value value,
                final Class<V> type,
                final String typename)
    throws TypeMismatchException
  {
    try {
      return type.cast(value);
    } catch (final ClassCastException exception) {
      throw new TypeMismatchException(value, typename);
    }
  }


  //#########################################################################
  //# Inner Class NameCompiler
  private class NameCompiler extends AbstractModuleProxyVisitor {

    public String visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy proxy)
      throws VisitorException
    {
      final String name = proxy.getName();
      final StringBuffer buffer = new StringBuffer(name);
      final List<SimpleExpressionProxy> indexes = proxy.getIndexes();
      for (final SimpleExpressionProxy index : indexes) {
        final Value value = (Value) index.acceptVisitor(ModuleCompiler.this);
        buffer.append('[');
        buffer.append(value);
        buffer.append(']');
      }
      return buffer.toString();
    }

    public String visitSimpleIdentifierProxy
      (final SimpleIdentifierProxy proxy)
    {
      return proxy.getName();
    }

  }


  //#########################################################################
  //# Inner Class VariableSearcher
  private static class VariableSearcher extends AbstractModuleProxyVisitor {

    //#######################################################################
    //# Constructor
    private VariableSearcher(final VariableComponentProxy variable)
    {
      mSoughtName = variable.getName();
    }

    //#######################################################################
    //# Invocation
    private boolean search(final SimpleExpressionProxy expr)
      throws VisitorException
    {
      final Boolean found = (Boolean) expr.acceptVisitor(this);
      return found;
    }

    @SuppressWarnings("unused")
    private boolean search(final CompiledClause clause)
      throws VisitorException
    {
      final Collection<SimpleExpressionProxy> literals = clause.getLiterals();
      for (final SimpleExpressionProxy literal : literals) {
        if (search(literal)) {
          return true;
        }
      }
      return false;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Boolean visitBinaryExpressionProxy
      (final BinaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy lhs = expr.getLeft();
      final SimpleExpressionProxy rhs = expr.getRight();
      return search(lhs) || search(rhs);
    }

    public Boolean visitSimpleExpressionProxy
      (final SimpleExpressionProxy expr)
    {
      return false;
    }

    public Boolean visitSimpleIdentifierProxy
      (final SimpleIdentifierProxy ident)
    {
      return ident.getName().equals(mSoughtName);
    }

    public Boolean visitUnaryExpressionProxy
      (final UnaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy subterm = expr.getSubTerm();
      return search(subterm);
    }

    //#######################################################################
    //# Data Members
    private final String mSoughtName;

  }

  //#########################################################################
  //# Data Members
  private final DocumentManager mDocumentManager;
  private final ProductDESProxyFactory mDESFactory;
  private final ModuleProxyFactory mModuleFactory;
  private final ModuleProxy mModule;
  private final NameCompiler mNameCompiler = new NameCompiler();

  private CompilerContext mContext;
  private CompiledEventListValue mEventList;
  private Map<String,CompiledParameterBinding> mParameterMap;
  private Map<String,AutomatonProxy> mAutomata;
  private Map<NodeProxy,CompiledNode> mPrecompiledNodes;
  private String mCurrentComponentName;
  private int mMaxInitialStates;
  private Set<EventProxy> mGlobalAlphabet;
  private Set<EventProxy> mLocalAlphabet;
  private Set<StateProxy> mStates;
  private Collection<TransitionProxy> mTransitions;

  // EFA---------------------
  private final CompilerOperatorTable mOperatorTable;
  private final Comparator<SimpleExpressionProxy> mComparator;
  private final DNFConverter mDNFConverter;
  private final DNFMinimizer mDNFMinimizer;
  /*
   * Mappings needed for EFA with guards on shared variables.
   */
  private Map<TransitionProxy, AutomatonProxy> mEFATransitionAutomatonMap;
  private Map<TransitionProxy, GuardActionBlockProxy> mEFATransitionGuardActionBlockMap;
  private Map<EventProxy,SimpleExpressionProxy> mEFAEventGuardClauseMap;
  private List<VariableComponentProxy> mVariableComponents;
  private Map<EventProxy, EventProxy> mEFAEventEventMap;
  private int mCurrentEventID;
  private Set<EventProxy> mOriginalAlphabet;
  private boolean mIsEFA;
  /*
   * Mappings to handle controllability when the flat EFA model is generated.
   */ 
  private Map<EventProxy, List<List<BinaryExpressionProxy>>> mEFAEventActionListsMap;
  /*
   * The set mUncontrollableEFASpecifications is not used. It may 
   * be used later for optimization. The specifications that
   * needs to be transformed into plants are those in mUncontrollableEFASpecifications
   * and specifications that contains events that have been relabelled into more 
   * than one name.    
   */
  private Set<LocationsAndExpression> mForbiddenStates;
  private Map<EventProxy, SimpleExpressionProxy> mEventForbiddenExp;
  private Map<EventProxy, Set<String>> mEventForbiddenLocations;
  private Set<EventProxy> mForbiddenEvents;
  private Map<String,CompiledAutomaton> mCompiledAutomata;
}
