//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: 
//# PACKAGE: org.supremica.automata.algorithms.LBSC
//# CLASS:   CSPSolver
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.automata.algorithms.LBSC;

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

/**
 * A Constraint Satisfaction Problem (CSP) solver based on SAT4J CSP package. Given a set of
 * constraints over variables, CSPSolver attempts to find all variables values that satisfies the
 * given constraints. Note that SAT4J does not contain a real CSP solver, it translates given CSP
 * problems into SAT problems to solve them. Hence, it might get hot on variables with large domain
 * (>5000 elements). Despite this fact, it is very efficient on solving problems with constraints
 * over boolean variables and logical connectivities (guess why?). The solver is equipped with
 * methods to work with sequence of conditional transition relations (such as EFAs) where a sequence
 * of updates can be modelled by automatic renaming of variables where next-variables (primed
 * variables) of the i-th condition in the sequence will serve the current variables (unprimed
 * variables) of i+1-th condition, and so on.
 * <p>
 * A sample code to use this solver is as follows.
 * <PRE>
 * {@link CSPSolver} solver = new {@link #CSPSolver(net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariableContext) (varcontext)};
 * solver.{@link #init(java.util.List)} (constraints);
 * solver.{@link #solve()};
 * while (solver.{@link #isSatisfiable()}){
 *    model = solver.{@link #getModel()};
 *    // Do something with the model
 * }
 * </PRE>
 * <p>
 * @author Mohammad Reza Shoaei
 */
public class CSPSolver
{
  /**
   * Creates a CSP solver.
   * <p>
   * @param mode       Solver mode 0: default 1: light
   * @param timeout    Timeout for the solver in second
   * @param varContext Variables context
   */
  public CSPSolver(final int mode,
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
    mCSPVarNameToDomain = new THashMap<>();
    mCSPVarNameToSimVar = new THashMap<>();
    mCSPVarNameToCSPVar = new THashMap<>();
    mCSPPredicatesList = new ArrayList<>();
    mRangeToDomain = new THashMap<>();
    mIntToCSPEval = new TIntObjectHashMap<>();
    mLearnedClauses = new ArrayList<>();
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    mExpIntMap = new ProxyAccessorHashMap<>(eq);
    mNbCSPVars = 0;
    mVarPostfix = 0;
    mTrivialFalsity = false;
  }
  /**
   * Creates a CSP solver.
   * <p>
   * @param varContext Variables context
   */
  public CSPSolver(final SimpleEFAVariableContext varContext)
  {
    this(SOLVER_LIGHT, DEFUALT_TIMEOUT, varContext);
  }

  /**
   * Clears and initialises the solver using the given constraints.
   * <p>
   * @param exp
   * @throws AnalysisException
   */
  public void init(final String exp)
   throws AnalysisException
  {
    reset();
    addExpression(exp);
  }

  /**
   * Clears and initialises the solver using the given constraint.
   * <p>
   * @param exps List of constraints viewed as conjunction of constraints
   * <p>
   * @throws AnalysisException
   */
  public void init(final List<SimpleExpressionProxy> exps)
   throws AnalysisException
  {
    reset();
    addExpression(exps);
  }

  /**
   * Add the given constraint to the list of constraints
   * <p>
   * @param exp
   * <p>
   * @throws AnalysisException
   */
  public void addExpression(final String exp)
   throws AnalysisException
  {
    addExpression(mHelper.parse(exp));
  }

  /**
   * Add negated of the given constraint to the list of constraints
   * <p>
   * @param str
   * <p>
   * @throws AnalysisException
   */
  public void addNegatedExpression(final String str)
   throws AnalysisException
  {
    addNegatedExpression(mHelper.parse(str));
  }

  /**
   * Add negated of the given constraint to the list of constraints
   * <p>
   * @param exps
   * <p>
   * @throws AnalysisException
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
   * <p>
   * @param exps
   * <p>
   * @throws AnalysisException
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
   * Resets and clears all the stored information.
   */
  public void reset()
  {
    mSolver.reset();
    mCSPVarNameToDomain.clear();
    mCSPVarNameToSimVar.clear();
    mCSPVarNameToCSPVar.clear();
    mCSPPredicatesList.clear();
    mIntToCSPEval.clear();
    mNbCSPVars = 0;
    mVarPostfix = 0;
    mTrivialFalsity = false;
    mLearnedClauses.clear();
    mRangeToDomain.clear();
    mExpIntMap.clear();
  }

  /**
   * Stores are the information (except learned clauses) in a buffer. The stored information can be
   * retrieved at any time by calling {@link #resume()} method.
   */
  public void pause()
  {
    mPauseInfo = new Object[]{
      new THashMap<>(mCSPVarNameToDomain),
      new THashMap<>(mCSPVarNameToSimVar),
      new THashMap<>(mCSPVarNameToCSPVar),
      new ArrayList<>(mCSPPredicatesList),
      new TIntObjectHashMap<>(mIntToCSPEval),
      mNbCSPVars,
      mVarPostfix,
      mTrivialFalsity};
  }

  /**
   * Restoring all the information stored by {@link #pause()}.
   */
  @SuppressWarnings("unchecked")
  public void resume()
  {
    mCSPVarNameToDomain = (THashMap<String, Domain>) mPauseInfo[0];
    mCSPVarNameToSimVar = (THashMap<String, String>) mPauseInfo[1];
    mCSPVarNameToCSPVar = (THashMap<String, Var>) mPauseInfo[2];
    mCSPPredicatesList = (ArrayList<Object[]>) mPauseInfo[3];
    mIntToCSPEval = (TIntObjectHashMap<Evaluable>) mPauseInfo[4];
    mNbCSPVars = (int) mPauseInfo[5];
    mVarPostfix = (int) mPauseInfo[6];
    mTrivialFalsity = (boolean) mPauseInfo[7];
    mPauseInfo = null;
    mSolver.reset();
  }

  /**
   * Solves, learns, and stores values of unprimed variables of the initial step (marked by postfix
   * 0) that satisfied all constraints. Note that the {@link #solve()} method is automatically
   * called by the method.
   * <p>
   * @param negate If the negated of the values must be stored.
   * <p>
   * @return Returns <CODE>true</CODE> if the learning process was successful. Otherwise,
   *         <CODE>false</CODE>.
   * <p>
   * @throws AnalysisException
   */
  public boolean learn(final boolean negate) throws AnalysisException
  {
    return learn(negate, 0);
  }

  /**
   * Solves, learns, and stores values of unprimed variables of the particular step that satisfied
   * all constraints. Note that the {@link #solve()} method is automatically called by the method.
   * <p>
   * @param negate  If the negated of the values must be stored.
   * @param postfix The step, initially it is 0.
   * <p>
   * @return Returns true if the learning process was successful.
   * <p>
   * @throws AnalysisException
   */
  public boolean learn(final boolean negate, final int postfix)
   throws AnalysisException
  {
    boolean success = false;
    final String fn = negate ? CSPOP_NOTEQUALS : CSPOP_EQUALS;
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
          pairs.add(fn + "(" + key + "," + value + ")");
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
          pExpStr += CSPOP_AND + "(";
        }
        pExpStr += pairs.get(j - 1);
        pExpStr += ",";
      }
      pExpStr = pExpStr.substring(0, pExpStr.length() - 1);
      for (int j = 1; j < pairs.size(); j++) {
        pExpStr += ")";
      }
      mLearnedClauses.add(new Object[]{pExpStr, pExpVars, pExpCons.toArray()});
      success = true;
    }
    return success;
  }

  /**
   * Returns whether there exists any solution for the current constraints. Note that
   * {@link #solve()} must be called before hand.
   * <p>
   * @return <CODE>true</CODE> is there is any solution, <CODE>false</CODE> otherwise.
   * <p>
   * @throws AnalysisException
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
   * <p>
   * @return A map of variables to the values.
   */
  public Map<String, SimpleExpressionProxy> getModel()
  {
    final THashMap<String, SimpleExpressionProxy> sVarToValue = new THashMap<>();
    final THashMap<String, SimpleExpressionProxy> translation = getInnerModel();
    for (final Map.Entry<String, SimpleExpressionProxy> entry : translation.entrySet()) {
      if (entry.getKey().endsWith("0")) {
        sVarToValue.put(mCSPVarNameToSimVar.get(entry.getKey()), entry.getValue());
      }
    }
    return sVarToValue;
  }

  /**
   * Returns the inner model, namely, variables with postfix. Note that {@link #isSatisfiable()}
   * must be called before hand.
   * <p>
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
   * <p>
   * @throws AnalysisException
   */
  @SuppressWarnings("unchecked")
  public void solve() throws AnalysisException
  {
    try {
      mSolver.reset();
      final List<Var> allVars = new ArrayList<>();
      final ArrayList<Object[]> predicates = new ArrayList<>(mCSPPredicatesList);
      predicates.addAll(mLearnedClauses);
      for (final Object[] obj : predicates) {
        final Predicate P = new Predicate();
        final String pStr = (String) obj[0];
        P.setExpression(pStr);
        final Set<String> pVars = (Set<String>) obj[1];
        final IVec<Evaluable> vars = new Vec<>();
        final IVec<Var> scope = new Vec<>();
        for (final String cVarName : pVars) {
          final String sVarName = mCSPVarNameToSimVar.get(cVarName);
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
      mSolver.newVar(mNbCSPVars);
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
      p[0] = CSPOP_NOT + "(" + pExpStr + ")";
      mCSPPredicatesList.add(p);
    }
  }

  private void addExpression(final SimpleExpressionProxy exp)
   throws AnalysisException
  {
    final Object[] p = getCSPPredicate(exp);
    if (p != null) {
      mCSPPredicatesList.add(p);
    }
  }

  private Var createCSPVar(final String name, final Domain domain)
  {
    Var var = mCSPVarNameToCSPVar.get(name);
    if (var == null) {
      var = new Var(name, domain, mNbCSPVars);
      mNbCSPVars += domain.size();
      mCSPVarNameToCSPVar.put(name, var);
    }
    return var;
  }

  private void setupCSPIntVar(final IVec<Evaluable> vars, final int[] constants)
  {
    for (final int d : constants) {
      Evaluable var = mIntToCSPEval.get(d);
      if (var == null) {
        var = new Constant(d);
        mIntToCSPEval.put(d, var);
      }
      vars.push(var);
    }
  }

  private Domain getCSPDomain(final String name, final CompiledRange range)
   throws AnalysisException
  {
    Domain domain = mRangeToDomain.get(range);
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
    mCSPVarNameToDomain.put(name, domain);
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
        pAndExp += CSPOP_AND + "(";
      }
      String pOrExp = "";
      final CompiledClause clause = clauses.get(i - 1);
      final List<SimpleExpressionProxy> literals = new ArrayList<>(clause.getLiterals());
      for (int j = literals.size(); j > 0; j--) {
        final SimpleExpressionProxy literal = literals.get(j - 1);
        if (j > 1) {
          pOrExp += CSPOP_OR + "(";
        }
        visitor.clear();
        literal.acceptVisitor(visitor);
        pOrExp += visitor.getPredicate();
        pOrExp += ",";
      }
      pOrExp = pOrExp.substring(0, pOrExp.length() - 1);
      for (int j = 1; j < literals.size(); j++) {
        pOrExp += ")";
      }
      pAndExp += pOrExp + ",";
    }
    pAndExp = pAndExp.substring(0, pAndExp.length() - 1);
    for (int i = 1; i < clauses.size(); i++) {
      pAndExp += ")";
    }
    return pAndExp;
  }
  private ModelIterator getSolver(final int mode)
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
    for (final String cVarName : mCSPVarNameToCSPVar.keySet()) {
      final Var cVar = mCSPVarNameToCSPVar.get(cVarName);
      final int value = cVar.findValue(model);
      final Domain domain = mCSPVarNameToDomain.get(cVarName);
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
        mPredicate += CSPOP_PLUS;
      } else if (op == mOp.getMinusOperator()) {
        mPredicate += CSPOP_MINUS;
      } else if (op == mOp.getModuloOperator()) {
        mPredicate += CSPOP_MODULO;
      } else if (op == mOp.getTimesOperator()) {
        mPredicate += CSPOP_TIMES;
      } else if (op == mOp.getDivideOperator()) {
        mPredicate += CSPOP_DIVIDE;
      } else if (op == mOp.getEqualsOperator()) {
        mPredicate += CSPOP_EQUALS;
      } else if (op == mOp.getNotEqualsOperator()) {
        mPredicate += CSPOP_NOTEQUALS;
      } else if (op == mOp.getLessThanOperator()) {
        mPredicate += CSPOP_LESS_THAN;
      } else if (op == mOp.getLessEqualsOperator()) {
        mPredicate += CSPOP_LESS_EQUALS;
      } else if (op == mOp.getGreaterThanOperator()) {
        mPredicate += CSPOP_GREATER_THAN;
      } else if (op == mOp.getGreaterEqualsOperator()) {
        mPredicate += CSPOP_GREATER_EQUALS;
      } else {
        mPredicate = "Unsupported operator: " + op.getName();
        mUnsupportedFlag = true;
        return null;
      }
      mPredicate += "(";
      exp.getLeft().acceptVisitor(this);
      mPredicate += ",";
      exp.getRight().acceptVisitor(this);
      mPredicate += ")";
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
        mCSPVarNameToSimVar.put(cName, sName);
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
          mPredicate += CSPOP_NOT + "(";
          exp.getSubTerm().acceptVisitor(this);
          mPredicate += ")";
        } else if (op == mOp.getNextOperator()) {
          final String sName = exp.getSubTerm().toString();
          final String cName = sName + (mVarPostfix + 1);
          mPredicate += cName;
          mExpVars.add(cName);
          mCSPVarNameToSimVar.put(cName, sName);
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

  private static final String CSPOP_EQUALS = "eq";
  private static final String CSPOP_NOTEQUALS = "ne";
  private static final String CSPOP_GREATER_THAN = "gt";
  private static final String CSPOP_GREATER_EQUALS = "ge";
  private static final String CSPOP_LESS_THAN = "lt";
  private static final String CSPOP_LESS_EQUALS = "le";
  private static final String CSPOP_MINUS = "sub";
  private static final String CSPOP_PLUS = "add";
  private static final String CSPOP_TIMES = "mul";
  private static final String CSPOP_DIVIDE = "div";
  private static final String CSPOP_MODULO = "mod";
  private static final String CSPOP_AND = "and";
  private static final String CSPOP_OR = "or";
  private static final String CSPOP_NOT = "not";

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
  private THashMap<String, Domain> mCSPVarNameToDomain;
  private THashMap<String, String> mCSPVarNameToSimVar;
  private THashMap<String, Var> mCSPVarNameToCSPVar;
  private TIntObjectHashMap<Evaluable> mIntToCSPEval;
  private ArrayList<Object[]> mCSPPredicatesList;
  private boolean mTrivialFalsity;
  private Object[] mPauseInfo;
  private int mNbCSPVars;
  private int mVarPostfix;
}
