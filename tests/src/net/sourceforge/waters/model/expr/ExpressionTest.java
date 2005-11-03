//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   ExpressionTest
//###########################################################################
//# $Id: ExpressionTest.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.expr;

import java.util.LinkedList;
import java.util.List;
import junit.framework.TestCase;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public class ExpressionTest extends TestCase
{

  //#########################################################################
  //# Successful Parse Tests
  public void testExpression_1()
    throws ParseException
  {
    testExpression("1", mExpr_1, Operator.TYPE_INT);
  }

  public void testExpression_m100()
    throws ParseException
  {
    testExpression("-100", mExpr_m100, Operator.TYPE_INT);
  }

  public void testExpression_mm100()
    throws ParseException
  {
    testExpression("--100", mExpr_mm100, Operator.TYPE_INT);
  }

  public void testExpression_1eqm100()
    throws ParseException
  {
    testExpression("1 == -100", mExpr_1eqm100, Operator.TYPE_INT);
  }

  public void testExpression_1minusm100()
    throws ParseException
  {
    testExpression("1--100", mExpr_1minusm100, Operator.TYPE_INT);
  }

  public void testExpression_1plus1()
    throws ParseException
  {
    testExpression("1 + 1", mExpr_1plus1, Operator.TYPE_INT);
  }

  public void testExpression_1plus1eq2()
    throws ParseException
  {
    testExpression("1 + 1 == 2", mExpr_1plus1eq2, Operator.TYPE_INT);
  }

  public void testExpression_1plus1plus2__1()
    throws ParseException
  {
    testExpression("1 + 1 + 2", mExpr_1plus1plus2, Operator.TYPE_INT);
  }

  public void testExpression_1plus1plus2__2()
    throws ParseException
  {
    testExpression("((1+  1)+2 )", mExpr_1plus1plus2, Operator.TYPE_INT);
  }

  public void testExpression_1plusm2()
    throws ParseException
  {
    testExpression("1+-2", mExpr_1plusm2, Operator.TYPE_INT);
  }

  public void testExpression_1to2()
    throws ParseException
  {
    testExpression("1..2", mExpr_1to2, Operator.TYPE_RANGE);
  }

  public void testExpression_2eq1plus1()
    throws ParseException
  {
    testExpression("2 == 1 + 1", mExpr_2eq1plus1, Operator.TYPE_INT);
  }

  public void testExpression_2minus1()
    throws ParseException
  {
    testExpression("2 - 1", mExpr_2minus1, Operator.TYPE_INT);
  }

  public void testExpression_2minus1minusa()
    throws ParseException
  {
    testExpression("2 - 1 - a", mExpr_2minus1minusa, Operator.TYPE_INT);
  }

  public void testExpression_2plus1minusa()
    throws ParseException
  {
    testExpression("2 + 1 - a", mExpr_2plus1minusa, Operator.TYPE_INT);
  }

  public void testExpression_2minus1plusa()
    throws ParseException
  {
    testExpression("2 - 1 + a", mExpr_2minus1plusa, Operator.TYPE_INT);
  }

  public void testExpression_2plus1plus1__1()
    throws ParseException
  {
    testExpression("2+(1+1)", mExpr_2plus1plus1, Operator.TYPE_INT);
  }

  public void testExpression_2plus1plus1__2()
    throws ParseException
  {
    testExpression("( 2+((1+1)) )", mExpr_2plus1plus1, Operator.TYPE_INT);
  }

  public void testExpression_m100tom2()
    throws ParseException
  {
    testExpression("-100..-2", mExpr_m100tom2, Operator.TYPE_RANGE);
  }

  public void testExpression_m_a()
    throws ParseException
  {
    testExpression("-a", mExpr_m_a, Operator.TYPE_INT);
  }

  public void testExpression_a_b_c()
    throws ParseException
  {
    testExpression("{a, b, c}", mExpr_a_b_c, Operator.TYPE_RANGE);
  }

  public void testExpression_event_1()
    throws ParseException
  {
    testExpression("event[1]", mExpr_event_1, Operator.TYPE_NAME);
  }

  public void testExpression_event_1_1plus1()
    throws ParseException
  {
    testExpression("event [1][1+1]", mExpr_event_1_1plus1, Operator.TYPE_NAME);
  }

  public void testExpression_1_multi()
    throws ParseException
  {
    testExpression("1", mExpr_1, Operator.TYPE_INT);
    testExpression("1", mExpr_1, Operator.TYPE_INT);
    testExpression("1", mExpr_1, Operator.TYPE_INT);
    testExpression("1", mExpr_1, Operator.TYPE_INT);
    testExpression("1", mExpr_1, Operator.TYPE_INT);
  }


  //#########################################################################
  //# Unsuccessful Parse Tests
  public void testError_event_1to2()
  {
    testExpression("event[1..2]", "'1..2'");
  }

  public void testError_event_1_plus_event_2()
  {
    testExpression("event[1] + event[2]", "'event[1]'");
  }

  public void testError_eq_s_multi()
  {
    testExpression("=s", "'='");
    testExpression("=s", "'='");
    testExpression("=s", "'='");
    testExpression("=s", "'='");
    testExpression("=s", "'='");
  }


  //#########################################################################
  //# Utilities
  private void testExpression(final String text,
                              final SimpleExpressionProxy expr,
                              final int mask)
    throws ParseException
  {
    try {
      final SimpleExpressionProxy parsed = mParser.parse(text, mask);
      assertTrue("Unexpected result!", parsed.equals(expr));
    } catch (final ParseException exception) {
      final int pos = exception.getErrorOffset();
      System.out.println(text);
      for (int i = 0; i < pos; i++) {
        System.out.print(' ');
      }
      System.out.println('^');
      throw exception;
    }
  }

  private void testExpression(final String text, final String culprit)
  {
    try {
      mParser.parse(text);
      fail("Expected ParseException not caught!");
    } catch (final ParseException exception) {
      final String msg = exception.getMessage();
      assertTrue("Caught ParseException <" + msg +
                 "> does not mention culprit <" + culprit + ">!",
                 msg.indexOf(culprit) >= 0);
    }
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
  {
    mFactory = ModuleElementFactory.getInstance();
    mOperatorTable = CompilerOperatorTable.getInstance();
    mParser = new ExpressionParser(mFactory, mOperatorTable);

    final BinaryOperator plus = mOperatorTable.getBinaryOperator("+");
    final BinaryOperator minus = mOperatorTable.getBinaryOperator("-");
    final BinaryOperator equals = mOperatorTable.getBinaryOperator("==");
    final BinaryOperator range = mOperatorTable.getBinaryOperator("..");
    final UnaryOperator uminus = mOperatorTable.getUnaryOperator("-");

    final List<SimpleIdentifierProxy> idlist =
      new LinkedList<SimpleIdentifierProxy>();
    final List<SimpleExpressionProxy> exlist =
      new LinkedList<SimpleExpressionProxy>();
    mExpr_1 = mFactory.createIntConstantProxy(1);
    mExpr_2 = mFactory.createIntConstantProxy(2);
    mExpr_m2 = mFactory.createIntConstantProxy(-2);
    mExpr_m100 = mFactory.createIntConstantProxy(-100);
    mExpr_mm100 = mFactory.createUnaryExpressionProxy(uminus, mExpr_m100);
    mExpr_a = mFactory.createSimpleIdentifierProxy("a");
    mExpr_b = mFactory.createSimpleIdentifierProxy("b");
    mExpr_c = mFactory.createSimpleIdentifierProxy("c");
    mExpr_1eqm100 = mFactory.createBinaryExpressionProxy
      (equals, mExpr_1, mExpr_m100);
    mExpr_1minusm100 =
      mFactory.createBinaryExpressionProxy(minus, mExpr_1, mExpr_m100);
    mExpr_1plus1 =
      mFactory.createBinaryExpressionProxy(plus, mExpr_1, mExpr_1);
    mExpr_1plus1eq2 =
      mFactory.createBinaryExpressionProxy(equals, mExpr_1plus1, mExpr_2);
    mExpr_1plus1plus2 =
      mFactory.createBinaryExpressionProxy(plus, mExpr_1plus1, mExpr_2);
    mExpr_1plusm2 =
      mFactory.createBinaryExpressionProxy(plus, mExpr_1, mExpr_m2);
    mExpr_1to2 =
      mFactory.createBinaryExpressionProxy(range, mExpr_1, mExpr_2);
    mExpr_2eq1plus1 =
      mFactory.createBinaryExpressionProxy(equals, mExpr_2, mExpr_1plus1);
    mExpr_2minus1 =
      mFactory.createBinaryExpressionProxy(minus, mExpr_2, mExpr_1);
    mExpr_2minus1minusa =
      mFactory.createBinaryExpressionProxy(minus, mExpr_2minus1, mExpr_a);
    mExpr_2minus1plusa =
      mFactory.createBinaryExpressionProxy(plus, mExpr_2minus1, mExpr_a);
    mExpr_2plus1 =
      mFactory.createBinaryExpressionProxy(plus, mExpr_2, mExpr_1);
    mExpr_2plus1minusa =
      mFactory.createBinaryExpressionProxy(minus, mExpr_2plus1, mExpr_a);
    mExpr_2plus1plus1 =
      mFactory.createBinaryExpressionProxy(plus, mExpr_2, mExpr_1plus1);
    mExpr_m100tom2 =
      mFactory.createBinaryExpressionProxy(range, mExpr_m100, mExpr_m2);
    mExpr_m_a = mFactory.createUnaryExpressionProxy(uminus, mExpr_a);
    idlist.add(mExpr_a);
    idlist.add(mExpr_b);
    idlist.add(mExpr_c);
    mExpr_a_b_c = mFactory.createEnumSetExpressionProxy(idlist);
    exlist.add(mExpr_1);
    mExpr_event_1 = mFactory.createIndexedIdentifierProxy("event", exlist);
    exlist.add(mExpr_1plus1);
    mExpr_event_1_1plus1 =
      mFactory.createIndexedIdentifierProxy("event", exlist);
  }

  protected void tearDown()
  {
    mFactory = null;
    mOperatorTable = null;
    mParser = null;
    mExpr_1 = null;
    mExpr_2 = null;
    mExpr_m2 = null;
    mExpr_m100 = null;
    mExpr_mm100 = null;
    mExpr_a = null;
    mExpr_b = null;
    mExpr_c = null;
    mExpr_1eqm100 = null;
    mExpr_1minusm100 = null;
    mExpr_1plus1 = null;
    mExpr_1plus1eq2 = null;
    mExpr_1plus1plus2 = null;
    mExpr_1plusm2 = null;
    mExpr_1to2 = null;
    mExpr_2eq1plus1 = null;
    mExpr_2minus1 = null;
    mExpr_2minus1minusa = null;
    mExpr_2minus1plusa = null;
    mExpr_2plus1 = null;
    mExpr_2plus1minusa = null;
    mExpr_2plus1plus1 = null;
    mExpr_m100tom2 = null;
    mExpr_m_a = null;
    mExpr_a_b_c = null;
    mExpr_event_1 = null;
    mExpr_event_1_1plus1 = null;
  }


  //#########################################################################
  //# Data Members
  private ModuleProxyFactory mFactory;
  private OperatorTable mOperatorTable;
  private ExpressionParser mParser;

  private SimpleExpressionProxy mExpr_1;
  private SimpleExpressionProxy mExpr_2;
  private SimpleExpressionProxy mExpr_m2;
  private SimpleExpressionProxy mExpr_m100;
  private SimpleExpressionProxy mExpr_mm100;
  private SimpleExpressionProxy mExpr_1eqm100;
  private SimpleExpressionProxy mExpr_1minusm100;
  private SimpleExpressionProxy mExpr_1plus1;
  private SimpleExpressionProxy mExpr_1plus1eq2;
  private SimpleExpressionProxy mExpr_1plus1plus2;
  private SimpleExpressionProxy mExpr_1plusm2;
  private SimpleExpressionProxy mExpr_1to2;
  private SimpleExpressionProxy mExpr_2eq1plus1;
  private SimpleExpressionProxy mExpr_2plus1plus1;
  private SimpleExpressionProxy mExpr_2minus1;
  private SimpleExpressionProxy mExpr_2minus1minusa;
  private SimpleExpressionProxy mExpr_2minus1plusa;
  private SimpleExpressionProxy mExpr_2plus1;
  private SimpleExpressionProxy mExpr_2plus1minusa;
  private SimpleIdentifierProxy mExpr_a;
  private SimpleIdentifierProxy mExpr_b;
  private SimpleIdentifierProxy mExpr_c;
  private SimpleExpressionProxy mExpr_m100tom2;
  private SimpleExpressionProxy mExpr_m_a;
  private SimpleExpressionProxy mExpr_a_b_c;
  private SimpleExpressionProxy mExpr_event_1;
  private SimpleExpressionProxy mExpr_event_1_1plus1;

}
