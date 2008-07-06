//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.dnf
//# CLASS:   DNFConverterTest
//###########################################################################
//# $Id: DNFConverterTest.java,v 1.1 2008-06-29 07:13:44 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.compiler.dnf;

import junit.framework.TestCase;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public class DNFConverterTest extends TestCase
{

  //#########################################################################
  //# Successful Converter Tests
  public void testDNF_1()
    throws EvalException, ParseException
  {
    String[][] expected = {{}};
    testDNF("1", expected);
  }

  public void testCNF_1()
    throws EvalException, ParseException
  {
    String[][] expected = {};
    testCNF("1", expected);
  }

  public void testDNF_0()
    throws EvalException, ParseException
  {
    String[][] expected = {};
    testDNF("0", expected);
  }

  public void testCNF_0()
    throws EvalException, ParseException
  {
    String[][] expected = {{}};
    testCNF("0", expected);
  }

  public void testDNF_a_or_xa()
    throws EvalException, ParseException
  {
    String[][] expected = {{}};
    testDNF("a | !a", expected);
  }

  public void testCNF_a_or_xa()
    throws EvalException, ParseException
  {
    String[][] expected = {};
    testCNF("a | !a", expected);
  }

  public void testDNF_a_and_xa()
    throws EvalException, ParseException
  {
    String[][] expected = {};
    testDNF("a & !a", expected);
  }

  public void testCNF_a_and_xa()
    throws EvalException, ParseException
  {
    String[][] expected = {{}};
    testCNF("a & !a", expected);
  }

  public void testDNF_a()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a"}};
    testDNF("a", expected);
  }

  public void testCNF_a()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a"}};
    testCNF("a", expected);
  }

  public void testDNF_xa()
    throws EvalException, ParseException
  {
    String[][] expected = {{"!a"}};
    testDNF("!a", expected);
  }

  public void testCNF_xa()
    throws EvalException, ParseException
  {
    String[][] expected = {{"!a"}};
    testCNF("!a", expected);
  }

  public void testDNF_xxa()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a"}};
    testDNF("!!a", expected);
  }

  public void testCNF_xxa()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a"}};
    testCNF("!!a", expected);
  }

  public void testDNF_a_or_b()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a"}, {"b"}};
    testDNF("a | b", expected);
  }

  public void testCNF_a_or_b()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a", "b"}};
    testCNF("a | b", expected);
  }

  public void testDNF_a_and_b()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a", "b"}};
    testDNF("a & b", expected);
  }

  public void testCNF_a_and_b()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a"}, {"b"}};
    testCNF("a & b", expected);
  }

  public void testDNF_x_a_or_b()
    throws EvalException, ParseException
  {
    String[][] expected = {{"!a", "!b"}};
    testDNF("!(a | b)", expected);
  }

  public void testCNF_x_a_or_b()
    throws EvalException, ParseException
  {
    String[][] expected = {{"!a"}, {"!b"}};
    testCNF("!(a | b)", expected);
  }

  public void testDNF_x_a_and_b()
    throws EvalException, ParseException
  {
    String[][] expected = {{"!a"}, {"!b"}};
    testDNF("!(a & b)", expected);
  }

  public void testCNF_x_a_and_b()
    throws EvalException, ParseException
  {
    String[][] expected = {{"!a", "!b"}};
    testCNF("!(a & b)", expected);
  }

  public void testDNF_a_or_b_or_c()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a"}, {"b"}, {"c"}};
    testDNF("a | b | c", expected);
  }

  public void testCNF_a_or_b_or_c()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a", "b", "c"}};
    testCNF("a | b | c", expected);
  }

  public void testDNF_a_and_b_and_c()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a", "b", "c"}};
    testDNF("a & b & c", expected);
  }

  public void testCNF_a_and_b_and_c()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a"}, {"b"}, {"c"}};
    testCNF("a & b & c", expected);
  }

  public void testDNF_distribute1()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a", "b"}, {"c"}};
    testDNF("a & b | c", expected);
  }

  public void testCNF_distribute1()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a", "c"}, {"b", "c"}};
    testCNF("a & b | c", expected);
  }

  public void testDNF_distribute1r()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a", "b"}, {"c"}};
    testDNF("c | a & b", expected);
  }

  public void testCNF_distribute1r()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a", "c"}, {"b", "c"}};
    testCNF("c | a & b", expected);
  }

  public void testDNF_distribute2()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a", "b"}, {"c", "d"}};
    testDNF("a & b | c & d", expected);
  }

  public void testCNF_distribute2()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a", "c"}, {"a", "d"}, {"b", "c"}, {"b", "d"}};
    testCNF("a & b | c & d", expected);
  }

  public void testDNF_distribute3()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a", "c"}, {"b", "c"}, {"b", "d"}};
    testDNF("(a | b) & (b | c) & (c | d)", expected);
  }

  public void testCNF_distribute3()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a", "b"}, {"b", "c"}, {"c", "d"}};
    testCNF("(a | b) & (b | c) & (c | d)", expected);
  }

  public void testDNF_distribute4()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a", "!b", "e"}, {"a", "c"}, {"a", "d", "e"},
                           {"b", "c"}, {"b", "d", "e"}};
    testDNF("(a | b) & (!b | c | d) & (c | e)", expected);
  }

  public void testCNF_distribute4()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a", "b"}, {"!b", "c", "d"}, {"c", "e"}};
    testCNF("(a | b) & (!b | c | d) & (c | e)", expected);
  }

  public void testDNF_distribute5()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a", "b"}, {"a", "c", "d"}};
    testDNF("a & (b | (c & d))", expected);
  }

  public void testCNF_distribute5()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a"}, {"b", "c"}, {"b", "d"}};
    testCNF("a & (b | (c & d))", expected);
  }

  public void testDNF_distribute6()
    throws EvalException, ParseException
  {
    String[][] expected = {{"!a"}, {"b", "!c"}, {"b", "!d"}};
    testDNF("!(a & (!b | (c & d)))", expected);
  }

  public void testCNF_distribute6()
    throws EvalException, ParseException
  {
    String[][] expected = {{"!a", "b"}, {"!a", "!c", "!d"}};
    testCNF("!(a & (!b | (c & d)))", expected);
  }

  public void testDNF_a_eq_b()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a == b"}};
    testDNF("a == b", expected);
  }

  public void testDNF_a_neq_b()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a != b"}};
    testDNF("a != b", expected);
  }

  public void testDNF_a_leq_b()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a <= b"}};
    testDNF("a <= b", expected);
  }

  public void testDNF_a_lt_b()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a < b"}};
    testDNF("a < b", expected);
  }

  public void testDNF_a_geq_b()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a >= b"}};
    testDNF("a >= b", expected);
  }

  public void testDNF_a_gt_b()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a > b"}};
    testDNF("a > b", expected);
  }

  public void testDNF_x_a_eq_b()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a != b"}};
    testDNF("!(a == b)", expected);
  }

  public void testDNF_x_a_neq_b()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a == b"}};
    testDNF("!(a != b)", expected);
  }

  public void testDNF_x_a_leq_b()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a > b"}};
    testDNF("!(a <= b)", expected);
  }

  public void testDNF_x_a_lt_b()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a >= b"}};
    testDNF("!(a < b)", expected);
  }

  public void testDNF_x_a_geq_b()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a < b"}};
    testDNF("!(a >= b)", expected);
  }

  public void testDNF_x_a_gt_b()
    throws EvalException, ParseException
  {
    String[][] expected = {{"a <= b"}};
    testDNF("!(a > b)", expected);
  }

  public void testReentrant()
    throws EvalException, ParseException
  {
    testCNF_1();
    testDNF_0();
    testCNF_a_or_xa();
    testDNF_a();
    testCNF_xa();
    testDNF_a_or_b();
    testCNF_a_and_b();
    testDNF_1();
    testCNF_0();
    testDNF_a_or_xa();
    testCNF_a();
    testDNF_xa();
    testCNF_a_or_b();
    testDNF_a_and_b();
  }


  //#########################################################################
  //# Utilities
  private void testDNF(final String text,
                       final String[][] expected)
    throws EvalException, ParseException
  {
    final SimpleExpressionProxy expr = mParser.parse(text);
    final CompiledNormalForm result = mConverter.convertToDNF(expr);
    final CompiledNormalForm dnf = parseNF(expected, mOrOp, mAndOp);
    assertEquals("Unexpected result!", dnf, result);
  }

  private void testCNF(final String text,
                       final String[][] expected)
    throws EvalException, ParseException
  {
    final SimpleExpressionProxy expr = mParser.parse(text);
    final CompiledNormalForm result = mConverter.convertToCNF(expr);
    final CompiledNormalForm cnf = parseNF(expected, mAndOp, mOrOp);
    assertEquals("Unexpected result!", cnf, result);
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
  private BinaryOperator mAndOp;
  private BinaryOperator mOrOp;

}
