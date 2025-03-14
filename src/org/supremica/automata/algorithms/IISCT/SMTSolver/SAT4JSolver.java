//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT:
//# PACKAGE: org.supremica.automata.algorithms.LBSC
//# CLASS:   SAT4JSolver
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.automata.algorithms.IISCT.SMTSolver;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.efa.simple.SimpleEFAHelper;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariable;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariableContext;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledEnumRange;
import net.sourceforge.waters.model.compiler.context.CompiledIntRange;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.dnf.CompiledClause;
import net.sourceforge.waters.model.compiler.dnf.CompiledNormalForm;
import net.sourceforge.waters.model.compiler.dnf.DNFConverter;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.plain.module.BinaryExpressionElement;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.plain.module.UnaryExpressionElement;

import org.sat4j.core.Vec;
import org.sat4j.csp.Constant;
import org.sat4j.csp.Domain;
import org.sat4j.csp.EnumeratedDomain;
import org.sat4j.csp.Evaluable;
import org.sat4j.csp.Predicate;
import org.sat4j.csp.RangeDomain;
import org.sat4j.csp.SolverFactory;
import org.sat4j.csp.Var;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;

/**
 * <P>A Constraint Satisfaction Problem (CSP) solver based on SAT4J CSP
 * package.</P>
 *
 * <P>Given a set of constraints over variables, SAT4JSolver attempts
 * to find all variables values that satisfies the given constraints. Note
 * that SAT4J does not contain a real CSP solver, it translates given CSP
 * problems into SAT problems to solve them. Hence, it might get hot on
 * variables with large domain (&gt;5000 elements). Despite this fact, it
 * is very efficient on solving problems with constraints over Boolean
 * variables and logical connectivities (guess why?). The solver is equipped
 * with methods to work with sequence of conditional transitions where a
 * sequence of updates is modelled by automatic renaming of variables where
 * next-variables (primed variables) of the i-th condition in the sequence
 * serve the current variables (unprimed variables) of (i+1)-th condition,
 * and so on.</p>
 *
 * <P>A sample code to use this solver is as follows.</P>
 * <PRE>
 * {@link SAT4JSolver} solver = new {@link #SAT4JSolver(SimpleEFAVariableContext) SAT4JSolver}(net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariableContext) (varcontext)};
 * solver.{@link #init(java.util.List) init}(constraints);
 * solver.{@link #solve()};
 * while (solver.{@link #isSatisfiable()}){
 *    model = solver.{@link #getModel()};
 *    // Do something with the model
 * }
 * </PRE>
 *
 * @author Mohammad Reza Shoaei
 */
public class SAT4JSolver
{
  /**
   * Creates a CSP solver.
   * @param mode       Solver mode 0: default 1: light
   * @param timeout    Timeout for the solver in second
   * @param varContext Variables context
   */
  public SAT4JSolver(final int mode,
                     final int timeout,
                   final SimpleEFAVariableContext varContext)
  {
    mSolver = getSolver(mode);
    mSolver.setTimeout(timeout);
    mFactory = ModuleElementFactory.getInstance();
    mOp = CompilerOperatorTable.getInstance();
    mCloner = mFactory.getCloner();
    mHelper = new SimpleEFAHelper(mFactory, mOp);
    mDNF = new DNFConverter(mFactory, mOp);
    mSimVarNameToRange = new THashMap<>();
    for (final SimpleEFAVariable var : varContext.getVariables()) {
      mSimVarNameToRange.put(var.getName(), var.getRange());
    }
    mJVarNameToDomain = new THashMap<>();
    mJVarNameToSimVar = new THashMap<>();
    mJVarNameToJVar = new THashMap<>();
    mJPredicatesList = new ArrayList<>();
    mRangeToDomain = new THashMap<>();
    mIntToJEval = new TIntObjectHashMap<>();
    mLearnedClauses = new ArrayList<>();
    mBuffer = new ArrayList<>();
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    mExpIntMap = new ProxyAccessorHashMap<>(eq);
    mNbJVars = 0;
    mVarPostfix = 0;
    mTrivialFalsity = false;
  }
  /**
   * Creates a CSP solver.
   * @param varContext Variables context
   */
  public SAT4JSolver(final SimpleEFAVariableContext varContext)
  {
    this(SOLVER_LIGHT, DEFUALT_TIMEOUT, varContext);
  }

  /**
   * Clears and initialises the solver using the given constraints.
   */
  public void init(final String exp)
   throws AnalysisException
  {
    reset();
    addExpression(exp);
  }

  /**
   * Clears and initialises the solver using the given constraint.
   * @param exps List of constraints viewed as conjunction of constraints
   */
  public void init(final List<SimpleExpressionProxy> exps)
   throws AnalysisException
  {
    reset();
    addExpression(exps);
  }

  /**
   * Add the given constraint to the list of constraints
   */
  public void addExpression(final String exp)
   throws AnalysisException
  {
    addExpression(mHelper.parse(exp));
  }

  /**
   * Add negated of the given constraint to the list of constraints
   */
  public void addNegatedExpression(final String str)
   throws AnalysisException
  {
    addNegatedExpression(mHelper.parse(str));
  }

  /**
   * Add negated of the given constraint to the list of constraints
   */
  public void addNegatedExpression(final List<SimpleExpressionProxy> exps)
   throws AnalysisException
  {
    for (final SimpleExpressionProxy exp : exps) {
      addNegatedExpression(exp);
    }
  }

  /**
   * Add the given constraint to the list of constraints
   */
  public void addExpression(final List<SimpleExpressionProxy> exps)
   throws AnalysisException
  {
    for (final SimpleExpressionProxy exp : exps) {
      addExpression(exp);
    }
  }

  /**
   * Increase the variables post fix by one. From now on, the primed variables, say x', of the
   * stored constraints will be considered as the unprimed variables of any constraint which will be
   * added later.
   */
  public void step()
  {
    mVarPostfix++;
  }

  /**
   * Step forward to the given value.
   */
  public void stepTo(final int step)
  {
    mVarPostfix = step;
  }

  /**
   * Resets and clears all the stored information (cold reset).
   */
  public void reset()
  {
    reset(false);
  }

  /**
   * Resets and clears all the stored information.
   * @param warm Clears only constrains, learned clauses and sets postfix value to 0.
   */
  public void reset(final boolean warm)
  {
    mSolver.reset();
    mJPredicatesList.clear();
    mVarPostfix = 0;
    mTrivialFalsity = false;
    mLearnedClauses.clear();
    mBuffer.clear();
    if (!warm) {
      mExpIntMap.clear();
      mJVarNameToDomain.clear();
      mJVarNameToSimVar.clear();
      mJVarNameToJVar.clear();
      mIntToJEval.clear();
      mNbJVars = 0;
      mRangeToDomain.clear();
    }
  }

  /**
   * Stores the information (except learned clauses) in the buffer at given index. The stored
   * information can be retrieved at any time by calling {@link #resume(int)}
   * and the index.
   */
  public void pause(final int index) throws AnalysisException
  {
    if (index < 0) {
      throw new AnalysisException("CSPSolver > pause > Index out of bounds!");
    }
    mBuffer.set(index, new Object[]{new THashMap<>(mJVarNameToDomain),
                                    new THashMap<>(mJVarNameToSimVar),
                                    new THashMap<>(mJVarNameToJVar),
                                    new ArrayList<>(mJPredicatesList),
                                    new TIntObjectHashMap<>(mIntToJEval),
                                    mNbJVars,
                                    mVarPostfix,
                                    mTrivialFalsity});
  }

  /**
   * Restoring all the information stored by {@link #pause(int)} at the given
   * index.
   * @param index index of the information in the buffer.
   * @throws net.sourceforge.waters.model.analysis.AnalysisException index out of bounds error
   */
  @SuppressWarnings("unchecked")
  public void resume(final int index) throws AnalysisException
  {
    if (index < 0 || index > mBuffer.size()) {
      throw new AnalysisException("CSPSolver > resume > Index out of bounds!");
    }
    final Object[] info = mBuffer.get(index);
    mJVarNameToDomain = (THashMap<String, Domain>) info[0];
    mJVarNameToSimVar = (THashMap<String, String>) info[1];
    mJVarNameToJVar = (THashMap<String, Var>) info[2];
    mJPredicatesList = (ArrayList<Object[]>) info[3];
    mIntToJEval = (TIntObjectHashMap<Evaluable>) info[4];
    mNbJVars = (int) info[5];
    mVarPostfix = (int) info[6];
    mTrivialFalsity = (boolean) info[7];
    mSolver.reset();
  }

  /**
   * Solves, learns, and stores values of unprimed variables of the initial step (marked by postfix
   * 0) that satisfied all constraints. Note that the {@link #solve()} method is automatically
   * called by the method.
   * @param negate If the negated of the values must be stored.
   * @return Returns <CODE>true</CODE> if the learning process was successful. Otherwise,
   *         <CODE>false</CODE>.
   */
  public boolean learn(final boolean negate) throws AnalysisException
  {
    return learn(negate, 0);
  }

  /**
   * Solves, learns, and stores values of unprimed variables of the particular step that satisfied
   * all constraints. Note that the {@link #solve()} method is automatically called by the method.
   * @param negate  If the negated of the values must be stored.
   * @param postfix The step, initially it is 0.
   * @return Returns true if the learning process was successful.
   */
  public boolean learn(final boolean negate, final int postfix)
   throws AnalysisException
  {
    boolean success = false;
    final String fn = negate ? _NOTEQUALS : _EQUALS;
    final Set<String> pExpVars = new THashSet<>();
    final TIntArrayList pExpCons = new TIntArrayList();
    solve();
    while (isSatisfiable()) {
      final THashMap<String, SimpleExpressionProxy> translation = getInnerModel();
      if (translation.isEmpty()) {
        throw new AnalysisException("Learning > I got an empty model!");
      }
      final ArrayList<String> pairs = new ArrayList<>();
      for (final Map.Entry<String, SimpleExpressionProxy> entry : translation.entrySet()) {
        final String key = entry.getKey();
        final SimpleExpressionProxy value = entry.getValue();
        if (key.endsWith(Integer.toString(postfix))) {
          pairs.add(fn + SimpleEFAHelper.DEFAULT_OPENING_STRING + key
           + SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR + value
           + SimpleEFAHelper.DEFAULT_CLOSING_STRING);
          pExpVars.add(key);
          pExpCons.add(getExpressionId(value));
        }
      }

      if (pairs.isEmpty()) {
        continue;
      }

      String pExpStr = "";
      for (int j = pairs.size(); j > 0; j--) {
        if (j > 1) {
          pExpStr += _AND + SimpleEFAHelper.DEFAULT_OPENING_STRING;
        }
        pExpStr += pairs.get(j - 1);
        pExpStr += SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR;
      }
      pExpStr = pExpStr.substring(0, pExpStr.length() - 1);
      for (int j = 1; j < pairs.size(); j++) {
        pExpStr += SimpleEFAHelper.DEFAULT_CLOSING_STRING;
      }
      mLearnedClauses.add(new Object[]{pExpStr, pExpVars, pExpCons.toArray()});
      success = true;
    }
    return success;
  }

  /**
   * Returns whether there exists any solution for the current constraints. Note that
   * {@link #solve()} must be called before hand.
   * @return <CODE>true</CODE> is there is any solution, <CODE>false</CODE> otherwise.
   */
  public boolean isSatisfiable()
   throws AnalysisException
  {
    try {
      if (!mTrivialFalsity) {
        return mSolver.isSatisfiable();
      }
    } catch (final TimeoutException ex) {
      throw new AnalysisException("Timeout Exception!");
    }
    return false;
  }

  /**
   * Returns a model that satisfies the current constraints. Note that {@link #isSatisfiable()} must
   * be called before hand.
   * @return A map of variables to the values.
   */
  public Map<String, SimpleExpressionProxy> getModel()
  {
    return getModel(0);
  }

  /**
   * Returns a model that satisfies the current constraints. Note that {@link #isSatisfiable()} must
   * be called before hand.
   * @param postfix The variables value at specific step. To get next value of step i use i+1.
   * @return A map of variables to the values.
   */
  public Map<String, SimpleExpressionProxy> getModel(final int postfix)
  {
    final THashMap<String, SimpleExpressionProxy> sVarToValue = new THashMap<>();
    final THashMap<String, SimpleExpressionProxy> translation = getInnerModel();
    for (final Map.Entry<String, SimpleExpressionProxy> entry : translation.entrySet()) {
      if (entry.getKey().endsWith(Integer.toString(postfix))) {
        sVarToValue.put(mJVarNameToSimVar.get(entry.getKey()), entry.getValue());
      }
    }
    return sVarToValue;
  }

  /**
   * Returns the inner model, namely, variables with postfix. Note that {@link #isSatisfiable()}
   * must be called before hand.
   * @return A map of inner variables to the values.
   */
  public THashMap<String, SimpleExpressionProxy> getInnerModel()
  {
    final int[] model = mSolver.model();
    return translate(model);
  }

  /**
   * Attempts to solve the CSP. Must be called before retrieving any model or checking the
   * satisfiability of the problem.
   */
  @SuppressWarnings("unchecked")
  public void solve() throws AnalysisException
  {
    try {
      mSolver.reset();
      final List<Var> allVars = new ArrayList<>();
      final ArrayList<Object[]> predicates = new ArrayList<>(mJPredicatesList);
      predicates.addAll(mLearnedClauses);
      for (final Object[] obj : predicates) {
        final Predicate P = new Predicate();
        final String pStr = (String) obj[0];
        P.setExpression(pStr);
        final Set<String> pVars = (Set<String>) obj[1];
        final IVec<Evaluable> vars = new Vec<>();
        final IVec<Var> scope = new Vec<>();
        for (final String cVarName : pVars) {
          final String sVarName = mJVarNameToSimVar.get(cVarName);
          final CompiledRange sRange = mSimVarNameToRange.get(sVarName);
          final Domain cDomain = getCSPDomain(cVarName, sRange);
          final Var cVar = createCSPVar(cVarName, cDomain);
          vars.push(cVar);
          scope.push(cVar);
          allVars.add(cVar);
          P.addVariable(cVarName);
        }
        final int[] pConstants = (int[]) obj[2];
        setupCSPIntVar(vars, pConstants);
        P.toClause(mSolver, scope, vars);
      }
      mSolver.newVar(mNbJVars);
      for (final Var var : allVars) {
        var.toClause(mSolver);
      }
    } catch (final ContradictionException ex) {
      mTrivialFalsity = true;
    }
  }

  private void addNegatedExpression(final SimpleExpressionProxy exp)
   throws AnalysisException
  {
    final Object[] p = getCSPPredicate(exp);
    if (p != null) {
      final String pExpStr = (String) p[0];
      p[0] = _NOT + SimpleEFAHelper.DEFAULT_OPENING_STRING + pExpStr
       + SimpleEFAHelper.DEFAULT_CLOSING_STRING;
      mJPredicatesList.add(p);
    }
  }

  private void addExpression(final SimpleExpressionProxy exp)
   throws AnalysisException
  {
    final Object[] p = getCSPPredicate(exp);
    if (p != null) {
      mJPredicatesList.add(p);
    }
  }

  private Var createCSPVar(final String name, final Domain domain)
  {
    Var var = mJVarNameToJVar.get(name);
    if (var == null) {
      var = new Var(name, domain, mNbJVars);
      mNbJVars += domain.size();
      mJVarNameToJVar.put(name, var);
    }
    return var;
  }

  private void setupCSPIntVar(final IVec<Evaluable> vars, final int[] constants)
  {
    for (final int d : constants) {
      Evaluable var = mIntToJEval.get(d);
      if (var == null) {
        var = new Constant(d);
        mIntToJEval.put(d, var);
      }
      vars.push(var);
    }
  }

  private Domain getCSPDomain(final String name, final CompiledRange range)
   throws AnalysisException
  {
    Domain domain = mJVarNameToDomain.get(name);
    if (domain == null) {
      domain = mRangeToDomain.get(range);
      if (domain == null) {
        if (range instanceof CompiledIntRange) {
          final CompiledIntRange dom = (CompiledIntRange) range;
          domain = new RangeDomain(dom.getLower(), dom.getUpper());
        } else if (range instanceof CompiledEnumRange) {
          final TIntArrayList elements = new TIntArrayList();
          for (final SimpleExpressionProxy exp : range.getValues()) {
            elements.add(getExpressionId(exp));
          }
          domain = new EnumeratedDomain(elements.toArray());
        } else {
          throw new AnalysisException("Unknown domain range!");
        }
        mRangeToDomain.put(range, domain);
      }
      mJVarNameToDomain.put(name, domain);
    }
    return domain;
  }

  private int getExpressionId(final SimpleExpressionProxy exp)
  {
    Integer value = mExpIntMap.getByProxy(exp);
    if (value == null) {
      final int id = mExpIntMap.size();
      mExpIntMap.putByProxy(exp, id);
      value = id;
    }
    return value;
  }

  private Object[] getCSPPredicate(final SimpleExpressionProxy exp)
   throws AnalysisException
  {
    try {
      final CompiledNormalForm normal = mDNF.convertToCNF(exp);
      final List<CompiledClause> clauses = new ArrayList<>(normal.getClauses());
      if (clauses.isEmpty()) {
        return null;
      }
      final ExpressionVisitor visitor = new ExpressionVisitor();
      final String pExpStr = getCSPExpressionString(clauses, visitor);
      if (visitor.hasUnsupportedElement()) {
        throw new AnalysisException(pExpStr);
      }
      final Set<String> pExpVars = visitor.getExpressionVars();
      final int[] pExpCons = visitor.getConstants().toArray();
      return new Object[]{pExpStr, pExpVars, pExpCons};
    } catch (final EvalException ex) {
      throw new AnalysisException("Eval Exception: " + ex);
    } catch (final VisitorException ex) {
      throw new AnalysisException("Visitor Exception: " + ex);
    }
  }

  private String getCSPExpressionString(final List<CompiledClause> clauses,
                                        final ExpressionVisitor visitor)
   throws VisitorException
  {
    String pAndExp = "";
    for (int i = clauses.size(); i > 0; i--) {
      if (i > 1) {
        pAndExp += _AND + SimpleEFAHelper.DEFAULT_OPENING_STRING;
      }
      String pOrExp = "";
      final CompiledClause clause = clauses.get(i - 1);
      final List<SimpleExpressionProxy> literals = new ArrayList<>(clause.getLiterals());
      for (int j = literals.size(); j > 0; j--) {
        final SimpleExpressionProxy literal = literals.get(j - 1);
        if (j > 1) {
          pOrExp += _OR + SimpleEFAHelper.DEFAULT_CLOSING_STRING;
        }
        visitor.clear();
        literal.acceptVisitor(visitor);
        pOrExp += visitor.getPredicate();
        pOrExp += SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR;
      }
      pOrExp = pOrExp.substring(0, pOrExp.length() - 1);
      for (int j = 1; j < literals.size(); j++) {
        pOrExp += SimpleEFAHelper.DEFAULT_CLOSING_STRING;
      }
      pAndExp += pOrExp + SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR;
    }
    pAndExp = pAndExp.substring(0, pAndExp.length() - 1);
    for (int i = 1; i < clauses.size(); i++) {
      pAndExp += SimpleEFAHelper.DEFAULT_CLOSING_STRING;
    }
    return pAndExp;
  }
  private static ModelIterator getSolver(final int mode)
  {
    final ModelIterator solver;
    switch (mode) {
      case 0:
        solver = new ModelIterator(SolverFactory.newDefault());
        break;
      case 1:
        solver = new ModelIterator(SolverFactory.newLight());
        break;
      default:
        solver = new ModelIterator(SolverFactory.newDefault());
        break;
    }
    return solver;
  }

  private THashMap<String, SimpleExpressionProxy> translate(final int[] model)
  {
    final THashMap<String, SimpleExpressionProxy> cVarToValue = new THashMap<>();
    for (final String cVarName : mJVarNameToJVar.keySet()) {
      final Var cVar = mJVarNameToJVar.get(cVarName);
      final int value = cVar.findValue(model);
      final Domain domain = mJVarNameToDomain.get(cVarName);
      if (domain instanceof RangeDomain) {
        cVarToValue.put(cVarName, mFactory.createIntConstantProxy(value));
      } else {
        for (final ProxyAccessor<SimpleExpressionProxy> proxy : mExpIntMap.keySet()) {
          final SimpleExpressionProxy exp = proxy.getProxy();
          if (value == mExpIntMap.getByProxy(exp)) {
            cVarToValue.put(cVarName, exp);
            break;
          }
        }
      }
    }
    return cVarToValue;
  }

  class ExpressionVisitor extends DefaultModuleProxyVisitor
  {

    ExpressionVisitor()
    {
      mExpVars = new THashSet<>();
      mConstants = new TIntHashSet();
      clear();
    }

    //#########################################################################
    @Override
    public Object visitBinaryExpressionProxy(final BinaryExpressionProxy proxy)
     throws VisitorException
    {
      if (mUnsupportedFlag) {
        return null;
      }
      final BinaryExpressionElement exp = (BinaryExpressionElement) mCloner.getClone(proxy);
      final BinaryOperator op = exp.getOperator();
      if (op == mOp.getPlusOperator()) {
        mPredicate += _PLUS;
      } else if (op == mOp.getMinusOperator()) {
        mPredicate += _MINUS;
      } else if (op == mOp.getModuloOperator()) {
        mPredicate += _MODULO;
      } else if (op == mOp.getTimesOperator()) {
        mPredicate += _TIMES;
      } else if (op == mOp.getDivideOperator()) {
        mPredicate += _DIVIDE;
      } else if (op == mOp.getEqualsOperator()) {
        mPredicate += _EQUALS;
      } else if (op == mOp.getNotEqualsOperator()) {
        mPredicate += _NOTEQUALS;
      } else if (op == mOp.getLessThanOperator()) {
        mPredicate += _LESS_THAN;
      } else if (op == mOp.getLessEqualsOperator()) {
        mPredicate += _LESS_EQUALS;
      } else if (op == mOp.getGreaterThanOperator()) {
        mPredicate += _GREATER_THAN;
      } else if (op == mOp.getGreaterEqualsOperator()) {
        mPredicate += _GREATER_EQUALS;
      } else {
        mPredicate = "Unsupported operator: " + op.getName();
        mUnsupportedFlag = true;
        return null;
      }
      mPredicate += SimpleEFAHelper.DEFAULT_OPENING_STRING;
      exp.getLeft().acceptVisitor(this);
      mPredicate += SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR;
      exp.getRight().acceptVisitor(this);
      mPredicate += SimpleEFAHelper.DEFAULT_CLOSING_STRING;
      return null;
    }

    @Override
    public Object visitIntConstantProxy(final IntConstantProxy proxy)
     throws VisitorException
    {
      if (!mUnsupportedFlag) {
        mPredicate += proxy.getValue();
        mConstants.add(proxy.getValue());
      }
      return null;
    }

    @Override
    public Object visitSimpleIdentifierProxy(final SimpleIdentifierProxy proxy)
     throws VisitorException
    {
      if (!mUnsupportedFlag) {
        final String sName = proxy.getName();
        final String cName = sName + mVarPostfix;
        mPredicate += cName;
        mExpVars.add(cName);
        mJVarNameToSimVar.put(cName, sName);
      }
      return null;
    }

    @Override
    public Object visitUnaryExpressionProxy(final UnaryExpressionProxy proxy)
     throws VisitorException
    {
      if (!mUnsupportedFlag) {
        final UnaryExpressionElement exp = (UnaryExpressionElement) mCloner.getClone(proxy);
        final UnaryOperator op = exp.getOperator();
        if (op == mOp.getNotOperator()) {
          mPredicate += _NOT + SimpleEFAHelper.DEFAULT_OPENING_STRING;
          exp.getSubTerm().acceptVisitor(this);
          mPredicate += SimpleEFAHelper.DEFAULT_CLOSING_STRING;
        } else if (op == mOp.getNextOperator()) {
          final String sName = exp.getSubTerm().toString();
          final String cName = sName + (mVarPostfix + 1);
          mPredicate += cName;
          mExpVars.add(cName);
          mJVarNameToSimVar.put(cName, sName);
        } else {
          mPredicate = "Unsupported operator: " + op.getName();
          mUnsupportedFlag = true;
        }
      }
      return null;
    }

    String getPredicate()
    {
      return mPredicate;
    }

    boolean hasUnsupportedElement()
    {
      return mUnsupportedFlag;
    }

    void clear()
    {
      mPredicate = "";
      mUnsupportedFlag = false;
    }

    THashSet<String> getExpressionVars()
    {
      return mExpVars;
    }

    TIntHashSet getConstants()
    {
      return mConstants;
    }

    private String mPredicate;
    private boolean mUnsupportedFlag;
    private final THashSet<String> mExpVars;
    private final TIntHashSet mConstants;

  }

  //#########################################################################
  public static final int SOLVER_LIGHT = 1;
  public static final int SOLVER_DEFUALT = 0;
  public static final int DEFUALT_TIMEOUT = 300;

  private static final String _EQUALS = "eq";
  private static final String _NOTEQUALS = "ne";
  private static final String _GREATER_THAN = "gt";
  private static final String _GREATER_EQUALS = "ge";
  private static final String _LESS_THAN = "lt";
  private static final String _LESS_EQUALS = "le";
  private static final String _MINUS = "sub";
  private static final String _PLUS = "add";
  private static final String _TIMES = "mul";
  private static final String _DIVIDE = "div";
  private static final String _MODULO = "mod";
  private static final String _AND = "and";
  private static final String _OR = "or";
  private static final String _NOT = "not";

  private final ModelIterator mSolver;
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOp;
  private final ProxyAccessorHashMap<SimpleExpressionProxy, Integer> mExpIntMap;
  private final THashMap<String, CompiledRange> mSimVarNameToRange;
  private final THashMap<CompiledRange, Domain> mRangeToDomain;
  private final ArrayList<Object[]> mLearnedClauses;
  private final SimpleEFAHelper mHelper;
  private final ModuleProxyCloner mCloner;
  private final DNFConverter mDNF;
  private THashMap<String, Domain> mJVarNameToDomain;
  private THashMap<String, String> mJVarNameToSimVar;
  private THashMap<String, Var> mJVarNameToJVar;
  private TIntObjectHashMap<Evaluable> mIntToJEval;
  private ArrayList<Object[]> mJPredicatesList;
  private boolean mTrivialFalsity;
  private final ArrayList<Object[]> mBuffer;
  private int mNbJVars;
  private int mVarPostfix;
}
