//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.dnf
//# CLASS:   DNFMinimizerTest
//###########################################################################
//# $Id: DNFMinimizerTest.java,v 1.1 2008-06-29 07:13:44 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.compiler.dnf;

import java.util.ArrayList;
import java.util.Collection;
import junit.framework.TestCase;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public class DNFMinimizerTest extends TestCase
{

  //#########################################################################
  //# Successful Minimizer Tests
  public void testMinimizer_1()
    throws EvalException, ParseException
  {
    final String[][] expected = {{}};
    testMinimizer("1", expected);
  }

  public void testMinimizer_0()
    throws EvalException, ParseException
  {
    final String[][] expected = {};
    testMinimizer("0", expected);
  }

  public void testMinimizer_a_or_xa()
    throws EvalException, ParseException
  {
    final String[][] expected = {{}};
    testMinimizer("a | !a", expected);
  }

  public void testMinimizer_a_and_xa()
    throws EvalException, ParseException
  {
    final String[][] expected = {};
    testMinimizer("a & !a", expected);
  }

  public void testMinimizer_a()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a"}};
    testMinimizer("a", expected);
  }

  public void testMinimizer_xa()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"!a"}};
    testMinimizer("!a", expected);
  }

  public void testMinimizer_xxa()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a"}};
    testMinimizer("!!a", expected);
  }

  public void testMinimizer_a_or_b()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a"}, {"b"}};
    testMinimizer("a | b", expected);
  }

  public void testMinimizer_a_and_b()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a", "b"}};
    testMinimizer("a & b", expected);
  }

  public void testMinimizer_x_a_or_b()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"!a", "!b"}};
    testMinimizer("!(a | b)", expected);
  }

  public void testMinimizer_x_a_and_b()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"!a"}, {"!b"}};
    testMinimizer("!(a & b)", expected);
  }

  public void testMinimizer_a_or_b_or_c()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a"}, {"b"}, {"c"}};
    testMinimizer("a | b | c", expected);
  }

  public void testMinimizer_a_and_b_and_c()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a", "b", "c"}};
    testMinimizer("a & b & c", expected);
  }

  public void testMinimizer_distribute1()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a", "b"}, {"c"}};
    testMinimizer("a & b | c", expected);
  }

  public void testMinimizer_distribute1r()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a", "b"}, {"c"}};
    testMinimizer("c | a & b", expected);
  }

  public void testMinimizer_distribute2()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a", "b"}, {"c", "d"}};
    testMinimizer("a & b | c & d", expected);
  }

  public void testMinimizer_distribute3()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a", "c"}, {"b", "c"}, {"b", "d"}};
    testMinimizer("(a | b) & (b | c) & (c | d)", expected);
  }

  public void testMinimizer_distribute4()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a", "!b", "e"}, {"a", "c"}, {"a", "d", "e"},
                           {"b", "c"}, {"b", "d", "e"}};
    testMinimizer("(a | b) & (!b | c | d) & (c | e)", expected);
  }

  public void testMinimizer_distribute5()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a", "b"}, {"a", "c", "d"}};
    testMinimizer("a & (b | (c & d))", expected);
  }

  public void testMinimizer_distribute6()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"!a"}, {"b", "!c"}, {"b", "!d"}};
    testMinimizer("!(a & (!b | (c & d)))", expected);
  }

  public void testMinimizer_a_eq_b()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a == b"}};
    testMinimizer("a == b", expected);
  }

  public void testMinimizer_a_neq_b()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a != b"}};
    testMinimizer("a != b", expected);
  }

  public void testMinimizer_a_leq_b()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a <= b"}};
    testMinimizer("a <= b", expected);
  }

  public void testMinimizer_a_lt_b()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a < b"}};
    testMinimizer("a < b", expected);
  }

  public void testMinimizer_a_geq_b()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"b <= a"}};
    testMinimizer("a >= b", expected);
  }

  public void testMinimizer_a_gt_b()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"b < a"}};
    testMinimizer("a > b", expected);
  }

  public void testMinimizer_x_a_eq_b()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a != b"}};
    testMinimizer("!(a == b)", expected);
  }

  public void testMinimizer_x_a_neq_b()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a == b"}};
    testMinimizer("!(a != b)", expected);
  }

  public void testMinimizer_x_a_leq_b()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"b < a"}};
    testMinimizer("!(a <= b)", expected);
  }

  public void testMinimizer_x_a_lt_b()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"b <= a"}};
    testMinimizer("!(a < b)", expected);
  }

  public void testMinimizer_x_a_geq_b()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a < b"}};
    testMinimizer("!(a >= b)", expected);
  }

  public void testMinimizer_x_a_gt_b()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a <= b"}};
    testMinimizer("!(a > b)", expected);
  }

  public void testMinimizer_exercise1()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a"}, {"b"}};
    testMinimizer("a & !b | a & b | b & !a", expected);
  }

  public void testMinimizer_exercise2()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a", "b"}, {"!b", "c"}};
    testMinimizer("a & b | a & c | !b & c", expected);
  }

  public void testMinimizer_exercise2a()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a", "b"}, {"!b", "c"}};
    testMinimizer("a & b | !b & c", expected);
  }

  public void testMinimizer_exercise3()
    throws EvalException, ParseException
  {
    final String[][] expected = {{"a", "b"}, {"!b", "c"}};
    testMinimizer
      ("a & b & !c | a & b & c | a & !b & c | !a & !b & c", expected);
  }

  public void testMinimizer_exercise4()
    throws EvalException, ParseException
  {
    final String[][] expected1 = {{"a", "!b"}, {"!a", "b"}, {"a", "c"}};
    final String[][] expected2 = {{"a", "!b"}, {"!a", "b"}, {"b", "c"}};
    final Collection<String[][]> expected = new ArrayList<String[][]>(2);
    expected.add(expected1);
    expected.add(expected2);
    testMinimizer
      ("a & !b & !c | !a & b & !c | a & b & c | a & !b & c | !a & b & c",
       expected);
  }

  public void testReentrant()
    throws EvalException, ParseException
  {
    testMinimizer_1();
    testMinimizer_0();
    testMinimizer_a_or_xa();
    testMinimizer_a();
    testMinimizer_xa();
    testMinimizer_a_or_b();
    testMinimizer_a_and_b();
    testMinimizer_1();
    testMinimizer_0();
    testMinimizer_a_or_xa();
    testMinimizer_a();
    testMinimizer_xa();
    testMinimizer_a_or_b();
    testMinimizer_a_and_b();
  }


  //#########################################################################
  //# Utilities
  private void testMinimizer(final String text,
                             final Collection<String[][]> expected)
    throws EvalException, ParseException
  {
    final SimpleExpressionProxy expr = mParser.parse(text);
    final CompiledNormalForm conv = mConverter.convertToDNF(expr);
    final CompiledNormalForm min = mMinimizer.minimize(conv); 
    final Collection<CompiledNormalForm> dnfs =
      new ArrayList<CompiledNormalForm>(expected.size());
    for (final String[][] matrix : expected) {
      final CompiledNormalForm dnf = parseNF(matrix, mOrOp, mAndOp);
      dnfs.add(dnf);
    }
    assertTrue("Unexpected result " + min + "!", dnfs.contains(min));
  }

  private void testMinimizer(final String text,
                             final String[][] expected)
    throws EvalException, ParseException
  {
    final SimpleExpressionProxy expr = mParser.parse(text);
    final CompiledNormalForm conv = mConverter.convertToDNF(expr);
    final CompiledNormalForm min = mMinimizer.minimize(conv); 
    final CompiledNormalForm dnf = parseNF(expected, mOrOp, mAndOp);
    assertEquals("Unexpected result!", dnf, min);
  }

  private CompiledNormalForm parseNF(final String[][] matrix,
                                     final BinaryOperator op1,
                                     final BinaryOperator op2)
    throws ParseException
  {
    final CompiledNormalForm nf = new CompiledNormalForm(op1);
    for (final String[] row : matrix) {
      final CompiledClause clause = new CompiledClause(op2);
      for (final String text : row) {
        final SimpleExpressionProxy literal = mParser.parse(text);
        clause.add(literal);
      }
      nf.add(clause);
    }
    return nf;
  }
        

  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
  {
    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    mAndOp = optable.getAndOperator();
    mOrOp = optable.getOrOperator();
    mParser = new ExpressionParser(factory, optable);
    mConverter = new DNFConverter(factory, optable);
    mMinimizer = new DNFMinimizer(mConverter, optable);
  }

  protected void tearDown()
  {
    mParser = null;
    mConverter = null;
    mAndOp = null;
    mOrOp = null;
  }


  //#########################################################################
  //# Data Members
  private ExpressionParser mParser;
  private DNFConverter mConverter;
  private DNFMinimizer mMinimizer;
  private BinaryOperator mAndOp;
  private BinaryOperator mOrOp;

}
