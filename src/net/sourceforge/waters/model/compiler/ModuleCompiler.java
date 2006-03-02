//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   ModuleCompiler
//###########################################################################
//# $Id: ModuleCompiler.java,v 1.5 2006-03-02 12:12:50 martin Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
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

import net.sourceforge.waters.model.base.DocumentProxy;
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
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.IndexValue;
import net.sourceforge.waters.model.expr.IntValue;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.expr.RangeValue;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.expr.Value;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.AliasProxy;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.BooleanConstantProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.EventParameterProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.IntParameterProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.ParameterProxy;
import net.sourceforge.waters.model.module.RangeParameterProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.SimpleParameterProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;

//EFA-------------------
import net.sourceforge.waters.model.module.VariableProxy;
//

import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

public class ModuleCompiler extends AbstractModuleProxyVisitor {

	// #########################################################################
	// # Constructors
	public ModuleCompiler(final DocumentManager<DocumentProxy> manager,
			final ProductDESProxyFactory factory, final ModuleProxy module) {
		mDocumentManager = manager;
		mFactory = factory;
		mModule = module;
		mContext = null;
		mParameterMap = null;
		mCurrentEventID = 1;
	}

	// #########################################################################
	// # Invocation
	public ProductDESProxy compile() throws EvalException {
		try {
			final String name = mModule.getName();
			final URI moduleLocation = mModule.getLocation();
			URI desLocation = null;
			if (moduleLocation != null) {
				try {
					final ProxyMarshaller<ProductDESProxy> marshaller = mDocumentManager
							.findProxyMarshaller(ProductDESProxy.class);
					final String ext = marshaller.getDefaultExtension();
					desLocation = moduleLocation.resolve(name + ext);
				} catch (final IllegalArgumentException exception) {
					// No marshaller --- O.K.
				}
			}
			mContext = new CompilerContext(mModule);
			mGlobalAlphabet = new TreeSet<EventProxy>();
			mAutomata = new TreeMap<String, AutomatonProxy>();
			visitModuleProxy(mModule); // mModule.acceptVisitor(this) more
										// correct?
			return mFactory.createProductDESProxy(name, desLocation,
					mGlobalAlphabet, mAutomata.values());
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

	// #########################################################################
	// # Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
	public Value visitAliasProxy(final AliasProxy proxy)
			throws VisitorException {
		try {
			final IdentifierProxy ident = proxy.getIdentifier();
			final String name = ident.getName();
			final ExpressionProxy expr = proxy.getExpression();
			final Value value = (Value) expr.acceptVisitor(this);
			if (ident instanceof SimpleIdentifierProxy) {
				mContext.add(name, value);
			} else {
				final EventValue event = checkType(value, EventValue.class,
						"EVENT");
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
				final IndexedIdentifierProxy indexedIdent = (IndexedIdentifierProxy) ident;
				final List<SimpleExpressionProxy> indexes = indexedIdent
						.getIndexes();
				final Iterator<SimpleExpressionProxy> iter = indexes.iterator();
				while (iter.hasNext()) {
					final SimpleExpressionProxy indexExpr = iter.next();
					final IndexValue indexValue = evalIndex(indexExpr);
					final EventValue next = entry.get(indexValue);
					if (iter.hasNext()) {
						if (next == null) {
							final CompiledArrayAliasValue nextEntry = new CompiledArrayAliasValue(
									entry, indexValue);
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
			}
			return value;
		} catch (final EvalException exception) {
			exception.provideLocation(proxy);
			throw wrap(exception);
		}
	}

	public Value visitBinaryExpressionProxy(final BinaryExpressionProxy proxy)
			throws VisitorException {
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

	public Object visitEdgeProxy(final EdgeProxy proxy) throws VisitorException {
		final EventListExpressionProxy labels = proxy.getLabelBlock();
		final List<Proxy> list = labels.getEventList();
		if (list.isEmpty()) {
			final EmptyLabelBlockException exception = new EmptyLabelBlockException(
					proxy);
			throw wrap(exception);
		}
		final NodeProxy source = proxy.getSource();
		final CompiledNode entry = mPrecompiledNodes.get(source);
		entry.addEdge(proxy);
		return null;
	}

	public Object visitEnumSetExpressionProxy(final EnumSetExpressionProxy proxy)
			throws VisitorException {
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
					final AtomValue atom = checkType(value, AtomValue.class,
							"ATOM");
					atoms.add(atom);
				} catch (final TypeMismatchException exception) {
					exception.provideLocation(item);
					throw wrap(exception);
				}
			}
		}
		return new CompiledEnumRangeValue(atoms);
	}

	public CompiledEventDecl visitEventDeclProxy(final EventDeclProxy proxy)
    throws VisitorException
  {
    try {
      final List<SimpleExpressionProxy> expressions = proxy.getRanges();
      final List<RangeValue> ranges =
        new ArrayList<RangeValue>(expressions.size());
      for (final SimpleExpressionProxy expr : expressions) {
        final RangeValue range = evalRange(expr);
        ranges.add(range);
      }
      final String name = proxy.getName();
      final String fullname = mContext.getPrefixedName(name);
      final CompiledEventDecl entry =
        new CompiledEventDecl(fullname, proxy, ranges);
      mContext.add(entry);
      return entry;
    } catch (final DuplicateIdentifierException exception) {
      exception.provideLocation(proxy);
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

  public Object visitEventParameterProxy
    (final EventParameterProxy proxy)
    throws VisitorException
  {
    try {
      final String name = proxy.getName();
      final EventValue value =
        getParameterValue(proxy, null, EventValue.class, "EVENT");
      final EventDeclProxy decl = proxy.getEventDecl();
      if (value != null) {
        final EventKind kind = decl.getKind();
        final int mask = value.getKindMask();
        if (!EventKindMask.isAssignable(kind, mask) ||
            decl.isObservable() && !value.isObservable()) {
          throw new EventKindException(decl, value);
        }
        final List<SimpleExpressionProxy> declRanges = decl.getRanges();
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
      } else {
        visitEventDeclProxy(decl);
      }
      return null;
    } catch (final EvalException exception) {
      exception.provideLocation(proxy);
      throw wrap(exception);
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

  public Object visitIntParameterProxy(final IntParameterProxy proxy)
    throws VisitorException
  {
    return visitSimpleParameterProxy(proxy, IntValue.class, "INTEGER");
  }

  public ProductDESProxy visitModuleProxy(final ModuleProxy proxy)
    throws VisitorException
  {
    final List<ParameterProxy> parameters = proxy.getParameterList();
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
    final List<AliasProxy> constants = proxy.getConstantAliasList();
    visitCollection(constants);
    final List<EventDeclProxy> events = proxy.getEventDeclList();
    visitCollection(events);
    final List<Proxy> aliases = proxy.getEventAliasList();
    visitCollection(aliases);
    final List<Proxy> components = proxy.getComponentList();
    visitCollection(components);
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

  public Object visitRangeParameterProxy(final RangeParameterProxy proxy)
    throws VisitorException
  {
    return visitSimpleParameterProxy
      (proxy, RangeValue.class, "RANGE");
  }

	/*
	 * Remark: possible to change return, AutomatonProxy to Map<String,AutomatonProxy>?
	 */

	public AutomatonProxy visitSimpleComponentProxy(
			final SimpleComponentProxy proxy) throws VisitorException {
		try {
			final IdentifierProxy ident = proxy.getIdentifier();
			final String name = (String) ident.acceptVisitor(mNameCompiler);
			final String fullName = mContext.getPrefixedName(name);
			if (mAutomata.containsKey(fullName)) {
				throw new DuplicateIdentifierException(name, "Automaton", proxy);
			}
			final ComponentKind kind = proxy.getKind();
			final GraphProxy graph = proxy.getGraph();
			final boolean deterministic = graph.isDeterministic();
			final EventListExpressionProxy blockedExpr = graph
					.getBlockedEvents();
			final CompiledEventListValue blocked = visitEventListExpressionProxy(blockedExpr);
			mLocalAlphabet = new TreeSet<EventProxy>();
			createAutomatonEvents(blocked);
			final Collection<NodeProxy> nodes = graph.getNodes();
			mStates = new TreeSet<StateProxy>();
			mPrecompiledNodes = new IdentityHashMap<NodeProxy, CompiledNode>(
					nodes.size());
			visitCollection(nodes);
			final Collection<EdgeProxy> edges = graph.getEdges();
			visitCollection(edges);
			mTransitions = new TreeSet<TransitionProxy>();
			// EFA----------------
			mIsEFA = hasNonEmptyGuardActionBlock(edges);
			mEFATransitions = new TreeSet<TransitionProxy>();
			
			//Each edge can potentially produce a number of different transitions
			//(via multiple events or guards with "OR" clauses). The mEFATransitionEdgeMap
			//maps these transitions back to their respective "generating" edges.
			mEFATransitionEdgeMap = new HashMap<TransitionProxy, EdgeProxy>();
			// --------------------
			for (final NodeProxy source : nodes) {
				final CompiledNode sourceEntry = mPrecompiledNodes.get(source);
				for (final EdgeProxy edge : sourceEntry.getEdges()) {
					final NodeProxy target = edge.getTarget();
					final EventListExpressionProxy labelBlock = edge
							.getLabelBlock();
					final CompiledEventListValue events = visitEventListExpressionProxy(
							labelBlock, EventKindMask.TYPEMASK_EVENT);
					createAutomatonEvents(events);
					// EFA-------------
					/*
					 * The mEFATransitions are created and the
					 * mEFATransitionEdgeMap is filled: keys = transitions ,
					 * elements = egdes.
					 */
					createTransitions(source, events, target, sourceEntry,
							deterministic, edge);
					// -----------------
				}
				sourceEntry.clearProperChildNodes();
			}
			
			if(mIsEFA){
				//The automaton is an EFA so we need to deal with variables
				//guards and actions.
				
				/*
				 * Perform the first relabeling of events and build mappings from
				 * the relabeled transitions to guards and actions i.e fill
				 * mEFARelabeledTransitionGuardClauseMap and mEFARelabeledTransitionActionMap.
				 */
				Set<TransitionProxy> relabeledEFATransitions = createRelabeledEFATransitions();
				
				/*
				 * Split transitions over logical OR-operators, relabel and update
				 * mappings from (the relabeled) transitions to guards and actions.
				 *
				 * Add new events to mGlobalAlphabet.
				 */
				final AutomatonProxy relabeledAutomaton = createRelabeledAutomaton(
						fullName, kind, relabeledEFATransitions);
				
				mAutomata.put(fullName + "_relabeled", relabeledAutomaton);
				
				final Set<EventProxy> relabeledLocalAlphabet = relabeledAutomaton
				.getEvents();
				final Collection<TransitionProxy> splitTransitions = relabeledAutomaton
				.getTransitions();
				
				/*
				 * Get the EFA variables.
				 */
				final List<VariableProxy> variables = proxy.getVariables();
				
				/*
				 * Create an automaton for each variable, the alphabet for each
				 * automaton is a subset of relabeledLocalAlphabet.
				 */
				for (VariableProxy variable : variables) {
					final AutomatonProxy variableAutomaton = createVariableAutomaton(
							kind, relabeledLocalAlphabet, splitTransitions,
							variable);
					
					//Add variable automaton to mAutomata.
					mAutomata.put(variable.getName(), variableAutomaton);
				}
				
				return relabeledAutomaton;
			}
			else{
				//The automaton is not an EFA so just create a standard automaton.
				final AutomatonProxy aut = mFactory.createAutomatonProxy(fullName,
						kind, mLocalAlphabet, mStates, mTransitions);
				mAutomata.put(fullName, aut);
				return aut;
			}
		}catch (final DuplicateIdentifierException exception) {
			throw wrap(exception);
		} finally {
			mLocalAlphabet = null;
			mStates = null;
			mPrecompiledNodes = null;
			mTransitions = null;
			/* EFA */
			mEFATransitionEdgeMap = null;
			mEFARelabeledTransitionActionMap = null;
			mEFARelabeledTransitionGuardClauseMap = null;
			// ---------------------
		}
	}

	private boolean hasNonEmptyGuardActionBlock(Collection<EdgeProxy> edges) {
		for(EdgeProxy edge: edges){
			if(edge.getGuardActionBlock() != null){
				return true;
			}
		}
		return false;
	}

	private AutomatonProxy createVariableAutomaton(final ComponentKind kind,
			final Set<EventProxy> relabeledLocalAlphabet,
			final Collection<TransitionProxy> splitEFATransitions,
			VariableProxy variable) {
		
		//Copy the relabeledLocalAlphabet. Events that translates into
		//selfloops in all variable states will be removed.
		final Set<EventProxy> variableAlphabet = new HashSet<EventProxy>();
		variableAlphabet.addAll(relabeledLocalAlphabet);

		// Create an expression handler to handle the guard expressions.
		GuardExpressionHandler handler = new GuardExpressionHandler();

		final Collection<EventProxy> VariableStateProps = new TreeSet<EventProxy>();

		List<StateProxy> variableStates = new LinkedList<StateProxy>();

		// Declare variable, enum not yet implemented (TODO remove comment when implemented).
		if (variable.getType() instanceof SimpleIdentifierProxy) {
			handler.declareVariable(variable.getName(),
					GuardExpressionHandler.Type.BOOLEAN);
		} else {
			handler.declareVariable(variable.getName(),
					GuardExpressionHandler.Type.INTEGER);
		}

		//Create states corresponding to the different states of the variable.
		createVariableStates(variable, variableStates, VariableStateProps);

		//Create transitions corresponding to all allowed updates of the variable.
		Set<TransitionProxy> variableTransitions = 
			createVariableTransitions(splitEFATransitions, variable,
					variableAlphabet, handler, variableStates);

		//Create variable automaton.
		final AutomatonProxy variableAutomaton = mFactory.createAutomatonProxy(
				variable.getName(), kind, variableAlphabet, variableStates,
				variableTransitions);
		
		return variableAutomaton;
	}

	private Set<TransitionProxy> createVariableTransitions(final Collection<TransitionProxy> splitEFATransitions, VariableProxy variable, final Set<EventProxy> variableAlphabet, GuardExpressionHandler handler, List<StateProxy> variableStates) {
		// Loop through the splitEFATransitions and create variableTransitions.
		Set<TransitionProxy> variableTransitions = new TreeSet<TransitionProxy>();
		for (TransitionProxy transition : splitEFATransitions) {
			
			//Get the right action.
			final List<BinaryExpressionProxy> actions = mEFARelabeledTransitionActionMap
					.get(transition);

			//For each variable, find the action that updates this variable.
			final BinaryExpressionProxy action = getAction(variable.getName(),
					actions);

			 //...and the corresponding guard
			final SimpleExpressionSubject guardClause = (SimpleExpressionSubject) mEFARelabeledTransitionGuardClauseMap
					.get(transition);
			handler.setPureAndExpression(guardClause);

			/*
			 * Translate the action to a transition in the variable automaton
			 * and add this transition in the variable states where the guard is
			 * true.
			 * 
			 * Remark: Here we require the action to be in the format:
			 * identifier-actionOperator-constant.
			 */
			
			if (!handler.variableInExpression(variable) && action == null) {
				// The action does not update this variable and it does
				// not occur in the guard expression => we can remove
				// the corresponding event from the local alphabet.
				variableAlphabet.remove(transition.getEvent());
			}
			else {
				//variable type = finite integer
				if (variable.getType() instanceof BinaryExpressionProxy) {
					createIntegerVariableTransition(variable, handler, variableStates,
							variableTransitions, transition, action);
				}
				//variable type = boolean
				else if (variable.getType() instanceof SimpleIdentifierProxy) {
					createBooleanVariableTransition(variable, handler, variableStates,
							variableTransitions, transition, action);
				}
				//variable type = enum
				else if (variable.getType() instanceof EnumSetExpressionProxy) {
					// TODO: implement this.
				}

				else {
					System.err.println("ModuleCompiler: Invalid variable type");
				}
			}
		}
		return variableTransitions;
	}

	private void createBooleanVariableTransition(VariableProxy variable, GuardExpressionHandler handler, List<StateProxy> variableStates, Set<TransitionProxy> variableTransitions, TransitionProxy transition, final BinaryExpressionProxy action) {
		int targetIndex;
		Set<Boolean> booleanValues = new HashSet<Boolean>();
		booleanValues.add(true);
		booleanValues.add(false);
		
		for(Boolean value: booleanValues) {
			handler.assignValueToVariable(variable.getName(), value);
			int sourceIndex = value ? 1 : 0;
			
			//Evaluate guard function for this variable.
			Boolean guardValue = evaluatePartialGuard(variable, handler);
			
			// Create action transition if guard == true.
			if (guardValue) {
				final StateProxy source = variableStates.get(sourceIndex);
				if(action != null) {
					final boolean constant = ((BooleanConstantProxy) action
							.getRight()).isValue();
					targetIndex = constant ? 1 : 0;
				} else {
					//action is null => no update takes place.
					targetIndex = sourceIndex;
				}
				final StateProxy target = variableStates
				.get(targetIndex);
				final TransitionProxy actionTransition = mFactory
				.createTransitionProxy(source, transition
						.getEvent(), target);
				variableTransitions.add(actionTransition);
			}
		}
	}

	private void createIntegerVariableTransition(VariableProxy variable, GuardExpressionHandler handler, List<StateProxy> variableStates, Set<TransitionProxy> variableTransitions, TransitionProxy transition, final BinaryExpressionProxy action) {
		BinaryExpressionProxy binExpr = ((BinaryExpressionProxy) variable
				.getType());

		final int lower = ((IntConstantProxy) binExpr.getLeft())
				.getValue();
		final int upper = ((IntConstantProxy) binExpr.getRight())
				.getValue();
		final int range = upper - lower + 1;

		for (int sourceIndex = 0; sourceIndex < range; sourceIndex++) {
			int targetIndex = sourceIndex;
			
			// Set the variable to the right value.
			handler.assignValueToVariable(variable.getName(),
					sourceIndex + lower);

			// Evaluate the guard function for this variable.
			Boolean guardValue;
			guardValue = evaluatePartialGuard(variable, handler);

			// Create action transition if guard == true.
			if(guardValue) {
				if (action != null) {
					//Get right hand side of action expression.
					int constant = ((IntConstantProxy) action.getRight())
					.getValue();
					
					//Get action expression operator.
					BinaryOperator operator = action.getOperator();
					
					//Calculate transition
					if (operator.equals("+=")) {
						targetIndex = modulo((sourceIndex + constant),
								range);
					} else if (operator.equals("-=")) {
						targetIndex = modulo((sourceIndex - constant),
								range);
					} else if (operator.equals("=")) {
						targetIndex = modulo(constant, range);
					} else {
						targetIndex = -1;
						System.err
						.println("ModuleCompiler: Invalid operator");
						// EFA TODO throw exception
					}
				}
				else {
					//action is null => no update takes place.
					targetIndex = sourceIndex;
				}
				//Create transition
				final StateProxy source = variableStates
				.get(sourceIndex);
				final StateProxy target = variableStates
				.get(targetIndex);
				final TransitionProxy actionTransition = mFactory
				.createTransitionProxy(source, transition
						.getEvent(), target);
				variableTransitions.add(actionTransition);
			}
		}
	}

	private Boolean evaluatePartialGuard(VariableProxy variable, GuardExpressionHandler handler) {
		Boolean guardValue;
		try {
			guardValue = handler
					.evaluatePartialExpression(variable.getName());
		} catch (EvalException e) {
			guardValue = null;
			e.printStackTrace();
		}
		return guardValue;
	}

	private BinaryExpressionProxy getAction(String name,
			List<BinaryExpressionProxy> actions) {
		for (BinaryExpressionProxy action : actions) {
			if (((SimpleIdentifierProxy) action.getLeft()).getName().equals(
					name)) {
				return action;
			}
		}
		return null;
	}

	private int modulo(int i, int range) {
		i = i % range;
		i = i + range;
		i = i % range;
		return i;
	}

	private void createVariableStates(VariableProxy variable,
			List<StateProxy> variableStates,
			Collection<EventProxy> VariableStateProps) {

		SimpleExpressionProxy type = variable.getType();

		if (type instanceof SimpleIdentifierProxy) {
			if (((SimpleIdentifierProxy) type).getName().equals("boolean")) {
				if (variable.getInitialValue() instanceof BooleanConstantProxy) {
					boolean initialValue = ((BooleanConstantProxy) variable
							.getInitialValue()).isValue();

					final StateProxy variableTrueState = mFactory
							.createStateProxy(variable.getName() + "="
									+ "false", initialValue == false,
									VariableStateProps);
					variableStates.add(variableTrueState);

					final StateProxy variableFalseState = mFactory
							.createStateProxy(
									variable.getName() + "=" + "true",
									initialValue == true, VariableStateProps);
					variableStates.add(variableFalseState);

				} else {
					System.err.println("ModuleCompiler: Invalid initialValue");
				}
			} else {
				System.err.println("ModuleCompiler: Invalid variable type "
						+ ((SimpleIdentifierProxy) type).getName());
			}
		}

		else if (type instanceof EnumSetExpressionProxy) {
			List<SimpleIdentifierProxy> itemList = ((EnumSetExpressionProxy) type)
					.getItems();
			for (SimpleIdentifierProxy item : itemList) {
				final boolean isInitial = (item.getName() == ((SimpleIdentifierProxy) variable
						.getInitialValue()).getName());
				final StateProxy variableState = mFactory.createStateProxy(
						variable.getName() + "=" + item.getName().toString(),
						isInitial, VariableStateProps);
				variableStates.add(variableState);
			}
		}

		else if (type instanceof BinaryExpressionProxy) {
			BinaryExpressionProxy binExpr = (BinaryExpressionProxy) type;
			if (binExpr.getOperator().getName().equals("..")) {
				int lower = ((IntConstantProxy) binExpr.getLeft()).getValue();
				int higher = ((IntConstantProxy) binExpr.getRight()).getValue();
				int initialValue = ((IntConstantProxy) variable
						.getInitialValue()).getValue();
				for (Integer i = lower; i <= higher; i++) {
					final StateProxy variableState = mFactory.createStateProxy(
							variable.getName() + "=" + i.toString(),
							initialValue == i, VariableStateProps);
					variableStates.add(variableState);
				}
			} else {
				System.err
						.println("ModuleCompiler: invalid range operator in variable declaration");
			}
		}

		else {
			System.err.println("ModuleCompiler: Invalid type");
		}

	} /* EFA */

	private AutomatonProxy createRelabeledAutomaton(final String fullName,
			final ComponentKind kind, Set<TransitionProxy> relabeledEFATransitions) {
		/*
		 * Creating an automata with the relabeledTransitions and the
		 * relabeledLocalAlphabet. This method splits transitions over logical
		 * OR-operators.
		 */

		Set<TransitionProxy> splitTransitions = new HashSet<TransitionProxy>();
		Set<EventProxy> relabeledLocalAlphabet = new TreeSet<EventProxy>();
		GuardExpressionHandler handler = new GuardExpressionHandler();

		for (TransitionProxy transition : relabeledEFATransitions) {
			SimpleExpressionSubject guard = (SimpleExpressionSubject) mEFARelabeledTransitionGuardClauseMap
					.get(transition);
			List<BinaryExpressionProxy> action = mEFARelabeledTransitionActionMap
					.get(transition);
			handler.setExpression(guard);
			List<SimpleExpressionSubject> andClauses = handler.getAndClauses();

			if(andClauses.size() >=  2) {
				mEFARelabeledTransitionActionMap.remove(transition);
				mEFARelabeledTransitionGuardClauseMap.remove(transition);
				
				for (SimpleExpressionSubject andClause : andClauses) {
					
					final EventProxy relabeledEvent = mFactory.createEventProxy(
							transition.getEvent().getName() + "_" + mCurrentEventID.toString(),
							transition.getEvent().getKind(), transition.getEvent()
							.isObservable());
					
					final TransitionProxy splitTransition = mFactory
					.createTransitionProxy(transition.getSource(),
							relabeledEvent, transition.getTarget());
					
					
					relabeledLocalAlphabet.add(relabeledEvent);
					mGlobalAlphabet.add(relabeledEvent);
					
					
					mEFARelabeledTransitionActionMap.put(splitTransition, action);
					mEFARelabeledTransitionGuardClauseMap.put(splitTransition,
							andClause);
					
					splitTransitions.add(splitTransition);
					
					mCurrentEventID++;
				}
			}
			else{
				relabeledLocalAlphabet.add(transition.getEvent());
				splitTransitions.add(transition);
				mGlobalAlphabet.add(transition.getEvent());
			}
		}
		
		AutomatonProxy relabeledAutomaton = mFactory.createAutomatonProxy(
				fullName + "_relabeled", kind, relabeledLocalAlphabet, mStates,
				splitTransitions);
		
		
		return relabeledAutomaton;
	}

	private Set<TransitionProxy> createRelabeledEFATransitions() {
		/*
		 * Rename the events that exist in transitions and construct
		 * relabeledTransitions. Map the
		 * relabeledTransitions to the action and the guard 
		 * at the corresponding edge.
		 * 
		 */
		ExpressionParser parser = new ExpressionParser(ModuleSubjectFactory
				.getInstance(), GuardExpressionOperatorTable.getInstance());

		mEFARelabeledTransitionActionMap = new HashMap<TransitionProxy, List<BinaryExpressionProxy>>();
		mEFARelabeledTransitionGuardClauseMap = new HashMap<TransitionProxy, SimpleExpressionProxy>();
		Set<TransitionProxy> relabeledTransitions = new TreeSet<TransitionProxy>();

		for (TransitionProxy transition : mEFATransitions) {

			final EventProxy relabeledEvent = mFactory.createEventProxy(
					transition.getEvent().getName() + "_" + mCurrentEventID.toString(),
					transition.getEvent().getKind(), transition.getEvent()
							.isObservable());

			final TransitionProxy relabeledTransition = mFactory
					.createTransitionProxy(transition.getSource(),
							relabeledEvent, transition.getTarget());

			mEFARelabeledTransitionActionMap.put(relabeledTransition,
					mEFATransitionEdgeMap.get(transition).getGuardActionBlock()
							.getActionList());

			String guardString = mEFATransitionEdgeMap.get(transition)
					.getGuardActionBlock().getGuard();

			if (guardString == null) {
				guardString = "true";
			}

			SimpleExpressionProxy guardExpression;

			try {
				guardExpression = parser.parse(guardString);
			} catch (ParseException e) {
				guardExpression = null;
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			mEFARelabeledTransitionGuardClauseMap.put(relabeledTransition,
					guardExpression);

			relabeledTransitions.add(relabeledTransition);

			mCurrentEventID++;
		}
		return relabeledTransitions;
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
    mFactory.createStateProxy(name, initial, stateProps);
  mStates.add(state);
  final CompiledNode compiled = new CompiledNode(proxy, state);
  mPrecompiledNodes.put(proxy, compiled);
  return compiled;
}

public Object visitSimpleParameterProxy
  (final SimpleParameterProxy proxy,
   final Class<? extends Value> type,
   final String typename)
  throws VisitorException
{
  try {
    final String name = proxy.getName();
    final SimpleExpressionProxy defaultExpr = proxy.getDefaultValue();
    final Value defaultValue =
      evalTyped(defaultExpr, type, typename);
    final Value value =
      getParameterValue(proxy, defaultValue, type, typename);
    mContext.add(name, value);
    return value;
  } catch (final EvalException exception) {
    exception.provideLocation(proxy);
    throw wrap(exception);
  }
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


//#########################################################################
//# Specific Evaluation Methods
private <V extends Value>
  V getParameterValue(final ParameterProxy param,
                      final V defaultValue,
                      final Class<? extends V> type,
                      final String typename)
  throws VisitorException
{
  try {
    if (mParameterMap == null) {
      return defaultValue;
    }
    final String name = param.getName();
    final CompiledParameterBinding binding = mParameterMap.remove(name);
    if (binding != null) {
      final Value actual = binding.getValue();
      return checkType(actual, type, typename);
    } else if (param.isRequired()) {
      throw new UndefinedIdentifierException
        (name, "required parameter", param);
    } else {
      return defaultValue;
    }
  } catch (final EvalException exception) {
    exception.provideLocation(param);
    throw wrap(exception);
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
      proxy = mFactory.createEventProxy(name, kind, observable);
      event.setEventProxy(proxy);
      mGlobalAlphabet.add(proxy);
      }
    mLocalAlphabet.add(proxy);
  }
}

	private void createTransitions(final NodeProxy source,
			final CompiledEventListValue events, final NodeProxy target,
			final CompiledNode groupEntry, final boolean deterministic,
			final EdgeProxy edge/* EFA */) throws VisitorException {
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
			final CompiledEventListValue events, final NodeProxy target,
			final CompiledNode groupEntry, final boolean deterministic,
			final EdgeProxy edge/* EFA */) throws VisitorException {
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
			final CompiledEventListValue events, final SimpleNodeProxy target,
			final CompiledNode groupEntry, final boolean deterministic,
			final EdgeProxy edge/* EFA */) throws VisitorException {
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
				CompiledTransition duplicate = null;
				boolean create = true;
				final Collection<CompiledTransition> compiledTransitions = sourceEntry
						.getCompiledTransitions(event);
				for (final CompiledTransition ctrans : compiledTransitions) {
					if (ctrans.getTarget() == targetEntry.getState()) {
						duplicate = ctrans;
						continue;
					}
					final NodeProxy cause = ctrans.getGroup();
			          if (groupEntry.hasProperChildNode(cause)) {
			            create = false;
			            break;
			          } else if (deterministic) {
			            throw new NondeterminismException
			              ("Multiple transitions labelled '" + event.getName() +
			               "' originating from state '" + source.getName() + "'!",
			               source);
			          }
			    }
				if (create) {
					final NodeProxy group = groupEntry.getNode();
					if (duplicate == null) {
						final TransitionProxy trans = mFactory
						.createTransitionProxy(sourceState, event,
								targetState);
						// EFA-----------
						mEFATransitionEdgeMap.put(trans, edge);
						mEFATransitions.add(trans);
						// --------------
						
						mTransitions.add(trans);
						sourceEntry.addTransition(trans, group);
					}
					
					else {
						final TransitionProxy trans = duplicate.getTransition();
						sourceEntry.addTransition(trans, group);
						// EFA---------
						/*if(mEFATransitionEdgeMap.containsKey(trans)
								&& !mEFATransitionEdgeMap
								.get(trans)
								.getGuardActionBlock()
								.equals(edge.getGuardActionBlock()))
							//Then we need to relabel the transition.
						{*/
							
							final EventProxy relabeledEvent = mFactory
							.createEventProxy(trans.getEvent()
									.getName()
									+ "*", trans.getEvent().getKind(),
									trans.getEvent().isObservable());
							
							final TransitionProxy relabeledTrans = mFactory
							.createTransitionProxy(trans.getSource(),
									relabeledEvent, trans.getTarget());
							
							mEFATransitionEdgeMap.put(relabeledTrans, edge);
							mEFATransitions.add(relabeledTrans);
						//}
						// ------------------
					}
				}
			}
		} catch (final NondeterminismException exception) {
			throw wrap(exception);
		}
	}

	private boolean evalBoolean(final SimpleExpressionProxy expr)
    throws VisitorException
  {
    final IntValue value = evalTyped(expr, IntValue.class, "INTEGER");
    return value.getValue() != 0;
  }

  private IndexValue evalIndex(final SimpleExpressionProxy expr)
    throws VisitorException
  {
    return evalTyped(expr, IndexValue.class, "INDEX");
  }

  private RangeValue evalRange(final SimpleExpressionProxy expr)
    throws VisitorException
  {
    return evalTyped(expr, RangeValue.class, "RANGE");
  }

  private <V extends Value>
    V evalTyped(final SimpleExpressionProxy expr,
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
  //# Data Members
  private final DocumentManager<DocumentProxy> mDocumentManager;
  private final ProductDESProxyFactory mFactory;
  private final ModuleProxy mModule;
  private final NameCompiler mNameCompiler = new NameCompiler();

  private CompilerContext mContext;
  private CompiledEventListValue mEventList;
  private Map<String,CompiledParameterBinding> mParameterMap;
  private Map<String,AutomatonProxy> mAutomata;
  private Map<NodeProxy,CompiledNode> mPrecompiledNodes;
  private Set<EventProxy> mGlobalAlphabet;
  private Set<EventProxy> mLocalAlphabet;
  private Set<StateProxy> mStates;
  private Collection<TransitionProxy> mTransitions;

	// EFA---------------------
	private Map<TransitionProxy, EdgeProxy> mEFATransitionEdgeMap;
	private Collection<TransitionProxy> mEFATransitions;
	private Map<TransitionProxy, List<BinaryExpressionProxy>> mEFARelabeledTransitionActionMap;
	private Map<TransitionProxy, SimpleExpressionProxy> mEFARelabeledTransitionGuardClauseMap;
	private boolean mIsEFA;
	private Integer mCurrentEventID;
	// ---------------------


}
