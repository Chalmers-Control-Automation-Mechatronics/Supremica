//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.model.compiler.context;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


public class PrimePreservingConditionCompilerTest
  extends AbstractExpressionCompilerTest
{

  //#########################################################################
  //# Test Cases
  public void testSimplify_keep_alone()
    throws EvalException, ParseException
  {
    final String input = "a' == a'";
    testSimplify(input, input);
  }

  public void testSimplify_keep_left()
    throws EvalException, ParseException
  {
    final String input = "a' == a' & b > 0";
    testSimplify(input, input);
  }

  public void testSimplify_keep_nested_or()
    throws EvalException, ParseException
  {
    final String input = "a' == a' | b' == b'";
    final String expected = "a' == a' & b' == b'";
    testSimplify(input, expected);
  }

  public void testSimplify_keep_right()
    throws EvalException, ParseException
  {
    final String input = "b > 0 & a' == a'";
    testSimplify(input, input);
  }

  public void testSimplify_keep_some()
    throws EvalException, ParseException
  {
    final String input = "a' == a' & (b = a) & b' == b'";
    final String expected = "a' == a' & (b = a)";
    testSimplify(input, expected);
  }


  public void testSimplify_redundant_assign()
    throws EvalException, ParseException
  {
    final String input = "(a = 0) & a' == a'";
    final String expected = "a = 0";
    testSimplify(input, expected);
  }

  public void testSimplify_redundant_dup()
    throws EvalException, ParseException
  {
    final String input = "a' == a' & a' == a'";
    final String expected = "a' == a'";
    testSimplify(input, expected);
  }

  public void testSimplify_redundant_dup_assign()
    throws EvalException, ParseException
  {
    final String input = "a' == a' & (b += a) & a' == a' & b' == b'";
    final String expected = "a' == a' & (b += a)";
    testSimplify(input, expected);
  }

  public void testSimplify_redundant_eq()
    throws EvalException, ParseException
  {
    final String input = "a' == 0 & a' == a'";
    final String expected = "a' == 0";
    testSimplify(input, expected);
  }


  public void testSimplify_rewrite_leq()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 10);
    addVariable("x'", range);
    final String input = "x' >= 0";
    final String expected = "x' == x'";
    testSimplify(input, expected);
  }

  public void testSimplify_rewrite_neq()
    throws EvalException, ParseException
  {
    final String input = "x' != x' & a > b";
    final String expected = "0";
    testSimplify(input, expected);
  }


  public void testSimplify_simplify_ga()
    throws EvalException, ParseException
  {
    final String input = "(a = 0) & (b > 1 + 1) & (b += a)";
    final String expected = "(a = 0) & (b > 2) & (b += a)";
    testSimplify(input, expected);
  }


  public void testSimplify_unchanged_assign1()
    throws EvalException, ParseException
  {
    final String input = "a = 0";
    testSimplify(input, input);
  }

  public void testSimplify_unchanged_assign2()
    throws EvalException, ParseException
  {
    final String input = "(a = 0) | (b = a)";
    testSimplify(input, input);
  }

  public void testSimplify_unchanged_ga()
    throws EvalException, ParseException
  {
    final String input = "(a = 0) & (b > 2) & (b += a)";
    testSimplify(input, input);
  }


  public void testSimplify_var_boolean()
    throws EvalException, ParseException
  {
    addBooleanVariable("a'");
    final String input = "a";
    testSimplify(input, input);
  }

  public void testSimplify_var_enum()
    throws EvalException, ParseException
  {
    final CompiledEnumRange range =
      createEnumRange(new String[] {"ok", "nok"});
    addVariable("crc", range);
    final String input = "crc==nok";
    testSimplify(input, input);
  }

  public void testSimplify_var_int()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 10);
    addVariable("x", range);
    final String input = "x<x";
    final String expected = "0";
    testSimplify(input, expected);
  }

  public void testSimplify_var_undef()
    throws EvalException, ParseException
  {
    final String input = "x >= 0";
    testSimplify(input, input);
  }


  public void testReentrant()
    throws EvalException, ParseException
  {
    testSimplify_var_undef();
    resetContext();
    testSimplify_rewrite_leq();
    resetContext();
    testSimplify_var_enum();
    resetContext();
    testSimplify_var_int();
    resetContext();
    testSimplify_var_undef();
    resetContext();
    testSimplify_redundant_dup_assign();
    resetContext();
    testSimplify_var_enum();
  }


  //#########################################################################
  //# Utilities
  private void testSimplify(final String input, final String expected)
    throws EvalException, ParseException
  {
    final ExpressionParser parser = getExpressionParser();
    final SimpleExpressionProxy inExpr = parser.parse(input);
    final SimpleExpressionProxy expectedExpr = parser.parse(expected);
    final BindingContext context = getContext();
    final SimpleExpressionProxy result = mCompiler.simplify(inExpr, context);
    if (!mEquality.equals(result, expectedExpr)) {
      final String msg =
        "Wrong output from prime preserving condition compiler! " +
        "Expected: " + expected + ", but was: " + result;
      fail(msg);
    }
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp()
  {
    super.setUp();
    final ModuleProxyFactory factory = getFactory();
    final CompilerOperatorTable optable = getOperatorTable();
    final CompilationInfo info = new CompilationInfo(false, false);
    mCompiler =
      new PrimePreservingConditionCompiler(factory, optable, info, false);
    mEquality = new ModuleEqualityVisitor(false);
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mCompiler = null;
    mEquality = null;
  }


  //#########################################################################
  //# Data Members
  private PrimePreservingConditionCompiler mCompiler;
  private ModuleEqualityVisitor mEquality;

}
