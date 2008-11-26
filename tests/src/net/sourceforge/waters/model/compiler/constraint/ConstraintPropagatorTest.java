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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorByContents;
import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.CompiledEnumRange;
import net.sourceforge.waters.model.compiler.context.CompiledIntRange;
import net.sourceforge.waters.model.compiler.context.ModuleBindingContext;
import net.sourceforge.waters.model.compiler.context.VariableContext;
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
    String[] constraints = {"a"};
    testPropagate(constraints, constraints);
  }

  public void testPropagate_not_a()
    throws EvalException, ParseException
  {
    String[] constraints = {"!a"};
    testPropagate(constraints, constraints);
  }

  public void testPropagate_not_not_a()
    throws EvalException, ParseException
  {
    String[] constraints = {"!!a"};
    String[] expected = {"a"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_a_and_b()
    throws EvalException, ParseException
  {
    String[] constraints = {"a & b"};
    String[] expected = {"a", "b"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_a_or_b()
    throws EvalException, ParseException
  {
    String[] constraints = {"a | b"};
    testPropagate(constraints, constraints);
  }

  public void testPropagate_b_or_a()
    throws EvalException, ParseException
  {
    String[] constraints = {"b | a"};
    String[] expected = {"a | b"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_b_or_c_or_a()
    throws EvalException, ParseException
  {
    String[] constraints = {"b | c | a"};
    String[] expected = {"a | b | c"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_not_a_or_b()
    throws EvalException, ParseException
  {
    String[] constraints = {"!(a | b)"};
    String[] expected = {"!a", "!b"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_not_a_or_not_b()
    throws EvalException, ParseException
  {
    String[] constraints = {"!(a | !b)"};
    String[] expected = {"!a", "b"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_not_a_and_b()
    throws EvalException, ParseException
  {
    String[] constraints = {"!(a & b)"};
    String[] expected = {"!a | !b"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_crc_1()
    throws EvalException, ParseException
  {
    final CompiledEnumRange range =
      createEnumRange(new String[] {"ok", "nok"});
    addVariable("crc", range);
    String[] constraints = {"crc==nok", "crc==ok | trouble"};
    String[] expected = {"crc==nok", "trouble"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_crc_2()
    throws EvalException, ParseException
  {
    final CompiledEnumRange range =
      createEnumRange(new String[] {"ok", "nok"});
    addVariable("crc", range);
    String[] constraints = {"crc!=ok", "crc==ok | trouble"};
    String[] expected = {"crc==nok", "trouble"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_intrange_1()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("x", range);
    String[] constraints = {"x<=5"};
    String[] expected = {};
    testPropagate(constraints, expected);
  }

  public void testPropagate_intrange_2()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("x", range);
    String[] constraints = {"x>5"};
    testPropagate(constraints, null);
  }

  public void testPropagate_intrange_3()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("x", range);
    String[] constraints = {"x>=2 & x<3"};
    String[] expected = {"x==2"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_intrange_4()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("x", range);
    String[] constraints = {"x>1 & x<3"};
    String[] expected = {"x==2"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_intrange_5()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("x", range);
    String[] constraints = {"x>1 & x<1"};
    testPropagate(constraints, null);
  }

  public void testPropagate_intrange_6()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("x", range);
    String[] constraints = {"x<4 & x<2"};
    String[] expected = {"x<=1"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_intrange_7()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("x", range);
    addVariable("y", range);
    String[] constraints = {"x>1 & x==y & y<=2"};
    String[] expected = {"x==2", "y==2"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_balllift_1()
    throws EvalException, ParseException
  {
    addBooleanVariable("qUp");
    addBooleanVariable("qUp'");
    addBooleanVariable("c_iBallDn");
    addBooleanVariable("c_iBallUp");
    String[] constraints = {"qUp'",
                            "qUp' == ((qUp | c_iBallDn) & c_iBallUp)"};
    String[] expected = {"qUp'", "c_iBallDn | qUp", "c_iBallUp"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_balllift_2()
    throws EvalException, ParseException
  {
    addBooleanVariable("qUp");
    addBooleanVariable("qUp'");
    addBooleanVariable("c_iBallDn");
    addBooleanVariable("c_iBallUp");
    String[] constraints = {"!qUp'",
                            "qUp' == ((qUp | c_iBallDn) & c_iBallUp)"};
    String[] expected = {"!qUp'", "!c_iBallUp | !(qUp | c_iBallDn)"};
    testPropagate(constraints, expected);
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
    final List<SimpleExpressionProxy> constraints = parse(inputs);
    // System.err.println(constraints);
    final List<SimpleExpressionProxy> expected = parse(outputs);
    final boolean unchanged = isEqualList(constraints, expected);
    mPropagator.init(constraints);
    final boolean retval = mPropagator.propagate();
    final List<SimpleExpressionProxy> result = mPropagator.getConstraints();
    final Comparator<SimpleExpressionProxy> comparator =
      mPropagator.getExpressionComparator();
    if (result != null) {
      Collections.sort(result, comparator);
    }
    if (expected != null) {
      Collections.sort(expected, comparator);
    }
    assertTrue("Wrong output from constraint propagator: got " +
               result + " but should have been " + expected + "!",
               isEqualList(result, expected));
    assertEquals("Wrong return value from constraint propagator: got " +
                 retval + " but should have been " + (!unchanged) + "!",
                 !unchanged, retval);
  }

  private List<SimpleExpressionProxy> parse(final String[] inputs)
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
      return list;
    }
  }

  private boolean isEqualList(final List<SimpleExpressionProxy> list1,
                              final List<SimpleExpressionProxy> list2)
  {
    if (list1 == null) {
      return list2 == null;
    } else if (list2 == null) {
      return false;
    } else if (list1.size() == list2.size()) {
      final Iterator<SimpleExpressionProxy> iter1 = list1.iterator();
      final Iterator<SimpleExpressionProxy> iter2 = list2.iterator();
      while (iter1.hasNext()) {
        final SimpleExpressionProxy expr1 = iter1.next();
        final SimpleExpressionProxy expr2 = iter2.next();
        if (!expr1.equalsByContents(expr2)) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }      


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
  {
    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    mContext = new TestContext();
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
  //# Inner Class TestContext
  private class TestContext implements VariableContext
  {

    //#######################################################################
    //# Constructor
    private TestContext()
    {
      mAtoms = new ProxyAccessorHashMapByContents<IdentifierProxy>();
      mRangeMap =
        new HashMap<ProxyAccessor<SimpleExpressionProxy>,CompiledRange>();
    }


    //#######################################################################
    //# Interface net.sourceforge.waters.model.compiler.context.BindingContext
    public SimpleExpressionProxy getBoundExpression
      (final SimpleExpressionProxy ident)
    {
      return null;
    }

    public boolean isEnumAtom(final IdentifierProxy ident)
    {
      return mAtoms.containsProxy(ident);
    }

    public ModuleBindingContext getModuleBindingContext()
    {
      return null;
    }


    //#######################################################################
    //# Interface net.sourceforge.waters.model.compiler.context.VariableContext
    public CompiledRange getVariableRange(final SimpleExpressionProxy varname)
    {
      final ProxyAccessor<SimpleExpressionProxy> accessor =
        new ProxyAccessorByContents<SimpleExpressionProxy>(varname);
      return getVariableRange(accessor);
    }

    public CompiledRange getVariableRange
      (final ProxyAccessor<SimpleExpressionProxy> accessor)
    {
      return mRangeMap.get(accessor);
    }

    public Collection<SimpleExpressionProxy> getVariableNames()
    {
      return new VariableNameSet(mRangeMap);
    }


    //#######################################################################
    //# Assignments
    private void addAtom(final IdentifierProxy ident)
    {
      mAtoms.addProxy(ident);
    }

    private void addVariable(final SimpleExpressionProxy varname,
                             final CompiledRange range)
    {
      final ProxyAccessor<SimpleExpressionProxy> accessor =
        new ProxyAccessorByContents<SimpleExpressionProxy>(varname);
      mRangeMap.put(accessor, range);
    }

    //#######################################################################
    //# Data Members
    private final ProxyAccessorMap<IdentifierProxy> mAtoms;
    private final Map<ProxyAccessor<SimpleExpressionProxy>,CompiledRange>
      mRangeMap;

  }


  //#########################################################################
  //# Inner Class VariableNameSet
  private class VariableNameSet
    extends AbstractSet<SimpleExpressionProxy>
  {

    //#######################################################################
    //# Constructor
    VariableNameSet
      (final Map<ProxyAccessor<SimpleExpressionProxy>,CompiledRange> map)
    {
      mMap = map;
    }

    //#######################################################################
    //# Interface java.util.Set
    public int size()
    {
      return mMap.size();
    }

    public Iterator<SimpleExpressionProxy> iterator()
    {
      return new VariableNameIterator(mMap.keySet().iterator());
    }

    //#######################################################################
    //# Data Members
    private final Map<ProxyAccessor<SimpleExpressionProxy>,CompiledRange> mMap;

  }


  //#########################################################################
  //# Inner Class VariableNameIterator
  private class VariableNameIterator
    implements Iterator<SimpleExpressionProxy>
  {

    //#######################################################################
    //# Constructor
    VariableNameIterator
      (final Iterator<ProxyAccessor<SimpleExpressionProxy>> master)
    {
      mMaster = master;
    }

    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return mMaster.hasNext();
    }

    public SimpleExpressionProxy next()
    {
      final ProxyAccessor<SimpleExpressionProxy> accessor = mMaster.next();
      return accessor.getProxy();
    }

    public void remove()
    {
      throw new UnsupportedOperationException
        ("Can't remove variables through variable name iteration!");
    }

    //#######################################################################
    //# Data Members
    private final Iterator<ProxyAccessor<SimpleExpressionProxy>> mMaster;

  }


  //#########################################################################
  //# Data Members
  private TestContext mContext;
  private ExpressionParser mParser;
  private ConstraintPropagator mPropagator;


  //#########################################################################
  //# Class Constants
  private static final CompiledIntRange BOOLEAN_RANGE =
    new CompiledIntRange(0, 1);

}
