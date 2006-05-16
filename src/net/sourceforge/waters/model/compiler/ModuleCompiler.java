//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   ModuleCompiler
//###########################################################################
//# $Id: ModuleCompiler.java,v 1.32 2006-05-16 11:15:51 markus Exp $
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
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
//

import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

public class ModuleCompiler 
	extends AbstractModuleProxyVisitor 
{
  
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
			

			mIsEFA=false;
			for(Proxy proxy: mModule.getComponentList()){
			List <EdgeProxy> edges= ((SimpleComponentProxy) proxy).getGraph().getEdges();
				mIsEFA = mIsEFA || componentHasNonEmptyGuardActionBlock(edges);
			}
			
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
	
	public Object visitEdgeProxy(final EdgeProxy proxy) throws VisitorException 
	{
		final EventListExpressionProxy labels = proxy.getLabelBlock();
		final List<Proxy> list = labels.getEventList();
		if (list.isEmpty()) 
		{
			final EmptyLabelBlockException exception = new EmptyLabelBlockException(proxy, currentComponentName);
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
		mEFATransitionAutomatonMap = new HashMap<TransitionProxy, AutomatonProxy>();
		mEFATransitionGuardActionBlockMap = new HashMap<TransitionProxy, GuardActionBlockProxy>();
		mSimpleComponents = new LinkedList<SimpleComponentProxy>();
		mEFAEventEventMap = new HashMap<EventProxy, EventProxy>();
		mOriginalAlphabet = new TreeSet<EventProxy>();

		final List<AliasProxy> constants = proxy.getConstantAliasList();
		visitCollection(constants);
		final List<EventDeclProxy> events = proxy.getEventDeclList();
		visitCollection(events);
		final List<Proxy> aliases = proxy.getEventAliasList();
		visitCollection(aliases);
		final List<Proxy> components = proxy.getComponentList();
		visitCollection(components);

		if (mIsEFA) {
			compileEFA();
		}
		return null;
	}

	private void compileEFA() {
		
		
		/*
		 * Mappings used to create variable automata instanciated.
		 */
		mEFAEventGuardClauseMap = new HashMap<EventProxy, SimpleExpressionSubject>();
		mEFAEventActionListsMap = new HashMap<EventProxy, List<List<BinaryExpressionProxy>>>();
		
		/*
		 * Copy the GlobalAlphabet.
		 */
		final Set<EventProxy> newEvents = new HashSet<EventProxy>();
		newEvents.addAll(mOriginalAlphabet);
		
		for (EventProxy event : mOriginalAlphabet) {
			/*
			 * Find all possible transitions (synch-combinations between the automata)
			 * that the event can trigger(allTrans = allPaths).
			 */
			List<List<TransitionProxy>> allTrans = allPossibleTransitions(event);

			/*
			 * Collect guard and actions. Split the guards into andClauses.
			 * Update the automata with the final events and
			 * transitions. Fill mappings, mEFAEventGuardClauseMap and
			 * mEFAEventActionListMap, needed to build variable automata. 
			 */
			for (List<TransitionProxy> path : allTrans) {
				/*
				 * The event is renamed in two steps: first for each path and
				 * then for each andClause in the guard expression.
				 */
				if (!path.isEmpty()) {

					newEvents.remove(event);

					SimpleExpressionSubject guardExpression = collectGuard(path);
					List<List<BinaryExpressionProxy>> actionLists = collectAction(path);

					GuardExpressionHandler handler = new GuardExpressionHandler();
					handler.setExpression(guardExpression);
					List<SimpleExpressionSubject> andClauses = handler
							.getAndClauses();

					for (SimpleExpressionSubject andClause : andClauses) {
						final EventProxy relabeledEvent = mFactory
								.createEventProxy(event.getName() + "_"
										+ mCurrentEventID, event.getKind(),
										event.isObservable());
						mEFAEventGuardClauseMap.put(relabeledEvent, andClause);
						mEFAEventActionListsMap
								.put(relabeledEvent, actionLists);
						newEvents.add(relabeledEvent);

						mEFAEventEventMap.put(relabeledEvent, event);

						mCurrentEventID++;
						/*
						 * New transitions with the new event names are added to
						 * the automata.
						 */
						addNewTransitionsToAutomtata(relabeledEvent, path);

					}

				}
			}

		}
		removeOldTransitionsFromAutomtata();
		mGlobalAlphabet.clear();
		mGlobalAlphabet.addAll(newEvents);
        
		/*
		 * The variable automata are constructed using mSimpleComponents,
		 * mEFAEventGuardClauseMap and mEFAEventActionListMap.
		 */
		buildVariableAutomata();
	}
	
	
	private void removeOldTransitionsFromAutomtata() {
		Map<String, AutomatonProxy> Automata = new TreeMap<String, AutomatonProxy>();
		for(AutomatonProxy aut: mAutomata.values()){
			
			Set<EventProxy> efaEvents = new TreeSet<EventProxy>();
			Collection<TransitionProxy> efaTransitions = new TreeSet<TransitionProxy>();
			efaEvents.addAll(aut.getEvents());
			efaTransitions.addAll(aut.getTransitions());
			for(EventProxy event: mEFAEventEventMap.values()){
				efaEvents.remove(event);
				for(TransitionProxy trans: aut.getTransitions()){
					if(trans.getEvent().equals(event)){
						efaTransitions.remove(trans);
					}
				}
		  }
		Automata.put(aut.getName(), mFactory.createAutomatonProxy(
				  aut.getName(),
				  aut.getKind(),
				  efaEvents,
				  aut.getStates(),
				  efaTransitions));
		
		}
		mAutomata.clear();
		mAutomata.putAll(Automata);
		
	}

	private void buildVariableAutomata() {
		for (SimpleComponentProxy simpleComponent : mSimpleComponents)
				 {

			/*
			 * Get the EFA variables.
			 */
			final List<VariableProxy> variables = simpleComponent
					.getVariables();

			/*
			 * Create an automaton for each variable, the alphabet for each
			 * automaton is a subset of mEFAEvent.
			 */
			for (VariableProxy variable : variables) {
				final AutomatonProxy variableAutomaton = createVariableAutomaton(
						simpleComponent.getKind(), variable);
				// Add variable automaton to mAutomata.
				mAutomata.put(variable.getName(), variableAutomaton);
			}
		}
	}
	private AutomatonProxy createVariableAutomaton(ComponentKind kind,
			VariableProxy variable) {
		

		/*
		 * Create an expression handler to handle the guard expressions.
		 */
		GuardExpressionHandler handler = new GuardExpressionHandler();
		/*
		 * Declare variable, enum not yet implemented. (TODO remove comment when
		 * implemented).
		 */
		if (variable.getType() instanceof SimpleIdentifierProxy) {
			handler.declareVariable(variable.getName(),
					GuardExpressionHandler.Type.BOOLEAN);
		} else {
			handler.declareVariable(variable.getName(),
					GuardExpressionHandler.Type.INTEGER);
		}
		List<StateProxy> variableStates = new LinkedList<StateProxy>();
		/*
		 * Create states corresponding to the different states of the variable.
		 */
		createVariableStates(variable, variableStates);

        /*
         * Create transitions corresponding to all allowed updates of the variable.
         */
		/*
		 * Copy the relabeledAlphabet. Events that translates into
		 * selfloops in all variable states will be removed.
		 */
		final Set<EventProxy> variableAlphabet = new HashSet<EventProxy>();
		variableAlphabet.addAll(mEFAEventGuardClauseMap.keySet());
		
		Set<TransitionProxy> variableTransitions = 
			createVariableTransitions(variable,
					                  variableAlphabet,
									  handler,
									  variableStates);

		//Create variable automaton.
		final AutomatonProxy variableAutomaton = 
			mFactory.createAutomatonProxy(variable.getName(),
					                      kind, 
					                      variableAlphabet,
					                      variableStates,
					                      variableTransitions);
		return variableAutomaton;
	}

	private Set<TransitionProxy> createVariableTransitions(
			VariableProxy variable,
			Set<EventProxy> variableAlphabet,
			GuardExpressionHandler handler, List<StateProxy> variableStates) {
		
		
		Set<TransitionProxy> variableTransitions = new TreeSet<TransitionProxy>();
        
		for (EventProxy relabeledEvent : mEFAEventGuardClauseMap.keySet()) {
			
			//Get the right action.
			final List <List<BinaryExpressionProxy>> actionLists = mEFAEventActionListsMap
				.get(relabeledEvent);

			/*
			 * For each variable, find the action that updates this variable.
			 * Only one of the actionLists is allowed to update each variable.
			 */
			final BinaryExpressionProxy action = findAction(variable.getName(),
														   actionLists);

			//...and the corresponding guard
			final SimpleExpressionSubject guardClause = (SimpleExpressionSubject) mEFAEventGuardClauseMap
				.get(relabeledEvent);
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
				variableAlphabet.remove(relabeledEvent);
			}
			else {
				//variable type = finite integer
				if (variable.getType() instanceof BinaryExpressionProxy) {
					createIntegerVariableTransition(variable, handler, variableStates,
													variableTransitions, relabeledEvent, action);
				}
				//variable type = boolean
				else if (variable.getType() instanceof SimpleIdentifierProxy) {
					createBooleanVariableTransition(variable, handler, variableStates,
													variableTransitions, relabeledEvent, action);
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

	private void createVariableStates(VariableProxy variable, List<StateProxy> variableStates ) {
		
		final Collection<EventProxy> markedState = new TreeSet<EventProxy>();
		final Collection<EventProxy> unMarkedState = new TreeSet<EventProxy>();
		boolean hasMarking = false;
		EventProxy  variableMarking = 
			mFactory.createEventProxy("variableMarking", EventKind.PROPOSITION);
		markedState.add(variableMarking);
		
		SimpleExpressionProxy markedValue;
	     
		if(variable.getMarkedValue()!= null){
			hasMarking = true;
			markedValue = variable.getMarkedValue();
		} else {
			markedValue = null;
		}
		SimpleExpressionProxy type = variable.getType();
	
		if (type instanceof SimpleIdentifierProxy) {
			if (((SimpleIdentifierProxy) type).getName().equals("boolean")) {
				Collection<EventProxy> marking;
				if (variable.getInitialValue() instanceof BooleanConstantProxy) {
					boolean initialValue = ((BooleanConstantProxy) variable
											.getInitialValue()).isValue();
	
					if(hasMarking) {
						Boolean castMarkedValue = ((BooleanConstantProxy) markedValue).isValue();
						marking = (castMarkedValue == true ? markedState : unMarkedState);
					} else {
						marking = markedState;
					}
					final StateProxy variableTrueState = mFactory
						.createStateProxy(variable.getName() + "="
										  + "false", initialValue == false,
										  marking);
					variableStates.add(variableTrueState);
	
					if(hasMarking) {
						Boolean castMarkedValue = ((BooleanConstantProxy) markedValue).isValue();
						marking = (castMarkedValue == false ? markedState : unMarkedState);
					} else {
						marking = markedState;
					}
					final StateProxy variableFalseState = mFactory
						.createStateProxy(
										  variable.getName() + "=" + "true",
										  initialValue == true,
										  marking);
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
																		   isInitial, markedState);
				variableStates.add(variableState);
			}
		}
	
		else if (type instanceof BinaryExpressionProxy) {
			BinaryExpressionProxy binExpr = (BinaryExpressionProxy) type;
			Collection<EventProxy> marking;
			if (binExpr.getOperator().getName().equals("..")) {
				int lower = ((IntConstantProxy) binExpr.getLeft()).getValue();
				int higher = ((IntConstantProxy) binExpr.getRight()).getValue();
				int initialValue = ((IntConstantProxy) variable
									.getInitialValue()).getValue();
				for (Integer i = lower; i <= higher; i++) {
					if(hasMarking) {
						Integer castMarkedValue = ((IntConstantProxy) markedValue).getValue();
						marking = (castMarkedValue == i ? markedState : unMarkedState);
					} else {
						marking = markedState;
					}
					
					final StateProxy variableState = mFactory.createStateProxy(
																			   variable.getName() + "=" + i.toString(),
																			   initialValue == i, 
																			   marking);
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

	private BinaryExpressionProxy findAction(String name,
			List<List<BinaryExpressionProxy>> actionLists) {
		//TODO: check that only one automtata updates each variable.
		for (List<BinaryExpressionProxy> actions : actionLists){
		for (BinaryExpressionProxy action : actions) {
			if (((SimpleIdentifierProxy) action.getLeft()).
					getName().
					equals(name)){
				return action;
			}
		}
		}
		return null;
		
	}

	private void createBooleanVariableTransition(VariableProxy variable,
			GuardExpressionHandler handler,
			List<StateProxy> variableStates,
			Set<TransitionProxy> variableTransitions,
			EventProxy relabeledEvent,
			BinaryExpressionProxy action) {
		
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
					.createTransitionProxy(source, relabeledEvent, target);
				variableTransitions.add(actionTransition);
			}
		}
	}

	private void createIntegerVariableTransition(VariableProxy variable,
			GuardExpressionHandler handler,
			List<StateProxy> variableStates,
			Set<TransitionProxy> variableTransitions,
			EventProxy relabeledEvent, BinaryExpressionProxy action) {
		
		BinaryExpressionProxy binExpr = ((BinaryExpressionProxy) variable
				.getType());

		final int lower = ((IntConstantProxy) binExpr.getLeft()).getValue();
		final int upper = ((IntConstantProxy) binExpr.getRight()).getValue();
		final int range = upper - lower + 1;

		for (int sourceIndex = 0; sourceIndex < range; sourceIndex++) {
			int targetIndex = sourceIndex;

			// Set the variable to the right value.
			handler.assignValueToVariable(variable.getName(), sourceIndex
					+ lower);

			// Evaluate the guard function for this variable.
			Boolean guardValue;
			guardValue = evaluatePartialGuard(variable, handler);

			// Create action transition if guard == true.
			if (guardValue) {
				if (action != null) {
					// Get right hand side of action expression.
					int constant = ((IntConstantProxy) action.getRight())
							.getValue();

					// Get action expression operator.
					String operator = action.getOperator().getName();

					// Calculate transition
					if (operator.equals("+=")) {
						targetIndex = modulo((sourceIndex + constant), range);
					} else if (operator.equals("-=")) {
						targetIndex = modulo((sourceIndex - constant), range);
					} else if (operator.equals("=")) {
						targetIndex = modulo(constant, range);
					} else {
						targetIndex = -1;
						System.err.println("ModuleCompiler: Invalid operator");
						// EFA TODO throw exception
					}
				} else {
					// action is null => no update takes place.
					targetIndex = sourceIndex;
				}
				// Create transition
				final StateProxy source = variableStates.get(sourceIndex);
				final StateProxy target = variableStates.get(targetIndex);
				final TransitionProxy actionTransition = mFactory
						.createTransitionProxy(source, relabeledEvent, target);
				variableTransitions.add(actionTransition);
			}
		}
	}

	private List<List<TransitionProxy>> allPossibleTransitions(EventProxy event) {
		LinkedList<List<TransitionProxy>> transitions = new LinkedList<List<TransitionProxy>>();
		/*
		 * It is important that events with different GuardActionBlocks are
		 * translated to separate transitions.(mEFATransitions)
		 */
		for (AutomatonProxy automaton : mAutomata.values()) {
			List<TransitionProxy> transInAutomaton = new LinkedList<TransitionProxy>();
			
			for (TransitionProxy transition : automaton.getTransitions()) {
				if (transition.getEvent().equals(event)) {
					transInAutomaton.add(transition);
				}
				else if(mEFAEventEventMap.get(transition.getEvent())!= null){
					
					if(mEFAEventEventMap.get(transition.getEvent()).equals(event)){
						
							transInAutomaton.add(transition);
					}
					
				}
			}
			if (transInAutomaton.isEmpty()
					&& automaton.getEvents().contains(event)) {
				transitions.clear();
				break;
			} else if(!transInAutomaton.isEmpty()){
				transitions.add(transInAutomaton);
			}
		}
		List<List<TransitionProxy>> allPaths = allPossiblePaths(transitions);
		/*
		 * For debugging
		 * 
		 */
		//System.out.println("Event: " + event);
		//System.out.println("Paths: " + allPaths);
		return allPaths;
	}
		
	
	private List<List<TransitionProxy>> allPossiblePaths(
	LinkedList<List<TransitionProxy>> transitions) {
		if(transitions.isEmpty()) {
			List<List<TransitionProxy>> noPaths = new LinkedList<List<TransitionProxy>>();
			noPaths.add(new LinkedList<TransitionProxy>());
			return noPaths;
		}
		
		List<TransitionProxy> transInAut = transitions.removeFirst();
		List<List<TransitionProxy>> subPaths = allPossiblePaths(transitions);
		
		List<List<TransitionProxy>> paths = new LinkedList<List<TransitionProxy>>();
		
		for (List<TransitionProxy> subPath : subPaths) {

			for (TransitionProxy trans : transInAut) {
				List<TransitionProxy> p = new LinkedList<TransitionProxy>();
				p.addAll(subPath);
				p.add(trans);
				paths.add(p);
			}
		}
		return paths;
	}
	
	private void addNewTransitionsToAutomtata(EventProxy relabeledEvent, List<TransitionProxy> path) {
		for(TransitionProxy trans: path){
			Set<EventProxy> relabeledEvents = new TreeSet<EventProxy>();
			Collection<TransitionProxy> relabeledTransitions = new TreeSet<TransitionProxy>();
			
			
			/*
			 * Finding the original automata.
			 */
			
			AutomatonProxy aut= mEFATransitionAutomatonMap.get(trans);
			
			/*
			 * Add the relabeledEvent to relabeledEvents.
			 *
			 * It may still exist old transitions with the 
			 * old event name. Therefore we cannot remove 
			 * the old events here. 
			 */
			relabeledEvents.addAll(mAutomata.
			get(aut.getName()).getEvents());
			relabeledEvents.add(relabeledEvent);
	        /*
	         * Add new transition to relabeledTransitions.
	         */
		    relabeledTransitions.addAll(mAutomata.
			get(aut.getName()).getTransitions());
			TransitionProxy relabeledTransition = mFactory.createTransitionProxy(
					trans.getSource(),
					relabeledEvent,
					trans.getTarget());
			relabeledTransitions.add(relabeledTransition);
			
			/*
			 * Create updated automaton.
			 */
			AutomatonProxy relabeledAutomaton = mFactory.createAutomatonProxy(
					  aut.getName(),
					  aut.getKind(),
					  relabeledEvents,
					  aut.getStates(),
					  relabeledTransitions);
			/*
			 * Update mapping.
			 */
			mAutomata.put(aut.getName(),relabeledAutomaton);
			}
		
	}

	private SimpleExpressionSubject collectGuard(List<TransitionProxy> path) {
		SimpleExpressionSubject guardExpression;
		String guardString= new String("true");
		for (TransitionProxy transition : path) {
			if (mEFATransitionGuardActionBlockMap.get(transition) != null) {
				guardString = guardString + "&" + mEFATransitionGuardActionBlockMap
						.get(transition).getGuard();
			}
	
		}
		ExpressionParser parser = new ExpressionParser(ModuleSubjectFactory
				.getInstance(), GuardExpressionOperatorTable.getInstance());
		try {
			guardExpression = (SimpleExpressionSubject)parser.parse(guardString);
		} catch (ParseException e) {
			guardExpression = null;
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return guardExpression;
	}

	private List <List <BinaryExpressionProxy>> collectAction(List<TransitionProxy> path) {
		List <List <BinaryExpressionProxy>> actionLists = 
			new LinkedList<List <BinaryExpressionProxy>>();
		for(TransitionProxy transition: path){
			if(mEFATransitionGuardActionBlockMap.get(transition)!= null){
				actionLists.add
				(mEFATransitionGuardActionBlockMap.get(transition).getActionList());
			}
			
		}
		return actionLists;
	}

	private boolean componentHasNonEmptyGuardActionBlock(Collection<EdgeProxy> edges) {
		for(EdgeProxy edge: edges){
			if(edge.getGuardActionBlock() != null){
				return true;
			}
		}
		return false;
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
	public AutomatonProxy visitSimpleComponentProxy(final SimpleComponentProxy proxy) 
		throws VisitorException 
	{
		try {
			final IdentifierProxy ident = proxy.getIdentifier();
			final String name = (String) ident.acceptVisitor(mNameCompiler);
			final String fullName = mContext.getPrefixedName(name);
			currentComponentName = fullName;
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
			mPrecompiledNodes = new IdentityHashMap<NodeProxy, CompiledNode>(nodes.size());
			visitCollection(nodes);
			final Collection<EdgeProxy> edges = graph.getEdges();
			visitCollection(edges);
			mTransitions = new TreeSet<TransitionProxy>();
			//EFA with shared variables---------
			mSimpleComponents.add(proxy);
			//---------------------
			
			for (final NodeProxy source : nodes) {
				final CompiledNode sourceEntry = mPrecompiledNodes.get(source);
				for (final EdgeProxy edge : sourceEntry.getEdges()) {
					final NodeProxy target = edge.getTarget();
					final EventListExpressionProxy labelBlock = edge.getLabelBlock();
					final CompiledEventListValue events = visitEventListExpressionProxy(labelBlock, EventKindMask.TYPEMASK_EVENT);
					createAutomatonEvents(events);
					createTransitions(source, events, target, sourceEntry,
									  deterministic, edge);
					}
				sourceEntry.clearProperChildNodes();
			}
			
				/*
				 * States in different Automata are considered equal if the names are the same.
				 * This must be fixed since it leads to problems in the mappings:
				 *  mEFATransitionGuardActionBlockMap and mEFATransitionAutomatonMap.
				 */
				
				/*
				 * The automaton is created. If it is an EFA-automaton
				 * the events will be relabeled, transitions divided and 
				 * variableAutmata will be created in the method compileEFA().
				 * 
				 */
			    if(mIsEFA){
			    	Map <StateProxy, StateProxy> stateStateMap = 
			    		new HashMap<StateProxy, StateProxy>();
			    	/*
			    	 * Rename all states.
			    	 */
					for(StateProxy state: mStates){
						final StateProxy s = mFactory
						.createStateProxy(
										  name+"("+state.getName()+")",
										  state.isInitial(),
										  state.getPropositions());
					stateStateMap.put(state,s);
					
					}
					LinkedList<TransitionProxy> Transitions = 
			    		new LinkedList<TransitionProxy>();
					/*
					 * Rename all transitions. Copy mapping: transition to guard.
					 */
					Map<TransitionProxy, GuardActionBlockProxy> transitionGuardActionBlockMap
					= new HashMap<TransitionProxy, GuardActionBlockProxy>();
					
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
					TransitionProxy trans = mFactory.createTransitionProxy(
							source, transition.getEvent(), target);
					
					if(mEFATransitionGuardActionBlockMap.containsKey(transition)){
					transitionGuardActionBlockMap.put(trans,
							mEFATransitionGuardActionBlockMap.get(transition));
					}
					
					Transitions.add(trans);
					
				}
					/*
					 * Update mappings.
					 */
					
					mTransitions.clear();
					mTransitions.addAll(Transitions);
					mStates.clear();
					mStates.addAll(stateStateMap.values());
					mEFATransitionGuardActionBlockMap.clear();
					mEFATransitionGuardActionBlockMap.putAll(transitionGuardActionBlockMap);
				
					
			    	final AutomatonProxy aut = mFactory.createAutomatonProxy(
						fullName, kind, mLocalAlphabet, mStates, mTransitions);

				for (TransitionProxy trans : mTransitions) {
					mEFATransitionAutomatonMap.put(trans, aut);
				}
				mAutomata.put(fullName, aut);
				
				return aut;
			    }
			    else{
			    	final AutomatonProxy aut = mFactory.createAutomatonProxy(
						fullName, kind, mLocalAlphabet, mStates, mTransitions);
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



	private int modulo(int i, int range) {
		i = i % range;
		i = i + range;
		i = i % range;
		return i;
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
				mOriginalAlphabet.add(proxy);
			}
			mLocalAlphabet.add(proxy);
			mOriginalAlphabet.add(proxy);
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
				CompiledTransition duplicate=null;
				boolean create = true;
				final Collection<CompiledTransition> compiledTransitions = sourceEntry
					.getCompiledTransitions(event);
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
						throw new NondeterminismException("Multiple transitions labelled '"
														  + event.getName()
														  + "' originating from state '"
														  + source.getName() + "'!", source);
					}
				}
				if (create) {
					final NodeProxy group = groupEntry.getNode();
					if (duplicate == null) {
						final TransitionProxy trans = mFactory
							.createTransitionProxy(sourceState, event,
												   targetState);
						//EFA with shared variables----------
						mEFATransitionGuardActionBlockMap.put(trans, edge.getGuardActionBlock());
						//---------------------------
						
						mTransitions.add(trans);
						sourceEntry.addTransition(trans, group);
					}

					else {
						final TransitionProxy trans = duplicate.getTransition();
						sourceEntry.addTransition(trans, group);

						if (mIsEFA) {
							/*
							 * EFA: duplicate but with different
							 * GuardActionBlock, needs to relabeled.
							 */
							if (mEFATransitionGuardActionBlockMap
									.containsKey(trans)) {
								if (!mEFATransitionGuardActionBlockMap.get(
										trans).equals(
										edge.getGuardActionBlock())) {
									final EventProxy relabeledEvent = mFactory
											.createEventProxy(trans.getEvent()
													.getName()
													+ "_" + mCurrentEventID,
													trans.getEvent().getKind(),
													trans.getEvent()
															.isObservable());
									mCurrentEventID++;
									final EventProxy key = mFactory
											.createEventProxy(trans.getEvent()
													.getName()
													+ "_" + mCurrentEventID,
													trans.getEvent().getKind(),
													trans.getEvent()
															.isObservable());
									mCurrentEventID++;
									mEFAEventEventMap.put(key, relabeledEvent);
									mGlobalAlphabet.add(relabeledEvent);
									mLocalAlphabet.add(relabeledEvent);
									mEFAEventEventMap.put(relabeledEvent, trans
											.getEvent());

									final TransitionProxy efaCopy = mFactory
											.createTransitionProxy(trans
													.getSource(),
													relabeledEvent, trans
															.getTarget());
									mEFATransitionGuardActionBlockMap
											.put(efaCopy, edge
													.getGuardActionBlock());
									mTransitions.add(efaCopy);
								}
							}

						}
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

	private <V extends Value> V checkType(final Value value,
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

	private String currentComponentName = null;

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
	/*
	 * Mappings needed for EFA with guards on shared variables.
	 */
	private Map<TransitionProxy, AutomatonProxy> mEFATransitionAutomatonMap;
	private Map<TransitionProxy, GuardActionBlockProxy> mEFATransitionGuardActionBlockMap;
	private Map<EventProxy, SimpleExpressionSubject> mEFAEventGuardClauseMap;
	private Map<EventProxy, List<List<BinaryExpressionProxy>>> mEFAEventActionListsMap;
	private List<SimpleComponentProxy> mSimpleComponents;
	private Map <EventProxy, EventProxy> mEFAEventEventMap;
	private Integer mCurrentEventID;
	private Set<EventProxy> mOriginalAlphabet;
	private boolean mIsEFA;
	//-------------------------------

}
