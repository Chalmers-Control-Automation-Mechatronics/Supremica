// # -*- indent-tabs-mode: nil c-basic-offset: 2 -*-
// ###########################################################################
// # PROJECT: Hierarchical and Decentralized Supervisory Control
// # PACKAGE: org.supremica.automata.algorithms.HDS
// # CLASS: EFAPartialEvaluator
// ###########################################################################
// # $Id$
// ###########################################################################

package org.supremica.automata.algorithms.IISCT;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.queue.TIntQueue;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.efa.simple.SimpleEFAComponent;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAEventEncoding;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAHelper;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFALabelEncoding;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAStateEncoding;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariable;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariableContext;
import net.sourceforge.waters.analysis.tr.DFSIntSearchSpace;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.constraint.ConstraintPropagator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

/**
 * A not efficient and hairy, yet working, implementation of partial evaluation
 * for EFAs
 * <p>
 *
 * @author Mohammad Reza Shoaei
 */
public class EFAPartialEvaluator extends DefaultModuleProxyVisitor {

	/**
	 * A utility to partially evaluates given variables. The obtained values at
	 * each state will be stored as an attribute.
	 */
	public EFAPartialEvaluator(final CompilerOperatorTable op,
			final SimpleEFAVariableContext varContext, final SimpleEFAEventEncoding encoding) {
		mFactory = ModuleElementFactory.getInstance();
		mOperatorTable = op;
		mVarContext = varContext;
		mEventEncoding = encoding;
		mPropagator = new ConstraintPropagator(mFactory, mOperatorTable, varContext);
		mResiduals = new THashSet<>();
		mHelper = new SimpleEFAHelper();
		mComparator = new ExpressionComparator(op);
	}

	/**
	 * A utility to partially evaluates given variables. The obtained values at
	 * each state will be stored as an attribute.
	 */
	public EFAPartialEvaluator(final SimpleEFAVariableContext varContext,
			final SimpleEFAEventEncoding encoding) {
		this(CompilerOperatorTable.getInstance(), varContext, encoding);
	}

	/**
	 * Initialize the PE with given component.
	 * @param component
	 *            EFA component
	 */
	public void init(final SimpleEFAComponent component) {
		reset();
		if (component != null) {
			init(setUp(Collections.singleton(component)));
		}
	}

	/**
	 * Initialize the PE with given components.
	 * @param components
	 *            EFA components
	 */
	public void init(final Collection<SimpleEFAComponent> components) {
		reset();
		if (!components.isEmpty()) {
			init(setUp(components));
		}
	}

	/**
	 * Initialize the PE with a map of component-variables for evaluation.
	 * @param componentVariablesMap
	 *            A map with key component and set of variables as value
	 */
	public void init(final THashMap<SimpleEFAComponent, TIntHashSet> componentVariablesMap) {
		reset();
		mCompVarsMap = componentVariablesMap;
	}

	/**
	 * If enabled, obtained values will be appended to the state names. This can
	 * be used for transferring the values.
	 */
	public void setAppendValueToStateName(final boolean enable) {
		mAppendValueToStateName = enable;
	}

	/**
	 * Returning the residual components.
	 * @return Set of residual components
	 */
	public Collection<SimpleEFAComponent> getResidualComponents() {
		return mResiduals;
	}

	/**
	 * Returning the set of given variables for evaluation.
	 * @return A set of given variables for evaluation
	 */
	public Collection<SimpleEFAVariable> getEvaluatedVariables() {
		final THashSet<SimpleEFAVariable> list = new THashSet<>(mCompVarsMap.size());
		for (final TIntHashSet vars : mCompVarsMap.values()) {
			list.addAll(mVarContext.getVariables(vars.toArray()));
		}
		return list;
	}

	/**
	 * Setting a suffix to be used to name the residual components.
	 */
	public void setSuffixName(final String suffix) {
		mSuffixName = suffix;
	}

	/**
	 * Evaluating given components w.r.t. input variables
	 * @return true if at least one component is evaluated; otherwise false
	 */
	public boolean evaluate() throws EvalException, AnalysisException {
		boolean successful = false;
		for (final SimpleEFAComponent component : mCompVarsMap.keySet()) {
			final TIntHashSet PEVars = mCompVarsMap.get(component);
			if (PEVars.isEmpty()) {
				continue;
			}
			evaluate(component, PEVars);
			final SimpleEFAComponent pe = createResidualEFA(component);
			if (pe != null) {
				successful = true;
				mResiduals.add(pe);
			}
		}
		return successful;
	}

	private void reset() {
		mResiduals.clear();
		mCompVarsMap = null;
		mTupleList = null;
		mLabelEncoding = null;
		mPrimed = null;
		mUnprimed = null;
		mStateMarking = null;
		pTR = null;
		pStatePropMap = null;
		mStateMarking = null;
	}

	private THashMap<SimpleEFAComponent, TIntHashSet> setUp(
			final Collection<SimpleEFAComponent> components) {
		final THashMap<SimpleEFAComponent, TIntHashSet> map = new THashMap<>(components.size());
		for (final SimpleEFAComponent component : components) {
			final TIntHashSet PEVars = new TIntHashSet();
			if (!component.isEFA()) {
				map.put(component, PEVars);
				continue;
			}
			for (final SimpleEFAVariable var : mVarContext.getVariables()) {
				// If the variable is only modified by this component
				// or is local in the system
				if (var.isLocalIn(component)) {
					PEVars.add(mVarContext.getVariableId(var));
				}
			}
			map.put(component, PEVars);
		}
		return map;
	}

	private void evaluate(final SimpleEFAComponent component, final TIntHashSet PEVars)
			throws EvalException, AnalysisException {
		mTupleList = new ArrayList<>();
		pTR = new ArrayList<>();
		pStatePropMap = new TIntLongHashMap();
		mPrimed = new TIntHashSet(component.getVariables().size());
		mUnprimed = new TIntHashSet(component.getVariables().size());
		mStateMarking = new ArrayList<>();
		final ConstraintList pMarking = getMarkingConstraints(PEVars);
		mHasMarkingValue = !pMarking.isTrue();
		final ListBufferTransitionRelation oRel = component.getTransitionRelation();
		oRel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
		final TransitionIterator oIter = oRel.createSuccessorsReadOnlyIterator();
		final SimpleEFAStateEncoding oStateEncoding = component.getStateEncoding();
		final SimpleEFALabelEncoding oLabelEncoding = component.getTransitionLabelEncoding();
		final TIntObjectHashMap<Object[]> oStateInfo = getStateInfoMap(oStateEncoding);
		final int oInitStateId = oStateEncoding.getInitialStateId();
		mLabelEncoding = new SimpleEFALabelEncoding(mEventEncoding, oLabelEncoding.size());
		final ConstraintList pInitExp = mergeByString(
				getPrettyExpressions(getInitialExpressions(PEVars)),
				(ConstraintList) oStateInfo.get(oInitStateId)[0]);
		pInitExp.sort(mComparator);
		final int pInitStateId = createStateId(oInitStateId, pInitExp);
		if (mHasMarkingValue) {
			mStateMarking.add(pInitStateId, !execute(pMarking, pInitExp));
		}
		pStatePropMap.put(pInitStateId, oRel.getAllMarkings(oInitStateId));
		final TIntQueue queue = new DFSIntSearchSpace();
		queue.add(pInitStateId);
		// System.err.println("PE Start Analyzing > " + component.getName());
		/////////////////////////////////////////////////////////////////////
		while (!queue.isEmpty()) {
			final int currSourceId = queue.element();
			final ConstraintList currSourceValue = getValue(currSourceId);
			oIter.resetState(getState(currSourceId));
			/**
			 * ****************************************************************
			 */
			while (oIter.advance()) {
				final int currLabelId = oIter.getCurrentEvent();
				final int currTargetId = oIter.getCurrentTargetState();
				final TIntHashSet currSourceVars = new TIntHashSet();
				mVarContext.collectAllVariables(currSourceValue, currSourceVars);
				final ConstraintList currTargetValue = (ConstraintList) oStateInfo
						.get(currTargetId)[0];
				if (!currTargetValue.isTrue()) {
					final TIntHashSet currTargetVars = new TIntHashSet();
					mVarContext.collectAllVariables(currTargetValue, currTargetVars);
					currSourceVars.removeAll(currTargetVars);
				}
				ConstraintList currCondition = oLabelEncoding.getConstraintByLabelId(currLabelId);
				currCondition = getCompleteFormExpressions(currCondition, currSourceVars);
				final List<SimpleExpressionProxy> nextValues = new ArrayList<>();
				final List<SimpleExpressionProxy> nextConditions = new ArrayList<>();
				final boolean isSatisfiable = execute(PEVars, currSourceValue, currCondition,
						nextValues, nextConditions);
				// If is not satisfiable then continue with the next transition.
				if (!isSatisfiable) {
					continue;
				}
				final ConstraintList nextCon = new ConstraintList(nextConditions);
				if (!nextCon.isTrue()) {
					mVarContext.collectAllVariables(nextCon, mUnprimed, mPrimed);
				}
				ConstraintList nextValue = new ConstraintList(nextValues);
				if (!currTargetValue.isTrue()) {
					nextValue = mergeByString(nextValue, currTargetValue);
				}
				nextValue.sort(mComparator);
				final int nextEvent = oLabelEncoding.getEventIdByLabelId(currLabelId);
				final int nextLabelId = mLabelEncoding.createTransitionLabelId(nextEvent, nextCon);
				int nextStateId = getStateId(currTargetId, nextValue);
				// if it is a new state
				if (nextStateId < 0) {
					nextStateId = createStateId(currTargetId, nextValue);
					pStatePropMap.put(nextStateId, oRel.getAllMarkings(currTargetId));
					if (mHasMarkingValue) {
						mStateMarking.add(nextStateId, !execute(pMarking, nextValue));
					}
					queue.add(nextStateId);
				}
				final int[] tr = { currSourceId, nextLabelId, nextStateId };
				// Adding the new transition to the list.
				pTR.add(tr);
			}
		}
	}

	private SimpleEFAComponent createResidualEFA(final SimpleEFAComponent component)
			throws AnalysisException {
		final String pComponentName = component.getName() + mSuffixName;
		final ListBufferTransitionRelation oRel = component.getTransitionRelation();
		final SimpleEFAStateEncoding pStateEncoding = getStateEncoding(
				component.getStateEncoding());
		final TIntHashSet pVars = new TIntHashSet(mUnprimed);
		pVars.addAll(mPrimed);
		// Creating a transition relation (pRel) based on the list of
		// transitions (pTR).
		final ListBufferTransitionRelation rel = createTransitionRelation(pComponentName,
				component.getKind(), mLabelEncoding.size(), oRel.getNumberOfPropositions(),
				pStateEncoding.size());
		// Creating a residual EFA.
		final SimpleEFAComponent residual = new SimpleEFAComponent(pComponentName, pVars.toArray(),
				mVarContext, pStateEncoding, mLabelEncoding, rel, component.getBlockedEvents(),
				component.getKind());
		// Registering the remaning variables to this component.
		residual.setStructurallyDeterministic(true);
		// Setting the visitor / modifiers of the variables
		residual.setIsEFA(!pVars.isEmpty());
		residual.setUnprimeVariables(mUnprimed.toArray());
		residual.setPrimeVariables(mPrimed.toArray());
		residual.register();

		return residual;
	}

	private ListBufferTransitionRelation createTransitionRelation(final String pComponentName,
			final ComponentKind kind, final int nbrLabels, final int nbrPropositions,
			final int nbrStates) throws OverflowException {
		final ListBufferTransitionRelation rel = new ListBufferTransitionRelation(pComponentName,
				kind, nbrLabels, nbrPropositions, nbrStates,
				ListBufferTransitionRelation.CONFIG_SUCCESSORS);
		if (!pTR.isEmpty()) {
			rel.setInitial(pTR.get(0)[0], true);
			if (mHasMarkingValue) {
				for (final int[] tr : pTR) {
					final int source = tr[0];
					final int label = tr[1];
					final int target = tr[2];
					rel.setAllMarkings(source, pStatePropMap.get(source));
					rel.setAllMarkings(target, pStatePropMap.get(target));
					final boolean m1 = mStateMarking.get(source)
							&& rel.isMarked(source, SimpleEFAHelper.DEFAULT_MARKING_ID);
					final boolean m2 = mStateMarking.get(target)
							&& rel.isMarked(target, SimpleEFAHelper.DEFAULT_MARKING_ID);
					rel.setMarked(source, SimpleEFAHelper.DEFAULT_MARKING_ID, m1);
					rel.setMarked(target, SimpleEFAHelper.DEFAULT_MARKING_ID, m2);
					rel.addTransition(source, label, target);
				}
			} else {
				for (final int[] tr : pTR) {
					rel.setAllMarkings(tr[0], pStatePropMap.get(tr[0]));
					rel.setAllMarkings(tr[2], pStatePropMap.get(tr[2]));
					rel.addTransition(tr[0], tr[1], tr[2]);
				}
			}
		}
		return rel;
	}

	private SimpleEFAStateEncoding getStateEncoding(final SimpleEFAStateEncoding oStateEncoding) {
		final SimpleEFAStateEncoding pStateEncoding = new SimpleEFAStateEncoding(mTupleList.size());
		for (int id = 0; id < mTupleList.size(); id++) {
			final Object[] item = mTupleList.get(id);
			final int oStateId = (int) item[0];
			boolean isInitial = false;
			if (id == 0) {
				isInitial = true;
			}
			String name = SimpleEFAHelper.DEFAULT_STATE_NAME + id;
			if (mAppendValueToStateName) {
				name = getStateName(name, (ConstraintList) item[1]);
			}
			boolean isMarked = oStateEncoding.isMarked(oStateId);
			if (mHasMarkingValue) {
				isMarked = isMarked && mStateMarking.get(id);
			}
			final boolean isForbidden = oStateEncoding.isForbidden(oStateId);
			final SimpleNodeProxy oState = oStateEncoding.getSimpleState(oStateId);
			final SimpleNodeProxy pState = mHelper.getSimpleNodeSubject(name, isInitial, isMarked,
					isForbidden, oState.getAttributes());
			final int pStateId = pStateEncoding.createSimpleStateId(pState);
			final String value = printExpressions((ConstraintList) item[1]);
			pStateEncoding.mergeToAttribute(pStateId, SimpleEFAHelper.DEFAULT_STATEVALUE_KEY,
					value);
		}
		return pStateEncoding;
	}

	private boolean execute(final TIntHashSet PEVars, final ConstraintList currValues,
			final ConstraintList currConditions, final List<SimpleExpressionProxy> nextValues,
			final List<SimpleExpressionProxy> nextConditions) throws EvalException {
		mPropagator.init(currValues);
		mPropagator.addConstraints(currConditions);
		mPropagator.propagate();
		if (mPropagator.isUnsatisfiable()) {
			return false;
		}
		final ConstraintList eCons = mPropagator.getAllConstraints();
		final List<SimpleExpressionProxy> eExps = new ArrayList<>(eCons.getConstraints());
		final List<SimpleExpressionProxy> oExps = new ArrayList<>(currValues.getConstraints());
		// Removing all currValues
		final List<SimpleExpressionProxy> cExps = removeExpressions(eExps, oExps);
		for (final SimpleExpressionProxy eExp : cExps) {
			if (mVarContext.findPrimeVariables(eExp, PEVars)) {
				final TIntHashSet primed = new TIntHashSet();
				final TIntHashSet unprimed = new TIntHashSet();
				mVarContext.collectAllVariables(eExp, unprimed, primed);
				if (unprimed.isEmpty()) {
					final SimpleExpressionProxy nextValue = getNextValue(eExp);
					nextValues.add(nextValue);
				} else {
					nextConditions.add(eExp);
				}
			} else {
				nextConditions.add(eExp);
			}
		}
		return true;
	}

	private boolean execute(final ConstraintList con1, final ConstraintList con2)
			throws EvalException {
		mPropagator.init(con1);
		mPropagator.addConstraints(con2);
		mPropagator.propagate();
		return mPropagator.isUnsatisfiable();
	}

	private TIntObjectHashMap<Object[]> getStateInfoMap(final SimpleEFAStateEncoding encode) {
		final TIntObjectHashMap<Object[]> map = new TIntObjectHashMap<>(encode.size());
		for (int id = 0; id < encode.size(); id++) {
			final String value = encode.getAttribute(id, SimpleEFAHelper.DEFAULT_STATEVALUE_KEY);
			final List<SimpleExpressionProxy> parse = new ArrayList<>();
			if (value != null) {
				parse.addAll(mHelper.parse(value, SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR));
			}
			final ConstraintList oStateValue = new ConstraintList(parse);
			final TIntHashSet oStateVars = new TIntHashSet();
			if (!oStateValue.isTrue()) {
				mVarContext.collectAllVariables(oStateValue, oStateVars);
			}
			map.put(id, new Object[] { oStateValue, oStateVars });
		}
		return map;
	}

	private ConstraintList getMarkingConstraints(final TIntHashSet vars) {
		final THashSet<SimpleExpressionProxy> varMarkings = new THashSet<>();
		for (final int var : vars.toArray()) {
			for (final VariableMarkingProxy mexp : mVarContext.getVariable(var)
					.getVariableMarkings()) {
				varMarkings.add(mexp.getPredicate());
			}
		}
		return new ConstraintList(varMarkings);
	}

	private SimpleExpressionProxy getNextValue(final SimpleExpressionProxy eExp)
			throws EvalException {
		try {
			final Object result = eExp.acceptVisitor(this);
			if (result != null) {
				return (SimpleExpressionProxy) result;
			} else {
				throw new EvalException("PE > getNextValue: Not the expected result");
			}
		} catch (final VisitorException ex) {
			throw new EvalException("PE > getNextValue: " + ex);
		}
	}

	private ConstraintList getPrettyExpressions(final ConstraintList constraint)
			throws EvalException {
		mPropagator.init(constraint);
		mPropagator.propagate();
		return mPropagator.getAllConstraints();
	}

	private ConstraintList getInitialExpressions(final TIntHashSet vars) {
		final List<SimpleExpressionProxy> inits = new ArrayList<>(vars.size());
		for (final int var : vars.toArray()) {
			final SimpleExpressionProxy exp = mVarContext.getVariable(var)
					.getInitialStatePredicate();
			inits.add(exp);
		}
		return new ConstraintList(inits);
	}

	private SimpleExpressionProxy getKeepCurrentValue(final UnaryExpressionProxy lhs) {
		return mFactory.createBinaryExpressionProxy(mOperatorTable.getEqualsOperator(), lhs,
				lhs.getSubTerm());
	}

	private String getStateName(final String oStateName, final ConstraintList list) {
		final List<SimpleExpressionProxy> nList = new ArrayList<>();
		for (final SimpleExpressionProxy exp : list.getConstraints()) {
			if (!oStateName.contains(exp.toString())) {
				nList.add(exp);
			}
		}
		if (nList.size() < 1) {
			return oStateName;
		}
		return oStateName + printExpressions(new ConstraintList(nList),
				SimpleEFAHelper.DEFAULT_OPENING_STRING, SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR,
				SimpleEFAHelper.DEFAULT_CLOSING_STRING);
	}

	private ConstraintList getCompleteFormExpressions(final ConstraintList contraints,
			final TIntHashSet localVars) {
		final List<SimpleExpressionProxy> exps = new ArrayList<>(contraints.getConstraints());
		for (final int var : localVars.toArray()) {
			if (!mVarContext.findPrimeVariable(contraints, var)) {
				final SimpleExpressionProxy exp = getKeepCurrentValue(
						mVarContext.getVariable(var).getPrimedVariableName());
				exps.add(exp);
			}
		}
		return new ConstraintList(exps);
	}

	private List<SimpleExpressionProxy> removeExpressions(final List<SimpleExpressionProxy> list,
			final List<SimpleExpressionProxy> exps) throws EvalException {
		final List<SimpleExpressionProxy> result = new ArrayList<>(list.size());
		final List<SimpleExpressionProxy> pair = new ArrayList<>(exps.size() + 1);
		pair.addAll(exps);
		for (final SimpleExpressionProxy item : list) {
			pair.add(item);
			mPropagator.init(new ConstraintList(pair));
			mPropagator.propagate();
			final ConstraintList prop = mPropagator.getAllConstraints();
			if (prop.size() > exps.size()) {
				result.add(item);
			}
			pair.remove(item);
		}
		return result;
	}

	private int getStateId(final int state, final ConstraintList value) {
		for (int index = 0; index < mTupleList.size(); index++) {
			final Object[] tuple = mTupleList.get(index);
			if ((int) tuple[0] == state) {
				final ConstraintList v = (ConstraintList) tuple[1];
				// Since expressions are sorted
				if (v.toString().equals(value.toString())) {
					return index;
				}
			}
		}
		return -1;
	}

	private int createStateId(final int state, final ConstraintList value) {
		final int id = mTupleList.size();
		mTupleList.add(new Object[] { state, value });
		return id;
	}

	private ConstraintList getValue(final int id) {
		return (ConstraintList) mTupleList.get(id)[1];
	}

	private int getState(final int id) {
		return (int) mTupleList.get(id)[0];
	}

	private static ConstraintList mergeByString(final ConstraintList con1,
			final ConstraintList con2) {
		if (con2 == null) {
			return con1;
		}
		if (con1.isTrue()) {
			return con2;
		}
		if (con2.isTrue()) {
			return con1;
		}
		final THashSet<SimpleExpressionProxy> sList = new THashSet<>(con1.getConstraints());
		sList.addAll(con2.getConstraints());
		final THashMap<String, SimpleExpressionProxy> map = new THashMap<>();
		for (final SimpleExpressionProxy exp : sList) {
			map.put(exp.toString(), exp);
		}
		sList.clear();
		sList.addAll(map.values());
		return new ConstraintList(sList);
	}

	private String printExpressions(final ConstraintList constraints) {
		return printExpressions(constraints, "", SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR, "");
	}

	private String printExpressions(final ConstraintList constraints, final String opening,
			final String separator, final String closing) {
		if (constraints.isTrue()) {
			return "";
		}
		return SimpleEFAHelper.printer(constraints, opening, separator, closing);
	}

	// #########################################################################
	@Override
	public Object visitIdentifierProxy(final IdentifierProxy ident) {
		return null;
	}

	@Override
	public BinaryExpressionProxy visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
			throws VisitorException {
		final SimpleExpressionProxy lhs = expr.getLeft();
		final SimpleExpressionProxy nlhs = (SimpleExpressionProxy) lhs.acceptVisitor(this);
		return mFactory.createBinaryExpressionProxy(expr.getOperator(), nlhs, expr.getRight());
	}

	@Override
	public Object visitSimpleExpressionProxy(final SimpleExpressionProxy expr) {
		return expr;
	}

	@Override
	public Object visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
			throws VisitorException {
		final SimpleExpressionProxy subterm = expr.getSubTerm();
		final UnaryOperator operator = expr.getOperator();
		if (operator == mOperatorTable.getNextOperator()) {
			return subterm;
		}
		final SimpleExpressionProxy result = (SimpleExpressionProxy) subterm.acceptVisitor(this);
		return mFactory.createUnaryExpressionProxy(operator, result);
	}

	// #########################################################################
	// # Data Members
	private final ConstraintPropagator mPropagator;
	private final ModuleProxyFactory mFactory;
	private final CompilerOperatorTable mOperatorTable;
	private THashMap<SimpleEFAComponent, TIntHashSet> mCompVarsMap;
	private ArrayList<Object[]> mTupleList;
	private final SimpleEFAVariableContext mVarContext;
	private final Collection<SimpleEFAComponent> mResiduals;
	private String mSuffixName = ".PE";
	private final SimpleEFAHelper mHelper;
	private boolean mAppendValueToStateName;
	private ArrayList<int[]> pTR;
	private SimpleEFALabelEncoding mLabelEncoding;
	private TIntLongHashMap pStatePropMap;
	private TIntHashSet mUnprimed;
	private TIntHashSet mPrimed;
	private ArrayList<Boolean> mStateMarking;
	private boolean mHasMarkingValue;
	private final ExpressionComparator mComparator;
	private final SimpleEFAEventEncoding mEventEncoding;
}
