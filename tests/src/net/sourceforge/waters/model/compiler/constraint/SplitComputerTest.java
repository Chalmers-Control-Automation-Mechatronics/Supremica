//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   SplitComputerTest
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.model.compiler.constraint;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.CompiledEnumRange;
import net.sourceforge.waters.model.compiler.context.CompiledIntRange;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public class SplitComputerTest extends TestCase
{

  //#########################################################################
  //# Successful Converter Tests
  public void testPropose_empty()
    throws EvalException, ParseException
  {
    final String[] constraints = {};
    testPropose(constraints, null);
  }

  public void testPropose_a()
    throws EvalException, ParseException
  {
    addBooleanVariable("a");
    final String[] constraints = {"a"};
    testPropose(constraints, null);
  }

  public void testPropose_a_and_b()
    throws EvalException, ParseException
  {
    addBooleanVariable("a");
    addBooleanVariable("b");
    final String[] constraints = {"a", "b"};
    testPropose(constraints, null);
  }

  public void testPropose_a_or_b()
    throws EvalException, ParseException
  {
    addBooleanVariable("a");
    addBooleanVariable("b");
    final String[] constraints = {"a | b"};
    final String[] expected = {"!a", "a"};
    testPropose(constraints, expected);
  }

  public void testPropose_b_or_a()
    throws EvalException, ParseException
  {
    addBooleanVariable("a");
    addBooleanVariable("b");
    final String[] constraints = {"b | a"};
    final String[] expected = {"!a", "a"};
    testPropose(constraints, expected);
  }

  public void testPropose_c_or_d_or_b()
    throws EvalException, ParseException
  {
    addBooleanVariable("b");
    addBooleanVariable("c");
    addBooleanVariable("d");
    final String[] constraints = {"c | d | b"};
    final String[] expected = {"!b", "b"};
    testPropose(constraints, expected);
  }

  public void testPropose_disjunction_1()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("a", range);
    addVariable("a'", range);
    addVariable("b", range);
    addVariable("b'", range);
    addVariable("c", range);
    final String[] constraints = {"a==a' | b==b'", "a==b+c"};
    final String[] expected = {"a==a'", "b==b'"};
    testPropose(constraints, expected);
  }

  public void testPropose_disjunction_2()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("a", range);
    addVariable("a'", range);
    addVariable("b", range);
    addVariable("b'", range);
    addVariable("c", range);
    addVariable("c'", range);
    addVariable("d", range);
    final String[] constraints = {"a==a' | b==b' | c>c'", "d==a+b+c"};
    final String[] expected = {"a==a'", "b==b'", "c>c'"};
    testPropose(constraints, expected);
  }

  public void testPropose_crc_1()
    throws EvalException, ParseException
  {
    final CompiledEnumRange range =
      createEnumRange(new String[] {"ok", "nok"});
    addVariable("crc", range);
    addBooleanVariable("trouble");
    final String[] constraints = {"crc==nok", "crc==ok | trouble"};
    final String[] expected = {"crc==ok", "crc==nok"};
    testPropose(constraints, expected);
  }

  public void testPropose_intrange_1()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("x", range);
    addVariable("y", range);
    final String[] constraints = {"x>1 & x==y & y<=2"};
    final String[] expected = {"x==0", "x==1", "x==2", "x==3", "x==4", "x==5"};
    testPropose(constraints, expected);
  }

  public void testPropose_balllift_1()
    throws EvalException, ParseException
  {
    addBooleanVariable("qUp");
    addBooleanVariable("qUp'");
    addBooleanVariable("c_iBallDn");
    addBooleanVariable("c_iBallUp");
    final String[] constraints = {"qUp'", "c_iBallDn | qUp", "c_iBallUp"};
    final String[] expected = {"!c_iBallDn", "c_iBallDn"};
    testPropose(constraints, expected);
  }

  public void testPropose_balllift_2()
    throws EvalException, ParseException
  {
    addBooleanVariable("qUp");
    addBooleanVariable("qUp'");
    addBooleanVariable("c_iBallDn");
    addBooleanVariable("c_iBallUp");
    final String[] constraints = {"!qUp'", "!c_iBallUp | !(qUp | c_iBallDn)"};
    final String[] expected = {"!c_iBallDn", "c_iBallDn"};
    testPropose(constraints, expected);
  }

  public void testPropose_profisafe_1()
    throws EvalException, ParseException
  {
    final CompiledIntRange seqno = createIntRange(0, 5);
    addVariable("in_cons_num", seqno);
    addVariable("out_cons_num", seqno);
    final CompiledEnumRange crc = createEnumRange(new String[] {"ok", "nok"});
    addVariable("in_CRC", crc);
    addBooleanVariable("fs_master_bit2_CRCNO");
    addBooleanVariable("fs_master_bit2_CRCNO'");
    final String[] constraints =
      {"fs_master_bit2_CRCNO' == 0",
       "fs_master_bit2_CRCNO' == (in_cons_num!=out_cons_num | in_CRC==nok)"};
    final String[] expected =
      {"in_CRC == ok", "in_CRC == nok"};
    testPropose(constraints, expected);
  }

  public void testReentrant()
    throws EvalException, ParseException
  {
    testPropose_a();
    testPropose_a_and_b();
    testPropose_c_or_d_or_b();
    testPropose_crc_1();
    testPropose_intrange_1();
  }


  //#########################################################################
  //# Utilities
  private void addAtom(final String name)
    throws ParseException
  {
    final IdentifierProxy ident = mParser.parseIdentifier(name);
    mContext.addAtom(ident);
  }

  private void addBooleanVariable(final String name)
    throws ParseException
  {
    addVariable(name, BOOLEAN_RANGE);
  }

  private CompiledIntRange createIntRange(final int lower, final int upper)
  {
    return new CompiledIntRange(lower, upper);
  }

  private CompiledEnumRange createEnumRange(final String[] names)
    throws ParseException
  {
    final List<IdentifierProxy> list =
      new ArrayList<IdentifierProxy>(names.length);
    for (final String name : names) {
      final IdentifierProxy ident = mParser.parseIdentifier(name);
      mContext.addAtom(ident);
      list.add(ident);
    }
    return new CompiledEnumRange(list);
  }

  private void addVariable(final String name, final CompiledRange range)
    throws ParseException
  {
    final SimpleExpressionProxy varname = mParser.parse(name);
    mContext.addVariable(varname, range);
  }

  private void testPropose(final String[] inputs, final String[] outputs)
    throws EvalException, ParseException
  {
    final ConstraintList constraints = parse(inputs);
    // System.err.println(constraints);
    final ConstraintList expected = parse(outputs);
    final boolean changed = !constraints.equals(expected);
    final SplitCandidate split =
      mSplitComputer.proposeSplit(constraints, mContext);
    if (split == null) {
      assertNull
        ("Wrong output from split computer: got NULL but should have been " +
         expected + "!", expected);
    } else {
      final List<SimpleExpressionProxy> splitlist =
        split.getSplitExpressions(mFactory, mOperatorTable);
      final ConstraintList splitconstraints =
        new ConstraintList(splitlist);
      assertTrue("Wrong output from split computer: got " +
                 splitconstraints + " but should have been " + expected + "!",
                 splitconstraints.equals(expected));
    }
  }

  private ConstraintList parse(final String[] inputs)
    throws ParseException
  {
    if (inputs == null) {
      return null;
    } else {
      final List<SimpleExpressionProxy> list =
        new ArrayList<SimpleExpressionProxy>(inputs.length);
      for (final String input : inputs) {
        final SimpleExpressionProxy expr = mParser.parse(input);
        list.add(expr);
      }
      return new ConstraintList(list);
    }
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
  {
    mFactory = ModuleElementFactory.getInstance();
    mOperatorTable = CompilerOperatorTable.getInstance();
    mContext = new DummyContext();
    mParser = new ExpressionParser(mFactory, mOperatorTable);
    mSplitComputer = new SplitComputer(mFactory, mOperatorTable, mContext);
  }

  protected void tearDown()
  {
    mFactory = null;
    mOperatorTable = null;
    mContext = null;
    mParser = null;
    mSplitComputer = null;
  }


  //#########################################################################
  //# Data Members
  private ModuleProxyFactory mFactory;
  private CompilerOperatorTable mOperatorTable;
  private DummyContext mContext;
  private ExpressionParser mParser;
  private SplitComputer mSplitComputer;


  //#########################################################################
  //# Class Constants
  private static final CompiledIntRange BOOLEAN_RANGE =
    new CompiledIntRange(0, 1);

}
