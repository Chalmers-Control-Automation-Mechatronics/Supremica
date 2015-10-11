// # -*- indent-tabs-mode: nil c-basic-offset: 2 -*-
// ###########################################################################
// # PROJECT:
// # PACKAGE: org.supremica.automata.algorithms.PDR
// # CLASS: Z3Solver
// ###########################################################################
// # $Id$
// ###########################################################################

package org.supremica.automata.algorithms.IISCT.SMTSolver;

import com.microsoft.z3.ApplyResult;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Goal;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Params;
import com.microsoft.z3.RealExpr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Symbol;
import com.microsoft.z3.Tactic;
import com.microsoft.z3.Z3Exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariable;
import net.sourceforge.waters.model.compiler.context.CompiledIntRange;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;

/**
 * This is a helper class to interact with Microsoft Z3 SMT Solver. Note that,
 * if not set explicitly, the variables are considered as Real.
 *
 * @author Mohammad Reza Shoaei
 */
public class Z3Solver extends AbstractSolver {

	public Z3Solver() throws SolverException {
		this(null);
	}

	public Z3Solver(final List<SimpleEFAVariable> variableContext) throws SolverException {
		zFormulas = new ArrayList<>();
		zAsserts = new HashMap<>();
		zSolverStatus = SolverStatus.UNKNOWN;
		zFuncDecl = new HashMap<>();
		mVariableContext = variableContext;
		mVarTypeReal = true;
		// Initializing Z3
		try {
			// Loading libz3.dll (requires libz3java.dll) from ./dist folder
			// Note that, for release version these dlls needs to be considered in the package
			System.loadLibrary("libz3");
			ctx = new Context();
			solver = ctx.mkSolver(ctx.mkTactic("smt"));
			params = ctx.mkParams();
			params.add("mbqi", true);
			solver.setParameters(params);
			mZDomain = new ArrayList<>();
			mkZDomain();
			// Making tactics
			TACTIC_SIMPLIFY = ctx.andThen(ctx.mkTactic("simplify"), ctx.mkTactic("nnf"),
					ctx.mkTactic("ctx-solver-simplify"), ctx.mkTactic("propagate-values"));
			TACTIC_SPLIT = ctx.andThen(ctx.orElse(ctx.mkTactic("split-clause"), ctx.skip()),
					TACTIC_SIMPLIFY);
			TACTIC_QE = ctx.andThen(ctx.mkTactic("qe"), TACTIC_SIMPLIFY);
			TACTIC_CNF = ctx.andThen(TACTIC_SIMPLIFY, ctx.mkTactic("tseitin-cnf"));
			TACTIC_DNF = ctx.andThen(TACTIC_SIMPLIFY,
					ctx.repeat(ctx.orElse(ctx.mkTactic("split-clause"), ctx.skip()), 5));
			ZFALSE = ctx.mkFalse();
			ZTRUE = ctx.mkTrue();
		} catch (final Z3Exception ex) {
			throw new SolverException(SOLVER_NAME, ex);
		} catch (final UnsatisfiedLinkError ex) {
			throw new SolverException(SOLVER_NAME,
					"Cannot find 'libz3.dll' and/or 'libz3java.dll' files in ./dist folder. "
							+ "Please add these files in the folder and try again. "
							+ "You can find these files from the compiled Z3 project on GitHub.");
		}
	}

	@SuppressWarnings("unused")
	/**
	 * For internal tests only. Do not call otherwise.
	 * @throws SolverException
	 */
	public void run() throws SolverException {
		try {
			final String[] str = new String[] { "" };
			final int[] index = new int[] { 0, 1 };
			addStrExpToContext(index, "x<=2 & x'==x+1");
			addStrExpToContext(index, "x'<=2");
			final List<BoolExpr> f = getContextZFormulas();
			System.err.println(entails(f.get(0), f.get(1)));
			this.dispose();
			System.err.println("");
		} catch (final Exception ex) {
			throw new SolverException(SOLVER_NAME, ex);
		}
	}

	public void addSimExpToContext(final int firstIndex, final boolean incremental,
			final List<SimpleExpressionProxy> exps) throws SolverException {
		zFormulas.add(mkZFormula(firstIndex, incremental, exps));
	}

	public void addSimExpToContext(final int[] indexSet,
			final List<List<SimpleExpressionProxy>> exps) throws SolverException {
		zFormulas.add(mkZFormula(indexSet, exps));
	}

	public void addStrExpToContext(final int firstIndex, final boolean incremental,
			final String... str) throws SolverException {
		zFormulas.add(mkZFormula(firstIndex, incremental, str));
	}

	public void addStrExpToContext(final int[] indexSet, final List<String> str)
			throws SolverException {
		zFormulas.add(mkZFormula(indexSet, str.toArray(new String[str.size()])));
	}

	public void addStrExpToContext(final int[] indexSet, final String... str)
			throws SolverException {
		zFormulas.add(mkZFormula(indexSet, str));
	}

	public void addZ3ExpToContext(final BoolExpr zExp) {
		if (zExp != null) {
			zFormulas.add(zExp);
		}
	}

	public void addZ3ExpToContext(final List<BoolExpr> zExp) {
		if (zExp != null) {
			zExp.stream().forEach(zBExp -> zFormulas.add(zBExp));
		}
	}

	public void assertAndTrack(final BoolExpr zExp) {
		zAsserts.put(UNSATCORE_STRING + Integer.toString(zAsserts.size()), zExp);
	}

	public void assertAndTrack(final List<BoolExpr> zExps) {
		zExps.forEach(e -> assertAndTrack(e));
	}

	@Override
	public SolverStatus check() throws SolverException {
		return check(new BoolExpr[0]);
	}

	public SolverStatus check(final BoolExpr... assumption) throws SolverException {
		try {
			solver.reset();
			zFormulas.stream().sequential().forEach(f -> solver.add(f));
			zSolverStatus = getStatus(solver.check(assumption));
			return zSolverStatus;
		} catch (final Z3Exception ex) {
			throw new SolverException(SOLVER_NAME, ex);
		}
	}

	public void clear() {
		zFormulas.clear();
		zAsserts.clear();
		zSMTLIB2.reset();
	}

	public void dispose() {
		ctx.dispose();
	}

	public Boolean entails(final BoolExpr f1, final BoolExpr f2, final BoolExpr... assumption) {
		try {
			solver.reset();
			solver.add(f1, ctx.mkNot(f2));
			return (solver.check(assumption) == Status.UNSATISFIABLE);
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	public Boolean entails(final List<BoolExpr> f1, final BoolExpr f2,
			final BoolExpr... assumption) {
		return entails(mkAnd(f1), f2, assumption);
	}

	public BoolExpr evaulate(final BoolExpr formula, final int index,
			final BoolExpr... assumption) {
		try {
			this.reset();
			addZ3ExpToContext(formula);
			check(assumption);
			return getZModel(index);
		} catch (final SolverException ex) {
		}
		return null;
	}

	public List<List<BoolExpr>> getCNF(final BoolExpr exp) {
		try {
			final Goal goal = applyTactic(TACTIC_CNF, exp)[0];
			final List<List<BoolExpr>> formula = new ArrayList<>();
			for (final BoolExpr clause : goal.getFormulas()) {
				formula.add(split(clause));
			}
			return formula;
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	public List<BoolExpr> getCNF2(final BoolExpr exp) {
		try {
			return Arrays.asList(applyTactic(TACTIC_CNF, exp)[0].getFormulas());
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	public List<String> getContextVarNames(final int index) {
		return zFuncDecl.keySet().stream().parallel()
				.filter(var -> var.endsWith(Integer.toString(index))).collect(Collectors.toList());
	}

	public BoolExpr getContextZFormula() {
		return mkAnd(zFormulas);
	}

	public List<BoolExpr> getContextZFormulas() {
		return zFormulas;
	}

	public List<List<BoolExpr>> getDNF(final BoolExpr exp) {
		final List<List<BoolExpr>> formula = new ArrayList<>();
		try {
			final Goal[] goals = applyTactic(TACTIC_DNF, exp);
			for (final Goal g : goals) {
				formula.add(Arrays.asList(g.getFormulas()));
			}
		} catch (final Z3Exception e) {
		}
		return formula;
	}

	public List<BoolExpr> getDNF2(final BoolExpr exp) {
		final List<BoolExpr> formula = new ArrayList<>();
		try {
			final Goal[] goals = applyTactic(TACTIC_DNF, exp);
			for (final Goal g : goals) {
				formula.add(mkAnd(g.getFormulas()));
			}
		} catch (final Z3Exception e) {
		}
		return formula;
	}

	public HashMap<String, String> getModel(final int index) throws SolverException {
		try {
			if (zSolverStatus.isSAT()) {
				final Model model = solver.getModel();
				final List<String> varNames = getContextVarNames(index);
				final HashMap<String, String> map = new HashMap<>(varNames.size());
				for (final Expr zVar : mkZExpr(varNames)) {
					map.put(zVar.toString(), model.eval(zVar, false).toString());
				}
				return map;
			}
		} catch (final Z3Exception ex) {
			throw new SolverException(SOLVER_NAME, ex);
		}
		return null;
	}

	@Override
	public int getNumScops() throws SolverException {
		try {
			return solver.getNumScopes();
		} catch (final Z3Exception ex) {
			throw new SolverException(SOLVER_NAME, ex);
		}
	}

	/**
	 * Computes the preimage of the given formula w.r.t. the given variable
	 * index. That is, QE(EXITS Y. F(X)) for some Y <= X where Y is the set of
	 * bounded variables and QE is the quantify elimination function. For
	 * example, for two integer variables x0 and x1 we have: <br/>
	 * getPreImage(x1=x0+1 & x1<2, 1) = {x0<1}.
	 *
	 * @param formula
	 *            The formula to perform the preimage
	 * @param boundindex
	 *            The variable index to be quantified
	 * @param assumption
	 *            Any assumption like range (domain) of the variable
	 * @return A set Im := {F1, F2, ..., Fn} of quantified free formulas.
	 *         Semantically Im equivalent to the formula F1 | F2 | ... | Fn.
	 * @throws SolverException
	 */
	public List<BoolExpr> getPreImage(final BoolExpr formula, final int boundindex,
			final BoolExpr... assumption) throws SolverException {
		return getPreImage(formula, getContextVarNames(boundindex), assumption);
	}

	public BoolExpr getPreImage2(final BoolExpr formula, final int boundindex,
			final BoolExpr... assumption) throws SolverException {
		return mkOr(getPreImage(formula, boundindex, assumption));
	}

	public List<BoolExpr> getUnsatCore() {
		solver.reset();
		params.add("unsat_core", true);
		solver.setParameters(params);
		zFormulas.forEach(e -> solver.add(e));
		zAsserts.forEach((k, v) -> solver.assertAndTrack(v, ctx.mkBoolConst(k)));
		final List<BoolExpr> cores = new ArrayList<>();
		if (solver.check() == Status.UNSATISFIABLE) {
			for (final Expr c : solver.getUnsatCore()) {
				cores.add(zAsserts.get(c.toString()));
			}
		}
		params.add("unsat_core", false);
		solver.setParameters(params);
		return cores;
	}

	public Context getZContext() {
		return ctx;
	}

	public BoolExpr getZDomain(final int varindex) {
		if (mZDomain.isEmpty()) {
			return mkTrue();
		}
		final List<BoolExpr> domains = mZDomain.stream().sequential()
				.map(d -> substitute(d, 0, varindex)).collect(Collectors.toList());
		return mkAnd(domains);
	}

	public BoolExpr getZDomain(final int varindex1, final int varindex2) {
		if (mZDomain.isEmpty()) {
			return mkTrue();
		}
		final int[] indices = new int[] { varindex1, varindex2 };
		final List<BoolExpr> domains = mZDomain.stream().sequential().flatMap(d -> {
			final List<BoolExpr> list = new ArrayList<>();
			for (final int index : indices) {
				list.add(substitute(d, 0, index));
			}
			return list.stream();
		}).collect(Collectors.toList());
		return mkAnd(domains);
	}

	public BoolExpr getZFormula(final String smtlib, final List<String> varNames)
			throws SolverException {
		return mkZFormula(smtlib, varNames);
	}

	public BoolExpr getZModel(final int index) throws SolverException {
		try {
			if (zSolverStatus.isSAT()) {
				final Model model = solver.getModel();
				final List<String> varNames = getContextVarNames(index);
				final List<BoolExpr> zmodel = new ArrayList<>(varNames.size());
				for (final Expr zVar : mkZExpr(varNames)) {
					zmodel.add(ctx.mkEq(zVar, model.eval(zVar, false)));
				}
				return mkAnd(zmodel.toArray(new BoolExpr[0]));
			}
		} catch (final Z3Exception ex) {
			throw new SolverException(SOLVER_NAME, ex);
		}
		return null;
	}

	public List<BoolExpr> getZModel(final List<Integer> indices) throws SolverException {
		try {
			if (zSolverStatus.isSAT()) {
				final Model model = solver.getModel();
				final List<BoolExpr> models = new ArrayList<>(indices.size());
				for (final int index : indices) {
					final List<String> varNames = getContextVarNames(index);
					final List<BoolExpr> zmodel = new ArrayList<>(varNames.size());
					for (final Expr zVar : mkZExpr(varNames)) {
						zmodel.add(ctx.mkEq(zVar, model.eval(zVar, false)));
					}
					models.add(mkAnd(zmodel.toArray(new BoolExpr[0])));
				}
				return models;
			}
		} catch (final Z3Exception ex) {
			throw new SolverException(SOLVER_NAME, ex);
		}
		return null;
	}

	public Boolean implies(final BoolExpr f1, final BoolExpr f2, final int firstindex,
			final int lastindex, final BoolExpr... assumption) {
		try {
			final List<String> bound = getContextVarNames(firstindex);
			bound.addAll(getContextVarNames(lastindex));
			final BoolExpr z = ctx.mkImplies(f1, f2);
			final BoolExpr exp = ctx.mkForall(mkZExpr(bound), z, 1, null, null, null, null);
			solver.reset();
			solver.add(exp);
			return solver.check(assumption) == Status.SATISFIABLE;
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	public Boolean isFalseSemantic(final BoolExpr exp, final BoolExpr... assumption) {
		try {
			solver.reset();
			solver.add(exp);
			return solver.check(assumption) == Status.UNSATISFIABLE;
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	public Boolean isFalseSyntactic(final BoolExpr exp) {
		try {
			return exp.simplify().isFalse();
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	public Boolean isSAT(final BoolExpr exp, final BoolExpr... assumption) {
		try {
			solver.reset();
			solver.add(exp);
			return solver.check(assumption) == Status.SATISFIABLE;
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	public Boolean isTrueSemantic(final BoolExpr exp, final BoolExpr... assumption) {
		try {
			return isFalseSemantic(ctx.mkNot(exp), assumption);
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	public Boolean isTrueSyntactic(final BoolExpr exp) {
		try {
			return exp.simplify().isTrue();
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	public Boolean isUNSAT(final BoolExpr exp, final BoolExpr... assumption) {
		try {
			solver.reset();
			solver.add(exp);
			return solver.check(assumption) == Status.UNSATISFIABLE;
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	public BoolExpr mkAnd(final BoolExpr exp1, final BoolExpr exp2) {
		try {
			return ctx.mkAnd(exp1, exp2);
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	public BoolExpr mkAnd(final BoolExpr[] exp) {
		try {
			if (exp.length == 0) {
				return ZTRUE;
			} else if (exp.length == 1) {
				return exp[0];
			} else {
				return ctx.mkAnd(exp);
			}
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	public BoolExpr mkAnd(final List<BoolExpr> exp) {
		if (exp != null) {
			return mkAnd(exp.stream().parallel().filter(e -> e != null).collect(Collectors.toList())
					.toArray(new BoolExpr[0]));
		}
		return null;
	}

	public BoolExpr mkFalse() {
		return ZFALSE;
	}

	public BoolExpr mkNot(final BoolExpr exp) {
		try {
			return ctx.mkNot(exp);
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	public BoolExpr mkOr(final BoolExpr exp1, final BoolExpr exp2) {
		try {
			return ctx.mkOr(exp1, exp2);
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	public BoolExpr mkOr(final BoolExpr[] exp) {
		try {
			if (exp.length == 0) {
				return ZFALSE;
			} else if (exp.length == 1) {
				return exp[0];
			} else {
				return ctx.mkOr(exp);
			}
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	public BoolExpr mkOr(final List<BoolExpr> exp) {
		if (exp != null) {
			return mkOr(exp.stream().parallel().filter(e -> e != null).collect(Collectors.toList())
					.toArray(new BoolExpr[0]));
		}
		return null;
	}

	public BoolExpr mkTrue() {
		return ZTRUE;
	}

	public BoolExpr mkZ3Expression(final int[] indexSet, final String str) {
		try {
			reset();
			addStrExpToContext(indexSet, str);
			return (BoolExpr) getContextZFormula().simplify();
		} catch (SolverException | Z3Exception ex) {
		}
		return null;
	}

	@Override
	public void pop() throws SolverException {
		try {
			solver.pop();
		} catch (final Z3Exception ex) {
			throw new SolverException(SOLVER_NAME, ex);
		}
	}

	@Override
	public void pop(final int i) throws SolverException {
		try {
			solver.pop(i);
		} catch (final Z3Exception ex) {
			throw new SolverException(SOLVER_NAME, ex);
		}
	}

	@Override
	public void push() throws SolverException {
		try {
			solver.push();
		} catch (final Z3Exception ex) {
			throw new SolverException(SOLVER_NAME, ex);
		}
	}

	/**
	 * Resets and clears all the stored information (cold reset, see
	 * {@link #reset(boolean) reset()}).
	 */
	@Override
	public void reset() {
		try {
			solver.reset();
			zSolverStatus = SolverStatus.UNKNOWN;
			clear();
		} catch (final Z3Exception ex) {
		}
	}

	public void setVarTypeReal() {
		mVarTypeReal = true;
	}

	public void setVarTypeInt() {
		mVarTypeReal = false;
	}

	public BoolExpr simplify(final BoolExpr exp) {
		try {
			return mkAnd(applyTactic(TACTIC_SIMPLIFY, exp)[0].getFormulas());
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	public List<BoolExpr> simplify(final List<BoolExpr> exps) {
		try {
			final BoolExpr sim = mkAnd(applyTactic(TACTIC_SIMPLIFY, mkAnd(exps))[0].getFormulas());
			return getDNF2(sim);
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	public BoolExpr simplifyExpr(final BoolExpr exp) {
		try {
			return (BoolExpr) exp.simplify();
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	public List<BoolExpr> split(final BoolExpr exp) {
		try {
			final List<BoolExpr> clauses = new ArrayList<>();
			for (final Goal g : applyTactic(TACTIC_SPLIT, exp)) {
				clauses.add(mkAnd(g.getFormulas()));
			}
			return clauses;
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	public BoolExpr substitute(final BoolExpr exp, final int oldIndex, final int newIndex) {
		try {
			if (exp != null && oldIndex >= 0 && newIndex >= -1) {
				if (oldIndex == newIndex || isTrueSyntactic(exp) || isFalseSyntactic(exp)) {
					return exp;
				}
				final List<String> oldVarNames = getContextVarNames(oldIndex);
				final Expr[] oldvars = new Expr[oldVarNames.size()];
				final Expr[] newvars = new Expr[oldVarNames.size()];
				int i = 0;
				for (final String oldName : oldVarNames) {
					final Expr oldExp = mVarTypeReal ? mkZRealExpr(oldName) : mkZIntExpr(oldName);
					final String oldIndexStr = Integer.toString(oldIndex);
					final String newName = oldName.substring(0,
							oldName.length() - oldIndexStr.length())
							+ (newIndex > -1 ? Integer.toString(newIndex) : "");
					mkZFuncDecl(newName);
					oldvars[i] = oldExp;
					newvars[i] = mVarTypeReal ? mkZRealExpr(newName) : mkZIntExpr(newName);
					i++;
				}
				return (BoolExpr) exp.substitute(oldvars, newvars);
			}
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	private Goal[] applyTactic(final Tactic t, final BoolExpr exp) throws Z3Exception {
		final Goal goal = ctx.mkGoal(true, false, false);
		goal.add(exp);
		final ApplyResult result = t.apply(goal);
		return result.getSubgoals();
	}

	private List<BoolExpr> getPreImage(final BoolExpr formula, final List<String> bounds,
			final BoolExpr... assumption) throws SolverException {
		try {
			final List<BoolExpr> images = new ArrayList<BoolExpr>();
			if (isFalseSemantic(formula, assumption)) {
				images.add(ZFALSE);
			} else if (isTrueSemantic(formula, assumption)) {
				images.add(ZTRUE);
			} else {
				final List<BoolExpr> dnf = getDNF2(formula);
				for (final BoolExpr c : dnf) {
					final BoolExpr exp = ctx.mkExists(mkZExpr(bounds), c, 1, null, null, null,
							null);
					final Goal goal = applyTactic(TACTIC_QE, exp)[0];
					if (goal.isDecidedUnsat()) {
						continue;
					}
					images.add(mkAnd(goal.getFormulas()));
				}
			}
			return images;
		} catch (final Z3Exception ex) {
			throw new SolverException(SOLVER_NAME, ex);
		}
	}

	private SolverStatus getStatus(final Status status) {
		switch (status) {
			case SATISFIABLE:
				return SolverStatus.SATISFIABLE;
			case UNSATISFIABLE:
				return SolverStatus.UNSATISFIABLE;
		}
		return SolverStatus.UNKNOWN;
	}

	private void mkZDomain() throws SolverException {
		if (mVariableContext == null) {
			return;
		}
		try {
			for (final SimpleEFAVariable var : mVariableContext) {
				final CompiledRange range = var.getRange();
				if (range instanceof CompiledIntRange) {
					final CompiledIntRange dom = (CompiledIntRange) range;
					final String varName = var.getName() + Integer.toString(0);
					mkZFuncDecl(varName);
					BoolExpr zDom = null;
					if(mVarTypeReal){
						final IntExpr zExpr = mkZIntExpr(varName);
						zDom = ctx.mkAnd(ctx.mkGe(zExpr, ctx.mkInt(dom.getLower())),
								ctx.mkLe(zExpr, ctx.mkInt(dom.getUpper())));
					} else {
						final RealExpr zExpr = mkZRealExpr(varName);
						zDom = ctx.mkAnd(ctx.mkGe(zExpr, ctx.mkInt(dom.getLower())),
								ctx.mkLe(zExpr, ctx.mkInt(dom.getUpper())));
					}
					mZDomain.add(zDom);
				}
			}
		} catch (final Z3Exception ex) {
			throw new SolverException(SOLVER_NAME, ex);
		}
	}

	private BoolExpr mkZFormula(final int firstIndex, final boolean incremental,
			final List<SimpleExpressionProxy> exps) throws SolverException {
		if (exps != null && !exps.isEmpty()) {
			return mkZFormula(zSMTLIB2.getSMTLIB2String(firstIndex, incremental, exps),
					zSMTLIB2.getVarNames());
		}
		return null;
	}

	private BoolExpr mkZFormula(final int firstIndex, final boolean incremental,
			final String... strings) throws SolverException {
		if (strings != null && strings.length > 0) {
			return mkZFormula(zSMTLIB2.getSMTLIB2String(firstIndex, incremental, strings),
					zSMTLIB2.getVarNames());
		}
		return null;
	}

	private BoolExpr mkZFormula(final int[] indexSet, final List<List<SimpleExpressionProxy>> exps)
			throws SolverException {
		if (exps != null && !exps.isEmpty()) {
			return mkZFormula(zSMTLIB2.getSMTLIB2String(indexSet, exps), zSMTLIB2.getVarNames());
		}
		return null;
	}

	private BoolExpr mkZFormula(final int[] indexSet, final String[] str) throws SolverException {
		if (str != null && str.length > 0) {
			return mkZFormula(zSMTLIB2.getSMTLIB2String(indexSet, str), zSMTLIB2.getVarNames());
		}
		return null;
	}

	private BoolExpr mkZFormula(final String smtlib, final List<String> varNames)
			throws SolverException {
		try {
			final int size = varNames.size();
			final FuncDecl[] fDecls = new FuncDecl[size];
			final Symbol[] sDecls = new Symbol[size];
			for (int j = 0; j < varNames.size(); j++) {
				final FuncDecl fn = mkZFuncDecl(varNames.get(j));
				fDecls[j] = fn;
				sDecls[j] = fn.getName();
			}
			return ctx.parseSMTLIB2String(smtlib, null, null, sDecls, fDecls);
		} catch (final Z3Exception ex) {
			throw new SolverException(SOLVER_NAME, ex);
		}
	}

	private FuncDecl mkZFuncDecl(final String varName) {
		try {
			return zFuncDecl.computeIfAbsent(varName,
					k -> mVarTypeReal ? ctx.mkConstDecl(varName, ctx.getRealSort()) :
						ctx.mkConstDecl(varName, ctx.getIntSort()));
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	private Expr[] mkZExpr(final List<String> varNames) {
		try {
			return varNames.stream()
					.map(name -> mVarTypeReal ? mkZRealExpr(name) : mkZIntExpr(name))
					.collect(Collectors.toList())
					.toArray(new Expr[0]);
		} catch (final NullPointerException ex) {
		}
		return null;
	}

	private IntExpr mkZIntExpr(final String varName) {
		try {
			return ctx.mkIntConst(varName);
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	private RealExpr mkZRealExpr(final String varName) {
		try {
			return ctx.mkRealConst(varName);
		} catch (final Z3Exception ex) {
		}
		return null;
	}

	private static final String SOLVER_NAME = "Z3";
	private final Context ctx;
	private final List<SimpleEFAVariable> mVariableContext;
	private boolean mVarTypeReal;
	private final ArrayList<BoolExpr> mZDomain;
	private final Params params;
	private final Solver solver;
	private final Tactic TACTIC_CNF; // Conjunction Normal Form Tactics
	private final Tactic TACTIC_DNF; // Disjunction Normal Form Tactics
	private final Tactic TACTIC_QE; // Quantification Elimination Tactics
	private final Tactic TACTIC_SIMPLIFY; // Simplification Tactics
	private final Tactic TACTIC_SPLIT; // Splitting Tactics
	private final String UNSATCORE_STRING = "P";
	private final HashMap<String, BoolExpr> zAsserts;
	private BoolExpr ZFALSE;
	private final List<BoolExpr> zFormulas;
	private final HashMap<String, FuncDecl> zFuncDecl;
	private final SMTLIB2Translator zSMTLIB2 = SMTLIB2Translator.getInstatnce();
	private SolverStatus zSolverStatus;
	private BoolExpr ZTRUE;
}
