//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   ConstraintPropagatorTest
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.model.compiler.constraint;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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


public class ConstraintPropagatorTest extends TestCase
{

  //#########################################################################
  //# Successful Converter Tests
  public void testPropagate_a()
    throws EvalException, ParseException
  {
    final String[] constraints = {"a"};
    testPropagate(constraints, constraints);
  }

  public void testPropagate_not_a()
    throws EvalException, ParseException
  {
    final String[] constraints = {"!a"};
    testPropagate(constraints, constraints);
  }

  public void testPropagate_not_not_a()
    throws EvalException, ParseException
  {
    final String[] constraints = {"!!a"};
    final String[] expected = {"a"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_a_and_b()
    throws EvalException, ParseException
  {
    final String[] constraints = {"a & b"};
    final String[] expected = {"a", "b"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_a_or_b()
    throws EvalException, ParseException
  {
    final String[] constraints = {"a | b"};
    testPropagate(constraints, constraints);
  }

  public void testPropagate_b_or_a()
    throws EvalException, ParseException
  {
    final String[] constraints = {"b | a"};
    final String[] expected = {"a | b"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_b_or_c_or_a()
    throws EvalException, ParseException
  {
    final String[] constraints = {"b | c | a"};
    final String[] expected = {"a | b | c"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_not_a_or_b()
    throws EvalException, ParseException
  {
    final String[] constraints = {"!(a | b)"};
    final String[] expected = {"!a", "!b"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_not_a_or_not_b()
    throws EvalException, ParseException
  {
    final String[] constraints = {"!(a | !b)"};
    final String[] expected = {"!a", "b"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_not_a_and_b()
    throws EvalException, ParseException
  {
    final String[] constraints = {"!(a & b)"};
    final String[] expected = {"!a | !b"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_a_and_aprime()
    throws EvalException, ParseException
  {
    addBooleanVariable("a");
    final String[] constraints = {"a", "a'"};
    testPropagate(constraints, constraints);
  }

  public void testPropagate_a_and_notaprime()
    throws EvalException, ParseException
  {
    addBooleanVariable("a");
    final String[] constraints = {"a", "!a'"};
    testPropagate(constraints, constraints);
  }

  public void testPropagate_negliteral_1()
    throws EvalException, ParseException
  {
    addBooleanVariable("a");
    addBooleanVariable("b");
    final String[] constraints = {"!a", "a==b"};
    final String[] expected = {"!a", "!b"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_negliteral_2()
    throws EvalException, ParseException
  {
    addBooleanVariable("a");
    addBooleanVariable("b'");
    final String[] constraints = {"!b'", "a==b'"};
    final String[] expected = {"!a", "!b'"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_enumvar()
    throws EvalException, ParseException
  {
    final CompiledEnumRange range =
      createEnumRange(new String[] {"a", "b", "c"});
    addVariable("enumvar", range);
    final String[] constraints = {"a==enumvar"};
    final String[] expected = {"enumvar==a"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_crc_1()
    throws EvalException, ParseException
  {
    final CompiledEnumRange range =
      createEnumRange(new String[] {"ok", "nok"});
    addVariable("crc", range);
    final String[] constraints = {"crc==nok", "crc==ok | trouble"};
    final String[] expected = {"crc==nok", "trouble"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_crc_2()
    throws EvalException, ParseException
  {
    final CompiledEnumRange range =
      createEnumRange(new String[] {"ok", "nok"});
    addVariable("crc", range);
    final String[] constraints = {"crc!=ok", "crc==ok | trouble"};
    final String[] expected = {"crc==nok", "trouble"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_intrange_1()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("x", range);
    final String[] constraints = {"x<=5"};
    final String[] expected = {};
    testPropagate(constraints, expected);
  }

  public void testPropagate_intrange_2()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("x", range);
    final String[] constraints = {"x>5"};
    testPropagate(constraints, null);
  }

  public void testPropagate_intrange_3()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("x", range);
    final String[] constraints = {"x>=2 & x<3"};
    final String[] expected = {"x==2"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_intrange_4()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("x", range);
    final String[] constraints = {"x>1 & x<3"};
    final String[] expected = {"x==2"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_intrange_5()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("x", range);
    final String[] constraints = {"x>1 & x<1"};
    testPropagate(constraints, null);
  }

  public void testPropagate_intrange_6()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("x", range);
    final String[] constraints = {"x<4 & x<2"};
    final String[] expected = {"x<=1"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_intrange_7()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 6);
    addVariable("x", range);
    final String[] constraints = {"x>4 & x>2"};
    final String[] expected = {"5<=x"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_intrange_8()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("x", range);
    addVariable("y", range);
    final String[] constraints = {"x>1 & x==y & y<=2"};
    final String[] expected = {"x==2", "y==2"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_boolrange_1()
    throws EvalException, ParseException
  {
    addBooleanVariable("x'");
    final String[] constraints = {"x'==0"};
    final String[] expected = {"!x'"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_boolrange_2()
    throws EvalException, ParseException
  {
    addBooleanVariable("x'");
    final String[] constraints = {"x'==1"};
    final String[] expected = {"x'"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_boolrange_3()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 2);
    addVariable("x'", range);
    final String[] constraints = {"x'==1"};
    final String[] expected = {"x'==1"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_boolrange_4()
    throws EvalException, ParseException
  {
    final String[] constraints = {"xx'==0"};
    final String[] expected = {"xx'==0"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_balllift_1()
    throws EvalException, ParseException
  {
    addBooleanVariable("qUp");
    addBooleanVariable("qUp'");
    addBooleanVariable("c_iBallDn");
    addBooleanVariable("c_iBallUp");
    final String[] constraints = {"qUp'",
                                  "qUp' == ((qUp | c_iBallDn) & c_iBallUp)"};
    final String[] expected = {"qUp'", "c_iBallDn | qUp", "c_iBallUp"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_balllift_2()
    throws EvalException, ParseException
  {
    addBooleanVariable("qUp");
    addBooleanVariable("qUp'");
    addBooleanVariable("c_iBallDn");
    addBooleanVariable("c_iBallUp");
    final String[] constraints = {"!qUp'",
                                  "qUp' == ((qUp | c_iBallDn) & c_iBallUp)"};
    final String[] expected = {"!qUp'", "!c_iBallUp | !(qUp | c_iBallDn)"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_balllift_3()
    throws EvalException, ParseException
  {
    addBooleanVariable("qOut");
    addBooleanVariable("qOut'");
    addBooleanVariable("qUp");
    addBooleanVariable("qUp'");
    addBooleanVariable("c_iBallDn");
    final String[] constraints = {"!c_iBallDn",
                                  "qUp' == ((qUp|c_iBallDn) & qOut')"};
    final String[] expected = {"!c_iBallDn", "qUp' == (qUp & qOut')"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_balllift_4()
    throws EvalException, ParseException
  {
    addBooleanVariable("qOut");
    addBooleanVariable("qOut'");
    addBooleanVariable("qUp");
    addBooleanVariable("qUp'");
    addBooleanVariable("c_iBallDn");
    addBooleanVariable("c_iBallDn'");
    addBooleanVariable("iBallDn");
    final String[] constraints = {"!c_iBallDn",
                                  "iBallDn == c_iBallDn'",
                                  "qUp' == ((qUp|c_iBallDn) & qOut')"};
    final String[] expected = {"!c_iBallDn",
                               "c_iBallDn' == iBallDn",
                               "qUp' == (qUp & qOut')"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_balllift_5()
    throws EvalException, ParseException
  {
    addBooleanVariable("qOut");
    addBooleanVariable("qOut'");
    addBooleanVariable("qUp");
    addBooleanVariable("qUp'");
    addBooleanVariable("c_iBallDn");
    addBooleanVariable("c_iBallDn'");
    addBooleanVariable("c_iBallUp");
    addBooleanVariable("c_iBallUp'");
    addBooleanVariable("iBallDn");
    addBooleanVariable("iBallUp");
    final String[] constraints = {"c_iBallUp==qOut'",
                                  "iBallDn==c_iBallDn'",
                                  "iBallUp==c_iBallUp'",
                                  "qUp'==((qUp|c_iBallDn)&qOut')",
                                  "!c_iBallDn'"};
    final String[] expected = {"qOut'==c_iBallUp",
                               "!iBallDn",
                               "c_iBallUp'==iBallUp",
                               "qUp'==((qUp|c_iBallDn)&c_iBallUp)",
                               "!c_iBallDn'"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_balllift_5a()
    throws EvalException, ParseException
  {
    addBooleanVariable("qOut");
    addBooleanVariable("qOut'");
    addBooleanVariable("qUp");
    addBooleanVariable("qUp'");
    addBooleanVariable("c_iBallDn");
    addBooleanVariable("c_iBallDn'");
    addBooleanVariable("c_iBallUp");
    addBooleanVariable("c_iBallUp'");
    addBooleanVariable("iBallDn");
    addBooleanVariable("iBallUp");
    final String[] constraints1 = {"c_iBallUp==qOut'",
                                   "iBallDn==c_iBallDn'",
                                   "iBallUp==c_iBallUp'",
                                   "qUp'==((qUp|c_iBallDn)&qOut')"};
    final String[] constraints2 = {"!c_iBallDn'"};
    final String[] expected = {"qOut'==c_iBallUp",
                               "!iBallDn",
                               "c_iBallUp'==iBallUp",
                               "qUp'==((qUp|c_iBallDn)&c_iBallUp)",
                               "!c_iBallDn'"};
    testPropagate(constraints1, constraints2, expected);
  }

  public void testPropagate_profisafe_1()
    throws EvalException, ParseException
  {
    final CompiledIntRange seqno = createIntRange(0, 5);
    addVariable("in_cons_num", seqno);
    addVariable("out_cons_num", seqno);
    final CompiledEnumRange crc = createEnumRange(new String[] {"ok", "nok"});
    addVariable("in_CRC", crc);
    addBooleanVariable("fs_master_bit2_CRCNO'");
    final String[] constraints =
      {"fs_master_bit2_CRCNO' == 0",
       "fs_master_bit2_CRCNO' == (in_cons_num!=out_cons_num | in_CRC==nok)"};
    final String[] expected =
      {"!fs_master_bit2_CRCNO'",
       "in_cons_num == out_cons_num",
       "in_CRC == ok"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_profisafe_2()
    throws EvalException, ParseException
  {
    final CompiledIntRange seqno = createIntRange(0, 5);
    addVariable("in_cons_num", seqno);
    addVariable("out_cons_num", seqno);
    final CompiledEnumRange crc = createEnumRange(new String[] {"ok", "nok"});
    addVariable("in_CRC", crc);
    addBooleanVariable("fs_master_bit2_CRCNO'");
    final String[] constraints =
      {"fs_master_bit2_CRCNO' == 1",
       "in_CRC == ok",
       "fs_master_bit2_CRCNO' == (in_cons_num!=out_cons_num | in_CRC==nok)"};
    final String[] expected =
      {"fs_master_bit2_CRCNO'",
       "in_CRC == ok",
       "in_cons_num!=out_cons_num"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_profisafe_3()
    throws EvalException, ParseException
  {
    final CompiledIntRange seqno = createIntRange(0, 5);
    addVariable("in_cons_num", seqno);
    addVariable("out_cons_num", seqno);
    final CompiledEnumRange crc = createEnumRange(new String[] {"ok", "nok"});
    addVariable("in_CRC", crc);
    addBooleanVariable("fs_master_bit2_CRCNO'");
    final String[] constraints =
      {"fs_master_bit2_CRCNO' == 1",
       "in_CRC == nok",
       "fs_master_bit2_CRCNO' == (in_cons_num!=out_cons_num | in_CRC==nok)"};
    final String[] expected =
      {"fs_master_bit2_CRCNO'",
       "in_CRC == nok"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_profisafe_4()
    throws EvalException, ParseException
  {
    final CompiledIntRange seqno = createIntRange(0, 5);
    addVariable("in_cons_num", seqno);
    addVariable("out_cons_num", seqno);
    final CompiledEnumRange crc = createEnumRange(new String[] {"ok", "nok"});
    addVariable("in_CRC", crc);
    addBooleanVariable("fs_master_bit2_CRCNO'");
    final String[] constraints =
      {"fs_master_bit2_CRCNO' == 0",
       "in_CRC == ok",
       "fs_master_bit2_CRCNO' == (in_cons_num!=out_cons_num) | in_CRC==nok"};
    final String[] expected =
      {"!fs_master_bit2_CRCNO'",
       "in_cons_num == out_cons_num",
       "in_CRC == ok"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_profisafe_5()
    throws EvalException, ParseException
  {
    final CompiledIntRange seqno = createIntRange(0, 5);
    addVariable("in_cons_num", seqno);
    addVariable("out_cons_num", seqno);
    final CompiledEnumRange crc = createEnumRange(new String[] {"ok", "nok"});
    addVariable("in_CRC", crc);
    addBooleanVariable("fs_master_bit2_CRCNO'");
    final String[] constraints =
      {"fs_master_bit2_CRCNO' == (1!=out_cons_num)",
       "out_cons_num!=1",
       "fs_master_bit2_CRCNO'"};
    final String[] expected =
      {"out_cons_num!=1",
       "fs_master_bit2_CRCNO'"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_profisafe_6()
    throws EvalException, ParseException
  {
    final CompiledIntRange seqno = createIntRange(0, 5);
    addVariable("in_cons_num", seqno);
    addVariable("out_cons_num", seqno);
    final CompiledEnumRange crc = createEnumRange(new String[] {"ok", "nok"});
    addVariable("in_CRC", crc);
    addBooleanVariable("fs_master_bit2_CRCNO'");
    final String[] constraints =
      {"fs_master_bit2_CRCNO' == (1!=out_cons_num)",
       "out_cons_num!=1",
       "!fs_master_bit2_CRCNO'"};
    testPropagate(constraints, null);
  }

  public void testPropagate_profisafe_7()
    throws EvalException, ParseException
  {
    final CompiledIntRange seqno = createIntRange(0, 5);
    addVariable("in_cons_num", seqno);
    addVariable("out_cons_num", seqno);
    final CompiledEnumRange crc = createEnumRange(new String[] {"ok", "nok"});
    addVariable("in_CRC", crc);
    addBooleanVariable("fs_master_bit2_CRCNO'");
    final String[] constraints1 =
      {"fs_master_bit2_CRCNO' == (1!=out_cons_num)",
       "out_cons_num!=1"};
    final String[] constraints2 =
      {"fs_master_bit2_CRCNO'"};
    final String[] expected =
      {"out_cons_num!=1",
       "fs_master_bit2_CRCNO'"};
    testPropagate(constraints1, constraints2,
                  expected, "fs_master_bit2_CRCNO'");
  }

  public void testPropagate_profisafe_8()
    throws EvalException, ParseException
  {
    final CompiledIntRange seqno = createIntRange(0, 5);
    addVariable("in_cons_num", seqno);
    addVariable("out_cons_num", seqno);
    final CompiledEnumRange crc = createEnumRange(new String[] {"ok", "nok"});
    addVariable("in_CRC", crc);
    addBooleanVariable("fs_master_bit2_CRCNO'");
    final String[] constraints1 =
      {"fs_master_bit2_CRCNO' == (1!=out_cons_num)",
       "out_cons_num!=1"};
    final String[] constraints2 =
      {"!fs_master_bit2_CRCNO'"};
    testPropagate(constraints1, constraints2, null, "fs_master_bit2_CRCNO'");
  }

  public void testReentrant()
    throws EvalException, ParseException
  {
    testPropagate_a();
    testPropagate_not_not_a();
    testPropagate_a_and_b();
    testPropagate_b_or_c_or_a();
    testPropagate_crc_1();
    testPropagate_intrange_3();
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

  private void testPropagate(final String[] inputs, final String[] outputs)
    throws EvalException, ParseException
  {
    final ConstraintList constraints = parse(inputs);
    // System.err.println(constraints);
    final ConstraintList expected = parse(outputs);
    mPropagator.init(constraints);
    mPropagator.propagate();
    final ConstraintList result = mPropagator.getAllConstraints();
    assertEquals("Wrong output from constraint propagator!", expected, result);
  }

  private void testPropagate(final String[] inputs1,
                             final String[] inputs2,
                             final String[] outputs)
    throws EvalException, ParseException
  {
    testPropagate(inputs1, inputs2, outputs, null);
  }

  private void testPropagate(final String[] inputs1,
                             final String[] inputs2,
                             final String[] outputs,
                             final String recall)
    throws EvalException, ParseException
  {
    final ConstraintList constraints1 = parse(inputs1);
    final ConstraintList constraints2 = parse(inputs2);
    final ConstraintList expected = parse(outputs);
    mPropagator.init(constraints1);
    mPropagator.propagate();
    final ConstraintPropagator propagator2 =
      new ConstraintPropagator(mPropagator);
    if (recall != null) {
      final SimpleExpressionProxy varname = mParser.parse(recall);
      final SimpleExpressionProxy eqn = propagator2.recallBinding(varname);
    }
    propagator2.addConstraints(constraints2);
    propagator2.propagate();
    final ConstraintList result = propagator2.getAllConstraints();
    assertEquals("Wrong output from constraint propagator!", expected, result);
  }

  private ConstraintList parse(final String[] inputs)
    throws ParseException
  {
    if (inputs == null) {
      return null;
    } else {
      final Comparator<SimpleExpressionProxy> comparator =
        mPropagator.getListComparator();
      final List<SimpleExpressionProxy> list =
        new ArrayList<SimpleExpressionProxy>(inputs.length);
      for (final String input : inputs) {
        final SimpleExpressionProxy expr = mParser.parse(input);
        list.add(expr);
      }
      Collections.sort(list, comparator);
      return new ConstraintList(list);
    }
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
  {
    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    mContext = new DummyContext();
    mParser = new ExpressionParser(factory, optable);
    mPropagator = new ConstraintPropagator(factory, optable, mContext);
  }

  protected void tearDown()
  {
    mContext = null;
    mParser = null;
    mPropagator = null;
  }


  //#########################################################################
  //# Data Members
  private DummyContext mContext;
  private ExpressionParser mParser;
  private ConstraintPropagator mPropagator;


  //#########################################################################
  //# Class Constants
  private static final CompiledIntRange BOOLEAN_RANGE =
    new CompiledIntRange(0, 1);

}
