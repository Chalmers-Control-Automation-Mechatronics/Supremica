//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   ArrayListSubjectTest
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.subject.base;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;


public class ArrayListSubjectTest extends AbstractWatersTest
{

  //#########################################################################
  //# Overrides for junit.framework.TestCase
  public static Test suite()
  {
    return new TestSuite(ArrayListSubjectTest.class);
  }

  public static void main(final String args[])
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Test Cases
  public void testAssign0()
  {
    final String[] array = {"A", "G", "C", "A", "T"};
    final ArrayListSubject<SimpleIdentifierSubject> list =
      createListSubject(array);
    final UndoInfo undo = list.createUndoInfo(list);
    assertNull("Got non-null assignment for unchanged lists!", undo);
  }

  public void testAssign1()
  {
    final String[] array1 = new String[0];
    final String[] array2 = {"G", "A", "C"};
    assign(array1, array2);
  }

  public void testAssign2()
  {
    final String[] array1 = {"A", "G", "C", "A", "T"};
    final String[] array2 = new String[0];
    assign(array1, array2);
  }

  public void testAssign3()
  {
    final String[] array1 = {"A", "G", "C", "A", "T"};
    final String[] array2 = {"G", "A", "C"};
    assign(array1, array2);
  }

  public void testAssign4()
  {
    final String[] array1 = {"G", "A", "C"};
    final String[] array2 = {"A", "G", "C", "A", "T"};
    assign(array1, array2);
  }

  public void testAssign5()
  {
    final String[] array1 = {"A", "G", "C", "A", "T"};
    final String[] array2 = {"A", "G", "X", "X", "T"};
    assign(array1, array2);
  }

  public void testAssign6()
  {
    final String[] array1 = {"A", "G", "C", "A", "T"};
    final String[] array2 = {"A", "G", "X", "Y", "Z", "T"};
    assign(array1, array2);
  }

  public void testAssign7()
  {
    final String[] array1 = {"X", "M", "J", "Y", "A", "U", "Z"};
    final String[] array2 = {"M", "Z", "J", "A", "W", "X", "U"};
    assign(array1, array2);
  }

  public void testAssign8()
  {
    final String[] array1 = {"M", "Z", "J", "A", "W", "X", "U"};
    final String[] array2 = {"X", "M", "J", "Y", "A", "U", "Z"};
    assign(array1, array2);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void assign(final String[] oldArray, final String[] newArray)
  {
    final ArrayListSubject<SimpleIdentifierSubject> oldList =
      createListSubject(oldArray);
    final List<SimpleIdentifierSubject> oldCopy =
      new ArrayList<SimpleIdentifierSubject>(oldList);
    final ArrayListSubject<SimpleIdentifierSubject> newList =
      createListSubject(newArray);
    final UndoInfo undo = oldList.createUndoInfo(newList);
    undo.redo(oldList);
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(true, true);
    assertProxyListEquals(eq, "Unexpected list contents after assignment!",
                          newList, oldList);
    final List<SimpleIdentifierSubject> newCopy =
      new ArrayList<SimpleIdentifierSubject>(oldList);
    undo.undo(oldList);
    assertEquals("Unexpected list contents after undo!", oldCopy, oldList);
    undo.redo(oldList);
    assertEquals("Unexpected list contents after redo!", newCopy, oldList);
  }

  private ArrayListSubject<SimpleIdentifierSubject> createListSubject
    (final String[] array)
  {
    final ArrayListSubject<SimpleIdentifierSubject> list =
      new ArrayListSubject<SimpleIdentifierSubject>(array.length);
    for (final String name : array) {
      final SimpleIdentifierSubject ident = new SimpleIdentifierSubject(name);
      list.add(ident);
    }
    return list;
  }

  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp()
    throws Exception
  {
    super.setUp();
  }

  @Override
  protected void tearDown()
    throws Exception
  {
    super.tearDown();
  }


  //#########################################################################
  //# Data Members

}
