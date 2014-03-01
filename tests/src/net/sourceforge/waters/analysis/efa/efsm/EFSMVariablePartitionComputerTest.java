//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   EFSMVariablePartitionComputer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import java.util.List;

import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorHashSet;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * A test for the {@link EFSMVariablePartitionComputer}.
 *
 * @author Robi Malik
 */

public class EFSMVariablePartitionComputerTest
  extends AbstractEFSMTest
{

  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  public EFSMVariablePartitionComputerTest()
  {
  }

  public EFSMVariablePartitionComputerTest(final String name)
  {
    super(name);
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final ModuleProxyFactory factory = getModuleProxyFactory();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    mVariablePartitionComputer =
      new EFSMVariablePartitionComputer(factory, optable);
  }

  @Override
  protected void tearDown() throws Exception
  {
    super.tearDown();
    mVariablePartitionComputer = null;
  }


  //#########################################################################
  //# Test Cases
  public void testUnfolding_11() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding11");
    runPartitionComputer(module);
  }

  public void testUnfolding_12() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding12");
    runPartitionComputer(module);
  }

  public void testReentrant() throws Exception
  {
    testUnfolding_11();
    testUnfolding_12();
    testUnfolding_11();
    testUnfolding_12();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void runPartitionComputer(final ModuleProxy module)
    throws Exception
  {
    runPartitionComputer(module, null);
  }

  private void runPartitionComputer(final ModuleProxy module,
                                    final List<ParameterBindingProxy> bindings)
    throws Exception
  {
    final EFSMSystem system = createEFSMSystem(module, bindings);
    final EFSMVariable var = system.getVariables().get(0);
    final TRPartition computedPartition =
      mVariablePartitionComputer.computePartition(var, system);
    final EFSMVariablePartition expectedPartition =
      parseExpectedPartition(module, var);
    expectedPartition.verify(computedPartition);
  }

  private EFSMVariablePartition parseExpectedPartition(final ModuleProxy module,
                                                       final EFSMVariable var)
  {
    final String comment = module.getComment();
    int pos = comment.indexOf(PARTITION);
    if (pos < 0) {
      fail("No expected partition found in comments of module '" +
           module.getName() + "'!");
    }
    pos += PARTITION.length();
    final EFSMVariablePartition partition = new EFSMVariablePartition(var);
    int classno = 0;
    while (true) {
      pos = scanToCharacter(comment, pos, '[');
      if (pos < 0 || comment.charAt(pos) != '[') {
        break;
      }
      pos++;
      do {
        pos = scanToCharacter(comment, pos, 'a');
        if (pos < 0) {
          break;
        }
        final String name = scanIdentifier(comment, pos);
        assertNotNull("Syntax error in partition description!", name);
        partition.setClass(name, classno);
        pos += name.length();
        pos = scanToCharacter(comment, pos, ']');
      } while (pos >= 0 && comment.charAt(pos++) == ',');
      classno++;
    }
    return partition;
  }

  private int scanToCharacter(final String text, int pos, final char sought)
  {
    while (pos < text.length()) {
      final char ch = text.charAt(pos);
      if (ch == sought) {
        return pos;
      } else if (Character.isWhitespace(ch)) {
        pos++;
      } else {
        return pos;
      }
    }
    return -1;
  }

  private String scanIdentifier(final String text, int pos)
  {
    final StringBuilder buffer = new StringBuilder();
    while (true) {
      final char ch = text.charAt(pos);
      if (Character.isJavaIdentifierPart(ch)) {
        buffer.append(ch);
        pos++;
      } else {
        break;
      }
    }
    if (buffer.length() > 0) {
      return buffer.toString();
    } else {
      return null;
    }
  }


  //#########################################################################
  //# Inner Class EFSMVariablePartition
  private class EFSMVariablePartition
  {

    //#########################################################################
    //# Constructor
    private EFSMVariablePartition(final EFSMVariable var)
    {
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      mRange = var.getRange();
      mStateToClass = new ProxyAccessorHashMap<>(eq, mRange.size());
    }

    //#########################################################################
    //# Initialisation
    private void setClass(final String name, final int classno)
    {
      final SimpleExpressionProxy expr = findInRange(name);
      mStateToClass.putByProxy(expr, classno);
    }

    private SimpleExpressionProxy findInRange(final String name)
    {
      for (final SimpleExpressionProxy state : mRange.getValues()) {
        if (state.toString().equals(name)) {
          return state;
        }
      }
      fail("Undefined variable value '" + name + "' in partition!");
      return null;
    }

    //#########################################################################
    //# Testing
    private void verify(final TRPartition computedPartition)
    {
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      final ProxyAccessorHashSet<SimpleExpressionProxy> unaccounted =
        new ProxyAccessorHashSet<>(eq, mRange.size());
      for (final ProxyAccessor<SimpleExpressionProxy> accessor :
           mStateToClass.keySet()) {
        final SimpleExpressionProxy expr = accessor.getProxy();
        unaccounted.addProxy(expr);
      }
      final List<? extends SimpleExpressionProxy> rangeValues =
        mRange.getValues();
      int classno = 0;
      for (final int[] clazz : computedPartition.getClasses()) {
        for (final int s : clazz) {
          final SimpleExpressionProxy expr = rangeValues.get(s);
          final int expected = mStateToClass.getByProxy(expr);
          assertNotNull("Computed value '" + expr +
                        "' not found in partition!",
                        expected);
          assertEquals("Unexpected class number for value '" + expr + "'!",
                       expected, classno);
          unaccounted.removeProxy(expr);
        }
        classno++;
      }
      if (!unaccounted.isEmpty()) {
        final SimpleExpressionProxy missing = unaccounted.iterator().next();
        fail("Listed value '" + missing + "' not found in computed partition!");
      }
    }

    //#########################################################################
    //# Data Members
    private final CompiledRange mRange;
    private final ProxyAccessorMap<SimpleExpressionProxy,Integer> mStateToClass;

  }


  //#########################################################################
  //# Data Members
  private EFSMVariablePartitionComputer mVariablePartitionComputer;


  //#########################################################################
  //# Class Constants
  private static final String PARTITION = "PARTITION";
}
