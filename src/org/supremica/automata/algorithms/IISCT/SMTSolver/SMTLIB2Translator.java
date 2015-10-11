//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT:
//# PACKAGE: org.supremica.automata.algorithms.PDR.SMTSolver
//# CLASS:   SMTLIB2Translator
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.automata.algorithms.IISCT.SMTSolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import net.sourceforge.waters.analysis.efa.simple.SimpleEFAHelper;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.dnf.CompiledClause;
import net.sourceforge.waters.model.compiler.dnf.DNFConverter;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.plain.module.BinaryExpressionElement;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.plain.module.UnaryExpressionElement;

/**
 * A helper class to translate given expressions (String or SimpleExpression)
 * to SMTLIB2 expressions
 * @author Mohammad Reza Shoaei
 */
public class SMTLIB2Translator
{

  public static SMTLIB2Translator getInstatnce()
  {
    return INSTANCE;
  }

  private SMTLIB2Translator()
  {
    mFactory = ModuleElementFactory.getInstance();
    mOp = CompilerOperatorTable.getInstance();
    mCloner = mFactory.getCloner();
    mHelper = new SimpleEFAHelper(mFactory, mOp);
    mDNF = new DNFConverter(mFactory, mOp);
    mVarNames = new HashSet<>();
    mVarIndexCurr = 0;
    mVarIndexNext = 1;
  }

  public List<String> getVarNames()
  {
    return new ArrayList<>(mVarNames);
  }

  public String getSMTLIB2String(final int firstIndex, final boolean incremental, final String... str)
   throws SolverException
  {
    if (str == null || str.length < 1) {
      return TRUE;
    }
    return getSMTLIB2String(firstIndex, incremental, mHelper.parse(str));
  }

  public String getSMTLIB2String(final int[] indexSet, final String... str)
   throws SolverException
  {
    final List<List<SimpleExpressionProxy>> exps = new ArrayList<>(str.length);
    for (final String s : str) {
    	final List<SimpleExpressionProxy> parse = mHelper.parse(s);
    	if (parse == null){
    		throw new SolverException("getSMTLIB2String > Parser returns null. Please check your string.");
    	}
      exps.add(parse);
    }
    return getSMTLIB2String(indexSet, exps);
  }

  public String getSMTLIB2String(final int firstIndex, final boolean incremental,
                                 final List<SimpleExpressionProxy> exps)
   throws SolverException
  {
    if (exps == null || exps.isEmpty()) {
      return TRUE;
    }
    return mkSMTLIB2String(mkIndexSet(firstIndex, incremental, exps.size()), Arrays.asList(exps));
  }

  public String getSMTLIB2String(final int[] indexSet, final List<List<SimpleExpressionProxy>> exps)
   throws SolverException
  {
    if (indexSet == null || exps == null) {
      throw new NullPointerException();
    }
    if (indexSet.length != exps.size() + 1) {
      throw new SolverException(
       "SMTLIB2Translator > Number of indices are not equal to the number of expressions plus one.");
    }
    if (exps.isEmpty()) {
      return TRUE;
    }
    return mkSMTLIB2String(indexSet, exps);
  }

  private String mkSMTLIB2String(final int[] indexSet, final List<List<SimpleExpressionProxy>> exps)
   throws SolverException
  {
    try {
      int nbrAndClauses = 0;
      Collection<CompiledClause> andClauses;
      Collection<SimpleExpressionProxy> orClauses;
      final ExpressionVisitor visitor = new ExpressionVisitor();
      String sAndExp = "";
      for (int i = 0; i < exps.size(); i++) {
        final List<SimpleExpressionProxy> exp = exps.get(i);
        if (indexSet != null) {
          mVarIndexCurr = indexSet[i];
          mVarIndexNext = indexSet[i + 1];
        }
        for (final SimpleExpressionProxy e : exp) {
          andClauses = mDNF.convertToCNF(e).getClauses();
          if (!andClauses.isEmpty()) {
            nbrAndClauses += andClauses.size();
            for (final CompiledClause clause : andClauses) {
              orClauses = clause.getLiterals();
              String sOrExp = "";
              if (orClauses.size() > 1) {
                sOrExp += _OPEN + _OR;
              }
              for (final SimpleExpressionProxy literal : orClauses) {
                literal.acceptVisitor(visitor);
                sOrExp += visitor.getFormula() + _SPACE;
                visitor.clear();
              }
              sOrExp = sOrExp.substring(0, sOrExp.length() - 1);
              if (orClauses.size() > 1) {
                sOrExp += _CLOSE;
              }
              sAndExp += sOrExp + _SPACE;
            }
          }
        }
      }
      if (sAndExp.isEmpty()) {
        return TRUE;
      }
      sAndExp = sAndExp.substring(0, sAndExp.length() - 1);
      String smtStr = _OPEN + _ASSERT;
      if (nbrAndClauses > 1) {
        smtStr += _OPEN + _AND;
      }
      smtStr += sAndExp;
      if (nbrAndClauses > 1) {
        smtStr += _CLOSE;
      }
      smtStr += _CLOSE;
      return smtStr;
    } catch (final EvalException ex) {
      throw new SolverException("SMTLIB2 Expo. > Eval Exception: " + ex);
    } catch (final VisitorException ex) {
      throw new SolverException("SMTLIB2 Expo. > Visitor Exception: " + ex);
    } catch (final Exception ex) {
    }
    return FALSE;
  }

  public void reset()
  {
    mVarIndexCurr = 0;
    mVarNames.clear();
  }

  private int[] mkIndexSet(final int firstIndex, final boolean incremental, final int size)
  {
    int[] idx = null;
    if (incremental) {
      idx = new int[size + 1];
      for (int i = 0; i < size + 1; i++) {
        idx[i] = i + firstIndex;
      }
    } else {
      mVarIndexCurr = firstIndex;
      mVarIndexNext = firstIndex + 1;
    }
    return idx;
  }

  //#########################################################################
  // Inner class
  //#########################################################################
  class ExpressionVisitor extends DefaultModuleProxyVisitor
  {

    //#########################################################################
    @Override
    public Object visitBinaryExpressionProxy(final BinaryExpressionProxy proxy)
     throws VisitorException
    {
      final BinaryExpressionElement exp = (BinaryExpressionElement) mCloner.getClone(proxy);
      final BinaryOperator op = exp.getOperator();
      vStr += _OPEN;
      if (op == mOp.getPlusOperator()) {
        vStr += _PLUS;
      } else if (op == mOp.getMinusOperator()) {
        vStr += _MINUS;
      } else if (op == mOp.getTimesOperator()) {
        vStr += _TIMES;
      } else if (op == mOp.getDivideOperator()) {
        vStr += _DIVIDE;
      } else if (op == mOp.getEqualsOperator()) {
        vStr += _EQUALS;
      } else if (op == mOp.getNotEqualsOperator()) {
        vStr += _NOT + _OPEN + _EQUALS;
        exp.getLeft().acceptVisitor(this);
        vStr += _SPACE;
        exp.getRight().acceptVisitor(this);
        vStr += _CLOSE + _CLOSE;
        return null;
      } else if (op == mOp.getLessThanOperator()) {
        vStr += _LESS_THAN;
      } else if (op == mOp.getLessEqualsOperator()) {
        vStr += _LESS_EQUALS;
      } else if (op == mOp.getGreaterThanOperator()) {
        vStr += _GREATER_THAN;
      } else if (op == mOp.getGreaterEqualsOperator()) {
        vStr += _GREATER_EQUALS;
      } else {
        throw new VisitorException("Unsupported operator: " + op.getName());
      }
      exp.getLeft().acceptVisitor(this);
      vStr += _SPACE;
      exp.getRight().acceptVisitor(this);
      vStr += _CLOSE;
      return null;
    }

    @Override
    public Object visitIntConstantProxy(final IntConstantProxy proxy)
     throws VisitorException
    {
      vStr += proxy.getValue();
      return null;
    }

    @Override
    public Object visitSimpleIdentifierProxy(final SimpleIdentifierProxy proxy)
     throws VisitorException
    {
      final String sName = proxy.getName();
      final String cName = sName + mVarIndexCurr;
      vStr += cName;
      mVarNames.add(cName);
      return null;
    }

    @Override
    public Object visitUnaryExpressionProxy(final UnaryExpressionProxy proxy)
     throws VisitorException
    {
      final UnaryExpressionElement exp = (UnaryExpressionElement) mCloner.getClone(proxy);
      final UnaryOperator op = exp.getOperator();
      if (op == mOp.getNotOperator()) {
        vStr += _NOT + SimpleEFAHelper.DEFAULT_OPENING_STRING;
        exp.getSubTerm().acceptVisitor(this);
        vStr += SimpleEFAHelper.DEFAULT_CLOSING_STRING;
      } else if (op == mOp.getNextOperator()) {
        final String sName = exp.getSubTerm().toString();
        final String cName = sName + mVarIndexNext;
        vStr += cName;
        mVarNames.add(cName);
      } else if (op == mOp.getUnaryMinusOperator()) {
        vStr += _OPEN + _MINUS + _SPACE;
        exp.getSubTerm().acceptVisitor(this);
        vStr += _CLOSE;
      } else {
        throw new VisitorException("Unsupported operator: " + op.getName());
      }
      return null;
    }

    String getFormula()
    {
      return vStr;
    }

    void clear()
    {
      vStr = "";
    }

    private String vStr = "";
  }

  private static final SMTLIB2Translator INSTANCE = new SMTLIB2Translator();
  private static final String _EQUALS = "= ";
  private static final String _GREATER_THAN = "> ";
  private static final String _GREATER_EQUALS = ">= ";
  private static final String _LESS_THAN = "< ";
  private static final String _LESS_EQUALS = "<= ";
  private static final String _MINUS = "- ";
  private static final String _PLUS = "+ ";
  private static final String _TIMES = "* ";
  private static final String _DIVIDE = "/ ";
  private static final String _NOT = "not ";
  private static final String _AND = "and ";
  private static final String _OR = "or ";
  private static final String _ASSERT = "assert ";
  private static final String _OPEN = "(";
  private static final String _CLOSE = ")";
  private static final String _SPACE = " ";
  public static final String TRUE = "(assert true)";
  public static final String FALSE = "(assert false)";

  private final ModuleElementFactory mFactory;
  private final CompilerOperatorTable mOp;
  private final ModuleProxyCloner mCloner;
  private final HashSet<String> mVarNames;
  private final SimpleEFAHelper mHelper;
  private final DNFConverter mDNF;
  private int mVarIndexCurr;
  private int mVarIndexNext;
}
