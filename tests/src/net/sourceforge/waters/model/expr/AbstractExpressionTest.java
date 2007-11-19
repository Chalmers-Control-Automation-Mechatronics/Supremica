//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   AbstractExpressionTest
//###########################################################################
//# $Id: AbstractExpressionTest.java,v 1.2 2007-11-19 03:05:23 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.expr;

import java.util.LinkedList;
import java.util.List;
import junit.framework.TestCase;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


public abstract class AbstractExpressionTest extends TestCase
{

  //#########################################################################
  //# Constructors
  public AbstractExpressionTest()
  {
  }

  public AbstractExpressionTest(final String name)
  {
    super(name);
  }


  //#########################################################################
  //# Provided by Subclasses
  protected abstract ModuleProxyFactory getFactory();


  //#########################################################################
  //# Successful Parse Tests
  public void testExpression_1()
    throws ParseException
  {
    testExpression("1", mExpr_1, Operator.TYPE_INT);
  }

  public void testExpression_1_a()
    throws ParseException
  {
    testExpression("001", mExpr_1, Operator.TYPE_INT);
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
    testExpression("1 == -100", mExpr_1eqm100, Operator.TYPE_BOOLEAN);
  }

  public void testExpression_1minusm100()
    throws ParseException
  {
    testExpression("1--100", mExpr_1minusm100, Operator.TYPE_INT);
  }

  public void testExpression_1minusmm100()
    throws ParseException
  {
    testExpression("1---100", mExpr_1minusmm100, Operator.TYPE_INT);
  }

  public void testExpression_1plus1_nospace()
    throws ParseException
  {
    testExpression("1+1", mExpr_1plus1, Operator.TYPE_INT);
  }

  public void testExpression_1plus1_space()
    throws ParseException
  {
    testExpression("1 + 1", mExpr_1plus1, Operator.TYPE_INT);
  }

  public void testExpression_1plus1eq2()
    throws ParseException
  {
    testExpression("1 + 1 == 2", mExpr_1plus1eq2, Operator.TYPE_BOOLEAN);
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
    testExpression("2 == 1 + 1", mExpr_2eq1plus1, Operator.TYPE_BOOLEAN);
  }

  public void testExpression_2minus1()
    throws ParseException
  {
    testExpression("2 - 1", mExpr_2minus1, Operator.TYPE_INT);
  }

  public void testExpression_2minus1_a()
    throws ParseException
  {
    testExpression("2-1", mExpr_2minus1, Operator.TYPE_INT);
  }

  public void testExpression_2minus1minus1()
    throws ParseException
  {
    testExpression("2-1-1", mExpr_2minus1minus1, Operator.TYPE_INT);
  }

  public void testExpression_2minus1minus1_a()
    throws ParseException
  {
    testExpression("(2-1)-1", mExpr_2minus1minus1, Operator.TYPE_INT);
  }

  public void testExpression_aminus1()
    throws ParseException
  {
    testExpression("a-1", mExpr_aminus1, Operator.TYPE_INT);
  }

  public void testExpression_2minus1minusa()
    throws ParseException
  {
    testExpression("2 - 1 - a", mExpr_2minus1minusa, Operator.TYPE_INT);
  }

  public void testExpression_2minus1minusa_a()
    throws ParseException
  {
    testExpression("2-1-a", mExpr_2minus1minusa, Operator.TYPE_INT);
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

  public void testExpression_a_b_c_nospace()
    throws ParseException
  {
    testExpression("{a,b,c}", mExpr_a_b_c, Operator.TYPE_RANGE);
  }

  public void testExpression_a_b_c_space()
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

  public void testExpression_event_m2()
    throws ParseException
  {
    testExpression("event[-2]", mExpr_event_m2, Operator.TYPE_NAME);
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
    testExpression("event[1..2]", "1..2");
  }

  public void testError_event_1_plus_event_2()
  {
    testExpression("event[1] + event[2]", "event[1]");
  }

  public void testError_a_b()
  {
    final String text = "{a, b";
    testExpression(text, text.length());
  }

  public void testError_a_b_comma()
  {
    final String text = "{a, b,";
    testExpression(text, text.length());
  }

  public void testError_eq_s_multi()
  {
    testExpression("=s", "=");
    testExpression("=s", "=");
    testExpression("=s", "=");
    testExpression("=s", "=");
    testExpression("=s", "=");
  }


  //#########################################################################
  //# Auxiliary Methods
  private void testExpression(final String text,
                              final SimpleExpressionProxy expr,
                              final int mask)
    throws ParseException
  {
    try {
      final SimpleExpressionProxy parsed = mParser.parse(text, mask);
      assertTrue("Unexpected result! - expected: <" + expr +
                 ">, but got <" + parsed + ">!",
                 parsed.equalsByContents(expr));
      assertEquals("Wrong plain text!", text, parsed.toString());
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
      final String eculprit = "'" + culprit + "'";
      assertTrue("Caught ParseException <" + msg +
                 "> does not mention culprit <" + culprit + ">!",
                 msg.indexOf(eculprit) >= 0);
      final int pos = exception.getErrorOffset();
      final int len = culprit.length();
      final String substring = text.substring(pos, pos + len);
      assertEquals("Indicated error position " + pos +
                   " in parsed string '" + text +
                   "' does not contain expected culprit '" + culprit + "'!",
                   culprit, substring);
    }
  }

  private void testExpression(final String text, final int errpos)
  {
    try {
      mParser.parse(text);
      fail("Expected ParseException not caught!");
    } catch (final ParseException exception) {
      final int pos = exception.getErrorOffset();
      assertEquals("Error in parsed string '" + text +
                   "' reported at wrong position!", pos, errpos);
    }
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
  {
    final ModuleProxyFactory factory = getFactory();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mParser = new ExpressionParser(factory, optable);

    final BinaryOperator plus = optable.getBinaryOperator("+");
    final BinaryOperator minus = optable.getBinaryOperator("-");
    final BinaryOperator equals = optable.getBinaryOperator("==");
    final BinaryOperator range = optable.getBinaryOperator("..");
    final UnaryOperator uminus = optable.getUnaryOperator("-");

    final List<SimpleIdentifierProxy> idlist =
      new LinkedList<SimpleIdentifierProxy>();
    final List<SimpleExpressionProxy> exlist =
      new LinkedList<SimpleExpressionProxy>();

    mExpr_1 = factory.createIntConstantProxy(1);
    mExpr_2 = factory.createIntConstantProxy(2);
    mExpr_m2 = factory.createIntConstantProxy(-2);
    mExpr_m100 = factory.createIntConstantProxy(-100);
    mExpr_mm100 = factory.createUnaryExpressionProxy(uminus, mExpr_m100);
    mExpr_a = factory.createSimpleIdentifierProxy("a");
    mExpr_b = factory.createSimpleIdentifierProxy("b");
    mExpr_c = factory.createSimpleIdentifierProxy("c");

    mExpr_1eqm100 = factory.createBinaryExpressionProxy
      (equals, mExpr_1.clone(), mExpr_m100.clone());
    mExpr_1minusm100 = factory.createBinaryExpressionProxy
      (minus, mExpr_1.clone(), mExpr_m100.clone());
    mExpr_1minusmm100 = factory.createBinaryExpressionProxy
      (minus, mExpr_1.clone(), mExpr_mm100.clone());
    mExpr_1plus1 = factory.createBinaryExpressionProxy
      (plus, mExpr_1.clone(), mExpr_1.clone());
    mExpr_1plus1eq2 = factory.createBinaryExpressionProxy
      (equals, mExpr_1plus1.clone(), mExpr_2.clone());
    mExpr_1plus1plus2 = factory.createBinaryExpressionProxy
      (plus, mExpr_1plus1.clone(), mExpr_2.clone());
    mExpr_1plusm2 = factory.createBinaryExpressionProxy
      (plus, mExpr_1.clone(), mExpr_m2.clone());
    mExpr_1to2 = factory.createBinaryExpressionProxy
      (range, mExpr_1.clone(), mExpr_2.clone());
    mExpr_2eq1plus1 = factory.createBinaryExpressionProxy
      (equals, mExpr_2.clone(), mExpr_1plus1.clone());
    mExpr_2minus1 = factory.createBinaryExpressionProxy
      (minus, mExpr_2.clone(), mExpr_1.clone());
    mExpr_2minus1minus1 = factory.createBinaryExpressionProxy
      (minus, mExpr_2minus1.clone(), mExpr_1.clone());
    mExpr_2minus1minusa = factory.createBinaryExpressionProxy
      (minus, mExpr_2minus1.clone(), mExpr_a.clone());
    mExpr_2minus1plusa = factory.createBinaryExpressionProxy
      (plus, mExpr_2minus1.clone(), mExpr_a.clone());
    mExpr_2plus1 = factory.createBinaryExpressionProxy
      (plus, mExpr_2.clone(), mExpr_1.clone());
    mExpr_2plus1minusa = factory.createBinaryExpressionProxy
      (minus, mExpr_2plus1.clone(), mExpr_a.clone());
    mExpr_2plus1plus1 = factory.createBinaryExpressionProxy
      (plus, mExpr_2.clone(), mExpr_1plus1.clone());
    mExpr_m100tom2 = factory.createBinaryExpressionProxy
      (range, mExpr_m100.clone(), mExpr_m2.clone());
    mExpr_m_a = factory.createUnaryExpressionProxy(uminus, mExpr_a.clone());
    mExpr_aminus1 = factory.createBinaryExpressionProxy
      (minus, mExpr_a.clone(), mExpr_1.clone());
    idlist.add(mExpr_a.clone());
    idlist.add(mExpr_b.clone());
    idlist.add(mExpr_c.clone());
    mExpr_a_b_c = factory.createEnumSetExpressionProxy(idlist);
    exlist.add(mExpr_1.clone());
    mExpr_event_1 = factory.createIndexedIdentifierProxy("event", exlist);
    exlist.clear();
    exlist.add(mExpr_1.clone());
    exlist.add(mExpr_1plus1.clone());
    mExpr_event_1_1plus1 =
      factory.createIndexedIdentifierProxy("event", exlist);
    exlist.clear();
    exlist.add(mExpr_m2.clone());
    mExpr_event_m2 = factory.createIndexedIdentifierProxy("event", exlist);
  }

  protected void tearDown()
  {
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
    mExpr_1minusmm100 = null;
    mExpr_1plus1 = null;
    mExpr_1plus1eq2 = null;
    mExpr_1plus1plus2 = null;
    mExpr_1plusm2 = null;
    mExpr_1to2 = null;
    mExpr_2eq1plus1 = null;
    mExpr_2minus1 = null;
    mExpr_2minus1minus1 = null;
    mExpr_2minus1minusa = null;
    mExpr_2minus1plusa = null;
    mExpr_2plus1 = null;
    mExpr_2plus1minusa = null;
    mExpr_2plus1plus1 = null;
    mExpr_m100tom2 = null;
    mExpr_m_a = null;
    mExpr_aminus1 = null;
    mExpr_a_b_c = null;
    mExpr_event_1 = null;
    mExpr_event_1_1plus1 = null;
    mExpr_event_m2 = null;
  }


  //#########################################################################
  //# Data Members
  private ExpressionParser mParser;

  private SimpleExpressionProxy mExpr_1;
  private SimpleExpressionProxy mExpr_2;
  private SimpleExpressionProxy mExpr_m2;
  private SimpleExpressionProxy mExpr_m100;
  private SimpleExpressionProxy mExpr_mm100;
  private SimpleExpressionProxy mExpr_1eqm100;
  private SimpleExpressionProxy mExpr_1minusm100;
  private SimpleExpressionProxy mExpr_1minusmm100;
  private SimpleExpressionProxy mExpr_1plus1;
  private SimpleExpressionProxy mExpr_1plus1eq2;
  private SimpleExpressionProxy mExpr_1plus1plus2;
  private SimpleExpressionProxy mExpr_1plusm2;
  private SimpleExpressionProxy mExpr_1to2;
  private SimpleExpressionProxy mExpr_2eq1plus1;
  private SimpleExpressionProxy mExpr_2plus1plus1;
  private SimpleExpressionProxy mExpr_2minus1;
  private SimpleExpressionProxy mExpr_2minus1minus1;
  private SimpleExpressionProxy mExpr_2minus1minusa;
  private SimpleExpressionProxy mExpr_2minus1plusa;
  private SimpleExpressionProxy mExpr_2plus1;
  private SimpleExpressionProxy mExpr_2plus1minusa;
  private SimpleIdentifierProxy mExpr_a;
  private SimpleIdentifierProxy mExpr_b;
  private SimpleIdentifierProxy mExpr_c;
  private SimpleExpressionProxy mExpr_m100tom2;
  private SimpleExpressionProxy mExpr_m_a;
  private SimpleExpressionProxy mExpr_aminus1;
  private SimpleExpressionProxy mExpr_a_b_c;
  private SimpleExpressionProxy mExpr_event_1;
  private SimpleExpressionProxy mExpr_event_1_1plus1;
  private SimpleExpressionProxy mExpr_event_m2;

}
