//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.junit
//# CLASS:   ExpressionTest
//###########################################################################
//# $Id: ExpressionTest.java,v 1.2 2005-02-17 19:34:19 robi Exp $
//###########################################################################


package net.sourceforge.waters.junit;

import java.util.LinkedList;
import java.util.List;
import junit.framework.TestCase;

import net.sourceforge.waters.model.expr.*;


public class ExpressionTest extends TestCase
{

  //#########################################################################
  //# Successful Parse Tests
  public void testExpression_1()
    throws ParseException
  {
    testExpression("1", mExpr_1, SimpleExpressionProxy.TYPE_INT);
  }

  public void testExpression_m100()
    throws ParseException
  {
    testExpression("-100", mExpr_m100, SimpleExpressionProxy.TYPE_INT);
  }

  public void testExpression_1eqm100()
    throws ParseException
  {
    testExpression("1 == -100", mExpr_1eqm100, SimpleExpressionProxy.TYPE_INT);
  }

  public void testExpression_1minusm100()
    throws ParseException
  {
    testExpression("1--100", mExpr_1minusm100, SimpleExpressionProxy.TYPE_INT);
  }

  public void testExpression_1plus1()
    throws ParseException
  {
    testExpression("1 + 1", mExpr_1plus1, SimpleExpressionProxy.TYPE_INT);
  }

  public void testExpression_1plus1eq2()
    throws ParseException
  {
    testExpression("1 + 1 == 2", mExpr_1plus1eq2,
		   SimpleExpressionProxy.TYPE_INT);
  }

  public void testExpression_1plus1plus2__1()
    throws ParseException
  {
    testExpression("1 + 1 + 2", mExpr_1plus1plus2,
		   SimpleExpressionProxy.TYPE_INT);
  }

  public void testExpression_1plus1plus2__2()
    throws ParseException
  {
    testExpression("((1+  1)+2 )", mExpr_1plus1plus2,
		   SimpleExpressionProxy.TYPE_INT);
  }

  public void testExpression_1plusm2()
    throws ParseException
  {
    testExpression("1+-2", mExpr_1plusm2, SimpleExpressionProxy.TYPE_INT);
  }

  public void testExpression_1to2()
    throws ParseException
  {
    testExpression("1..2", mExpr_1to2, SimpleExpressionProxy.TYPE_RANGE);
  }

  public void testExpression_2eq1plus1()
    throws ParseException
  {
    testExpression("2 == 1 + 1", mExpr_2eq1plus1,
		   SimpleExpressionProxy.TYPE_INT);
  }

  public void testExpression_2minus1()
    throws ParseException
  {
    testExpression("2 - 1", mExpr_2minus1,
		   SimpleExpressionProxy.TYPE_INT);
  }

  public void testExpression_2minus1minusa()
    throws ParseException
  {
    testExpression("2 - 1 - a", mExpr_2minus1minusa,
		   SimpleExpressionProxy.TYPE_INT);
  }

  public void testExpression_2plus1minusa()
    throws ParseException
  {
    testExpression("2 + 1 - a", mExpr_2plus1minusa,
		   SimpleExpressionProxy.TYPE_INT);
  }

  public void testExpression_2minus1plusa()
    throws ParseException
  {
    testExpression("2 - 1 + a", mExpr_2minus1plusa,
		   SimpleExpressionProxy.TYPE_INT);
  }

  public void testExpression_2plus1plus1__1()
    throws ParseException
  {
    testExpression("2+(1+1)", mExpr_2plus1plus1,
		   SimpleExpressionProxy.TYPE_INT);
  }

  public void testExpression_2plus1plus1__2()
    throws ParseException
  {
    testExpression("( 2+((1+1)) )", mExpr_2plus1plus1,
		   SimpleExpressionProxy.TYPE_INT);
  }

  public void testExpression_m100tom2()
    throws ParseException
  {
    testExpression("-100..-2", mExpr_m100tom2,
		   SimpleExpressionProxy.TYPE_RANGE);
  }

  public void testExpression_a_b_c()
    throws ParseException
  {
    testExpression("{a, b, c}", mExpr_a_b_c, SimpleExpressionProxy.TYPE_RANGE);
  }

  public void testExpression_event_1()
    throws ParseException
  {
    testExpression("event[1]", mExpr_event_1, SimpleExpressionProxy.TYPE_NAME);
  }

  public void testExpression_event_1_1plus1()
    throws ParseException
  {
    testExpression("event [1][1+1]", mExpr_event_1_1plus1,
		   SimpleExpressionProxy.TYPE_NAME);
  }

  public void testMultiInvoke()
    throws ParseException
  {
    testExpression("1", mExpr_1, SimpleExpressionProxy.TYPE_INT);
    testExpression("1", mExpr_1, SimpleExpressionProxy.TYPE_INT);
    testExpression("1", mExpr_1, SimpleExpressionProxy.TYPE_INT);
    testExpression("1", mExpr_1, SimpleExpressionProxy.TYPE_INT);
    testExpression("1", mExpr_1, SimpleExpressionProxy.TYPE_INT);
  }


  //#########################################################################
  //# Unsuccessful Parse Tests
  public void testError_event_1to2()
    throws ParseException
  {
    testExpression("event[1..2]", "'1..2'");
  }

  public void testError_event_1_plus_event_2()
    throws ParseException
  {
    testExpression("event[1] + event[2]", "'event[1]'");
  }


  //#########################################################################
  //# Utilities
  void testExpression(final String text, final SimpleExpressionProxy expr)
    throws ParseException
  {
    final int mask = expr.getResultTypes();
    testExpression(text, expr, mask);
  }

  void testExpression(final String text,
		      final SimpleExpressionProxy expr,
		      final int mask)
    throws ParseException
  {
    try {
      final SimpleExpressionProxy parsed = mParser.parse(text, mask);
      assertTrue("Unexpected result!", parsed.equals(expr));
    } catch (final ParseException exception) {
      final int pos = exception.getPosition();
      System.out.println(text);
      for (int i = 0; i < pos; i++) {
	System.out.print(' ');
      }
      System.out.println('^');
      throw exception;
    }
  }

  void testExpression(final String text, final String culprit)
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
    final List list = new LinkedList();
    mParser = new ExpressionParser();
    mExpr_1 = new IntConstantProxy(1);
    mExpr_2 = new IntConstantProxy(2);
    mExpr_m2 = new IntConstantProxy(-2);
    mExpr_m100 = new IntConstantProxy(-100);
    mExpr_a = new SimpleIdentifierProxy("a");
    mExpr_b = new SimpleIdentifierProxy("b");
    mExpr_c = new SimpleIdentifierProxy("c");
    mExpr_1eqm100 = new EqualsExpressionProxy(mExpr_1, mExpr_m100);
    mExpr_1minusm100 = new MinusExpressionProxy(mExpr_1, mExpr_m100);
    mExpr_1plus1 = new PlusExpressionProxy(mExpr_1, mExpr_1);
    mExpr_1plus1eq2 = new EqualsExpressionProxy(mExpr_1plus1, mExpr_2);
    mExpr_1plus1plus2 = new PlusExpressionProxy(mExpr_1plus1, mExpr_2);
    mExpr_1plusm2 = new PlusExpressionProxy(mExpr_1, mExpr_m2);
    mExpr_1to2 = new IntRangeExpressionProxy(mExpr_1, mExpr_2);
    mExpr_2eq1plus1 = new EqualsExpressionProxy(mExpr_2, mExpr_1plus1);
    mExpr_2minus1 = new MinusExpressionProxy(mExpr_2, mExpr_1);
    mExpr_2minus1minusa = new MinusExpressionProxy(mExpr_2minus1, mExpr_a);
    mExpr_2minus1plusa = new PlusExpressionProxy(mExpr_2minus1, mExpr_a);
    mExpr_2plus1 = new PlusExpressionProxy(mExpr_2, mExpr_1);
    mExpr_2plus1minusa = new MinusExpressionProxy(mExpr_2plus1, mExpr_a);
    mExpr_2plus1plus1 = new PlusExpressionProxy(mExpr_2, mExpr_1plus1);
    mExpr_m100tom2 = new IntRangeExpressionProxy(mExpr_m100, mExpr_m2);
    list.add(mExpr_a);
    list.add(mExpr_b);
    list.add(mExpr_c);
    mExpr_a_b_c = new EnumSetExpressionProxy(list);
    list.clear();
    list.add(mExpr_1);
    mExpr_event_1 = new IndexedIdentifierProxy("event", list);
    list.add(mExpr_1plus1);
    mExpr_event_1_1plus1 = new IndexedIdentifierProxy("event", list);
  }

  protected void tearDown()
  {
    mParser = null;
    mExpr_1 = null;
    mExpr_2 = null;
    mExpr_m2 = null;
    mExpr_m100 = null;
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
    mExpr_a_b_c = null;
    mExpr_event_1 = null;
    mExpr_event_1_1plus1 = null;
  }


  //#########################################################################
  //# Data Members
  private ExpressionParser mParser;
  private SimpleExpressionProxy mExpr_1;
  private SimpleExpressionProxy mExpr_2;
  private SimpleExpressionProxy mExpr_m2;
  private SimpleExpressionProxy mExpr_m100;
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
  private SimpleExpressionProxy mExpr_a;
  private SimpleExpressionProxy mExpr_b;
  private SimpleExpressionProxy mExpr_c;
  private SimpleExpressionProxy mExpr_m100tom2;
  private SimpleExpressionProxy mExpr_a_b_c;
  private SimpleExpressionProxy mExpr_event_1;
  private SimpleExpressionProxy mExpr_event_1_1plus1;

}
